package ar.edu.utn.frc.dsi.bolsines.gps;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Patrón Adapter - rol ADAPTABLE (Adaptee): servicio de terceros que no se modifica.
 * <p>
 * Servicio externo del GPS Tracker modelo GeoPulse MTR-900:
 * {@code GET /api/geopulse-mtr-900/fetchCargoPositions?numeroBolsin=}.
 * No recibe código de comisión médica y devuelve una matriz:
 * {@code [[numeroBolsin, latitud, longitud, "fechaHoraUltimaActualizacion"]]}.
 */
public class ServicioGPSTrackerGeoPulseMTR900 {

    /** Tiempo máximo de espera de la respuesta del GPS Tracker (ms). */
    private static final int TIMEOUT_MS = 4000;

    private final String urlBase;
    private final RestClient clienteHttp;

    public ServicioGPSTrackerGeoPulseMTR900(String urlBase) {
        this.urlBase = urlBase;
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(TIMEOUT_MS);
        fabrica.setReadTimeout(TIMEOUT_MS);
        this.clienteHttp = RestClient.builder().baseUrl(urlBase).requestFactory(fabrica).build();
    }

    public Object[][] fetchCargoPositions(String apiKey, int numeroBolsin) {
        try {
            return clienteHttp.get()
                    .uri(uri -> uri.path("/api/geopulse-mtr-900/fetchCargoPositions")
                            .queryParam("numeroBolsin", numeroBolsin)
                            .build())
                    .header("X-API-Key", apiKey)
                    .retrieve()
                    .body(Object[][].class);
        } catch (RestClientException error) {
            throw ErrorGPSTrackerException.desde(error, "GeoPulse MTR-900", numeroBolsin);
        }
    }

    public String getUrlBase() {
        return urlBase;
    }
}
