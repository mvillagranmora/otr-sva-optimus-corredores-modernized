package cl.bcs.custodia.flujo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.custodia.flujo.FlujoCustodiaModelo.AgrupadoPor;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosFlujo;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.DatosProcesados;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.MatrizUso;
import cl.bcs.custodia.flujo.FlujoCustodiaModelo.Totalizado;
import cl.bcs.custodia.flujo.kit.DiaSiguiente.FechaSalida;
import cl.bcs.custodia.flujo.kit.FechaLegacy;

/**
 * The grouping of the processed rows (CUS-FLU-29..32): each base row of the matrix is joined, per requested date, with the rows of that date / use /
 * origin / item that match its non-null dimensions; the counters of what was found decide which lines count (CUS-FLU-30); the lines are then
 * totalised by the requested grouping (CUS-FLU-31). Sequential (D-CUS-06: the legacy ran it in a parallel stream with unsynchronised counters, so
 * its counts could vary between runs; the sequential result is the one every non-racing legacy run gives). No I/O.
 */
final class AgrupacionFlujo {

    /** Lines of the join, the counters the totalising reads, and a row per instrument kept for the valuation. */
    record Union(List<DatosProcesados> lineas, Map<String, Integer> cantidadItem, Map<String, Integer> sItemCalculo, Map<String, Integer> sItemOrigen,
            Map<String, Integer> sReg, Map<Long, DatosFlujo> instrumentos) {
    }

    private AgrupacionFlujo() {
    }

