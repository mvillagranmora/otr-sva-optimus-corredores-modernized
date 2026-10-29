package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.ProcesarDistribucion;
import cl.bcs.apv.api.casouso.TraspasarEntreCuentasBancarias;
import cl.bcs.apv.api.casouso.ValidarContrato;
import cl.bcs.apv.api.dto.procesardistribucion.ParametrosRecaudacion;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionEntrada;
import cl.bcs.apv.api.dto.procesardistribucion.ProcesarDistribucionResultado;
import cl.bcs.apv.api.dto.procesardistribucion.SalidaSolicitudFFMM;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.OrigenRecaudacion;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasEntrada;
import cl.bcs.apv.api.dto.traspasarentrecuentasbancarias.TraspasarEntreCuentasBancariasResultado;
import cl.bcs.apv.api.dto.validarcontrato.DetalleValidacionContratoSAAPV;
import cl.bcs.apv.api.dto.validarcontrato.RecaudacionCliente;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoEntrada;
import cl.bcs.apv.api.dto.validarcontrato.ValidarContratoResultado;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.AgregarLog.InAgregarLog;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CalcularMontoNumeroCuota;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CalcularMontoYCobrosOrden;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Entrada;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.ConsultarInstrumentoPrecioBolsa.ConsultarInstrumentoPrecioBolsa_Salida;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.ConsultarInstrumentoPrecioBolsa.DatosInstrumentosPrecioBolsa_Salida;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.ConsultarInstrumentoPrecioBolsa.Instrumento;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearCargoAbono;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearOrdenes;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearSolicitudAporteRescateNormal.EntradaCrearSolicitudAporteRescateNormal;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearSolicitudAporteRescateNormal.InFormaPagoSpot;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearSolicitudAporteRescateNormal.InSolicitudesAporteRescate;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearSolicitudAporteRescateNormal.ResultadoSolicitud;
import cl.bcs.apv.application.recaudacion.ProcesarDistribucionContratos.CrearSolicitudAporteRescateNormal.SalidaCrearSolicitudAporteRescateNormal;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ProcesarDistribucion} (family C): validates the SAAPV contract of each collected deposit, sends the invalid ones to "rezago", distributes
 * the valid ones among the funds / shares of the contract (cuenta inversion cargo+abono, orders, fondos mutuos), moves the money between bank accounts
 * and writes the final states. Legacy {@code cl.bcs.APV.ProcesarDistribucion} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProcesarDistribucionServicio implements ProcesarDistribucion {

    // constants of the legacy ConstantesFondoMutuo used here
    private static final String SI = "S";
    private static final String NO = "N";
    private static final String TIPO_SOLICITUD_APORTE = "APO";
    private static final String DOLAR = "USD";
    private static final String CALCULO_POR_MONTO = "M";
    private static final String UF = "UF";
    private static final String ORIGEN_MANUAL = "M";
    private static final String CUENTA_INVERSION = "CIN";
    private static final String APV = "APV";
    private static final String SAAPV = "SAAPV";
    private static final String CANAL_AGENTE = "AGE";
    private static final String CANAL_WEB = "WEB";
    private static final String CANAL_OPERADOR = "OPE";
    private static final String CANAL_EJECUTIVO = "EJE";

    private static final String SQL_SELECT_RECAUDACION = SqlLegacy.recurso("ConsultarDetalleRecaudacionADistribuir");
    private static final String SQL_SELECT_DISTRIBUCION_CONTRATO_SIN_FONDO_RESTRINGIDO = SqlLegacy.recurso("ConsultaDistribucionContratoSinFondoRestringido");
    private static final String SQL_SELECT_DISTRIBUCION_DEFECTO = SqlLegacy.recurso("ConsultaDistribucionDefecto");
    private static final String SQL_CAMBIA_ESTADO_RECAUDACION = SqlLegacy.recurso("Deposito_Update");
    private static final String SQL_CAMBIA_ESTADO_EMPLEADOR = SqlLegacy.recurso("DetalleEmpleador_CambiaEstado");
    private static final String SQL_CAMBIA_ESTADO_CLIENTE = SqlLegacy.recurso("DetalleCliente_CambiaEstado");
    private static final String SQL_INSERT_ESTADO_TRANSICION = SqlLegacy.recurso("EstadoTransicion_Insert");
    private static final String SQL_UPDATE_CONTRATO_DISTRIBUCION = SqlLegacy.recurso("ContratoDistribucion_Update");
    private static final String SQL_CAMBIA_ESTADO_CONTRATO_SAAPV = SqlLegacy.recurso("CambiaEstadoContratoSAAPVRecaudacion");
    private static final String SQL_SELECT_CONTRATO_SAAPV = SqlLegacy.recurso("ProcesarDistribucion_SelectContratoSAAPV");
    private static final String SQL_SELECT_SOLICITUDES_FFMM_ASOCIADAS = SqlLegacy.recurso("ProcesarDistribucion_SelectSolicitudesFFMMAsociadasARecaudacionCliente");
    private static final String SQL_SELECT_BLOQUEO_DISTRIBUCION = SqlLegacy.recurso("ProcesarDistribucion_SelectBloqueoDistribucion");
    private static final String SQL_PORTAFOLIO_EMPRESA = SqlLegacy.recurso("ProcesarDistribucion_PortafolioEmpresa");
    private static final String SQL_MONEDA_FORMATO = SqlLegacy.recurso("Formato_ConsultarMonedaFormato");

    // ------------------------------------------------------------------------------------------------ rows (legacy public static classes)

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
        public String CuentaCorriente;
        public String CodigoEstado;
        public String Observacion;
        public String Pago;
        public List<DistribucionContrato> Distribucion;
        public String RutCliente;

        public Recaudacion() {
            this.Distribucion = new ArrayList<>();
        }
    }

    public static class DistribucionContrato {
        public Long IdDistribucionInversion;
        public Long IdContratoSAAPV;
        public String IdTipoInstrumento;
        public Long IdInstrumento;
        public String Nemotecnico;
        public BigDecimal MontoPesos;
        public BigDecimal Porcentaje;
        public String IdEstado;
        public String IdMoneda;
        public String IdMonedaOrigen;

        public DistribucionContrato() {
            IdDistribucionInversion = 0L;
        }
    }

    public static class Recaudacion_CambiaEstado {
        public Long IdRecaudacion;
        public String FolioCartola;
        public Long LineaCartola;
        public Long Sucursal;
        public Date FechaDeposito;
        public String CuentaCorriente;
        public Long MontoDeposito;
        public Long ConceptoRecaudacion;
        public Long Origen;
        public Long Observacion;
        public Long NumeroDocumento;
        public Long IdTipoDeposito;
        public Long IdBanco;
        public Long IdRegimenTributario;
        public Long IdCarga;
        public String IdEstado;
        public Long IdEmpresa;
    }

    public static class DetalleCliente_CambiaEstado {
        public Long IdRecaudacionCliente;
        public String IdEstado;
        public Long IdEmpresa;
    }

    public static class DetalleEmpleador_CambiaEstado {
        public Long IdRecaudacionEmpleador;
        public String IdEstado;
        public Long IdEmpresa;
    }

    public static class CambiaEstadoContratoSAAPVRecaudacion {
        public Long IdContratoSAAPV;
        public String IdEstado;
    }

    public static class ParamsInsertEstadoTransicion {
        public String Tipo;
        public Long IdRegistro;
        public java.sql.Timestamp FechaTransicion;
        public String EstadoAnterior;
        public String EstadoNuevo;
        public String Observacion;
        public Long Usuario;
    }

    public static class UpdateContratoDistribucion {
        public Long IdRecaudacionCliente;
        public Long IdContratoSAAPV;
    }

    public static class ContratoConsultado {
        public Long IdContratoSAAPV;
        public String FolioContratoSAAPV;
        public String Canal;
        public String IdEstado;
    }

    public static class SolicitudesFFMMAsociadas {
        public Long IdRecaudacion;
        public Long IdRecaudacionCliente;
        public Long Cantidad;
    }

    public static class IdPortafolioDefecto {
        public Long IdPortafolioDefecto;
    }

    /** Legacy {@code MonedaFormato.ObjMonedaFormato} (the format table of the platform). */
    public static class ObjMonedaFormato {
        public Long IdMonedaFormato;
        public String IdMoneda;
        public String IdUsoMonedaFormato;
        public String IdTipoInstrumento;
        public String IdTipoOperacion;
        public Integer PrecioDecimales;
        public String PrecioTrucaRedondea;
        public Integer TasaDecimales;
        public String TasaTrucaRedondea;
        public Integer CantidadDecimales;
        public String CantidadTrucaRedondea;
        public Integer MontoDecimales;
        public String MontoTrucaRedondea;
        public Integer TipoCambioDecimales;
        public String TipoCambioTrucaRedondea;
        public Integer ParidadDecimales;
        public String ParidadTrucaRedondea;
        public Integer PuntosDecimales;
        public String PuntosTrucaRedondea;
        public String IdEstado;
    }

    // ------------------------------------------------------------------------------------------------ wiring

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Secuencias secuencias;
    private final Reloj reloj;
    private final Colaboradores colaboradores;
    private final TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias;
    private final ValidarContrato validarContrato;

    public ProcesarDistribucionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores,
            TraspasarEntreCuentasBancarias traspasarEntreCuentasBancarias, ValidarContrato validarContrato) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.secuencias = secuencias;
        this.reloj = reloj;
        this.colaboradores = colaboradores;
        this.traspasarEntreCuentasBancarias = traspasarEntreCuentasBancarias;
        this.validarContrato = validarContrato;
    }

    @Override
    public ProcesarDistribucionResultado ejecutar(ProcesarDistribucionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = new ArrayList<>();
            Long[] contadorErrores = {0L};
            List<SalidaSolicitudFFMM> solicitudesFFMM = new ArrayList<>();
            logica(in, mensajes, contadorErrores, solicitudesFFMM);
            // the legacy call() never recomputes the counter: the value the logic left is the answer
            return new ProcesarDistribucionResultado(MensajeLegacy.aMensajes(mensajes), contadorErrores[0], solicitudesFFMM);
        } catch (SQLException e) {
            throw Sneaky.lanzar(SqlExceptionPlana.de(e));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    // ------------------------------------------------------------------------------------------------ logic

    /** {@code salidaContador[0]} is the legacy {@code salida.P_OUT_ERRORESCONTADOR} (a mutable Long shared across the steps). */
    private void logica(ProcesarDistribucionEntrada in, List<MensajeLegacy> salidaMensajes, Long[] salidaContador, List<SalidaSolicitudFFMM> salidaSolicitudes)
            throws Exception {
        Long contadorFFMM = 1L;
        Long contadorOrden = 1L;
        Long contadorF = 0L;
        Long contadorFFMMSAR = 0L;

        Long time = reloj.ahora().toEpochMilli();
        String idMonedaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.MonedaSistema");
        String FechaSistema = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "PLA.FechaSistema");
        String AbreRegistrosTesoreria = Plataforma.parametro(configuracion, in.idEmpresa(), "PLATAFORMA", "APV.AbreRegistrosTesoreria");

        Long IdPortafolioCorredora = 0L;
        Timestamp FechaHora = new Timestamp(time);
        HashMap<String, Object> ParamsRecaudacion = new HashMap<String, Object>();
        BigDecimal sumaMontosDistribuido = BigDecimal.valueOf(0);
        BigDecimal sumaMontosRezagado = BigDecimal.valueOf(0);

        List<Long> secuenciasFolio = Colab.secuencia(secuencias, "BSCL.FondoMutuo.BD.Secuencias::FolioContrato", 1L);

        ArrayList<Long> idsRecaudacion = new ArrayList<Long>();
        if (in.parametros().get(0).idRecaudacion() == 0) {
            ParamsRecaudacion.put("IdRecaudacion", 0);
            ParamsRecaudacion.put("IdsRecaudacion", 0);
        } else {
            for (ParametrosRecaudacion fila : in.parametros()) {
                idsRecaudacion.add(fila.idRecaudacion());
            }
            ParamsRecaudacion.put("IdRecaudacion", 1);
            ParamsRecaudacion.put("IdsRecaudacion", idsRecaudacion);
        }

        if ("DIS".equalsIgnoreCase(in.opcion())) {
            bloqueoDistribucion(ParamsRecaudacion);
        }

        List<Recaudacion> _Recaudacion = sql.consultar(SQL_SELECT_RECAUDACION, ParamsRecaudacion, Recaudacion.class);

        List<Long> listaIdRecaudacionCliente = new ArrayList<>();
        List<SolicitudesFFMMAsociadas> listaSolicitudesAsociadas = new ArrayList<>();

        if (!_Recaudacion.isEmpty() && "DIS".equalsIgnoreCase(in.opcion())) {
            listaIdRecaudacionCliente = _Recaudacion.stream().map(recaudacion -> {
                Long idRecaudacionCliente = recaudacion.IdRecaudacionCliente != null ? recaudacion.IdRecaudacionCliente : 0L;
                return idRecaudacionCliente;
            }).distinct().collect(Collectors.toList());

            if (!listaIdRecaudacionCliente.isEmpty()) {
                listaSolicitudesAsociadas = buscarSolicitudesFFMMAsociadas(listaIdRecaudacionCliente);
            }

            if (!listaSolicitudesAsociadas.isEmpty()) {
                validarSolicitudesFFMMAsociadas(in.idEmpresa(), _Recaudacion, listaSolicitudesAsociadas, salidaMensajes);
            }
        }

        // lists of the collaborator inputs
        List<CrearCargoAbono.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<CrearCargoAbono.ObjCargoAbonoAGenerar>();
        List<CrearOrdenes.ObjOrdenesEntrada> ObjOrden = new ArrayList<CrearOrdenes.ObjOrdenesEntrada>();
        List<CalcularMontoYCobrosOrden.Orden_Entrada> ObjCalculoMontoOrden = new ArrayList<CalcularMontoYCobrosOrden.Orden_Entrada>();
        List<OrigenRecaudacion> recadacionATraspasar = new ArrayList<OrigenRecaudacion>();

        List<RecaudacionCliente> paramsValSAAPVRecaudacionCliente = new ArrayList<RecaudacionCliente>();
        HashMap<String, String> valDic = new HashMap<String, String>();

        if (_Recaudacion.size() == 0) {
            salidaMensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), 0L, MensajeLegacy.ERROR, "NO_SE_ENCONTRARON_REGISTROS_PARA_DISTRIBUIR", valDic));
            return;
        }

        // -----------------------------------------------------------------
        // SAAPV validations
        // -----------------------------------------------------------------
        for (Recaudacion fila : _Recaudacion) {
            paramsValSAAPVRecaudacionCliente.add(new RecaudacionCliente(fila.FolioRecaudacion, fila.IdRecaudacionEmpleador, fila.IdRecaudacionCliente,
                    fila.RecTotal, fila.CodigoEstado, null));
        }

        List<cl.bcs.apv.api.dto.validarcontrato.ContratosSAAPV> contrato = new ArrayList<>();
        ValidarContratoEntrada paramsValSAAPV_Entrada = new ValidarContratoEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(),
                in.opcion().equals("RECTRA") ? "RECTRA" : "V", contrato, paramsValSAAPVRecaudacionCliente, in.idsDetalleTraspasoReapertura());

        ValidarContratoResultado OutValSAAPV = validarContrato.ejecutar(paramsValSAAPV_Entrada);

        for (DetalleValidacionContratoSAAPV fila : OutValSAAPV.idsRecaudacion()) {
            if (fila.validacionOK().compareTo("NOK") == 0 && !fila.idEstadoRecaudacionCliente().equals("REZ") && fila.monto().compareTo(BigDecimal.ZERO) > 0) {
                sumaMontosRezagado = sumaMontosRezagado.add(fila.monto());
                recadacionATraspasar.add(new OrigenRecaudacion(fila.idRecaudacionCliente()));
                salidaMensajes.add(new MensajeLegacy(fila.idRecaudacionCliente(), MensajeLegacy.ADVERTENCIA, "CAMBIO_DE_ESTADO_A_REZAGO", "Se cambia estado a rezago"));
            }
        }

        if (sumaMontosRezagado.compareTo(BigDecimal.ZERO) > 0) {
            TraspasarEntreCuentasBancariasEntrada paramsTrasCtaBca_Entrada = new TraspasarEntreCuentasBancariasEntrada(in.simula(), in.idEmpresa(),
                    in.idUsuario(), in.idModulo(), "REZ", FechaSistema.toString(), sumaMontosRezagado, false, recadacionATraspasar);

            TraspasarEntreCuentasBancariasResultado OutTrasCtaBca_Entrada = traspasarEntreCuentasBancarias.ejecutar(paramsTrasCtaBca_Entrada);

            salidaContador[0] += OutTrasCtaBca_Entrada.contadorErrores();

            for (cl.bcs.apv.api.comun.Mensaje filaMensaje : OutTrasCtaBca_Entrada.mensajes()) {
                salidaMensajes.add(new MensajeLegacy(0L, filaMensaje.tipo(), filaMensaje.codigo(), filaMensaje.descripcion()));
            }
        }

        List<ParamsInsertEstadoTransicion> lstEstadoTransicion1 = new ArrayList<ParamsInsertEstadoTransicion>();
        List<DetalleCliente_CambiaEstado> lstEstadoCliente1 = new ArrayList<DetalleCliente_CambiaEstado>();
        ParamsInsertEstadoTransicion objEstadoTransicion1;
        DetalleCliente_CambiaEstado objEstadoCliente1;

        for (DetalleValidacionContratoSAAPV fila : OutValSAAPV.idsRecaudacion()) {
            if (fila.validacionOK().compareTo("NOK") == 0) {
                // state transition
                objEstadoTransicion1 = new ParamsInsertEstadoTransicion();
                objEstadoTransicion1.Tipo = "Recaudacion";
                objEstadoTransicion1.IdRegistro = fila.idRecaudacionCliente();
                objEstadoTransicion1.FechaTransicion = FechaHora;
                objEstadoTransicion1.EstadoAnterior = fila.idEstadoRecaudacionCliente();
                objEstadoTransicion1.EstadoNuevo = "REZ";
                objEstadoTransicion1.Observacion = fila.detalleValidacion();
                objEstadoTransicion1.Usuario = in.idUsuario();
                lstEstadoTransicion1.add(objEstadoTransicion1);

                // output messages of the validations not met
                salidaMensajes.add(new MensajeLegacy(fila.idRecaudacionCliente(), MensajeLegacy.ADVERTENCIA, "DETALLE_VALIDACION_SAAPV", fila.detalleValidacion()));

                // client detail state
                objEstadoCliente1 = new DetalleCliente_CambiaEstado();
                objEstadoCliente1.IdRecaudacionCliente = fila.idRecaudacionCliente();
                objEstadoCliente1.IdEstado = "REZ";
                objEstadoCliente1.IdEmpresa = in.idEmpresa();
                lstEstadoCliente1.add(objEstadoCliente1);

                for (int i = _Recaudacion.size() - 1; i >= 0; i--) {
                    if (_Recaudacion.get(i).IdRecaudacionCliente == fila.idRecaudacionCliente()) {
                        _Recaudacion.remove(i);
                    }
                }
            } else {
                for (int i = _Recaudacion.size() - 1; i >= 0; i--) {
                    if (_Recaudacion.get(i).IdRecaudacionCliente == fila.idRecaudacionCliente()) {
                        _Recaudacion.get(i).IdPortafolio = fila.idPortafolio();
                        _Recaudacion.get(i).IdContrato = fila.idContrato();
                        _Recaudacion.get(i).Pago = fila.pago();
                        _Recaudacion.get(i).IdRegimen = fila.idRegimen();
                    }
                }
            }
        }

        if (lstEstadoTransicion1.size() > 0) {
            sql.lote(SQL_INSERT_ESTADO_TRANSICION, lstEstadoTransicion1);
        }

        if (lstEstadoCliente1.size() > 0) {
            sql.lote(SQL_CAMBIA_ESTADO_CLIENTE, lstEstadoCliente1);
        }

        HashMap<String, Object> ParamsSelectPortafolio = new HashMap<String, Object>();
        ParamsSelectPortafolio.put("IdEmpresa", in.idEmpresa());
        List<IdPortafolioDefecto> _IdPortafolioDefecto = sql.consultar(SQL_PORTAFOLIO_EMPRESA, ParamsSelectPortafolio, IdPortafolioDefecto.class);

        EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new EntradaCrearSolicitudAporteRescateNormal();

        Long contadorCtaInv = 1L;
        IdPortafolioCorredora = _IdPortafolioDefecto.get(0).IdPortafolioDefecto;

        CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvAbono;
        CrearCargoAbono.ObjCargoAbonoAGenerar CtaInvCargo;

        List<Instrumento> lstInstrumentoPrecioBolsa = new ArrayList<Instrumento>();
        Instrumento objInstrumentoPrecioBolsa;

        MonedaFormatoCache formatos = new MonedaFormatoCache();

        for (Recaudacion fila : _Recaudacion) {
            HashMap<String, Object> ParamsConsultaDistribucionContrato = new HashMap<String, Object>();
            Long idContrato = fila.IdContrato == 0 ? in.parametros().get(0).idContratoSAAPV() : fila.IdContrato;
            ParamsConsultaDistribucionContrato.put("IdContrato", idContrato);
            List<ContratoConsultado> _ContratoSAAPV = sql.consultar(SQL_SELECT_CONTRATO_SAAPV, ParamsConsultaDistribucionContrato, ContratoConsultado.class);
            ContratoConsultado contratoSAAPV = _ContratoSAAPV.stream().findFirst().orElse(new ContratoConsultado());

            // from "DistribucionOrigen" S the restricted funds are brought for the contribution
            ParamsConsultaDistribucionContrato.put("DistribucionOrigen", in.distribucionOrigen());

            List<DistribucionContrato> _DistribucionContrato = sql.consultar(SQL_SELECT_DISTRIBUCION_CONTRATO_SIN_FONDO_RESTRINGIDO,
                    ParamsConsultaDistribucionContrato, DistribucionContrato.class);

            if (_DistribucionContrato.size() == 0 && "N".equals(in.soloAporteCuentaInversion())) {
                HashMap<String, Object> ParamsConsultaDistribucionDefecto = new HashMap<String, Object>();
                _DistribucionContrato = sql.consultar(SQL_SELECT_DISTRIBUCION_DEFECTO, ParamsConsultaDistribucionDefecto, DistribucionContrato.class);
            }

            fila.Distribucion = _DistribucionContrato;

            CtaInvAbono = new CrearCargoAbono.ObjCargoAbonoAGenerar();
            CtaInvCargo = new CrearCargoAbono.ObjCargoAbonoAGenerar();

            // sum of amounts for later use
            sumaMontosDistribuido = sumaMontosDistribuido.add(fila.RecTotal);

            // a cargo or abono is created to move the cuenta inversion
            if ("N".equals(in.soloAporteSAAPV())) {

                CtaInvAbono.NumeroCargoAbono = contadorCtaInv;
                CtaInvAbono.IdPortafolio = fila.IdPortafolio == 0 ? in.parametros().get(0).idPortafolio() : fila.IdPortafolio;
                CtaInvAbono.IdMoneda = "CLP";
                CtaInvAbono.IdTipoMovimiento = "MOVABN";
                CtaInvAbono.IdConcepto = fila.Distribucion.size() > 0 ? "ABOAPV" : "ACIAPV";
                CtaInvAbono.Descripcion = "APV";
                CtaInvAbono.IdCondicionDeLiquidacion = "T0";
                CtaInvAbono.FechaCondicionDeLiquidacion = FechaSistema.toString();
                CtaInvAbono.Monto = fila.RecTotal;
                CtaInvAbono.IdEjecutivo = 0L;
                CtaInvAbono.IdSucursal = 0L;
                CtaInvAbono.IdTipoProducto = "";
                CtaInvAbono.IdTipoOperacion = "";
                CtaInvAbono.IdOperacion = fila.IdRecaudacionCliente;
                CtaInvAbono.FechaOperacion = "0001-01-01";
                CtaInvAbono.FechaLiquidacionOperacion = "0001-01-01";
                CtaInvAbono.FechaVencimientoOperacion = null;
                CtaInvAbono.IdMonedaOperacion = "";
                CtaInvAbono.MontoOperacion = null;
                CtaInvAbono.TipoCambioOperacion = BigDecimal.valueOf(0L);
                CtaInvAbono.IdContraparte = null;
                CtaInvAbono.IdTipoComprobante = "APV";
                CtaInvAbono.IdComprobante = contratoSAAPV.IdContratoSAAPV;
                CtaInvAbono.FolioComprobante = contratoSAAPV.FolioContratoSAAPV;
                CtaInvAbono.IdComprobanteDetalle = null;
                CtaInvAbono.IdComprobanteFormaDePago = null;
                CtaInvAbono.IdTipoMovimientoAsociado = null;
                CtaInvAbono.IdMovimientoAsociado = null;
                CtaInvAbono.FechaComprobante = null;
                CtaInvAbono.IdPersonaPagoCobro = null;
                CtaInvAbono.IdInstrumento = null;
                CtaInvAbono.IdTipoInstrumento = null;
                CtaInvAbono.Nemo = null;
                CtaInvAbono.EsIndicadorBilateral = "N";
                CtaInvAbono.EsCargoAbonoPorTraspaso = "N";
                CtaInvAbono.IdPortafolioTraspaso = null;
                CtaInvAbono.GeneroContraparteEnTesoreria = "N";
                CtaInvAbono.CertificadoAutomatico = "S";
                paramsCargoAbono.add(CtaInvAbono);
                contadorCtaInv++;

                CtaInvCargo.NumeroCargoAbono = contadorCtaInv;
                CtaInvCargo.IdPortafolio = IdPortafolioCorredora;
                CtaInvCargo.IdMoneda = "CLP";
                CtaInvCargo.IdTipoMovimiento = "MOVCAR";
                CtaInvCargo.IdConcepto = "CARAPV";
                CtaInvCargo.Descripcion = "APV";
                CtaInvCargo.IdCondicionDeLiquidacion = "T0";
                CtaInvCargo.FechaCondicionDeLiquidacion = FechaSistema.toString();
                CtaInvCargo.Monto = fila.RecTotal;
                CtaInvCargo.IdEjecutivo = 0L;
                CtaInvCargo.IdSucursal = 0L;
                CtaInvCargo.IdTipoProducto = "";
                CtaInvCargo.IdTipoOperacion = "";
                CtaInvCargo.IdOperacion = fila.IdRecaudacionCliente;
                CtaInvCargo.FechaOperacion = "0001-01-01";
                CtaInvCargo.FechaLiquidacionOperacion = "0001-01-01";
                CtaInvCargo.FechaVencimientoOperacion = null;
                CtaInvCargo.IdMonedaOperacion = "";
                CtaInvCargo.MontoOperacion = null;
                CtaInvCargo.TipoCambioOperacion = BigDecimal.valueOf(0L);
                CtaInvCargo.IdContraparte = null;
                CtaInvCargo.IdTipoComprobante = null;
                CtaInvCargo.IdComprobante = null;
                CtaInvCargo.FolioComprobante = null;
                CtaInvCargo.IdComprobanteDetalle = null;
                CtaInvCargo.IdComprobanteFormaDePago = null;
                CtaInvCargo.IdTipoMovimientoAsociado = null;
                CtaInvCargo.IdMovimientoAsociado = null;
                CtaInvCargo.FechaComprobante = null;
                CtaInvCargo.IdPersonaPagoCobro = null;
                CtaInvCargo.IdInstrumento = null;
                CtaInvCargo.IdTipoInstrumento = null;
                CtaInvCargo.Nemo = null;
                CtaInvCargo.EsIndicadorBilateral = "N";
                CtaInvCargo.EsCargoAbonoPorTraspaso = "N";
                CtaInvCargo.IdPortafolioTraspaso = null;
                CtaInvCargo.GeneroContraparteEnTesoreria = "N";
                CtaInvCargo.CertificadoAutomatico = "S";
                paramsCargoAbono.add(CtaInvCargo);
                contadorCtaInv++;
            }

            HashMap<Long, BigDecimal> hashMontoMonedaPago = new HashMap<Long, BigDecimal>();

            BigDecimal montoTotal = fila.RecTotal;
            BigDecimal suma = BigDecimal.ZERO;
            for (DistribucionContrato filaDis : _DistribucionContrato) {
                // distribute in the hash
                BigDecimal montoDistribucion = montoTotal.multiply(filaDis.Porcentaje).divide(new BigDecimal("100"), 20, RoundingMode.HALF_UP).setScale(0, RoundingMode.DOWN);
                montoDistribucion = formatos.formatearMonto(montoDistribucion, filaDis.IdMoneda);
                hashMontoMonedaPago.put(contadorFFMM, montoDistribucion);
                suma = suma.add(montoDistribucion);
                contadorFFMM++;
            }

            if (_DistribucionContrato.size() > 0) {
                // a few pesos were left over: they go to the last fund
                if (montoTotal.compareTo(suma) == 1) {
                    BigDecimal diferencia = montoTotal.subtract(suma);
                    BigDecimal ultimo = hashMontoMonedaPago.get(contadorFFMM - 1);
                    hashMontoMonedaPago.put(contadorFFMM - 1, ultimo.add(diferencia));
                }

                if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() == 0) {
                    contadorFFMM = 1L;
                } else {
                    contadorFFMM = contadorFFMMSAR;
                }
            }

            for (DistribucionContrato filaDistribucionContrato : _DistribucionContrato) {

                if (filaDistribucionContrato.IdTipoInstrumento.equals("FFM")) {
                    InSolicitudesAporteRescate s = new InSolicitudesAporteRescate();
                    s.IdInterno = contadorFFMM;
                    s.IdCliente = fila.IdCliente;
                    s.IdPortafolio = fila.IdPortafolio == 0 ? in.parametros().get(0).idPortafolio() : fila.IdPortafolio;
                    s.TipoSolicitud = TIPO_SOLICITUD_APORTE;
                    s.IdSerieFondo = filaDistribucionContrato.Nemotecnico;
                    s.CalculoPorMontoCuota = CALCULO_POR_MONTO;
                    s.EsPorMontoLiquido = NO;
                    s.Monto = hashMontoMonedaPago.get(contadorFFMM);
                    s.NumeroCuotas = BigDecimal.valueOf(0);
                    s.IdMoneda = filaDistribucionContrato.IdMoneda;
                    s.IdMonedaPago = idMonedaSistema;
                    s.IdMedioDePago = CUENTA_INVERSION;
                    s.IdPreferenciaPagoPortafolio = 0L;
                    s.Visado = NO;
                    s.SolicitudBatch = NO;
                    s.OrigenOperacion = ORIGEN_MANUAL;
                    s.FechaOperacion = FechaSistema.toString();
                    s.IdRegimenTributario = fila.IdRegimen == null ? fila.IdRegimen : fila.IdRegimen.toString();
                    s.Glosa = fila.IdRecaudacionCliente.toString();
                    s.IndicadorMLT = NO;
                    s.IdSerieFondoDestino = "";
                    s.IdEmisorDestino = "";
                    s.IdAutorizadoOrigen = 0L;
                    s.Porcentaje = BigDecimal.ZERO;
                    s.FolioContrato = secuenciasFolio.get(0);
                    s.IngresoAutomatico = ("OPT".equalsIgnoreCase(in.canal()) && "WEB".equalsIgnoreCase(in.origen())) ? NO
                            : esAporteSAAPVOrigenWebService(in.canal()) ? NO : "UNI".equalsIgnoreCase(fila.Pago) ? NO : SI;
                    s.TipoContratoOperacion = SAAPV;
                    s.IdOrigenRecaCliente = fila.IdRecaudacionCliente;

                    boolean esDistribucionAPVCorredores = esAporteSAAPVOrigenWebService(in.canal()) == false && "DIS".equals(in.opcion()) && APV.equals(in.idModulo());
                    if (esDistribucionAPVCorredores) {
                        s.IgnorarMontoMinimo = SI;
                    }

                    if (("APV").equalsIgnoreCase(in.idModulo()) && ("RECTRA").equalsIgnoreCase(in.opcion())) {
                        s.EsTraspaso = SI;
                    }
                    if (_ContratoSAAPV.size() > 0) {
                        s.Canal = _ContratoSAAPV.get(0).Canal;
                        s.FolioContratoOperacion = _ContratoSAAPV.get(0).FolioContratoSAAPV;
                    } else {
                        s.Canal = "";
                        s.FolioContratoOperacion = "";
                    }

                    if (DOLAR.equals(s.IdMoneda)) {

                        List<InFormaPagoSpot> p_FormaPagoSpot = new ArrayList<>();
                        contadorF = contadorF + 1;

                        InFormaPagoSpot FormaPagoSpot = new InFormaPagoSpot();
                        FormaPagoSpot.IdInternoPagoOperacion = contadorF;
                        FormaPagoSpot.IdComprobantePagoOperacion = 0L;
                        FormaPagoSpot.IdTipoMovimiento = "MOVEGR";
                        FormaPagoSpot.IdFormaDePago = "VENSPT_CIN_USD_0";
                        FormaPagoSpot.IdOperacion = contadorFFMM;
                        FormaPagoSpot.RecibePagaPortafolio = "P";
                        FormaPagoSpot.IdMoneda = s.IdMoneda;
                        FormaPagoSpot.IdMonedaLiquidacion = s.IdMoneda;
                        FormaPagoSpot.MontoFormaDePago = BigDecimal.ZERO; // later (CalcularMontoNumeroCuota) it is replaced with the right amount
                        FormaPagoSpot.AfectarTesoreria = "N";
                        p_FormaPagoSpot.add(FormaPagoSpot);

                        contadorF = contadorF + 1;

                        InFormaPagoSpot FormaPagoSpot2 = new InFormaPagoSpot();
                        FormaPagoSpot2.IdInternoPagoOperacion = contadorF;
                        FormaPagoSpot2.IdComprobantePagoOperacion = 0L;
                        FormaPagoSpot2.IdTipoMovimiento = "MOVING";
                        FormaPagoSpot2.IdFormaDePago = "COMSPT_CIN_CLP_0";
                        FormaPagoSpot2.IdOperacion = contadorFFMM;
                        FormaPagoSpot2.RecibePagaPortafolio = "R";
                        FormaPagoSpot2.IdMoneda = "CLP";
                        FormaPagoSpot2.IdMonedaLiquidacion = "CLP";
                        FormaPagoSpot2.MontoFormaDePago = s.Monto;
                        FormaPagoSpot2.AfectarTesoreria = "N";
                        p_FormaPagoSpot.add(FormaPagoSpot2);

                        s.ForzarSpot = SI;

                        entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
                        entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot.addAll(p_FormaPagoSpot);

                    } else {
                        entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(s);
                    }

                    contadorFFMM = contadorFFMM + 1;
                    contadorFFMMSAR = contadorFFMM;
                } else {

                    CrearOrdenes.ObjOrdenesEntrada Orden = new CrearOrdenes.ObjOrdenesEntrada();

                    objInstrumentoPrecioBolsa = new Instrumento();
                    objInstrumentoPrecioBolsa.IdEmpresa = in.idEmpresa();
                    objInstrumentoPrecioBolsa.IdInstrumento = filaDistribucionContrato.IdInstrumento;
                    objInstrumentoPrecioBolsa.IdBolsa = "BCS";
                    objInstrumentoPrecioBolsa.Fecha = FechaSistema;
                    objInstrumentoPrecioBolsa.IdCondicionDeLiquidacion = "T0";

                    // Instrumento has no equals(): contains() is by identity, so every instrument is added (legacy behaviour)
                    if (!lstInstrumentoPrecioBolsa.contains(objInstrumentoPrecioBolsa)) {
                        lstInstrumentoPrecioBolsa.add(objInstrumentoPrecioBolsa);
                    }

                    CalcularMontoYCobrosOrden.Orden_Entrada CalculoMontoOrdenes = new CalcularMontoYCobrosOrden.Orden_Entrada();
                    CalculoMontoOrdenes.IdInterno = contadorOrden;
                    CalculoMontoOrdenes.CantidadOrden = BigDecimal.ZERO;
                    CalculoMontoOrdenes.EsFungible = "N";
                    CalculoMontoOrdenes.IdBolsa = "BCS";
                    CalculoMontoOrdenes.IdCorredorCorresponsal = null;
                    CalculoMontoOrdenes.IdInstrumento = filaDistribucionContrato.IdInstrumento;
                    CalculoMontoOrdenes.IdInstrumentoSubyacente = 0L;
                    CalculoMontoOrdenes.IdMonedaLiquidacionOrden = filaDistribucionContrato.IdMoneda;
                    CalculoMontoOrdenes.IdPortafolio = fila.IdPortafolio;
                    CalculoMontoOrdenes.IdTipoProducto = "INAC";
                    CalculoMontoOrdenes.IdTipoOperacion = "COMACN";
                    CalculoMontoOrdenes.IdTipoCanalIngreso = "AGE";
                    CalculoMontoOrdenes.FechaOperacion = FechaSistema;
                    CalculoMontoOrdenes.MontoOrden = BigDecimal.ZERO;
                    CalculoMontoOrdenes.TasaSimultanea = BigDecimal.ZERO;
                    CalculoMontoOrdenes.TasaRFIF = BigDecimal.ZERO;
                    CalculoMontoOrdenes.TasaVentaCorta = BigDecimal.ZERO;
                    CalculoMontoOrdenes.TipoCambio = BigDecimal.ZERO;
                    CalculoMontoOrdenes.ValorRescateOrden = BigDecimal.ZERO;
                    CalculoMontoOrdenes.PlazoSimultanea = 0L;
                    CalculoMontoOrdenes.PlazoVentaCorta = 0L;
                    CalculoMontoOrdenes.PlazoIF = 0L;
                    CalculoMontoOrdenes.EsAPlazo = "N";
                    CalculoMontoOrdenes.MontoTotal = (fila.RecTotal.multiply(filaDistribucionContrato.Porcentaje).divide(BigDecimal.valueOf(100)));
                    CalculoMontoOrdenes.EsPorMonto = "S";
                    CalculoMontoOrdenes.TasaReporte = BigDecimal.ZERO;
                    CalculoMontoOrdenes.TasaReportado = BigDecimal.ZERO;
                    CalculoMontoOrdenes.PlazoReporte = 0L;
                    CalculoMontoOrdenes.BaseTasa = 0L;
                    ObjCalculoMontoOrden.add(CalculoMontoOrdenes);

                    Orden.IdInterno = contadorOrden;
                    Orden.FechaRecepcion = "0001-01-01 00:00:00";
                    Orden.FechaVigencia = "2018-05-09";
                    Orden.FechaEmision = "0001-01-01";
                    Orden.FechaOperacion = "0001-01-01";
                    Orden.IdCondicionDeLiquidacion = "T0";
                    Orden.IdTipoOperacion = "COMACN";
                    Orden.IdTipoVigencia = "SH";
                    Orden.Glosa = fila.IdRecaudacionCliente.toString();
                    Orden.Cantidad = BigDecimal.valueOf(0L);
                    Orden.MontoNeto = (fila.RecTotal.multiply(filaDistribucionContrato.Porcentaje).divide(BigDecimal.valueOf(100)));
                    Orden.ValorRescate = BigDecimal.valueOf(0L);
                    Orden.IdBoveda = "DCV";
                    Orden.IdFormaCustodia = "NOM";
                    Orden.IdPorcentajeVisibilidad = 1L;
                    Orden.PrecioLimite = BigDecimal.valueOf(10000L);
                    Orden.EsFacturaInmediata = "N";
                    Orden.IdTipoPrecio = "M";
                    Orden.IdPortafolio = fila.IdPortafolio;
                    Orden.IdTipoCanalIngreso = "AGE";
                    Orden.IdTipoReajuste = "";
                    Orden.IdEmisor = filaDistribucionContrato.Nemotecnico;
                    Orden.IdBolsa = "BCS";
                    Orden.EspecificaBolsa = "S";
                    Orden.IdMonedaLiquidacionOrden = "CLP";
                    Orden.TipoCambio = null;
                    Orden.IdInstrumento = filaDistribucionContrato.IdInstrumento;
                    Orden.IdInstrumentoSubyacente = 0L;
                    Orden.IdCondicionDeLiquidacionPlazo = "";
                    Orden.TasaVentaCorta = BigDecimal.valueOf(0);
                    Orden.PlazoVentaCorta = 0L;
                    Orden.EsVentaCorta = "N";
                    Orden.TasaSimultanea = BigDecimal.valueOf(0);
                    Orden.PlazoSimultanea = 0L;
                    Orden.PlazoIF = 0L;
                    Orden.TasaRFIF = BigDecimal.valueOf(0);
                    Orden.EsOD = "N";
                    Orden.EsFungible = "N";
                    Orden.IdTipoLibroOrden = "NORMAL";
                    Orden.IdLibroOrdenes = null;
                    Orden.IdCuentaCustodioPortafolio = null;
                    Orden.EsPorMonto = "S";
                    Orden.IdCorredorCorresponsal = null;
                    Orden.IdPersonaAutorizadoDarOrdenes = null;
                    Orden.IdTipoCanalDistribucion = "MAN";
                    Orden.EsOrdenEspecial = "N";
                    Orden.PagoCobroCuentaInversion = "S";
                    Orden.IdPreferenciaPagoPortafolio = fila.IdPortafolio;
                    Orden.BaseTasa = 0L;
                    Orden.TipoInteres = "";
                    Orden.IdMecanismoCaptacion = "OM";

                    ObjOrden.add(Orden);

                    contadorOrden++;
                }
            }
        }

        // the order price is refreshed
        if (lstInstrumentoPrecioBolsa.size() > 0) {
            ConsultarInstrumentoPrecioBolsa_Entrada entradaConsultarInstrumentoPrecioBolsa = new ConsultarInstrumentoPrecioBolsa_Entrada();
            entradaConsultarInstrumentoPrecioBolsa.p_IdEmpresa = in.idEmpresa();
            entradaConsultarInstrumentoPrecioBolsa.p_IdUsuario = in.idUsuario();
            entradaConsultarInstrumentoPrecioBolsa.p_Instrumentos = lstInstrumentoPrecioBolsa;

            ConsultarInstrumentoPrecioBolsa_Salida resultadoPrecioInstrumento = Colab.de(
                    colaboradores.invocar("ConsultarInstrumentoPrecioBolsa", Colab.aJson(entradaConsultarInstrumentoPrecioBolsa)),
                    ConsultarInstrumentoPrecioBolsa_Salida.class);

            if (resultadoPrecioInstrumento.P_OUT_INSTRUMENTO.size() > 0) {
                HashMap<Long, DatosInstrumentosPrecioBolsa_Salida> hashResultadoPrecioInstrumento = lista2Hash(resultadoPrecioInstrumento.P_OUT_INSTRUMENTO,
                        d -> d.IdInstrumento);

                for (CalcularMontoYCobrosOrden.Orden_Entrada valOrden : ObjCalculoMontoOrden) {
                    DatosInstrumentosPrecioBolsa_Salida infoPrecioInstrumento = hashResultadoPrecioInstrumento.get(valOrden.IdInstrumento);
                    valOrden.PrecioOrden = infoPrecioInstrumento.PrecioCierre;
                }
            }
        }

        // ---------------------------------------------------------
        // CUENTA INVERSION
        // ---------------------------------------------------------
        if (paramsCargoAbono.size() > 0) {
            CrearCargoAbono.CrearCargoAbonoEntrada paramsCuentaInversionCargoAbono_Entrada = new CrearCargoAbono.CrearCargoAbonoEntrada();

            paramsCuentaInversionCargoAbono_Entrada.p_IdEmpresa = in.idEmpresa();
            paramsCuentaInversionCargoAbono_Entrada.p_IdUsuario = in.idUsuario();
            paramsCuentaInversionCargoAbono_Entrada.p_IdModulo = in.idModulo();
            paramsCuentaInversionCargoAbono_Entrada.p_simularOperacion = "N";
            paramsCuentaInversionCargoAbono_Entrada.p_UsaReglasNegocio = "S";
            paramsCuentaInversionCargoAbono_Entrada.p_FechaSistema = FechaSistema.toString();
            paramsCuentaInversionCargoAbono_Entrada.p_CargoAbonoAGenerar = paramsCargoAbono;
            paramsCuentaInversionCargoAbono_Entrada.p_EsIngresoManual = "N";

            CrearCargoAbono.CrearCargoAbonoSalida OutCuentaInversion = Colab.de(
                    colaboradores.invocar("CrearCargoAbono", Colab.aJson(paramsCuentaInversionCargoAbono_Entrada)), CrearCargoAbono.CrearCargoAbonoSalida.class);
            List<MensajeLegacy> mensajesErroresCuentaInversion = OutCuentaInversion.P_OUT_MENSAJES.parallelStream()
                    .filter(mensaje -> MensajeLegacy.ERROR.equals(mensaje.tipo)).collect(Collectors.toList());
            salidaMensajes.addAll(mensajesErroresCuentaInversion);
            salidaContador[0] = MensajeLegacy.contarErrores(salidaMensajes);
        }

        // ---------------------------------------------------------
        // ORDERS
        // ---------------------------------------------------------
        if (ObjOrden.size() > 0) {

            CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada paramsCalcularMonto_Entrada = new CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Entrada();
            paramsCalcularMonto_Entrada.p_IdEmpresa = in.idEmpresa();
            paramsCalcularMonto_Entrada.p_IdUsuario = in.idUsuario();
            paramsCalcularMonto_Entrada.p_IdModulo = in.idModulo();
            paramsCalcularMonto_Entrada.p_Ordenes = ObjCalculoMontoOrden;

            CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida OutCalcularmonto = Colab.de(
                    colaboradores.invocar("CalcularMontoYCobrosOrden", Colab.aJson(paramsCalcularMonto_Entrada)),
                    CalcularMontoYCobrosOrden.CalcularMontoYCobrosOrden_Salida.class);

            for (int i = OutCalcularmonto.p_out_Ordenes.size() - 1; i >= 0; i--) {
                for (int j = ObjOrden.size() - 1; j >= 0; j--) {
                    if (OutCalcularmonto.p_out_Ordenes.get(i).IdInterno.toString().equals(ObjOrden.get(j).IdInterno.toString())) {
                        ObjOrden.get(j).Cantidad = OutCalcularmonto.p_out_Ordenes.get(i).CantidadOperacion;
                    }
                }
            }

            CrearOrdenes.CrearOrdenesEntrada paramsOrdenesEntrada = new CrearOrdenes.CrearOrdenesEntrada();

            paramsOrdenesEntrada.p_IdEmpresa = in.idEmpresa();
            paramsOrdenesEntrada.p_IdUsuario = in.idUsuario();
            paramsOrdenesEntrada.p_IdModulo = "APV";
            paramsOrdenesEntrada.p_simularOperacion = "N";
            paramsOrdenesEntrada.p_UsaReglasNegocio = "S";
            paramsOrdenesEntrada.p_Ordenes = ObjOrden;

            CrearOrdenes.CrearOrdenesSalida OutOrdenes = Colab.de(colaboradores.invocar("CrearOrdenes", Colab.aJson(paramsOrdenesEntrada)),
                    CrearOrdenes.CrearOrdenesSalida.class);

            for (MensajeLegacy filaMensajeRV : OutOrdenes.P_OUT_MENSAJES) {

                for (int i = _Recaudacion.size() - 1; i >= 0; i--) {

                    for (int j = ObjOrden.size() - 1; j >= 0; j--) {

                        if (_Recaudacion.get(i).IdRecaudacionCliente.toString().equals(ObjOrden.get(j).Glosa)
                                && ObjOrden.get(j).IdInterno.toString().equals(filaMensajeRV.id.toString())) {

                            // LEGACY DEFECT kept (see README APV-C-41..44): the order built here never gets NemotecnicoSVS, so the observation reads "RV: : <text>"
                            if (_Recaudacion.get(i).Observacion == null) {
                                _Recaudacion.get(i).Observacion = "RV: " + ObjOrden.get(j).NemotecnicoSVS + ": " + filaMensajeRV.descripcion;
                            } else {
                                _Recaudacion.get(i).Observacion = _Recaudacion.get(i).Observacion + "\n" + ObjOrden.get(j).NemotecnicoSVS + ": " + filaMensajeRV.descripcion;
                            }

                            salidaMensajes.add(new MensajeLegacy(_Recaudacion.get(i).IdRecaudacionCliente, filaMensajeRV.tipo, filaMensajeRV.codigo, filaMensajeRV.descripcion));
                        }
                    }
                }
            }
        }

        // ---------------------------------------------------------
        // FONDOS MUTUOS
        // ---------------------------------------------------------
        if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0) {

            try {
                List<CalcularMontoNumeroCuota.Serie> lstMontoNumeroCuotaSerie = new ArrayList<CalcularMontoNumeroCuota.Serie>();
                CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota entradaCalcularMontoNumeroCuota = new CalcularMontoNumeroCuota.EntradaCalcularMontoNumeroCuota();
                CalcularMontoNumeroCuota.Serie objSerie;

                for (InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate) {
                    objSerie = new CalcularMontoNumeroCuota.Serie();
                    objSerie.Fecha = FechaSistema.toString();

                    objSerie.IdInterno = sol.IdInterno;
                    objSerie.IdSerieFondo = sol.IdSerieFondo;
                    objSerie.CalculoPorMontoCuota = sol.CalculoPorMontoCuota;
                    objSerie.Monto = sol.Monto;
                    objSerie.NumeroCuotas = sol.NumeroCuotas;
                    objSerie.IdMonedaFondo = sol.IdMoneda;

                    objSerie.IdMonedaInversion = (DOLAR.equals(sol.IdMoneda)) ? "CLP" : sol.IdMoneda;
                    objSerie.TipoSolicitud = sol.TipoSolicitud;
                    objSerie.TipoFactorAjuste = UF;
                    objSerie.IdEmpresa = in.idEmpresa();
                    lstMontoNumeroCuotaSerie.add(objSerie);
                }

                if (!lstMontoNumeroCuotaSerie.isEmpty()) {
                    entradaCalcularMontoNumeroCuota.p_IdEmpresa = in.idEmpresa();
                    entradaCalcularMontoNumeroCuota.p_Series = lstMontoNumeroCuotaSerie;

                    CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota salidaCalculo = Colab.de(
                            colaboradores.invocar("CalcularMontoNumeroCuota", Colab.aJson(entradaCalcularMontoNumeroCuota)),
                            CalcularMontoNumeroCuota.SalidaCalcularMontoNumeroCuota.class);
                    List<CalcularMontoNumeroCuota.Resultado> resultadoSeriesFFMM = salidaCalculo.P_OUT_RESULTADO;
                    HashMap<Long, CalcularMontoNumeroCuota.Resultado> hashResultadoSeriesFFMM = lista2Hash(resultadoSeriesFFMM, r -> r.IdInterno);

                    for (InSolicitudesAporteRescate sol : entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate) {
                        CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = hashResultadoSeriesFFMM.get(sol.IdInterno);
                        sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;

                        if (DOLAR.equals(sol.IdMoneda)) {
                            sol.Monto = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO : infoResultadoSeriesFFMM.Monto;
                            sol.NumeroCuotas = infoResultadoSeriesFFMM.NumeroCuotas;
                        }
                    }

                    // the amount of the spot payment form is refreshed
                    for (InFormaPagoSpot forPag : entradaCrearSolicitudAporteRescateNormal.p_FormaPagoSpot) {
                        CalcularMontoNumeroCuota.Resultado infoResultadoSeriesFFMM = hashResultadoSeriesFFMM.get(forPag.IdOperacion);

                        if (forPag.IdTipoMovimiento.equalsIgnoreCase("MOVEGR")) {
                            forPag.MontoFormaDePago = infoResultadoSeriesFFMM.Monto == null ? BigDecimal.ZERO : infoResultadoSeriesFFMM.Monto;
                        }
                    }
                }

                entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = in.idEmpresa();
                entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = in.idUsuario();
                entradaCrearSolicitudAporteRescateNormal.p_IdModulo = in.idModulo();

                entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = SI;
                entradaCrearSolicitudAporteRescateNormal.p_IdTipoCanalIngreso = ("".equalsIgnoreCase(in.canal()) ? CANAL_AGENTE : in.canal());
                entradaCrearSolicitudAporteRescateNormal.p_NumeroDividendo = 0L;

                entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso = esAporteSAAPVOrigenWebService(in.canal()) ? "" : "REC"; // the origin is Recaudacion (REC)
                entradaCrearSolicitudAporteRescateNormal.p_PermiteOperarProspectos = in.permiteOperarProspectos();

                // creation of the Solicitud Aporte Rescate Normal of the valid deposits
                if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0) {
                    entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = in.simula().trim().equalsIgnoreCase("S") ? SI : NO;
                    SalidaCrearSolicitudAporteRescateNormal salidaCrearSolicitudAporteRescateNormal = Colab.de(
                            colaboradores.invocar("CrearSolicitudAporteRescateNormal", Colab.aJson(entradaCrearSolicitudAporteRescateNormal)),
                            SalidaCrearSolicitudAporteRescateNormal.class);
                    salidaContador[0] += salidaCrearSolicitudAporteRescateNormal.P_OUT_ERRORESCONTADOR;
                    for (MensajeLegacy mje : salidaCrearSolicitudAporteRescateNormal.P_OUT_MENSAJES) {
                        if (MensajeLegacy.ERROR.equals(mje.tipo)) {
                            salidaMensajes.add(mje);
                        }
                    }

                    if (salidaContador[0] == 0) {
                        for (ResultadoSolicitud resultado : salidaCrearSolicitudAporteRescateNormal.P_OUT_RESULTADO) {
                            salidaSolicitudes.add(new SalidaSolicitudFFMM(resultado.IdSolicitudAporteRescate, resultado.FolioContrato));
                        }
                    }
                }

            } catch (Exception er1) { // to validate errors of the ffmm call
                for (Recaudacion fila : _Recaudacion) {

                    if (fila.Observacion == null) {
                        fila.Observacion = "No se ingresa aporte FFMM: Error al crear Aporte FFMM";
                    } else {
                        fila.Observacion = fila.Observacion + "\n" + "No se ingresa aporte FFMM: Error al crear Aporte FFMM";
                    }
                }
            }
        }

        recadacionATraspasar = new ArrayList<OrigenRecaudacion>();
        for (Recaudacion fila : _Recaudacion) {
            recadacionATraspasar.add(new OrigenRecaudacion(fila.IdRecaudacionCliente));
        }

        if (recadacionATraspasar.size() > 0) {

            TraspasarEntreCuentasBancariasEntrada paramsTrasCtaBca_Entrada = new TraspasarEntreCuentasBancariasEntrada(in.simula(), in.idEmpresa(),
                    in.idUsuario(), in.idModulo(), "DIS", FechaSistema.toString(), sumaMontosDistribuido, esProvenienteDeUnaRecaudacionPorTraspaso(in),
                    recadacionATraspasar);

            TraspasarEntreCuentasBancariasResultado OutTrasCtaBca = traspasarEntreCuentasBancarias.ejecutar(paramsTrasCtaBca_Entrada);
            salidaContador[0] += OutTrasCtaBca.contadorErrores();

            for (cl.bcs.apv.api.comun.Mensaje filaMensaje : OutTrasCtaBca.mensajes()) {
                salidaMensajes.add(new MensajeLegacy(0L, filaMensaje.tipo(), filaMensaje.codigo(), filaMensaje.descripcion()));
            }

            if (salidaContador[0] == 0) {

                // ----------------------------------------------------------
                // FINAL STATE UPDATES
                // -----------------------------------------------------------

                List<ParamsInsertEstadoTransicion> lstEstadoTransicion2 = new ArrayList<ParamsInsertEstadoTransicion>();
                List<UpdateContratoDistribucion> lstContratoDistribucion = new ArrayList<UpdateContratoDistribucion>();
                List<Recaudacion_CambiaEstado> lstEstadoRecaudacion = new ArrayList<Recaudacion_CambiaEstado>();
                List<DetalleEmpleador_CambiaEstado> lstEstadoEmpleador = new ArrayList<DetalleEmpleador_CambiaEstado>();
                List<DetalleCliente_CambiaEstado> lstEstadoCliente = new ArrayList<DetalleCliente_CambiaEstado>();
                List<CambiaEstadoContratoSAAPVRecaudacion> lstEstadoContratoSAAPV = new ArrayList<CambiaEstadoContratoSAAPVRecaudacion>();

                for (Recaudacion fila : _Recaudacion) {
                    if (fila.Distribucion.size() > 0) {
                        // state transition
                        ParamsInsertEstadoTransicion objEstadoTransicion2 = new ParamsInsertEstadoTransicion();
                        objEstadoTransicion2.Tipo = "Recaudacion";
                        objEstadoTransicion2.IdRegistro = fila.IdRecaudacionCliente;
                        objEstadoTransicion2.FechaTransicion = FechaHora;
                        objEstadoTransicion2.EstadoAnterior = fila.CodigoEstado;
                        if (fila.Observacion == null) {
                            objEstadoTransicion2.Observacion = "Distribucion Procesada";
                        } else {
                            objEstadoTransicion2.Observacion = fila.Observacion;
                        }

                        objEstadoTransicion2.EstadoNuevo = "DIS";
                        objEstadoTransicion2.Usuario = in.idUsuario();
                        lstEstadoTransicion2.add(objEstadoTransicion2);

                        // updates the distribution contract
                        UpdateContratoDistribucion objContratoDistribucion = new UpdateContratoDistribucion();
                        objContratoDistribucion.IdRecaudacionCliente = fila.IdRecaudacionCliente;
                        objContratoDistribucion.IdContratoSAAPV = fila.IdContrato;
                        lstContratoDistribucion.add(objContratoDistribucion);

                        // deposit state
                        Recaudacion_CambiaEstado objEstadoRecaudacion = new Recaudacion_CambiaEstado();
                        objEstadoRecaudacion.IdRecaudacion = fila.FolioRecaudacion;
                        objEstadoRecaudacion.IdEstado = "DIS";
                        objEstadoRecaudacion.IdEmpresa = in.idEmpresa();
                        lstEstadoRecaudacion.add(objEstadoRecaudacion);

                        // employer detail state
                        DetalleEmpleador_CambiaEstado objEstadoEmpleador = new DetalleEmpleador_CambiaEstado();
                        objEstadoEmpleador.IdRecaudacionEmpleador = fila.IdRecaudacionEmpleador;
                        objEstadoEmpleador.IdEstado = "DIS";
                        objEstadoEmpleador.IdEmpresa = in.idEmpresa();
                        lstEstadoEmpleador.add(objEstadoEmpleador);

                        // client detail state
                        DetalleCliente_CambiaEstado objEstadoCliente = new DetalleCliente_CambiaEstado();
                        objEstadoCliente.IdRecaudacionCliente = fila.IdRecaudacionCliente;
                        objEstadoCliente.IdEstado = "DIS";
                        objEstadoCliente.IdEmpresa = in.idEmpresa();
                        lstEstadoCliente.add(objEstadoCliente);

                        // ContratoSAAPV state
                        if (fila.Pago != null) {
                            if (fila.Pago.equals("UNI")) {
                                CambiaEstadoContratoSAAPVRecaudacion objEstadoContratoSAAPV = new CambiaEstadoContratoSAAPVRecaudacion();
                                objEstadoContratoSAAPV.IdEstado = "COM";
                                objEstadoContratoSAAPV.IdContratoSAAPV = fila.IdContrato;
                                lstEstadoContratoSAAPV.add(objEstadoContratoSAAPV);
                            }
                        }
                    }
                }

                if (lstEstadoTransicion2.size() > 0) {
                    sql.lote(SQL_INSERT_ESTADO_TRANSICION, lstEstadoTransicion2);
                }

                if (lstContratoDistribucion.size() > 0) {
                    sql.lote(SQL_UPDATE_CONTRATO_DISTRIBUCION, lstContratoDistribucion);
                }

                if (lstEstadoRecaudacion.size() > 0) {
                    sql.lote(SQL_CAMBIA_ESTADO_RECAUDACION, lstEstadoRecaudacion);
                    // log
                    agregarLog(in, FechaSistema, lstEstadoRecaudacion);
                }

                if (lstEstadoEmpleador.size() > 0) {
                    sql.lote(SQL_CAMBIA_ESTADO_EMPLEADOR, lstEstadoEmpleador);
                }
                if (lstEstadoCliente.size() > 0) {
                    sql.lote(SQL_CAMBIA_ESTADO_CLIENTE, lstEstadoCliente);
                }
                if (lstEstadoContratoSAAPV.size() > 0) {
                    sql.lote(SQL_CAMBIA_ESTADO_CONTRATO_SAAPV, lstEstadoContratoSAAPV);
                }

                // OK output messages
                for (Recaudacion fila : _Recaudacion) {
                    salidaMensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), fila.IdRecaudacionCliente, MensajeLegacy.OK, "PROCESO_DISTRIBUCION_COMPLETADO", valDic));
                }
            } else {
                // post-increment: the message id is the counter BEFORE the increment
                Long idMensaje = salidaContador[0]++;
                salidaMensajes.add(Plataforma.mensaje(diccionario, in.idEmpresa(), idMensaje, MensajeLegacy.ERROR, "PROCESO_DISTRIBUCION_CON_ERRORES", valDic));
            }
        }
    }

    private Boolean esProvenienteDeUnaRecaudacionPorTraspaso(ProcesarDistribucionEntrada in) {
        return "RECTRA".equals(in.opcion()) && noEsAporteSAAPVOrigenWebService(in.canal());
    }

    private Boolean noEsAporteSAAPVOrigenWebService(String canal) {
        return !CANAL_WEB.equals(canal) && !CANAL_OPERADOR.equals(canal) && !CANAL_EJECUTIVO.equals(canal);
    }

    private Boolean esAporteSAAPVOrigenWebService(String canal) {
        return CANAL_WEB.equals(canal) || CANAL_OPERADOR.equals(canal) || CANAL_EJECUTIVO.equals(canal);
    }

    private void agregarLog(ProcesarDistribucionEntrada in, String fechaSistema, List<Recaudacion_CambiaEstado> lstEstadoRecaudacion) throws Exception {
        InAgregarLog entradaAgregarLog = new InAgregarLog();

        if (!lstEstadoRecaudacion.isEmpty()) {

            InAgregarLog.Log log = new InAgregarLog.Log();
            Long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
            log.IdCabeceraLog = 1L;
            log.IdTrabajo = idTrabajo;
            log.IdTipoLog = "INFORMATIVO";
            log.IdModulo = in.idModulo();
            log.IdUsuario = in.idUsuario();
            log.IdEmpresa = in.idEmpresa();
            log.FechaSistemaLog = fechaSistema;
            log.IdUsuarioAtendido = in.idUsuario();
            log.IdTipoRegistro = "DISTRIBUCION APV";
            log.NombreProceso = "Procesar Distribucion";
            entradaAgregarLog.p_Log.add(log);
            List<Long> listaIdRecaudacion = new ArrayList<>();

            lstEstadoRecaudacion.stream()
                    .filter(recaudacion -> recaudacion.IdRecaudacion != null && recaudacion.IdRecaudacion.compareTo(0L) > 0)
                    .sorted(Comparator.comparingLong(recaudacion -> recaudacion.IdRecaudacion))
                    .forEach(recaudacion -> {
                        if (!listaIdRecaudacion.contains(recaudacion.IdRecaudacion)) {
                            String subRegistro = String.format("Folio Recaudación %s", recaudacion.IdRecaudacion.toString());
                            entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "IdEstado", "", "DIS", subRegistro);
                            listaIdRecaudacion.add(recaudacion.IdRecaudacion);
                        }
                    });

            if (!entradaAgregarLog.p_LogDetalle.isEmpty()) {
                colaboradores.invocar("AgregarLog", Colab.aJson(entradaAgregarLog));
            }
        }
    }

    private List<SolicitudesFFMMAsociadas> buscarSolicitudesFFMMAsociadas(List<Long> listaIdRecaudacionCliente) throws SQLException {
        HashMap<String, Object> paramsSolicitudesAsociadas = new HashMap<String, Object>();
        paramsSolicitudesAsociadas.put("IdRecaudacionCliente", listaIdRecaudacionCliente);
        return sql.consultar(SQL_SELECT_SOLICITUDES_FFMM_ASOCIADAS, paramsSolicitudesAsociadas, SolicitudesFFMMAsociadas.class);
    }

    private void validarSolicitudesFFMMAsociadas(Long idEmpresa, List<Recaudacion> listaRecaudacion, List<SolicitudesFFMMAsociadas> listaSolicitudesAsociadas,
            List<MensajeLegacy> salidaMensajes) throws Exception {
        HashMap<Long, SolicitudesFFMMAsociadas> hashIdRecaudacionCliente = lista2Hash(listaSolicitudesAsociadas, s -> s.IdRecaudacionCliente);
        if (!listaRecaudacion.isEmpty()) {
            for (int i = listaRecaudacion.size() - 1; i >= 0; i--) {
                Long idRecaudacionCliente = listaRecaudacion.get(i).IdRecaudacionCliente != null ? listaRecaudacion.get(i).IdRecaudacionCliente : 0L;
                SolicitudesFFMMAsociadas solicitudAsociada = hashIdRecaudacionCliente.get(idRecaudacionCliente);
                if (solicitudAsociada != null && solicitudAsociada.Cantidad.compareTo(0L) > 0) {
                    HashMap<String, String> parametrosMensaje = new HashMap<String, String>();
                    String folioRecaudacion = listaRecaudacion.get(i).FolioRecaudacion != null ? listaRecaudacion.get(i).FolioRecaudacion.toString() : "";
                    String rutCliente = listaRecaudacion.get(i).RutCliente != null ? listaRecaudacion.get(i).RutCliente : "";
                    parametrosMensaje.put("IdRecaudacion", folioRecaudacion);
                    parametrosMensaje.put("DocumentoIdentidadCiente", rutCliente);
                    salidaMensajes.add(Plataforma.mensaje(diccionario, idEmpresa, idRecaudacionCliente, MensajeLegacy.ADVERTENCIA,
                            "RECAUDACION_CON_SOLICITUD_FFMM_ASOCIADA", parametrosMensaje));
                    listaRecaudacion.remove(i);
                }
            }
        }
    }

    /** Legacy: a SELECT ... FOR UPDATE whose rows are discarded (it only locks the deposits). */
    private void bloqueoDistribucion(HashMap<String, Object> paramsDistribucion) throws SQLException {
        sql.consultar(SQL_SELECT_BLOQUEO_DISTRIBUCION, paramsDistribucion, Recaudacion.class);
    }

    /**
     * Legacy {@code Utils.List2Hash}: a null list or a repeated key is an exception, an empty list an empty map; a null element is a
     * NullPointerException (the key is read through the element).
     */
    private static <T, K> HashMap<K, T> lista2Hash(List<T> list, Function<T, K> clave) throws Exception {
        HashMap<K, T> hobj = new HashMap<K, T>();
        if (list == null) {
            throw new Exception("List must be not null");
        }
        if (list.size() == 0) {
            return hobj;
        }
        for (T obj : list) {
            K kf = clave.apply(obj);
            if (hobj.containsKey(kf)) {
                throw new Exception("key field is not unique");
            }
            hobj.put(kf, obj);
        }
        return hobj;
    }

    /**
     * Legacy {@code MonedaFormato.formatear(con, valor, MONTO, idMoneda)} (idTipoInstrumento and idTipoOperacion empty): the format table of the
     * platform is read once (on first use) and the amount is rounded or truncated to the decimals of the currency.
     */
    private final class MonedaFormatoCache {
        private List<ObjMonedaFormato> formatos = null;

        BigDecimal formatearMonto(BigDecimal valor, String idMoneda) throws Exception {
            if (formatos == null) {
                formatos = sql.consultar(SQL_MONEDA_FORMATO, new HashMap<String, Object>(), ObjMonedaFormato.class);
            }
            String uso = "GEN";
            String idTipoInstrumento = "";
            String idTipoOperacion = "";
            if (idMoneda == null) {
                throw new Exception("debe especificar moneda");
            }
            if (valor == null) {
                valor = BigDecimal.ZERO;
            }
            ObjMonedaFormato formato = null;
            for (ObjMonedaFormato f : formatos) {
                if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda) && f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)
                        && f.IdTipoOperacion.equalsIgnoreCase(idTipoOperacion)) {
                    formato = f;
                    break;
                }
            }
            if (formato == null) {
                for (ObjMonedaFormato f : formatos) {
                    if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda) && f.IdTipoInstrumento.equalsIgnoreCase(idTipoInstrumento)) {
                        formato = f;
                        break;
                    }
                }
            }
            if (formato == null) {
                for (ObjMonedaFormato f : formatos) {
                    if (f.IdUsoMonedaFormato.equalsIgnoreCase(uso) && f.IdMoneda.equalsIgnoreCase(idMoneda)) {
                        formato = f;
                        break;
                    }
                }
            }
            if (formato == null) {
                throw new Exception("formato no encontrado moneda " + idMoneda + " uso " + uso + " tipo instrumento " + idTipoInstrumento + " tipo operacion " + idTipoOperacion);
            }
            String truncaRedondea = formato.MontoTrucaRedondea;
            Integer decimales = formato.MontoDecimales;
            BigDecimal valorFinal = BigDecimal.ZERO;
            if (truncaRedondea.equalsIgnoreCase("R")) {
                valorFinal = valor.setScale(decimales, RoundingMode.HALF_UP);
            } else if (truncaRedondea.equalsIgnoreCase("T")) {
                valorFinal = valor.setScale(decimales, RoundingMode.FLOOR);
            } else {
                throw new Exception("trunca o redondea invalido " + truncaRedondea + ".");
            }
            return valorFinal;
        }
    }
}
