package ar.edu.utn.frc.dsi.bolsines.boundary;

import ar.edu.utn.frc.dsi.bolsines.config.ConfiguracionGPSTracker;
import ar.edu.utn.frc.dsi.bolsines.control.GestorConsultarSeguimientoBolsines;
import ar.edu.utn.frc.dsi.bolsines.entidad.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidad.Sesion;
import ar.edu.utn.frc.dsi.bolsines.entidad.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.SesionRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Inicio de sesión y menú principal. No forma parte del CU 36, pero lo precede:
 * el CU necesita una Sesion activa para saber cuál es el usuario logueado y su CM.
 */
@Controller
public class PantallaInicio {

    private final UsuarioRepository usuarioRepository;
    private final SesionRepository sesionRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ConfiguracionGPSTracker configuracionGPSTracker;

    public PantallaInicio(UsuarioRepository usuarioRepository, SesionRepository sesionRepository,
                          EmpleadoRepository empleadoRepository, ConfiguracionGPSTracker configuracionGPSTracker) {
        this.usuarioRepository = usuarioRepository;
        this.sesionRepository = sesionRepository;
        this.empleadoRepository = empleadoRepository;
        this.configuracionGPSTracker = configuracionGPSTracker;
    }

    @GetMapping("/")
    public String inicio(HttpSession httpSession) {
        return buscarSesionActiva(httpSession).isPresent() ? "redirect:/menu" : "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String iniciarSesion(@RequestParam("nombreUsuario") String nombreUsuario, @RequestParam("contrasena") String contrasena,
                                HttpSession httpSession, Model model) {
        Optional<Usuario> usuario = usuarioRepository.findByNombreUsuario(nombreUsuario.trim());
        if (usuario.isEmpty() || !usuario.get().validarContrasena(contrasena)) {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            model.addAttribute("nombreUsuario", nombreUsuario);
            return "login";
        }
        Sesion sesion = sesionRepository.save(new Sesion(usuario.get(), LocalDateTime.now()));
        httpSession.setAttribute(GestorConsultarSeguimientoBolsines.ATRIBUTO_ID_SESION, sesion.getId());
        return "redirect:/menu";
    }

    @GetMapping("/menu")
    public String menu(HttpSession httpSession, Model model) {
        Optional<Sesion> sesion = buscarSesionActiva(httpSession);
        if (sesion.isEmpty()) {
            return "redirect:/login";
        }
        Usuario usuario = sesion.get().getUsuario();
        Empleado empleado = empleadoRepository.findAll().stream()
                .filter(e -> e.esTuUsuario(usuario))
                .findFirst()
                .orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("empleado", empleado);
        model.addAttribute("modeloGPSTracker", configuracionGPSTracker.getModelo());
        return "menu";
    }

    @PostMapping("/logout")
    public String cerrarSesion(HttpSession httpSession) {
        buscarSesionActiva(httpSession).ifPresent(sesion -> {
            sesion.cerrar(LocalDateTime.now());
            sesionRepository.save(sesion);
        });
        httpSession.invalidate();
        return "redirect:/login";
    }

    private Optional<Sesion> buscarSesionActiva(HttpSession httpSession) {
        Object idSesion = httpSession.getAttribute(GestorConsultarSeguimientoBolsines.ATRIBUTO_ID_SESION);
        if (idSesion instanceof Long id) {
            return sesionRepository.findById(id).filter(Sesion::estaActiva);
        }
        return Optional.empty();
    }
}
