package cl.bcs.apv.application.recaudacion;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.bcs.apv.api.casouso.AsignarContratoARecaudacion;
import cl.bcs.apv.api.casouso.BuscarContratosParaRecaudacion;
import cl.bcs.apv.api.casouso.ConciliarDeposito;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionEntrada;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.AsignarContratoARecaudacionResultado;
import cl.bcs.apv.api.dto.asignarcontratoarecaudacion.ParametrosAsignacionContratoRecaudacion;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.AsignarContratoRecaudacion;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionEntrada;
import cl.bcs.apv.api.dto.buscarcontratospararecaudacion.BuscarContratosParaRecaudacionResultado;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoEntrada;
import cl.bcs.apv.api.dto.conciliardeposito.ConciliarDepositoResultado;
import cl.bcs.apv.api.dto.conciliardeposito.Recaudacion;
import cl.bcs.apv.application.recaudacion.ConciliarAgregarLog.InAgregarLog;
import cl.bcs.apv.domain.port.out.AlmacenArchivos;
import cl.bcs.apv.domain.port.out.Colaboradores;
import cl.bcs.apv.domain.port.out.ConfiguracionSistema;
import cl.bcs.apv.domain.port.out.DiccionarioMensajes;
import cl.bcs.apv.domain.port.out.Reloj;
import cl.bcs.apv.domain.port.out.Secuencias;

