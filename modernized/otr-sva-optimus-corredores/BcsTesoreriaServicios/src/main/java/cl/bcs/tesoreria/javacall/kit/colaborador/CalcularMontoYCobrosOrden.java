package cl.bcs.tesoreria.javacall.kit.colaborador;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.kit.JsonLegacy;
import cl.bcs.tesoreria.javacall.kit.MensajeSalida;
import cl.bcs.tesoreria.port.out.Colaboradores;
import cl.bcs.utils.BcsPojo;
import cl.bcs.utils.Utils;

/**
 * Contract of the cl.bcs.ordenes.CalcularMontoYCobrosOrden (only the members ProcesarDistribucion uses) as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CalcularMontoYCobrosOrden {

    private CalcularMontoYCobrosOrden() {
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

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static CalcularMontoYCobrosOrden_Salida invocar(Colaboradores colaboradores, CalcularMontoYCobrosOrden_Entrada entrada) {
        JsonObject r = colaboradores.invocar("CalcularMontoYCobrosOrden", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, CalcularMontoYCobrosOrden_Salida.class);
    }
}
