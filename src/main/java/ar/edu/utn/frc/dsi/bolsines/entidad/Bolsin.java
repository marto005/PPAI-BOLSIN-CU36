package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Bolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private int numeroBolsin;

    private String numeroPrecinto;
    private LocalDate fecha;
    /** Peso expresado en gramos. */
    private double peso;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_origen_id")
    private ComisionMedica origen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cm_destino_id")
    private ComisionMedica destino;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "bolsin_id")
    @OrderBy("fechaHoraInicio ASC")
    private List<CambioEstadoBolsin> cambioEstado = new ArrayList<>();

    protected Bolsin() {
    }

    public Bolsin(int numeroBolsin, String numeroPrecinto, LocalDate fecha, double peso,
                  ComisionMedica origen, ComisionMedica destino) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.fecha = fecha;
        this.peso = peso;
        this.origen = origen;
        this.destino = destino;
    }

    // ---------------------------------------------------------------- CU 36

    public boolean esEnviado() {
        CambioEstadoBolsin actual = obtenerEstadoActualBolsin();
        return actual != null && actual.esEnviado();
    }

    public CambioEstadoBolsin obtenerEstadoActualBolsin() {
        for (CambioEstadoBolsin cambio : cambioEstado) {
            if (cambio.sosActual()) {
                return cambio;
            }
        }
        return null;
    }

    public boolean sosComisionMedicaUsuarioLogueado(ComisionMedica cmUsuarioLogueado) {
        return origen.sosComisionMedicaUsuarioLog(cmUsuarioLogueado);
    }

    public int getNroBolsin() {
        return numeroBolsin;
    }

    public ComisionMedica buscarCMDestino() {
        return destino;
    }

    // ------------------------------------------------- cambio de estado

    /** Cierra el cambio de estado actual y crea uno nuevo (usado por la carga de datos). */
    public void cambiarEstado(Estado nuevoEstado, LocalDateTime fechaHora) {
        CambioEstadoBolsin actual = obtenerEstadoActualBolsin();
        if (actual != null) {
            actual.setFechaHoraFin(fechaHora);
        }
        cambioEstado.add(new CambioEstadoBolsin(fechaHora, nuevoEstado));
    }

    public String getNombreEstadoActual() {
        CambioEstadoBolsin actual = obtenerEstadoActualBolsin();
        return actual == null ? "-" : actual.getEstado().getNombre();
    }

    public Long getId() {
        return id;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getPeso() {
        return peso;
    }

    public ComisionMedica getOrigen() {
        return origen;
    }

    public List<CambioEstadoBolsin> getCambioEstado() {
        return cambioEstado;
    }
}
