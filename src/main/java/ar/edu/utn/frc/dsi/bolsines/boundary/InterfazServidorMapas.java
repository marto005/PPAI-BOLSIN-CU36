package ar.edu.utn.frc.dsi.bolsines.boundary;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Boundary con el servidor de mapas (actor secundario del CU 36).
 * En la ERS es Google Maps; en la implementación se usa OpenStreetMap con la librería Leaflet,
 * que no requiere API key ni cuenta de facturación.
 */
@Component
public class InterfazServidorMapas {

    private static final String URL_TESELAS = "https://tile.openstreetmap.org/{z}/{x}/{y}.png";
    private static final String ATRIBUCION = "&copy; colaboradores de OpenStreetMap";
    private static final double LATITUD_ARGENTINA = -34.6;
    private static final double LONGITUD_ARGENTINA = -64.0;

    /** Recibe las ubicaciones en el formato común {numeroBolsin, latitud, longitud, fechaHora}. */
    public MapaLocalizacion obtenerMapaLocalizacion(List<String[]> ubicaciones) {
        if (ubicaciones == null || ubicaciones.isEmpty()) {
            return new MapaLocalizacion(URL_TESELAS, ATRIBUCION, LATITUD_ARGENTINA, LONGITUD_ARGENTINA, 4);
        }
        double latitud = ubicaciones.stream().mapToDouble(u -> Double.parseDouble(u[1])).average().orElse(LATITUD_ARGENTINA);
        double longitud = ubicaciones.stream().mapToDouble(u -> Double.parseDouble(u[2])).average().orElse(LONGITUD_ARGENTINA);
        int zoom = ubicaciones.size() == 1 ? 11 : 5;
        return new MapaLocalizacion(URL_TESELAS, ATRIBUCION, latitud, longitud, zoom);
    }
}
