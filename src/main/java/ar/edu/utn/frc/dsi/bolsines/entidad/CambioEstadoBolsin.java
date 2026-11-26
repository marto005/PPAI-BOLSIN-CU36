package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class CambioEstadoBolsin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;

    @ManyToOne(optional = false)
    private Estado estado;

    protected CambioEstadoBolsin() {
    }

    public CambioEstadoBolsin(LocalDateTime fechaHoraInicio, Estado estado) {
        this.fechaHoraInicio = fechaHoraInicio;
        this.estado = estado;
    }

    /** Es el cambio de estado actual si todavía no tiene fecha y hora de fin. */
    public boolean sosActual() {
        return fechaHoraFin == null;
    }

    public boolean esEnviado() {
        return estado.sosEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }

    public Estado getEstado() {
        return estado;
    }
}
