package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.math.MathContext;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/** The two requests to the requirements check of one pair and the issuer-equity candidate (FM-REI-20..22, RR:843-1053), in the legacy order. */
final class RreRequisitos {

    private static final String SI = RreConstantes.SI;
    private static final String NO = RreConstantes.NO;

    private RreRequisitos() {
    }

    /** FM-REI-20 (RR:843-917): the rescue side. */
    static void rescate(RreContexto c, InSolicitudesRescate solicitud, InSolicitudesAporte apor, SolicitudAporteRescate sol, SerieFondoSolicitudes serie,
            SerieFondoSolicitudes serieDestino, ClientePortafolioSolicitudes cliente, RreDatos.RegimenSerieDeFondo rRescate,
            RreDatos.ConfiguracionRegimenFondoMutuo confRegimen, TipoMovimientoFondoMutuo tipoMovimientoRescate, InSolicitudes solVal, InPortafolio portVal) {
        boolean comercial = RreConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        solVal.IdInterno = sol.IdInterno;
        solVal.IdCliente = sol.IdCliente;
        solVal.IdPortafolio = sol.IdPortafolio;
        solVal.IdSerieFondo = sol.IdSerieFondo;
        solVal.IdTipoMovimientoFondoMutuo = tipoMovimientoRescate != null ? tipoMovimientoRescate.IdTipoMovimientoFondoMutuo : "";
        solVal.Monto = sol.Monto;
        solVal.NumeroCuotas = sol.NumeroCuotas;
        solVal.IdEstadoSerie = "";
        solVal.EsFechaValidaAporte = SI;
        solVal.EsFechaValidaRescate = SI;
        solVal.EsDiferido = SI;
        solVal.EsMontoMaximo = SI;
        solVal.EsMontoMinimo = SI;
        solVal.ValorCuota = sol.ValorCuota;
        solVal.Overnight = NO;
        solVal.EstadoLiquidez = "";
        solVal.EsMoneyMarket = SI;
        solVal.EsFamiliaEquivalente = SI;
        solVal.EsPortafolioRegimenValido = SI;
        solVal.EsRegimenValido = SI;
        solVal.PermiteRescateMandato = SI;
        solVal.PermiteCambioGrupo = SI;
        solVal.EsPortafolioValido = SI;
        solVal.EsCuentaInversionValido = SI;
        solVal.PermiteAporte = SI;
        solVal.EsRegimenDestinoValido = SI;
        if (serie != null) {
            solVal.IdEstadoSerie = serie.IdEstado;
            solVal.RestringeAporte = serie.RestringeAporte;
            solVal.RestringeRescate = serie.RestringeRescate;
            solVal.EstadoFondo = comercial ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
            solVal.EstadoBloqueo = serie.BloqueoRescate;
            solVal.EstadoLiquidez = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
            solVal.EsFondoValido = serie.IdSerieFondo.equalsIgnoreCase(serieDestino.IdSerieFondo) ? NO : SI;
            solVal.PermiteCambioFondo = serie.CambioFondo;
            if (!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && NO.equalsIgnoreCase(serie.Diferido) && NO.equals(c.rescateGeneraVentaSpotDolar)) {
                solVal.EsDiferido = NO;
            }
        }
        if (rRescate == null) {
            solVal.EsRegimenValido = NO;
        }
        if (solicitud.IdRegimenTributario != null) {
            if (!solicitud.IdRegimenTributario.equalsIgnoreCase(apor.IdRegimenTributario) && confRegimen == null) {
                solVal.EsRegimenDestinoValido = NO;
            }
        }
        if (SI.equalsIgnoreCase(apor.IndicadorMLT) && "N".equalsIgnoreCase(rRescate.PermiteMLT)) {
            solVal.PermiteRescateMandato = NO;
        }
        if (cliente != null) {
            portafolio(portVal, sol.IdInterno, sol, cliente);
        }
        solVal.IdTipoOperacion = RreConstantes.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
        solVal.IdTipoProducto = RreConstantes.TIPO_OPERACION;
    }

    private static void portafolio(InPortafolio p, Long idInterno, SolicitudAporteRescate sol, ClientePortafolioSolicitudes cliente) {
        p.IdInterno = idInterno;
        p.IdPortafolio = sol.IdPortafolio;
        p.IdPortafolioReal = cliente.IdPortafolio;
        p.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
        p.IdCliente = sol.IdCliente;
        p.IdClienteReal = cliente.IdCliente;
        p.IdEstadoCliente = cliente.IdEstadoCliente;
        p.DocumentoIdentidad = cliente.DocumentoIdentidad;
        p.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
        p.IdClienteDestino = sol.IdClienteDestino;
        p.IdClienteRealDestino = 0L;
        p.IdPortafolioDestino = sol.IdPortafolio;
        p.IdPortafolioRealDestino = 0L;
        p.IdEstadoClienteDestino = "";
        p.IdEstadoPortafolioDestino = "";
    }

