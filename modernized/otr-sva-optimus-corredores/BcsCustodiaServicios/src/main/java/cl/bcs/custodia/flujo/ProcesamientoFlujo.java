package cl.bcs.custodia.flujo;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.ConfiguracionUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosFlujo;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Detallado;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.TipoInstrumentoParaGarantia;
import cl.bcs.custodia.flujo.kit.FechaLegacy;

/**
 * What happens to the rows once read (CUS-FLU-24..28): each real row takes the process values and the operator sign, may be discarded (type D, with a
 * reason), the detail of the response is the real rows sorted by {@code IdRegistro}, and every real row is projected to every other requested date.
 * No I/O; null handling as the legacy (a null where it dereferenced one still fails).
 */
final class ProcesamientoFlujo {

    private ProcesamientoFlujo() {
    }

    /** What the pass needs besides the rows. */
    record Parametros(Timestamp fechaEjecucion, Long keyFlujo, java.sql.Date fechaMinima, String valorizarFlujo, Set<Long> portafoliosCuentaPropia,
            Map<String, TipoInstrumentoParaGarantia> tiposParaGarantia, Map<String, ConfiguracionUso> itemsPorUsoYCalculo) {
    }

    /**
     * CUS-FLU-24/25 on every real row, in place. Returns the keys of the instruments of ALL rows (type of specific instrument # type # instrument),
     * in {@code HashSet} order: they feed the underlying query (CUS-FLU-26).
     */
    static List<String> marcar(List<DatosFlujo> lista, Parametros p) {
        Set<String> instrumentos = new HashSet<>();
        for (DatosFlujo rec : lista) {
            if (rec.TipoRegistro.equals("R")) {
                rec.FechaEjecucion = p.fechaEjecucion();
                rec.KeyFlujoCustodia = p.keyFlujo();
                rec.FechaLiquidacionProceso = rec.FechaLiquidacion.after(p.fechaMinima()) ? rec.FechaLiquidacion : p.fechaMinima();
                rec.ValorizarFlujo = p.valorizarFlujo();
                rec.EsCuentaPropia = p.portafoliosCuentaPropia().contains(rec.IdPortafolio) ? "S" : "N";

                ConfiguracionUso item = p.itemsPorUsoYCalculo().get(rec.IdUsoFlujoCustodia + "#" + rec.ItemCalculo);
                rec.FuncionOperador = item == null ? "" : item.FuncionOperador;

                rec.CantidadProceso = FlujoCustodiaReglas.aplicarOperador(rec.FuncionOperador, rec.Cantidad);
                rec.ValorRescateProceso = FlujoCustodiaReglas.aplicarOperador(rec.FuncionOperador, rec.ValorRescate);
                rec.PrecioCompraProceso = rec.PrecioCompra;
                rec.TasaCompraProceso = rec.TasaCompra;
                rec.PrecioMercadoProceso = rec.PrecioMercado;
                rec.TasaMercadoProceso = rec.TasaMercado;

                TipoInstrumentoParaGarantia tipg = p.tiposParaGarantia().get(rec.IdTipoInstrumento + "#" + rec.IdTipoInstrumentoEspecifico);
                String motivo = FlujoCustodiaReglas.motivoDescarte(rec.FuncionOperador, rec.TipoCarteraCorredor, rec.UsoTipoOperacion,
                        rec.BusquedaEspecifica, tipg != null && tipg.IdTipoInstrumentoAval != null);
                if (!motivo.isEmpty()) {
                    rec.TipoRegistro = "D";
                    rec.MotivoEliminacioRegistro = motivo;
                }
            }

            String clave = "";
            clave += rec.IdTipoInstrumentoEspecifico == null ? "NULL#" : rec.IdTipoInstrumentoEspecifico + "#";
            clave += rec.IdTipoInstrumento == null ? "NULL#" : rec.IdTipoInstrumento + "#";
            clave += rec.IdInstrumento == null ? "-1" : rec.IdInstrumento;
            instrumentos.add(clave);
        }
        return new ArrayList<>(instrumentos);
    }

