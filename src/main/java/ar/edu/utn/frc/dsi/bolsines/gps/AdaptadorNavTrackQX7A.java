package ar.edu.utn.frc.dsi.bolsines.gps;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Patrón Adapter - rol ADAPTADOR (Adapter) para el GPS Tracker NavTrack QX-7A.
 * <p>
 * El NavTrack QX-7A identifica al bolsín por su número y por el código de la CM de DESTINO,
 * y responde un único String con los ítems separados por comas:
 * {@code 101,-31.4201,-64.1888,2026-03-07T12:30}. Este adaptador lo transforma al formato común
 * {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion} (ver {@link IAdaptadorGPSTracker}).
 */
public class AdaptadorNavTrackQX7A implements IAdaptadorGPSTracker {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String apiKey;
    private final ServicioGPSTrackerNavTrackQX7A servicio;
    private int numeroBolsinSolicitado;
    private String ubicacionRecibida;
    private String[] ubicacionAdaptada;

    public AdaptadorNavTrackQX7A(String apiKey, String urlBase) {
        this.apiKey = apiKey;
        this.servicio = new ServicioGPSTrackerNavTrackQX7A(urlBase);
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
        // El NavTrack QX-7A solo necesita la CM de destino: la CM origen no se usa.
        this.ubicacionRecibida = servicio.retrieveTrackingData(apiKey, numeroBolsin, codigoCMDestino);
    }

    @Override
    public void adaptarUbicacionBolsin() {
        String[] campos = separarCampos();
        try {
            int numeroBolsin = Integer.parseInt(campos[0].trim());
            if (numeroBolsin != numeroBolsinSolicitado) {
                throw new ErrorGPSTrackerException("El GPS Tracker NavTrack QX-7A informó el bolsín " + numeroBolsin
                        + " cuando se pidió el " + numeroBolsinSolicitado);
            }
            String latitud = String.valueOf(Double.parseDouble(campos[1].trim()));
            String longitud = String.valueOf(Double.parseDouble(campos[2].trim()));
            String fechaHora = convertirFechaHora(campos[3]);
            this.ubicacionAdaptada = new String[]{String.valueOf(numeroBolsin), latitud, longitud, fechaHora};
        } catch (NumberFormatException | DateTimeParseException error) {
            throw new ErrorGPSTrackerException("El GPS Tracker NavTrack QX-7A devolvió datos inválidos: " + ubicacionRecibida, error);
        }
    }

    /** Separa el String recibido en sus 4 ítems: número, latitud, longitud y fecha y hora. */
    private String[] separarCampos() {
        if (ubicacionRecibida == null || ubicacionRecibida.isBlank()) {
            throw new ErrorGPSTrackerException("El GPS Tracker NavTrack QX-7A no informó la ubicación del bolsín "
                    + numeroBolsinSolicitado);
        }
        String[] campos = ubicacionRecibida.trim().split(",");
        if (campos.length < 4) {
            throw new ErrorGPSTrackerException("El GPS Tracker NavTrack QX-7A devolvió un formato inesperado: " + ubicacionRecibida);
        }
        return campos;
    }

    /** El NavTrack QX-7A puede omitir los segundos (2026-03-07T12:30): se pasa a dd/MM/yyyy HH:mm:ss. */
    private String convertirFechaHora(String fechaHora) {
        return LocalDateTime.parse(fechaHora.trim()).format(FORMATO_FECHA_HORA);
    }
}
