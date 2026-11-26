package ar.edu.utn.frc.dsi.bolsines.boundary;

/**
 * Datos del mapa que devuelve el servidor de mapas: de dónde se obtienen las teselas,
 * la atribución obligatoria y el centro/zoom inicial.
 */
public record MapaLocalizacion(String urlTeselas, String atribucion,
                               double centroLatitud, double centroLongitud, int zoom) {
}
