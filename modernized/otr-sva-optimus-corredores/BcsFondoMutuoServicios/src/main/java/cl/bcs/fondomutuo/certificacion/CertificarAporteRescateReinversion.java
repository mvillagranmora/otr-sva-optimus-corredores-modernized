package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.CANAL_AGENTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.DOLAR;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SWITCH;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.AporteReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.AporteReinversionEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.AporteReinversionSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.ResultadoAporte;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.InSolicitudes;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.OrigenReinversion;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.ReinversionDestino;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.FormatoMoneda;
import cl.bcs.fondomutuo.cierre.ListaHash;

/**
 * Front F-I (1: creation of the aportes of reinversion) of legacy {@code CertificarSolicitudAporteRescate.logica} C:762-787 and {@code crearAporteReinversion} C:2343-2557, bug for bug
 * (FM-CER-52..58): the aportes of every rescate {@code RESREINV} not rejected (one per retired cartera when the destination has the same family and currency, otherwise one per destination),
 * ONE call to AR, the mapping of the correlative ids to the rescates and the aportes of switch ({@code ctx.aportesSwitch}). The rounding difference goes to the FIRST aporte of the rescate, also the
 * one of another destination (D-F3B-13, D-F3B-45, D-F3B-46); the destinations are grouped in a {@code HashMap} (D-F3B-32). Does nothing without rescates, writes nothing, works on the
 * {@link CertificarAporteRescateContexto} and lets any exception go up untouched. No state: every call builds its own {@link Armado}.
 */
public final class CertificarAporteRescateReinversion {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CrearAporteReinversionPort aporteReinversion;
    private final CertificarAporteRescateDatos datos;
    private final FormatoMoneda formato;

    public CertificarAporteRescateReinversion(CrearAporteReinversionPort aporteReinversion, CertificarAporteRescateDatos datos, FormatoMoneda formato) {
        this.aporteReinversion = aporteReinversion;
        this.datos = datos;
        this.formato = formato;
    }

    /** Legacy C:762-787. */
    public void reinvertir(CertificarAporteRescateContexto ctx) throws Exception {
        if (ctx.rescates.size() == 0) return;
        AporteReinversionSalida salida = crearAportes(ctx);
        ctx.salida.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
        CertificarAporteRescateCustodia.rechazarPorMensajes(ctx.salida, salida.P_OUT_MENSAJES, ctx.rescates);

        // FM-CER-58 (D-F3B-30): a result without Switch is a NullPointerException; an aporte rejected by AR is not looked at
        for (ResultadoAporte res : salida.P_OUT_RESULTADO) {
            if (res.Switch.equals(SWITCH)) {
                InSolicitudes aporte = new InSolicitudes();
                aporte.IdSolicitudAporteRescate = res.IdSolicitudAporteRescate;
                aporte.IdTipoCanalIngreso = res.IdTipoCanalIngreso;
                ctx.aportesSwitch.add(aporte);
            }
        }
    }

