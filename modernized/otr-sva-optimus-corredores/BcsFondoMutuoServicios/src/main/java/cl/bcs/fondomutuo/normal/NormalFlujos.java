package cl.bcs.fondomutuo.normal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.cierre.SerieFondoSolicitudes;
import cl.bcs.fondomutuo.comisionnn.SolicitudAporteRescate;
import cl.bcs.fondomutuo.custodia.ConsultarFlujoCustodiaFondoMutuoService;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.port.out.http.FlujoCajaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;

/** The cash-flow (Plataforma, gate G3) and custody (gate G-CUS) reads of Normal and the checks that use them (legacy :1164-1411). */
final class NormalFlujos {

    private final FlujoCajaPort flujoCaja;
    private final ConsultarFlujoCustodiaFondoMutuoService custodia;
    private final DiccionarioMensajesPort diccionario;
    private final Compuertas compuertas;

    NormalFlujos(FlujoCajaPort flujoCaja, ConsultarFlujoCustodiaFondoMutuoService custodia, DiccionarioMensajesPort diccionario, Compuertas compuertas) {
        this.flujoCaja = flujoCaja;
        this.custodia = custodia;
        this.diccionario = diccionario;
        this.compuertas = compuertas;
    }

    private MensajeSalida error(NormalContexto c, Long id, String codigo, HashMap<String, String> valores) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, valores, diccionario, c.entrada.p_IdEmpresa);
    }

    /** Legacy {@code FondoMutuoUtils.esFlujoCustodiaParaSolicitudProceso}. */
    static boolean esFlujoCustodiaParaSolicitud(ConsultarFlujoCustodiaFondoMutuoService.FlujoFondoMutuoTotalizado f, SolicitudAporteRescate s) {
        boolean regimenVacio = s.IdRegimenTributario == null || s.IdRegimenTributario.isEmpty();
        return f.IdPortafolio.equals(s.IdPortafolio) && f.IdInstrumento.equals(s.IdInstrumento) && (regimenVacio || s.IdRegimenTributario.equals(f.IdRegimenTributario));
    }

    /** The balance of the investment account per portfolio and currency (legacy :1164-1209); the gate G3 is evaluated before the call. */
    NormalContratos.FlujoCajaSalida saldoFlujoCaja(NormalContexto c, HashMap<String, BigDecimal> portTotalesMontos) {
        List<Long> portFlujo = new ArrayList<>();
        List<String> monedaFlujo = new ArrayList<>();
        NormalContratos.FlujoCajaEntrada entrada = new NormalContratos.FlujoCajaEntrada();
        entrada.P_IDEMPRESA = c.entrada.p_IdEmpresa;
        entrada.P_FECHADESDE = c.fechaSistema;
        entrada.P_FECHAHASTA = FechaLegacy.texto(c.fechaMaximaCertificacion);   // computed BEFORE the equity check, as the legacy
        entrada.P_USO = NormalConstantes.USO_FLUJO_CAJA_INGRESO;
        if (NormalValidacion.esOrigenProcesosManuales(c.entrada)) {
            for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
                if ("PAC".equalsIgnoreCase(c.entrada.p_IdOrigenProceso) && "EUR".equalsIgnoreCase(sol.IdMoneda)) {   // D-NOR-8: dead code, PAC is not manual
                    HashMap<String, String> valores = new HashMap<>();
                    valores.put("IdSerieFondo", sol.IdSerieFondo);
                    c.out.P_OUT_ERRORESCONTADOR++;
                    c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "MONEDA_INVALIDA_PAC", valores));
                } else if (NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso)
                        && NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)
                        && NormalConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago)) {
                    String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
                    BigDecimal montoAcum = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
                    portTotalesMontos.put(id, montoAcum.add(sol.MontoMonedaPago, MathContext.DECIMAL128));
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
        }
        NormalContratos.FlujoCajaSalida salida = new NormalContratos.FlujoCajaSalida();
        if (entrada.P_PORTAFOLIOACONSULTAR.size() > 0) {
            compuertas.g3SaldoFlujoCaja();
            salida = NormalContratos.saldo(flujoCaja, entrada);
        }
        return salida;
    }

    /** The custody flow of the portfolios and instruments of the pending rescues and of the contributions with minimum equity (legacy :1210-1253). */
    ConsultarFlujoCustodiaFondoMutuoService.Salida flujoCustodia(NormalContexto c) throws Exception {
        ConsultarFlujoCustodiaFondoMutuoService.Entrada entrada = new ConsultarFlujoCustodiaFondoMutuoService.Entrada();
        entrada.p_IdEmpresa = c.entrada.p_IdEmpresa;
        entrada.p_IdUsuario = c.entrada.p_IdUsuario;
        entrada.p_IdModulo = c.entrada.p_IdModulo;
        entrada.p_IdPais = c.idPaisEmpresa;
        entrada.p_FechaSaldo = c.fechaSistema;
        entrada.p_DiasProyeccion = 0;
        List<Long> fPortafolio = new ArrayList<>();
        List<String> fRegimen = new ArrayList<>();
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
            if (NormalConstantes.ESTADO_PENDIENTE.equalsIgnoreCase(sol.EstadoProceso) && (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(sol.TipoSolicitud)
                    || (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) && serie.PatrimonioMinimo.compareTo(BigDecimal.ZERO) == 1))) {
                if (!fPortafolio.contains(sol.IdPortafolio)) {
                    fPortafolio.add(sol.IdPortafolio);
                    ConsultarFlujoCustodiaFondoMutuoService.InCarteras cartera = new ConsultarFlujoCustodiaFondoMutuoService.InCarteras();
                    cartera.IdCliente = sol.IdCliente;
                    cartera.IdPortafolio = sol.IdPortafolio;
                    entrada.p_Carteras.add(cartera);
                }
                String regimen = sol.IdRegimenTributario.isEmpty() ? null : sol.IdRegimenTributario;
                if (!fRegimen.contains(serie.IdInstrumento + regimen)) {   // legacy: the key is the text "idnull" for an empty regime
                    fRegimen.add(serie.IdInstrumento + regimen);
                    ConsultarFlujoCustodiaFondoMutuoService.InInstrumento instrumento = new ConsultarFlujoCustodiaFondoMutuoService.InInstrumento();
                    instrumento.IdInstrumento = serie.IdInstrumento;
                    instrumento.IdRegimenFondoMutuo = regimen;
                    entrada.p_Instrumentos.add(instrumento);
                }
            }
        }
        ConsultarFlujoCustodiaFondoMutuoService.Salida salida = new ConsultarFlujoCustodiaFondoMutuoService.Salida();
        if (entrada.p_Carteras.size() > 0) {
            compuertas.gCustodiaNormal();   // H3: Normal calls the custody flow only with fondomutuo.custodia.normal-habilitada (the 6-argument Compuertas follows G-CUS)
            salida = custodia.logica(entrada);
        }
        return salida;
    }
}