    /**
     * CUS-FLU-26: with {@code SOLO_SUBYACENTE}, a row whose instrument is not one of the underlyings found is discarded. Applies to every row of the
     * list, also the matrix rows and the ones already discarded (the reason is overwritten), and only when at least one underlying was found.
     */
    static void descartarNoSubyacentes(List<DatosFlujo> lista, List<DatosFlujo> subyacentes) {
        List<Long> detectados = subyacentes.stream().map(a -> a.IdInstrumento).distinct().collect(Collectors.toList());
        if (detectados.isEmpty()) {
            return;
        }
        for (DatosFlujo rec : lista) {
            int coincidencia = 0;
            for (Long encontrado : detectados) {
                if (encontrado.equals(rec.IdInstrumento)) {
                    if ("R".equals(rec.TipoRegistro) && rec.IdInstrumento == null && "SOLO_SUBYACENTE".equals(rec.BusquedaEspecifica)) {
                        rec.TipoRegistro = "D";
                        rec.MotivoEliminacioRegistro = "CONFIGURACION_SUBYACENTE_EXCLUYE_INSTRUMENTO";
                    }
                    coincidencia++;
                }
            }
            if ("SOLO_SUBYACENTE".equals(rec.BusquedaEspecifica) && coincidencia == 0) {
                rec.TipoRegistro = "D";
                rec.MotivoEliminacioRegistro = "INSTRUMENTO_NO_SUBYACENTE";
            }
        }
    }

    /**
     * CUS-FLU-27/28: the detail is every real row (sorted by {@code IdRegistro}); each real row is copied (type P) to every requested date other than
     * its process liquidation date, with quantities, prices and rates at 0 when the date is before the liquidation. The projected rows are returned
     * (the caller appends them to the list), each with the next id of the sequence.
     */
    static List<DatosFlujo> proyectar(List<DatosFlujo> lista, List<String> fechas, Iterator<Long> ids, List<Detallado> detalle) {
        List<DatosFlujo> proyectados = new ArrayList<>();
        for (DatosFlujo rec : lista) {
            if (rec.TipoRegistro.equals("R")) {
                detalle.add(aDetallado(rec));
            }
            for (String fecha : fechas) {
                if (rec.TipoRegistro.equals("R") && !(rec.FechaLiquidacionProceso.toString()).equals(fecha)) {
                    java.sql.Date fechaD = FechaLegacy.aFechaSql(fecha);
                    DatosFlujo dfc = copiar(rec);
                    dfc.IdDatosFlujoCustodia = ids.next();
                    if (dfc.FechaLiquidacionProceso.after(fechaD)) {
                        dfc.CantidadProceso = new BigDecimal(0);
                        dfc.ValorRescateProceso = new BigDecimal(0);
                        dfc.PrecioCompraProceso = new BigDecimal(0);
                        dfc.TasaCompraProceso = new BigDecimal(0);
                        dfc.PrecioMercadoProceso = new BigDecimal(0);
                        dfc.TasaMercadoProceso = new BigDecimal(0);
                    }
                    dfc.FechaLiquidacionProceso = fechaD;
                    dfc.TipoRegistro = "P";
                    proyectados.add(dfc);
                }
            }
        }
        Collections.sort(detalle, (a, b) -> a.IdRegistro.compareTo(b.IdRegistro));
        return proyectados;
    }

