package ar.edu.utn.odontologia.acceso.persistencia;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="historial_contrasena",schema="odontologia")
public class HistorialContrasena {
    @Id @GeneratedValue(strategy=GenerationType.UUID)
    private UUID id;
    @Column(name="usuario_id",nullable=false)
    private UUID usuarioId;
    @Column(nullable=false)
    private long secuencia;
    @Column(name="hash_credencial",nullable=false,columnDefinition="text")
    private String hashCredencial;
    @Column(name="creado_en",nullable=false)
    private Instant creadoEn;
    protected HistorialContrasena() { }
    public HistorialContrasena(UUID usuarioId,long secuencia,String hashCredencial) {
        this.usuarioId=usuarioId;this.secuencia=secuencia;this.hashCredencial=hashCredencial;this.creadoEn=Instant.now();
    }
    public long getSecuencia(){return secuencia;}
    public String getHashCredencial(){return hashCredencial;}
}
