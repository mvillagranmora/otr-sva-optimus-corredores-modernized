package cl.bcs.contabilidad.envio.support;

import cl.bcs.contabilidad.domain.model.sap.CuentaSap;
import cl.bcs.contabilidad.domain.port.out.DiccionarioPort;
import cl.bcs.contabilidad.domain.port.out.PlanCuentasSapPort;
import cl.bcs.contabilidad.domain.port.out.PlataformaPort;
import cl.bcs.contabilidad.domain.port.out.RelojSistemaPort;
import cl.bcs.contabilidad.domain.port.out.SecuenciaPort;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Deterministic fakes that mirror the legacy-oracle shims one-to-one, so modern output can be compared
 * with the recorded goldens:
 * <ul>
 *   <li>Diccionario: "[CODIGO]{k=v,...}" (keys sorted) = shim Diccionario.formato.</li>
 *   <li>Secuencias: IdPreComprobante starts at 5000, IdPreComprobanteAsiento at 9000 = shim Utils.SECUENCIAS.</li>
 *   <li>Reloj: 2025-04-01 / 10:15:30 = shim FechaDB.</li>
 *   <li>Plataforma: values of seed/config.json + case overrides, keyed "MODULO/Parametro" = shim ConfiguracionSistema.</li>
 * </ul>
 */
public final class Fakes {

    public static final String SEQ_PRECOMPROBANTE = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobante";
    public static final String SEQ_PRECOMPROBANTE_ASIENTO = "BSCL.Contabilidad.BD.Secuencias::IdPreComprobanteAsiento";

    private Fakes() {
    }

    public static DiccionarioPort diccionario() {
        return (idEmpresa, codigo, valores) -> "[" + codigo + "]" + new TreeMap<>(valores == null ? Map.of() : valores);
    }

    /**
     * PlataformaPort backed by a "MODULO/Parametro" map. Works whether the implementation reads through
     * obtenerParametroConfiguracion or through the dedicated getters. A parameter absent from the map
     * returns the caller's default (legacy: UsaCentroCosto / CalculaParidad... fall back to "N").
     */
    public static PlataformaPort plataforma(Map<String, String> config) {
        return Mockito.mock(PlataformaPort.class, inv -> switch (inv.getMethod().getName()) {
            case "obtenerParametroConfiguracion" -> {
                String k = inv.getArgument(1) + "/" + inv.getArgument(2);
                yield config.containsKey(k) ? config.get(k) : inv.getArgument(3);
            }
            case "obtenerMonedaSistema" -> config.get("PLATAFORMA/PLA.MonedaSistema");
            case "obtenerLocalizacionSistema" -> config.get("PLATAFORMA/PLA.LocalizacionSistema");
            case "obtenerFechaSistema" -> "2025-04-01";
            case "toString" -> "FakePlataforma" + config;
            case "hashCode" -> System.identityHashCode(inv.getMock());
            case "equals" -> inv.getMock() == inv.getArgument(0);
            default -> throw new UnsupportedOperationException("PlataformaPort." + inv.getMethod().getName() + " not expected in WP-B");
        });
    }

    public static PlanCuentasSapPort planCuentas(List<CuentaSap> cuentas) {
        return () -> cuentas;
    }

    /** Legacy: interfazSAP.properties not found (EnvioContabilidad.java:359-363). */
    public static PlanCuentasSapPort planCuentasSinConfiguracion() {
        return () -> {
            throw new PlanCuentasSapPort.ConfiguracionNoEncontradaException("interfazSAP configuration not found (fake)");
        };
    }

    public static RelojSistemaPort reloj() {
        return new RelojSistemaPort() {
            @Override
            public String fechaProceso(Long idEmpresa) {
                return "2025-04-01";
            }

            @Override
            public String horaProceso(Long idEmpresa) {
                return "10:15:30";
            }
        };
    }

    /** Sequence fake; {@link #llamadas()} records "name#cantidad" like the oracle shim. */
    public static final class Secuencias implements SecuenciaPort {
        private final Map<String, Long> siguiente = new HashMap<>(Map.of(SEQ_PRECOMPROBANTE, 5000L, SEQ_PRECOMPROBANTE_ASIENTO, 9000L));
        private final List<String> llamadas = new ArrayList<>();

        @Override
        public synchronized List<Long> siguientes(String nombreSecuencia, long cantidad) {
            llamadas.add(nombreSecuencia + "#" + cantidad);
            List<Long> out = new ArrayList<>();
            if (cantidad <= 0) {
                return out;
            }
            long n = siguiente.getOrDefault(nombreSecuencia, 1L);
            for (long i = 0; i < cantidad; i++) {
                out.add(n++);
            }
            siguiente.put(nombreSecuencia, n);
            return out;
        }

        public synchronized List<String> llamadas() {
            return List.copyOf(llamadas);
        }
    }
}
