package ar.edu.utn.frc.dsi.bolsines.gps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Patrón Adapter - rol ADAPTADOR (Adapter) para el GPS Tracker XTR-4500L.
 * <p>
 * El XTR-4500L identifica al bolsín por su número y por el código de la CM de ORIGEN,
 * y responde un JSON {@code {"items":[...]}}. Este adaptador lo transforma al formato común
 * {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion} (ver {@link IAdaptadorGPSTracker}).
 */
public class AdaptadorXTR4500L implements IAdaptadorGPSTracker {

    private static final ObjectMapper LECTOR_JSON = new ObjectMapper();
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final String apiKey;
    private final ServicioGPSTrackerXTR4500L servicio;
    private int numeroBolsinSolicitado;
    private String ubicacionRecibida;
    private String[] ubicacionAdaptada;

    public AdaptadorXTR4500L(String apiKey, String urlBase) {
        this.apiKey = apiKey;
        this.servicio = new ServicioGPSTrackerXTR4500L(urlBase);
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
        // El XTR-4500L solo necesita la CM de origen: la CM destino no se usa.
        this.ubicacionRecibida = servicio.getBolsinLocation(apiKey, numeroBolsin, codigoCMOrigen);
    }

    @Override
    public void adaptarUbicacionBolsin() {
        Map<String, Object> item = obtenerItemBolsin();
        try {
            String latitud = String.valueOf(((Number) item.get("latitud")).doubleValue());
            String longitud = String.valueOf(((Number) item.get("longitud")).doubleValue());
            String fechaHora = convertirFechaHora(String.valueOf(item.get("fechaHoraUltimaActualizacion")));
            this.ubicacionAdaptada = new String[]{String.valueOf(numeroBolsinSolicitado), latitud, longitud, fechaHora};
        } catch (ClassCastException | NullPointerException | DateTimeParseException error) {
            throw new ErrorGPSTrackerException("El GPS Tracker XTR-4500L devolvió datos inválidos para el bolsín "
                    + numeroBolsinSolicitado + ": " + ubicacionRecibida, error);
        }
    }

    /** Interpreta el JSON recibido y devuelve el ítem que corresponde al bolsín solicitado. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> obtenerItemBolsin() {
        try {
            Map<String, Object> respuesta = LECTOR_JSON.readValue(ubicacionRecibida, new TypeReference<Map<String, Object>>() {
            });
            Object items = respuesta.get("items");
            if (items instanceof List<?> lista) {
                for (Object elemento : lista) {
                    if (elemento instanceof Map<?, ?> item
                            && item.get("numeroBolsin") instanceof Number numero
                            && numero.intValue() == numeroBolsinSolicitado) {
                        return (Map<String, Object>) item;
                    }
                }
            }
        } catch (JsonProcessingException | IllegalArgumentException error) {
            throw new ErrorGPSTrackerException("El GPS Tracker XTR-4500L devolvió un JSON inválido: " + ubicacionRecibida, error);
        }
        throw new ErrorGPSTrackerException("El GPS Tracker XTR-4500L no informó la ubicación del bolsín " + numeroBolsinSolicitado);
    }

    /** El XTR-4500L informa la fecha y hora en formato ISO (2026-03-07T12:30:00): se pasa a dd/MM/yyyy HH:mm:ss. */
    private String convertirFechaHora(String fechaHora) {
        return LocalDateTime.parse(fechaHora.trim()).format(FORMATO_FECHA_HORA);
    }
}
