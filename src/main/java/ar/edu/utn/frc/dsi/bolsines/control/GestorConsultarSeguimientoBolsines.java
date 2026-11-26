package ar.edu.utn.frc.dsi.bolsines.control;

import ar.edu.utn.frc.dsi.bolsines.boundary.BolsinEnMapa;
import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazServidorMapas;
import ar.edu.utn.frc.dsi.bolsines.boundary.MapaLocalizacion;
import ar.edu.utn.frc.dsi.bolsines.boundary.PantallaConsultaSeguimientoBolsines;
import ar.edu.utn.frc.dsi.bolsines.config.ConfiguracionGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.entidad.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entidad.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entidad.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidad.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entidad.Usuario;
import ar.edu.utn.frc.dsi.bolsines.gps.ErrorGPSTrackerException;
import ar.edu.utn.frc.dsi.bolsines.repositorio.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.SesionRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Control del CU 36 Consultar seguimiento de bolsines.
 * Tiene un estado por cada sesión de usuario (@SessionScope), igual que el gestor del análisis.
 * <p>
 * Patrón Adapter - rol CLIENTE: contiene la lógica de negocio y recibe siempre la ubicación en el mismo
 * formato (vector String[] {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion}), sin conocer
 * los modelos de GPS Tracker ni sus formatos.
 */
@Component
@SessionScope
public class GestorConsultarSeguimientoBolsines {

    public static final String ATRIBUTO_ID_SESION = "idSesion";

    // colaboradores
    private final InterfazGPSTracker interfazGPSTracker;
    private final InterfazServidorMapas interfazServidorMapas;
    private final GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin;
    private final ConfiguracionGPSTracker configuracionGPSTracker;
    private final BolsinRepository bolsinRepository;
    private final EmpleadoRepository empleadoRepository;
    private final SesionRepository sesionRepository;
    private final HttpSession httpSession;

    // estado de la consulta
    private PantallaConsultaSeguimientoBolsines pantalla;
    private String apiKey;
    private Usuario usuarioLogueado;
    private Empleado empleadoLogueado;
    private ComisionMedica cmUsuarioLogueado;
    private List<Bolsin> bolsinesEnviados = new ArrayList<>();
    /** Cada ubicación: {numeroBolsin, latitud, longitud, fechaHoraUltimaActualizacion}. */
    private List<String[]> ubicacionesBolsines = new ArrayList<>();
    private Map<Integer, String> bolsinesSinUbicacion = new LinkedHashMap<>();
    private Bolsin bolsinSeleccionado;
    private String correoGerenteCMDestino;

    public GestorConsultarSeguimientoBolsines(InterfazGPSTracker interfazGPSTracker,
                                              InterfazServidorMapas interfazServidorMapas,
                                              GestorNotificarUbicacionBolsin gestorNotificarUbicacionBolsin,
                                              ConfiguracionGPSTracker configuracionGPSTracker,
                                              BolsinRepository bolsinRepository,
                                              EmpleadoRepository empleadoRepository,
                                              SesionRepository sesionRepository,
                                              HttpSession httpSession) {
        this.interfazGPSTracker = interfazGPSTracker;
        this.interfazServidorMapas = interfazServidorMapas;
        this.gestorNotificarUbicacionBolsin = gestorNotificarUbicacionBolsin;
        this.configuracionGPSTracker = configuracionGPSTracker;
        this.bolsinRepository = bolsinRepository;
        this.empleadoRepository = empleadoRepository;
        this.sesionRepository = sesionRepository;
        this.httpSession = httpSession;
    }

    // =====================================================================
    // Flujo básico
    // =====================================================================

    /** Paso 1: el EB selecciona la opción. Pasos 2 a 6 del flujo descripto. */
    public void nuevaConsultaSeguimiento(PantallaConsultaSeguimientoBolsines pantalla) {
        this.pantalla = pantalla;
        reiniciarConsulta();
        buscarUsuario();
        buscarCMUsuarioLogueado();
        pantalla.mostrarCMUsuarioLogueado(cmUsuarioLogueado.getNombre() + " (" + cmUsuarioLogueado.getCodigo() + ")");
        buscarBolsinesEnviadoCMOrigen();
        if (bolsinesEnviados.isEmpty()) {
            // A1: no se encuentran bolsines en estado Enviado
            pantalla.informarSinBolsinesEnviados();
            finCU();
            return;
        }
        solicitarDatosUbicacionBolsines();
        obtenerMapaLocalizacion();
    }

    private void buscarUsuario() {
        Object idSesion = httpSession.getAttribute(ATRIBUTO_ID_SESION);
        Sesion actual = (idSesion instanceof Long id) ? sesionRepository.findById(id).orElse(null) : null;
        if (actual == null || !actual.estaActiva()) {
            throw new IllegalStateException("No hay una sesión activa");
        }
        usuarioLogueado = actual.getUsuario();
        empleadoLogueado = null;
        for (Empleado empleado : empleadoRepository.findAll()) {
            if (empleado.esTuUsuario(usuarioLogueado)) {
                empleadoLogueado = empleado;
                break;
            }
        }
        if (empleadoLogueado == null) {
            throw new IllegalStateException("El usuario " + usuarioLogueado.getNombreUsuario() + " no tiene un empleado asociado");
        }
    }