    private static Detallado aDetallado(DatosFlujo rec) {
        Detallado d = new Detallado();
        d.IdCliente = rec.IdCliente;
        d.IdPortafolio = rec.IdPortafolio;
        d.IdClasificacionTipoInstrumento = rec.IdClasificacionTipoInstrumento;
        d.IdTipoInstrumento = rec.IdTipoInstrumento;
        d.IdInstrumento = rec.IdInstrumento;
        d.TipoValorizacion = rec.TipoValorizacion;
        d.IdEmisor = rec.IdEmisor;
        d.Nemotecnico = rec.Nemotecnico;
        d.NemoSVS = rec.NemoSVS;
        d.CorteMinimo = rec.CorteMinimo;
        d.IdMonedaInstrumento = rec.IdMonedaInstrumento;
        d.IdTipoReajuste = rec.IdTipoReajuste;
        d.FechaEmisionInstrumento = rec.FechaEmisionInstrumento;
        d.FechaVencimientoInstrumento = rec.FechaVencimientoInstrumento;
        d.CodigoIFRS = rec.CodigoIFRS;
        d.EsFungible = rec.EsFungible;
        d.IdBoveda = rec.IdBoveda;
        d.IdBolsa = rec.IdBolsa;
        d.IdTipoOperacion = rec.IdTipoOperacion;
        d.Accion = rec.Accion;
        d.IdTipoProducto = rec.IdTipoProducto;
        d.IdMesa = rec.IdMesa;
        d.FechaLiquidacion = rec.FechaLiquidacion;
        d.Cantidad = rec.Cantidad;
        d.ValorRescate = rec.ValorRescate;
        d.PrecioCompra = rec.PrecioCompra;
        d.TasaCompra = rec.TasaCompra;
        d.MontoATasaCompra = rec.MontoATasaCompra;
        d.PrecioMercado = rec.PrecioMercado;
        d.TasaMercado = rec.TasaMercado;
        d.MontoATasaMercado = rec.MontoATasaMercado;
        d.PrecioVector = rec.PrecioVector;
        d.MontoVector = rec.MontoVector;
        d.IdRegistro = rec.IdRegistro;
        d.FolioRegistro = rec.FolioRegistro;
        d.IdConcepto = rec.IdConcepto;
        d.IdDatosFlujoCustodia = rec.IdDatosFlujoCustodia;
        d.IdUsoFlujoCustodia = rec.IdUsoFlujoCustodia;
        d.UsoTipoOperacion = rec.UsoTipoOperacion;
        d.BusquedaEspecifica = rec.BusquedaEspecifica;
        d.ItemCalculo = rec.ItemCalculo;
        d.ItemOrigen = rec.ItemOrigen;
        d.FuncionOperador = rec.FuncionOperador;
        d.CantidadProceso = rec.CantidadProceso;
        d.ValorRescateProceso = rec.ValorRescateProceso;
        d.PrecioCompraProceso = rec.PrecioCompraProceso;
        d.TasaCompraProceso = rec.TasaCompraProceso;
        d.PrecioMercadoProceso = rec.PrecioMercadoProceso;
        d.TasaMercadoProceso = rec.TasaMercadoProceso;
        d.FechaLiquidacionProceso = rec.FechaLiquidacionProceso;
        d.RequiereValorizacion = rec.RequiereValorizacion;
        d.ValorizarFlujo = rec.ValorizarFlujo;
        d.IdRegimenFondoMutuo = rec.IdRegimenFondoMutuo;
        d.GrupoRegimen = rec.GrupoRegimen;
        d.FechaOrigenCompra = rec.FechaOrigenCompra;
        d.FechaOperacion = rec.FechaOperacion;
        d.IdContraparte = rec.IdContraparte;
        d.IdCorredorContraparte = rec.IdCorredorContraparte;
        return d;
    }

