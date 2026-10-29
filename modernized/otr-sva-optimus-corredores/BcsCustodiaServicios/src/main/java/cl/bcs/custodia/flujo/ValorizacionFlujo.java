package cl.bcs.custodia.flujo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConfiguracionUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosFlujo;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Entrada;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Salida;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Totalizado;
import cl.bcs.custodia.flujo.kit.FechaLegacy;
import cl.bcs.custodia.flujo.kit.JsonFlujo;
import cl.bcs.custodia.flujo.kit.MensajeSalida;

/**
 * The valuation of the totalised lines (CUS-FLU-33..35), when {@code p_ValorizarFlujo = "S"}. ValorizarInstrumento is OUT of this slice: its request
 * and answer cross a port as JSON (the shape of the legacy {@code ValorizarInstrumentoEntrada} / {@code ValorizarInstrumentoSalida}). Lines with no
 * quantity, or fixed-income lines with no redemption value, are not valued; the market results ({@code DimensionTasa = MERC}) overwrite price, rate and
 * the two market amounts of the line with the same internal id. A valuation with errors REPLACES the messages of the response with its own (D-CUS-10).
 */
final class ValorizacionFlujo {

    /** Legacy {@code ValorizarInstrumentoEntrada}. */
    static class Solicitud {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public Long p_IdPais = -1L;
        public String p_IdMonedaAdicional = null;
        public List<Instrumento> p_Instrumentos = new ArrayList<>();
        public String p_OrigenProcesamiento = "";
    }

    /** Legacy {@code ObjEntradaInstrumentos}. */
    static class Instrumento {
        public Long IdValorizacion;
        public String OrigenProcesamiento;
        public String FechaValorizacion;
        public String FechaOperacion;
        public String FechaVencimientoOperacion;
        public Long PlazoEnDiasOperacion;
        public String IdClasificacionTipoInstrumento;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico = "";
        public String FechaVencimiento;
        public String FechaEmision;
        public String IdMonedaInstrumento;
        public String IdMonedaEmisionInstrumento;
        public String IdTipoReajusteInstrumento;
        public String Emisor;
        public String IdCondicionDeLiquidacion;
        public String FechaDeLiquidacion;

        public BigDecimal Cantidad;
        public BigDecimal ValorRescate;
        public BigDecimal PrecioCompra;
        public BigDecimal TasaCompra;
        public BigDecimal MontoCompra;
        public BigDecimal MontoCompraMonedaSistema;
        public BigDecimal PrecioSubyacente;
        public BigDecimal TasaSubyacente;
        public BigDecimal PrecioGarantia;
        public BigDecimal TasaGarantia;
        public BigDecimal TasaTIF;
        public Long IdPortafolio;

        public String TablaOrigen;
        public String IdUso;
        public String TipoCartera;
        public String IdTipoCriterioTasa;

        public String IdTipoOperacion;
        public String IdTipoReajusteCabecera = "";
        public BigDecimal MontoFinalSubyacente;
    }

    /** The part of legacy {@code ValorizarInstrumentoSalida} this slice reads. */
    static class Respuesta {
        public List<Resultado> P_OUT_INSTRUMENTOSVALORIZADOS = new ArrayList<>();
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR;
    }

    /** The part of legacy {@code ObjResultadoValorizacion} this slice reads. */
    static class Resultado {
        public Long IdValorizacion;
        public String DimensionTasa;
        public BigDecimal Precio;
        public BigDecimal Tasa;
        public BigDecimal MontoMonedaSistema;
        public BigDecimal MontoMonedaInstrumento;
    }

    private ValorizacionFlujo() {
    }

