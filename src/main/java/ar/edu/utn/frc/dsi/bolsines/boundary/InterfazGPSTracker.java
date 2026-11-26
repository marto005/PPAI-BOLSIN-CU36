package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.config.ConfiguracionGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.gps.AdaptadorGeoPulseMTR900;
import ar.edu.utn.frc.dsi.bolsines.gps.AdaptadorNavTrackQX7A;
import ar.edu.utn.frc.dsi.bolsines.gps.AdaptadorXTR4500L;
import ar.edu.utn.frc.dsi.bolsines.gps.IAdaptadorGPSTracker;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

/**
 * Boundary con el sistema externo GPS Tracker (clase que se conserva de la realización de análisis).
 * <p>
 * Patrón Adapter: es el punto de entrada al sistema externo. Según el modelo de GPS Tracker
 * configurado para el sistema crea el adaptador concreto con new() y lo usa solo a través de la
 * interfaz objetivo {@link IAdaptadorGPSTracker}. El CLIENTE del patrón es
 * {@code GestorConsultarSeguimientoBolsines}, que recibe las ubicaciones ya adaptadas.
 */
@Component
@SessionScope
public class InterfazGPSTracker {

    private final String modeloGPSTracker;
    private final String urlBase;
    private IAdaptadorGPSTracker adaptador;

    public InterfazGPSTracker(ConfiguracionGPSTracker configuracion) {
        this.modeloGPSTracker = configuracion.getModelo();
        this.urlBase = configuracion.getUrlBase();
    }

    public void crearAdaptadorGPSTracker(String apiKey) {
        String modelo = ConfiguracionGPSTracker.normalizar(modeloGPSTracker);
        if (modelo.equals(ConfiguracionGPSTracker.normalizar(ConfiguracionGPSTracker.XTR_4500L))) {
            adaptador = new AdaptadorXTR4500L(apiKey, urlBase);
        } else if (modelo.equals(ConfiguracionGPSTracker.normalizar(ConfiguracionGPSTracker.NAVTRACK_QX_7A))) {
            adaptador = new AdaptadorNavTrackQX7A(apiKey, urlBase);
        } else if (modelo.equals(ConfiguracionGPSTracker.normalizar(ConfiguracionGPSTracker.GEOPULSE_MTR_900))) {
            adaptador = new AdaptadorGeoPulseMTR900(apiKey, urlBase);
        } else {
            throw new IllegalStateException("Modelo de GPS Tracker no soportado: " + modeloGPSTracker);
        }
    }

    /** Devuelve la ubicación en el formato común {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion}. */
    public String[] solicitarUbicacionBolsin(int numeroBolsin, String codigoCMOrigen, String codigoCMDestino) {
        if (adaptador == null) {
            throw new IllegalStateException("Primero se debe crear el adaptador del GPS Tracker");
        }
        return adaptador.recibirUbicacionBolsin(numeroBolsin, codigoCMOrigen, codigoCMDestino);
    }

    public String getModeloGPSTracker() {
        return modeloGPSTracker;
    }
}
