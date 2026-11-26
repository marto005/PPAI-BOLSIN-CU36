package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String email;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_asignada_id")
    private ComisionMedica asignadaA;

    @ManyToOne(optional = false)
    private Cargo cargo;

    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    protected Empleado() {
    }

    public Empleado(String nombre, String apellido, String email, ComisionMedica asignadaA, Cargo cargo, Usuario usuario) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.asignadaA = asignadaA;
        this.cargo = cargo;
        this.usuario = usuario;
    }

    public boolean esTuUsuario(Usuario usuarioLogueado) {
        return usuario != null && usuarioLogueado != null && usuario.getId().equals(usuarioLogueado.getId());
    }

    public ComisionMedica obtenerComisionMedica() {
        return asignadaA;
    }

    public boolean esTuComisionMedica(ComisionMedica comisionMedica) {
        return asignadaA.getCodigo().equalsIgnoreCase(comisionMedica.getCodigo());
    }

    public boolean sosGerenteDeCM() {
        return cargo.sosGerente();
    }

    public String getCorreo() {
        return email;
    }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public Cargo getCargo() {
        return cargo;
    }
}