    private void buscarCMUsuarioLogueado() {
        cmUsuarioLogueado = empleadoLogueado.obtenerComisionMedica();
    }

    /** Paso 3: bolsines en estado Enviado cuya CM origen es la CM del usuario logueado. */
    private void buscarBolsinesEnviadoCMOrigen() {
        bolsinesEnviados = new ArrayList<>();
        for (Bolsin bolsin : bolsinRepository.findAll()) {
            if (bolsin.esEnviado() && bolsin.sosComisionMedicaUsuarioLogueado(cmUsuarioLogueado)) {
                bolsinesEnviados.add(bolsin);
            }
        }
        bolsinesEnviados.sort(Comparator.comparingInt(Bolsin::getNroBolsin));
    }

    /**
     * Pasos 4 y 5: se solicita al GPS Tracker la ubicación de cada bolsín.
     * Método de enganche: a partir de este mensaje del análisis se aplica el patrón Adapter (ver vista dinámica).
     */
    private void solicitarDatosUbicacionBolsines() {
        apiKey = getApiKey();
        interfazGPSTracker.crearAdaptadorGPSTracker(apiKey);
        ubicacionesBolsines = new ArrayList<>();
        bolsinesSinUbicacion = new LinkedHashMap<>();
        for (Bolsin bolsin : bolsinesEnviados) {
            try {
                String[] ubicacion = interfazGPSTracker.solicitarUbicacionBolsin(
                        bolsin.getNroBolsin(), cmUsuarioLogueado.getCodigo(), bolsin.buscarCMDestino().getCodigo());
                ubicacionesBolsines.add(ubicacion);
            } catch (ErrorGPSTrackerException error) {
                // A3: el GPS Tracker no pudo informar la ubicación del bolsín
                bolsinesSinUbicacion.put(bolsin.getNroBolsin(), error.getMessage());
            }
        }
    }

    private String getApiKey() {
        return configuracionGPSTracker.getApiKeyModeloConfigurado();
    }

    /** Paso 6: se muestra sobre un mapa la posición de los bolsines. */
    private void obtenerMapaLocalizacion() {
        MapaLocalizacion mapa = interfazServidorMapas.obtenerMapaLocalizacion(ubicacionesBolsines);
        pantalla.mostrarBolsinesEnMapa(mapa, armarBolsinesEnMapa(ubicacionesBolsines));
        if (!bolsinesSinUbicacion.isEmpty()) {
            pantalla.informarBolsinesSinUbicacion(bolsinesSinUbicacion);
        }
        if (ubicacionesBolsines.isEmpty()) {
            pantalla.informarSinBolsinesUbicados();
            return;
        }
        pantalla.habilitarFiltros(buscarOpcionesCMDestino());
        pantalla.pedirSeleccionOpcionBolsin();
    }

    /** Paso 6 (opción de filtrar por número de precinto o CM destino). */
    public void filtrarBolsines(String numeroPrecinto, String codigoCMDestino) {
        if (ubicacionesBolsines.isEmpty()) {
            return;
        }
        String precinto = numeroPrecinto == null ? "" : numeroPrecinto.trim();
        String cmDestino = codigoCMDestino == null ? "" : codigoCMDestino.trim();
        List<String[]> filtradas = new ArrayList<>();
        for (String[] ubicacion : ubicacionesBolsines) {
            Bolsin bolsin = buscarBolsinEnviado(Integer.parseInt(ubicacion[0]));
            boolean coincidePrecinto = precinto.isEmpty() || precinto.equalsIgnoreCase(bolsin.getNumeroPrecinto());
            boolean coincideCMDestino = cmDestino.isEmpty() || cmDestino.equalsIgnoreCase(bolsin.buscarCMDestino().getCodigo());
            if (coincidePrecinto && coincideCMDestino) {
                filtradas.add(ubicacion);
            }
        }
        if (filtradas.isEmpty()) {
            if (!precinto.isEmpty()) {
                // A2: no se encuentra bolsín con el número de precinto ingresado
                pantalla.informarBolsinNoEncontrado(precinto);
            } else {
                pantalla.informarSinResultadosFiltro();
            }
            filtradas = ubicacionesBolsines;
        }
        pantalla.mostrarBolsinesEnMapa(interfazServidorMapas.obtenerMapaLocalizacion(filtradas), armarBolsinesEnMapa(filtradas));
        pantalla.pedirSeleccionOpcionBolsin();
    }

