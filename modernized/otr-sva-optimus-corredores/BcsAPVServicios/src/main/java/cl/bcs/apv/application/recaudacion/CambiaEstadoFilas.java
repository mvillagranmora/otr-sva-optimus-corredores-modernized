package cl.bcs.apv.application.recaudacion;

import java.sql.Date;

/** Parameter rows of the three state-change statements (legacy {@code ProcesarDistribucion.Recaudacion_CambiaEstado} and siblings): public fields are the SQL parameter names. */
public final class CambiaEstadoFilas {

    private CambiaEstadoFilas() {
    }

    public static class Recaudacion {
        public Long IdRecaudacion;
        public String FolioCartola;
        public Long LineaCartola;
        public Long Sucursal;
        public Date FechaDeposito;
        public String CuentaCorriente;
        public Long MontoDeposito;
        public Long ConceptoRecaudacion;
        public Long Origen;
        public Long Observacion;
        public Long NumeroDocumento;
        public Long IdTipoDeposito;
        public Long IdBanco;
        public Long IdRegimenTributario;
        public Long IdCarga;
        public String IdEstado;
        public Long IdEmpresa;
    }

    public static class DetalleCliente {
        public Long IdRecaudacionCliente;
        public String IdEstado;
        public Long IdEmpresa;
    }

    public static class DetalleEmpleador {
        public Long IdRecaudacionEmpleador;
        public String IdEstado;
        public Long IdEmpresa;
    }
}
