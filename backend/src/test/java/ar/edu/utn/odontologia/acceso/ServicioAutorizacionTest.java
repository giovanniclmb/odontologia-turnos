package ar.edu.utn.odontologia.acceso;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ServicioAutorizacionTest {
    private final ServicioAutorizacion authz = new ServicioAutorizacion();

    @Test void escriturasAdministrativasSonExclusivasDeRecepcion() {
        Identidad recepcion = new Identidad(UUID.randomUUID(), "recepcion-ficticia", Rol.RECEPCION, null);
        Identidad odontologo = new Identidad(UUID.randomUUID(), "odontologo-ficticio", Rol.ODONTOLOGO, null);
        for (Permiso permiso : java.util.List.of(Permiso.CONFIG_INTERVALO, Permiso.CONFIG_RECURSOS,
                Permiso.USUARIO_CREAR_ASIGNAR)) {
            assertTrue(authz.tienePermiso(recepcion, permiso), permiso::codigo);
            assertFalse(authz.tienePermiso(odontologo, permiso), permiso::codigo);
            assertFalse(authz.tienePermiso(null, permiso), permiso::codigo);
        }
    }

    @Test void permisoConcedido() {
        Identidad id = new Identidad(UUID.randomUUID(), "u", Rol.RECEPCION, null);
        assertTrue(authz.tienePermiso(id, Permiso.FICHA_LEER));
        assertTrue(authz.tienePermiso(id, Permiso.PACIENTE_DATOS_ADMIN));
        assertFalse(authz.tienePermiso(id, Permiso.PACIENTE_ELIMINAR));
        Identidad odontologo = new Identidad(UUID.randomUUID(), "o", Rol.ODONTOLOGO, UUID.randomUUID());
        assertTrue(authz.tienePermiso(odontologo, Permiso.PACIENTE_DATOS_ADMIN));
        assertFalse(authz.tienePermiso(odontologo, Permiso.PACIENTE_ELIMINAR));
        assertFalse(authz.tienePermiso(new Identidad(UUID.randomUUID(), "sin-rol", null, null), Permiso.FICHA_LEER));
    }

    @Test void permisoDenegado() {
        Identidad id = new Identidad(UUID.randomUUID(), "u", Rol.RECEPCION, null);
        assertFalse(authz.tienePermiso(id, Permiso.FICHA_EDITAR));
        assertThrows(AutorizacionDenegadaException.class, () -> authz.exigir(id, Permiso.FICHA_EDITAR));
    }

    @Test void anonimoDenegado() {
        assertFalse(authz.tienePermiso(null, Permiso.FICHA_LEER));
        assertThrows(AutorizacionDenegadaException.class, () -> authz.exigir(null, Permiso.FICHA_LEER));
    }

    @Test void catalogoRolesSinSuperadmin() {
        assertEquals(2, Rol.values().length);
        assertThrows(IllegalArgumentException.class, () -> Rol.desdeCodigo("admin"));
    }

    @Test void alcanceDenyByDefault() {
        UUID prof = UUID.randomUUID();
        Identidad odontoSinVinculo = new Identidad(UUID.randomUUID(), "o", Rol.ODONTOLOGO, null);
        assertFalse(authz.puedeConsultarPaciente(odontoSinVinculo, prof));
        Identidad odontoVinculado = new Identidad(UUID.randomUUID(), "o", Rol.ODONTOLOGO, prof);
        assertTrue(authz.puedeConsultarPaciente(odontoVinculado, prof));
        assertFalse(authz.puedeConsultarPaciente(odontoVinculado, UUID.randomUUID()));
        Identidad recep = new Identidad(UUID.randomUUID(), "r", Rol.RECEPCION, null);
        assertTrue(authz.puedeConsultarPaciente(recep, prof));
    }

    @Test void separacionClinica() {
        Identidad recep = new Identidad(UUID.randomUUID(), "r", Rol.RECEPCION, null);
        Identidad odonto = new Identidad(UUID.randomUUID(), "o", Rol.ODONTOLOGO, null);
        assertTrue(authz.tienePermiso(recep, Permiso.FICHA_LEER));
        assertFalse(authz.tienePermiso(recep, Permiso.FICHA_EDITAR));
        assertTrue(authz.tienePermiso(odonto, Permiso.FICHA_LEER));
        assertTrue(authz.tienePermiso(odonto, Permiso.FICHA_EDITAR));
    }
}
