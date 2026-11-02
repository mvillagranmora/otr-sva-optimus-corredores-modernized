package cl.bcs.fondomutuo.aportereinversion;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.aportereinversion.AporteReinversionDatos.IndicadoresPortafolioFondoMutuo;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.cierre.contrato.ParidadContrato.CalculoSalida;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.NormalContratos;
import cl.bcs.fondomutuo.normal.NormalDatos.RegimenSerieDeFondo;
import cl.bcs.fondomutuo.patrimonio.ValidaPatrimonioEmisorService;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.ServicioCumpleRequisitosService;
import cl.bcs.fondomutuo.requisitos.ValidaMontoInversionService;

/**
 * The checks of one lot of {@code CrearSolicitudAporteReinversion} that decide which requests stay pending (legacy :1020-1236): requirements, investment amounts, issuer
 * equity, the cash flow (gate G3) and custody (gate G-CUS, inside the custody service) reads, and the controls that use them.
 */
final class AporteReinversionControles {

    private final ServicioCumpleRequisitosService requisitos;
    private final ValidaMontoInversionService montoInversion;
    private final ValidaPatrimonioEmisorService patrimonio;
    private final FlujoCajaPort flujoCaja;
    private final ConsultarFlujoCustodiaFondoMutuoService custodia;
    private final DiccionarioMensajesPort diccionario;
    private final Compuertas compuertas;

    AporteReinversionControles(ServicioCumpleRequisitosService requisitos, ValidaMontoInversionService montoInversion, ValidaPatrimonioEmisorService patrimonio,
            FlujoCajaPort flujoCaja, ConsultarFlujoCustodiaFondoMutuoService custodia, DiccionarioMensajesPort diccionario, Compuertas compuertas) {
        this.requisitos = requisitos;
        this.montoInversion = montoInversion;
        this.patrimonio = patrimonio;
        this.flujoCaja = flujoCaja;
        this.custodia = custodia;
        this.diccionario = diccionario;
        this.compuertas = compuertas;
    }

