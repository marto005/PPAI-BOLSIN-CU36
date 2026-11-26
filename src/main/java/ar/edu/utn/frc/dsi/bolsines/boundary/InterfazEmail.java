package ar.edu.utn.frc.dsi.bolsines.boundary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Boundary con el servidor de correo. Para la entrega el envío está SIMULADO:
 * el correo se escribe en la consola de la aplicación en lugar de enviarse.
 */
@Component
public class InterfazEmail {

    private static final Logger LOG = LoggerFactory.getLogger(InterfazEmail.class);

    public boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        LOG.info("""

                ==================== CORREO (envío simulado) ====================
                Para:   {}
                Asunto: {}
                {}
                =================================================================""", destinatario, asunto, cuerpo);
        return true;
    }
}
