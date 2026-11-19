#!/usr/bin/env python3
"""Asigna Issues pendientes del reparto aprobado; simula salvo --apply."""
import argparse
import json
import os
from pathlib import Path
import re
import subprocess
import sys

MARKER = re.compile(r'^<!-- odontologia-change:(C-\d{2}) -->\s*$', re.MULTILINE)
LOGIN = r'[A-Za-z0-9][A-Za-z0-9-]*'


def read_plan(changes, contributing):
    team = {}
    for name, login, url_login in re.findall(
        rf'^\|\s*([^|]+?)\s*\|\s*\[@({LOGIN})\]\(https://github.com/({LOGIN})\)\s*\|',
        contributing, re.MULTILINE,
    ):
        if name in team or login.lower() != url_login.lower():
            raise ValueError('Equipo ambiguo en CONTRIBUTING.md')
        team[name] = login
    cards = re.split(r'^### \[(C-\d{2})\].*$', changes, flags=re.MULTILINE)
    plan = {}
    for index in range(1, len(cards), 2):
        change, body = cards[index:index + 2]
        owners = re.findall(r'^- \*\*Responsable\*\*: (.+?)\s*$', body, re.MULTILINE)
        if change in plan or len(owners) != 1 or owners[0] not in team:
            raise ValueError(f'Responsable ausente, duplicado o desconocido: {change}')
        plan[change] = team[owners[0]]
    if not plan:
        raise ValueError('CHANGES.md no contiene un reparto legible')
    return plan


def issue_change(issue):
    matches = MARKER.findall(issue.get('body') or '')
    if len(matches) > 1:
        raise ValueError(f'Issue #{issue["number"]} con múltiples marcadores')
    return matches[0] if matches else None


def synchronize(plan, github, *, dry_run=True):
    writers = {login.lower() for login in github.active_writers()}
    issues = {}
    for issue in github.open_issues():
        if issue.get('pull_request') or issue['state'] != 'open':
            continue
        change = issue_change(issue)
        if change not in plan:
            continue
        if change in issues:
            raise ValueError(f'Dos Issues abiertos representan {change}; no se asignó ninguno')
        issues[change] = issue
    lines = ['Modo: simulación (sin escrituras).' if dry_run else 'Modo: aplicar asignaciones pendientes.']
    for change, issue in sorted(issues.items()):
        login = plan[change]
        number = issue['number']
        if issue['assignees']:
            lines.append(f'{change} / #{number}: conserva su asignación existente.')
            continue
        if login.lower() not in writers:
            lines.append(f'{change} / #{number}: pendiente de acceso activo con escritura de @{login}.')
            continue
        if dry_run:
            lines.append(f'{change} / #{number}: se asignaría a @{login}.')
            continue
        # Volver a leer antes de escribir para respetar cambios manuales durante la ejecución.
        fresh = github.get_issue(number)
        if fresh['state'] != 'open' or fresh['assignees'] or fresh.get('pull_request') or issue_change(fresh) != change:
            lines.append(f'{change} / #{number}: cambió durante la ejecución; se conserva.')
            continue
        updated = github.add_assignee(number, login)
        if login.lower() not in {a['login'].lower() for a in updated['assignees']}:
            raise RuntimeError(f'GitHub no confirmó la asignación de {change} / #{number}')
        lines.append(f'{change} / #{number}: asignado a @{login}.')
    return lines


class GitHub:
    def __init__(self, repository):
        if not re.fullmatch(r'[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+', repository):
            raise ValueError('Repositorio inválido; usar owner/repo')
        self.base = f'repos/{repository}'

    def request(self, path, *, data=None, paginated=False):
        command = ['gh', 'api', '--method', 'POST' if data is not None else 'GET', f'{self.base}/{path}']
        if paginated:
            command += ['--paginate', '--slurp']
        if data is not None:
            command += ['--input', '-']
        result = subprocess.run(command, input=json.dumps(data) if data is not None else None,
                                text=True, capture_output=True, timeout=90, check=False)
        if result.returncode:
            raise RuntimeError(f'GitHub API falló ({result.returncode}) al consultar {path.split("?")[0]}')
        value = json.loads(result.stdout)
        return [item for page in value for item in page] if paginated else value

    def active_writers(self):
        return [user['login'] for user in self.request('collaborators?per_page=100', paginated=True)
                if user.get('permissions', {}).get('push') is True]

    def open_issues(self):
        return self.request('issues?state=open&per_page=100', paginated=True)

    def get_issue(self, number):
        return self.request(f'issues/{int(number)}')

    def add_assignee(self, number, login):
        # POST añade; nunca usa PATCH que reemplace el conjunto de responsables.
        return self.request(f'issues/{int(number)}/assignees', data={'assignees': [login]})


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply', action='store_true', help='Aplicar; por defecto solo simula')
    parser.add_argument('--repo', default=os.environ.get('GITHUB_REPOSITORY'), help='owner/repo')
    args = parser.parse_args()
    if not args.repo:
        parser.error('Indicar --repo o GITHUB_REPOSITORY')
    root = Path(__file__).resolve().parent.parent
    try:
        plan = read_plan((root / 'CHANGES.md').read_text(), (root / 'CONTRIBUTING.md').read_text())
        lines = synchronize(plan, GitHub(args.repo), dry_run=not args.apply)
        summary = '# Asignación del roadmap\n\n' + '\n'.join(f'- {line}' for line in lines) + '\n'
        print(summary)
        if os.environ.get('GITHUB_STEP_SUMMARY'):
            with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as output:
                output.write(summary)
    except (ValueError, RuntimeError, OSError, subprocess.TimeoutExpired) as error:
        print(f'No se pudo completar la asignación: {error}', file=sys.stderr)
        return 1
    return 0


if __name__ == '__main__':
    sys.exit(main())
