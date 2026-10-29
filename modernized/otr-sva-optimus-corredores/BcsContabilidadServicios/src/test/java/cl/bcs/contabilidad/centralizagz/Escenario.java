package cl.bcs.contabilidad.centralizagz;

import cl.bcs.contabilidad.domain.model.SalidaCentralizacion;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabEventoRef;
import cl.bcs.contabilidad.domain.model.SalidaCentralizacion.TabMovimientoRef;
import cl.bcs.contabilidad.domain.port.out.ContabilidadRepository;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.service.BcsCentralizacionService;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static cl.bcs.contabilidad.centralizagz.Fx.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test double world for one centralization run.
 *
 * <ul>
 *   <li>{@link PlataformaPort} answers with the fixture company / dates; configuration
 *       parameters are answered ONLY for the exact (module, parameter) key the legacy reads,
 *       otherwise the caller's default comes back. A port that reads the wrong key therefore
 *       sees the default and behaves as if the flag were off.</li>
 *   <li>{@link ContabilidadRepository#queryDatosMovimientoRows} is the port's only "run this
 *       named SQL with these named parameters" method; every invocation is recorded and
 *       answered from rows registered per legacy SQL file name.</li>
 *   <li>The AnticipoPactos-specific port methods (queryMovimientos / queryEventosOperacion /
 *       queryEventosSubyacente) return a SENTINEL row: in the JDBC adapter they execute
 *       CentralizaAnticipoPactos SQL (ContabilidadJdbcRepository.java:72-105,
 *       sql/queryMovimientos.sql:1), so a sentinel in the output means the wrong SQL ran.</li>
 * </ul>
 */
final class Escenario {

    record SqlCall(String recurso, Map<String, Object> params) {}

    static final Long SENTINEL_ID = 666L;
    static final TabMovimientoRef MOV_SENTINEL = new TabMovimientoRef(ID_TIPO, SENTINEL_ID,
            "SENTINEL-ANTICIPO-PACTOS", ID_EMPRESA, 0L, 0L, FECHA_CONTABLE, "0-0", 0L, EJERCICIO,
            "00", BigDecimal.ONE, "XXX", "SENTINEL");
    static final TabEventoRef EVE_SENTINEL = new TabEventoRef(ID_TIPO, SENTINEL_ID,
            "SENTINEL-ANTICIPO-PACTOS", "sentinel", BigDecimal.ONE, "XXX");

    final PlataformaPort plataforma = mock(PlataformaPort.class);
    final ContabilidadRepository repository = mock(ContabilidadRepository.class);
    /** key = MODULO + "|" + PARAMETRO exactly as the legacy reads it. */
    final Map<String, String> config = new HashMap<>();
    final Map<String, Function<Map<String, Object>, List<Map<String, Object>>>> filas = new LinkedHashMap<>();
    final List<SqlCall> llamadas = new ArrayList<>();
    String fechaSistema = FECHA_CONTABLE;
    String clasificacionTipoInstrumento = "PRE";

    Escenario() {
        when(plataforma.consultarEmpresa(any())).thenReturn(EMPRESA);
        when(plataforma.obtenerDiaAnterior(any(), any())).thenAnswer(inv ->
                ID_PAIS.equals(inv.getArgument(0)) && FECHA_CONTABLE.equals(inv.getArgument(1))
                        ? FECHA_ANTERIOR : "DIA-ANTERIOR-CON-ARGUMENTOS-INCORRECTOS");
        when(plataforma.obtenerLocalizacionSistema(any())).thenReturn(LOC_SISTEMA);
        when(plataforma.obtenerMonedaSistema(any())).thenReturn(MONEDA_SISTEMA);
        when(plataforma.obtenerFechaSistema(any())).thenAnswer(inv -> fechaSistema);
        when(plataforma.obtenerParametroConfiguracion(any(), any(), any(), any())).thenAnswer(inv -> {
            String key = inv.getArgument(1) + "|" + inv.getArgument(2);
            switch (key) {
                case "PLATAFORMA|PLA.FechaSistema":          return fechaSistema;
                case "PLATAFORMA|PLA.LocalizacionSistema":   return LOC_SISTEMA;
                case "PLATAFORMA|PLA.MonedaSistema":         return MONEDA_SISTEMA;
                default:                                     return config.getOrDefault(key, inv.getArgument(3));
            }
        });

        when(repository.queryMovimientos(any(), any(), any())).thenReturn(List.of(MOV_SENTINEL));
        when(repository.queryEventosOperacion(any(), any(), any())).thenReturn(List.of(EVE_SENTINEL));
        when(repository.queryEventosSubyacente(any(), any(), any(), any())).thenReturn(List.of(EVE_SENTINEL));
        when(repository.queryDatosMovimientoAnticipo(any(), any())).thenReturn(List.of());
        when(repository.queryClasificacionTipoInstrumento(any())).thenAnswer(inv -> clasificacionTipoInstrumento);
        when(repository.queryDatosMovimientoRows(anyString(), any())).thenAnswer(inv -> {
            String recurso = inv.getArgument(0);
            MapSqlParameterSource p = inv.getArgument(1);
            Map<String, Object> valores = p == null ? new LinkedHashMap<>() : new LinkedHashMap<>(p.getValues());
            llamadas.add(new SqlCall(recurso, valores));
            for (var e : filas.entrySet()) {
                if (coincide(recurso, e.getKey())) {
                    return copia(e.getValue().apply(valores));
                }
            }
            return new ArrayList<Map<String, Object>>();
        });
    }

    Escenario filas(String archivoSql, List<Map<String, Object>> rows) {
        filas.put(archivoSql, p -> rows);
        return this;
    }

    Escenario filas(String archivoSql, Function<Map<String, Object>, List<Map<String, Object>>> f) {
        filas.put(archivoSql, f);
        return this;
    }

    List<SqlCall> llamadasA(String archivoSql) {
        return llamadas.stream().filter(c -> coincide(c.recurso(), archivoSql)).toList();
    }

    SalidaCentralizacion ejecutar(Spec.Tipo tipo, String simulaOperacion) {
        BcsCentralizacionService svc = tipo.factory().apply(plataforma, repository);
        return svc.centralizar(entrada(simulaOperacion));
    }

    /** Legacy loads "cl/bcs/contabilidad/sql/X.sql"; the port uses "sql/X.sql". Both identify file X.sql. */
    static boolean coincide(String recurso, String archivoSql) {
        return recurso.equals(archivoSql) || recurso.endsWith("/" + archivoSql);
    }

    private static List<Map<String, Object>> copia(List<Map<String, Object>> rows) {
        List<Map<String, Object>> out = new ArrayList<>();
        for (var r : rows) {
            out.add(new HashMap<>(r));
        }
        return out;
    }
}
