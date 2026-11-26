package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ComisionMedica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código con el que la identifica el GPS Tracker (por ejemplo CM-CBA-01). */
    @Column(unique = true, nullable = false)
    private String codigo;

    private String nombre;
    private String direccion;
    private String email;
    private String telefono;

    protected ComisionMedica() {
    }

    public ComisionMedica(String codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
    }

    public boolean sosComisionMedicaUsuarioLog(ComisionMedica cmUsuarioLogueado) {
        return cmUsuarioLogueado != null && codigo.equalsIgnoreCase(cmUsuarioLogueado.getCodigo());
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getEmail() {
        return email;
    }

    public String getTelefono() {
        return telefono;
    }
}
