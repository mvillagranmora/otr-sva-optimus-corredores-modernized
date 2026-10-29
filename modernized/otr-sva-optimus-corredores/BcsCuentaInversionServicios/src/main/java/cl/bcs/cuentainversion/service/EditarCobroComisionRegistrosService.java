package cl.bcs.cuentainversion.service;

import cl.bcs.cuentainversion.reportes.ConfiguracionSistema;
import cl.bcs.cuentainversion.reportes.JsonReportes;
import cl.bcs.cuentainversion.reportes.MensajeSalida;
import cl.bcs.cuentainversion.reportes.Mensajeria;
import cl.bcs.cuentainversion.reportes.SqlReportes;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Edits the commission of one portfolio and period in the commission blotter (legacy {@code EditarCobroComisionRegistros}).
 * Behaviours CTA-B-32..39 and CTA-B-45.
 *
 * <p>Only a closed period can be edited (the system date must be after the period end), only for a VIGENTE record, and never a
 * record whose edition was already processed (PRO). A first edition inserts the edited-commission row (next sequence value, system
 * date); a later one, including the re-activation of a deleted (INA) edition, updates amount and date and leaves it VIGENTE.
 */
@Service
public class EditarCobroComisionRegistrosService {

    private static final Logger log = LoggerFactory.getLogger(EditarCobroComisionRegistrosService.class);

    private static final String SQL_PERIODO = SqlReportes.recurso("CobroComision_ConsultarPeriodos");
    private static final String SQL_EDITADO = SqlReportes.recurso("CobroComision_ConsultarEditados");
    private static final String SQL_INSERT_EDITADO = SqlReportes.recurso("CobroComision_AgregarEditado");
    private static final String SQL_MODIFICAR_EDITADO = SqlReportes.recurso("CobroComision_ModificarEditado");
    private static final String SECUENCIA = "BSCL.CuentaInversion.BD.Secuencias::RegistrosEditadosComisiones";

    private final SqlReportes sql;

    public EditarCobroComisionRegistrosService(DataSource dataSource) {
        this.sql = new SqlReportes(new NamedParameterJdbcTemplate(dataSource));
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject execute(JsonObject params) throws Exception {
        return JsonReportes.escribir(editar(JsonReportes.leer(params, Entrada.class)));
    }

    Salida editar(Entrada in) throws Exception {
        log.info("INICIO::EditarCobroComisionRegistros");
        Salida salida = new Salida();
        Mensajeria mensajes = new Mensajeria(sql, in.p_IdEmpresa);
        String fechaSistema = new ConfiguracionSistema(sql).fechaSistema();
        Date fechaSis = java.sql.Date.valueOf(fechaSistema);

        List<Periodo> periodos = sql.consultar(SQL_PERIODO, Map.of("IdPeriodo", in.p_Periodo), Periodo.class);
        if (periodos.isEmpty()) {
            return rechazar(salida, mensajes, "UI_NO_EXISTE_PERIODO");
        }
        if (!fechaSis.after(periodos.get(0).FechaHasta)) {
            return rechazar(salida, mensajes, "UI_FUERA_DE_PERIODO");
        }
        if (!in.p_EstadoComision.equals("VIG")) {
            return rechazar(salida, mensajes, "UI_REGISTRO_DISTINTO_VIG");
        }

        List<Editado> registro = sql.consultar(SQL_EDITADO, Map.of("Periodo", in.p_Periodo, "IdPortafolio", in.p_IdPortafolio), Editado.class);
        if (registro.isEmpty()) {
            Long idRegistro = sql.secuencia(SECUENCIA, 1L).get(0);
            AgregarEditado nuevo = new AgregarEditado();
            nuevo.IdRegistro = idRegistro;
            nuevo.IdPortafolio = in.p_IdPortafolio;
            nuevo.Periodo = in.p_Periodo;
            nuevo.MontoComision = in.p_MontoComision;
            nuevo.FechaModificacion = fechaSistema;
            nuevo.EstadoComision = in.p_EstadoComision;
            sql.lote(SQL_INSERT_EDITADO, List.of(nuevo));
        } else {
            Editado existente = registro.get(0);
            if (existente.EstadoComision.equals("PRO")) {
                return rechazar(salida, mensajes, "UI_REGISTRO_EDITADO_PROCESADO");
            }
            // An INA edition (deleted and then edited again) takes the same update as a VIGENTE one: the update sets the state to VIG.
            ModificarEditado cambio = new ModificarEditado();
            cambio.IdRegistro = existente.IdRegistro;
            cambio.FechaModificacion = fechaSistema;
            cambio.MontoComision = in.p_MontoComision;
            sql.lote(SQL_MODIFICAR_EDITADO, List.of(cambio));
        }
        salida.P_OUT_MENSAJES.add(mensajes.ok("UI_REGISTRO_EDITADO_CORRECTAMENTE"));
        return salida;
    }

    private static Salida rechazar(Salida salida, Mensajeria mensajes, String codigo) {
        salida.P_OUT_MENSAJES.add(mensajes.error(codigo));
        salida.P_OUT_ERRORESCONTADOR = MensajeSalida.contarErrores(salida.P_OUT_MENSAJES);
        return salida;
    }

    // ---------------------------------------------------------------------------------------------- JSON contract

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_UsaReglasNegocio;
        public Long p_IdPortafolio;
        public BigDecimal p_MontoComision;
        public Long p_Periodo;
        public String p_EstadoComision;
    }

    public static class Periodo {
        public Date FechaDesde;
        public Date FechaHasta;
    }

    public static class Editado {
        public Long IdRegistro;
        public String EstadoComision;
        public Long IdPortafolio;
    }

    /** Parameters of the INSERT of an edited commission. */
    public static class AgregarEditado {
        public Long IdRegistro;
        public Long IdPortafolio;
        public Long Periodo;
        public BigDecimal MontoComision;
        public String FechaModificacion;
        public String EstadoComision;
    }

    /** Parameters of the UPDATE of an edited commission. */
    public static class ModificarEditado {
        public Long IdRegistro;
        public BigDecimal MontoComision;
        public String FechaModificacion;
    }

    public static class Salida {
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public Long P_OUT_ADVERTENCIASCONTADOR = 0L;
    }
}
