package cl.bcs.cuentainversion.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.cuentainversion.port.out.ConfiguracionSistemaPort;
import cl.bcs.cuentainversion.port.out.SecuenciasPort;
import cl.bcs.cuentainversion.service.CtaJavaCallInfra.MensajeSalida;
import cl.bcs.utils.Utils;

/**
 * Registers an uploaded bulk-load file: one header per file (state ING, concept of the load type) and one detail row per
 * record (state OK), README CTA-A-113..119. Any failure is reported as a generic error naming the number of records written
 * so far.
 */
@Service
public class CrearCargaMasivaService {

    private static final Logger log = LoggerFactory.getLogger(CrearCargaMasivaService.class);

    private static final String SQL_INSERT_CABECERA = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargaMasiva_InsertCreaCabecera.sql");
    private static final String SQL_INSERT_DETALLE = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargaMasiva_InsertCreaDetalle.sql");
    private static final String SQL_ID_CONCEPTO = CtaJavaCallInfra.loadSql("cl/bcs/cuentainversion/sql/CrearCargaMasiva_SelectIdConcepto.sql");
    private static final String SECUENCIA_CABECERA = "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasiva";
    private static final String SECUENCIA_DETALLE = "BSCL.CuentaInversion.BD.Secuencias::CAR.CargaMasivaDetalle";

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public String p_FechaCarga;
        public String p_TipoCargaSeleccionada;
        public List<Archivo> p_CargaMasiva;
        public List<Registro> p_CargaMasivaDetalle;
    }

    public static class Archivo {
        public Long IdInterno;
        public String CodigoTipoCargaMasiva;
        public String NombreArchivo;
    }

    public static class Registro {
        public Long IdInterno;
        public Long IdRegistro;
        public String RutCliente;
        public String Portafolio;
        public String Moneda;
        public String TipoMovimiento;
        public String IdConcepto;
        public BigDecimal Monto;
        public String Descripcion;
        public String Plan;
        public String ProductoAgrupador;
        public String FechaGeneracion;
        public String FechaInicioServ;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final SecuenciasPort secuencias;
    private final ConfiguracionSistemaPort configuracion;

    public CrearCargaMasivaService(NamedParameterJdbcTemplate jdbc, SecuenciasPort secuencias, ConfiguracionSistemaPort configuracion) {
        this.jdbc = jdbc;
        this.secuencias = secuencias;
        this.configuracion = configuracion;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        Entrada entrada = Utils.getGsonBuilder().fromJson(params, Entrada.class);
        Salida salida = cargar(entrada);
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.countErrors(salida.P_OUT_MENSAJES);
        return Utils.getGsonBuilder().toJsonTree(salida).getAsJsonObject();
    }

    private Salida cargar(Entrada entrada) {
        Salida salida = new Salida();
        long registrados = 0;
        try {
            for (Registro r : entrada.p_CargaMasivaDetalle) {
                if (!r.TipoMovimiento.equals(entrada.p_TipoCargaSeleccionada)) {
                    salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, "UI_PROCESO_EJECUTADO_ERROR",
                            "Existe un tipo movimiento que no corresponde al tipo de carga seleccionado"));
                    return salida;
                }
            }
            String fechaSistema = CtaJavaCallInfra.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.FechaSistema");

            // LEGACY DEFECT kept (see README CTA-A-116): every header takes the FIRST value of the sequence, so two files collide
            Long idCarga = secuencias.siguientes(SECUENCIA_CABECERA, (long) entrada.p_CargaMasiva.size()).get(0);
            List<SqlParameterSource> cabeceras = new ArrayList<>();
            for (Archivo archivo : entrada.p_CargaMasiva) {
                List<String> conceptos = jdbc.queryForList(SQL_ID_CONCEPTO,
                        new MapSqlParameterSource("p_CodigoTipoCargaMasiva", archivo.CodigoTipoCargaMasiva), String.class);
                cabeceras.add(new MapSqlParameterSource()
                        .addValue("IdCargaMasiva", idCarga)
                        .addValue("CodigoTipoCargaMasiva", archivo.CodigoTipoCargaMasiva)
                        .addValue("IdEmpresa", entrada.p_IdEmpresa)
                        .addValue("IdUsuario", entrada.p_IdUsuario)
                        .addValue("CantidadRegistros", entrada.p_CargaMasivaDetalle.size())
                        .addValue("MontoTotal", CtaJavaCallInfra.escala16(BigDecimal.ZERO)) // several currencies: left at zero
                        .addValue("NombreArchivo", archivo.NombreArchivo)
                        .addValue("FechaCarga", entrada.p_FechaCarga)
                        .addValue("FechaProceso", fechaSistema)
                        .addValue("IdMoneda", "")
                        .addValue("IdConcepto", conceptos.isEmpty() ? null : conceptos.get(0)));
            }
            jdbc.batchUpdate(SQL_INSERT_CABECERA, cabeceras.toArray(SqlParameterSource[]::new));

            List<SqlParameterSource> detalles = new ArrayList<>();
            for (Registro r : entrada.p_CargaMasivaDetalle) {
                Long idDetalle = secuencias.siguientes(SECUENCIA_DETALLE, 1L).get(0);
                detalles.add(new MapSqlParameterSource()
                        .addValue("IdCargaMasivaDetalle", idDetalle)
                        .addValue("IdCargaMasiva", idCarga)
                        .addValue("DocumentoIdentidad", r.RutCliente)
                        .addValue("IdPortafolio", r.Portafolio)
                        .addValue("IdMoneda", r.Moneda)
                        .addValue("IdTipoMovimiento", r.TipoMovimiento)
                        .addValue("IdConcepto", r.IdConcepto)
                        .addValue("Monto", CtaJavaCallInfra.escala16(r.Monto))
                        .addValue("Descripcion", r.Descripcion)
                        .addValue("Plan", r.Plan)
                        .addValue("ProductoAgrupador", r.ProductoAgrupador)
                        .addValue("IdEstado", "OK")
                        .addValue("ObservacionValidacion", ""));
                registrados++;
            }
            jdbc.batchUpdate(SQL_INSERT_DETALLE, detalles.toArray(SqlParameterSource[]::new));
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.OK, "UI_PROCESO_EJECUTADO_EXITO",
                    "Archivo Cargado con exito, se cargaron " + registrados + " registros."));
        } catch (Exception e) {
            // LEGACY DEFECT kept (see README CTA-A-115, 117): any exception, whatever its cause, becomes this generic message
            log.warn("CrearCargaMasiva failed after {} records", registrados, e);
            salida.P_OUT_MENSAJES.add(new MensajeSalida(1L, MensajeSalida.ERROR, "UI_PROCESO_EJECUTADO_ERROR",
                    "Archivo presenta problemas en la linea : " + registrados));
        }
        return salida;
    }
}
