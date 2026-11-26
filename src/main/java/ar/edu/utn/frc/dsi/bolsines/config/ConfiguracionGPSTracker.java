package ar.edu.utn.frc.dsi.bolsines.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del GPS Tracker que usa el sistema (propiedades gps.tracker.* de application.properties).
 * Se trabaja con UN modelo para todo el sistema, igual que en la vista estática y dinámica del diseño.
 */
@ConfigurationProperties(prefix = "gps.tracker")
public class ConfiguracionGPSTracker {

    public static final String XTR_4500L = "XTR-4500L";
    public static final String NAVTRACK_QX_7A = "NavTrack QX-7A";
    public static final String GEOPULSE_MTR_900 = "GeoPulse MTR-900";

    private String modelo = XTR_4500L;
    private String urlBase = "http://localhost:8080";
    private String apiKeyXtr4500l;
    private String apiKeyNavtrackQx7a;
    private String apiKeyGeopulseMtr900;

    /** Devuelve la API Key del modelo configurado. */
    public String getApiKeyModeloConfigurado() {
        String modeloNormalizado = normalizar(modelo);
        if (modeloNormalizado.equals(normalizar(XTR_4500L))) {
            return apiKeyXtr4500l;
        }
        if (modeloNormalizado.equals(normalizar(NAVTRACK_QX_7A))) {
            return apiKeyNavtrackQx7a;
        }
        if (modeloNormalizado.equals(normalizar(GEOPULSE_MTR_900))) {
            return apiKeyGeopulseMtr900;
        }
        throw new IllegalStateException("Modelo de GPS Tracker no soportado: " + modelo
                + ". Valores posibles: " + XTR_4500L + ", " + NAVTRACK_QX_7A + ", " + GEOPULSE_MTR_900);
    }

    /** "NavTrack QX-7A", "navtrack-qx7a" y "NAVTRACKQX7A" se consideran el mismo modelo. */
    public static String normalizar(String nombreModelo) {
        return nombreModelo == null ? "" : nombreModelo.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getUrlBase() {
        return urlBase;
    }

    public void setUrlBase(String urlBase) {
        this.urlBase = urlBase;
    }

    public String getApiKeyXtr4500l() {
        return apiKeyXtr4500l;
    }

    public void setApiKeyXtr4500l(String apiKeyXtr4500l) {
        this.apiKeyXtr4500l = apiKeyXtr4500l;
    }

    public String getApiKeyNavtrackQx7a() {
        return apiKeyNavtrackQx7a;
    }

    public void setApiKeyNavtrackQx7a(String apiKeyNavtrackQx7a) {
        this.apiKeyNavtrackQx7a = apiKeyNavtrackQx7a;
    }

    public String getApiKeyGeopulseMtr900() {
        return apiKeyGeopulseMtr900;
    }

    public void setApiKeyGeopulseMtr900(String apiKeyGeopulseMtr900) {
        this.apiKeyGeopulseMtr900 = apiKeyGeopulseMtr900;
    }
}