    /** Paso 7: el EB selecciona un bolsín del mapa. Paso 8: se consulta si desea enviar el correo. */
    public void tomarSeleccionBolsinDeMapa(int numeroBolsin) {
        Bolsin bolsin = buscarBolsinEnviado(numeroBolsin);
        if (bolsin == null || buscarUbicacion(numeroBolsin) == null) {
            pantalla.informarError("El bolsín N° " + numeroBolsin + " no está disponible en el mapa.");
            return;
        }
        bolsinSeleccionado = bolsin;
        pantalla.solicitarConfirmacionEnvioEmail(numeroBolsin, bolsin.buscarCMDestino().getNombre());
    }

    /** Paso 9: el EB confirma (o no) el envío del correo. Pasos 10 y 11. */
    public void tomarConfirmacionEmail(boolean confirmaEnvio) {
        if (bolsinSeleccionado == null) {
            pantalla.informarError("Primero debe seleccionar un bolsín del mapa.");
            return;
        }
        if (!confirmaEnvio) {
            // A5: el EB no selecciona la opción para enviar un correo electrónico
            pantalla.informarEnvioNoConfirmado(bolsinSeleccionado.getNroBolsin());
            finCU();
            return;
        }
        buscarCorreoGerenteCMDestino();
        if (correoGerenteCMDestino == null) {
            pantalla.informarError("No se encontró el Gerente de la " + bolsinSeleccionado.buscarCMDestino().getNombre()
                    + " para enviarle el correo.");
            return;
        }
        if (llamarCU31()) {
            pantalla.informarEjecucionExitosa(bolsinSeleccionado.getNroBolsin(), correoGerenteCMDestino);
        } else {
            pantalla.informarError("No se pudo enviar el correo al Gerente de la CM destino.");
        }
        finCU();
    }

    /** Paso 10: busca el correo del Gerente de la CM destino del bolsín seleccionado. */
    private void buscarCorreoGerenteCMDestino() {
        ComisionMedica cmDestino = bolsinSeleccionado.buscarCMDestino();
        correoGerenteCMDestino = null;
        for (Empleado empleado : empleadoRepository.findAll()) {
            if (empleado.esTuComisionMedica(cmDestino) && empleado.sosGerenteDeCM()) {
                correoGerenteCMDestino = empleado.getCorreo();
                break;
            }
        }
    }

    /** Paso 11: include del CU 31 Notificar ubicación de bolsín. */
    private boolean llamarCU31() {
        String[] ubicacion = buscarUbicacion(bolsinSeleccionado.getNroBolsin());
        return gestorNotificarUbicacionBolsin.notificarUbicacionBolsin(correoGerenteCMDestino, ubicacion[0],
                ubicacion[3], ubicacion[1], ubicacion[2]);
    }

    /** A4: el EB cancela la ejecución del caso de uso. */
    public void cancelarConsulta() {
        finCU();
    }

    private void finCU() {
        if (pantalla != null) {
            pantalla.cerrarVentana();
        }
        reiniciarConsulta();
    }

    // =====================================================================
    // Auxiliares
    // =====================================================================

    private void reiniciarConsulta() {
        apiKey = null;
        bolsinesEnviados = new ArrayList<>();
        ubicacionesBolsines = new ArrayList<>();
        bolsinesSinUbicacion = new LinkedHashMap<>();
        bolsinSeleccionado = null;
        correoGerenteCMDestino = null;
    }

    private Bolsin buscarBolsinEnviado(int numeroBolsin) {
        for (Bolsin bolsin : bolsinesEnviados) {
            if (bolsin.getNroBolsin() == numeroBolsin) {
                return bolsin;
            }
        }
        return null;
    }

    private String[] buscarUbicacion(int numeroBolsin) {
        for (String[] ubicacion : ubicacionesBolsines) {
            if (Integer.parseInt(ubicacion[0]) == numeroBolsin) {
                return ubicacion;
            }
        }
        return null;
    }

    private Map<String, String> buscarOpcionesCMDestino() {
        Map<String, String> opciones = new LinkedHashMap<>();
        for (String[] ubicacion : ubicacionesBolsines) {
            ComisionMedica destino = buscarBolsinEnviado(Integer.parseInt(ubicacion[0])).buscarCMDestino();
            opciones.put(destino.getCodigo(), destino.getNombre());
        }
        return opciones;
    }

    /** Convierte las ubicaciones (formato común String[]) en los datos que muestra la pantalla. */
    private List<BolsinEnMapa> armarBolsinesEnMapa(List<String[]> ubicaciones) {
        List<BolsinEnMapa> resultado = new ArrayList<>();
        for (String[] ubicacion : ubicaciones) {
            int numeroBolsin = Integer.parseInt(ubicacion[0]);
            Bolsin bolsin = buscarBolsinEnviado(numeroBolsin);
            ComisionMedica destino = bolsin.buscarCMDestino();
            resultado.add(new BolsinEnMapa(numeroBolsin, bolsin.getNumeroPrecinto(), destino.getCodigo(),
                    destino.getNombre(), Double.parseDouble(ubicacion[1]), Double.parseDouble(ubicacion[2]),
                    ubicacion[3]));
        }
        return resultado;
    }
}