    /** FM-CER-52..57 (C:2343-2557). */
    private AporteReinversionSalida crearAportes(CertificarAporteRescateContexto ctx) throws Exception {
        List<SolicitudProceso> rescates = ctx.rescates;
        List<Long> idSolicitudes = rescates.stream().map(sol -> sol.IdSolicitudAporteRescate).distinct().collect(Collectors.toList());
        List<ReinversionDestino> destinos = datos.destinosReinversion(idSolicitudes);
        List<OrigenReinversion> series = datos.seriesOrigenReinversion(ctx.entrada.p_IdEmpresa, idSolicitudes);
        Map<Long, List<ReinversionDestino>> destinosPorRescate = destinos.stream().collect(Collectors.groupingBy(destino -> destino.IdSolicitudAporteRescate));
        Map<Long, List<MovimientoSalida>> movimientosPorRescate = ctx.carterasRetiradas.stream().collect(Collectors.groupingBy(mov -> mov.IdGenerarMovimientoCustodia));
        Armado armado = new Armado(ctx, ListaHash.aHash(rescates, sol -> sol.IdSolicitudAporteRescate), ListaHash.aHash(series, serie -> serie.IdSolicitudAporteRescate),
                movimientosPorRescate);

        for (List<ReinversionDestino> destinosDelRescate : destinosPorRescate.values()) {
            armado.repartir(destinosDelRescate);
        }

        AporteReinversionSalida salida = new AporteReinversionSalida();
        AporteReinversionEntrada entrada = armado.entrada;
        if (entrada.p_SolicitudesAporte.size() > 0) {
            entrada.p_IdEmpresa = ctx.entrada.p_IdEmpresa;
            entrada.p_IdUsuario = ctx.entrada.p_IdUsuario;
            entrada.p_IdModulo = ctx.entrada.p_IdModulo;
            entrada.p_SimularOperacion = NO;
            entrada.p_UsaReglasNegocio = ctx.entrada.p_UsaReglasNegocio;
            entrada.p_IdTipoCanalIngreso = CANAL_AGENTE;
            salida = CertificarAporteRescateColaboradores.crearAporteReinversion(aporteReinversion, entrada);
            Map<Long, Long> rescatePorCorrelativo = armado.rescatePorCorrelativo;
            salida.P_OUT_MENSAJES.forEach(msj -> {
                if (rescatePorCorrelativo.get(msj.id) != null) msj.id = rescatePorCorrelativo.get(msj.id);
            });
            salida.P_OUT_RESULTADO.forEach(res -> res.IdInterno = rescatePorCorrelativo.get(res.IdInterno));
        }
        return salida;
    }

    /** The state of ONE call: the entry to AR that is being built, the correlative id and its map to the rescates. */
    private final class Armado {
        final AporteReinversionEntrada entrada = new AporteReinversionEntrada();
        final Map<Long, Long> rescatePorCorrelativo = new HashMap<>();
        private final CertificarAporteRescateContexto ctx;
        private final Map<Long, SolicitudProceso> rescatesPorId;
        private final Map<Long, OrigenReinversion> origenPorRescate;
        private final Map<Long, List<MovimientoSalida>> movimientosPorRescate;
        private long correlativo = 1L;

        Armado(CertificarAporteRescateContexto ctx, Map<Long, SolicitudProceso> rescatesPorId, Map<Long, OrigenReinversion> origenPorRescate,
                Map<Long, List<MovimientoSalida>> movimientosPorRescate) {
            this.ctx = ctx;
            this.rescatesPorId = rescatesPorId;
            this.origenPorRescate = origenPorRescate;
            this.movimientosPorRescate = movimientosPorRescate;
        }

