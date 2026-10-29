package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.math.MathContext;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.normal.NormalContrato.InSolicitud;
import cl.bcs.fondomutuo.normal.NormalDatos.ConfiguracionPropositoPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.IndicadoresPortafolio;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.normal.NormalDatos.SalidaTraspasoTotal;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;

/** The data one request gives to {@code ServicioCumpleRequisitos...} and to the equity check of the issuer (legacy :921-1046). */
final class NormalRequisitos {

    private NormalRequisitos() {
    }

    static void completar(NormalContexto c, InSolicitud solicitud, SolicitudAporteRescate sol, String procedenciaCanal, InSolicitudes solVal,
            InPortafolio portVal) {
        TipoMovimientoFondoMutuo tipoMovimiento = c.htipoMovimiento.get(NormalArmado.tipoMovimiento(solicitud));
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        RegimenSerieDeFondo r = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        ConfiguracionPropositoPortafolio confPropositoPortafolio = null;
        if (cliente != null) confPropositoPortafolio = c.hconfRegimenPortafolio.get(solicitud.IdRegimenTributario + "_" + cliente.Proposito);
        IndicadoresPortafolio indicadorPortafolio = c.hindicadoresPortafolio.get(solicitud.IdPortafolio);
        boolean comercial = NormalConstantes.PERFIL_COMERCIAL.equalsIgnoreCase(c.perfilFondoMutuo);

        solVal.IdInterno = sol.IdInterno;
        solVal.IdCliente = sol.IdCliente;
        solVal.IdPortafolio = sol.IdPortafolio;
        solVal.IdSerieFondo = sol.IdSerieFondo;
        solVal.IdTipoMovimientoFondoMutuo = tipoMovimiento != null ? tipoMovimiento.IdTipoMovimientoFondoMutuo : "";
        solVal.Monto = sol.Monto;
        solVal.NumeroCuotas = sol.NumeroCuotas;
        solVal.IdEstadoSerie = "";
        solVal.EsDiferido = NormalConstantes.SI;
        solVal.EsMontoMaximo = NormalConstantes.SI;
        solVal.EsMontoMinimo = NormalConstantes.SI;
        solVal.PermiteCambioGrupo = NormalConstantes.SI;
        solVal.EsPortafolioValido = NormalConstantes.SI;
        solVal.PermiteRescateMandato = NormalConstantes.SI;
        solVal.EsCuentaInversionValido = NormalConstantes.SI;
        solVal.EsFechaValidaAporte = NormalConstantes.SI;
        solVal.EsFechaValidaRescate = NormalConstantes.SI;
        solVal.ValorCuota = sol.ValorCuota;
        solVal.Overnight = NormalConstantes.NO;
        solVal.EsMoneyMarket = NormalConstantes.SI;
        solVal.EsFamiliaEquivalente = NormalConstantes.SI;
        solVal.EsPortafolioRegimenValido = NormalConstantes.SI;
        solVal.EsRegimenValido = NormalConstantes.SI;
        solVal.PermiteAporte = NormalConstantes.SI;
        solVal.EsRegimenDestinoValido = NormalConstantes.SI;
        solVal.Glosa = solicitud.Glosa;
        solVal.ProcedenciaCanal = procedenciaCanal;
        if (serie != null) {
            solVal.IdEstadoSerie = serie.IdEstado;
            solVal.RestringeAporte = serie.RestringeAporte;
            solVal.RestringeRescate = serie.RestringeRescate;
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                aporte(c, solicitud, sol, solVal, serie, r, comercial);
            } else if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(solicitud.TipoSolicitud)) {
                solVal.EstadoFondo = comercial ? serie.EstadoRescateComercial : serie.EstadoRescateBackoffice;
                solVal.EstadoBloqueo = serie.BloqueoRescate;
                solVal.EsMontoMaximo = NormalConstantes.SI;
                solVal.EsMontoMinimo = NormalConstantes.SI;
            }
            solVal.EstadoLiquidez = comercial ? serie.EstadoLiquidezComercial : serie.EstadoLiquidezBackoffice;
            solVal.EsFondoValido = NormalConstantes.SI;
            if (!sol.IdMoneda.equalsIgnoreCase(serie.IdMoneda) && NormalConstantes.NO.equalsIgnoreCase(serie.Diferido)) {
                solVal.EsDiferido = NormalConstantes.NO;
            }
        }
        regimen(solicitud, sol, solVal, r, c.hcustodiasDC.get(sol.IdPortafolio + "_" + sol.IdSerieFondo));
        if (confPropositoPortafolio == null && (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)
                || (NormalConstantes.TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) && !NormalConstantes.CALCULO_POR_TOTAL.equals(solicitud.CalculoPorMontoCuota)))) {
            solVal.EsPortafolioRegimenValido = NormalConstantes.NO;
        }
        if (NormalConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago) && NormalConstantes.NO.equalsIgnoreCase(indicadorPortafolio.ValorIndicador)) {
            solVal.EsCuentaInversionValido = NormalConstantes.NO;
        }
        if (cliente != null) {
            portafolio(sol, portVal, cliente);
        }
        solVal.IdTipoOperacion = NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? NormalConstantes.TIPO_OPERACION_APORTE_FONDO_MUTUO
                : NormalConstantes.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
        solVal.IdTipoProducto = NormalConstantes.TIPO_OPERACION;
        // portfolios of Bonificacion Fiscal and requests that ignore the minimum amount are not checked against it (legacy :1037-1043)
        boolean esPortafolioBonificacionFiscal = cliente.CodigoPortafolio.equalsIgnoreCase(NormalConstantes.BONIFICACION_FISCAL);
        boolean solicitudIgnoraMontoMinimo = NormalConstantes.NO.equals(solVal.EsMontoMinimo) && NormalConstantes.SI.equals(solicitud.IgnorarMontoMinimo);
        if (esPortafolioBonificacionFiscal || solicitudIgnoraMontoMinimo) {
            solVal.EsMontoMinimo = NormalConstantes.SI;
        }
    }

    private static void aporte(NormalContexto c, InSolicitud solicitud, SolicitudAporteRescate sol, InSolicitudes solVal, SerieFondoSolicitudes serie,
            RegimenSerieDeFondo r, boolean comercial) {
        solVal.EstadoFondo = comercial ? serie.EstadoAporteComercial : serie.EstadoAporteBackoffice;
        solVal.EstadoBloqueo = serie.BloqueoAporte;
        solVal.EsMontoMaximo = serie.MontoMaximo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMaximo.compareTo(sol.Monto) == -1 ? NormalConstantes.NO : NormalConstantes.SI;
        if (r != null && NormalConstantes.SI.equals(r.ValidaMontoMinimoInstrumento)) {
            solVal.EsMontoMinimo = serie.MontoMinimo.compareTo(BigDecimal.ZERO) != 0 && serie.MontoMinimo.compareTo(sol.Monto) == 1 ? NormalConstantes.NO : NormalConstantes.SI;
        }
        // high equity: the amount in system currency is accumulated per client and issuer (legacy :961-975)
        BigDecimal montoPortafolioPorEmisor = c.hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + serie.IdEmisor);
        if (montoPortafolioPorEmisor == null) montoPortafolioPorEmisor = BigDecimal.ZERO;
        montoPortafolioPorEmisor = montoPortafolioPorEmisor.add(sol.MontoMonedaSistema, MathContext.DECIMAL128);
        c.hashMontosPortafolioEmisor.put(sol.IdCliente + "_" + serie.IdEmisor, montoPortafolioPorEmisor);
        if (NormalConstantes.SI.equals(serie.PatrimonioAltos)) {
            ValidaPatrimonioEmisorService.Solicitud valPatrimonio = new ValidaPatrimonioEmisorService.Solicitud();
            valPatrimonio.IdInterno = sol.IdInterno;
            valPatrimonio.IdCliente = sol.IdCliente;
            valPatrimonio.IdSerieFondo = serie.IdSerieFondo;
            valPatrimonio.IdEmisor = serie.IdEmisor;
            valPatrimonio.MontoPatrimonio = solicitud.Monto;
            valPatrimonio.ValorCuota = sol.ValorCuota;
            c.entradaValidaPatrimonioEmisor.p_Solicitudes.add(valPatrimonio);
        }
    }

    private static void regimen(InSolicitud solicitud, SolicitudAporteRescate sol, InSolicitudes solVal, RegimenSerieDeFondo r, SalidaTraspasoTotal custodiaDC) {
        if (r == null && ((NormalConstantes.TIPO_SOLICITUD_RESCATE.equals(solicitud.TipoSolicitud) && !NormalConstantes.CALCULO_POR_TOTAL.equals(solicitud.CalculoPorMontoCuota))
                || NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud))) {
            solVal.EsRegimenValido = NormalConstantes.NO;
        } else {
            solVal.PermiteAporte = NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) ? r.PermiteAporte : NormalConstantes.SI;
        }
        if (r != null) {
            if ("S".equalsIgnoreCase(r.PermiteRescate) || ("S".equalsIgnoreCase(r.PermiteTraspaso) && NormalConstantes.RAZON_RETIRO_TRASPASO.equalsIgnoreCase(solicitud.EsTraspaso))) {
                solVal.PermiteRescate = NormalConstantes.SI;
            } else {
                solVal.PermiteRescate = NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(sol.TipoSolicitud) ? r.PermiteRescate : NormalConstantes.SI;
            }
        } else if (custodiaDC != null && custodiaDC.Id != null && custodiaDC.Cantidad > 0) {
            solVal.PermiteRescate = NormalConstantes.NO;
        }
    }

    private static void portafolio(SolicitudAporteRescate sol, InPortafolio portVal, ClientePortafolioSolicitudes cliente) {
        portVal.IdInterno = sol.IdInterno;
        portVal.IdPortafolio = sol.IdPortafolio;
        portVal.IdPortafolioReal = cliente.IdPortafolio;
        portVal.IdEstadoPortafolio = cliente.IdEstadoPortafolio;
        portVal.IdCliente = sol.IdCliente;
        portVal.IdClienteReal = cliente.IdCliente;
        portVal.IdEstadoCliente = cliente.IdEstadoCliente;
        portVal.DocumentoIdentidad = cliente.DocumentoIdentidad;
        portVal.IdEmpleadoPortafolio = cliente.IdEmpleadoEjecutivo;
        portVal.IdClienteDestino = sol.IdClienteDestino;
        portVal.IdClienteRealDestino = 0L;
        portVal.IdPortafolioDestino = sol.IdPortafolio;
        portVal.IdPortafolioRealDestino = 0L;
        portVal.IdEstadoClienteDestino = "";
        portVal.IdEstadoPortafolioDestino = "";
    }
}
