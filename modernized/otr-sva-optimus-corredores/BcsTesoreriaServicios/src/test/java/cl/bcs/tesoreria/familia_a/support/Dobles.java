package cl.bcs.tesoreria.familia_a.support;

import java.time.Instant;

import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.tesoreria.port.out.Secuencias;

/**
 * The injectable ports of a service under test, built from one case: configuration, dictionary, collaborators (with their call trace), sequences and
 * the fixed clock of the recording (2025-04-01 10:15:30 UTC).
 */
public final class Dobles {

    public static final Instant AHORA = Instant.parse("2025-04-01T10:15:30Z");

    private final ConfiguracionSistema configuracion;
    private final DiccionarioMensajes diccionario;
    private final ColaboradoresDeCaso colaboradores;
    private final SecuenciasDeCaso secuencias;
    private final Reloj reloj = () -> AHORA;

    private Dobles(Caso caso) {
        this.configuracion = new ConfiguracionDelCaso(caso);
        this.diccionario = new DiccionarioDeCaso();
        this.colaboradores = new ColaboradoresDeCaso(caso);
        this.secuencias = new SecuenciasDeCaso(caso);
    }

    public static Dobles de(Caso caso) {
        return new Dobles(caso);
    }

    public ConfiguracionSistema configuracion() {
        return configuracion;
    }

    public DiccionarioMensajes diccionario() {
        return diccionario;
    }

    public ColaboradoresDeCaso colaboradores() {
        return colaboradores;
    }

    public Secuencias secuencias() {
        return secuencias;
    }

    public SecuenciasDeCaso secuenciasDeCaso() {
        return secuencias;
    }

    public Reloj reloj() {
        return reloj;
    }
}
