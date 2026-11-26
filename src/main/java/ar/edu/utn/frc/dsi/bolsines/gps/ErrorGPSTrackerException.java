package ar.edu.utn.frc.dsi.bolsines.gps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * El GPS Tracker no pudo informar la ubicación de un bolsín (flujo alternativo A3 del CU 36).
 */
public class ErrorGPSTrackerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private static final ObjectMapper LECTOR_JSON = new ObjectMapper();

    public ErrorGPSTrackerException(String mensaje) {
        super(mensaje);
    }

    public ErrorGPSTrackerException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /** Traduce un error de comunicación con la API a un mensaje entendible para el usuario. */
    public static ErrorGPSTrackerException desde(RestClientException error, String modelo, int numeroBolsin) {
        String detalle;
        if (error instanceof RestClientResponseException respuesta) {
            detalle = "respondió HTTP " + respuesta.getStatusCode().value() + leerMensajeApi(respuesta.getResponseBodyAsString());
        } else if (error instanceof ResourceAccessException) {
            detalle = "no se pudo establecer comunicación con el servicio (¿está levantada la API del GPS Tracker?)";
        } else {
            detalle = error.getMessage();
        }
        return new ErrorGPSTrackerException(
                "El GPS Tracker " + modelo + " no pudo informar la ubicación del bolsín " + numeroBolsin + ": " + detalle,
                error);
    }

    private static String leerMensajeApi(String cuerpo) {
        try {
            JsonNode json = LECTOR_JSON.readTree(cuerpo);
            if (json != null && json.hasNonNull("mensaje")) {
                return " (" + json.get("mensaje").asText() + ")";
            }
        } catch (Exception ignorada) {
            // la respuesta no era JSON: se informa solo el código HTTP
        }
        return "";
    }
}