        /** The destinations of ONE rescate: the aportes of each and, after the last, the difference of the total (FM-CER-53..55). */
        void repartir(List<ReinversionDestino> destinosDelRescate) throws Exception {
            long tamano = destinosDelRescate.size();
            BigDecimal montoCuotasTotal = BigDecimal.ZERO;
            long indexTotal = 1L;
            for (ReinversionDestino destino : destinosDelRescate) {
                SolicitudProceso sol = rescatesPorId.get(destino.IdSolicitudAporteRescate);
                OrigenReinversion origen = origenPorRescate.get(sol.IdSolicitudAporteRescate);
                if (ESTADO_RECHAZADO.equals(sol.EstadoProceso) || !REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo)) continue;
                BigDecimal proporcion = destino.Porcentaje == null ? null : destino.Porcentaje.divide(CIEN, MathContext.DECIMAL128);

                if (!sol.IdFamiliaFondo.isEmpty() && sol.IdFamiliaFondo.equals(destino.IdFamiliaFondo) && sol.IdMoneda.equals(destino.IdMoneda)) {
                    montoCuotasTotal = unAportePorCartera(sol, destino, origen, proporcion, montoCuotasTotal);
                } else {
                    AporteReinversion aporte = nuevoAporte(sol, destino, origen);
                    aporte.CostoUF = sol.CostoUF.multiply(proporcion, MathContext.DECIMAL128);
                    aporte.FechaOrigenInversion = FechaLegacy.texto(sol.FechaCertificacion);
                    aporte.MontoOrigen = sol.MontoPagoLocal.multiply(proporcion, MathContext.DECIMAL128);
                    aporte.Monto = formato.formatear(aporte.MontoOrigen, FormatoMoneda.MONTO, sol.IdMoneda);
                    aporte.MontoOrigen = aporte.Monto;
                    montoCuotasTotal = montoCuotasTotal.add(aporte.Monto, MathContext.DECIMAL128);
                    entrada.p_SolicitudesAporte.add(aporte);
                }
                indexTotal++;

                // the rounding difference of the whole rescate, after its last destination
                if (indexTotal > tamano) {
                    BigDecimal diferencia = sol.MontoPagoLocal.subtract(montoCuotasTotal);
                    AporteReinversion primero = primerAporte(destino.IdSolicitudAporteRescate);
                    if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                        sumarDiferencia(primero, diferencia, sol.IdMoneda);
                    }
                }
            }
        }

        /** FM-CER-53: one aporte per retired cartera, in proportion to the quantity; the difference of THIS destination goes to the first aporte of the rescate. */
        private BigDecimal unAportePorCartera(SolicitudProceso sol, ReinversionDestino destino, OrigenReinversion origen, BigDecimal proporcion, BigDecimal montoCuotasTotal)
                throws Exception {
            List<MovimientoSalida> movimientos = movimientosPorRescate.get(destino.IdSolicitudAporteRescate);
            BigDecimal montoEsperado = formato.formatear(sol.MontoPagoLocal.multiply(proporcion, MathContext.DECIMAL128), FormatoMoneda.MONTO, sol.IdMoneda);
            BigDecimal montoAporte = BigDecimal.ZERO;
            for (MovimientoSalida movimiento : movimientos == null ? List.<MovimientoSalida>of() : movimientos) {
                BigDecimal porcentajeRelacionCantidad = movimiento.Cantidad.divide(sol.NumeroCuotas, MathContext.DECIMAL128);
                BigDecimal bruto = sol.MontoPagoLocal.multiply(proporcion, MathContext.DECIMAL128).multiply(porcentajeRelacionCantidad, MathContext.DECIMAL128);
                BigDecimal monto = formato.formatear(bruto, FormatoMoneda.MONTO, sol.IdMoneda);
                if (monto.compareTo(BigDecimal.ZERO) != 1) continue;   // a zero amount is skipped
                AporteReinversion aporte = nuevoAporte(sol, destino, origen);
                aporte.Monto = monto;
                aporte.MontoOrigen = monto;
                aporte.CostoUF = movimiento.CostoInversionEnFactorAjusteARetirar.multiply(proporcion, MathContext.DECIMAL128);
                aporte.FechaOrigenInversion = FechaLegacy.texto(FechaLegacy.aFechaSql(movimiento.FechaOrigenCompra));
                entrada.p_SolicitudesAporte.add(aporte);
                montoAporte = montoAporte.add(aporte.Monto, MathContext.DECIMAL128);
                montoCuotasTotal = montoCuotasTotal.add(aporte.Monto, MathContext.DECIMAL128);
            }
            // D-F3B-13 / D-F3B-45: an empty list (no movement, or every amount zero) is an IndexOutOfBoundsException
            BigDecimal diferencia = montoEsperado.subtract(montoAporte);
            AporteReinversion primero = primerAporte(destino.IdSolicitudAporteRescate);
            if (diferencia.compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal montoAnterior = primero.Monto;
                sumarDiferencia(primero, diferencia, sol.IdMoneda);
                montoCuotasTotal = montoCuotasTotal.add(primero.Monto.subtract(montoAnterior, MathContext.DECIMAL128), MathContext.DECIMAL128);
            }
            return montoCuotasTotal;
        }

        /** The fields both branches share (FM-CER-56, 57); takes the next correlative id and maps it to the rescate. */
        private AporteReinversion nuevoAporte(SolicitudProceso sol, ReinversionDestino destino, OrigenReinversion origen) {
            AporteReinversion aporte = new AporteReinversion();
            rescatePorCorrelativo.put(correlativo, sol.IdSolicitudAporteRescate);
            aporte.IdInterno = correlativo++;
            aporte.IdCliente = sol.IdCliente;
            aporte.IdPortafolio = sol.IdPortafolio;
            aporte.IdSerieFondoOrigen = sol.IdSerieFondo;
            aporte.IdEmisorOrigen = sol.IdEmisor;
            aporte.IndicadorMLT = sol.IndicadorMLT;
            aporte.IdSerieFondo = destino.IdSerieFondoDestino;
            aporte.NumeroCuotas = BigDecimal.ZERO;
            aporte.IdMoneda = sol.IdMoneda;
            aporte.IdMedioDePago = sol.IdMedioDePago;
            aporte.Visado = SI;
            aporte.SolicitudBatch = sol.SolicitudBatch;
            aporte.OrigenOperacion = sol.OrigenOperacion;
            aporte.IdRegimenTributario = destino.IdRegimenTributario;
            aporte.Glosa = sol.Glosa;
            aporte.IdAutorizadoOrigen = 0L;
            aporte.FolioContrato = sol.FolioContrato;
            if (sol.IdMoneda.equalsIgnoreCase(destino.IdMoneda)) {
                boolean internacionalDolar = SI.equals(origen.FondoOrigenInternacionalDolar) && SI.equals(origen.FondoDestinoInternacionalDolar) && NO.equals(destino.Switch);
                aporte.FechaEmision = FechaLegacy.texto(internacionalDolar ? sol.FechaCertificacion : sol.FechaLiquidacion);
            } else {
                aporte.FechaEmision = FechaLegacy.texto(sol.FechaLiquidacion);
            }
            aporte.IdSolicitudAporteRescateRelacionado = sol.IdSolicitudAporteRescate;
            aporte.IdAgrupador = sol.IdAgrupador;
            aporte.Switch = destino.Switch;
            aporte.FechaCertificacionRescateOrigen = FechaLegacy.texto(sol.FechaCertificacion);
            aporte.FechaLiquidacionRescateOrigen = FechaLegacy.texto(sol.FechaLiquidacion);
            aporte.FechaValorCuotaRescateOrigen = FechaLegacy.texto(sol.FechaValorCuota);
            aporte.Canal = sol.IdTipoCanalIngreso;
            aporte.IngresoAutomatico = sol.IngresoAutomatico;
            aporte.MontoTotalInversion = sol.MontoPagoLocal.multiply(destino.Porcentaje.divide(CIEN, MathContext.DECIMAL128));
            // FM-CER-56: the Spot dates; the second block overwrites the first when the system currency is the one of the rescate
            if (DOLAR.equalsIgnoreCase(sol.IdMoneda) && sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
                aporte.FechaCertificaSpotCompra = texto(sol.FechaCertificacion);
                aporte.FechaLiquidaSpotCompra = texto(sol.FechaLiquidacion);
            }
            if (sol.IdMonedaSistema.equals(sol.IdMoneda) && sol.FechaCertificacion != null && sol.FechaLiquidacion != null) {
                aporte.FechaCertificaSpotCompra = texto(sol.FechaAceptacion);
                aporte.FechaLiquidaSpotCompra = texto(sol.FechaAceptacion);
            }
            return aporte;
        }

        /** The "last" aporte of the legacy is the FIRST one of the rescate in the entry (D-F3B-13); none raises IndexOutOfBoundsException. */
        private AporteReinversion primerAporte(Long idRescate) {
            return entrada.p_SolicitudesAporte.stream().filter(aporte -> aporte.IdSolicitudAporteRescateRelacionado.equals(idRescate)).findFirst()
                    .orElseThrow(() -> new IndexOutOfBoundsException("Index 0 out of bounds for length 0"));
        }

        private void sumarDiferencia(AporteReinversion aporte, BigDecimal diferencia, String idMoneda) throws Exception {
            aporte.Monto = formato.formatear(aporte.Monto.add(diferencia, MathContext.DECIMAL128), FormatoMoneda.MONTO, idMoneda);
            aporte.MontoOrigen = formato.formatear(aporte.MontoOrigen.add(diferencia, MathContext.DECIMAL128), FormatoMoneda.MONTO, idMoneda);
        }
    }

    /** Legacy {@code Fecha.toStringDate(java.sql.Date)}: {@code yyyy-MM-dd}, the empty text for null. */
    private static String texto(java.sql.Date fecha) {
        return FechaLegacy.aTexto(fecha == null ? null : fecha.toLocalDate());
    }
}
