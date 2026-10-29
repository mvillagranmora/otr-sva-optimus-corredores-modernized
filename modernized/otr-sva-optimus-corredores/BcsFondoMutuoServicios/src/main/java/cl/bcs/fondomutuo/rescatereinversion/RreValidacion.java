package cl.bcs.fondomutuo.rescatereinversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService.FlujoFondoMutuoTotalizado;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/** Requirements, one request per group, issuer equity, custody and minimum equity (FM-REI-24..29, RR:1089-1231), in the legacy order. */
final class RreValidacion {

    private final ServicioCumpleRequisitosService requisitos;
    private final ValidaPatrimonioEmisorService patrimonio;
    private final ConsultarFlujoCustodiaFondoMutuoService custodia;
    private final DiccionarioMensajesPort diccionario;

    RreValidacion(ServicioCumpleRequisitosService requisitos, ValidaPatrimonioEmisorService patrimonio, ConsultarFlujoCustodiaFondoMutuoService custodia,
            DiccionarioMensajesPort diccionario) {
        this.requisitos = requisitos;
        this.patrimonio = patrimonio;
        this.custodia = custodia;
        this.diccionario = diccionario;
    }

    private static void rechazarPrimera(RreContexto c, Long idInterno) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (sol.IdInterno.equals(idInterno)) {
                sol.EstadoProceso = RreConstantes.ESTADO_RECHAZADO;
                break;
            }
        }
    }

    /** FM-REI-24..27. @return false when nothing is pending (the caller releases the lock and answers, RR:1142-1145). */
    boolean validar(RreContexto c) throws Exception {
        ServicioCumpleRequisitosService.Salida salida = requisitos.logica(c.entradaCumpleRequisitos);
        for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
            mje.id = Math.abs(mje.id);
            c.out.P_OUT_MENSAJES.add(mje);
            if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                c.out.P_OUT_ERRORESCONTADOR++;
                rechazarPrimera(c, mje.id);
            }
        }
        List<Long> ids = new ArrayList<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {   // FM-REI-25, D-F2-6: the first request of each IdSolicitudAporteRescate
            if (!ids.contains(sol.IdSolicitudAporteRescate)) {
                c.solicitudesProcesarFinal.add(sol);
                ids.add(sol.IdSolicitudAporteRescate);
            }
            if (c.rescatesUnicos.containsKey(sol.IdAgrupadorInterno)) {
                c.rescatesUnicos.remove(sol.IdAgrupadorInterno);
            }
        }
        if (c.entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0) {
            ValidaPatrimonioEmisorService.Salida sp = patrimonio.logica(c.entradaValidaPatrimonioEmisor);
            for (MensajeSalida mje : sp.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(mje);
                    rechazarPrimera(c, mje.id);
                }
            }
        }
        long registrosPorProcesar = c.solicitudesProcesar.stream().filter(sol -> RreConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)).count();
        return registrosPorProcesar != 0L;
    }

    /** FM-REI-28/29: the custody flow is ALWAYS read (no guard on an empty list, unlike Normal) and then custody and minimum equity are checked. */
    void custodia(RreContexto c) throws Exception {
        ConsultarFlujoCustodiaFondoMutuoService.Entrada entradaFlujo = new ConsultarFlujoCustodiaFondoMutuoService.Entrada();
        entradaFlujo.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entradaFlujo.p_IdUsuario = c.entrada.p_IdUsuario;
        entradaFlujo.p_IdModulo = c.entrada.p_IdModulo;
        entradaFlujo.p_IdPais = c.idPaisEmpresa;
        entradaFlujo.p_FechaSaldo = c.fechaSistema;
        entradaFlujo.p_DiasProyeccion = 0;
        List<Long> fPortafolio = new ArrayList<>();
        Set<String> fCustodiaInstrumento = new HashSet<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesarFinal) {
            if (RreConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)) {
                SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
                if (!fPortafolio.contains(sol.IdPortafolio)) {
                    fPortafolio.add(sol.IdPortafolio);
                    ConsultarFlujoCustodiaFondoMutuoService.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuoService.InCarteras();
                    cartera.IdCliente = sol.IdCliente;
                    cartera.IdPortafolio = sol.IdPortafolio;
                    entradaFlujo.p_Carteras.add(cartera);
                }
                String idInstrumento = serie.IdInstrumento != null ? serie.IdInstrumento.toString() : "";
                String idRegimenTributario = sol.IdRegimenTributario != null ? sol.IdRegimenTributario : "";
                if (fCustodiaInstrumento.add(idInstrumento + "_" + idRegimenTributario)) {
                    ConsultarFlujoCustodiaFondoMutuoService.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuoService.InInstrumento();
                    instrumento.IdInstrumento = serie.IdInstrumento;
                    instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
                    entradaFlujo.p_Instrumentos.add(instrumento);
                }
            }
        }
        ConsultarFlujoCustodiaFondoMutuoService.Salida salidaFlujo = custodia.logica(entradaFlujo);
        for (SolicitudAporteRescate sol : c.solicitudesProcesarFinal) {
            if (RreConstantes.ESTADO_RECHAZADO.equalsIgnoreCase(sol.EstadoProceso)) continue;
            BigDecimal cantidad = BigDecimal.ZERO;
            BigDecimal monto = BigDecimal.ZERO;
            for (FlujoFondoMutuoTotalizado flujo : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO) {
                if (esFlujoCustodiaParaSolicitudProceso(flujo, sol)) {   // D-F2-8: the LAST matching flow wins
                    cantidad = flujo.CantidadDisponible;
                    monto = flujo.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
                }
            }
            if (sol.NumeroCuotas.compareTo(cantidad) == 1) {
                sol.EstadoProceso = RreConstantes.ESTADO_RECHAZADO;
                c.out.P_OUT_ERRORESCONTADOR++;
                HashMap<String, String> parametros = new HashMap<>();
                parametros.put("Nemotecnico", sol.IdSerieFondo);
                parametros.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
                parametros.put("CantidadCustodia", cantidad.toPlainString());
                c.out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(sol.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_FM_CLIENTE_SIN_CUSTODIA", parametros, diccionario,
                        c.entrada.p_IdEmpresa));
                if (RreConstantes.ORIGEN_CARGA_MASIVA_FM.equalsIgnoreCase(c.entrada.p_IdOrigenProceso)) {
                    continue;
                } else {
                    break;   // D-F2-8: the following requests are not checked
                }
            }
            for (InSolicitudesRescate solicitud : c.entrada.p_SolicitudesRescate) {   // D-F2-8: against the destination of EVERY rescue of the input
                InSolicitudesAporte apor = c.haportes.get(solicitud.IdInterno);
                SerieFondoSolicitudes serieDestino = c.hseries.get(apor.IdSerieFondo);
                HashMap<String, String> params2 = new HashMap<>();
                params2.put("IdInstrumento", serieDestino.IdSerieFondo);
                if (serieDestino.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1) {
                    sol.EstadoProceso = RreConstantes.ESTADO_RECHAZADO;
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(MensajeSalida.conTexto(sol.IdInterno, MensajeSalida.MENSAJE_ERROR, "UI_PATRIMONIO_MINIMO_INSUFICIENTE", params2,
                            diccionario, c.entrada.p_IdEmpresa));
                    break;
                }
            }
        }
    }

    /** Legacy {@code FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso}. */
    static boolean esFlujoCustodiaParaSolicitudProceso(FlujoFondoMutuoTotalizado f, SolicitudAporteRescate s) {
        boolean regimenVacio = s.IdRegimenTributario == null || s.IdRegimenTributario.isEmpty();
        return f.IdPortafolio.equals(s.IdPortafolio) && f.IdInstrumento.equals(s.IdInstrumento) && (regimenVacio || s.IdRegimenTributario.equals(f.IdRegimenTributario));
    }
}
