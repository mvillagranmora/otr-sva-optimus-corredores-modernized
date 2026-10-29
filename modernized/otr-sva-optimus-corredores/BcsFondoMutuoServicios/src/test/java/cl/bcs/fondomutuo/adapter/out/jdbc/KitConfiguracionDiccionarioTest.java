package cl.bcs.fondomutuo.adapter.out.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import cl.bcs.fondomutuo.TestBd;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.ParametroNoEncontradoException;

/** FM-KIT-10..22: ports {@code ConfiguracionSistemaPort} and {@code DiccionarioMensajesPort} (legacy {@code ConfiguracionSistema}, {@code Diccionario}). */
class KitConfiguracionDiccionarioTest {

    DataSource ds;
    ConexionUnidadDeTrabajo conexion;
    ConfiguracionSistemaJdbc config;
    DiccionarioJdbc dic;

    @BeforeEach
    void preparar() {
        ds = TestBd.nueva();
        conexion = new ConexionUnidadDeTrabajo(ds);
        config = new ConfiguracionSistemaJdbc(conexion);
        dic = new DiccionarioJdbc(conexion);
        TestBd.ejecutar(ds,
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.FechaSistema','2026-03-02')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.LocalizacionSistema','es-CL')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (1,'PLA.SegundosZonaHoraria','-10800')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Configuracion\" VALUES (2,'PLA.FechaSistema','2030-01-01')",
                "INSERT INTO \"BSCL.FondoMutuo.BD::FMM.Parametro\" VALUES (1,'CALCULAR_COMISION_CERTIFICACION','S')",
                "INSERT INTO \"BSCL.FondoMutuo.BD::FMM.Parametro\" VALUES (1,'MONTO_MAXIMO','1250.50')",
                "INSERT INTO \"BSCL.FondoMutuo.BD::FMM.Parametro\" VALUES (1,'ORIGENES','PAC;REC;BONIF')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('es-CL','UI_ERROR_X','Error en ::Campo:: de ::Valor::')",
                "INSERT INTO \"BSCL.Plataforma.BD::PLA.Diccionario\" VALUES ('en-US','UI_ERROR_X','Wrong ::Campo::')");
    }

    @Test
    void fmKit10_parametroDePlataformaYDeFondoMutuo_porEmpresa() {
        assertThat(config.texto(1L, "PLATAFORMA", "PLA.FechaSistema")).isEqualTo("2026-03-02");
        assertThat(config.texto(2L, "PLATAFORMA", "PLA.FechaSistema")).isEqualTo("2030-01-01");
        assertThat(config.texto(1L, "FONDOMUTUO", "CALCULAR_COMISION_CERTIFICACION")).isEqualTo("S");
    }

    @Test
    void fmKit11_tiposDelLegacy_entero_decimal_conjunto() {
        assertThat(config.entero(1L, "PLATAFORMA", "PLA.SegundosZonaHoraria")).isEqualTo(-10800L);
        assertThat(config.decimal(1L, "FONDOMUTUO", "MONTO_MAXIMO")).isEqualByComparingTo(new BigDecimal("1250.50"));
        assertThat(config.conjunto(1L, "FONDOMUTUO", "ORIGENES", ";")).containsExactlyInAnyOrder("PAC", "REC", "BONIF");
    }

    @Test
    void fmKit12_parametroInexistente_conElMensajeDelLegacy() {
        assertThatThrownBy(() -> config.texto(1L, "FONDOMUTUO", "NO_EXISTE")).isInstanceOf(ParametroNoEncontradoException.class)
                .hasMessage("Parametro NO_EXISTE en modulo FONDOMUTUO no encontrado");
        assertThatThrownBy(() -> config.texto(99L, "FONDOMUTUO", "MONTO_MAXIMO")).isInstanceOf(ParametroNoEncontradoException.class);
    }

    @Test
    void fmKit13_moduloDesconocido_fallaConElMensajeDelLegacy() {
        assertThatThrownBy(() -> config.texto(1L, "INVENTADO", "X")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuracion modulo INVENTADO no encontrada");
    }

    @Test
    void fmKit14_sinCache_unCambioEnLaTablaSeVeEnLaSiguienteLectura() {
        assertThat(config.texto(1L, "PLATAFORMA", "PLA.FechaSistema")).isEqualTo("2026-03-02");
        TestBd.ejecutar(ds, "UPDATE \"BSCL.Plataforma.BD::PLA.Configuracion\" SET \"Valor\"='2026-03-03' WHERE \"CodigoParametro\"='PLA.FechaSistema' AND \"IdEmpresa\"=1");
        assertThat(config.texto(1L, "PLATAFORMA", "PLA.FechaSistema")).isEqualTo("2026-03-03");
    }

    @Test
    void fmKit15_diccionario_reemplazaLosMarcadoresConDosPuntosDobles() {
        Map<String, String> v = new HashMap<>();
        v.put("Campo", "Monto");
        v.put("Valor", null);
        // the language comes from PLA.Configuracion (PLA.LocalizacionSistema) of the company: es-CL, not en-US
        assertThat(dic.mensaje(1L, "UI_ERROR_X", v)).isEqualTo("Error en Monto de ");
    }

    @Test
    void fmKit16_codigoDesconocido_oValoresNull_dan_sinTraduccion() {
        assertThat(dic.mensaje(1L, "NO_EXISTE", Map.of())).isEqualTo("NO_EXISTE sin traduccion");
        assertThat(dic.mensaje(1L, "UI_ERROR_X", null)).isEqualTo("UI_ERROR_X sin traduccion");
        assertThat(dic.mensaje(1L, "UI_ERROR_X")).isEqualTo("Error en ::Campo:: de ::Valor::");
    }

    @Test
    void fmKit17_diccionario_unaVezPorUnidadDeTrabajo_yNadaSobreviveALaTransaccion_DEVF2() {
        TransactionTemplate tx = new TransactionTemplate(new DataSourceTransactionManager(ds));
        tx.executeWithoutResult(s -> {
            assertThat(dic.mensaje(1L, "UI_ERROR_X")).startsWith("Error en");
            TestBd.ejecutar(ds, "UPDATE \"BSCL.Plataforma.BD::PLA.Diccionario\" SET \"Descripcion\"='NUEVO' WHERE \"CodLenguaje\"='es-CL'");
            // same unit of work: the dictionary already read is kept (and the second statement is on another connection, uncommitted view irrelevant)
            assertThat(dic.mensaje(1L, "UI_ERROR_X")).startsWith("Error en");
        });
        // next unit of work: nothing was kept
        assertThat(dic.mensaje(1L, "UI_ERROR_X")).isEqualTo("NUEVO");
        tx.executeWithoutResult(s -> assertThat(dic.mensaje(1L, "UI_ERROR_X")).isEqualTo("NUEVO"));
    }

    @Test
    void fmKit18_mensajeSalida_tieneElJsonDelLegacy_conTextoDelDiccionario() {
        MensajeSalida m = MensajeSalida.conTexto(0L, MensajeSalida.MENSAJE_ERROR, "UI_ERROR_X", Map.of("Campo", "A", "Valor", "B"), dic, 1L);
        assertThat(cl.bcs.fondomutuo.kit.JsonLegacy.plano().toJson(m))
                .isEqualTo("{\"id\":0,\"tipo\":\"ERROR\",\"codigo\":\"UI_ERROR_X\",\"descripcion\":\"Error en A de B\"}");
        assertThat(cl.bcs.fondomutuo.kit.JsonLegacy.plano().toJson(new MensajeSalida(1L, "OK", "C", null))).doesNotContain("descripcion");
        assertThat(new MensajeSalida(1L, "OK", "C", null).toString()).contains("\"descripcion\":null");
    }
}
