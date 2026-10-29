package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Timestamp;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.ComisionNnDatos.TipoMovimientoFondoMutuo;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.cuotas.CalcularMontoNumeroCuota.Resultado;
import cl.bcs.fondomutuo.fechas.CalcularFechaSolicitudAporteRescate.ResultadoFecha;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InPortafolio;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService.InSolicitudes;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/**
 * The sequences, the grouped contribution amounts, the UTC stamp and the loop that builds, per rescue, the RESREINV row, the two requests to the
 * requirements check (rescue side and contribution side), the issuer-equity candidate and the output element (FM-REI-16..23, RR:644-1089). Statement by
 * statement in the legacy order; the requirement inputs are built by {@link RreRequisitos}.
 */
final class RreArmado {

    private final SecuenciaPort secuencias;
    private final ConfiguracionSistemaPort configuracion;
    private final CalendarioPort calendario;

    RreArmado(SecuenciaPort secuencias, ConfiguracionSistemaPort configuracion, CalendarioPort calendario) {
        this.secuencias = secuencias;
        this.configuracion = configuracion;
        this.calendario = calendario;
    }

    void armar(RreContexto c) throws Exception {
        c.secuencias = secuencias.siguientes(RreConstantes.SECUENCIA_SOLICITUD, Long.valueOf(c.rescatesUnicos.size()));
        c.secuenciasIdAgrupador = secuencias.siguientes(RreConstantes.SECUENCIA_AGRUPADOR, Long.valueOf(c.rescatesUnicos.size()));
        c.entradaCumpleRequisitos.p_IdEmpresa = c.entrada.p_IdEmpresa;
        c.entradaCumpleRequisitos.p_IdUsuario = c.entrada.p_IdUsuario;
        c.entradaValidaPatrimonioEmisor.p_IdEmpresa = c.entrada.p_IdEmpresa;
        c.entradaValidaPatrimonioEmisor.p_IdUsuario = c.entrada.p_IdUsuario;
        c.entradaValidaPatrimonioEmisor.p_Fecha = c.fechaSistema;
        for (InSolicitudesAporte revTtl : c.entrada.p_SolicitudesAporte) {   // FM-REI-17: written with the IdAgrupador of the INPUT (D-F2-15)
            Resultado revApo = c.valoresCuota.get(revTtl.IdInterno * -1);
            RreReglas.KeyAporteMonto key = new RreReglas.KeyAporteMonto();
            key.IdSerieFondo = revTtl.IdSerieFondo;
            key.IdAgrupador = RreReglas.esCanalIngresoWebServices(c.entrada.p_IdTipoCanalIngreso) ? 0L : revTtl.IdAgrupador;
            BigDecimal monto = c.aportesAgrupadosPorSerieOperacion.get(key) == null ? BigDecimal.ZERO : c.aportesAgrupadosPorSerieOperacion.get(key);
            monto = monto.add(revApo.Monto, MathContext.DECIMAL128);
            c.aportesAgrupadosPorSerieOperacion.put(key, monto);
        }
        c.folioContratoSolicitudes = secuencias.siguientes(RreConstantes.SECUENCIA_FOLIO_CONTRATO, (long) c.entrada.p_SolicitudesRescate.size());
        int segundosZona = (int) configuracion.entero(c.entrada.p_IdEmpresa, "PLATAFORMA", "PLA.SegundosZonaHoraria");
        Timestamp local = calendario.fechaHoraSistemaLocal(c.entrada.p_IdEmpresa);
        c.fechaHoraUTC = new Timestamp(local.getTime() - segundosZona * 1000L);   // DEV-F2: local to the call (legacy: Fecha.setLocalOffSet, static)
        for (InSolicitudesRescate solicitud : c.entrada.p_SolicitudesRescate) {
            unaSolicitud(c, solicitud);
        }
        c.hSolicitudSalida = ListaHash.aHash(c.resultadoTemp, r -> r.IdInterno);
    }

