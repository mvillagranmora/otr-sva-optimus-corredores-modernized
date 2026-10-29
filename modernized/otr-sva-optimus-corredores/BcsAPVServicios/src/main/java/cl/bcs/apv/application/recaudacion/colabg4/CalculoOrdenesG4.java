package cl.bcs.apv.application.recaudacion.colabg4;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Verbatim legacy contract (field names = JSON names) of a collaborator outside APV; copied from the legacy source through the oracle shim. */
public final class CalculoOrdenesG4 {

    private CalculoOrdenesG4() {
    }

    public static class Orden_Entrada {
    		public BigDecimal PrecioOrden;
    		public Long IdInterno;
    		public BigDecimal CantidadOrden;
    		public String EsFungible;
    		public String IdBolsa;
    		public Long IdCorredorCorresponsal;
    		public Long IdInstrumento;
    		public Long IdInstrumentoSubyacente;
    		public String IdMonedaLiquidacionOrden;
    		public Long IdPortafolio;
    		public String IdTipoProducto;
    		public String IdTipoOperacion;
    		public String IdTipoCanalIngreso;
    		public String FechaOperacion;
    		public BigDecimal MontoOrden;
    		public BigDecimal TasaSimultanea;
    		public BigDecimal TasaRFIF;
    		public BigDecimal TasaVentaCorta;
    		public BigDecimal TipoCambio;
    		public BigDecimal ValorRescateOrden;
    		public Long PlazoSimultanea;
    		public Long PlazoVentaCorta;
    		public Long PlazoIF;
    		public String EsAPlazo;
    		public BigDecimal MontoTotal;
    		public String EsPorMonto;
    		public BigDecimal TasaReporte;
    		public BigDecimal TasaReportado;
    		public Long PlazoReporte;
    		public Long BaseTasa;
    		public String IdEmisor;
    	}

    public static class OrdenSalida {
    		public Long IdInterno;
    		public BigDecimal CantidadOperacion;
    	}

    public static class CalcularMontoYCobrosOrden_Entrada {
    		public Long p_IdEmpresa;
    		public Long p_IdUsuario;
    		public String p_IdModulo;
    		public List<Orden_Entrada> p_Ordenes = new ArrayList<>();
    	}

    public static class CalcularMontoYCobrosOrden_Salida {
    		public List<OrdenSalida> p_out_Ordenes = new ArrayList<>();
    	}


}