    /** CUS-FLU-29: the join of the base table with the rows, per requested date. */
    static Union unir(List<MatrizUso> tablaBase, List<DatosFlujo> lista, List<String> fechas) {
        java.sql.Date fechaOperacionMinima = FechaLegacy.aFechaSql("0001-01-01");
        List<DatosProcesados> procesados = new ArrayList<>();
        Map<String, Integer> cantidadItem = new HashMap<>();
        Map<String, Integer> sItemCalculo = new HashMap<>();
        Map<String, Integer> sItemOrigen = new HashMap<>();
        Map<String, Integer> sReg = new HashMap<>();
        Map<Long, DatosFlujo> instrumentos = new HashMap<>();

        List<DatosFlujo> utiles = lista.stream().filter(dfc -> !"M".equals(dfc.TipoRegistro) && !"D".equals(dfc.TipoRegistro)).collect(Collectors.toList());
        Map<String, List<DatosFlujo>> porClave = utiles.stream().collect(Collectors.groupingBy(
                dfc -> FechaLegacy.texto(dfc.FechaLiquidacionProceso) + "#" + dfc.IdUsoFlujoCustodia + "#" + dfc.ItemOrigen + "#" + dfc.ItemCalculo));

        for (MatrizUso tb : tablaBase) {
            for (String fecha : fechas) {
                boolean unido = false;

                String hashSItem = fecha + "#" + tb.IdCliente + "#" + tb.IdPortafolio + "#" + tb.UsoTipoOperacion + "#" + tb.IdClasificacionTipoInstrumento
                        + "#" + tb.IdTipoInstrumento + "#" + tb.IdInstrumento + "#" + tb.IdEmisor + "#" + tb.FechaVencimiento + "#" + tb.IdTipoReajuste + "#"
                        + tb.EsFungible + "#" + tb.IdBoveda + "#" + tb.IdUsoFlujoCustodia + "#" + tb.IdRegimenFondoMutuo;
                String hashSReg = fecha + "#" + tb.IdCliente + "#" + tb.IdPortafolio + "#" + tb.UsoTipoOperacion + "#" + tb.BusquedaEspecifica + "#"
                        + tb.IdInstrumento;
                String hashFiltro = fecha + "#" + tb.IdUsoFlujoCustodia + "#" + tb.ItemOrigen + "#" + tb.ItemCalculo;

                List<DatosFlujo> filas = porClave.get(hashFiltro);
                if (filas != null) {
                    for (DatosFlujo dfc : filas) {
                        if (!((tb.IdCliente == null || dfc.IdCliente.equals(tb.IdCliente))
                                && (tb.IdPortafolio == null || dfc.IdPortafolio.equals(tb.IdPortafolio))
                                && (tb.IdClasificacionTipoInstrumento == null || dfc.IdClasificacionTipoInstrumento.equals(tb.IdClasificacionTipoInstrumento))
                                && (tb.IdTipoInstrumento == null || dfc.IdTipoInstrumento.equals(tb.IdTipoInstrumento))
                                && (tb.IdInstrumento == null || dfc.IdInstrumento.equals(tb.IdInstrumento))
                                && (tb.IdRegimenFondoMutuo == null || dfc.IdRegimenFondoMutuo.equals(tb.IdRegimenFondoMutuo)))) {
                            continue;
                        }
                        if (dfc.IdInstrumento != null) {
                            instrumentos.put(dfc.IdInstrumento, dfc);
                        }
                        unido = true;

                        DatosProcesados item = new DatosProcesados();
                        item.FechaSaldo = dfc.FechaLiquidacionProceso == null ? FechaLegacy.aFechaSql(fecha) : dfc.FechaLiquidacionProceso;
                        item.IdCliente = tb.IdCliente == null ? (dfc.IdCliente == null ? 0L : dfc.IdCliente) : tb.IdCliente;
                        item.IdPortafolio = tb.IdPortafolio == null ? (dfc.IdPortafolio == null ? 0L : dfc.IdPortafolio) : tb.IdPortafolio;
                        item.FolioRegistro = dfc.FolioRegistro == null ? 0L : dfc.FolioRegistro;
                        item.IdTipoOperacion = dfc.IdTipoOperacion == null ? "" : dfc.IdTipoOperacion;
                        item.Accion = dfc.Accion == null ? "" : dfc.Accion;
                        item.IdMonedaInstrumento = dfc.IdMonedaInstrumento == null ? "" : dfc.IdMonedaInstrumento;
                        item.IdMonedaEmision = dfc.IdMonedaEmision == null ? "" : dfc.IdMonedaEmision;
                        item.IdClasificacionTipoInstrumento = dfc.IdClasificacionTipoInstrumento == null ? tb.IdClasificacionTipoInstrumento
                                : dfc.IdClasificacionTipoInstrumento;
                        item.IdTipoInstrumento = dfc.IdTipoInstrumento == null ? tb.IdTipoInstrumento : dfc.IdTipoInstrumento;
                        item.IdInstrumento = dfc.IdInstrumento == null ? tb.IdInstrumento : dfc.IdInstrumento;
                        item.Nemotecnico = dfc.Nemotecnico == null ? "" : dfc.Nemotecnico;
                        item.NemoSVS = dfc.NemoSVS == null ? "" : dfc.NemoSVS;
                        item.IdEmisor = dfc.IdEmisor == null ? tb.IdEmisor : dfc.IdEmisor;
                        item.FechaEmisionInstrumento = dfc.FechaEmisionInstrumento == null ? fechaOperacionMinima : dfc.FechaEmisionInstrumento;
                        item.FechaVencimientoInstrumento = dfc.FechaVencimientoInstrumento == null ? tb.FechaVencimiento : dfc.FechaVencimientoInstrumento;
                        item.FechaLiquidacionInstrumento = dfc.FechaLiquidacion == null ? fechaOperacionMinima : dfc.FechaLiquidacion;
                        item.IdTipoReajuste = dfc.IdTipoReajuste == null ? tb.IdTipoReajuste : dfc.IdTipoReajuste;
                        item.EsFungible = dfc.EsFungible == null ? tb.EsFungible : dfc.EsFungible;
                        item.IdBoveda = dfc.IdBoveda == null ? tb.IdBoveda : dfc.IdBoveda;
                        item.IdBolsa = dfc.IdBolsa == null ? "" : dfc.IdBolsa;
                        item.CodigoIFRS = dfc.CodigoIFRS == null ? "" : dfc.CodigoIFRS;
                        item.IdMesa = dfc.IdMesa == null ? 0L : dfc.IdMesa;
                        item.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
                        item.UsoTipoOperacion = tb.UsoTipoOperacion;
                        item.BusquedaEspecifica = tb.BusquedaEspecifica;
                        item.ItemOrigen = tb.ItemOrigen;
                        item.ItemCalculo = tb.ItemCalculo;
                        if (dfc.ItemCalculo.equals(tb.ItemCalculo)) {
                            item.CantidadProceso = cero(dfc.CantidadProceso);
                            item.ValorRescateProceso = cero(dfc.ValorRescateProceso);
                            item.PrecioCompraProceso = cero(dfc.PrecioCompraProceso);
                            item.TasaCompraProceso = cero(dfc.TasaCompraProceso);
                            item.MontoCompraProcesoMonedaSistema = cero(dfc.MontoCompraProcesoMonedaSistema);
                            item.MontoCompraProcesoMonedaInstrumento = cero(dfc.MontoCompraProcesoMonedaInstrumento);
                            item.PrecioMercadoProceso = cero(dfc.PrecioMercadoProceso);
                            item.TasaMercadoProceso = cero(dfc.TasaMercadoProceso);
                            item.MontoMercadoProcesoMonedaSistema = cero(dfc.MontoMercadoProcesoMonedaSistema);
                            item.MontoMercadoProcesoMonedaInstrumento = cero(dfc.MontoMercadoProcesoMonedaInstrumento);
                            item.MontoCompraMonedaLocalProceso = cero(dfc.MontoCompraMonedaLocalProceso);
                        }

                        item.Fecha = fecha;
                        item.CorteMinimo = dfc.CorteMinimo;

                        int cuenta = dfc.ItemCalculo == null ? 0 : 1;
                        item.hashSItemCalculo = hashSItem + tb.ItemCalculo;
                        sumar(sItemCalculo, item.hashSItemCalculo, cuenta);
                        item.hashSItemOrigen = hashSItem + tb.ItemOrigen;
                        sumar(sItemOrigen, item.hashSItemOrigen, cuenta);
                        item.hashSREG = hashSReg;
                        sumar(sReg, hashSReg, cuenta);

                        item.IdRegimenFondoMutuo = dfc.IdRegimenFondoMutuo;
                        item.GrupoRegimen = dfc.GrupoRegimen;
                        procesados.add(item);
                    }
                }

                if (!unido) {
                    // a base row with no match yields an empty line (its counters exist, at 0)
                    DatosProcesados item = new DatosProcesados();
                    item.FechaSaldo = FechaLegacy.aFechaSql(fecha);
                    item.FechaEmisionInstrumento = fechaOperacionMinima;
                    item.FechaLiquidacionInstrumento = fechaOperacionMinima;

                    item.IdCliente = tb.IdCliente == null ? 0 : tb.IdCliente;
                    item.IdPortafolio = tb.IdPortafolio == null ? 0 : tb.IdPortafolio;

                    item.IdClasificacionTipoInstrumento = tb.IdClasificacionTipoInstrumento;
                    item.IdTipoInstrumento = tb.IdTipoInstrumento;
                    item.IdInstrumento = tb.IdInstrumento;
                    item.IdEmisor = tb.IdEmisor;
                    item.FechaVencimientoInstrumento = tb.FechaVencimiento;
                    item.IdTipoReajuste = tb.IdTipoReajuste;
                    item.EsFungible = tb.EsFungible;
                    item.IdBoveda = tb.IdBoveda;

                    item.IdUsoFlujoCustodia = tb.IdUsoFlujoCustodia;
                    item.UsoTipoOperacion = tb.UsoTipoOperacion;
                    item.BusquedaEspecifica = tb.BusquedaEspecifica;
                    item.ItemOrigen = tb.ItemOrigen;
                    item.ItemCalculo = tb.ItemCalculo;

                    item.Fecha = fecha;

                    item.hashSItemCalculo = hashSItem + tb.ItemCalculo;
                    sumar(sItemCalculo, item.hashSItemCalculo, 0);
                    item.hashSItemOrigen = hashSItem + tb.ItemOrigen;
                    sumar(sItemOrigen, item.hashSItemOrigen, 0);
                    item.hashSREG = hashSReg;
                    sumar(sReg, hashSReg, 0);

                    item.IdRegimenFondoMutuo = tb.IdRegimenFondoMutuo;
                    item.GrupoRegimen = "";
                    procesados.add(item);
                }

                sumar(cantidadItem, fecha + "#" + tb.ItemOrigen, 1);
            }
        }
        return new Union(procesados, cantidadItem, sItemCalculo, sItemOrigen, sReg, instrumentos);
    }

