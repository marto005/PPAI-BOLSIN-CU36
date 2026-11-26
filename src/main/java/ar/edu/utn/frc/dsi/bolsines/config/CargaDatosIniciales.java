package ar.edu.utn.frc.dsi.bolsines.config;

import ar.edu.utn.frc.dsi.bolsines.entidad.Bolsin;
import ar.edu.utn.frc.dsi.bolsines.entidad.Cargo;
import ar.edu.utn.frc.dsi.bolsines.entidad.ComisionMedica;
import ar.edu.utn.frc.dsi.bolsines.entidad.Empleado;
import ar.edu.utn.frc.dsi.bolsines.entidad.Estado;
import ar.edu.utn.frc.dsi.bolsines.entidad.Usuario;
import ar.edu.utn.frc.dsi.bolsines.repositorio.BolsinRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.CargoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.ComisionMedicaRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EmpleadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.EstadoRepository;
import ar.edu.utn.frc.dsi.bolsines.repositorio.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Carga los datos de prueba la primera vez que se levanta la aplicación (si la base está vacía).
 * <p>
 * Los números de bolsín y los códigos de CM coinciden con los datos de la API mock del GPS Tracker
 * (dsi-api-bolsines), salvo los casos armados a propósito para probar el flujo alternativo A3.
 */
