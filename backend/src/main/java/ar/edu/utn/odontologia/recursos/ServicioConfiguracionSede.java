package ar.edu.utn.odontologia.recursos;

import ar.edu.utn.odontologia.acceso.AutorizacionDenegadaException;
import ar.edu.utn.odontologia.acceso.Identidad;
import ar.edu.utn.odontologia.acceso.Permiso;
import ar.edu.utn.odontologia.acceso.ServicioAutorizacion;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ServicioConfiguracionSede {
    private final JdbcClient jdbc;
    private final ServicioAutorizacion autorizacion;

    public ServicioConfiguracionSede(DataSource datos, ServicioAutorizacion autorizacion) {
        this.jdbc = JdbcClient.create(datos);
        this.autorizacion = autorizacion;
    }

    public Optional<Integer> consultarIntervalo(Identidad solicitante) {
        if (solicitante == null || solicitante.rol() == null) {
            throw new AutorizacionDenegadaException();
        }
        return jdbc.sql("select intervalo_minutos from odontologia.configuracion_sede where fila = 1")
                .query(Integer.class).optional();
    }

    public void establecerIntervalo(Identidad solicitante, Integer minutos) {
        autorizacion.exigir(solicitante, Permiso.CONFIG_INTERVALO);
        if (minutos == null || minutos <= 0) {
            throw new IllegalArgumentException("El intervalo debe ser un entero positivo");
        }
        int modificadas = jdbc.sql(
                "update odontologia.configuracion_sede set intervalo_minutos = :minutos where fila = 1")
                .param("minutos", minutos).update();
        if (modificadas != 1) {
            throw new IllegalStateException("Falta la configuración de la sede");
        }
    }
}
