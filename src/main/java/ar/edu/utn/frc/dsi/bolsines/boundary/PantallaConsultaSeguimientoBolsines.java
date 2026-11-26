package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.control.GestorConsultarSeguimientoBolsines;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Boundary (pantalla) del CU 36 Consultar seguimiento de bolsines.
 * <p>
 * Es un controlador web de Spring MVC con estado por sesión: cada método público que no es un
 * endpoint (mostrar..., informar..., solicitar...) es invocado por el gestor, como en el diagrama
 * de secuencia; los endpoints (@GetMapping/@PostMapping) reciben las acciones del Encargado de Bolsines.
 * La vista es la plantilla Thymeleaf templates/seguimiento.html.
 */
@Controller
@SessionScope
@RequestMapping("/seguimiento")
public class PantallaConsultaSeguimientoBolsines {

    private final GestorConsultarSeguimientoBolsines gestor;
    private final InterfazGPSTracker interfazGPSTracker;

    // elementos de la ventana
    private String lblCMUsuarioLogueado;
    private MapaLocalizacion mapaDeBolsines;
    private List<BolsinEnMapa> bolsinesEnMapa = new ArrayList<>();
    private Map<String, String> opcionesCMDestino = new LinkedHashMap<>();
    private Map<Integer, String> bolsinesSinUbicacion = new LinkedHashMap<>();
    private boolean seleccionHabilitada;
    private boolean filtrosHabilitados;
    private Integer bolsinAConfirmar;
    private String cmDestinoAConfirmar;
    private String filtroPrecinto = "";
    private String filtroCMDestino = "";
    private String mensajeInformativo;
    private String mensajeAdvertencia;
    private String mensajeError;
    private String lblEjecucionExitosa;
    private boolean ventanaCerrada;

    public PantallaConsultaSeguimientoBolsines(GestorConsultarSeguimientoBolsines gestor, InterfazGPSTracker interfazGPSTracker) {
        this.gestor = gestor;
        this.interfazGPSTracker = interfazGPSTracker;
    }

    // =====================================================================
    // Acciones del Encargado de Bolsines (endpoints)
    // =====================================================================

