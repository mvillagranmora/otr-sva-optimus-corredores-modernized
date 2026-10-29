package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.ConsultarCustodiaPortafolio;
import cl.bcs.apv.api.casouso.CrearRecaudacionCliente;
import cl.bcs.apv.api.casouso.CrearRecaudacionDeposito;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioEntrada;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.ConsultarCustodiaPortafolioResultado;
import cl.bcs.apv.api.dto.consultarcustodiaportafolio.Custodia;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.Cliente;
import cl.bcs.apv.api.dto.crearrecaudacioncliente.CrearRecaudacionClienteEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoEntrada;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.CrearRecaudacionDepositoResultado;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Recaudacion;
import cl.bcs.apv.api.dto.crearrecaudaciondeposito.Resultado;
import cl.bcs.apv.api.dto.ejecutarbonificacion.EjecutarBonificacionEntrada;
import cl.bcs.apv.api.dto.ejecutarbonificacion.EjecutarBonificacionResultado;
import cl.bcs.apv.api.dto.ejecutarbonificacion.ParametrosBonificacion;
import cl.bcs.apv.application.recaudacion.colabg4.CalculoOrdenesG4;
import cl.bcs.apv.application.recaudacion.colabg4.CuentaInversionG4;
import cl.bcs.apv.application.recaudacion.colabg4.InstrumentoPrecioG4;
import cl.bcs.apv.application.recaudacion.colabg4.OrdenesG4;
import cl.bcs.apv.application.recaudacion.colabg4.SolicitudFFMMG4;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code EjecutarBonificacion} (family C): executes the bonificacion of each selected row: creates the BF portfolio when missing, distributes the
 * amount over the custody of the client (funds as subscription requests, shares as orders), creates the deposit and client lines of the recaudacion, the
 * cargo/abono in the investment account and marks the bonificacion as EJE. Legacy {@code cl.bcs.APV.EjecutarBonificacion} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class EjecutarBonificacionServicio implements cl.bcs.apv.api.casouso.EjecutarBonificacion {

    private static final String SQL_SELECT_ORIGEN_DESTINO_TRANSICION = SqlLegacy.recurso("ConsultaOrigenDestinoTransicion");
    private static final String SQL_SELECT_CONSULTAR_BONIFICACION = SqlLegacy.recurso("ConsultarBonificacionAEjecutar");
    private static final String SQL_CALL_CREAR_PORTAFOLIO = SqlLegacy.recurso("Crear_Portafolio_APV_Call");
    private static final String SQL_UPDATE_CAMBIO_ESTADO = SqlLegacy.recurso("Bonificacion_cambioEstado");
    private static final String SQL_SELECT_REGIMEN = SqlLegacy.recurso("ComunesSolicitudes_SelectRegimenSerieDeFondo");
    private static final String SQL_UPDATE_PORTAFOLIO = SqlLegacy.recurso("Bonificacion_UpdatePortafolioBF");
    private static final String SQL_PORTAFOLIO_EMPRESA = SqlLegacy.recurso("Bonificacion_PortafolioEmpresa");
    private static final String SQL_MONEDA_FONDO = SqlLegacy.recurso("Bonificacion_MonedaFondo");

    private final SqlLegacy sql;
    private final ConfiguracionSistema configuracion;
    private final Colaboradores colaboradores;
    private final ConsultarCustodiaPortafolio consultarCustodiaPortafolio;
    private final CrearRecaudacionCliente crearRecaudacionCliente;
    private final CrearRecaudacionDeposito crearRecaudacionDeposito;

    public EjecutarBonificacionServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores,
            ConsultarCustodiaPortafolio consultarCustodiaPortafolio, CrearRecaudacionCliente crearRecaudacionCliente,
            CrearRecaudacionDeposito crearRecaudacionDeposito) {
        this.sql = new SqlLegacy(jdbc);
        this.configuracion = configuracion;
        this.colaboradores = colaboradores;
        this.consultarCustodiaPortafolio = consultarCustodiaPortafolio;
        this.crearRecaudacionCliente = crearRecaudacionCliente;
        this.crearRecaudacionDeposito = crearRecaudacionDeposito;
    }

    // ---------------------------------------------------------------------------------------------------- legacy rows (public fields = column labels / SQL parameters)

    public static class OrigenDestinoTransicion {
        public String OrigenDestino;
        public String MueveCuentaCorriente;
        public String MueveCuentaInversion;
        public Long IdCuentaBanco;
        public Long IdBanco;
        public String IdTipoCuentaBancaria;
        public String NombreTipoCuentaBancaria;
        public Long IdSocioDeNegocio;
        public Long SocioNegocioCtaBco;
    }

    public static class IdPortafolioDefecto {
        public Long IdPortafolioDefecto;
    }

    public static class IdMonedaFondo {
        public String IdMoneda;
    }

    public static class RegimenSerieDeFondo {
        public String IdRegimenFondoMutuoSerieFondo;
    }

    public static class Bonificacion {
        public Long IdInterno = 0L;
        public Long IdBonificacion;
        public Long AnoAhorro;
        public Long AnoProceso;
        public BigDecimal MontoPagadoNeto;
        public BigDecimal MontoPagadoReajustado;
        public BigDecimal MontoCalculado;
        public BigDecimal MontoBonificacionAfiliadoNeto;
        public BigDecimal MontoDiferencia;
        public Long IdPersona;
        public Long IdCliente;
        public Long IdPortafolio;
        public Long IdMatrizAgrupadorAPV;
        public Long IdRegimenTributario;
        public Long IdPortafolioBonificacion;
        public String IdPropositoPortafolio;
        public String NombrePlan;
        public String CodigoAgrupador;
        public String IdPerfilComercialBaseOrigen;
        public String TipoAhorro;
        public BigDecimal MontoAfiliadoNeto;
        public BigDecimal MontoReajustadoPagadoAfiliado;
        public Long IdTraspaso = 0L;
    }

    public static class BonificacionUpdate {
        public String IdEstado;
        public Long IdBonificacion;
        public Long IdEmpresa;
    }

    public static class UpdatePortafolio {
        public Long IdPortafolio;
    }

    /** Mutable copy of a custody line (the legacy mutates the answer of the custody service: removes USD lines and overwrites the currency of fund lines). */
    private static final class CustodiaFila {
        String Nemotecnico;
        BigDecimal MontoPPPCustodia;
        String IdMonedaInstrReajuste;
        String IdTipoInstrumento;
        Long IdInstrumento;

        CustodiaFila(Custodia c) {
            this.Nemotecnico = c.nemotecnico();
            this.MontoPPPCustodia = c.montoPPPCustodia();
            this.IdMonedaInstrReajuste = c.idMonedaInstrReajuste();
            this.IdTipoInstrumento = c.idTipoInstrumento();
            this.IdInstrumento = c.idInstrumento();
        }
    }

    /** Mutable deposit line (the legacy fills IdInterno... and later reads it). */
    private static final class RecaudacionFila {
        Long IdInterno;
        Long IdRecaudacion;
        Long IdEmpresa;
        String CodigoArchivo;
        Long FolioCartola;
        Long LineaCartola;
        String Sucursal;
        String FechaDeposito;
        Long IdCuentaBanco;
        String CuentaCorriente;
        BigDecimal MontoDeposito;
        String ConceptoRecaudacion;
        String Origen;
        String Observacion;
        Long NumeroDocumento;
        String IdTipoDeposito;
        Long IdBanco;
        Long IdRegimenTributario;
        Long IdCarga;
        String IdEstado;

        Recaudacion aRecord() {
            return new Recaudacion(IdInterno, IdRecaudacion, IdEmpresa, CodigoArchivo, FolioCartola, LineaCartola, Sucursal, FechaDeposito, IdCuentaBanco, IdBanco,
                    CuentaCorriente, MontoDeposito, ConceptoRecaudacion, Origen, Observacion, NumeroDocumento, IdTipoDeposito, IdRegimenTributario, IdCarga, IdEstado);
        }
    }

    /** Mutable client line (the legacy fills IdRecaudacion once the deposits exist). */
    private static final class ClienteFila {
        Long IdInterno;
        Long IdRecaudacionCliente;
        Long IdRecaudacion;
        Long IdRecaudacionEmpleador;
        Long IdEmpresa;
        Long IdCliente;
        String FechaIngreso;
        String TipoAfiliado;
        String IdRegimenTriburario;
        BigDecimal MontoDepositoConvenido;
        BigDecimal MontoDepositoAPV;
        String Origen;
        String IdEstado;

        Cliente aRecord() {
            return new Cliente(IdInterno, IdRecaudacionCliente, IdRecaudacion, IdRecaudacionEmpleador, IdEmpresa, IdCliente, FechaIngreso, TipoAfiliado,
                    IdRegimenTriburario, MontoDepositoConvenido, MontoDepositoAPV, Origen, IdEstado);
        }
    }

    @Override
    public EjecutarBonificacionResultado ejecutar(EjecutarBonificacionEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new EjecutarBonificacionResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(EjecutarBonificacionEntrada in) throws Exception {
        List<MensajeLegacy> salida = new ArrayList<>();
        Long errores = 0L; // legacy salida.P_OUT_ERRORESCONTADOR (call() recomputes the final value from the messages)
        Long idCtaBco = 0L;
        Long idBco = 0L;
        Long idPortafolioCorredora = 0L;
        BigDecimal sumaMontosDiferencia = BigDecimal.valueOf(0);

        Long contadorFFMM = 0L;
        Long contadorOrden = 1L;
        Long contador = 1L;
        List<RecaudacionFila> listParamsRecaudacion = new ArrayList<>();
        List<ClienteFila> listParamsCliente = new ArrayList<>();
        SolicitudFFMMG4.EntradaCrearSolicitudAporteRescateNormal entradaCrearSolicitudAporteRescateNormal = new SolicitudFFMMG4.EntradaCrearSolicitudAporteRescateNormal();
        List<OrdenesG4.ObjOrdenesEntrada> objOrden = new ArrayList<>();
        List<CalculoOrdenesG4.Orden_Entrada> objCalculoMontoOrden = new ArrayList<>();
        List<BonificacionUpdate> listBonUpdate = new ArrayList<>();

        // ---- initial variables
        Map<String, Object> paramsSelectOrigenDestinoTransicion = new HashMap<>();
        paramsSelectOrigenDestinoTransicion.put("IdEmpresa", in.idEmpresa());
        paramsSelectOrigenDestinoTransicion.put("Operacion", "BON");
        // bank id BON that receives the bank movement
        List<OrigenDestinoTransicion> transicion = sql.consultar(SQL_SELECT_ORIGEN_DESTINO_TRANSICION, paramsSelectOrigenDestinoTransicion, OrigenDestinoTransicion.class);
        for (OrigenDestinoTransicion fila : transicion) {
            if (fila.OrigenDestino.equals("DESTINO")) {
                idCtaBco = fila.IdCuentaBanco;
                idBco = fila.IdBanco;
            }
        }
        ArrayList<Long> idsBonificacion = new ArrayList<>();
        Map<String, Object> parametrosConsultaBonificacion = new HashMap<>();
        for (ParametrosBonificacion fila : in.parametros()) {
            idsBonificacion.add(fila.idBonificacion());
        }
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());
        parametrosConsultaBonificacion.put("IdsBonificacion", idsBonificacion);
        List<Bonificacion> listaBonificacion = sql.consultar(SQL_SELECT_CONSULTAR_BONIFICACION, parametrosConsultaBonificacion, Bonificacion.class);

        Map<String, Object> paramsSelectPortafolio = new HashMap<>();
        paramsSelectPortafolio.put("IdEmpresa", in.idEmpresa());
        List<IdPortafolioDefecto> portafolioDefecto = sql.consultar(SQL_PORTAFOLIO_EMPRESA, paramsSelectPortafolio, IdPortafolioDefecto.class);

        Long contadorCtaInv = 1L;
        idPortafolioCorredora = portafolioDefecto.get(0).IdPortafolioDefecto;

        // ---- processing loop
        Long contadorId = 0L;
        for (Bonificacion fila : listaBonificacion) {
            contadorId = contadorId + 1;
            fila.IdInterno = contadorId;

            // CUSTODY
            ConsultarCustodiaPortafolioEntrada paramsCustodia = new ConsultarCustodiaPortafolioEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), fila.IdCliente,
                    fila.IdPortafolio, "", "", 0L, "", "N", "N", fechaSistema, "LIQ", 0L, 0, 0, 0L, "0");
            ConsultarCustodiaPortafolioResultado rpt = consultarCustodiaPortafolio.ejecutar(paramsCustodia);

            List<CustodiaFila> resultadoCustodia = new ArrayList<>();
            for (Custodia c : rpt.custodia()) {
                resultadoCustodia.add(new CustodiaFila(c));
            }

            BigDecimal totalAPagar = BigDecimal.ZERO;
            for (CustodiaFila filaresultadoCustodia : resultadoCustodia) {
                if (filaresultadoCustodia.MontoPPPCustodia.compareTo(BigDecimal.ZERO) != 0) {
                    totalAPagar = totalAPagar.add(filaresultadoCustodia.MontoPPPCustodia);
                }
            }

            // BF PORTFOLIO: validates whether it already exists, otherwise creates it
            if (fila.IdPortafolioBonificacion == 0) {
                Map<String, Object> paramsPorta = new HashMap<>();
                paramsPorta.put("IdEmpresa", in.idEmpresa());
                paramsPorta.put("IdUsuario", in.idUsuario());
                paramsPorta.put("IdModulo", in.idModulo());
                paramsPorta.put("PropositoPortafolio", fila.IdPropositoPortafolio);
                paramsPorta.put("IdCliente", fila.IdCliente);
                paramsPorta.put("IdPerfilComercial", fila.IdPerfilComercialBaseOrigen);
                paramsPorta.put("CodigoPortafolio", "BF");
                paramsPorta.put("CodigoRegimen", "BF");
                paramsPorta.put("NombrePlan", fila.NombrePlan);
                paramsPorta.put("CodigoAgrupador", fila.CodigoAgrupador);

                JsonObject resultSP = Colab.procedimiento(colaboradores, SQL_CALL_CREAR_PORTAFOLIO, paramsPorta);

                if (largo(resultSP, "p_out_ErroresContador").equals(Long.valueOf(0))) {
                    fila.IdPortafolioBonificacion = largo(resultSP, "p_out_RefId");
                    // CodigoPortafolio is set to BF
                    UpdatePortafolio updPort = new UpdatePortafolio();
                    List<UpdatePortafolio> listUpdPort = new ArrayList<>();
                    updPort.IdPortafolio = fila.IdPortafolioBonificacion;
                    listUpdPort.add(updPort);
                    sql.lote(SQL_UPDATE_PORTAFOLIO, listUpdPort);
                    salida.add(new MensajeLegacy(fila.IdInterno, "INFORMACION", "CREACION_PORTAFOLIO_BF_EXITOSO", "CREACION_PORTAFOLIO_BF_EXITOSO"));
                } else {
                    // LEGACY DEFECT kept (see README APV-C-303): the CrearPortafolio errors and warnings are read (NullPointerException when the list is absent) but never reported
                    for (Object errorDetalle : lista(resultSP, "p_out_ErroresDetalle")) {
                        // message construction commented out in the legacy
                    }
                    for (Object advertenciaDetalle : lista(resultSP, "p_out_AdvertenciasDetalle")) {
                        // message construction commented out in the legacy
                    }
                }
                if (fila.IdPortafolioBonificacion != 0) { // legacy: unboxes (NullPointerException when CrearPortafolio answers no RefId); message code commented out
                }
            }

            if (resultadoCustodia.size() == 0) {
                salida.add(new MensajeLegacy(fila.IdInterno, "ADVERTENCIA", "CLIENTE_NO_TIENE_CUSTODIA", "Cliente no tiene custodia"));
            } else {
                for (int i = resultadoCustodia.size() - 1; i >= 0; i--) {
                    // LEGACY DEFECT kept (see README APV-C-305, 322): USD custody is dropped before distributing; a null currency throws NullPointerException
                    if (resultadoCustodia.get(i).IdMonedaInstrReajuste.equals("USD")) {
                        resultadoCustodia.remove(i);
                    }
                }
            }

            // ORDERS (shares) AND MUTUAL FUND REQUESTS
            for (CustodiaFila filaresultadoCustodia : resultadoCustodia) {
                Map<String, Object> params = new HashMap<>();
                params.put("IdEmpresa", in.idEmpresa());
                params.put("IdSerieFondo", filaresultadoCustodia.Nemotecnico);
                params.put("IdRegimenTributario", "BF");

                List<RegimenSerieDeFondo> regimenesSeries = sql.consultar(SQL_SELECT_REGIMEN, params, RegimenSerieDeFondo.class);
                Map<Object, Object> hseriesRegimen = lista2Hash(regimenesSeries);

                // LEGACY DEFECT kept (see README APV-C-306): the BF regimen check applies to shares too; a series without BF row is skipped silently
                Object r = hseriesRegimen.get(filaresultadoCustodia.Nemotecnico + "_" + "BF");

                if (r != null) {
                    if (filaresultadoCustodia.IdTipoInstrumento.equals("FFM")) {
                        BigDecimal montoApagar = filaresultadoCustodia.MontoPPPCustodia;
                        BigDecimal montoApagar2 = montoApagar.multiply(BigDecimal.valueOf(100L));
                        // LEGACY DEFECT kept (see README APV-C-307, 321): divide with the scale of the dividend (percentage rounded to an integer, HALF_DOWN for funds); a zero total throws ArithmeticException
                        BigDecimal montoApagar4 = montoApagar2.divide(totalAPagar, RoundingMode.HALF_DOWN);

                        BigDecimal montoCalc1 = fila.MontoCalculado;
                        BigDecimal montoCalc2 = montoCalc1.multiply(montoApagar4);
                        BigDecimal montoApagar3 = montoCalc2.divide(BigDecimal.valueOf(100L), RoundingMode.HALF_DOWN);

                        Map<String, Object> paramsSelectMonedaFondo = new HashMap<>();
                        paramsSelectMonedaFondo.put("IdSerieFondo", filaresultadoCustodia.Nemotecnico);
                        List<IdMonedaFondo> idMonedaFondo = sql.consultar(SQL_MONEDA_FONDO, paramsSelectMonedaFondo, IdMonedaFondo.class);

                        filaresultadoCustodia.IdMonedaInstrReajuste = idMonedaFondo.get(0).IdMoneda;
                        SolicitudFFMMG4.InSolicitudesAporteRescate aporteRescateFFMM = new SolicitudFFMMG4.InSolicitudesAporteRescate();

                        contadorFFMM++;
                        aporteRescateFFMM.IdInterno = contadorFFMM;
                        aporteRescateFFMM.IdCliente = fila.IdCliente;
                        aporteRescateFFMM.IdPortafolio = fila.IdPortafolioBonificacion;
                        aporteRescateFFMM.TipoSolicitud = "APO";
                        aporteRescateFFMM.IdSerieFondo = filaresultadoCustodia.Nemotecnico;
                        aporteRescateFFMM.CalculoPorMontoCuota = "M";
                        aporteRescateFFMM.EsPorMontoLiquido = "N";
                        aporteRescateFFMM.Monto = montoApagar3;
                        aporteRescateFFMM.NumeroCuotas = BigDecimal.valueOf(0);
                        aporteRescateFFMM.IdMoneda = filaresultadoCustodia.IdMonedaInstrReajuste;
                        aporteRescateFFMM.IdMedioDePago = "CIN";
                        aporteRescateFFMM.IdPreferenciaPagoPortafolio = fila.IdPortafolioBonificacion;
                        aporteRescateFFMM.Visado = "N";
                        aporteRescateFFMM.SolicitudBatch = "N";
                        aporteRescateFFMM.OrigenOperacion = "M";
                        aporteRescateFFMM.FechaOperacion = fechaSistema;
                        aporteRescateFFMM.IdRegimenTributario = "BF";
                        aporteRescateFFMM.Glosa = fila.IdBonificacion.toString();
                        aporteRescateFFMM.IndicadorMLT = "N";
                        aporteRescateFFMM.IdSerieFondoDestino = "";
                        aporteRescateFFMM.IdEmisorDestino = "";
                        aporteRescateFFMM.IdAutorizadoOrigen = 0L;
                        aporteRescateFFMM.Porcentaje = BigDecimal.valueOf(0);
                        aporteRescateFFMM.FolioContrato = 0L;
                        aporteRescateFFMM.IngresoAutomatico = "S";

                        entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.add(aporteRescateFFMM);
                    } else {
                        if (filaresultadoCustodia.MontoPPPCustodia.compareTo(BigDecimal.ZERO) != 0) {
                            BigDecimal montoApagar = filaresultadoCustodia.MontoPPPCustodia;
                            BigDecimal montoApagar2 = montoApagar.multiply(BigDecimal.valueOf(100L));
                            // LEGACY DEFECT kept (see README APV-C-307): HALF_UP for shares (87.5% becomes 88)
                            BigDecimal montoApagar4 = montoApagar2.divide(totalAPagar, RoundingMode.HALF_UP);

                            BigDecimal montoCalc1 = fila.MontoCalculado;
                            BigDecimal montoCalc2 = montoCalc1.multiply(montoApagar4);
                            BigDecimal montoApagar3 = montoCalc2.divide(BigDecimal.valueOf(100L), RoundingMode.HALF_UP);

                            OrdenesG4.ObjOrdenesEntrada orden = new OrdenesG4.ObjOrdenesEntrada();

                            List<InstrumentoPrecioG4.Instrumento> objInstrumento = new ArrayList<>();
                            InstrumentoPrecioG4.Instrumento instru = new InstrumentoPrecioG4.Instrumento();

                            instru.IdEmpresa = in.idEmpresa();
                            instru.IdInstrumento = filaresultadoCustodia.IdInstrumento;
                            instru.IdBolsa = "BCS";
                            instru.Fecha = fechaSistema;
                            instru.IdCondicionDeLiquidacion = "T0";
                            objInstrumento.add(instru);

                            InstrumentoPrecioG4.ConsultarInstrumentoPrecioBolsa_Entrada entradaConsultarInstrumentoPrecioBolsa = new InstrumentoPrecioG4.ConsultarInstrumentoPrecioBolsa_Entrada();
                            entradaConsultarInstrumentoPrecioBolsa.p_IdEmpresa = in.idEmpresa();
                            entradaConsultarInstrumentoPrecioBolsa.p_IdUsuario = in.idUsuario();
                            entradaConsultarInstrumentoPrecioBolsa.p_Instrumentos = objInstrumento;

                            InstrumentoPrecioG4.ConsultarInstrumentoPrecioBolsa_Salida resultadoInstrumento = Colab.de(
                                    colaboradores.invocar("ConsultarInstrumentoPrecioBolsa", Colab.aJson(entradaConsultarInstrumentoPrecioBolsa)),
                                    InstrumentoPrecioG4.ConsultarInstrumentoPrecioBolsa_Salida.class);

                            CalculoOrdenesG4.Orden_Entrada calculoMontoOrdenes = new CalculoOrdenesG4.Orden_Entrada();
                            for (InstrumentoPrecioG4.DatosInstrumentosPrecioBolsa_Salida filaResultadoInstrumento : resultadoInstrumento.P_OUT_INSTRUMENTO) {
                                calculoMontoOrdenes.PrecioOrden = filaResultadoInstrumento.PrecioCierre;
                            }

                            calculoMontoOrdenes.IdInterno = contadorOrden;
                            calculoMontoOrdenes.CantidadOrden = BigDecimal.ZERO;
                            calculoMontoOrdenes.EsFungible = "N";
                            calculoMontoOrdenes.IdBolsa = "BCS";
                            calculoMontoOrdenes.IdCorredorCorresponsal = null;
                            calculoMontoOrdenes.IdInstrumento = filaresultadoCustodia.IdInstrumento;
                            calculoMontoOrdenes.IdInstrumentoSubyacente = 0L;
                            calculoMontoOrdenes.IdMonedaLiquidacionOrden = filaresultadoCustodia.IdMonedaInstrReajuste;
                            calculoMontoOrdenes.IdPortafolio = fila.IdPortafolioBonificacion;
                            calculoMontoOrdenes.IdTipoProducto = "INAC";
                            calculoMontoOrdenes.IdTipoOperacion = "COMACN";
                            calculoMontoOrdenes.IdTipoCanalIngreso = "AGE";
                            calculoMontoOrdenes.FechaOperacion = fechaSistema;
                            calculoMontoOrdenes.MontoOrden = BigDecimal.ZERO;
                            calculoMontoOrdenes.TasaSimultanea = BigDecimal.ZERO;
                            calculoMontoOrdenes.TasaRFIF = BigDecimal.ZERO;
                            calculoMontoOrdenes.TasaVentaCorta = BigDecimal.ZERO;
                            calculoMontoOrdenes.TipoCambio = BigDecimal.ZERO;
                            calculoMontoOrdenes.ValorRescateOrden = BigDecimal.ZERO;
                            calculoMontoOrdenes.PlazoSimultanea = 0L;
                            calculoMontoOrdenes.PlazoVentaCorta = 0L;
                            calculoMontoOrdenes.PlazoIF = 0L;
                            calculoMontoOrdenes.EsAPlazo = "N";
                            calculoMontoOrdenes.MontoTotal = montoApagar3;
                            calculoMontoOrdenes.EsPorMonto = "S";
                            calculoMontoOrdenes.TasaReporte = BigDecimal.ZERO;
                            calculoMontoOrdenes.TasaReportado = BigDecimal.ZERO;
                            calculoMontoOrdenes.PlazoReporte = 0L;
                            calculoMontoOrdenes.BaseTasa = 0L;
                            objCalculoMontoOrden.add(calculoMontoOrdenes);
                            // LEGACY DEFECT kept (see README APV-C-301): the order never gets IdInterno, so matching the quantity calculation to the order throws NullPointerException
                            orden.FechaRecepcion = "0001-01-01 00:00:00";
                            orden.FechaVigencia = "2018-05-09";
                            orden.FechaEmision = "0001-01-01";
                            orden.FechaOperacion = "0001-01-01";
                            orden.IdCondicionDeLiquidacion = "T0";
                            orden.IdTipoOperacion = "COMACN";
                            orden.IdTipoVigencia = "SH";
                            orden.Glosa = fila.IdBonificacion.toString();
                            orden.Cantidad = BigDecimal.valueOf(0L);
                            orden.MontoNeto = montoApagar3;
                            orden.ValorRescate = BigDecimal.valueOf(0L);
                            orden.IdBoveda = "DCV";
                            orden.IdFormaCustodia = "NOM";
                            orden.IdPorcentajeVisibilidad = 1L;
                            orden.PrecioLimite = BigDecimal.valueOf(10000L);
                            orden.EsFacturaInmediata = "N";
                            orden.IdTipoPrecio = "M";
                            orden.IdPortafolio = fila.IdPortafolioBonificacion;
                            orden.IdTipoCanalIngreso = "AGE";
                            orden.IdTipoReajuste = "";
                            orden.IdEmisor = filaresultadoCustodia.Nemotecnico;
                            orden.IdBolsa = "BCS";
                            orden.EspecificaBolsa = "S";
                            orden.IdMonedaLiquidacionOrden = "CLP";
                            orden.TipoCambio = null;
                            orden.IdInstrumento = filaresultadoCustodia.IdInstrumento;
                            orden.IdInstrumentoSubyacente = 0L;
                            orden.IdCondicionDeLiquidacionPlazo = "";
                            orden.TasaVentaCorta = BigDecimal.valueOf(0);
                            orden.PlazoVentaCorta = 0L;
                            orden.EsVentaCorta = "N";
                            orden.TasaSimultanea = BigDecimal.valueOf(0);
                            orden.PlazoSimultanea = 0L;
                            orden.PlazoIF = 0L;
                            orden.TasaRFIF = BigDecimal.valueOf(0);
                            orden.EsOD = "N";
                            orden.EsFungible = "N";
                            orden.IdTipoLibroOrden = "NORMAL";
                            orden.IdLibroOrdenes = null;
                            orden.IdCuentaCustodioPortafolio = null;
                            orden.EsPorMonto = "S";
                            orden.IdCorredorCorresponsal = null;
                            orden.IdPersonaAutorizadoDarOrdenes = null;
                            orden.IdTipoCanalDistribucion = "MAN";
                            orden.EsOrdenEspecial = "N";
                            orden.PagoCobroCuentaInversion = "S";
                            orden.IdPreferenciaPagoPortafolio = fila.IdPortafolioBonificacion;
                            orden.BaseTasa = 0L;
                            orden.TipoInteres = "";
                            orden.IdMecanismoCaptacion = "OM";

                            objOrden.add(orden);

                            contadorOrden++;
                        }
                    }
                }
            }

            RecaudacionFila paramRecaudacion = new RecaudacionFila();
            paramRecaudacion.IdInterno = contadorId;
            paramRecaudacion.IdRecaudacion = 0L;
            paramRecaudacion.IdEmpresa = in.idEmpresa();
            paramRecaudacion.CodigoArchivo = "";
            paramRecaudacion.FolioCartola = 0L;
            paramRecaudacion.LineaCartola = 0L;
            paramRecaudacion.Sucursal = "0";
            paramRecaudacion.FechaDeposito = fechaSistema;
            paramRecaudacion.IdCuentaBanco = idCtaBco;
            paramRecaudacion.CuentaCorriente = "";
            paramRecaudacion.MontoDeposito = fila.MontoBonificacionAfiliadoNeto;
            paramRecaudacion.ConceptoRecaudacion = "";
            paramRecaudacion.Origen = "BONIF";
            paramRecaudacion.Observacion = "";
            paramRecaudacion.NumeroDocumento = 0L;
            paramRecaudacion.IdTipoDeposito = "TRA";
            paramRecaudacion.IdBanco = idBco;
            paramRecaudacion.IdRegimenTributario = 0L;
            paramRecaudacion.IdCarga = 0L;
            paramRecaudacion.IdEstado = "ING";
            listParamsRecaudacion.add(paramRecaudacion);

            ClienteFila paramCliente = new ClienteFila();
            paramCliente.IdInterno = contadorId;
            paramCliente.IdRecaudacionCliente = 0L;
            paramCliente.IdRecaudacion = 0L;
            paramCliente.IdRecaudacionEmpleador = 0L;
            paramCliente.IdEmpresa = in.idEmpresa();
            paramCliente.IdCliente = fila.IdCliente;
            paramCliente.FechaIngreso = fechaSistema;
            paramCliente.TipoAfiliado = "IND";
            paramCliente.IdRegimenTriburario = "BF";
            paramCliente.MontoDepositoConvenido = BigDecimal.valueOf(0L);
            paramCliente.MontoDepositoAPV = fila.MontoBonificacionAfiliadoNeto;
            paramCliente.Origen = "BONIF";
            paramCliente.IdEstado = "VIG";
            listParamsCliente.add(paramCliente);

            sumaMontosDiferencia = sumaMontosDiferencia.add(fila.MontoDiferencia);
        }

        // no deposit means no bonificacion for anybody
        if (listParamsRecaudacion.size() == 0) {
            return salida;
        }

        try {
            // ---- CREATE DEPOSIT
            List<Recaudacion> paramsDeposito = new ArrayList<>();
            for (RecaudacionFila f : listParamsRecaudacion) {
                paramsDeposito.add(f.aRecord());
            }
            CrearRecaudacionDepositoResultado objOutDep = crearRecaudacionDeposito
                    .ejecutar(new CrearRecaudacionDepositoEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", "BONIF", paramsDeposito));

            errores += objOutDep.contadorErrores();

            for (Mensaje fila : objOutDep.mensajes()) {
                if (!fila.codigo().equals("UI_CREACION_DEPOSITO_OK")) {
                    salida.add(new MensajeLegacy(fila.id(), fila.tipo(), fila.codigo(), fila.descripcion()));
                }
            }

            for (int idRec = 0; idRec < objOutDep.resultado().size(); idRec++) {
                for (int idCli = 0; idCli < listParamsCliente.size(); idCli++) {
                    if (listParamsCliente.get(idCli).IdInterno.equals(objOutDep.resultado().get(idRec).idInterno())) {
                        listParamsCliente.get(idCli).IdRecaudacion = objOutDep.resultado().get(idRec).idRecaudacion();
                    }
                }
            }
            try {
                // ---- CREATE CLIENT LINES
                List<Cliente> paramsCliente = new ArrayList<>();
                for (ClienteFila f : listParamsCliente) {
                    paramsCliente.add(f.aRecord());
                }
                crearRecaudacionCliente.ejecutar(new CrearRecaudacionClienteEntrada(in.idEmpresa(), in.idUsuario(), "APV", "I", "CARGA", paramsCliente));

                List<CuentaInversionG4.ObjCargoAbonoAGenerar> paramsCargoAbono = new ArrayList<>();
                for (int idRec = 0; idRec < objOutDep.resultado().size(); idRec++) {
                    for (int idCli = 0; idCli < listParamsCliente.size(); idCli++) {
                        if (listParamsCliente.get(idCli).IdInterno.equals(objOutDep.resultado().get(idRec).idInterno())) {
                            CuentaInversionG4.ObjCargoAbonoAGenerar ctaInvAbono = new CuentaInversionG4.ObjCargoAbonoAGenerar();
                            CuentaInversionG4.ObjCargoAbonoAGenerar ctaInvCargo = new CuentaInversionG4.ObjCargoAbonoAGenerar();

                            // cargo/abono that moves the investment account
                            ctaInvAbono.NumeroCargoAbono = contadorCtaInv;
                            ctaInvAbono.IdPortafolio = listaBonificacion.get(idCli).IdPortafolioBonificacion;
                            ctaInvAbono.IdMoneda = "CLP";
                            ctaInvAbono.IdTipoMovimiento = "MOVABN";
                            ctaInvAbono.IdConcepto = "ABOBON";
                            ctaInvAbono.Descripcion = "APV";
                            ctaInvAbono.IdCondicionDeLiquidacion = "T0";
                            ctaInvAbono.FechaCondicionDeLiquidacion = fechaSistema;
                            ctaInvAbono.Monto = listaBonificacion.get(idCli).MontoCalculado;
                            ctaInvAbono.IdEjecutivo = 0L;
                            ctaInvAbono.IdSucursal = 0L;
                            ctaInvAbono.IdTipoProducto = "";
                            ctaInvAbono.IdTipoOperacion = "";
                            ctaInvAbono.IdOperacion = listaBonificacion.get(idCli).IdBonificacion;
                            ctaInvAbono.FechaOperacion = "0001-01-01";
                            ctaInvAbono.FechaLiquidacionOperacion = "0001-01-01";
                            ctaInvAbono.FechaVencimientoOperacion = null;
                            ctaInvAbono.IdMonedaOperacion = "";
                            ctaInvAbono.MontoOperacion = null;
                            ctaInvAbono.TipoCambioOperacion = BigDecimal.valueOf(0L);
                            ctaInvAbono.IdContraparte = null;
                            ctaInvAbono.IdTipoComprobante = null;
                            ctaInvAbono.IdComprobante = null;
                            ctaInvAbono.FolioComprobante = null;
                            ctaInvAbono.IdComprobanteDetalle = null;
                            ctaInvAbono.IdComprobanteFormaDePago = null;
                            ctaInvAbono.IdTipoMovimientoAsociado = null;
                            ctaInvAbono.IdMovimientoAsociado = null;
                            ctaInvAbono.FechaComprobante = null;
                            ctaInvAbono.IdPersonaPagoCobro = null;
                            ctaInvAbono.IdInstrumento = null;
                            ctaInvAbono.IdTipoInstrumento = null;
                            ctaInvAbono.Nemo = null;
                            ctaInvAbono.EsIndicadorBilateral = "N";
                            ctaInvAbono.EsCargoAbonoPorTraspaso = "N";
                            ctaInvAbono.IdPortafolioTraspaso = null;
                            ctaInvAbono.GeneroContraparteEnTesoreria = "N";
                            ctaInvAbono.CertificadoAutomatico = "S";
                            paramsCargoAbono.add(ctaInvAbono);

                            contadorCtaInv++;
                            ctaInvCargo.NumeroCargoAbono = contadorCtaInv;
                            ctaInvCargo.IdPortafolio = idPortafolioCorredora;
                            ctaInvCargo.IdMoneda = "CLP";
                            ctaInvCargo.IdTipoMovimiento = "MOVCAR";
                            ctaInvCargo.IdConcepto = "CARBON";
                            ctaInvCargo.Descripcion = "APV";
                            ctaInvCargo.IdCondicionDeLiquidacion = "T0";
                            ctaInvCargo.FechaCondicionDeLiquidacion = fechaSistema;
                            ctaInvCargo.Monto = listaBonificacion.get(idCli).MontoCalculado;
                            ctaInvCargo.IdEjecutivo = 0L;
                            ctaInvCargo.IdSucursal = 0L;
                            ctaInvCargo.IdTipoProducto = "";
                            ctaInvCargo.IdTipoOperacion = "";
                            ctaInvCargo.IdOperacion = listaBonificacion.get(idCli).IdBonificacion;
                            ctaInvCargo.FechaOperacion = "0001-01-01";
                            ctaInvCargo.FechaLiquidacionOperacion = "0001-01-01";
                            ctaInvCargo.FechaVencimientoOperacion = null;
                            ctaInvCargo.IdMonedaOperacion = "";
                            ctaInvCargo.MontoOperacion = null;
                            ctaInvCargo.TipoCambioOperacion = BigDecimal.valueOf(0L);
                            ctaInvCargo.IdContraparte = null;
                            ctaInvCargo.IdTipoComprobante = null;
                            ctaInvCargo.IdComprobante = null;
                            ctaInvCargo.FolioComprobante = null;
                            ctaInvCargo.IdComprobanteDetalle = null;
                            ctaInvCargo.IdComprobanteFormaDePago = null;
                            ctaInvCargo.IdTipoMovimientoAsociado = null;
                            ctaInvCargo.IdMovimientoAsociado = null;
                            ctaInvCargo.FechaComprobante = null;
                            ctaInvCargo.IdPersonaPagoCobro = null;
                            ctaInvCargo.IdInstrumento = null;
                            ctaInvCargo.IdTipoInstrumento = null;
                            ctaInvCargo.Nemo = null;
                            ctaInvCargo.EsIndicadorBilateral = "N";
                            ctaInvCargo.EsCargoAbonoPorTraspaso = "N";
                            ctaInvCargo.IdPortafolioTraspaso = null;
                            ctaInvCargo.GeneroContraparteEnTesoreria = "N";
                            ctaInvCargo.CertificadoAutomatico = "S";
                            contadorCtaInv++;
                            paramsCargoAbono.add(ctaInvCargo);

                            BonificacionUpdate bonUpdate = new BonificacionUpdate();
                            bonUpdate.IdBonificacion = listaBonificacion.get(idCli).IdBonificacion;
                            bonUpdate.IdEmpresa = in.idEmpresa();
                            bonUpdate.IdEstado = "EJE";
                            listBonUpdate.add(bonUpdate);
                        }
                    }
                }

                // ---- INVESTMENT ACCOUNT
                CuentaInversionG4.CrearCargoAbonoEntrada paramsCuentaInversion = new CuentaInversionG4.CrearCargoAbonoEntrada();
                paramsCuentaInversion.p_IdEmpresa = in.idEmpresa();
                paramsCuentaInversion.p_IdUsuario = in.idUsuario();
                paramsCuentaInversion.p_IdModulo = in.idModulo();
                paramsCuentaInversion.p_simularOperacion = "N";
                paramsCuentaInversion.p_UsaReglasNegocio = "S";
                paramsCuentaInversion.p_FechaSistema = fechaSistema;
                paramsCuentaInversion.p_CargoAbonoAGenerar = paramsCargoAbono;
                paramsCuentaInversion.p_EsIngresoManual = "N";

                CuentaInversionG4.CrearCargoAbonoSalida outCuentaInversion = Colab.de(colaboradores.invocar("CrearCargoAbono", Colab.aJson(paramsCuentaInversion)),
                        CuentaInversionG4.CrearCargoAbonoSalida.class);
                errores += outCuentaInversion.P_OUT_ERRORESCONTADOR;

                // LEGACY DEFECT kept (see README APV-C-318): the translated message is built but never added to the output (the errors only reach the counter)
                for (CuentaInversionG4.Msg filaResultadoCtaInv : outCuentaInversion.P_OUT_MENSAJES) {
                    for (int i = listaBonificacion.size() - 1; i >= 0; i--) {
                        for (int j = paramsCargoAbono.size() - 1; j >= 0; j--) {
                            if (paramsCargoAbono.get(j).IdOperacion.toString().equals(listaBonificacion.get(i).IdBonificacion.toString())
                                    && paramsCargoAbono.get(j).NumeroCargoAbono.toString().equals(filaResultadoCtaInv.id.toString())) {
                                // message built and dropped
                            }
                        }
                    }
                }

                // ---- ORDERS
                if (objOrden.size() > 0) {
                    CalculoOrdenesG4.CalcularMontoYCobrosOrden_Entrada paramsCalcularMonto = new CalculoOrdenesG4.CalcularMontoYCobrosOrden_Entrada();
                    paramsCalcularMonto.p_IdEmpresa = in.idEmpresa();
                    paramsCalcularMonto.p_IdUsuario = in.idUsuario();
                    paramsCalcularMonto.p_IdModulo = in.idModulo();
                    paramsCalcularMonto.p_Ordenes = objCalculoMontoOrden;

                    CalculoOrdenesG4.CalcularMontoYCobrosOrden_Salida outCalcularMonto = Colab.de(
                            colaboradores.invocar("CalcularMontoYCobrosOrden", Colab.aJson(paramsCalcularMonto)), CalculoOrdenesG4.CalcularMontoYCobrosOrden_Salida.class);

                    for (int i = outCalcularMonto.p_out_Ordenes.size() - 1; i >= 0; i--) {
                        for (int j = objOrden.size() - 1; j >= 0; j--) {
                            if (outCalcularMonto.p_out_Ordenes.get(i).IdInterno.toString().equals(objOrden.get(j).IdInterno.toString())) {
                                objOrden.get(j).Cantidad = outCalcularMonto.p_out_Ordenes.get(i).CantidadOperacion;
                            }
                        }
                    }

                    OrdenesG4.CrearOrdenesEntrada paramsOrdenesEntrada = new OrdenesG4.CrearOrdenesEntrada();
                    paramsOrdenesEntrada.p_IdEmpresa = in.idEmpresa();
                    paramsOrdenesEntrada.p_IdUsuario = in.idUsuario();
                    paramsOrdenesEntrada.p_IdModulo = "APV";
                    paramsOrdenesEntrada.p_simularOperacion = "N";
                    paramsOrdenesEntrada.p_UsaReglasNegocio = "S";
                    paramsOrdenesEntrada.p_Ordenes = objOrden;

                    try {
                        OrdenesG4.CrearOrdenesSalida outOrdenes = Colab.de(colaboradores.invocar("CrearOrdenes", Colab.aJson(paramsOrdenesEntrada)), OrdenesG4.CrearOrdenesSalida.class);
                        for (OrdenesG4.Msg m : outOrdenes.P_OUT_MENSAJES) {
                            salida.add(new MensajeLegacy(m.id, m.tipo, m.codigo, m.descripcion));
                        }
                        errores += outOrdenes.P_OUT_ERRORESCONTADOR;
                    } catch (Exception er) {
                        // LEGACY DEFECT kept (see README APV-C-315, 317): exceptions of the order service are swallowed
                    }
                }
                if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() > 0) {
                    try {
                        entradaCrearSolicitudAporteRescateNormal.p_IdEmpresa = in.idEmpresa();
                        entradaCrearSolicitudAporteRescateNormal.p_IdUsuario = in.idUsuario();
                        entradaCrearSolicitudAporteRescateNormal.p_IdModulo = in.idModulo();
                        entradaCrearSolicitudAporteRescateNormal.p_SimularOperacion = "N";
                        entradaCrearSolicitudAporteRescateNormal.p_UsaReglasNegocio = "S";
                        entradaCrearSolicitudAporteRescateNormal.p_IdOrigenProceso = "BONIF";
                        SolicitudFFMMG4.SalidaCrearSolicitudAporteRescateNormal rpt = Colab.de(
                                colaboradores.invocar("CrearSolicitudAporteRescateNormal", Colab.aJson(entradaCrearSolicitudAporteRescateNormal)),
                                SolicitudFFMMG4.SalidaCrearSolicitudAporteRescateNormal.class);

                        errores += rpt.P_OUT_ERRORESCONTADOR;

                        for (SolicitudFFMMG4.Msg mje : rpt.P_OUT_MENSAJES) {
                            for (int i = listaBonificacion.size() - 1; i >= 0; i--) {
                                for (int j = entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.size() - 1; j >= 0; j--) {
                                    if (entradaCrearSolicitudAporteRescateNormal.p_SolicitudesAporteRescate.get(j).Glosa.toString()
                                            .equals(listaBonificacion.get(i).IdBonificacion.toString())) {
                                        salida.add(new MensajeLegacy(listaBonificacion.get(i).IdInterno, mje.tipo, mje.codigo, mje.descripcion));
                                    }
                                }
                            }
                        }
                    } catch (Exception er1) {
                        // LEGACY DEFECT kept (see README APV-C-315): exceptions of the fund service are swallowed (after trying to drop the local temp table)
                        eliminarTablaTemporal();
                    } finally {
                        eliminarTablaTemporal();
                    }
                }

                sql.lote(SQL_UPDATE_CAMBIO_ESTADO, listBonUpdate);

            } catch (Exception er) {
                // LEGACY DEFECT kept (see README APV-C-301, 319): every failure of the second stage is reported as "Error al crear cliente"; the deposit already written stays
                salida.add(new MensajeLegacy((long) contador, "ERROR", "ERROR_CREAR_CLIENTE_APV", "Error al crear cliente"));
            }

        } catch (Exception er) {
            sql.rollback();
            // LEGACY DEFECT kept (see README APV-C-320): the first stage failure rolls the connection back and is reported as "Error al crear Recaudacion"
            salida.add(new MensajeLegacy((long) contador, "ERROR", "ERROR_CREAR_RECAUDACION_APV", "Error al crear Recaudacion"));
        }

        return salida;
    }

    /** Legacy {@code SQL.exec(con, "DROP TABLE #TablaApvAporteRescate")} wrapped in its empty catch. */
    private void eliminarTablaTemporal() {
        try {
            sql.ejecutar("DROP TABLE #TablaApvAporteRescate");
        } catch (Exception er) {
            // ignored as in the legacy
        }
    }

    /** Legacy {@code Utils.List2Hash(list, "IdRegimenFondoMutuoSerieFondo")}: throws {@code Exception("key field is not unique")} on a duplicated key. */
    private static Map<Object, Object> lista2Hash(List<RegimenSerieDeFondo> lista) throws Exception {
        Map<Object, Object> hobj = new HashMap<>();
        for (RegimenSerieDeFondo obj : lista) {
            Object kf = obj.IdRegimenFondoMutuoSerieFondo;
            if (hobj.containsKey(kf)) {
                throw new Exception("key field is not unique");
            }
            hobj.put(kf, obj);
        }
        return hobj;
    }

    /** OUT scalar of a stored procedure: null when the answer does not carry it (legacy {@code resultSP.get(...)}). */
    private static Long largo(JsonObject sp, String clave) {
        JsonElement v = sp.get(clave);
        return v == null || v.isJsonNull() ? null : Long.valueOf(v.getAsLong());
    }

    /** OUT table of a stored procedure: null when the answer does not list it (the legacy for-each then throws NullPointerException). */
    private static List<JsonElement> lista(JsonObject sp, String clave) {
        JsonElement v = sp.get(clave);
        if (v == null || v.isJsonNull()) {
            return null;
        }
        List<JsonElement> r = new ArrayList<>();
        v.getAsJsonArray().forEach(r::add);
        return r;
    }
}
