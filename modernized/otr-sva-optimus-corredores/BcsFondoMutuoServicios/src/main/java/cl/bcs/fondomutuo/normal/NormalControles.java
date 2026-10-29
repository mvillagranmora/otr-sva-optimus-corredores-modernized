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
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.normal.NormalContrato.InFormaPagoSpot;
import cl.bcs.fondomutuo.normal.NormalDatos.ParametroOperaCSN;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.requisitos.RequisitosDatos.ClientePortafolioSolicitudes;

/** The checks of custody, minimum equity and investment-account balance, and the preparation of the Tesoreria / Spot inputs (legacy :1266-1411). */
final class NormalControles {

    private final NormalDatos datos;
    private final DiccionarioMensajesPort diccionario;

    NormalControles(NormalDatos datos, DiccionarioMensajesPort diccionario) {
        this.datos = datos;
        this.diccionario = diccionario;
    }

    private MensajeSalida error(NormalContexto c, Long id, String codigo, HashMap<String, String> valores) {
        return MensajeSalida.conTexto(id, MensajeSalida.MENSAJE_ERROR, codigo, valores, diccionario, c.entrada.p_IdEmpresa);
    }

    void controlar(NormalContexto c, NormalContratos.FlujoCajaSalida salidaFlujoCaja, HashMap<String, BigDecimal> portTotalesMontos,
            ConsultarFlujoCustodiaFondoMutuoService.Salida salidaFlujo) {
        ArrayList<Long> portafoliosABuscar = new ArrayList<>();
        c.solicitudesProcesar.forEach(soli -> portafoliosABuscar.add(soli.IdPortafolio));
        List<ParametroOperaCSN> parametrosOPCSN = datos.parametrosOperaConSaldoNegativo(portafoliosABuscar);
        NormalContrato.Entrada entrada = c.entrada;
        for (SolicitudAporteRescate sol : c.solicitudesProcesar) {
            if (NormalConstantes.ESTADO_RECHAZADO.equalsIgnoreCase(sol.EstadoProceso)) continue;
            SerieFondoSolicitudes serie = c.hseries.get(sol.IdSerieFondo);
            BigDecimal custodia = BigDecimal.ZERO;
            BigDecimal monto = BigDecimal.ZERO;
            for (ConsultarFlujoCustodiaFondoMutuoService.FlujoFondoMutuoTotalizado flujoCustodia : salidaFlujo.P_OUT_FLUJOFONDOMUTUOTOTALIZADO) {
                if (NormalFlujos.esFlujoCustodiaParaSolicitud(flujoCustodia, sol)) {
                    custodia = flujoCustodia.CantidadDisponible;
                    monto = flujoCustodia.CantidadDisponible.multiply(sol.ValorCuota, MathContext.DECIMAL128);
                    break;
                }
            }
            if (NormalConstantes.TIPO_SOLICITUD_RESCATE.equalsIgnoreCase(sol.TipoSolicitud) && sol.NumeroCuotas.compareTo(custodia) == 1) {
                sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                c.out.P_OUT_ERRORESCONTADOR++;
                HashMap<String, String> parametrosMensaje = new HashMap<>();
                parametrosMensaje.put("Nemotecnico", sol.IdSerieFondo);
                parametrosMensaje.put("CantidadRetiro", sol.NumeroCuotas.toPlainString());
                parametrosMensaje.put("CantidadCustodia", custodia.toPlainString());
                c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "UI_ERROR_FM_CLIENTE_SIN_CUSTODIA", parametrosMensaje));
                continue;
            }
            // D-NOR-4: the legacy compares the strings BY REFERENCE ("!=") with a literal and with the constant: with the request parsed from JSON
            // (every caller of the module) the strings are never the interned literals, so both are always true and the minimum equity is demanded
            // from PAC and SAAPV requests too; an in-process caller with literals would be exempt. Kept bug for bug: do not "fix" to equals.
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)
                    && serie.PatrimonioMinimo.compareTo(sol.Monto.add(monto, MathContext.DECIMAL128)) == 1
                    && entrada.p_IdOrigenProceso != "PAC" && !NormalConstantes.ORIGEN_BONIFICACION.equalsIgnoreCase(entrada.p_IdOrigenProceso)
                    && sol.TipoContratoOperacion != NormalConstantes.SAAPV) {
                sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                c.out.P_OUT_ERRORESCONTADOR++;
                c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "UI_PATRIMONIO_MINIMO_INSUFICIENTE", new HashMap<>()));
                continue;
            }
            String id = sol.IdPortafolio.toString() + "_" + sol.IdMonedaPago;
            String fecha = FechaLegacy.texto(sol.FechaAceptacion);
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud) && NormalConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago)
                    && NormalValidacion.esOrigenProcesosManuales(entrada)) {
                if (saldoInsuficiente(c, sol, id, fecha, salidaFlujoCaja, portTotalesMontos, parametrosOPCSN)) {
                    continue;
                }
            }
            if (NormalConstantes.TIPO_SOLICITUD_APORTE.equalsIgnoreCase(sol.TipoSolicitud)) {
                prepararMovimientos(c, sol);
            }
        }
    }

    /** Legacy :1320-1352: the total of the contributions to the account against its balance (CELLINCECT-763: "operates with a negative balance"). */
    private boolean saldoInsuficiente(NormalContexto c, SolicitudAporteRescate sol, String id, String fecha, NormalContratos.FlujoCajaSalida salidaFlujoCaja,
            HashMap<String, BigDecimal> portTotalesMontos, List<ParametroOperaCSN> parametrosOPCSN) {
        BigDecimal saldo = BigDecimal.ZERO;
        BigDecimal total = portTotalesMontos.get(id) != null ? portTotalesMontos.get(id) : BigDecimal.ZERO;
        for (NormalContratos.FlujoCaja f : salidaFlujoCaja.P_OUT_FLUJOCAJA) {
            if (f.IdPortafolio.longValue() == sol.IdPortafolio && f.IdMoneda.equalsIgnoreCase(sol.IdMonedaPago) && f.FechaSaldo.equalsIgnoreCase(fecha)) {
                saldo = f.Saldo;
            }
        }
        ParametroOperaCSN paramOPCSN = parametrosOPCSN.stream().filter(parametro -> parametro.IdEntidad.equals(sol.IdPortafolio)).findFirst()
                .orElse(new ParametroOperaCSN());
        if ("N".equalsIgnoreCase(paramOPCSN.ValorIndicador) || !"CORREDORES".equalsIgnoreCase(sol.IngresoDesde)) {
            if (total.compareTo(saldo) == 1 && NormalConstantes.NO.equals(sol.SimulaCambioPortafolioEjecutadoOk)) {
                ClientePortafolioSolicitudes clientePortafolio = c.hclientes.get(sol.IdPortafolio);
                HashMap<String, String> mapMsg = new HashMap<>();
                mapMsg.put("CodigoPortafolio", clientePortafolio.CodigoPortafolio);
                mapMsg.put("DocumentoIdentidad", clientePortafolio.DocumentoIdentidad);
                sol.EstadoProceso = NormalConstantes.ESTADO_RECHAZADO;
                c.out.P_OUT_ERRORESCONTADOR++;
                c.out.P_OUT_MENSAJES.add(error(c, sol.IdInterno, "UI_SALDO_INSUFICIENTE_FLUJO_CAJA", mapMsg));
                return true;
            }
        }
        return false;
    }

    /** Legacy :1354-1409: a contribution that is not paid from the account goes to Tesoreria; one that generates Spot goes to Mesa with its forms of payment. */
    private static void prepararMovimientos(NormalContexto c, SolicitudAporteRescate sol) {
        String omitirTesoreria = NormalConstantes.NO;
        if (NormalConstantes.CUENTA_INVERSION.equalsIgnoreCase(sol.IdMedioDePago)) {
            omitirTesoreria = NormalConstantes.SI;
        }
        if (NormalConstantes.NO.equalsIgnoreCase(omitirTesoreria)) {
            c.entradaTesoreria.p_Solicitudes.add(sol);
        }
        if (NormalConstantes.SI.equalsIgnoreCase(sol.GeneraSpot)) {
            sol.Estado = NormalConstantes.ESTADO_PENDIENTE;
            c.entradaSpot.p_DatosOperacion.add(sol);
            for (InFormaPagoSpot pagos : c.entrada.p_FormaPagoSpot) {
                ProcesosExternos.FormaPagoSpot pag = new ProcesosExternos.FormaPagoSpot();
                if (pagos.IdOperacion.equals(sol.IdInterno)) {
                    pag.AfectarTesoreria = pagos.AfectarTesoreria;
                    pag.IdComprobantePagoOperacion = pagos.IdComprobantePagoOperacion;
                    pag.IdFormaDePago = pagos.IdFormaDePago;
                    pag.IdInternoPagoOperacion = pagos.IdInternoPagoOperacion;
                    pag.IdMoneda = pagos.IdMoneda;
                    pag.IdMonedaLiquidacion = pagos.IdMonedaLiquidacion;
                    pag.IdOperacion = pagos.IdOperacion;
                    pag.IdTipoMovimiento = pagos.IdTipoMovimiento;
                    pag.MontoFormaDePago = pagos.MontoFormaDePago;
                    pag.RecibePagaPortafolio = pagos.RecibePagaPortafolio;
                    pag.FechaLiquidacion = sol.FechaAceptacion;
                    c.entradaSpot.p_DatosFormaPago.add(pag);
                }
            }
        }
    }
}
