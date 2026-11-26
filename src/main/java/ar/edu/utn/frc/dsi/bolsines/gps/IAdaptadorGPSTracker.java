package ar.edu.utn.frc.dsi.bolsines.gps;

/**
 * Patrón Adapter - rol OBJETIVO (Target).
 * <p>
 * Es la interfaz que usa la {@code InterfazGPSTracker} para atender al cliente
 * ({@code GestorConsultarSeguimientoBolsines}). Cada modelo de GPS Tracker
 * (XTR-4500L, NavTrack QX-7A, GeoPulse MTR-900) expone un endpoint distinto, con parámetros y
 * formato de respuesta distintos; los adaptadores concretos los traducen a esta interfaz común.
 * <p>
 * Formato común de la ubicación (dato adaptado), un vector de String de 4 posiciones:
 * <pre>
 *   [0] número de bolsín
 *   [1] latitud  (grados decimales, con punto)
 *   [2] longitud (grados decimales, con punto)
 *   [3] fecha y hora de la última actualización, con formato dd/MM/yyyy HH:mm:ss
 * </pre>
 */
public interface IAdaptadorGPSTracker {

    /**
     * Método polimórfico que usa el cliente: solicita la ubicación al GPS Tracker, la adapta y la devuelve
     * en el formato común {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion}.
     */
    String[] recibirUbicacionBolsin(int numeroBolsin, String codigoCMOrigen, String codigoCMDestino);

    /** Invoca al servicio externo (adaptable) con los parámetros que ese modelo necesita. */
    void solicitarUbicacionBolsin(int numeroBolsin, String codigoCMOrigen, String codigoCMDestino);

    /** Transforma la respuesta del servicio externo al formato común que espera el sistema. */
    void adaptarUbicacionBolsin();
}
