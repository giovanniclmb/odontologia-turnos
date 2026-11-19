package ar.edu.utn.odontologia.acceso.persistencia;

import ar.edu.utn.odontologia.acceso.Rol;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "usuario", schema = "odontologia")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(name = "nombre_usuario", nullable = false, columnDefinition = "text", unique = true)
    private String nombreUsuario;

    @Column(name = "hash_credencial", nullable = false, columnDefinition = "text")
    private String hashCredencial;

    @Column(name = "rol", nullable = false, columnDefinition = "text")
    private String rol;

    @Column(nullable = false)
    private boolean activo;

    @Column(name = "profesional_ref")
    private UUID profesionalRef;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    protected Usuario() {
    }

    public Usuario(String nombreUsuario, String hashCredencial, Rol rol, boolean activo, UUID profesionalRef) {
        this.nombreUsuario = nombreUsuario;
        this.hashCredencial = hashCredencial;
        this.rol = (rol == null ? null : rol.codigo());
        this.activo = activo;
        this.profesionalRef = profesionalRef;
        this.creadoEn = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashCredencial() {
        return hashCredencial;
    }

    public void setHashCredencial(String hashCredencial) {
        this.hashCredencial = hashCredencial;
    }

    public Rol getRol() {
        return rol == null ? null : Rol.desdeCodigo(rol);
    }

    public String getRolCodigo() {
        return rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public UUID getProfesionalRef() {
        return profesionalRef;
    }

    public void setProfesionalRef(UUID profesionalRef) {
        this.profesionalRef = profesionalRef;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }
}
