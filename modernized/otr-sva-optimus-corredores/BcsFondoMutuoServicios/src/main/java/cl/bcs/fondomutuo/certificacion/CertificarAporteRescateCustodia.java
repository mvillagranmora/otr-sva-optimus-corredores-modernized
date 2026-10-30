package cl.bcs.fondomutuo.certificacion;

import static cl.bcs.fondomutuo.certificacion.CerConstantes.APORTE_REINVERSION;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.BCS;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.COR;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.ESTADO_RECHAZADO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.NO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.SI;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_COMPROBANTE_CUSTODIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_OPERACION_APORTE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_OPERACION_RESCATE_FONDO_MUTUO;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_PRODUCTO_CUSTODIA;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.TIPO_SOLICITUD_APORTE;
import static cl.bcs.fondomutuo.certificacion.CerConstantes.UF;

import java.math.BigDecimal;
import java.util.List;

import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CustodiaEntrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.CustodiaSalida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateColaboradores.MovimientoCustodia;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Entrada;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.Salida;
import cl.bcs.fondomutuo.certificacion.CertificarAporteRescateContrato.SolicitudProceso;
import cl.bcs.fondomutuo.cierre.FechaLegacy;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/** What the entry (aportes) and the retirement (rescates) of custody share: legacy {@code ingresoRetiroCustodia} and the handling of the error messages. No state. */
final class CertificarAporteRescateCustodia {

    private CertificarAporteRescateCustodia() {
    }

    /**
     * Legacy {@code ingresoRetiroCustodia}: one movement per solicitud that is not rejected (in the order of the list); the collaborator is called only when there is at least one.
     * Without movements the answer is an empty one (counter 0).
     */
    static CustodiaSalida ingresoRetiro(ProcesarCustodiaFondoMutuoPort custodia, List<SolicitudProceso> solicitudes, Entrada entrada) {
        CustodiaEntrada entradaCustodia = new CustodiaEntrada();
        CustodiaSalida salidaCustodia = new CustodiaSalida();
        for (SolicitudProceso sol : solicitudes) {
            if (ESTADO_RECHAZADO.equals(sol.EstadoProceso)) continue;
            MovimientoCustodia mov = new MovimientoCustodia();
            mov.IdGenerarMovimientoCustodia = sol.IdSolicitudAporteRescate;
            mov.FechaOperacion = sol.FechaCertificacion;
            mov.IdCliente = sol.IdCliente;
            mov.IdPortafolio = sol.IdPortafolio;
            mov.IdTipoProducto = TIPO_PRODUCTO_CUSTODIA;
            mov.IdConcepto = sol.IdConcepto;
            mov.IdTipoOperacion = TIPO_SOLICITUD_APORTE.equals(sol.TipoSolicitud) ? TIPO_OPERACION_APORTE_FONDO_MUTUO : TIPO_OPERACION_RESCATE_FONDO_MUTUO;
            mov.IdOperacion = sol.IdSolicitudAporteRescate;
            mov.IdTipoComprobante = TIPO_COMPROBANTE_CUSTODIA;
            mov.FolioComprobante = sol.IdSolicitudAporteRescate;
            mov.FechaComprobante = sol.FechaCertificacion;
            mov.IdBolsa = BCS;
            mov.IdAgente = sol.IdEmpleadoEjecutivo;
            mov.FechaLiquidacion = sol.FechaCertificacion;
            mov.IdUsuario = entrada.p_IdUsuario;
            mov.TipoCarteraNormativa = sol.TipoCarteraNormativa;
            mov.FechaCurse = sol.FechaAceptacion;
            mov.TipoFactorAjuste = UF;
            mov.ValorFactorAjusteaFechaCurse = sol.ValorUFFechaInversion;
            mov.CostoEnFactorAjuste = sol.CostoUF;
            mov.IdInstrumento = sol.IdInstrumento;
            mov.IdEmisor = sol.IdEmisor;
            mov.IdMonedaInstrumento = sol.IdMonedaFondo;
            mov.IdBoveda = COR;
            mov.Cantidad = sol.NumeroCuotas;
            mov.ValorRescate = BigDecimal.ZERO;
            mov.PrecioCompra = sol.ValorCuota;
            mov.MontoCompra = sol.MontoMonedaSistema;
            mov.MontoCompraMonedaInstrumento = sol.Monto;
            mov.IdTipoCanalIngreso = "";
            mov.IdRegimenTributario = sol.IdRegimenTributario;
            // D-FEC-6: the sentinel text 0001-01-01 is compared with the text the JVM gives the date (the legacy and this code format it the same way)
            mov.FechaOrigenCompra = APORTE_REINVERSION.equals(sol.IdTipoMovimientoFondoMutuo) && !"0001-01-01".equals(FechaLegacy.texto(sol.FechaOriginalInversion))
                    ? sol.FechaOriginalInversion
                    : sol.FechaValorCuota;
            entradaCustodia.p_Movimientos.add(mov);
        }

        if (entradaCustodia.p_Movimientos.size() > 0) {
            entradaCustodia.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaCustodia.p_IdUsuario = entrada.p_IdUsuario;
            entradaCustodia.p_IdModulo = entrada.p_IdModulo;
            entradaCustodia.p_simularOperacion = NO;
            entradaCustodia.p_UsaReglasNegocio = SI;
            salidaCustodia = CertificarAporteRescateColaboradores.procesar(custodia, entradaCustodia);
        }
        return salidaCustodia;
    }

    /**
     * The messages of type ERROR of the custody go to the output with the ABSOLUTE value of the id (a message without id is a NullPointerException: D-NOR-9) and reject the FIRST
     * solicitud of the list with that id (also one that was already rejected).
     */
    static void rechazarPorMensajes(Salida salida, List<MensajeSalida> mensajes, List<SolicitudProceso> solicitudes) {
        for (MensajeSalida mje : mensajes) {
            if (MensajeSalida.MENSAJE_ERROR.equals(mje.tipo)) {
                Long id = Math.abs(mje.id);
                salida.P_OUT_MENSAJES.add(new MensajeSalida(id, mje.tipo, mje.codigo, mje.descripcion));
                for (SolicitudProceso sol : solicitudes) {
                    if (id.longValue() == sol.IdSolicitudAporteRescate.longValue()) {
                        sol.EstadoProceso = ESTADO_RECHAZADO;
                        sol.Codigo = mje.codigo;
                        sol.DescripcionCodigo = mje.descripcion;
                        break;
                    }
                }
            }
        }
    }
}