    /** Paso 1: el EB selecciona la opción "Consultar seguimiento de bolsines". */
    @GetMapping
    public String opcionConsultarSeguimientoBolsines(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute(GestorConsultarSeguimientoBolsines.ATRIBUTO_ID_SESION) == null) {
            return "redirect:/login";
        }
        habilitarVentana();
        try {
            gestor.nuevaConsultaSeguimiento(this);
        } catch (IllegalStateException error) {
            // sesión vencida o configuración del GPS Tracker inválida
            informarError(error.getMessage());
            cerrarVentana();
        }
        return mostrarVentana(model);
    }

    @GetMapping("/filtrar")
    public String tomarFiltro(@RequestParam(value = "numeroPrecinto", required = false, defaultValue = "") String numeroPrecinto,
                              @RequestParam(value = "codigoCMDestino", required = false, defaultValue = "") String codigoCMDestino,
                              Model model) {
        if (ventanaCerrada || !filtrosHabilitados) {
            return "redirect:/seguimiento";
        }
        limpiarMensajes();
        filtroPrecinto = numeroPrecinto.trim();
        filtroCMDestino = codigoCMDestino.trim();
        gestor.filtrarBolsines(filtroPrecinto, filtroCMDestino);
        return mostrarVentana(model);
    }

    /** Paso 7: el EB selecciona un bolsín del mapa. */
    @PostMapping("/seleccionar")
    public String tomarSeleccionBolsinDeMapa(@RequestParam("numeroBolsin") int numeroBolsin, Model model) {
        if (ventanaCerrada || !seleccionHabilitada) {
            return "redirect:/seguimiento";
        }
        limpiarMensajes();
        gestor.tomarSeleccionBolsinDeMapa(numeroBolsin);
        return mostrarVentana(model);
    }

    /** Paso 9: el EB confirma (o no) el envío del correo electrónico. */
    @PostMapping("/confirmar")
    public String tomarConfirmacionEmail(@RequestParam("confirma") boolean confirma, Model model) {
        if (ventanaCerrada || bolsinAConfirmar == null) {
            return "redirect:/seguimiento";
        }
        limpiarMensajes();
        bolsinAConfirmar = null;
        gestor.tomarConfirmacionEmail(confirma);
        return mostrarVentana(model);
    }

    /** A4: el EB cancela la ejecución del caso de uso. */
    @PostMapping("/cancelar")
    public String cancelar() {
        gestor.cancelarConsulta();
        return "redirect:/menu";
    }

    // =====================================================================
    // Mensajes que recibe del gestor
    // =====================================================================

    public void habilitarVentana() {
        lblCMUsuarioLogueado = null;
        mapaDeBolsines = null;
        bolsinesEnMapa = new ArrayList<>();
        opcionesCMDestino = new LinkedHashMap<>();
        bolsinesSinUbicacion = new LinkedHashMap<>();
        seleccionHabilitada = false;
        filtrosHabilitados = false;
        bolsinAConfirmar = null;
        cmDestinoAConfirmar = null;
        filtroPrecinto = "";
        filtroCMDestino = "";
        ventanaCerrada = false;
        limpiarMensajes();
    }

    public void mostrarCMUsuarioLogueado(String cmUsuarioLogueado) {
        this.lblCMUsuarioLogueado = cmUsuarioLogueado;
    }

    public void mostrarBolsinesEnMapa(MapaLocalizacion mapa, List<BolsinEnMapa> bolsines) {
        this.mapaDeBolsines = mapa;
        this.bolsinesEnMapa = new ArrayList<>(bolsines);
    }

    public void habilitarFiltros(Map<String, String> opcionesCMDestino) {
        this.opcionesCMDestino = new LinkedHashMap<>(opcionesCMDestino);
        this.filtrosHabilitados = true;
    }

    public void pedirSeleccionOpcionBolsin() {
        this.seleccionHabilitada = true;
    }

    public void solicitarConfirmacionEnvioEmail(int numeroBolsin, String nombreCMDestino) {
        this.bolsinAConfirmar = numeroBolsin;
        this.cmDestinoAConfirmar = nombreCMDestino;
    }

    public void informarEjecucionExitosa(int numeroBolsin, String correoGerente) {
        this.lblEjecucionExitosa = "Se notificó la ubicación del bolsín N° " + numeroBolsin
                + " al Gerente de la CM destino (" + correoGerente + "). El caso de uso finalizó correctamente.";
    }

    /** A1 */
    public void informarSinBolsinesEnviados() {
        this.mensajeInformativo = "No se encontraron bolsines en estado Enviado cuya CM origen sea "
                + lblCMUsuarioLogueado + ".";
    }

    /** A2 */
    public void informarBolsinNoEncontrado(String numeroPrecinto) {
        this.mensajeAdvertencia = "No se encontró ningún bolsín con el número de precinto \"" + numeroPrecinto
                + "\". Se muestran todos los bolsines.";
    }

    public void informarSinResultadosFiltro() {
        this.mensajeAdvertencia = "Ningún bolsín coincide con el filtro. Se muestran todos los bolsines.";
    }

    /** A3 */
    public void informarBolsinesSinUbicacion(Map<Integer, String> bolsinesSinUbicacion) {
        this.bolsinesSinUbicacion = new LinkedHashMap<>(bolsinesSinUbicacion);
    }

    public void informarSinBolsinesUbicados() {
        this.mensajeAdvertencia = "El GPS Tracker no pudo informar la ubicación de ninguno de los bolsines enviados.";
    }

    /** A5 */
    public void informarEnvioNoConfirmado(int numeroBolsin) {
        this.mensajeInformativo = "No se envió el correo electrónico para el bolsín N° " + numeroBolsin
                + ". El caso de uso finalizó.";
    }

    public void informarError(String mensaje) {
        this.mensajeError = mensaje;
    }

    public void cerrarVentana() {
        this.ventanaCerrada = true;
        this.seleccionHabilitada = false;
        this.filtrosHabilitados = false;
        this.bolsinAConfirmar = null;
    }

    // =====================================================================
    // Render
    // =====================================================================

    private String mostrarVentana(Model model) {
        model.addAttribute("pantalla", this);
        model.addAttribute("modeloGPSTracker", interfazGPSTracker.getModeloGPSTracker());
        return "seguimiento";
    }

    private void limpiarMensajes() {
        mensajeInformativo = null;
        mensajeAdvertencia = null;
        mensajeError = null;
        lblEjecucionExitosa = null;
    }

    // getters para la plantilla
    public String getLblCMUsuarioLogueado() {
        return lblCMUsuarioLogueado;
    }

    public MapaLocalizacion getMapaDeBolsines() {
        return mapaDeBolsines;
    }

    public List<BolsinEnMapa> getBolsinesEnMapa() {
        return bolsinesEnMapa;
    }

    public Map<String, String> getOpcionesCMDestino() {
        return opcionesCMDestino;
    }

    public Map<Integer, String> getBolsinesSinUbicacion() {
        return bolsinesSinUbicacion;
    }

    public boolean isSeleccionHabilitada() {
        return seleccionHabilitada;
    }

    public boolean isFiltrosHabilitados() {
        return filtrosHabilitados;
    }

    public Integer getBolsinAConfirmar() {
        return bolsinAConfirmar;
    }

    public String getCmDestinoAConfirmar() {
        return cmDestinoAConfirmar;
    }

    public String getFiltroPrecinto() {
        return filtroPrecinto;
    }

    public String getFiltroCMDestino() {
        return filtroCMDestino;
    }

    public String getMensajeInformativo() {
        return mensajeInformativo;
    }

    public String getMensajeAdvertencia() {
        return mensajeAdvertencia;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public String getLblEjecucionExitosa() {
        return lblEjecucionExitosa;
    }

    public boolean isVentanaCerrada() {
        return ventanaCerrada;
    }
}
