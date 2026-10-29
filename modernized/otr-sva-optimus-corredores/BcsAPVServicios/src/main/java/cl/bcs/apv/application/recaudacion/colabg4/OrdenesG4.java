package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class OrdenesG4 {

    private OrdenesG4() {
    }

    /** Legacy PlataformaUtils.MensajeSalida as it travels in the JSON. */
    public static class Msg {
        public Long id;
        public String tipo;
        public String codigo;
        public String descripcion;
    }

    public static class ObjOrdenesEntrada {
    		public String NemotecnicoSVS = "";   // legacy CrearOrdenes.java:327 (the inferred Tesoreria shim lacked it; APV.ProcesarDistribucion:1201 reads it)
    		public Long IdInterno;
    		public String FechaRecepcion, FechaVigencia, FechaEmision, FechaOperacion, IdCondicionDeLiquidacion, IdTipoOperacion, IdTipoVigencia, Glosa;
    		public BigDecimal MontoNeto, Cantidad, ValorRescate, PrecioLimite, TipoCambio, TasaVentaCorta, TasaSimultanea, TasaRFIF;
    		public String EsPorMonto, IdBoveda, IdFormaCustodia, EsFacturaInmediata, IdTipoPrecio, IdTipoCanalIngreso, IdTipoReajuste, IdEmisor, IdBolsa, EspecificaBolsa;
    		public String IdMonedaLiquidacionOrden, IdCondicionDeLiquidacionPlazo, EsVentaCorta, EsOD, EsFungible, IdTipoLibroOrden, IdTipoCanalDistribucion, EsOrdenEspecial;
    		public String PagoCobroCuentaInversion, TipoInteres, IdMecanismoCaptacion;
    		public Long IdPorcentajeVisibilidad, IdPortafolio, IdInstrumento, IdInstrumentoSubyacente, PlazoVentaCorta, PlazoSimultanea, PlazoIF, IdLibroOrdenes;
    		public Long IdCuentaCustodioPortafolio, IdCorredorCorresponsal, IdPersonaAutorizadoDarOrdenes, IdPreferenciaPagoPortafolio, BaseTasa;
    	}

    public static class ObjRESULTADO {
    		public Long IdInterno;
    		public Long IdOrden;
    		public String DocumentoIdentidad;
    		public String CodigoPortafolio;
    		public Timestamp FechaOperacionOrden;
    		public BigDecimal Cantidad;
    	}

    public static class CrearOrdenesEntrada {
    		public Long p_IdEmpresa;
    		public Long p_IdUsuario;
    		public String p_IdModulo;
    		public String p_simularOperacion;
    		public String p_UsaReglasNegocio;
    		public List<ObjOrdenesEntrada> p_Ordenes = new ArrayList<>();
    	}

    public static class CrearOrdenesSalida {
    		public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
    		public Long P_OUT_ERRORESCONTADOR = 0L;
    		public List<ObjRESULTADO> P_OUT_RESULTADO = new ArrayList<>();
    	}


}