    private MensajeSalida error(AporteReinversionContexto c, Long id, String codigo) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, new HashMap<>(), diccionario, c.entrada.p_IdEmpresa);
    }

    private static void rechazar(AporteReinversionContexto c, Long idInterno) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (sol.IdInterno.equals(idInterno)) {
                sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                break;
            }
        }
    }

    /** Legacy :1023-1045. @return false when no request is left to process (the lot ends without writing). */
    boolean requisitos(AporteReinversionContexto c) throws Exception {
        ServicioCumpleRequisitosService.Salida salida = requisitos.logica(c.entradaCumpleRequisitos);
        for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
            c.out.P_OUT_MENSAJES.add(mje);
            if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                c.out.P_OUT_ERRORESCONTADOR++;
                for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                    if (sol.IdInterno.equals(Math.abs(mje.id))) {
                        sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                        break;
                    }
                }
            }
        }
        long porProcesar = c.solicitudesProcesar.stream().filter(sol -> AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)).count();
        return porProcesar != 0;
    }

    /** Legacy :1047-1086: the amount of every request (rejected ones too) in the system currency, and the latest certification date of the pending ones. */
    void montoInversion(AporteReinversionContexto c) throws Exception {
        ValidaMontoInversionService.Entrada entrada = new ValidaMontoInversionService.Entrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_Solicitudes = new ArrayList<>();
        for (SolicitudAporteRescate solicitud : c.solicitudesProcesar) {
            SerieFondoSolicitudes serieFondoSolicitud = c.hseries.get(solicitud.IdSerieFondo);
            RegimenSerieDeFondo regimenSerieFondo = c.hseriesRegimen.get(solicitud.IdSerieFondo + "_" + solicitud.IdRegimenTributario);
            CalculoSalida montoMonedaSistema = c.hashMontoTotalInversionMonedaSistema.get(solicitud.IdInterno);
            ValidaMontoInversionService.Solicitud aValidar = new ValidaMontoInversionService.Solicitud();
            aValidar.IdInterno = solicitud.IdInterno;
            aValidar.FolioContrato = 0L;
            aValidar.AportesAltos = serieFondoSolicitud.AportesAltos;   // legacy: no null check (a missing series is a NullPointerException)
            aValidar.Monto = montoMonedaSistema != null ? montoMonedaSistema.MontoDestino : BigDecimal.ZERO;
            aValidar.ValidaMontoMinimo = regimenSerieFondo != null ? regimenSerieFondo.ValidaMontoMinimoInstrumento : AporteReinversionConstantes.SI;
            entrada.p_Solicitudes.add(aValidar);
        }
        ValidaMontoInversionService.Salida salida = montoInversion.logica(entrada);
        for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
            c.out.P_OUT_MENSAJES.add(mje);
            if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                c.out.P_OUT_ERRORESCONTADOR++;
                rechazar(c, mje.id);
            }
        }
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso) && sol.FechaCertificacion.compareTo(c.fechaMaximaCertificacion) == 1) {
                c.fechaMaximaCertificacion = sol.FechaCertificacion;
            }
        }
    }

    /** Legacy :1088-1107: the equity of the issuer, with what the lot adds per client and issuer; no check of the origin of the process. */
    void patrimonioEmisor(AporteReinversionContexto c) throws Exception {
        if (c.entradaValidaPatrimonioEmisor.p_Solicitudes.size() > 0) {
            for (ValidaPatrimonioEmisorService.Solicitud sol : c.entradaValidaPatrimonioEmisor.p_Solicitudes) {
                BigDecimal montoPorEmisor = c.hashMontosPortafolioEmisor.get(sol.IdCliente + "_" + sol.IdEmisor);
                if (montoPorEmisor != null) sol.MontoPatrimonio = sol.MontoPatrimonio.add(montoPorEmisor, MathContext.DECIMAL128);
            }
            ValidaPatrimonioEmisorService.Salida salida = patrimonio.logica(c.entradaValidaPatrimonioEmisor);
            for (MensajeSalida mje : salida.P_OUT_MENSAJES) {
                if (MensajeSalida.MENSAJE_ERROR.equalsIgnoreCase(mje.tipo)) {
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(mje);
                    rechazar(c, mje.id);
                }
            }
        }
    }

    /**
     * Legacy :1109-1143: the balance of the investment account per portfolio and currency for the pending CIN requests (the total uses the local cost). The gate G3 is evaluated
     * before the call, in simulation too (it is a read).
     */
    NormalContratos.FlujoCajaSalida saldoFlujoCaja(AporteReinversionContexto c, HashMap<String, BigDecimal> portTotalesMontos) {
        List<Long> portFlujo = new ArrayList<>();
        List<String> monedaFlujo = new ArrayList<>();
        NormalContratos.FlujoCajaEntrada entrada = new NormalContratos.FlujoCajaEntrada();
        entrada.P_IDEMPRESA = c.entrada.p_IdEmpresa;
        entrada.P_FECHADESDE = c.fechaSistema;
        entrada.P_FECHAHASTA = FechaLegacy.texto(c.fechaMaximaCertificacion);
        entrada.P_USO = AporteReinversionConstantes.USO_FLUJO_CAJA_INGRESO;
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso) && AporteReinversionConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago)) {
                String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
                BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
                portTotalesMontos.put(id, montoAcum.add(sol.CostoMonedaLocal, MathContext.DECIMAL128));
                if (!portFlujo.contains(sol.IdPortafolio) || !monedaFlujo.contains(sol.IdMonedaPago)) {
                    NormalContratos.PortafolioAConsultar saldo = new NormalContratos.PortafolioAConsultar();
                    saldo.IdPortafolio = sol.IdPortafolio;
                    saldo.IdMoneda = sol.IdMonedaPago;
                    entrada.P_PORTAFOLIOACONSULTAR.add(saldo);
                    if (!portFlujo.contains(sol.IdPortafolio)) portFlujo.add(sol.IdPortafolio);
                    if (!monedaFlujo.contains(sol.IdMonedaPago)) monedaFlujo.add(sol.IdMonedaPago);
                }
            }
        }
        NormalContratos.FlujoCajaSalida salida = new NormalContratos.FlujoCajaSalida();
        if (entrada.P_PORTAFOLIOACONSULTAR.size() > 0) {
            compuertas.g3SaldoFlujoCaja();
            salida = NormalContratos.saldo(flujoCaja, entrada);
        }
        return salida;
    }

    /** Legacy :1145-1181: the custody flow of the portfolios and instruments of the pending requests whose series demands a minimum equity. */
    ConsultarFlujoCustodiaFondoMutuoService.Salida flujoCustodia(AporteReinversionContexto c) throws Exception {
        ConsultarFlujoCustodiaFondoMutuoService.Entrada entrada = new ConsultarFlujoCustodiaFondoMutuoService.Entrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_IdModulo = c.entrada.p_IdModulo;
        entrada.p_IdPais = c.idPaisEmpresa;
        entrada.p_FechaSaldo = c.fechaSistema;
        entrada.p_DiasProyeccion = 0;
        List<Long> fPortafolio = new ArrayList<>();
        List<Long> fInstrumento = new ArrayList<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
            if (AporteReinversionConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso) && serie.PatrimonioMinimo.compareTo(BigDecimal.ZERO) == 1) {
                if (!fPortafolio.contains(sol.IdPortafolio)) {
                    fPortafolio.add(sol.IdPortafolio);
                    ConsultarFlujoCustodiaFondoMutuoService.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuoService.InCarteras();
                    cartera.IdCliente = sol.IdCliente;
                    cartera.IdPortafolio = sol.IdPortafolio;
                    entrada.p_Carteras.add(cartera);
                }
                if (!fInstrumento.contains(serie.IdInstrumento)) {
                    fInstrumento.add(serie.IdInstrumento);
                    ConsultarFlujoCustodiaFondoMutuoService.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuoService.InInstrumento();
                    instrumento.IdInstrumento = serie.IdInstrumento;
                    instrumento.IdRegimenFondoMutuo = sol.IdRegimenTributario;
                    entrada.p_Instrumentos.add(instrumento);
                }
            }
        }
        ConsultarFlujoCustodiaFondoMutuoService.Salida salida = new ConsultarFlujoCustodiaFondoMutuoService.Salida();
        if (entrada.p_Carteras.size() > 0) {
            salida = custodia.logica(entrada);   // the service evaluates G-CUS itself
        }
        return salida;
    }

    /** Legacy {@code FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso}. */
    private static boolean esFlujoCustodiaParaSolicitud(ConsultarFlujoCustodiaFondoMutuoService.FlujoFondoMutuoTotalizado f, SolicitudAporteRescate s) {
        boolean regimenVacio = s.IdRegimenTributario == null || s.IdRegimenTributario.isEmpty();
        return f.IdPortafolio.equals(s.IdPortafolio) && f.IdInstrumento.equals(s.IdInstrumento) && (regimenVacio || s.IdRegimenTributario.equals(f.IdRegimenTributario));
    }

    /**
     * Legacy :1190-1236: minimum equity (the custody counts), balance of the investment account (only for requests that do not reinvest a rescue and whose portfolio does not
     * operate with an overdraft line), and the requests that go to Tesoreria (every medium but CIN).
     */
    void controlar(AporteReinversionContexto c, NormalContratos.FlujoCajaSalida salidaFlujoCaja, HashMap<String, BigDecimal> portTotalesMontos,
            ConsultarFlujoCustodiaFondoMutuoService.Salida salidaFlujo) {
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (AporteReinversionConstantes.ESTADO_RECHAZADO.equalsIgnoreCase(sol.EstadoProceso)) continue;
            SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
            IndicadoresPortafolioFondoMutuo ind = c.hindicadoresPortafolioFFMM.get(sol.IdPortafolio);
            String sobregiro = ind != null ? ind.OperaLineaSobregiro : AporteReinversionConstantes.NO;
            BigDecimal monto = BigDecimal.ZERO;
            for (ConsultarFlujoCustodiaFondoMutuoService.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO) {
                if (esFlujoCustodiaParaSolicitud(flujoCustodia, sol)) {
                    monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
                    break;
                }
            }
            if (serie.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1) {
                sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                c.out.P_OUT_ERRORESCONTADOR++;
                c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "UI_PATRIMONIO_MINIMO_INSUFICIENTE"));
                continue;
            }
            if (AporteReinversionConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago) && AporteReinversionConstantes.NO.equalsIgnoreCase(sobregiro)
                    && sol.IdSolicitudAporteRescateRelacionado.equals(0L)) {
                String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
                BigDecimal saldo = BigDecimal.ZERO;
                BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
                String fecha = FechaLegacy.texto(sol.FechaCertificacion);
                for (NormalContratos.FlujoCaja f : salidaFlujoCaja.P_OUT_FLUJOCAJA) {
                    if (f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) && f.FechaSaldo.equalsIgnoreCase(fecha)) {
                        saldo = f.Saldo;
                    }
                }
                if (total.compareTo(saldo) == 1) {
                    sol.EstadoProceso = AporteReinversionConstantes.ESTADO_RECHAZADO;
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "UI_SALDO_INSUFICIENTE_FLUJO_CAJA"));
                    continue;
                }
            }
            if (!AporteReinversionConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago)) {
                c.entradaTesoreria.p_Solicitudes.add(sol);
            }
        }
    }

    /** The first date of the lot: legacy :459 ({@code Fecha.toDate("0001-01-01")}). */
    static Date fechaInicialMaxima() {
        return FechaLegacy.aFechaSql(AporteReinversionConstantes.FECHA_DEFECTO);
    }
}
