package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.CambiarEstadoRecaudacion;
import cl.bcs.apv.api.casouso.DevolverPagoRezagos;
import cl.bcs.apv.api.casouso.TraspasarEntreCuentasBancarias;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.CambiarEstadoRecaudacionEntrada;
import cl.bcs.apv.api.dto.cambiarestadorecaudacion.ParametrosRecaudacionEstado;
import cl.bcs.apv.api.dto.devolverpagorezagos.DevolverPagoRezagosEntrada;
import cl.bcs.apv.api.dto.devolverpagorezagos.DevolverPagoRezagosResultado;
import cl.bcs.apv.api.dto.devolverpagorezagos.ParametrosRecaudacion;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasEntrada;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasResultado;
import cl.bcs.apv.application.recaudacion.colabg4.AgregarLogG4;
import cl.bcs.apv.application.recaudacion.colabg4.TesoreriaComprobanteG4;
import cl.bcs.apv.application.recaudacion.colabg4.TesoreriaG4;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code DevolverPagoRezagos} (family C): returns the late payments (rezagos) of clients through a bank transfer DEV, an egress movement and a
 * voucher in Treasury, marks the lines as DEV and logs the change. Legacy {@code cl.bcs.APV.DevolucionPagoRezagos} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DevolverPagoRezagosServicio implements DevolverPagoRezagos {

    private static final String SQL_SELECT_RECAUDACION = SqlLegacy.recurso("ConsultarDetalleRecaudacionADevolver");
    private static final String SQL_SELECT_DATOS_BANCARIOS = SqlLegacy.recurso("ConsultarDatosCuentaBancaria");
    private static final String SQL_DATOS_EMPRESA = SqlLegacy.recurso("Bonificacion_DatosEmpresa");
    private static final String SQL_PORTAFOLIO_EMPRESA = SqlLegacy.recurso("Bonificacion_PortafolioEmpresa");
    private static final String SQL_CTA_BCO = SqlLegacy.recurso("Bonificacion_CtaBcoDEV");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final CambiarEstadoRecaudacion cambiarEstadoRecaudacion;
    private final TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias;

    public DevolverPagoRezagosServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores,
            CambiarEstadoRecaudacion cambiarEstadoRecaudacion, TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.diccionario = diccionario;
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

    public static class Recaudacion {
        public Long FolioRecaudacion;
        public Long IdRecaudacionCliente;
        public Long IdRecaudacionEmpleador;
        public Long IdPersonaCliente;
        public Long IdCliente;
        public String RutEmpleador;
        public Date FechaDeposito;
        public BigDecimal RecTotal;
        public Long IdBanco;
        public Long IdContrato;
        public Long IdPortafolio;
        public String IdRegimen;
        public String IdTipoDeposito;
        public String NombreTipoDeposito;
        public Long CuentaCorriente;
        public String CodigoEstado;
        public String Observacion;
    }

    public static class DatosBancarios {
        public Long IdCuentaBancaria = 0L;
        public Long IdBanco = 0L;
        public String IdTipoCuentaBancaria = "";
        public String IdMoneda = "";
        public String NumeroCuentaBancaria = "";
        public String NombreTipoCuentaBancaria = "";
    }

    @Override
    public DevolverPagoRezagosResultado ejecutar(DevolverPagoRezagosEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new DevolverPagoRezagosResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes), new ArrayList<>(), new ArrayList<>());
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(DevolverPagoRezagosEntrada in) throws Exception {
        List<MensajeLegacy> out = new ArrayList<>();
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());

        BigDecimal sumaMontos = BigDecimal.valueOf(0);

        Map<String, Object> paramsSelectRecaudacion = new HashMap<>();
        ArrayList<Long> clientesBuscados = new ArrayList<>();
        for (ParametrosRecaudacion fila : in.parametros()) {
            clientesBuscados.add(fila.idRecaudacionCliente());
        }
        paramsSelectRecaudacion.put("IdRecaudacionClienteGrupo", clientesBuscados);

        Map<String, Object> paramsSelectPortafolio = new HashMap<>();
        paramsSelectPortafolio.put("IdEmpresa", in.idEmpresa());

        List<Recaudacion> recaudacion = sql.consultar(SQL_SELECT_RECAUDACION, paramsSelectRecaudacion, Recaudacion.class);
        sql.consultar(SQL_PORTAFOLIO_EMPRESA, paramsSelectPortafolio, IdPortafolioDefecto.class);
        List<DestinoCtaBco> ctaBco = sql.consultar(SQL_CTA_BCO, paramsSelectPortafolio, DestinoCtaBco.class);

        // bank data: LEGACY DEFECT kept (see README APV-C-248): p_DevolucionIdBanco is used as the IdCuentaBancaria of the person
        Map<String, Object> params = new HashMap<>();
        params.put("IdCuentaBancaria", in.devolucionIdBanco());
        List<DatosBancarios> datosBancarios = sql.consultar(SQL_SELECT_DATOS_BANCARIOS, params, DatosBancarios.class);

        Long idCtaBco = ctaBco.get(0).DestinoCtaBco;

        Map<String, Object> paramsDatosEmpresa = new HashMap<>();
        paramsDatosEmpresa.put("IdEmpresa", in.idEmpresa());
        List<DatosEmpresa> datosEmpresa = sql.consultar(SQL_DATOS_EMPRESA, paramsDatosEmpresa, DatosEmpresa.class);
        datosEmpresa.get(0); // legacy reads the first row: an empty list ends in IndexOutOfBoundsException

        ArrayList<TesoreriaG4.IngresoEgreso_Entrada> paramsIngresoEgreso = new ArrayList<>();
        ArrayList<TesoreriaG4.FormaDePago_Entrada> paramsFormaDePago = new ArrayList<>();
        List<TesoreriaComprobanteG4.FormaDePago_Entrada> paramsComprobanteFormaDePago = new ArrayList<>();
        List<ParametrosRecaudacionEstado> paramsCambioEstado = new ArrayList<>();

        for (Recaudacion fila : recaudacion) {
            TesoreriaG4.IngresoEgreso_Entrada ingresoEgreso = new TesoreriaG4.IngresoEgreso_Entrada();
            TesoreriaG4.FormaDePago_Entrada formaDePago = new TesoreriaG4.FormaDePago_Entrada();
            TesoreriaComprobanteG4.FormaDePago_Entrada comprobanteFormaDePago = new TesoreriaComprobanteG4.FormaDePago_Entrada();

            sumaMontos = sumaMontos.add(fila.RecTotal);

            // Treasury movement built from the recaudacion
            ingresoEgreso.NumeroReferencia = fila.IdRecaudacionCliente;
            ingresoEgreso.AgruparEnComprobante = "N";
            ingresoEgreso.Bilateral = "N";
            ingresoEgreso.FechaComprobante = "0001-01-01";
            ingresoEgreso.FechaLiquidacion = fechaSistema;
            ingresoEgreso.FechaLiquidacionComprobante = "0001-01-01";
            ingresoEgreso.FechaOperacion = fila.FechaDeposito + " 00:00:00";
            ingresoEgreso.FechaParidad = "0001-01-01";
            ingresoEgreso.FolioCartera = 0L;
            ingresoEgreso.GeneraComprobante = "N";
            ingresoEgreso.Glosa = "";
            ingresoEgreso.IdBolsa = "";
            ingresoEgreso.IdCaja = 0L;
            ingresoEgreso.IdClasificacionTipoInstrumento = "";
            ingresoEgreso.IdComprobante = fila.FolioRecaudacion;
            ingresoEgreso.IdConcepto = "DEVRZG";
            ingresoEgreso.IdCondicionLiquidacion = "";
            ingresoEgreso.IdEmpleadoDigitador = 0L;
            ingresoEgreso.IdInstrumento = 0L;
            ingresoEgreso.IdMoneda = "CLP";
            ingresoEgreso.IdMonedaOperacion = "";
            ingresoEgreso.IdOperacion = fila.IdRecaudacionCliente;
            ingresoEgreso.IdPersona = fila.IdPersonaCliente;
            ingresoEgreso.IdPersonaTitularPago = in.devolucionIdPersona();
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
            ingresoEgreso.Monto = fila.RecTotal;
            ingresoEgreso.MontoOperacion = fila.RecTotal;
            ingresoEgreso.MueveCuentaInversion = "S";
            ingresoEgreso.SecuenciaAgrupacion = "";
            ingresoEgreso.SecuenciaRelacionada = null;
            ingresoEgreso.TitularPago = "S";
            ingresoEgreso.ValorParidad = BigDecimal.valueOf(0);
            paramsIngresoEgreso.add(ingresoEgreso);

            // payment form of the movement
            formaDePago.NumeroReferencia = fila.IdRecaudacionCliente;
            formaDePago.NumeroReferenciaFormaDePago = fila.IdRecaudacionCliente;
            formaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
            formaDePago.FechaDocumentoPagoSocio = fechaSistema + " 00:00:00";
            formaDePago.IdBancoSocio = datosBancarios.get(0).IdBanco;
            formaDePago.IdChequera = 0L;
            formaDePago.IdCuentaBancariaCorredora = idCtaBco;
            formaDePago.IdCuentaBancariaSocio = 0L;
            formaDePago.IdMedioDePago = in.devolucionIdMedioPago();
            formaDePago.IdModalidadDePago = "MODDEP";
            formaDePago.IdMoneda = "CLP";
            formaDePago.IdTarjetaBancariaCorredora = 0L;
            formaDePago.IdTarjetaBancariaSocio = 0L;
            formaDePago.IdTipoCuentaBancariaSocio = datosBancarios.get(0).IdTipoCuentaBancaria;
            formaDePago.IdTipoTarjetaBancariaSocio = "";
            formaDePago.Monto = fila.RecTotal;
            formaDePago.NombreCuentaBancariaSocio = "";
            formaDePago.NombreTipoCuentaBancariaSocio = datosBancarios.get(0).NombreTipoCuentaBancaria;
            formaDePago.NombreTipoTarjetaBancariaSocio = "";
            formaDePago.NumeroCuentaBancariaSocio = datosBancarios.get(0).NumeroCuentaBancaria;
            formaDePago.NumeroDocumentoCorredora = 0L;
            formaDePago.NumeroDocumentoSocio = 0L;
            formaDePago.NumeroSerieSocio = "";
            formaDePago.NumeroTarjetaBancariaSocio = "";
            paramsFormaDePago.add(formaDePago);

            // payment form of the voucher
            comprobanteFormaDePago.NumeroReferencia = 1L;
            comprobanteFormaDePago.IdMedioDePago = fila.IdTipoDeposito;
            comprobanteFormaDePago.IdModalidadDePago = "MODDEP";
            comprobanteFormaDePago.IdMoneda = "CLP";
            comprobanteFormaDePago.Monto = fila.RecTotal;
            comprobanteFormaDePago.IdCuentaBancariaCorredora = idCtaBco;
            comprobanteFormaDePago.IdTarjetaBancariaCorredora = 0L;
            comprobanteFormaDePago.IdChequera = 0L;
            comprobanteFormaDePago.NumeroDocumentoCorredora = 0L;
            comprobanteFormaDePago.FechaDocumentoPagoCorredora = "0001-01-01";
            comprobanteFormaDePago.IdCuentaBancariaSocio = 0L;
            comprobanteFormaDePago.IdBancoSocio = datosBancarios.get(0).IdBanco;
            comprobanteFormaDePago.NombreCuentaBancariaSocio = "";
            comprobanteFormaDePago.NumeroCuentaBancariaSocio = datosBancarios.get(0).NumeroCuentaBancaria;
            comprobanteFormaDePago.IdTipoCuentaBancariaSocio = datosBancarios.get(0).IdTipoCuentaBancaria;
            comprobanteFormaDePago.NombreTipoCuentaBancariaSocio = datosBancarios.get(0).NombreTipoCuentaBancaria;
            comprobanteFormaDePago.IdTarjetaBancariaSocio = 0L;
            comprobanteFormaDePago.IdTipoTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NombreTipoTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NumeroTarjetaBancariaSocio = "";
            comprobanteFormaDePago.NumeroSerieSocio = "";
            comprobanteFormaDePago.NumeroDocumentoSocio = 0L;
            comprobanteFormaDePago.FechaDocumentoPagoSocio = fechaSistema;
            paramsComprobanteFormaDePago.add(comprobanteFormaDePago);

            // the deposit state is not updated (folio 0); employer and client lines go to DEV
            paramsCambioEstado.add(new ParametrosRecaudacionEstado(0L, fila.IdRecaudacionEmpleador, fila.IdRecaudacionCliente, "DEV"));
        }

        // LEGACY DEFECT kept (see README APV-C-243..245): the zero-amount check counts an error and returns; call() recomputes the counter from the messages
        if (sumaMontos.compareTo(BigDecimal.ZERO) == 0) {
            out.add(Plataforma.mensaje(diccionario, in.idEmpresa(), 0L, MensajeLegacy.ERROR, "MONTO_TOTAL_RECAUDADO_IGUAL_CERO", null));
            return out;
        }

        // bank transfer between the corporate accounts (option DEV)
        TraspasarEntreCuentasBancariasEntrada traspasoEntrada = new TraspasarEntreCuentasBancariasEntrada("N", in.idEmpresa(), in.idUsuario(), in.idModulo(), "DEV",
                fechaSistema, sumaMontos, false, new ArrayList<>());
        TraspasarEntreCuentasBancariasResultado outTraspaso = traspasarEntreCuentasBancarias.ejecutar(traspasoEntrada);
        List<MensajeLegacy> mensajesTraspaso = aLegacy(outTraspaso.mensajes());
        if (MensajeLegacy.contarErrores(mensajesTraspaso) > 0) {
            agregarErrores(out, mensajesTraspaso);
            return out;
        }

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
        List<MensajeLegacy> mensajesTesoreria = aLegacy(outTesoreria.P_OUT_MENSAJES);
        if (MensajeLegacy.contarErrores(mensajesTesoreria) > 0) {
            agregarErrores(out, mensajesTesoreria);
            return out;
        }

        for (TesoreriaG4.IngresoEgresoGenerado_Salida outIngresoEgreso : outTesoreria.P_OUT_INGRESOEGRESOGENERADO) {
            TesoreriaComprobanteG4.IngresoEgresoGenerar_Entrada comprobanteIngresoEgreso = new TesoreriaComprobanteG4.IngresoEgresoGenerar_Entrada();
            comprobanteIngresoEgreso.NumeroReferencia = 1L;
            comprobanteIngresoEgreso.IdIngresoEgreso = outIngresoEgreso.IdIngresoEgreso;
            comprobanteIngresoEgreso.TipoComprobante = "";
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
        List<MensajeLegacy> mensajesComprobante = aLegacy(outComprobante.P_OUT_MENSAJES);
        if (MensajeLegacy.contarErrores(mensajesComprobante) > 0) {
            agregarErrores(out, mensajesComprobante);
            return out;
        }

        // state change
        cambiarEstadoRecaudacion.ejecutar(new CambiarEstadoRecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), paramsCambioEstado));

        agregarLog(in, fechaSistema, paramsCambioEstado);

        return out;
    }

    /**
     * The legacy adds the ERROR messages of each stage to the output once its running counter is positive (the counter is cumulative: an earlier stage
     * cannot reach here with errors, as it returned).
     */
    private static void agregarErrores(List<MensajeLegacy> out, List<MensajeLegacy> mensajes) {
        for (MensajeLegacy m : mensajes) {
            if (MensajeLegacy.ERROR.equals(m.tipo)) {
                out.add(m);
            }
        }
    }

    private static List<MensajeLegacy> aLegacy(List<?> mensajes) {
        List<MensajeLegacy> r = new ArrayList<>();
        for (Object o : mensajes) {
            if (o instanceof cl.bcs.apv.api.comun.Mensaje m) {
                r.add(MensajeLegacy.de(m));
            } else if (o instanceof TesoreriaG4.Msg m) {
                r.add(new MensajeLegacy(m.id, m.tipo, m.codigo, m.descripcion));
            } else if (o instanceof TesoreriaComprobanteG4.Msg m) {
                r.add(new MensajeLegacy(m.id, m.tipo, m.codigo, m.descripcion));
            } else {
                throw new IllegalStateException("unexpected message type");
            }
        }
        return r;
    }

    private void agregarLog(DevolverPagoRezagosEntrada in, String fechaSistema, List<ParametrosRecaudacionEstado> paramsCambioEstado) {
        AgregarLogG4.InAgregarLog entradaAgregarLog = new AgregarLogG4.InAgregarLog();

        if (!paramsCambioEstado.isEmpty()) {
            AgregarLogG4.InAgregarLog.Log log = new AgregarLogG4.InAgregarLog.Log();
            Long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
            log.IdCabeceraLog = 1L;
            log.IdTrabajo = idTrabajo;
            log.IdTipoLog = "INFORMATIVO";
            log.IdModulo = in.idModulo();
            log.IdUsuario = in.idUsuario();
            log.IdEmpresa = in.idEmpresa();
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = in.idUsuario();
            log.IdTipoRegistro = "DEVOLUCION REZAGOS";
            log.NombreProceso = "Devolución pago rezagos";
            entradaAgregarLog.p_Log.add(log);
            List<Long> listaFolioRecaudacionCliente = new ArrayList<>();

            paramsCambioEstado.stream()
                    .filter(recaudacion -> recaudacion.idRecaudacionCliente() != null && recaudacion.idRecaudacionCliente().compareTo(0L) > 0)
                    .sorted(Comparator.comparingLong(recaudacion -> recaudacion.idRecaudacionCliente()))
                    .forEach(recaudacion -> {
                        if (!listaFolioRecaudacionCliente.contains(recaudacion.idRecaudacionCliente())) {
                            String subRegistro = String.format("Folio Recaudación Cliente %s", recaudacion.idRecaudacionCliente().toString());
                            entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "IdEstado", "", "DEV", subRegistro);
                            listaFolioRecaudacionCliente.add(recaudacion.idRecaudacionCliente());
                        }
                    });

            if (!entradaAgregarLog.p_LogDetalle.isEmpty()) {
                colaboradores.invocar("AgregarLog", Colab.aJson(entradaAgregarLog));
            }
        }
    }
}
