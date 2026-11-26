package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Cargo {

    public static final String ENCARGADO_DE_BOLSINES = "Encargado de Bolsines";
    public static final String GERENTE_DE_CM = "Gerente de Comisión Médica";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String descripcion;

    protected Cargo() {
    }

    public Cargo(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean sosGerente() {
        return GERENTE_DE_CM.equalsIgnoreCase(nombre);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
