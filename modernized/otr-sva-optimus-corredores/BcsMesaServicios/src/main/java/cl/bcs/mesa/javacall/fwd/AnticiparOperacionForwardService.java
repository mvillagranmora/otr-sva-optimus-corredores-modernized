package cl.bcs.mesa.javacall.fwd;

import cl.bcs.mesa.javacall.JavaCallInfra;
import cl.bcs.mesa.javacall.JavaCallService;
import cl.bcs.mesa.javacall.PlataformaBridge;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnticiparOperacionForwardService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AnticiparOperacionForwardService.class);
    private static final String SQL_ANTICIPO_PARCIAL = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarAnticipoCompletoOParcial.sql");
    private static final String SQL_FP_ENTREGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoEntregaFisica.sql");
    private static final String SQL_FP_PARTE_ANTICIPADA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoParteAnticipada.sql");
    private static final String SQL_FP_PARTE_VIGENTE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarFormaDePagoParteVigente.sql");
    private static final String SQL_OP_ENTREGA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarOperacionesForwardEntregaFisica.sql");
    private static final String SQL_PARAM_MESA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_ConsultarParametrosMesaForward.sql");
    private static final String SQL_DEL_RESERVA_FOLIO = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_DeleteReservaFolioCarteraOrigen.sql");
    private static final String SQL_DEL_RESERVA_OP = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_DeleteReservaIdOperacion.sql");
    private static final String SQL_UPD_CARTERA = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateCarteraAnticipoEntregaFisica.sql");
    private static final String SQL_UPD_COMPROBANTE = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateComprobantePagoOperacionEntregaFisica.sql");
    private static final String SQL_UPD_SPOT = JavaCallInfra.loadSql("cl/bcs/mesa/FWD/sql/AnticiparOperacionForward_UpdateOperacionesSpotFolioOrigenFw.sql");
    private static final Gson GSON = new Gson();

    private final DataSource dataSource;
    private final PlataformaBridge plataforma;

    public AnticiparOperacionForwardService(DataSource dataSource, PlataformaBridge plataforma) {
        this.dataSource = dataSource;
        this.plataforma = plataforma;
    }

    @Override public String fqn() { return "cl.bcs.mesa.FWD.AnticiparOperacionForward"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        try (Connection conn = dataSource.getConnection()) {
            Entrada in = GSON.fromJson(params, Entrada.class);

            JsonObject reqResult = plataforma.callRevisarRequisitosBasicos(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo);
            if (reqResult.has("P_OUT_ERRORESCONTADOR") && reqResult.get("P_OUT_ERRORESCONTADOR").getAsLong() > 0) {
                return reqResult;
            }

            Map<String, Object> p = new HashMap<>();
            p.put("IdEmpresa", in.p_IdEmpresa);
            p.put("IdOperacion", in.p_IdOperacion);
            p.put("IdPortafolio", in.p_IdPortafolio);

            Salida out = new Salida();
            out.P_OUT_ANTICIPO_PARCIAL = JavaCallInfra.query(conn, SQL_ANTICIPO_PARCIAL, p, JavaCallInfra.fieldMapper(AnticipoInfo.class));
            out.P_OUT_FP_ENTREGA = JavaCallInfra.query(conn, SQL_FP_ENTREGA, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_ANTICIPO = JavaCallInfra.query(conn, SQL_FP_PARTE_ANTICIPADA, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_FP_VIGENTE = JavaCallInfra.query(conn, SQL_FP_PARTE_VIGENTE, p, JavaCallInfra.fieldMapper(FormaPago.class));
            out.P_OUT_OP_ENTREGA = JavaCallInfra.query(conn, SQL_OP_ENTREGA, p, JavaCallInfra.fieldMapper(OperacionFwd.class));
            out.P_OUT_PARAMETROS = JavaCallInfra.query(conn, SQL_PARAM_MESA, p, JavaCallInfra.fieldMapper(Parametro.class));

            var jdbc = new NamedParameterJdbcTemplate(new SingleConnectionDataSource(conn, true));
            var pUpd = new MapSqlParameterSource(p).addValue("IdUsuario", in.p_IdUsuario);
            jdbc.update(SQL_DEL_RESERVA_FOLIO, pUpd);
            jdbc.update(SQL_DEL_RESERVA_OP, pUpd);
            jdbc.update(SQL_UPD_CARTERA, pUpd);
            jdbc.update(SQL_UPD_COMPROBANTE, pUpd);
            jdbc.update(SQL_UPD_SPOT, pUpd);

            plataforma.callAgregarLog(in.p_IdEmpresa, in.p_IdUsuario, in.p_IdModulo, "INFO",
                    "AnticiparOperacionForward op=" + in.p_IdOperacion);
            return GSON.fromJson(GSON.toJson(out), JsonObject.class);
        } catch (Exception e) {
            log.error("AnticiparOperacionForward failed", e);
            JsonObject err = new JsonObject(); err.addProperty("P_OUT_ERRORESCONTADOR", 1L); err.addProperty("error", e.getMessage()); return err;
        }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario; public String p_IdModulo;
        public Long p_IdOperacion; public Long p_IdPortafolio; public BigDecimal p_MontoAnticipo;
        public String p_FechaAnticipo;
    }
    public static class AnticipoInfo { public Boolean Completo; public BigDecimal MontoAnticipado; public BigDecimal MontoTotal; }
    public static class FormaPago { public Long IdFormaPago; public String Descripcion; public String IdMoneda; }
    public static class OperacionFwd { public Long IdOperacion; public String IdEstado; public BigDecimal MontoCartera; }
    public static class Parametro { public String IdParametro; public String Valor; }
    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<AnticipoInfo> P_OUT_ANTICIPO_PARCIAL = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_ENTREGA = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_ANTICIPO = new ArrayList<>();
        public List<FormaPago> P_OUT_FP_VIGENTE = new ArrayList<>();
        public List<OperacionFwd> P_OUT_OP_ENTREGA = new ArrayList<>();
        public List<Parametro> P_OUT_PARAMETROS = new ArrayList<>();
    }
}
