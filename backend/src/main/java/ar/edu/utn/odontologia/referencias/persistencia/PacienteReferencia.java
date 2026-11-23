package ar.edu.utn.odontologia.referencias.persistencia;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name="paciente",schema="odontologia")
public class PacienteReferencia extends Referencia {
    @Column(nullable = false, columnDefinition = "text")
    private String nombre;
    @Column(nullable = false, columnDefinition = "text")
    private String apellido;
    @Column(nullable = false, columnDefinition = "text")
    private String email;
    @Column(columnDefinition = "text")
    private String telefono;

    protected PacienteReferencia() {}
    public PacienteReferencia(String codigo, String nombre, String apellido, String email, String telefono) {
        super(codigo);
        actualizarDatos(nombre, apellido, email, telefono);
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }

    public void actualizarDatos(String nombre, String apellido, String email, String telefono) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.telefono = telefono;
    }
}
