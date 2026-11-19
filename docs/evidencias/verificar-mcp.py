"""Comprueba initialize y tools/list contra los tres MCP configurados en Codex."""
import asyncio
import json
import os
from pathlib import Path
import tomllib
from datetime import datetime
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent
CONFIG = tomllib.loads((Path.home() / '.codex/config.toml').read_text())

async def check(name):
    config = CONFIG['mcp_servers'][name]
    result = {'name': name, 'enabled': config.get('enabled', True)}
    process = None
    try:
        if not result['enabled']:
            raise RuntimeError('Servidor deshabilitado')
        with (ROOT / f'mcp-{name}-stderr.log').open('w') as err:
            process = await asyncio.create_subprocess_exec(
                config['command'], *config.get('args', []),
                stdin=asyncio.subprocess.PIPE, stdout=asyncio.subprocess.PIPE,
                stderr=err, env={**os.environ, **config.get('env', {})})
            async def rpc(identifier, method, params):
                process.stdin.write((json.dumps({'jsonrpc':'2.0', 'id':identifier,
                    'method':method, 'params':params}) + '\n').encode())
                await process.stdin.drain()
                while True:
                    line = await asyncio.wait_for(process.stdout.readline(), 45)
                    if not line:
                        raise RuntimeError('El servidor cerró stdout; consultar stderr')
                    value = json.loads(line)
                    if value.get('id') == identifier:
                        if 'error' in value:
                            raise RuntimeError(value['error'])
                        return value['result']
            init = await rpc(1, 'initialize', {'protocolVersion':'2024-11-05',
                'capabilities':{}, 'clientInfo':{'name':'odonto-mcp-check','version':'1.0'}})
            process.stdin.write(b'{"jsonrpc":"2.0","method":"notifications/initialized"}\n')
            await process.stdin.drain()
            tools = await rpc(2, 'tools/list', {})
            result.update(status='OK', server=init.get('serverInfo'),
                tool_count=len(tools.get('tools', [])),
                tools=[t['name'] for t in tools.get('tools', [])])
    except Exception as exc:
        result.update(status='ERROR', error=str(exc) or type(exc).__name__)
    finally:
        if process and process.returncode is None:
            process.terminate()
            try:
                await asyncio.wait_for(process.wait(), 5)
            except asyncio.TimeoutError:
                process.kill()
                await process.wait()
    print(name + ': ' + result['status'], flush=True)
    return result

async def main():
    results = await asyncio.gather(*(check(n) for n in ('engram','context7','playwright')))
    report = {'checked_at': datetime.now(ZoneInfo('America/Argentina/Mendoza')).isoformat(),
        'method':'Clientes MCP independientes: initialize y tools/list. No demuestra carga en el chat de la app.',
        'results': results}
    (ROOT / 'mcp-verificacion.json').write_text(json.dumps(report, ensure_ascii=False, indent=2)+'\n')
    raise SystemExit(0 if all(r['status']=='OK' for r in results) else 1)

if __name__ == '__main__':
    asyncio.run(main())
