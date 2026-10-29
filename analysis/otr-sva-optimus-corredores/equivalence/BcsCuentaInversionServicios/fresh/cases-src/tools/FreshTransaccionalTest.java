package cl.bcs.cuentainversion.transaccional;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonElement;

import cl.bcs.cuentainversion.infrastructure.adapter.HanaRepository;
import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.AnularCargoAbonoService;
import cl.bcs.cuentainversion.service.CalendarioHabil;
import cl.bcs.cuentainversion.service.CertificarCargoAbonoService;
import cl.bcs.cuentainversion.service.ConsultarMovimientoCuentaInversionService;
import cl.bcs.cuentainversion.service.ConsultarSaldoCuentaInversionService;
import cl.bcs.cuentainversion.service.CrearCargoAbonoService;
import cl.bcs.cuentainversion.service.CrearCargoAbonoWSService;
import cl.bcs.cuentainversion.service.CuadraturaCierreDiarioService;
import cl.bcs.cuentainversion.service.FormatoMoneda;
import cl.bcs.cuentainversion.service.SaldoCuentaInversionService;
import cl.bcs.cuentainversion.transaccional.support.Caso;
import cl.bcs.cuentainversion.transaccional.support.ColaboradoresDeCaso;
import cl.bcs.cuentainversion.transaccional.support.ConfiguracionDelCaso;
import cl.bcs.cuentainversion.transaccional.support.DiccionarioDeCaso;
import cl.bcs.cuentainversion.transaccional.support.Verifica;
import cl.bcs.cuentainversion.transaccional.support.ZonaUtc;

/** Fresh-input driver (verifier): runs the modern code on the CTA-F-* cases with the same wiring as the development tests. */
@ExtendWith(ZonaUtc.class)
class FreshTransaccionalTest {

    static List<String> casos() {
        return Caso.todos().stream().filter(n -> n.startsWith("CTA-F-")).toList();
    }

    @FunctionalInterface
    interface Llamada {
        JsonElement ejecutar() throws Exception;
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> JsonElement lanzar(Throwable t) throws E {
        throw (E) t;
    }

    private static Verifica.Ejecucion ejecutar(Llamada l) {
        return Verifica.Ejecucion.de(() -> {
            try {
                return l.ejecutar();
            } catch (Exception e) {
                return FreshTransaccionalTest.<RuntimeException>lanzar(e);
            }
        });
    }

    @ParameterizedTest
    @MethodSource("casos")
    void fresco(String nombre) {
        Caso caso = Caso.cargar(nombre);
        String servicio = caso.caso().get("servicio").getAsString();
        ColaboradoresDeCaso colab = new ColaboradoresDeCaso(caso);
        DiccionarioDeCaso dic = new DiccionarioDeCaso();
        ActualizarSaldoCargoAbonoService act = new ActualizarSaldoCargoAbonoService(caso.jdbc());
        CrearCargoAbonoService crear = new CrearCargoAbonoService(caso.jdbc(), act, new CalendarioHabil(caso.jdbc()),
                new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), dic, colab);
        Verifica.Ejecucion r;
        String[] sinOrden = {};
        switch (servicio) {
            case "CrearCargoAbono" -> {
                sinOrden = new String[] {"P_OUT_MENSAJES", "P_OUT_CARGOABONOGENERADO"};
                r = ejecutar(() -> crear.execute(caso.entrada().deepCopy()));
            }
            case "CertificarCargoAbono" -> {
                sinOrden = new String[] {"P_OUT_INFORMACIONDETALLE", "P_OUT_ERRORESDETALLE", "P_OUT_ADVERTENCIASDETALLE"};
                CertificarCargoAbonoService s = new CertificarCargoAbonoService(caso.jdbc(), act, new CalendarioHabil(caso.jdbc()),
                        new FormatoMoneda(caso.jdbc()), new ConfiguracionDelCaso(caso), dic, colab);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
            }
            case "AnularCargoAbono" -> {
                sinOrden = new String[] {"P_OUT_MENSAJES"};
                AnularCargoAbonoService s = new AnularCargoAbonoService(caso.jdbc(), act, crear, dic, colab);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
            }
            case "ConsultarSaldoCuentaInversion" -> {
                sinOrden = new String[] {"P_OUT_SALDOINICIALFINAL", "P_OUT_SALDOPORFECHAEFECTIVA", "P_OUT_SALDOPORFECHAMOVIMIENTO", "P_OUT_ERRORESDETALLE"};
                ConsultarSaldoCuentaInversionService s = new ConsultarSaldoCuentaInversionService(caso.jdbc(),
                        new SaldoCuentaInversionService(caso.jdbc()), new ConfiguracionDelCaso(caso), dic, colab);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
            }
            case "ConsultarMovimientoCuentaInversion" -> {
                ConsultarMovimientoCuentaInversionService s = new ConsultarMovimientoCuentaInversionService(new HanaRepository(caso.jdbc()),
                        new ConfiguracionDelCaso(caso), dic);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
                Verifica.comparar(caso, r);
                return;
            }
            case "CuadraturaCierreDiario" -> {
                CuadraturaCierreDiarioService s = new CuadraturaCierreDiarioService(caso.jdbc(), new ConfiguracionDelCaso(caso), dic, colab);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
            }
            case "webservices.CrearCargoAbonoWS" -> {
                sinOrden = new String[] {"P_OUT_MENSAJES", "P_OUT_CARGOABONOGENERADO"};
                CrearCargoAbonoWSService s = new CrearCargoAbonoWSService(caso.jdbc(), crear, new ConfiguracionDelCaso(caso), dic);
                r = ejecutar(() -> s.execute(caso.entrada().deepCopy()));
            }
            default -> throw new AssertionError("servicio sin conductor: " + servicio);
        }
        Verifica.comparar(caso, r, colab.traza(), sinOrden);
    }
}
