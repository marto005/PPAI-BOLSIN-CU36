package ar.edu.utn.frc.dsi.bolsines.control;

import ar.edu.utn.frc.dsi.bolsines.boundary.InterfazEmail;
import org.springframework.stereotype.Component;

/**
 * Control del CU 31 Notificar ubicación de bolsín (incluido por el CU 36).
 * Informa por correo al Gerente de la CM destino el número de bolsín, la fecha y hora
 * de la última actualización y las coordenadas.
 */
@Component
public class GestorNotificarUbicacionBolsin {

    private final InterfazEmail interfazEmail;

    public GestorNotificarUbicacionBolsin(InterfazEmail interfazEmail) {
        this.interfazEmail = interfazEmail;
    }

    public boolean notificarUbicacionBolsin(String correoGerenteCMDestino, String numeroBolsin,
                                            String fechaHoraUltimaActualizacion, String latitud, String longitud) {
        String asunto = "Ubicación del bolsín N° " + numeroBolsin;
        String cuerpo = "Se informa la ubicación del bolsín N° " + numeroBolsin + " que se dirige a su comisión médica.\n"
                + "Última actualización: " + fechaHoraUltimaActualizacion + "\n"
                + "Coordenadas: latitud " + latitud + ", longitud " + longitud + "\n"
                + "Ver en el mapa: https://www.openstreetmap.org/?mlat=" + latitud + "&mlon=" + longitud + "#map=12/"
                + latitud + "/" + longitud;
        return interfazEmail.enviarCorreo(correoGerenteCMDestino, asunto, cuerpo);
    }
}
