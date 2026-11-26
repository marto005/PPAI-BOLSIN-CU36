package ar.edu.utn.frc.dsi.bolsines.entidad;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";

    public static final String CREADO = "Creado";
    public static final String CERRADO = "Cerrado";
    public static final String ENVIADO = "Enviado";
    public static final String RECIBIDO_EN_CM_DESTINO = "RecibidoEnCMDestino";
    public static final String DE_BAJA = "DeBaja";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ambito;
    private String nombre;
    private String descripcion;

    protected Estado() {
    }

    public Estado(String ambito, String nombre, String descripcion) {
        this.ambito = ambito;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equalsIgnoreCase(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equalsIgnoreCase(ambito);
    }

    public boolean sosEnviado() {
        return esAmbitoBolsin() && ENVIADO.equalsIgnoreCase(nombre);
    }

    public Long getId() {
        return id;
    }

    public String getAmbito() {
        return ambito;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