    static void valorizar(Function<JsonObject, JsonObject> valorizador, Entrada in, Salida out, Map<Long, DatosFlujo> instrumentos,
            List<ConfiguracionUso> usosConfigurados) {
        Solicitud vie = new Solicitud();
        vie.p_IdEmpresa = in.p_IdEmpresa;
        vie.p_IdUsuario = in.p_IdUsuario;
        vie.p_IdModulo = in.p_IdModulo;
        vie.p_IdPais = in.p_IdPais;
        vie.p_OrigenProcesamiento = "FLUJOCUSTODIA";

        for (Totalizado rec : out.p_out_FlujoTotalizado) {
            if (rec.Cantidad.equals(BigDecimal.ZERO)
                    || (rec.ValorRescate.equals(BigDecimal.ZERO) && rec.IdClasificacionTipoInstrumento.equalsIgnoreCase("IF"))) {
                continue;
            }
            DatosFlujo ins = instrumentos.get(rec.IdInstrumento);
            ConfiguracionUso uso = new ConfiguracionUso();
            for (ConfiguracionUso ufc : usosConfigurados) {
                if (ufc.UsoTipoOperacion.equals(rec.UsoTipoOperacion)) {
                    uso = ufc;
                    break;
                }
            }
            Instrumento i = new Instrumento();
            i.IdValorizacion = rec.IdProcesamientoInterno;
            i.FechaValorizacion = FechaLegacy.formato(ins.FechaLiquidacionProceso == null ? rec.FechaLiquidacionInstrumento : ins.FechaLiquidacionProceso,
                    "yyyy-MM-dd");
            i.IdClasificacionTipoInstrumento = ins.IdClasificacionTipoInstrumento;
            i.IdTipoInstrumento = ins.IdTipoInstrumento;
            i.IdInstrumento = rec.IdInstrumento;
            i.Nemotecnico = rec.Nemotecnico;
            i.FechaOperacion = FechaLegacy.formato(ins.FechaEjecucion, "yyyy-MM-dd");
            i.FechaVencimiento = FechaLegacy.formato(
                    ins.FechaVencimientoInstrumento == null ? rec.FechaVencimientoInstrumento : ins.FechaVencimientoInstrumento, "yyyy-MM-dd");
            i.FechaEmision = FechaLegacy.formato(ins.FechaEmisionInstrumento == null ? rec.FechaEmisionInstrumento : ins.FechaEmisionInstrumento,
                    "yyyy-MM-dd");
            i.IdMonedaInstrumento = ins.IdMonedaInstrumento;
            i.IdTipoReajusteInstrumento = ins.IdTipoReajuste;
            i.Cantidad = rec.Cantidad;
            i.TasaCompra = ins.TasaMercado;
            i.ValorRescate = rec.ValorRescate;
            i.IdPortafolio = rec.IdPortafolio;
            i.Emisor = rec.IdEmisor;
            i.IdMonedaEmisionInstrumento = rec.IdMonedaEmisionInstrumento == null ? rec.IdTipoReajuste : rec.IdMonedaEmisionInstrumento;
            i.TablaOrigen = uso.UsoValorizacion;
            vie.p_Instrumentos.add(i);
        }

        if (vie.p_Instrumentos.isEmpty()) {
            return;
        }
        JsonObject respuestaJson = valorizador.apply(JsonFlujo.legacy().toJsonTree(vie).getAsJsonObject());
        Respuesta respuesta = JsonFlujo.legacy().fromJson(respuestaJson, Respuesta.class);

        if (!respuesta.P_OUT_ERRORESCONTADOR.equals(0L)) {
            out.P_OUT_ERRORESCONTADOR = 1L;
            out.P_OUT_MENSAJES = respuesta.P_OUT_MENSAJES;
        }

        Map<Long, Resultado> mercado = new HashMap<>();
        for (Resultado r : respuesta.P_OUT_INSTRUMENTOSVALORIZADOS.stream().filter(rv -> "MERC".equals(rv.DimensionTasa)).collect(Collectors.toList())) {
            mercado.put(r.IdValorizacion, r);
        }
        for (Totalizado rec : out.p_out_FlujoTotalizado) {
            Resultado r = mercado.get(rec.IdProcesamientoInterno);
            if (r != null) {
                rec.PrecioMercado = r.Precio;
                rec.TasaMercado = r.Tasa;
                rec.MontoAMercadoMonedaInstrumento = r.MontoMonedaInstrumento;
                rec.MontoAMercadoMonedaSistema = r.MontoMonedaSistema;
            }
        }
    }
}