@Component
public class CargaDatosIniciales implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(CargaDatosIniciales.class);

    private final EstadoRepository estadoRepository;
    private final CargoRepository cargoRepository;
    private final ComisionMedicaRepository comisionMedicaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmpleadoRepository empleadoRepository;
    private final BolsinRepository bolsinRepository;

    private final Map<String, Estado> estados = new HashMap<>();
    private final Map<String, ComisionMedica> comisiones = new HashMap<>();

    public CargaDatosIniciales(EstadoRepository estadoRepository, CargoRepository cargoRepository,
                               ComisionMedicaRepository comisionMedicaRepository, UsuarioRepository usuarioRepository,
                               EmpleadoRepository empleadoRepository, BolsinRepository bolsinRepository) {
        this.estadoRepository = estadoRepository;
        this.cargoRepository = cargoRepository;
        this.comisionMedicaRepository = comisionMedicaRepository;
        this.usuarioRepository = usuarioRepository;
        this.empleadoRepository = empleadoRepository;
        this.bolsinRepository = bolsinRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (estadoRepository.count() > 0) {
            LOG.info("La base de datos ya tiene datos: no se cargan los datos de prueba.");
            return;
        }
        cargarEstados();
        cargarComisionesMedicas();
        cargarEmpleados();
        cargarBolsines();
        LOG.info("Datos de prueba cargados en la base H2.");
    }

    private void cargarEstados() {
        estado(Estado.CREADO, "Bolsín generado, abierto para agregar remitos");
        estado(Estado.CERRADO, "Bolsín cerrado con precinto");
        estado(Estado.ENVIADO, "Bolsín retirado por el correo, en viaje a la CM destino");
        estado(Estado.RECIBIDO_EN_CM_DESTINO, "Bolsín recibido en la CM destino");
        estado(Estado.DE_BAJA, "Bolsín eliminado");
    }

    private void estado(String nombre, String descripcion) {
        estados.put(nombre, estadoRepository.save(new Estado(Estado.AMBITO_BOLSIN, nombre, descripcion)));
    }

    private void cargarComisionesMedicas() {
        cm("CM-CBA-01", "CM Córdoba Capital", "Av. Colón 1200, Córdoba", "cm.cordoba@comisionmedica.test", "0351-4100001");
        cm("CM-CBA-02", "CM Río Cuarto", "Constitución 850, Río Cuarto", "cm.riocuarto@comisionmedica.test", "0358-4100002");
        cm("CM-ROS-02", "CM Rosario", "Bv. Oroño 1500, Rosario", "cm.rosario@comisionmedica.test", "0341-4100003");
        cm("CM-MDZ-03", "CM Mendoza", "San Martín 1100, Mendoza", "cm.mendoza@comisionmedica.test", "0261-4100004");
        cm("CM-BA-01", "CM Central CABA", "Av. Corrientes 3000, CABA", "cm.caba@comisionmedica.test", "011-41000005");
        cm("CM-TUC-01", "CM Tucumán", "24 de Septiembre 600, S. M. de Tucumán", "cm.tucuman@comisionmedica.test", "0381-4100006");
        cm("CM-SLA-01", "CM Salta", "Belgrano 900, Salta", "cm.salta@comisionmedica.test", "0387-4100007");
        cm("CM-JUJ-01", "CM Jujuy", "Belgrano 450, S. S. de Jujuy", "cm.jujuy@comisionmedica.test", "0388-4100008");
        cm("CM-NQN-01", "CM Neuquén", "Av. Argentina 300, Neuquén", "cm.neuquen@comisionmedica.test", "0299-4100009");
        cm("CM-MDQ-01", "CM Mar del Plata", "Av. Independencia 2000, Mar del Plata", "cm.mardelplata@comisionmedica.test", "0223-4100010");
        cm("CM-LPL-01", "CM La Plata", "Calle 7 N° 900, La Plata", "cm.laplata@comisionmedica.test", "0221-4100011");
    }

    private void cm(String codigo, String nombre, String direccion, String email, String telefono) {
        comisiones.put(codigo, comisionMedicaRepository.save(new ComisionMedica(codigo, nombre, direccion, email, telefono)));
    }

    private void cargarEmpleados() {
        Cargo encargado = cargoRepository.save(new Cargo(Cargo.ENCARGADO_DE_BOLSINES,
                "Arma, envía y recibe los bolsines de la CM"));
        Cargo gerente = cargoRepository.save(new Cargo(Cargo.GERENTE_DE_CM, "Gerente de la Comisión Médica"));

        // Encargados de Bolsines con usuario (los que inician sesión)
        empleado("Lucía", "Fernández", "lfernandez@comisionmedica.test", "CM-CBA-01", encargado, "eb.cordoba");
        empleado("Tomás", "Ríos", "trios@comisionmedica.test", "CM-ROS-02", encargado, "eb.rosario");
        empleado("Valeria", "Quispe", "vquispe@comisionmedica.test", "CM-JUJ-01", encargado, "eb.jujuy");

        // Un Gerente por cada CM (destinatarios del correo del CU 31)
        String[][] gerentes = {
                {"Martín", "Paz", "CM-CBA-01", "gerente.cordoba"},
                {"Ana", "Sosa", "CM-CBA-02", "gerente.riocuarto"},
                {"Carlos", "Medina", "CM-ROS-02", "gerente.rosario"},
                {"Julieta", "Ortiz", "CM-MDZ-03", "gerente.mendoza"},
                {"Diego", "Romero", "CM-BA-01", "gerente.caba"},
                {"Paula", "Herrera", "CM-TUC-01", "gerente.tucuman"},
                {"Sergio", "Luna", "CM-SLA-01", "gerente.salta"},
                {"Natalia", "Vera", "CM-JUJ-01", "gerente.jujuy"},
                {"Pablo", "Acosta", "CM-NQN-01", "gerente.neuquen"},
                {"Laura", "Benítez", "CM-MDQ-01", "gerente.mardelplata"},
                {"Gustavo", "Molina", "CM-LPL-01", "gerente.laplata"},
        };
        for (String[] g : gerentes) {
            empleadoRepository.save(new Empleado(g[0], g[1], g[3] + "@comisionmedica.test", comisiones.get(g[2]), gerente, null));
        }
    }

    private void empleado(String nombre, String apellido, String email, String codigoCM, Cargo cargo, String nombreUsuario) {
        Usuario usuario = usuarioRepository.save(new Usuario(nombreUsuario, "1234"));
        empleadoRepository.save(new Empleado(nombre, apellido, email, comisiones.get(codigoCM), cargo, usuario));
    }

    private void cargarBolsines() {
        // --- CM Córdoba Capital (usuario eb.cordoba) ---
        // 101: coincide con la API mock -> se ubica con los 3 modelos de GPS Tracker
        bolsin(101, "PR-0101", "CM-CBA-01", "CM-ROS-02", 1250, Estado.ENVIADO);
        // 110: en la API mock figura con CM origen CM-CBA-02. El XTR-4500L valida la CM origen
        //      -> no lo ubica (A3). NavTrack QX-7A (valida CM destino) y GeoPulse MTR-900 sí lo ubican.
        bolsin(110, "PR-0110", "CM-CBA-01", "CM-ROS-02", 980, Estado.ENVIADO);
        // 111: no está registrado en la API mock -> ningún modelo lo puede ubicar (A3)
        bolsin(111, "PR-0111", "CM-CBA-01", "CM-MDZ-03", 1430, Estado.ENVIADO);
        // 120 y 121: de la misma CM pero en otros estados -> no se consultan
        bolsin(120, "PR-0120", "CM-CBA-01", "CM-BA-01", 760, Estado.CERRADO);
        bolsin(121, "PR-0121", "CM-CBA-01", "CM-TUC-01", 1100, Estado.RECIBIDO_EN_CM_DESTINO);

        // --- Resto de las CM: iguales a la API mock ---
        bolsin(102, "PR-0102", "CM-ROS-02", "CM-MDZ-03", 1320, Estado.ENVIADO);   // usuario eb.rosario
        bolsin(103, "PR-0103", "CM-MDZ-03", "CM-BA-01", 870, Estado.ENVIADO);
        bolsin(104, "PR-0104", "CM-BA-01", "CM-CBA-01", 1510, Estado.ENVIADO);
        bolsin(105, "PR-0105", "CM-TUC-01", "CM-SLA-01", 640, Estado.ENVIADO);
        bolsin(106, "PR-0106", "CM-SLA-01", "CM-JUJ-01", 1180, Estado.ENVIADO);
        bolsin(107, "PR-0107", "CM-NQN-01", "CM-MDQ-01", 905, Estado.ENVIADO);
        bolsin(108, "PR-0108", "CM-MDQ-01", "CM-BA-01", 1275, Estado.ENVIADO);
        bolsin(109, "PR-0109", "CM-LPL-01", "CM-CBA-01", 1390, Estado.ENVIADO);
        // CM Jujuy (usuario eb.jujuy) no tiene bolsines enviados -> A1
        bolsin(130, "PR-0130", "CM-JUJ-01", "CM-SLA-01", 820, Estado.CERRADO);
    }

    /** Crea el bolsín y le registra los cambios de estado hasta llegar al estado indicado. */
    private void bolsin(int numero, String precinto, String origen, String destino, double peso, String estadoFinal) {
        LocalDateTime base = LocalDateTime.of(2026, 3, 6, 9, 0).plusMinutes(numero);
        Bolsin bolsin = new Bolsin(numero, precinto, base.toLocalDate(), peso, comisiones.get(origen), comisiones.get(destino));
        String[] recorrido = {Estado.CREADO, Estado.CERRADO, Estado.ENVIADO, Estado.RECIBIDO_EN_CM_DESTINO};
        int horas = 0;
        for (String nombreEstado : recorrido) {
            bolsin.cambiarEstado(estados.get(nombreEstado), base.plusHours(horas));
            horas += 6;
            if (nombreEstado.equals(estadoFinal)) {
                break;
            }
        }
        bolsinRepository.save(bolsin);
    }
}