    /** The projected copy (legacy field list: the row's own data; the counterpart and operation date of the detail are not carried). */
    private static DatosFlujo copiar(DatosFlujo r) {
        DatosFlujo c = new DatosFlujo();
        c.IdDatosFlujoCustodia = r.IdDatosFlujoCustodia;
        c.KeyFlujoCustodia = r.KeyFlujoCustodia;
        c.IdUsoFlujoCustodia = r.IdUsoFlujoCustodia;
        c.UsoTipoOperacion = r.UsoTipoOperacion;
        c.BusquedaEspecifica = r.BusquedaEspecifica;
        c.ItemCalculo = r.ItemCalculo;
        c.ItemOrigen = r.ItemOrigen;
        c.FuncionOperador = r.FuncionOperador;
        c.FechaEjecucion = r.FechaEjecucion;
        c.IdCliente = r.IdCliente;
        c.IdPortafolio = r.IdPortafolio;
        c.EsCuentaPropia = r.EsCuentaPropia;
        c.IdRegistro = r.IdRegistro;
        c.FolioRegistro = r.FolioRegistro;
        c.IdConcepto = r.IdConcepto;
        c.IdClasificacionTipoInstrumento = r.IdClasificacionTipoInstrumento;
        c.IdTipoInstrumento = r.IdTipoInstrumento;
        c.IdInstrumento = r.IdInstrumento;
        c.TipoValorizacion = r.TipoValorizacion;
        c.IdEmisor = r.IdEmisor;
        c.Nemotecnico = r.Nemotecnico;
        c.NemoSVS = r.NemoSVS;
        c.CorteMinimo = r.CorteMinimo;
        c.IdMonedaInstrumento = r.IdMonedaInstrumento;
        c.IdTipoReajuste = r.IdTipoReajuste;
        c.FechaEmisionInstrumento = r.FechaEmisionInstrumento;
        c.FechaVencimientoInstrumento = r.FechaVencimientoInstrumento;
        c.CodigoIFRS = r.CodigoIFRS;
        c.EsFungible = r.EsFungible;
        c.TipoCarteraCorredor = r.TipoCarteraCorredor;
        c.IdBoveda = r.IdBoveda;
        c.IdTipoInstrumentoEspecifico = r.IdTipoInstrumentoEspecifico;
        c.IdBolsa = r.IdBolsa;
        c.IdTipoOperacion = r.IdTipoOperacion;
        c.Accion = r.Accion;
        c.IdTipoProducto = r.IdTipoProducto;
        c.IdMesa = r.IdMesa;
        c.FechaLiquidacion = r.FechaLiquidacion;
        c.Cantidad = r.Cantidad;
        c.ValorRescate = r.ValorRescate;
        c.PrecioCompra = r.PrecioCompra;
        c.TasaCompra = r.TasaCompra;
        c.MontoATasaCompra = r.MontoATasaCompra;
        c.PrecioMercado = r.PrecioMercado;
        c.TasaMercado = r.TasaMercado;
        c.MontoATasaMercado = r.MontoATasaMercado;
        c.PrecioVector = r.PrecioVector;
        c.MontoVector = r.MontoVector;
        c.CantidadProceso = r.CantidadProceso;
        c.ValorRescateProceso = r.ValorRescateProceso;
        c.PrecioCompraProceso = r.PrecioCompraProceso;
        c.TasaCompraProceso = r.TasaCompraProceso;
        c.MontoCompraProcesoMonedaSistema = r.MontoCompraProcesoMonedaSistema;
        c.MontoCompraProcesoMonedaInstrumento = r.MontoCompraProcesoMonedaInstrumento;
        c.PrecioMercadoProceso = r.PrecioMercadoProceso;
        c.TasaMercadoProceso = r.TasaMercadoProceso;
        c.MontoMercadoProcesoMonedaSistema = r.MontoMercadoProcesoMonedaSistema;
        c.MontoMercadoProcesoMonedaInstrumento = r.MontoMercadoProcesoMonedaInstrumento;
        c.FechaLiquidacionProceso = r.FechaLiquidacionProceso;
        c.RequiereValorizacion = r.RequiereValorizacion;
        c.TipoRegistro = r.TipoRegistro;
        c.MotivoEliminacioRegistro = r.MotivoEliminacioRegistro;
        c.ValorizarFlujo = r.ValorizarFlujo;
        c.VALTipoCartera = r.VALTipoCartera;
        c.VALUsoCriterio = r.VALUsoCriterio;
        c.VALIdTipoCriterioTasa = r.VALIdTipoCriterioTasa;
        c.VALPrecioCompra = r.VALPrecioCompra;
        c.VALTasaCompra = r.VALTasaCompra;
        c.VALMontoATasaCompraMonedaInstrumento = r.VALMontoATasaCompraMonedaInstrumento;
        c.VALMontoATasaCompraMonedaSistema = r.VALMontoATasaCompraMonedaSistema;
        c.VALPrecioMercado = r.VALPrecioMercado;
        c.VALTasaMercado = r.VALTasaMercado;
        c.VALMontoATasaMercadoMonedaInstrumento = r.VALMontoATasaMercadoMonedaInstrumento;
        c.VALMontoATasaMercadoMonedaSistema = r.VALMontoATasaMercadoMonedaSistema;
        c.MontoCompraMonedaLocalProceso = r.MontoCompraMonedaLocalProceso;
        c.IdAccionOperacion = r.IdAccionOperacion;
        c.IdMonedaEmision = r.IdMonedaEmision;
        c.IdRegimenFondoMutuo = r.IdRegimenFondoMutuo;
        c.EsVentaDescubierta = r.EsVentaDescubierta;
        c.GrupoRegimen = r.GrupoRegimen;
        c.FechaOrigenCompra = r.FechaOrigenCompra;
        return c;
    }
}