/**
 * Use case {@code ConciliarDeposito} (family C): reconciles deposits against their employer and client detail, optionally assigning contracts, and
 * writes the reconciliation log. Legacy {@code cl.bcs.APV.ConciliarDeposito} (call and logica).
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ConciliarDepositoServicio implements ConciliarDeposito {

    private static final String SI = "S";
    private static final String NO = "N";

    private static final String SQL_CONCILIAR_DEPOSITO = SqlLegacy.recurso("ConciliarDeposito");
    private static final String SQL_CONCILIAR_EMPLEADOR = SqlLegacy.recurso("ConciliarRecaudacionEmpleador");
    private static final String SQL_CONCILIAR_CLIENTE = SqlLegacy.recurso("ConciliarRecaudacionCliente");
    private static final String SQL_DEPOSITO = SqlLegacy.recurso("ConsultaDeposito");
    private static final String SQL_EMPLEADOR = SqlLegacy.recurso("ConsultarEmpleadorPorRecaudacion");
    private static final String SQL_CLIENTES = SqlLegacy.recurso("ConsultarClientesPorRecaudacion");

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;
    private final SqlLegacy sql;
    private final AsignarContratoARecaudacion asignarContratoARecaudacion;
    private final BuscarContratosParaRecaudacion buscarContratosParaRecaudacion;

    public ConciliarDepositoServicio(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, DiccionarioMensajes diccionario,
            Secuencias secuencias, Reloj reloj, AlmacenArchivos archivos, Colaboradores colaboradores,
            AsignarContratoARecaudacion asignarContratoARecaudacion, BuscarContratosParaRecaudacion buscarContratosParaRecaudacion) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
        this.sql = new SqlLegacy(jdbc);
        this.asignarContratoARecaudacion = asignarContratoARecaudacion;
        this.buscarContratosParaRecaudacion = buscarContratosParaRecaudacion;
    }

    /** Parameter row of the reconcile statements (legacy {@code ConciliarDeposito.Recaudacion}). */
    public static class RecaudacionFila {
        public Long IdRecaudacion;
        public String IdEstado;
    }

    /** Employer detail row (legacy {@code ConciliarDeposito.Empleador}). */
    public static class EmpleadorFila {
        public Long IdRecaudacionEmpleador;
        public Long IdRecaudacion;
        public String CodigoArchivo;
        public Long IdEmpresa;
        public Long IdPersona;
        public String FechaIngreso;
        public String PeriodoPago;
        public Long NumeroHojas;
        public String TipoAfiliado;
        public String TipoPago;
        public String Insistencias;
        public BigDecimal MontoDepositoConvenido;
        public BigDecimal MontoDepositoAPV;
        public BigDecimal Reajustes;
        public BigDecimal Intereses;
        public String Origen;
        public String Observaciones;
        public String IdEstado;
        public String DocumentoIdentidad;
    }

    /** Client detail row (legacy {@code ConciliarDeposito.Cliente}). */
    public static class ClienteFila {
        public Long IdRecaudacionCliente;
        public Long IdRecaudacion;
        public Long IdRecaudacionEmpleador;
        public Long IdEmpresa;
        public Long IdCliente;
        public String FechaIngreso;
        public String TipoAfiliado;
        public String IdRegimenTriburario;
        public BigDecimal MontoDepositoConvenido;
        public BigDecimal MontoDepositoAPV;
        public String Origen;
        public String IdEstado;
        public String DocumentoIdentidad;
    }

    @Override
    public ConciliarDepositoResultado ejecutar(ConciliarDepositoEntrada in) {
        try {
            List<MensajeLegacy> mensajes = logica(in);
            return new ConciliarDepositoResultado(MensajeLegacy.aMensajes(mensajes), MensajeLegacy.contarErrores(mensajes));
        } catch (Exception e) {
            throw Sneaky.lanzar(e);
        }
    }

    private List<MensajeLegacy> logica(ConciliarDepositoEntrada in) throws Exception {
        List<MensajeLegacy> salida = new ArrayList<>();
        HashMap<String, String> valoresDiccionario = new HashMap<>(3);

        for (Recaudacion fila : in.parametros()) {
            RecaudacionFila rc = new RecaudacionFila();
            MensajeLegacy msg = new MensajeLegacy();
            valoresDiccionario.clear();
            HashMap<String, Object> param = new HashMap<>();
            param.put("p_IdRecaudacion", fila.idRecaudacion());
            valoresDiccionario.put("FolioRecaudacion", fila.idRecaudacion().toString());

            List<DepositoFilas.Recaudacion> deposito = sql.consultar(SQL_DEPOSITO, param, DepositoFilas.Recaudacion.class);

            if (deposito.size() > 0) {
                String estado = deposito.get(0).IdEstado;
                if (estado.equals("ING") == true || estado.equals("NCO") == true) {
                    List<EmpleadorFila> empleadores = sql.consultar(SQL_EMPLEADOR, param, EmpleadorFila.class);
                    List<ClienteFila> clientes = sql.consultar(SQL_CLIENTES, param, ClienteFila.class);

                    if (empleadores.size() > 0) {
                        valoresDiccionario.put("DocumentoIdentidadEmpleador", empleadores.get(0).DocumentoIdentidad);
                        Long totalEmpleador = 0L;
                        Long totalEmpleadorAPV = 0L;
                        Long totalEmpleadorDC = 0L;
                        for (int i = 0; i < empleadores.size(); i++) {
                            // LEGACY DEFECT kept (see README APV-C ConciliarDeposito): amounts are compared as long (truncation)
                            totalEmpleadorAPV = totalEmpleadorAPV + empleadores.get(i).MontoDepositoAPV.longValue();
                            totalEmpleadorDC = totalEmpleadorDC + empleadores.get(i).MontoDepositoConvenido.longValue();
                        }
                        totalEmpleador = totalEmpleadorAPV + totalEmpleadorDC;
                        Long montoDeposito = deposito.get(0).MontoDeposito.longValue();

                        if (montoDeposito.equals(totalEmpleador) == true) {
                            if (clientes.size() > 0) {
                                valoresDiccionario.put("DocumentoIdentidadCliente", clientes.get(0).DocumentoIdentidad);
                                Long totalClienteAPV = 0L;
                                Long totalClienteDC = 0L;
                                for (int i = 0; i < clientes.size(); i++) {
                                    totalClienteAPV = totalClienteAPV + clientes.get(i).MontoDepositoAPV.longValue();
                                    totalClienteDC = totalClienteDC + clientes.get(i).MontoDepositoConvenido.longValue();
                                }
                                if (totalEmpleadorAPV.equals(totalClienteAPV) == true && totalEmpleadorDC.equals(totalClienteDC) == true) {
                                    rc.IdRecaudacion = fila.idRecaudacion();
                                    rc.IdEstado = "CON";
                                    List<RecaudacionFila> paramsRecaudacion = new ArrayList<>();
                                    paramsRecaudacion.add(rc);
                                    int[] res = LoteConteo.ejecutar(jdbc, SQL_CONCILIAR_DEPOSITO, paramsRecaudacion);
                                    LoteConteo.ejecutar(jdbc, SQL_CONCILIAR_CLIENTE, paramsRecaudacion);
                                    LoteConteo.ejecutar(jdbc, SQL_CONCILIAR_EMPLEADOR, paramsRecaudacion);
                                    if (res != null && res.length > 0 && res[0] == 1) {
                                        conciliadoOk(in, fila, msg, valoresDiccionario, salida);
                                    } else {
                                        msg.id = fila.idRecaudacion();
                                        msg.tipo = "ERROR";
                                        msg.codigo = "ERROR";
                                        msg.descripcion = "Problemas al Conciliar";
                                        salida.add(msg);
                                    }
                                } else {
                                    error(in, fila, msg, salida, NO.equals(in.esProcesoMasivo()), "UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE",
                                            "UI_NO_CUADRAN_MONTOS_APV_DC_DE_EMPLEADOR_CLIENTE_MASIVO", valoresDiccionario);
                                }
                            } else {
                                error(in, fila, msg, salida, NO.equals(in.esProcesoMasivo()), "UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE",
                                        "UI_DEPOSITO_CON_EMPLEADOR_SIN_CLIENTE_MASIVO", valoresDiccionario);
                            }
                        } else {
                            error(in, fila, msg, salida, NO.equals(in.esProcesoMasivo()), "UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR",
                                    "UI_NO_CUADRA_DEPOSITO_CON_EMPLEADOR_MASIVO", valoresDiccionario);
                        }
                    } else {
                        if (clientes.size() > 0) {
                            Long totalCliente = 0L;
                            for (int i = 0; i < clientes.size(); i++) {
                                totalCliente = totalCliente + clientes.get(i).MontoDepositoConvenido.longValue() + clientes.get(i).MontoDepositoAPV.longValue();
                            }
                            Long montoDeposito = deposito.get(0).MontoDeposito.longValue();
                            if (montoDeposito.equals(totalCliente) == true) {
                                rc.IdRecaudacion = fila.idRecaudacion();
                                rc.IdEstado = "CON";
                                List<RecaudacionFila> paramsRecaudacion = new ArrayList<>();
                                paramsRecaudacion.add(rc);
                                int[] res = LoteConteo.ejecutar(jdbc, SQL_CONCILIAR_DEPOSITO, paramsRecaudacion);
                                LoteConteo.ejecutar(jdbc, SQL_CONCILIAR_CLIENTE, paramsRecaudacion);
                                if (res != null && res.length > 0 && res[0] == 1) {
                                    conciliadoOk(in, fila, msg, valoresDiccionario, salida);
                                } else {
                                    msg.id = fila.idRecaudacion();
                                    msg.tipo = "ERROR";
                                    msg.codigo = "ERROR";
                                    msg.descripcion = "Problemas al Conciliar";
                                    salida.add(msg);
                                }
                            } else {
                                error(in, fila, msg, salida, NO.equals(in.esProcesoMasivo()), "UI_NO_CUADRA_DEPOSITO_CON_CLIENTE",
                                        "UI_NO_CUADRA_DEPOSITO_CON_CLIENTE_MASIVO", valoresDiccionario);
                            }
                        } else {
                            error(in, fila, msg, salida, NO.equals(in.esProcesoMasivo()), "UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES",
                                    "UI_DEPOSITO_SIN_EMPLEADOR_NI_CLIENTES_MASIVO", valoresDiccionario);
                        }
                    }
                } else {
                    HashMap<String, String> parametrosMensaje = new HashMap<>();
                    parametrosMensaje.put("IdRecaudacion", fila.idRecaudacion().toString());
                    msg.id = fila.idRecaudacion();
                    msg.tipo = MensajeLegacy.ERROR;
                    msg.codigo = "UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO";
                    msg.descripcion = diccionario.mensaje(in.idEmpresa(), "UI_DEPOSITO_CON_ESTADO_NO_PERMITIDO", parametrosMensaje);
                    salida.add(msg);
                }
            } else {
                msg.id = fila.idRecaudacion();
                msg.tipo = "ERROR";
                msg.codigo = "ERROR";
                msg.descripcion = diccionario.mensaje(in.idEmpresa(), "UI_SELECCIONE_DEPOSITO", valoresDiccionario);
                salida.add(msg);
            }
        }
        // LEGACY DEFECT kept (see README APV-C ConciliarDeposito): in masivo mode the logic forces the counter to 0 (so the log is written) but call recomputes it from the messages
        long contadorLogica = NO.equals(in.esProcesoMasivo()) ? MensajeLegacy.contarErrores(salida) : 0L;
        if (contadorLogica == 0L) {
            agregarLog(in);
        }
        return salida;
    }

    private void conciliadoOk(ConciliarDepositoEntrada in, Recaudacion fila, MensajeLegacy msg, HashMap<String, String> valoresDiccionario,
            List<MensajeLegacy> salida) throws Exception {
        msg.id = fila.idRecaudacion();
        msg.tipo = "INFORMACION";
        msg.codigo = "CONCILIADO_OK";
        msg.descripcion = diccionario.mensaje(in.idEmpresa(), "UI_DEPOSITO_CONCILIADO_CORRECTAMENTE", valoresDiccionario);
        salida.add(msg);
        if ("S".equals(in.asignacionAutomaticaContrato())) {
            salida.addAll(asignaContrato(in, fila.idRecaudacion()));
        }
    }

    private void error(ConciliarDepositoEntrada in, Recaudacion fila, MensajeLegacy msg, List<MensajeLegacy> salida, boolean noMasivo, String codigoUi,
            String codigoMasivo, HashMap<String, String> valoresDiccionario) {
        msg.id = fila.idRecaudacion();
        msg.tipo = "ERROR";
        msg.codigo = "ERROR";
        msg.descripcion = noMasivo ? diccionario.mensaje(in.idEmpresa(), codigoUi, valoresDiccionario)
                : diccionario.mensaje(in.idEmpresa(), codigoMasivo, valoresDiccionario);
        salida.add(msg);
    }

    private List<MensajeLegacy> asignaContrato(ConciliarDepositoEntrada in, Long idRecaudacion) {
        List<MensajeLegacy> outAsignaContrato = new ArrayList<>();
        List<ParametrosAsignacionContratoRecaudacion> contratosAAsignar = new ArrayList<>();
        List<Long> idsRecaudacion = new ArrayList<>();
        idsRecaudacion.add(idRecaudacion);

        BuscarContratosParaRecaudacionResultado salidaBusqueda = buscarContratosParaRecaudacion
                .ejecutar(new BuscarContratosParaRecaudacionEntrada(in.idEmpresa(), in.idUsuario(), in.idModulo(), idsRecaudacion));

        for (AsignarContratoRecaudacion asignacionContrato : salidaBusqueda.resultado()) {
            if (asignacionContrato.idContratoSAAPV() > 0) {
                contratosAAsignar.add(new ParametrosAsignacionContratoRecaudacion(asignacionContrato.idRecaudacionCliente(),
                        asignacionContrato.idContratoSAAPV(), asignacionContrato.idRecaudacion()));
            }
        }

        if (contratosAAsignar.size() > 0) {
            AsignarContratoARecaudacionEntrada defecto = AsignarContratoARecaudacionEntrada.porDefecto();
            AsignarContratoARecaudacionResultado salidaAsignacion = asignarContratoARecaudacion.ejecutar(new AsignarContratoARecaudacionEntrada(
                    in.idEmpresa(), in.idUsuario(), in.idModulo(), defecto.origen(), defecto.simulacion(), new ArrayList<>(contratosAAsignar)));
            for (Mensaje m : salidaAsignacion.mensajes()) {
                outAsignaContrato.add(MensajeLegacy.de(m));
            }
        }
        return outAsignaContrato;
    }

    private void agregarLog(ConciliarDepositoEntrada in) throws Exception {
        String fechaSistema = Plataforma.fechaSistema(configuracion, in.idEmpresa());
        InAgregarLog entradaAgregarLog = new InAgregarLog();

        if (!in.parametros().isEmpty()) {
            List<Recaudacion> parametros = new ArrayList<>(in.parametros());
            parametros.sort(Comparator.comparingLong(recaudacion -> recaudacion.idRecaudacion()));

            if (SI.equalsIgnoreCase(in.esProcesoMasivo())) {
                ConciliarAgregarLog.InAgregarLog.Log log = new ConciliarAgregarLog.InAgregarLog.Log();
                // the log id is random (the cases mask IdTrabajo)
                Long idTrabajo = Double.valueOf(Math.random() * 10000000000L).longValue();
                log.IdCabeceraLog = 1L;
                log.IdTrabajo = idTrabajo;
                log.IdTipoLog = "INFORMATIVO";
                log.IdModulo = in.idModulo();
                log.IdUsuario = in.idUsuario();
                log.IdEmpresa = in.idEmpresa();
                log.FechaSistemaLog = fechaSistema;
                log.IdUsuarioAtendido = in.idUsuario();
                log.IdTipoRegistro = "CONCILIACION";
                log.NombreProceso = "Conciliar Deposito";
                entradaAgregarLog.p_Log.add(log);

                Long folioDesde = parametros.get(0).idRecaudacion();
                Long folioHasta = parametros.get(parametros.size() - 1).idRecaudacion();
                entradaAgregarLog.agregarDetalleLogBasico(1L, idTrabajo, "Folio Recaudación", folioDesde.toString(), folioHasta.toString(),
                        "Rango Conciliación Masiva");
            } else {
                List<Recaudacion> ordenadas = new ArrayList<>();
                for (Recaudacion fila : parametros) {
                    if (fila.idRecaudacion() != null && fila.idRecaudacion().compareTo(0L) > 0) {
                        ordenadas.add(fila);
                    }
                }
                ordenadas.sort(Comparator.comparingLong(fila -> fila.idRecaudacion()));
                for (Recaudacion fila : ordenadas) {
                    ConciliarAgregarLog.InAgregarLog.Log log = new ConciliarAgregarLog.InAgregarLog.Log();
                    log.IdCabeceraLog = fila.idRecaudacion();
                    log.IdTrabajo = fila.idRecaudacion();
                    log.IdTipoLog = "INFORMATIVO";
                    log.IdModulo = in.idModulo();
                    log.IdUsuario = in.idUsuario();
                    log.IdEmpresa = in.idEmpresa();
                    log.FechaSistemaLog = fechaSistema;
                    log.IdUsuarioAtendido = in.idUsuario();
                    log.IdTipoRegistro = "CONCILIACION";
                    log.NombreProceso = "Conciliar Deposito";
                    entradaAgregarLog.p_Log.add(log);
                    String subRegistro = String.format("Folio Recaudación %s", fila.idRecaudacion().toString());
                    entradaAgregarLog.agregarDetalleLogBasico(fila.idRecaudacion(), fila.idRecaudacion(), "IdEstado", "", "CON", subRegistro);
                }
            }

            if (!entradaAgregarLog.p_LogDetalle.isEmpty()) {
                // the legacy ignores the answer of AgregarLog
                colaboradores.invocar("AgregarLog", Colab.aJson(entradaAgregarLog));
            }
        }
    }
}
