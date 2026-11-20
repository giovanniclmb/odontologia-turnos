package ar.edu.utn.odontologia.persistencia;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

final class ProcesoAislado {
    private ProcesoAislado() { }
    record Resultado(int salida, String log) { }

    static ProcessBuilder preparar(List<String> comando, Map<String, String> entorno) {
        var builder = new ProcessBuilder(comando).redirectErrorStream(true);
        builder.environment().clear();
        builder.environment().putAll(entorno);
        return builder;
    }

    static Resultado ejecutar(List<String> comando, Map<String, String> entorno, int segundos) throws Exception {
        return ejecutar(comando, entorno, segundos, null);
    }

    static Resultado ejecutar(List<String> comando, Map<String, String> entorno, int segundos, Path directorio) throws Exception {
        var builder = preparar(comando, entorno);
        if (directorio != null) builder.directory(directorio.toFile());
        Process proceso = builder.start();
        try {
            var salida = CompletableFuture.supplyAsync(() -> {
                try {
                    byte[] bytes = proceso.getInputStream().readNBytes(1_048_577);
                    if (bytes.length > 1_048_576) throw new IllegalStateException("Salida del proceso demasiado extensa");
                    return new String(bytes, StandardCharsets.UTF_8);
                } catch (java.io.IOException error) { throw new CompletionException(error); }
            });
            if (!proceso.waitFor(segundos, TimeUnit.SECONDS)) throw new IllegalStateException("Tiempo máximo del proceso agotado");
            return new Resultado(proceso.exitValue(), salida.get(5, TimeUnit.SECONDS));
        } finally {
            proceso.descendants().filter(ProcessHandle::isAlive).forEach(ProcessHandle::destroyForcibly);
            if (proceso.isAlive()) proceso.destroyForcibly().waitFor(5, TimeUnit.SECONDS);
            proceso.getInputStream().close();
        }
    }
}
