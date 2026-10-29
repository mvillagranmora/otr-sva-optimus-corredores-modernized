package cl.bcs.contabilidad.servicios;

import cl.bcs.contabilidad.service.FiltrarCuentasService;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static cl.bcs.contabilidad.servicios.Out.*;
import static cl.bcs.contabilidad.servicios.RecordingJdbc.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Characterization of legacy cl.bcs.contabilidad.FiltrarCuentas (FiltrarCuentas.java). */
class FiltrarCuentasServiceTest {

    static final String SEL = "FiltrarCuentas_SelectCuentaContable.sql";

    final RecordingJdbc jdbc = new RecordingJdbc().rows(SEL,
            r("C1", "Bolsa", "=", "BCS"), r("C1", "Moneda", "=", "CLP"),
            r("C2", "Bolsa", "=", "BEC"), r("C2", "Moneda", "=", "USD"),
            r("C3", "Bolsa", "=", "bcs"));

    static java.util.Map<String, Object> r(String cuenta, String tipo, String cond, String valor) {
        return row("CuentaOptimus", cuenta, "CuentaContabilidad", "K" + cuenta, "NombreCuenta", "Nombre " + cuenta,
                "DetalleCuenta", "Detalle " + cuenta, "IdTipoDatoEvento", tipo, "Condicion", cond, "Valor", valor);
    }

    JsonObject run() {
        FiltrarCuentasService svc = LegacySql.injectBlank(new FiltrarCuentasService(jdbc), "SQL_SELECT_CUENTAS=" + SEL);
        return svc.execute(json("{\"p_IdEmpresa\":1,\"p_IdUsuario\":21,\"p_IdModulo\":\"CON\",\"p_Filtros\":["
                + "{\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BCS\"},"
                + "{\"IdTipoDatoEvento\":\"Moneda\",\"Condicion\":\"T\",\"Valor\":\"\"},"
                + "{\"IdTipoDatoEvento\":\"Bolsa\",\"Condicion\":\"=\",\"Valor\":\"BEC\"}]}"));
    }

    @Test
    @DisplayName("CONT-G-420 :IdTipoDatoEvento = distinct tipos in input order (first filter per tipo wins)")
    void contG420_param() {
        // legacy FiltrarCuentas.java:98-109
        run();
        assertThat(jdbc.single(SEL).params()).containsEntry("IdTipoDatoEvento", List.of("Bolsa", "Moneda"));
    }

    @Test
    @DisplayName("CONT-G-421 account kept only if every tipo matches (T = any value; else condicion+valor case-insensitive)")
    void contG421_filtra() {
        // legacy FiltrarCuentas.java:110-153
        JsonObject out = run();
        assertThat(errores(out)).isZero();
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_EXITO");
        assertThat(array(out, "P_OUT_CUENTASCONTABLES")).singleElement().satisfies(o -> {
            assertThat(str(o, "CuentaOptimus")).isEqualTo("C1");
            assertThat(str(o, "CuentaContabilidad")).isEqualTo("KC1");
            assertThat(str(o, "NombreCuenta")).isEqualTo("Nombre C1");
            assertThat(str(o, "DetalleCuenta")).isEqualTo("Detalle C1");
        });
        assertThat(array(out, "P_OUT_CAMPOSFILTROS"))
                .extracting(o -> str(o, "CuentaOptimus") + "/" + str(o, "IdTipoDatoEvento") + "/" + str(o, "Condicion") + "/" + str(o, "Valor") + "/" + str(o, "EsTodos"))
                .containsExactlyInAnyOrder("C1/Bolsa/=/BCS/N", "C1/Moneda/=/CLP/S");
    }

    @Test
    @DisplayName("CONT-G-422 failure -> {1, UI_ERROR} with empty arrays")
    void contG422_failure() {
        // legacy FiltrarCuentas.java:51-52, 156-159
        jdbc.failOn(SEL);
        JsonObject out = run();
        assertThat(errores(out)).isEqualTo(1L);
        assertThat(str(out, "P_OUT_MENSAJE")).isEqualTo("UI_ERROR");
        assertThat(array(out, "P_OUT_CUENTASCONTABLES")).isEmpty();
        assertThat(array(out, "P_OUT_CAMPOSFILTROS")).isEmpty();
    }
}
