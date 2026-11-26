package ar.edu.utn.frc.dsi.bolsines.boundary;

/**
 * Datos de un bolsín que la pantalla muestra en el mapa (marcador) y en la grilla.
 */
public record BolsinEnMapa(int numeroBolsin, String numeroPrecinto, String codigoCMDestino, String nombreCMDestino,
                           double latitud, double longitud, String fechaHoraUltimaActualizacion) {
}
