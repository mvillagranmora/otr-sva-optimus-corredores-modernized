package cl.bcs.tesoreria.familia_b;

import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.gson.JsonElement;

import cl.bcs.tesoreria.familia_b.support.AlmacenArchivosDeCaso;
import cl.bcs.tesoreria.familia_b.support.Caso;
import cl.bcs.tesoreria.familia_b.support.ColaboradoresDeCaso;
import cl.bcs.tesoreria.familia_b.support.ConfiguracionDelCaso;
import cl.bcs.tesoreria.familia_b.support.DiccionarioDeCaso;
import cl.bcs.tesoreria.familia_b.support.Equivalencia;
import cl.bcs.tesoreria.familia_b.support.RelojDeCaso;
import cl.bcs.tesoreria.familia_b.support.SecuenciasDeCaso;
import cl.bcs.tesoreria.familia_b.support.Verifica;
import cl.bcs.tesoreria.familia_b.support.ZonaUtc;
import cl.bcs.tesoreria.javacall.CargaMasivaIngresoEgresoDAPFFMMService;
import cl.bcs.tesoreria.javacall.ConsultaBCPService;
import cl.bcs.tesoreria.javacall.ModificarPACService;
import cl.bcs.tesoreria.javacall.RecaudacionBCPService;
import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.javacall.ban.ConsultarLibretaBancoService;
import cl.bcs.tesoreria.javacall.ban.ConsultarSaldoCuentaBancoService;
import cl.bcs.tesoreria.javacall.bcp.DiaHabilSiguiente;
import cl.bcs.tesoreria.javacall.bcp.LogMensajeTransaccionBcp;
import cl.bcs.tesoreria.javacall.dep.GestionDeDepositosService;
import cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService;
import cl.bcs.tesoreria.javacall.liq.ProcesarClearingFinancieroService;
import cl.bcs.tesoreria.javacall.pac.MandatoPacComun;

/**
 * Fresh-input driver (independent verifier), family B: runs the modern code on the TES-B-F* cases with the SAME wiring
 * as the development tests (copied from each *ServiceTest.verificar). Lists are compared strictly (no "sin orden" list),
 * except none: an order difference would be reported as a finding.
 */
@ExtendWith(ZonaUtc.class)
class FreshBTest {

    static List<String> casos() {
        return Caso.todos().stream().filter(n -> n.startsWith("TES-B-F")).toList();
    }

    private static final Caso.Contador EJECUTADOS = new Caso.Contador();

    @ParameterizedTest
    @MethodSource("casos")
    void fresco(String nombre) {
        String servicio = Caso.cargar(nombre).caso().get("servicio").getAsString();
        ColaboradoresDeCaso[] col = new ColaboradoresDeCaso[1];
        switch (servicio) {
            case "ConsultaBCP" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> new ConsultaBCPService(caso.jdbc(), new DiccionarioDeCaso(),
                    new ConfiguracionDelCaso(caso), new LogMensajeTransaccionBcp(caso.jdbc())).execute(caso.entrada().deepCopy()));
            case "RecaudacionBCP" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                return new RecaudacionBCPService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), new RelojDeCaso(),
                        new LogMensajeTransaccionBcp(caso.jdbc()), new DiaHabilSiguiente(caso.jdbc()), col[0]).execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "ban.ConsultarLibretaBanco" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                return new ConsultarLibretaBancoService(caso.jdbc(), new cl.bcs.tesoreria.javacall.ban.FormatoMoneda(caso.jdbc()), col[0]).execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "ban.ConsultarSaldoCuentaBanco" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                return new ConsultarSaldoCuentaBancoService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), col[0]).execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "liq.ConsultarClearingFinanciero" -> Equivalencia.verificar(EJECUTADOS, nombre,
                    caso -> new ConsultarClearingFinancieroService(caso.jdbc(), new ConfiguracionDelCaso(caso)).execute(caso.entrada().deepCopy()));
            case "liq.ProcesarClearingFinanciero" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                ConfiguracionDelCaso configuracion = new ConfiguracionDelCaso(caso);
                return new ProcesarClearingFinancieroService(caso.jdbc(), new DiccionarioDeCaso(), configuracion,
                        new ConsultarClearingFinancieroService(caso.jdbc(), configuracion), col[0]).execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "ModificarPAC" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                DiccionarioDeCaso diccionario = new DiccionarioDeCaso();
                return new ModificarPACService(caso.jdbc(), diccionario, new ConfiguracionDelCaso(caso),
                        new MandatoPacComun(caso.jdbc(), diccionario, col[0]), col[0]).execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "dep.GestionDeDepositos" -> Equivalencia.verificar(EJECUTADOS, nombre, caso -> {
                col[0] = new ColaboradoresDeCaso(caso);
                return new GestionDeDepositosService(caso.jdbc(), new DiccionarioDeCaso(), new ActualizarSaldosCuentaBancoService(caso.jdbc()), col[0])
                        .execute(caso.entrada().deepCopy());
            }, () -> col[0].traza());
            case "CargaMasivaIngresoEgresoDAPFFMM" -> {
                Caso[] cargado = new Caso[1];
                Equivalencia.verificarConArchivos(EJECUTADOS, nombre, caso -> {
                    cargado[0] = caso;
                    col[0] = new ColaboradoresDeCaso(caso);
                    return new CargaMasivaIngresoEgresoDAPFFMMService(caso.jdbc(), new DiccionarioDeCaso(), new ConfiguracionDelCaso(caso), new SecuenciasDeCaso(caso),
                            new AlmacenArchivosDeCaso(caso), col[0]).execute(caso.entrada().deepCopy());
                });
                Verifica.colaboradores(cargado[0], col[0].traza());
            }
            default -> throw new AssertionError("servicio sin conductor: " + servicio);
        }
    }
}
