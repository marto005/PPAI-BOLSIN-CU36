package ar.edu.utn.frc.dsi.bolsines.gps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Patrón Adapter - rol ADAPTADOR (Adapter) para el GPS Tracker GeoPulse MTR-900.
 * <p>
 * El GeoPulse MTR-900 identifica al bolsín solo por su número (no usa códigos de CM)
 * y responde una matriz {@code [[numeroBolsin, latitud, longitud, "fechaHora"]]}.
 * Este adaptador la transforma al formato común
 * {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion} (ver {@link IAdaptadorGPSTracker}).
 */
public class AdaptadorGeoPulseMTR900 implements IAdaptadorGPSTracker {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String apiKey;
    private final ServicioGPSTrackerGeoPulseMTR900 servicio;
    private int numeroBolsinSolicitado;
    private Object[][] ubicacionRecibida;
    private String[] ubicacionAdaptada;

    public AdaptadorGeoPulseMTR900(String apiKey, String urlBase) {
        this.apiKey = apiKey;
        this.servicio = new ServicioGPSTrackerGeoPulseMTR900(urlBase);
    }

    @Override
    public String[] recibirUbicacionBolsin(int numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        solicitarUbicacionBolsin(numeroBolsin, codigoCMOrigen, codigoCMDestino);
        adaptarUbicacionBolsin();
        return ubicacionAdaptada;
    }

    @Override
    public void solicitarUbicacionBolsin(int numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        this.numeroBolsinSolicitado = numeroBolsin;
        // El GeoPulse MTR-900 no recibe códigos de comisión médica.
        this.ubicacionRecibida = servicio.fetchCargoPositions(apiKey, numeroBolsin);
    }

    @Override
    public void adaptarUbicacionBolsin() {
        Object[] fila = obtenerFilaBolsin();
        try {
            String latitud = String.valueOf(((Number) fila[1]).doubleValue());
            String longitud = String.valueOf(((Number) fila[2]).doubleValue());
            String fechaHora = convertirFechaHora(String.valueOf(fila[3]));
            this.ubicacionAdaptada = new String[]{String.valueOf(numeroBolsinSolicitado), latitud, longitud, fechaHora};
        } catch (ClassCastException | NullPointerException | DateTimeParseException error) {
            throw new ErrorGPSTrackerException("El GPS Tracker GeoPulse MTR-900 devolvió datos inválidos para el bolsín "
                    + numeroBolsinSolicitado, error);
        }
    }

    /** Recorre la matriz recibida y devuelve la fila del bolsín solicitado. */
    private Object[] obtenerFilaBolsin() {
        if (ubicacionRecibida != null) {
            for (Object[] fila : ubicacionRecibida) {
                if (fila != null && fila.length >= 4
                        && fila[0] instanceof Number numero
                        && numero.intValue() == numeroBolsinSolicitado) {
                    return fila;
                }
            }
        }
        throw new ErrorGPSTrackerException("El GPS Tracker GeoPulse MTR-900 no informó la ubicación del bolsín "
                + numeroBolsinSolicitado);
    }

    /** El GeoPulse MTR-900 informa la fecha y hora como texto ISO (2026-03-07T12:30:00): se pasa a dd/MM/yyyy HH:mm:ss. */
    private String convertirFechaHora(String fechaHora) {
        return LocalDateTime.parse(fechaHora.trim()).format(FORMATO_FECHA_HORA);
    }
}