    /**
     * CUS-FLU-30/31/32: the counters are attached to each line, the lines are grouped by every requested grouping (a dimension is kept only when its
     * flag is "S"), only the lines counted are kept, and each group is summed. With no grouping the result is empty (CUS-FLU-32).
     * The output order is that of a {@link HashMap} of the 31-field key (D-CUS-05); a line is one instance reused by every grouping, as the legacy did.
     */
    static List<Totalizado> totalizar(Union union, List<AgrupadoPor> agrupaciones, Map<String, FechaSalida> fechasPorCalculo) {
        java.sql.Date fechaOperacionMinima = FechaLegacy.aFechaSql("0001-01-01");
        HashMap<String, List<DatosProcesados>> mapa = new HashMap<>();
        for (DatosProcesados dfc : union.lineas()) {
            dfc.CantidadItem = union.cantidadItem().get(dfc.Fecha + "#" + dfc.ItemOrigen) != null ? (long) union.cantidadItem().get(dfc.Fecha + "#" + dfc.ItemOrigen) : 0L;
            dfc.SItemCalculo = union.sItemCalculo().get(dfc.hashSItemCalculo) != null ? (long) union.sItemCalculo().get(dfc.hashSItemCalculo) : 0L;
            dfc.SItemOrigen = union.sItemOrigen().get(dfc.hashSItemOrigen) != null ? (long) union.sItemOrigen().get(dfc.hashSItemOrigen) : 0L;
            dfc.SREG = union.sReg().get(dfc.hashSREG) != null ? (long) union.sReg().get(dfc.hashSREG) : 0L;
            DatosProcesados item = new DatosProcesados();

            for (AgrupadoPor eap : agrupaciones) {
                item.FechaSaldo = dfc.FechaSaldo;
                item.IdCliente = dfc.IdCliente;
                item.IdPortafolio = dfc.IdPortafolio;
                item.IdMonedaInstrumento = dfc.IdMonedaInstrumento;
                item.IdMonedaEmision = dfc.IdMonedaEmision;
                item.IdClasificacionTipoInstrumento = activo(eap.ClasificacionTipoInstrumento) ? dfc.IdClasificacionTipoInstrumento : "";
                item.IdTipoInstrumento = activo(eap.TipoInstrumento) ? dfc.IdTipoInstrumento : "";
                item.IdInstrumento = activo(eap.Instrumento) ? dfc.IdInstrumento : Long.valueOf(0);
                item.IdEmisor = activo(eap.Emisor) ? dfc.IdEmisor : "";
                item.Nemotecnico = activo(eap.Nemotecnico) ? dfc.Nemotecnico : "";
                item.NemoSVS = activo(eap.NemoSVS) ? dfc.NemoSVS : "";
                item.FechaEmisionInstrumento = activo(eap.FechaEmisionInstrumento) ? dfc.FechaEmisionInstrumento : fechaOperacionMinima;
                item.FechaVencimientoInstrumento = activo(eap.FechaVencimientoInstrumento) ? dfc.FechaVencimientoInstrumento : fechaOperacionMinima;
                item.FechaLiquidacionInstrumento = activo(eap.FechaLiquidacionInstrumento) ? dfc.FechaLiquidacionInstrumento : fechaOperacionMinima;
                item.IdTipoReajuste = activo(eap.TipoReajuste) ? dfc.IdTipoReajuste : "";
                item.EsFungible = activo(eap.EsFungible) ? dfc.EsFungible : "";
                item.CodigoIFRS = activo(eap.CodigoIFRS) ? dfc.CodigoIFRS : "";
                item.IdBoveda = activo(eap.Boveda) ? dfc.IdBoveda : "";
                item.IdBolsa = activo(eap.Bolsa) ? dfc.IdBolsa : "";
                item.IdTipoOperacion = activo(eap.TipoOperacion) ? dfc.IdTipoOperacion : "";
                item.Accion = activo(eap.TipoOperacion) ? dfc.Accion : "";
                item.IdMesa = activo(eap.Mesa) ? dfc.IdMesa : Long.valueOf(0);
                item.FolioRegistro = activo(eap.FolioRegistro) ? dfc.FolioRegistro : Long.valueOf(0);
                item.IdRegimenFondoMutuo = activo(eap.IdRegimenFondoMutuo) ? dfc.IdRegimenFondoMutuo : "";
                item.GrupoRegimen = activo(eap.GrupoRegimen) ? dfc.GrupoRegimen : "";
                String itemOrigen = eap.ItemOrigen == null ? "N" : eap.ItemOrigen;
                item.ItemOrigen = itemOrigen.equals("S") ? dfc.ItemOrigen : "";
                item.ConsiderarRegistro = FlujoCustodiaReglas.considerarRegistro(eap.ItemOrigen, dfc.CantidadItem, dfc.SItemOrigen, dfc.SItemCalculo, dfc.SREG);
                item.CantidadProceso = dfc.CantidadProceso;
                item.ValorRescateProceso = dfc.ValorRescateProceso;
                item.UsoTipoOperacion = dfc.UsoTipoOperacion;
                item.BusquedaEspecifica = dfc.BusquedaEspecifica;
                item.CorteMinimo = dfc.CorteMinimo;

                String clave = item.UsoTipoOperacion + "#" + item.BusquedaEspecifica + "#" + item.FechaSaldo + "#" + item.IdCliente + "#" + item.IdPortafolio
                        + "#" + item.IdClasificacionTipoInstrumento + "#" + item.IdTipoInstrumento + "#" + item.Accion + "#" + item.IdInstrumento + "#"
                        + item.IdEmisor + "#" + item.Nemotecnico + "#" + item.NemoSVS + "#"
                        // minimum cut: null or zero counts as 1 (the portfolio of a variable-income instrument has minimum cut 1)
                        + (item.CorteMinimo != null && item.CorteMinimo.compareTo(BigDecimal.ZERO) != 0 ? item.CorteMinimo.doubleValue()
                                : new BigDecimal(1L).doubleValue())
                        + "#" + item.IdMonedaInstrumento + "#" + item.IdTipoReajuste + "#" + item.FechaEmisionInstrumento + "#"
                        + item.FechaVencimientoInstrumento + "#" + item.FechaSaldo + "#" + item.CodigoIFRS + "#" + item.EsFungible + "#" + item.IdBoveda + "#"
                        + item.IdBolsa + "#" + item.IdMesa + "#" + item.FolioRegistro + "#" + item.ItemOrigen + "#" + item.IdTipoOperacion + "#"
                        + item.PrecioMercadoProceso + "#" + item.TasaMercadoProceso + "#" + item.IdRegimenFondoMutuo + "#";

                // put, not computeIfAbsent: HashMap.computeIfAbsent links a new node at the head of its bucket, put at the tail, and the order of the
                // totalised lines (D-CUS-05) is the iteration order of this map
                List<DatosProcesados> grupo = mapa.get(clave);
                if (grupo == null) {
                    grupo = new ArrayList<>();
                    mapa.put(clave, grupo);
                }
                grupo.add(item);
            }
        }

        List<Totalizado> salida = new ArrayList<>();
        long i = 1L;
        for (Map.Entry<String, List<DatosProcesados>> entrada : mapa.entrySet()) {
            List<DatosProcesados> lista = entrada.getValue();
            DatosProcesados primero = lista.get(0);
            if (primero.ConsiderarRegistro.equals("S")) {
                FechaSalida fecha = fechasPorCalculo.get(primero.FechaSaldo.toString());
                Totalizado fc = new Totalizado();
                fc.IdProcesamientoInterno = i;
                fc.UsoTipoOperacion = primero.UsoTipoOperacion;
                fc.BusquedaEspecifica = primero.BusquedaEspecifica;
                fc.FechaSaldo = primero.FechaSaldo;
                fc.Dias = fecha != null ? fecha.numeroDiasInicial : fc.Dias;
                fc.IdCliente = primero.IdCliente;
                fc.IdPortafolio = primero.IdPortafolio;
                fc.IdClasificacionTipoInstrumento = primero.IdClasificacionTipoInstrumento;
                fc.IdTipoInstrumento = primero.IdTipoInstrumento;
                fc.IdInstrumento = primero.IdInstrumento;
                fc.IdEmisor = primero.IdEmisor;
                fc.Nemotecnico = primero.Nemotecnico;
                fc.NemoSVS = primero.NemoSVS;
                fc.CorteMinimo = primero.CorteMinimo;
                fc.IdMonedaInstrumento = primero.IdMonedaInstrumento;
                fc.IdMonedaEmisionInstrumento = primero.IdMonedaEmision;
                fc.IdTipoReajuste = primero.IdTipoReajuste;
                fc.FechaEmisionInstrumento = primero.FechaEmisionInstrumento;
                fc.FechaVencimientoInstrumento = primero.FechaVencimientoInstrumento;
                fc.FechaLiquidacionInstrumento = primero.FechaLiquidacionInstrumento;
                fc.CodigoIFRS = primero.CodigoIFRS;
                fc.EsFungible = primero.EsFungible;
                fc.IdBoveda = primero.IdBoveda;
                fc.IdBolsa = primero.IdBolsa;
                fc.IdTipoOperacion = primero.IdTipoOperacion;
                fc.Accion = primero.Accion;
                fc.IdMesa = primero.IdMesa;
                fc.FolioRegistro = primero.FolioRegistro;
                fc.ItemOrigen = primero.ItemOrigen;
                fc.IdRegimenFondoMutuo = primero.IdRegimenFondoMutuo;
                fc.GrupoRegimen = primero.GrupoRegimen;

                fc.PrecioMercado = primero.PrecioMercadoProceso;
                fc.TasaMercado = primero.TasaMercadoProceso;
                fc.Cantidad = new BigDecimal(0);
                fc.ValorRescate = new BigDecimal(0);
                fc.MontoAMercadoMonedaSistema = new BigDecimal(0);
                fc.MontoAMercadoMonedaInstrumento = new BigDecimal(0);

                for (DatosProcesados item : lista) {
                    fc.Cantidad = fc.Cantidad.add(item.CantidadProceso);
                    fc.ValorRescate = fc.ValorRescate.add(item.ValorRescateProceso);
                    fc.MontoAMercadoMonedaSistema = fc.MontoAMercadoMonedaSistema.add(item.MontoMercadoProcesoMonedaSistema);
                    fc.MontoAMercadoMonedaInstrumento = fc.MontoAMercadoMonedaInstrumento.add(item.MontoMercadoProcesoMonedaInstrumento);
                }
                salida.add(fc);
                i++;
            }
        }
        return salida;
    }

    private static boolean activo(String flag) {
        return (flag == null ? "N" : flag).equals("S");
    }

    private static BigDecimal cero(BigDecimal v) {
        return v == null ? new BigDecimal(0) : v;
    }

    private static void sumar(Map<String, Integer> contador, String clave, int cuanto) {
        contador.merge(clave, cuanto, Integer::sum);
    }
}
