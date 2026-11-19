"""Pruebas de decisiones y efectos de asignación; GitHub se sustituye solo en su frontera."""
import copy
import importlib.util
from pathlib import Path
import unittest

SCRIPT = Path(__file__).resolve().parents[1] / 'assign_roadmap.py'


class AssignmentTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.module = None
        if SCRIPT.exists():
            spec = importlib.util.spec_from_file_location('assign_roadmap', SCRIPT)
            cls.module = importlib.util.module_from_spec(spec)
            spec.loader.exec_module(cls.module)

    def setUp(self):
        self.assertIsNotNone(self.module, 'Falta el asignador del roadmap aprobado')

    def test_reads_owners_from_cards_and_logins_from_team(self):
        changes = '### [C-03] `acceso`\n- **Responsable**: Fabi\n### [C-04] `pacientes`\n- **Responsable**: Sofia\n'
        team = '| Fabi | [@FabianoRamos05](https://github.com/FabianoRamos05) | Escritura |\n| Sofia | [@jowy0](https://github.com/jowy0) | Escritura |'
        self.assertEqual(self.module.read_plan(changes, team), {'C-03': 'FabianoRamos05', 'C-04': 'jowy0'})

    def test_rejects_ambiguous_or_unmapped_plan_before_writing(self):
        team = '| Fabi | [@FabianoRamos05](https://github.com/FabianoRamos05) | Escritura |'
        for changes in ['', '### [C-03] `acceso`\n', '### [C-03] `acceso`\n- **Responsable**: Otro\n', ('### [C-03] `acceso`\n- **Responsable**: Fabi\n') * 2]:
            with self.subTest(changes=changes), self.assertRaises(ValueError):
                self.module.read_plan(changes, team)

    def test_assigns_only_active_writer_preserving_existing_owners_closed_and_prs(self):
        api = FakeGitHub([
            issue(4, 'C-03'), issue(5, 'C-04'), issue(6, 'C-05', ['otro']),
            issue(7, 'C-06', ['Giovanni']), issue(8, 'C-07', state='closed'),
            {**issue(9, 'C-08'), 'pull_request': {'url': 'https://example.invalid/pr/9'}},
        ], ['FabianoRamos05', 'Giovanni'])
        plan = {'C-03': 'FabianoRamos05', 'C-04': 'jowy0', 'C-05': 'FabianoRamos05', 'C-06': 'giovanni', 'C-07': 'FabianoRamos05', 'C-08': 'FabianoRamos05'}
        self.module.synchronize(plan, api, dry_run=False)
        self.assertEqual(api.assigned, [(4, 'FabianoRamos05')])
        self.assertEqual(api.issues[6]['assignees'], [{'login': 'otro'}])

    def test_dry_run_performs_no_writes_and_rerun_is_idempotent(self):
        api = FakeGitHub([issue(4, 'C-03')], ['FabianoRamos05'])
        plan = {'C-03': 'FabianoRamos05'}
        result = self.module.synchronize(plan, api, dry_run=True)
        self.assertEqual(api.assigned, [])
        self.assertIn('simulación', '\n'.join(result).lower())
        self.module.synchronize(plan, api, dry_run=False)
        self.module.synchronize(plan, api, dry_run=False)
        self.assertEqual(api.assigned, [(4, 'FabianoRamos05')])

    def test_duplicate_marker_blocks_entire_run_before_any_write(self):
        api = FakeGitHub([issue(4, 'C-03'), issue(5, 'C-04'), issue(6, 'C-04')], ['FabianoRamos05'])
        with self.assertRaises(ValueError):
            self.module.synchronize({'C-03': 'FabianoRamos05', 'C-04': 'FabianoRamos05'}, api, dry_run=False)
        self.assertEqual(api.assigned, [])

    def test_rechecks_issue_to_preserve_assignment_made_during_run(self):
        api = FakeGitHub([issue(4, 'C-03')], ['FabianoRamos05'])
        api.on_refresh = lambda: api.issues[4].update(assignees=[{'login': 'otro'}])
        self.module.synchronize({'C-03': 'FabianoRamos05'}, api, dry_run=False)
        self.assertEqual(api.assigned, [])

    def test_unmarked_and_unknown_changes_are_not_assigned(self):
        api = FakeGitHub([issue(4, 'C-99'), {**issue(5, 'C-03'), 'body': 'Sin marcador'}], ['FabianoRamos05'])
        self.module.synchronize({'C-03': 'FabianoRamos05'}, api, dry_run=False)
        self.assertEqual(api.assigned, [])

    def test_github_read_error_is_not_treated_as_invitation_pending(self):
        api = FakeGitHub([issue(4, 'C-03')], ['FabianoRamos05'])
        def unavailable():
            raise RuntimeError('GitHub no disponible')
        api.active_writers = unavailable
        with self.assertRaises(RuntimeError):
            self.module.synchronize({'C-03': 'FabianoRamos05'}, api, dry_run=False)
        self.assertEqual(api.assigned, [])

    def test_real_repository_plan_has_25_changes_and_approved_owners(self):
        root = SCRIPT.parent.parent
        plan = self.module.read_plan((root / 'CHANGES.md').read_text(), (root / 'CONTRIBUTING.md').read_text())
        self.assertEqual(len(plan), 25)
        self.assertEqual(plan['C-03'], 'FabianoRamos05')
        self.assertEqual(plan['C-04'], 'jowy0')
        self.assertEqual(plan['C-05'], 'Dreamboy2006')
        self.assertEqual(plan['C-06'], 'giovanniclmb')


def issue(number, change, owners=(), state='open'):
    return {'number': number, 'body': f'<!-- odontologia-change:{change} -->', 'state': state,
            'assignees': [{'login': login} for login in owners]}


class FakeGitHub:
    def __init__(self, issues, writers):
        self.issues = {i['number']: copy.deepcopy(i) for i in issues}
        self.writers = writers
        self.assigned = []
        self.on_refresh = lambda: None

    def open_issues(self):
        return copy.deepcopy(list(self.issues.values()))

    def active_writers(self):
        return self.writers

    def get_issue(self, number):
        self.on_refresh()
        return copy.deepcopy(self.issues[number])

    def add_assignee(self, number, login):
        self.assigned.append((number, login))
        self.issues[number]['assignees'].append({'login': login})
        return copy.deepcopy(self.issues[number])


if __name__ == '__main__':
    unittest.main()
