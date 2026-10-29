package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.DevolverBonificacionEntidadRecaudadora;
import cl.bcs.apv.api.casouso.TraspasarEntreCuentasBancarias;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.api.dto.devolverbonificacionentidadrecaudadora.DevolverBonificacionEntidadRecaudadoraEntrada;
import cl.bcs.apv.api.dto.devolverbonificacionentidadrecaudadora.DevolverBonificacionEntidadRecaudadoraResultado;
import cl.bcs.apv.api.dto.devolverbonificacionentidadrecaudadora.ParametrosBonificacion;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasEntrada;
import cl.bcs.apv.application.recaudacion.colabg4.TesoreriaComprobanteG4;
import cl.bcs.apv.application.recaudacion.colabg4.TesoreriaG4;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code DevolverBonificacionEntidadRecaudadora} (family C): returns the bonificacion paid to the collecting entity (bank transfer DBF, egress
 * movement and voucher in Treasury). Legacy {@code cl.bcs.APV.DevolucionBF_EntidadRecaudadora} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DevolverBonificacionEntidadRecaudadoraServicio implements DevolverBonificacionEntidadRecaudadora {

    private static final String SQL_SELECT_BONIFICACION = SqlLegacy.recurso("ConsultarBonificacionADevolver");
    private static final String SQL_DATOS_EMPRESA = SqlLegacy.recurso("Bonificacion_DatosEmpresa");
    private static final String SQL_PORTAFOLIO_EMPRESA = SqlLegacy.recurso("Bonificacion_PortafolioEmpresa");
    private static final String SQL_CTA_BCO = SqlLegacy.recurso("Bonificacion_CtaBcoDBF");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;
    private final TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias;

    public DevolverBonificacionEntidadRecaudadoraServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores,
            CambiarEstadoRecaudacion cambiarEstadoRecaudacion, TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.cambiarEstadoRecaudacion = cambiarEstadoRecaudacion;
        this.traspasarEntreCuentasBancarias = traspasarEntreCuentasBancarias;
    }

    public static class DatosEmpresa {
        public Long IdPersona;
        public Long IdPortafolioDefecto;
    }

    public static class IdPortafolioDefecto {
        public Long IdPortafolioDefecto;
    }

    public static class DestinoCtaBco {
        public Long DestinoCtaBco;
    }

    public static class Bonificacion {
        public Long IdBonificacion;
        public Long IdPersonaEntidad;
        public BigDecimal Monto;
    }

    @Override
    public DevolverBonificacionEntidadRecaudadoraResultado ejecutar(DevolverBonificacionEntidadRecaudadoraEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new DevolverBonificacionEntidadRecaudadoraResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes),
                    new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(DevolverBonificacionEntidadRecaudadoraEntrada in) throws Exception {
        List<MensajeLegacy> out = new ArrayList<>();
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());

        long contador = 0L;
        BigDecimal sumaMontos = BigDecimal.valueOf(0);

        Map<String, Object> paramsSelectBonificacion = new HashMap<>();
        ArrayList<Long> bonificacionBuscada = new ArrayList<>();
        for (ParametrosBonificacion fila : in.parametros()) {
            bonificacionBuscada.add(fila.idBonificacion());
        }
        paramsSelectBonificacion.put("IdBonificacionGrupo", bonificacionBuscada);
        paramsSelectBonificacion.put("IdEmpresa", in.idEmpresa());

        Map<String, Object> paramsSelectPortafolio = new HashMap<>();
        paramsSelectPortafolio.put("IdEmpresa", in.idEmpresa());

        List<Bonificacion> bonificacion = sql.consultar(SQL_SELECT_BONIFICACION, paramsSelectBonificacion, Bonificacion.class);
        sql.consultar(SQL_PORTAFOLIO_EMPRESA, paramsSelectPortafolio, IdPortafolioDefecto.class);
        List<DestinoCtaBco> ctaBco = sql.consultar(SQL_CTA_BCO, paramsSelectPortafolio, DestinoCtaBco.class);

        Long idCtaBco = ctaBco.get(0).DestinoCtaBco;

        Map<String, Object> paramsDatosEmpresa = new HashMap<>();
        paramsDatosEmpresa.put("IdEmpresa", in.idEmpresa());
        List<DatosEmpresa> datosEmpresa = sql.consultar(SQL_DATOS_EMPRESA, paramsDatosEmpresa, DatosEmpresa.class);
        datosEmpresa.get(0); // legacy reads IdPortafolioDefecto/IdPersona of the first row: an empty list ends in IndexOutOfBoundsException

        ArrayList<TesoreriaG4.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<>();
        ArrayList<TesoreriaG4.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<>();
        List<TesoreriaComprobanteG4.FormaDePago_Entrada> paramsComprobanteFormaDePago = new ArrayList<>();
        List<ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<>();

        for (Bonificacion fila : bonificacion) {
            contador++;
            TesoreriaG4.IngresoEgreso_Entrada ingresoEgreso = new TesoreriaG4.IngresoEgreso_Entrada();
            TesoreriaG4.FormaDePago_Entrada formaDePago = new TesoreriaG4.FormaDePago_Entrada();
            TesoreriaComprobanteG4.FormaDePago_Entrada comprobanteFormaDePago = new TesoreriaComprobanteG4.FormaDePago_Entrada();

            sumaMontos = sumaMontos.add(fila.Monto);

            // Treasury movement built from the bonificacion
            ingresoEgreso.NumeroReferencia = contador;
            ingresoEgreso.AgruparEnComprobante = "N";
            ingresoEgreso.Bilateral = "N";
            ingresoEgreso.FechaComprobante = "0001-01-01";
            ingresoEgreso.FechaLiquidacion = null;
            ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
            ingresoEgreso.FechaOperacion = null;
            ingresoEgreso.FechaParidad = "0001-01-01";
            ingresoEgreso.FolioCartera = 0L;
            ingresoEgreso.GeneraComprobante = "N";
            ingresoEgreso.Glosa = "";
            ingresoEgreso.IdBolsa = "";
            ingresoEgreso.IdCaja = 0L;
            ingresoEgreso.IdClasificacionTipoInstrumento = "";
            ingresoEgreso.IdComprobante = 0L;
            ingresoEgreso.IdConcepto = "EGRBON";
            ingresoEgreso.IdCondicionLiquidacion = "";
            ingresoEgreso.IdEmpleadoDigitador = 0L;
            ingresoEgreso.IdInstrumento = 0L;
            ingresoEgreso.IdMoneda = "CLP";
            ingresoEgreso.IdMonedaOperacion = "";
            ingresoEgreso.IdOperacion = 0L;
            ingresoEgreso.IdPersona = fila.IdPersonaEntidad;
            ingresoEgreso.IdPersonaTitularPago = fila.IdPersonaEntidad;
            ingresoEgreso.IdPortafolio = 0L;
            ingresoEgreso.IdPortafolioTitularPago = 0L;
            ingresoEgreso.IdSucursalEmpleadoDigitador = 0L;
            ingresoEgreso.IdTipoComprobante = "APV";
            ingresoEgreso.IdTipoInstrumento = "";
            ingresoEgreso.IdTipoMovimiento = "MOVEGR";
            ingresoEgreso.IdTipoOperacion = "EGRTES";
            ingresoEgreso.IdTipoPersona = "PER";
            ingresoEgreso.IdTipoPersonaTitularPago = "PER";
            ingresoEgreso.IdTipoProducto = "";
            ingresoEgreso.IdTipoReajuste = "";
            ingresoEgreso.Monto = fila.Monto;
            ingresoEgreso.MontoOperacion = fila.Monto;
            ingresoEgreso.MueveCuentaInversion = "S";
            ingresoEgreso.SecuenciaAgrupacion = "";
            ingresoEgreso.SecuenciaRelacionada = null;
            ingresoEgreso.TitularPago = "S";
            ingresoEgreso.ValorParidad = BigDecimal.valueOf(0);
            paramsIngresoEgreso.add(ingresoEgreso);

            // payment form of the movement
            formaDePago.NumeroReferencia = contador;
            formaDePago.NumeroReferenciaFormaDePago = contador;
            formaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
            formaDePago.FechaDocumentoPagoSocio = fechaSistema + " 00:00:00";
            formaDePago.IdBancoSocio = 0L;
            formaDePago.IdChequera = 0L;
            formaDePago.IdCuentaBancariaCorredora = idCtaBco;
            formaDePago.IdCuentaBancariaSocio = 0L;
            formaDePago.IdMedioDePago = "TRA";
            formaDePago.IdModalidadDePago = "MODDEP";
            formaDePago.IdMoneda = "CLP";
            formaDePago.IdTarjetaBancariaCorredora = 0L;
            formaDePago.IdTarjetaBancariaSocio = 0L;
            formaDePago.IdTipoCuentaBancariaSocio = "";
            formaDePago.IdTipoTarjetaBancariaSocio = "";
            formaDePago.Monto = fila.Monto;
            formaDePago.NombreCuentaBancariaSocio = "";
            formaDePago.NombreTipoCuentaBancariaSocio = "";
            formaDePago.NombreTipoTarjetaBancariaSocio = "";
            formaDePago.NumeroCuentaBancariaSocio = "";
            formaDePago.NumeroDocumentoCorredora = 0L;
            formaDePago.NumeroDocumentoSocio = 0L;
            formaDePago.NumeroSerieSocio = "";
            formaDePago.NumeroTarjetaBancariaSocio = "";
            paramsFormaDePago.add(formaDePago);

            // payment form of the voucher
            comprobanteFormaDePago.NumeroReferencia = 1L;
            comprobanteFormaDePago.IdMedioDePago = "TRA";
            comprobanteFormaDePago.IdModalidadDePago = "MODDEP";
            comprobanteFormaDePago.IdMoneda = "CLP";
            comprobanteFormaDePago.Monto = fila.Monto;
            comprobanteFormaDePago.IdCuentaBancariaCorredora = idCtaBco;
            comprobanteFormaDePago.IdTarjetaBancariaCorredora = 0L;
            comprobanteFormaDePago.IdChequera = 0L;
            comprobanteFormaDePago.NumeroDocumentoCorredora = 0L;
            comprobanteFormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
            comprobanteFormaDePago.IdCuentaBancariaSocio = 0L;
            comprobanteFormaDePago.IdBancoSocio = 0L;
            comprobanteFormaDePago.NombreCuentaBancariaSocio = "";
            comprobanteFormaDePago.NumeroCuentaBancariaSocio = "";
            comprobanteFormaDePago.IdTipoCuentaBancariaSocio = "";
            comprobanteFormaDePago.NombreTipoCuentaBancariaSocio = "";
            comprobanteFormaDePago.IdTarjetaBancariaSocio = 0L;
            comprobanteFormaDePago.IdTipoTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NombreTipoTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NumeroTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NumeroSerieSocio = "";
            comprobanteFormaDePago.NumeroDocumentoSocio = 0L;
            comprobanteFormaDePago.FechaDocumentoPagoSocio = fechaSistema;
            paramsComprobanteFormaDePago.add(comprobanteFormaDePago);
        }

        // bank transfer between the corporate accounts (option DBF)
        TraspasarEntreCuentasBancariasEntrada traspasoEntrada = new TraspasarEntreCuentasBancariasEntrada("N", in.idEmpresa(), in.idUsuario(), in.idModulo(), "DBF",
                fechaSistema, sumaMontos, false, new ArrayList<>());
        // LEGACY DEFECT kept (see README APV-C-264, 265): with no bonificacion the zero amount is still sent, and the answer (errors included) of the transfer is never looked at
        traspasarEntreCuentasBancarias.ejecutar(traspasoEntrada);

        // LEGACY DEFECT kept (see README APV-C-269): the legacy commits here, before the Treasury calls; a later exception does not undo the transfer
        sql.commit();

        TesoreriaG4.CrearIngresoEgreso_Entrada paramsIngresoEgresoEntrada = new TesoreriaG4.CrearIngresoEgreso_Entrada();
        paramsIngresoEgresoEntrada.p_IdEmpresa = in.idEmpresa();
        paramsIngresoEgresoEntrada.p_IdUsuario = in.idUsuario();
        paramsIngresoEgresoEntrada.p_IdModulo = in.idModulo();
        paramsIngresoEgresoEntrada.p_SimularOperacion = "N";
        paramsIngresoEgresoEntrada.p_UsaReglasNegocio = "S";
        paramsIngresoEgresoEntrada.p_EsIngresoAutomatico = "N";
        paramsIngresoEgresoEntrada.p_FormaDePago = paramsFormaDePago;
        paramsIngresoEgresoEntrada.p_IngresoEgresoGenerar = paramsIngresoEgreso;

        List<TesoreriaComprobanteG4.IngresoEgresoGenerar_Entrada> paramsComprobanteIngresoEgreso = new ArrayList<>();
        TesoreriaG4.CrearIngresoEgreso_Salida outTesoreria = Colab.de(colaboradores.invocar("CrearIngresoEgreso", Colab.aJson(paramsIngresoEgresoEntrada)),
                TesoreriaG4.CrearIngresoEgreso_Salida.class);

        for (TesoreriaG4.IngresoEgresoGenerado_Salida outIngresoEgreso : outTesoreria.P_OUT_INGRESOEGRESOGENERADO) {
            TesoreriaComprobanteG4.IngresoEgresoGenerar_Entrada comprobanteIngresoEgreso = new TesoreriaComprobanteG4.IngresoEgresoGenerar_Entrada();
            comprobanteIngresoEgreso.NumeroReferencia = 1L;
            comprobanteIngresoEgreso.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
            comprobanteIngresoEgreso.TipoComprobante = "PAREGR";
            paramsComprobanteIngresoEgreso.add(comprobanteIngresoEgreso);
        }

        // voucher
        TesoreriaComprobanteG4.CrearComprobante_Entrada paramsComprobanteEntrada = new TesoreriaComprobanteG4.CrearComprobante_Entrada();
        List<TesoreriaComprobanteG4.ComprobanteGenerar_Entrada> paramsComprobanteGenerar = new ArrayList<>();
        paramsComprobanteEntrada.p_IdEmpresa = in.idEmpresa();
        paramsComprobanteEntrada.p_IdUsuario = in.idUsuario();
        paramsComprobanteEntrada.p_IdModulo = in.idModulo();
        paramsComprobanteEntrada.p_UsaReglasNegocio = "S";
        paramsComprobanteEntrada.p_FechaMovimiento = fechaSistema;
        paramsComprobanteEntrada.p_IdCaja = 1L;
        paramsComprobanteEntrada.p_GenerarComprobanteDesdeMovimientos = "S";
        paramsComprobanteEntrada.p_ComprobanteGenerar = paramsComprobanteGenerar;
        paramsComprobanteEntrada.p_IngresoEgresoGenerar = paramsComprobanteIngresoEgreso;
        paramsComprobanteEntrada.p_FormaDePago = paramsComprobanteFormaDePago;

        TesoreriaComprobanteG4.CrearComprobante_Salida outComprobante = Colab.de(colaboradores.invocar("CrearComprobante", Colab.aJson(paramsComprobanteEntrada)),
                TesoreriaComprobanteG4.CrearComprobante_Salida.class);

        // LEGACY DEFECT kept (see README APV-C-265): the errors of the voucher are never reported (the liquidation that used them is commented out in the legacy)
        if (contarErrores(outComprobante.P_OUT_MENSAJES) == 0) {
            for (int i = 0; i < outComprobante.P_OUT_RESULTADOPROCESO.size(); i++) {
                // legacy reads Campo (NullPointerException when null) to build a ComprobanteLiquidar_Entrada that is never used
                outComprobante.P_OUT_RESULTADOPROCESO.get(i).Campo.equals("IdComprobante");
            }
        }

        // state change: the legacy list is always empty (the code that filled it is commented out); the call still happens
        cambiarEstadoRecaudacion.ejecutar(new CambiarEstadoRecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), paramsCambioEstado));

        return out;
    }

    private static long contarErrores(List<TesoreriaComprobanteG4.Msg> mensajes) {
        long c = 0;
        for (TesoreriaComprobanteG4.Msg m : mensajes) {
            if (MensajeLegacy.ERROR.equals(m.tipo)) {
                c++;
            }
        }
        return c;
    }
}
