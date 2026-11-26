package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Patrón Adapter - rol ADAPTABLE (Adaptee): servicio de terceros que no se modifica.
 * <p>
 * Representa al servicio externo del GPS Tracker modelo XTR-4500L, tal como lo expone la API:
 * {@code GET /api/xtr-4500l/getBolsinLocation?numeroBolsin=&codigoComisionMedicaOrigen=}
 * con la API Key en el header {@code X-API-Key}. Devuelve un JSON en formato cadena de caracteres:
 * {@code {"items":[{"numeroBolsin":101,"latitud":..,"longitud":..,"fechaHoraUltimaActualizacion":".."}]}}
 */
public class ServicioGPSTrackerXTR4500L {

    /** Tiempo máximo de espera de la respuesta del GPS Tracker (ms). */
    private static final int TIMEOUT_MS = 4000;

    private final String urlBase;
    private final RestClient clienteHttp;

    public ServicioGPSTrackerXTR4500L(String urlBase) {
        this.urlBase = urlBase;
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT_MS);
        fabrica.setReadTimeout(TIMEOUT_MS);
        this.clienteHttp = RestClient.builder().baseUrl(urlBase).requestFactory(fabrica).build();
    }

    public String getBolsinLocation(String apiKey, int numeroBolsin, String codigoComisionMedicaOrigen) {
        try {
            return clienteHttp.get()
                    .uri(uri -> uri.path("/api/xtr-4500l/getBolsinLocation")
                            .queryParam("numeroBolsin", numeroBolsin)
                            .queryParam("codigoComisionMedicaOrigen", codigoComisionMedicaOrigen)
                            .build())
                    .header("X-API-Key", apiKey)
                    .retrieve()
                    .body(String.class);
        } catch (RestClientException error) {
            throw ErrorGPSTrackerException.desde(error, "XTR-4500L", numeroBolsin);
        }
    }

    public String getUrlBase() {
        return urlBase;
    }
}