    /** FM-REI-21/22 (RR:921-1053): the contribution side (IdInterno negative); it also rewrites three flags of the rescue side and feeds the equity check. */
    static void aporte(RreContexto c, InSolicitudesRescate solicitud, InSolicitudesAporte apor, SolicitudAporteRescate sol, SerieFondoSolicitudes serie,
            SerieFondoSolicitudes serieDestino, ClientePortafolioSolicitudes cliente, RreDatos.RegimenSerieDeFondo rRescate, RreDatos.RegimenSerieDeFondo rAporte,
            RreDatos.RegimenMandato rMandato, RreDatos.ConfiguracionPropositoPortafolio confProposito, Resultado vcApor, TipoMovimientoFondoMutuo tipoMovimientoAporte,
            InSolicitudes solVal, InSolicitudes solValApor, InPortafolio portValApor) {
        boolean comercial = RreConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);
        solValApor.IdInterno = sol.IdInterno * -1;
        solValApor.IdCliente = sol.IdCliente;
        solValApor.IdPortafolio = sol.IdPortafolio;
        solValApor.IdSerieFondo = serieDestino != null ? serieDestino.IdSerieFondo : "";
        solValApor.IdTipoMovimientoFondoMutuo = tipoMovimientoAporte != null ? tipoMovimientoAporte.IdTipoMovimientoFondoMutuo : "";
        solValApor.Monto = BigDecimal.ZERO;
        solValApor.NumeroCuotas = BigDecimal.ZERO;
        solValApor.ValorCuota = BigDecimal.ZERO;
        solValApor.IdEstadoSerie = "";
        solValApor.EsFechaValidaAporte = SI;
        solValApor.EsFechaValidaRescate = SI;
        solValApor.EsDiferido = SI;
        solValApor.Overnight = NO;
        solValApor.EstadoLiquidez = "";
        solValApor.EsMoneyMarket = SI;
        solValApor.EsFamiliaEquivalente = SI;
        solValApor.EsPortafolioRegimenValido = SI;
        solValApor.EsRegimenValido = SI;
        solValApor.PermiteRescateMandato = SI;
        solValApor.PermiteCambioGrupo = SI;
        solValApor.EsPortafolioValido = SI;
        solValApor.EsCuentaInversionValido = SI;
        solValApor.IdTipoOperacion = RreConstantes.APORTE_FONDO_MUTUO;   // overwritten below with APORFM, as the legacy (:944 / :1050)
        solValApor.IdTipoProducto = RreConstantes.TIPO_OPERACION;
        solValApor.PermiteAporte = SI;
        solValApor.EsRegimenDestinoValido = SI;
        solValApor.EsMontoMinimo = SI;
        if (vcApor != null) {
            solValApor.Monto = vcApor.Monto;
            solValApor.NumeroCuotas = vcApor.NumeroCuotas;
            solValApor.ValorCuota = vcApor.ValorCuota;
        }
        if (serieDestino != null) {
            destino(c, solicitud, apor, sol, serie, serieDestino, rAporte, vcApor, comercial, solVal, solValApor);
        }
        if (rAporte == null) {
            solValApor.EsRegimenValido = NO;
        } else {
            solValApor.PermiteAporte = rAporte.PermiteAporte;
        }
        if (confProposito == null) solValApor.EsPortafolioRegimenValido = NO;
        if (SI.equalsIgnoreCase(apor.IndicadorMLT) && ((rAporte == null || rRescate == null) || (!rAporte.GrupoRegimen.equalsIgnoreCase(rRescate.GrupoRegimen)
                || rMandato == null))) {
            solVal.PermiteRescateMandato = NO;   // D-F2-14: rMandato is never null
        }
        if (SI.equalsIgnoreCase(apor.IndicadorMLT) && "N".equalsIgnoreCase(rAporte.PermiteMLT)) {
            solValApor.PermiteRescateMandato = NO;
        }
        if (cliente != null) {
            portafolio(portValApor, sol.IdInterno * -1, sol, cliente);
        }
        solValApor.IdTipoOperacion = RreConstantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
        solValApor.IdTipoProducto = RreConstantes.TIPO_OPERACION;
    }

    /** RR:954-1015: the destination series exists. */
    private static void destino(RreContexto c, InSolicitudesRescate solicitud, InSolicitudesAporte apor, SolicitudAporteRescate sol, SerieFondoSolicitudes serie,
            SerieFondoSolicitudes serieDestino, RreDatos.RegimenSerieDeFondo rAporte, Resultado vcApor, boolean comercial, InSolicitudes solVal,
            InSolicitudes solValApor) {
        solValApor.PermiteCambioFondo = serieDestino.CambioFondo;
        solValApor.IdEstadoSerie = serieDestino.IdEstado;
        solValApor.RestringeAporte = serieDestino.RestringeAporte;
        solValApor.RestringeRescate = serieDestino.RestringeRescate;
        solValApor.EstadoFondo = comercial ? serieDestino.EstadoAporteComercial : serieDestino.EstadoAporteBackoffice;
        solValApor.EstadoBloqueo = serieDestino.BloqueoAporte;
        solValApor.EsFondoValido = SI;
        RreReglas.KeyAporteMonto key = new RreReglas.KeyAporteMonto();   // FM-REI-21, D-F2-15: read with the NEW IdAgrupador (written with the input one)
        key.IdSerieFondo = serieDestino.IdSerieFondo;
        key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : sol.IdAgrupador;
        BigDecimal montoTotalAporte = c.aportesAgrupadosPorSerieOperacion.get(key);
        BigDecimal monto = montoTotalAporte == null ? (vcApor == null ? null : vcApor.Monto) : montoTotalAporte;
        if (montoTotalAporte == null) {
            solValApor.EsMontoMaximo = vcApor == null || (serieDestino.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && vcApor.Monto.compareTo(serieDestino.MontoMaximo) == 1) ? NO : SI;
        } else {
            solValApor.EsMontoMaximo = vcApor == null || (serieDestino.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && montoTotalAporte.compareTo(serieDestino.MontoMaximo) == 1) ? NO : SI;
        }
        if (rAporte != null && SI.equals(rAporte.ValidaMontoMinimoInstrumento)) {
            solValApor.EsMontoMinimo = vcApor == null || (serieDestino.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && monto.compareTo(serieDestino.MontoMinimo) == -1) ? NO : SI;
        }
        solVal.EstadoLiquidez = comercial ? serieDestino.EstadoLiquidezComercial : serieDestino.EstadoLiquidezBackoffice;
        solVal.EsFondoValido = serie == null || serie.IdSerieFondo.equalsIgnoreCase(serieDestino.IdSerieFondo) ? NO : SI;
        if (!apor.IdMoneda.equalsIgnoreCase(serieDestino.IdMoneda) && NO.equalsIgnoreCase(serieDestino.Diferido) && NO.equals(c.rescateGeneraVentaSpotDolar)) {
            solVal.EsDiferido = NO;
        }
        // PATRIMONIOS ALTOS (FM-REI-22): the accumulated amount is never read afterwards; its arithmetic still runs (and may throw) as in the legacy
        BigDecimal montoPortafolioPorEmisor = c.hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serieDestino.IdEmisor);
        if (montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
        if (serie.IdMoneda != null && serieDestino.IdMoneda != null && "CLP".equalsIgnoreCase(serieDestino.IdMoneda)) {
            montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema.multiply(apor.Porcentaje.divide(new BigDecimal("100"), MathContext.DECIMAL128),
                    MathContext.DECIMAL128), MathContext.DECIMAL128);
        } else if (serie.IdMoneda != null && serieDestino.IdMoneda != null && !"CLP".equalsIgnoreCase(serieDestino.IdMoneda)) {
            montoPortafolioPorEmisor = solValApor.Monto;
        }
        c.hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serieDestino.IdEmisor, montoPortafolioPorEmisor);
        if (SI.equals(serieDestino.PatrimonioAltos)) {
            ValidaPatrimonioEmisorService.Solicitud valPatrimonio = new ValidaPatrimonioEmisorService.Solicitud();
            valPatrimonio.IdInterno = sol.IdInterno;
            valPatrimonio.IdCliente = sol.IdCliente;
            valPatrimonio.IdSerieFondo = serieDestino.IdSerieFondo;
            valPatrimonio.IdEmisor = serieDestino.IdEmisor;
            valPatrimonio.MontoPatrimonio = serie.IdEmisor.equals(serieDestino.IdEmisor) ? BigDecimal.ZERO : solicitud.MontoPatrimonio.add(solValApor.Monto);
            valPatrimonio.ValorCuota = sol.ValorCuota;
            c.entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
        }
    }
}
