package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;

/** Row / parameter classes of CrearRecaudacionDeposito (legacy nested POJOs; public fields are the SQL parameter and column names). */
public final class DepositoFilas {

    private DepositoFilas() {
    }

    public static class Recaudacion {
        public Long IdInterno = 0L;
        public Long IdRecaudacion;
        public Long IdEmpresa;
        public String CodigoArchivo;
        public Long FolioCartola;
        public Long LineaCartola;
        public String Sucursal;
        public String FechaDeposito;
        public Long IdCuentaBanco = 0L;
        public Long IdBanco;
        public String CuentaCorriente;
        public BigDecimal MontoDeposito;
        public String ConceptoRecaudacion;
        public String Origen;
        public String Observacion;
        public Long NumeroDocumento;
        public String IdTipoDeposito;
        public Long IdRegimenTributario;
        public Long IdCarga;
        public String IdEstado;
    }

    public static class DatosEmpresa {
        public Long IdPersona;
        public Long IdPortafolioDefecto;
    }

    public static class CuentaBanco {
        public Long IdCuentaBanco;
        public Long IdBanco;
        public String NumeroCuentaBancaria;
    }

    public static class DatosEntidad {
        public Long IdPersonaEntidad;
        public Long IdContratoSAAPV;
    }

    public static class DatosBanco {
        public Long IdPersonaBanco;
        public Long IdBanco;
    }

    public static class CajaTesoreria {
        public Long IdCaja;
        public String Codigo;
        public String Nombre;
    }
}