    /** The body of the legacy loop for one rescue (RR:688-1085); the {@code continue} of FM-REI-19 returns before the row is kept. */
    private void unaSolicitud(RreContexto c, InSolicitudesRescate solicitud) throws Exception {
        InSolicitudesAporte apor = c.haportes.get(solicitud.IdInterno);
        ClientePortafolioSolicitudes cliente = c.hclientes.get(solicitud.IdPortafolio);
        TipoMovimientoFondoMutuo tipoMovimientoRescate = c.htipoMovimiento.get(RreConstantes.REINVERSION);
        TipoMovimientoFondoMutuo tipoMovimientoAporte = c.htipoMovimiento.get(RreConstantes.APORTE_REINVERSION);
        SerieFondoSolicitudes serie = c.hseries.get(solicitud.IdSerieFondo);
        SerieFondoSolicitudes serieDestino = c.hseries.get(apor.IdSerieFondo);
        ResultadoFecha fecha = c.hfechas.get(solicitud.IdInterno);
        RreDatos.RegimenSerieDeFondo rRescate = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
        RreDatos.RegimenSerieDeFondo rAporte = c.hseriesRegimen.get(apor.IdSerieFondo + "_" + apor.IdRegimenTributario);
        RreDatos.ConfiguracionRegimenFondoMutuo confRegimen = c.hregimenConfiguraciones.get(solicitud.IdRegimenTributario + "_" + apor.IdRegimenTributario);
        Resultado vc = c.valoresCuota.get(solicitud.IdInterno);
        Resultado vcApor = c.valoresCuota.get(solicitud.IdInterno * -1);
        RreDatos.ConfiguracionPropositoPortafolio confProposito = null;
        if (cliente != null) confProposito = c.hconfRegimenPortafolio.get(apor.IdRegimenTributario + "_" + cliente.Proposito);
        RreDatos.RegimenMandato rMandato = new RreDatos.RegimenMandato();   // D-F2-14: never null
        for (RreDatos.RegimenMandato rm : c.regimenMandato) {
            if (solicitud.IdSerieFondo.equalsIgnoreCase(rm.IdSerieFondo)) rMandato = rm;
        }
        if (!c.rescatesUnicosV2.containsKey(solicitud.IdAgrupador)) c.rescatesUnicosV2.put(solicitud.IdAgrupador, solicitud);
        SolicitudAporteRescate sol = RreEncabezado.solicitud(c, solicitud, apor, cliente, serie, serieDestino, vc, fecha, rRescate);
        // FM-REI-19, D-F2-17: BF with zero quotas or amount (rescue or contribution) is dropped without a message
        if ((BigDecimal.ZERO.compareTo(sol.NumeroCuotas) == 0 || BigDecimal.ZERO.compareTo(sol.Monto) == 0) && "BF".equalsIgnoreCase(sol.IdRegimenTributario)) {
            return;
        }
        if ((vcApor != null) && ((BigDecimal.ZERO.compareTo(vcApor.NumeroCuotas) == 0 || BigDecimal.ZERO.compareTo(vcApor.Monto) == 0)
                && "BF".equalsIgnoreCase(sol.IdRegimenTributario))) {
            return;
        }
        c.solicitudesProcesar.add(sol);
        InSolicitudes solVal = new InSolicitudes();
        InPortafolio portVal = new InPortafolio();
        InSolicitudes solValApor = new InSolicitudes();
        InPortafolio portValApor = new InPortafolio();
        RreRequisitos.rescate(c, solicitud, apor, sol, serie, serieDestino, cliente, rRescate, confRegimen, tipoMovimientoRescate, solVal, portVal);
        c.entradaCumpleRequisitos.p_Portafolios.add(portVal);
        c.entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solVal);
        RreRequisitos.aporte(c, solicitud, apor, sol, serie, serieDestino, cliente, rRescate, rAporte, rMandato, confProposito, vcApor, tipoMovimientoAporte,
                solVal, solValApor, portValApor);
        c.entradaCumpleRequisitos.p_Portafolios.add(portValApor);
        c.entradaCumpleRequisitos.p_SolicitudesAporteRescate.add(solValApor);
        c.resultadoTemp.add(RreEncabezado.salida(sol, serie, cliente, vcApor, apor));
    }

    /** {@code Fecha.stringToSqlDate} of a row date for the output (null = NullPointerException). */
    static String texto(java.util.Date fecha) {
        return FechaLegacy.texto(fecha);
    }
}
