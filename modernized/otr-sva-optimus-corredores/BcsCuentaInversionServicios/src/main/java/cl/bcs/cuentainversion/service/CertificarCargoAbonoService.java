package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.domain.colaborador.AgregarEvento;
import cl.bcs.cuentainversion.domain.colaborador.AgregarLog;
import cl.bcs.cuentainversion.domain.colaborador.ConsultarImagen;
import cl.bcs.cuentainversion.domain.colaborador.RevisarRequisitosBasicos;
import cl.bcs.cuentainversion.domain.colaborador.SaldoFlujoCaja;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.DiccionarioMensajes;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Fecha;
import cl.bcs.utils.Utils;

/**
 * Certifies pending (PEN) cargo/abono movements (CTA-A-12..31; legacy CertificarCargoAbono): validates them (basic requirements,
 * state, transfer rules, cash flow of the portfolio), sets the settlement date and the CRT state, writes the audit log, recalculates the
 * running balances of each account and notifies the client by e-mail. A movement with an error is left out (with the other leg of its
 * transfer) and the rest are certified.
 */
@Service
public class CertificarCargoAbonoService {

    private static final Logger log = LoggerFactory.getLogger(CertificarCargoAbonoService.class);

    private static final String SQL = "cl/bcs/cuentainversion/sql/CertificarCargoAbono_";
    private static final String SQL_SELECT_A_CERTIFICAR = CtaJavaCallInfra.loadSql(SQL + "Select_Cargo_Abono_A_Certificar.sql");
    private static final String SQL_UPDATE_CERTIFICADOS = CtaJavaCallInfra.loadSql(SQL + "Update_Movimientos_Certificados.sql");
    private static final String SQL_INSERT_ID_PASO = CtaJavaCallInfra.loadSql(SQL + "Insert_IdCargoAbono_Tabla_Paso.sql");
    private static final String SQL_INFO_EXTRA_NOTIFICACION = CtaJavaCallInfra.loadSql(SQL + "Select_Info_Extra_Notificacion.sql");
    private static final String SQL_HORA_UTC = "SELECT CURRENT_UTCTIMESTAMP AS UTC FROM DUMMY";
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaMovimiento;
        public Long p_IdPais;
        public List<CargoAbonoId> p_CargosAbonosACertificar = new ArrayList<>();
    }

    public static class CargoAbonoId {
        public Long IdCargoAbonoMovimiento;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Detalle> P_OUT_ERRORESDETALLE = new ArrayList<>();
        public Long P_OUT_INFORMACIONCONTADOR = 0L;
        public List<Detalle> P_OUT_INFORMACIONDETALLE = new ArrayList<>();
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
        public List<Detalle> P_OUT_ADVERTENCIASDETALLE = new ArrayList<>();
    }

    public static class Detalle {
        public Long IdCargoAbonoMovimiento;
        public String Tipo;
        public String Codigo;
        public String Descripcion;

        Detalle(Long id, String tipo, String codigo, String descripcion) {
            this.IdCargoAbonoMovimiento = id;
            this.Tipo = tipo;
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    /** One movement to certify with the client data the validations and the notification need. */
    public static class CargoAbono {
        public Integer IdSesion;
        public Long IdInterno;
        public Long IdSecuencia;
        public Long IdCuentaInversion;
        public Long IdPortafolio;
        public String IdMoneda;
        public Long IdCargoAbonoMovimiento;
        public String IdCondicionDeLiquidacion;
        public int PlazoEnDias;
        public Date FechaEfectivaMovimiento;
        public Date FechaSistema;
        public Timestamp FechaIngresoRegistro;
        public String FechaCalculada;
        public String IdTipoMovimiento;
        public String EsCargoAbonoPorTraspaso;
        public Long IdCargoAbonoRelacionadoTraspaso;
        public BigDecimal Monto;
        public String IdEstado;
        public String Informado;
        public String CodigoPortafolio;
        public String NombreCompleto;
        public Long IdPersona;
        public String DocumentoIdentidad;
        public Long IdDireccionElectronicaPrincipal;
        public String IdTipoCliente;
        public String TelCliente;
        public String TipoDocumento;
        public String NombreOperacion;
        public String NombreConcepto;
        public String Correo;
        public String Agente;
        public Long Errores = 0L;
        public Long Ok = 0L;
        public BigDecimal TotalMontoPortafolio = BigDecimal.ZERO;
        public BigDecimal SaldoCajaPortafolio = BigDecimal.ZERO;
    }

    public static class InfoExtraNotificacion {
        public Long IdPersona;
        public Long IdDireccionElectronica;
        public String RazonCorredora;
        public String DireccionCorredora;
        public String RutCorredora;
        public String TelefonoCorredora;
    }

    /** Cash-flow bucket: currency, date and portfolio. */
    private record LlaveFlujo(String moneda, String fecha, Long portafolio) {
        LlaveFlujo {
            moneda = moneda == null ? null : moneda.toLowerCase();
            fecha = fecha == null ? null : fecha.toLowerCase();
        }
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ActualizarSaldoCargoAbonoService actualizarSaldo;
    private final CalendarioHabil calendario;
    private final FormatoMoneda formatoMoneda;
    private final ConfiguracionSistemaPort configuracion;
    private final DiccionarioMensajes diccionario;
    private final Colaboradores colaboradores;

    public CertificarCargoAbonoService(NamedParameterJdbcTemplate jdbc, ActualizarSaldoCargoAbonoService actualizarSaldo,
                                       CalendarioHabil calendario, FormatoMoneda formatoMoneda, ConfiguracionSistemaPort configuracion,
                                       DiccionarioMensajes diccionario, Colaboradores colaboradores) {
        this.jdbc = jdbc;
        this.actualizarSaldo = actualizarSaldo;
        this.calendario = calendario;
        this.formatoMoneda = formatoMoneda;
        this.configuracion = configuracion;
        this.diccionario = diccionario;
        this.colaboradores = colaboradores;
    }

    @Transactional
    public JsonObject execute(JsonObject params) throws Exception {
        Gson gson = Utils.getGsonBuilder();
        Salida salida = certificar(gson.fromJson(params, Entrada.class));
        // LEGACY DEFECT kept (see README CTA-A-12): the JavaCall entry point forces the error counter to 0, so it never reflects the errors
        salida.P_OUT_ERRORESCONTADOR = 0L;
        return gson.toJsonTree(salida).getAsJsonObject();
    }

    Salida certificar(Entrada entrada) throws Exception {
        Salida out = new Salida();
        boolean validaFlujoCaja = configuracion.validaFlujoCajaEnCertificacion(entrada.p_IdEmpresa);
        try {
            jdbc.batchUpdate(SQL_INSERT_ID_PASO, entrada.p_CargosAbonosACertificar.stream()
                    .map(c -> (SqlParameterSource) new MapSqlParameterSource("IdCargoAbonoMovimiento", c.IdCargoAbonoMovimiento))
                    .toArray(SqlParameterSource[]::new));
            List<CargoAbono> cargosAbonos = new ArrayList<>(jdbc.query(SQL_SELECT_A_CERTIFICAR,
                    new MapSqlParameterSource("p_IdEmpresa", entrada.p_IdEmpresa), CtaJavaCallInfra.fieldMapper(CargoAbono.class)));
            if (cargosAbonos.isEmpty()) {
                out.P_OUT_ERRORESDETALLE.add(new Detalle(0L, MensajeSalida.ERROR, "NO_HAY_MOVIMIENTOS", "No se encontraron Movimientos"));
                return out;
            }
            if ("S".equalsIgnoreCase(entrada.p_UsaReglasNegocio)) {
                if (!validar(entrada, out, cargosAbonos, validaFlujoCaja)) {
                    return out;
                }
            }
            retirarConErrores(cargosAbonos);
            if (cargosAbonos.isEmpty()) {
                return out;
            }
            String fechaHoraSistema = fechaHoraSistemaUtc(entrada.p_IdEmpresa);
            actualizarMovimientos(entrada, out, cargosAbonos, fechaHoraSistema);
            registrarLog(entrada, cargosAbonos);
            for (CargoAbono c : cargosAbonos) {
                ActualizarSaldoCargoAbonoService.Entrada saldo = new ActualizarSaldoCargoAbonoService.Entrada();
                saldo.p_IdEmpresa = entrada.p_IdEmpresa;
                saldo.p_IdUsuario = entrada.p_IdUsuario;
                saldo.p_FechaMovimiento = entrada.p_FechaMovimiento;
                ActualizarSaldoCargoAbonoService.CuentaInversion cuenta = new ActualizarSaldoCargoAbonoService.CuentaInversion();
                cuenta.IdCuentaInversion = c.IdCuentaInversion;
                saldo.p_CuentaInversion.add(cuenta);
                actualizarSaldo.actualizar(saldo);
            }
            if (out.P_OUT_ERRORESCONTADOR == 0) {
                notificar(entrada, out, cargosAbonos, fechaHoraSistema);
            }
            return out;
        } catch (Exception e) {
            log.error("CertificarCargoAbono", e);
            throw e;
        }
    }

    /** Runs the business-rule validations; false when the basic requirements already fail (answer is final). */
    private boolean validar(Entrada entrada, Salida out, List<CargoAbono> cargosAbonos, boolean validaFlujoCaja) throws Exception {
        RevisarRequisitosBasicos.Salida basicos = colaboradores.invocar(RevisarRequisitosBasicos.CLASE,
                RevisarRequisitosBasicos.entrada(entrada.p_IdEmpresa, entrada.p_IdUsuario, entrada.p_IdModulo), RevisarRequisitosBasicos.Salida.class);
        if (basicos.p_out_Mensajes.stream().anyMatch(m -> MensajeSalida.ERROR.equals(m.tipo))) {
            for (MensajeSalida m : basicos.p_out_Mensajes) {
                out.P_OUT_ERRORESCONTADOR++;
                out.P_OUT_ERRORESDETALLE.add(new Detalle(0L, m.tipo, m.codigo, m.descripcion));
            }
            return false;
        }
        for (CargoAbono c : cargosAbonos) {
            requisito(entrada, c.FechaSistema == null, "FECHA_SISTEMA_INVALIDA", c, out);
            requisito(entrada, c.IdCargoAbonoMovimiento == null, "MOVIMIENTO_CARGO_ABONO_NO_EXISTE", c, out);
            requisito(entrada, c.IdEstado.equals("CRT"), "MOVIMIENTO_CARGO_ABONO_CERTIFICADO", c, out);
            requisito(entrada, c.IdEstado.equals("ANU"), "MOVIMIENTO_CARGO_ABONO_ANULADO", c, out);
            requisito(entrada, c.IdTipoMovimiento.equals("MOVABN") && c.EsCargoAbonoPorTraspaso.equals("S") && c.Informado.equals("S"),
                    "MOVIMIENTO_ABONO_ES_PARTE_TRANSFERENCIA", c, out);
            // LEGACY DEFECT kept (see README CTA-A-13): PlazoEnDias is never mapped (the SQL column is not aliased), so it is 0 and
            // the calculated date is the movement date
            c.FechaCalculada = calendario.siguiente(Fecha.toDate(entrada.p_FechaMovimiento).toLocalDate(), (long) c.PlazoEnDias,
                    entrada.p_IdPais, true, true, false).fecha().toString();
        }
        if (validaFlujoCaja) {
            validarFlujoCaja(entrada, out, cargosAbonos);
        }
        return true;
    }

    private void validarFlujoCaja(Entrada entrada, Salida out, List<CargoAbono> cargosAbonos) {
        Map<LlaveFlujo, BigDecimal> totalCargos = new HashMap<>();
        Map<LlaveFlujo, BigDecimal> totalFlujoCaja = new HashMap<>();
        // LEGACY DEFECT kept (see README CTA-A-21): the legacy HashSet never removes duplicate portfolios (identity equality)
        List<SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR> portafolios = new ArrayList<>();
        Date minima = Fecha.toDate("2099-09-09");
        Date maxima = Fecha.toDate("0001-01-01");
        for (CargoAbono c : cargosAbonos) {
            if (c.IdTipoMovimiento.equalsIgnoreCase("MOVCAR")) {
                Date calculada = Fecha.toDate(c.FechaCalculada);
                if (calculada.before(minima)) {
                    minima = calculada;
                }
                if (calculada.after(maxima)) {
                    maxima = calculada;
                }
                totalCargos.merge(new LlaveFlujo(c.IdMoneda, c.FechaCalculada, c.IdPortafolio), c.Monto, BigDecimal::add);
                SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR p = new SaldoFlujoCaja.ObjPORTAFOLIOACONSULTAR();
                p.IdMoneda = c.IdMoneda;
                p.IdPortafolio = c.IdPortafolio;
                portafolios.add(p);
            }
        }
        if (!portafolios.isEmpty()) {
            SaldoFlujoCaja.ServiceEntrada flujo = new SaldoFlujoCaja.ServiceEntrada();
            flujo.P_IDEMPRESA = entrada.p_IdEmpresa;
            flujo.P_FECHADESDE = Fecha.toStringDate(minima);
            flujo.P_FECHAHASTA = Fecha.toStringDate(maxima);
            flujo.P_USO = "CUENTA_INVERSION";
            flujo.P_PORTAFOLIOACONSULTAR = portafolios;
            SaldoFlujoCaja.ServiceSalida salida = colaboradores.invocar(SaldoFlujoCaja.CLASE, flujo, SaldoFlujoCaja.ServiceSalida.class);
            for (SaldoFlujoCaja.ObjFLUJOCAJA f : salida.P_OUT_FLUJOCAJA) {
                totalFlujoCaja.merge(new LlaveFlujo(f.IdMoneda, f.FechaSaldo, f.IdPortafolio), f.Saldo, BigDecimal::add);
            }
        }
        for (CargoAbono c : cargosAbonos) {
            LlaveFlujo llave = new LlaveFlujo(c.IdMoneda, c.FechaCalculada, c.IdPortafolio);
            if (totalCargos.get(llave) != null) {
                c.TotalMontoPortafolio = totalCargos.get(llave);
            }
            if (totalFlujoCaja.get(llave) != null) {
                c.SaldoCajaPortafolio = totalFlujoCaja.get(llave);
            }
        }
        for (CargoAbono c : cargosAbonos) {
            // LEGACY DEFECT kept (see README CTA-A-23): the comparison goes through doubleValue, so a 1e-16 excess is accepted
            requisito(entrada, c.IdTipoMovimiento.equalsIgnoreCase("MOVCAR")
                    && c.TotalMontoPortafolio.doubleValue() > c.SaldoCajaPortafolio.doubleValue(), "FLUJO_CAJA_INSUFICIENTE_PORTAFOLIO", c, out);
        }
    }

    private void requisito(Entrada entrada, boolean incumple, String codigo, CargoAbono c, Salida out) {
        if (incumple) {
            String mensaje = diccionario.traducir(entrada.p_IdEmpresa, codigo,
                    DiccionarioMensajes.valores("IdCargoAbonoMovimiento", c.IdCargoAbonoMovimiento, "DocumentoIdentidad", c.DocumentoIdentidad));
            // LEGACY DEFECT kept (see README CTA-A-16): the detail carries IdInterno, which is never filled, so its id is null
            out.P_OUT_ERRORESDETALLE.add(new Detalle(c.IdInterno, MensajeSalida.ERROR, codigo, mensaje));
            out.P_OUT_ERRORESCONTADOR++;
            c.Errores++;
        }
    }

    /** Takes out the movements with errors and, for a transfer, the movement on the other leg. */
    private static void retirarConErrores(List<CargoAbono> cargosAbonos) throws Exception {
        List<CargoAbono> conErrores = new ArrayList<>();
        for (CargoAbono c : cargosAbonos) {
            if (c.Errores > 0) {
                conErrores.add(c);
            }
        }
        // LEGACY DEFECT kept (see README CTA-A-20): both legs of a transfer listed -> "key field is not unique"
        HashMap<Long, Object> porId = Utils.List2Hash(cargosAbonos, "IdCargoAbonoMovimiento");
        for (CargoAbono c : conErrores) {
            cargosAbonos.remove(c);
            if (c.EsCargoAbonoPorTraspaso.equalsIgnoreCase("S") && c.IdCargoAbonoRelacionadoTraspaso.compareTo(0L) > 0) {
                cargosAbonos.remove(porId.get(c.IdCargoAbonoRelacionadoTraspaso));
            }
        }
    }

    /** System date plus the UTC time of the database ("yyyy-MM-dd HH:mm:ss"). */
    private String fechaHoraSistemaUtc(Long idEmpresa) throws Exception {
        String fechaSistema = CtaJavaCallInfra.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        Timestamp utc = jdbc.getJdbcTemplate().queryForObject(SQL_HORA_UTC, Timestamp.class);
        return fechaSistema + " " + LocalTime.from(utc.toLocalDateTime()).format(HORA);
    }

    private void actualizarMovimientos(Entrada entrada, Salida out, List<CargoAbono> cargosAbonos, String fechaHoraSistema) {
        List<SqlParameterSource> actualizaciones = new ArrayList<>();
        for (CargoAbono c : cargosAbonos) {
            actualizaciones.add(new MapSqlParameterSource()
                    .addValue("p_IdUsuario", entrada.p_IdUsuario)
                    .addValue("p_FechaCalculada", c.FechaCalculada)
                    .addValue("p_FechaHoraSistema", fechaHoraSistema)
                    .addValue("p_Monto", CtaJavaCallInfra.escala16(c.Monto))
                    .addValue("p_IdCargoAbonoMovimiento", c.IdCargoAbonoMovimiento)
                    .addValue("p_IdEmpresa", entrada.p_IdEmpresa));
            boolean cargo = c.IdTipoMovimiento.equalsIgnoreCase("MOVCAR");
            out.P_OUT_INFORMACIONDETALLE.add(new Detalle(c.IdCargoAbonoMovimiento, MensajeSalida.INFORMACION,
                    cargo ? "UI_CARGO_CERTIFICADO" : "UI_ABONO_CERTIFICADO",
                    (cargo ? "Cargo N°" : "Abono N°") + c.IdCargoAbonoMovimiento + " certificado"));
            out.P_OUT_INFORMACIONCONTADOR++;
        }
        jdbc.batchUpdate(SQL_UPDATE_CERTIFICADOS, actualizaciones.toArray(SqlParameterSource[]::new));
    }

    private void registrarLog(Entrada entrada, List<CargoAbono> cargosAbonos) {
        AgregarLog.InAgregarLog log = new AgregarLog.InAgregarLog();
        for (CargoAbono c : cargosAbonos) {
            AgregarLog.InAgregarLog.Log cabecera = new AgregarLog.InAgregarLog.Log();
            cabecera.NombreProceso = "Certificación Cargo Abono";
            cabecera.IdCabeceraLog = c.IdCargoAbonoMovimiento != null ? c.IdCargoAbonoMovimiento : 0L;
            cabecera.IdTipoLog = "CERTIFICACION";
            cabecera.IdModulo = entrada.p_IdModulo;
            cabecera.IdUsuario = entrada.p_IdUsuario;
            cabecera.IdEmpresa = entrada.p_IdEmpresa;
            cabecera.FechaSistemaLog = entrada.p_FechaMovimiento != null ? entrada.p_FechaMovimiento : "";
            cabecera.IdUsuarioAtendido = entrada.p_IdUsuario;
            cabecera.IdCliente = 0L;
            cabecera.Nemo = c.NombreConcepto != null ? c.NombreConcepto : "";
            cabecera.IdPortafolio = c.IdPortafolio != null ? c.IdPortafolio : 0L;
            cabecera.IdTipoOperacion = c.IdTipoMovimiento != null ? c.IdTipoMovimiento : "";
            cabecera.Folio = c.IdCargoAbonoMovimiento != null ? c.IdCargoAbonoMovimiento : 0L;
            cabecera.Monto = c.Monto != null ? c.Monto : BigDecimal.ZERO;
            cabecera.IdMonedaOrigen = c.IdMoneda != null ? c.IdMoneda : "";
            if ("MOVABN".equals(c.IdTipoMovimiento)) {
                cabecera.IdTipoRegistro = "ABONO_MOVIMIENTO";
            }
            if ("MOVCAR".equals(c.IdTipoMovimiento)) {
                cabecera.IdTipoRegistro = "CARGO_MOVIMIENTO";
            }
            log.p_Log.add(cabecera);
        }
        colaboradores.invocar(AgregarLog.CLASE, log, AgregarLog.OutAgregarLog.class);
    }

    private void notificar(Entrada entrada, Salida out, List<CargoAbono> cargosAbonos, String fechaHoraSistema) {
        List<InfoExtraNotificacion> infoExtra = jdbc.query(SQL_INFO_EXTRA_NOTIFICACION, new MapSqlParameterSource()
                .addValue("IdEmpresa", entrada.p_IdEmpresa).addValue("IdUsuario", entrada.p_IdUsuario),
                CtaJavaCallInfra.fieldMapper(InfoExtraNotificacion.class));
        AgregarEvento.ServiceEntrada notificacion = new AgregarEvento.ServiceEntrada();
        notificacion.P_IDEMPRESA = entrada.p_IdEmpresa;
        notificacion.P_IDUSUARIO = entrada.p_IdUsuario;
        notificacion.P_IDMODULO = entrada.p_IdModulo;

        ConsultarImagen.ConsultarImagenEntrada imagen = new ConsultarImagen.ConsultarImagenEntrada();
        imagen.p_IdEmpresa = entrada.p_IdEmpresa;
        imagen.p_imagenNombres.add("LOGO_CORREDOR");
        ConsultarImagen.ConsultarImagenSalida logos = colaboradores.invocar(ConsultarImagen.CLASE, imagen, ConsultarImagen.ConsultarImagenSalida.class);
        String logo = logos.OUT_IMAGENES.isEmpty() ? "" : logos.OUT_IMAGENES.get(0).RutaPublica;

        for (CargoAbono c : cargosAbonos) {
            if (c.IdDireccionElectronicaPrincipal == null) {
                continue;
            }
            // LEGACY DEFECT kept (see README CTA-A-31): IndexOutOfBoundsException when the user is not an employee (no extra info row)
            InfoExtraNotificacion corredora = infoExtra.get(0);
            AgregarEvento.ObjEVENTOS evento = new AgregarEvento.ObjEVENTOS();
            evento.CodigoTipoEvento = "MOVCAJAS";
            evento.IdSecuencia = c.IdCargoAbonoMovimiento;
            evento.FechaCreacionSistema = Fecha.toStringDate(Fecha.toTimeStamp(fechaHoraSistema));
            evento.IdPersonaRemitente = corredora.IdPersona;
            evento.IdPersonaDestinatario = c.IdPersona;
            evento.IdPortafolio = c.IdPortafolio;
            notificacion.P_EVENTOS.add(evento);
            AgregarEvento.ObjIDDIRECCIONES direccion = new AgregarEvento.ObjIDDIRECCIONES();
            direccion.IdSecuencia = c.IdCargoAbonoMovimiento;
            direccion.TipoDestinatario = "TO";
            direccion.IdDireccion = Long.valueOf(c.IdDireccionElectronicaPrincipal);
            notificacion.P_IDDIRECCIONES.add(direccion);
            // LEGACY DEFECT kept (see README CTA-A-28): "dd-MM-YYYY" uses the week-year
            String fechaIngreso = Fecha.toFormat(c.FechaEfectivaMovimiento, "dd-MM-YYYY");
            Long id = c.IdCargoAbonoMovimiento;
            notificacion.addValor(id, "CLIENTE", c.NombreCompleto)
                    .addValor(id, "TIPODOCUMENTOIDENTIDAD", c.TipoDocumento)
                    .addValor(id, "CORREO", c.Correo)
                    .addValor(id, "DOCUMENTOIDENTIDAD", c.DocumentoIdentidad)
                    .addValor(id, "IDTIPOCLIENTE", c.IdTipoCliente)
                    .addValor(id, "TELEFONO", c.TelCliente)
                    .addValor(id, "CORREDORA", corredora.RazonCorredora)
                    .addValor(id, "DIRECCIONCORREDORA", corredora.DireccionCorredora)
                    .addValor(id, "RUTCORREDORA", corredora.RutCorredora)
                    .addValor(id, "ATENCIONCLIENTE", corredora.TelefonoCorredora)
                    .addValor(id, "MONTO", formatoMoneda.formatear(c.Monto, FormatoMoneda.Tipo.MONTO, c.IdMoneda).valorConFormato())
                    .addValor(id, "OPERACION", c.NombreOperacion)
                    .addValor(id, "MONEDA", c.IdMoneda)
                    .addValor(id, "FECHA", c.FechaSistema + "")
                    .addValor(id, "CONCEPTO", c.NombreConcepto)
                    .addValor(id, "IDINTERNO", id + "")
                    .addValor(id, "FECHAINGRESO", fechaIngreso)
                    .addValor(id, "PORTAFOLIO", c.CodigoPortafolio)
                    .addValor(id, "AGENTE", c.Agente)
                    .addValor(id, "LOGO_CORREDOR", logo);
        }
        if (!notificacion.P_EVENTOS.isEmpty()) {
            AgregarEvento.ServiceSalida enviadas = colaboradores.invocar(AgregarEvento.CLASE, notificacion, AgregarEvento.ServiceSalida.class);
            for (AgregarEvento.ObjIDEVENTO e : enviadas.P_OUT_IDEVENTO) {
                if (e.IdEvento == null) {
                    out.P_OUT_ADVERTENCIASCONTADOR++;
                    out.P_OUT_ADVERTENCIASDETALLE.add(new Detalle(0L, MensajeSalida.ADVERTENCIA, "Advertencia", "No se envio la notificacion"));
                }
            }
        }
    }
}
