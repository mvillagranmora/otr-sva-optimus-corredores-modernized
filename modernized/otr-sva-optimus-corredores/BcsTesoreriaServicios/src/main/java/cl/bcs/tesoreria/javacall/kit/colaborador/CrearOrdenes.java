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
 * Contract of the cl.bcs.ordenes.CrearOrdenes (only the members ProcesarDistribucion uses) as ProcesarDistribucion uses it through the {@link Colaboradores} port.
 * The nested classes keep the legacy names and field names (they ARE the JSON contract).
 */
public final class CrearOrdenes {

    private CrearOrdenes() {
    }


    public static class ObjOrdenesEntrada {
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
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<ObjRESULTADO> P_OUT_RESULTADO = new ArrayList<>();
    }

    /** Calls the collaborator; the legacy {@code logica(con, entrada)}. */
    public static CrearOrdenesSalida invocar(Colaboradores colaboradores, CrearOrdenesEntrada entrada) {
        JsonObject r = colaboradores.invocar("CrearOrdenes", JsonLegacy.aJson(entrada));
        return JsonLegacy.de(r, CrearOrdenesSalida.class);
    }
}
