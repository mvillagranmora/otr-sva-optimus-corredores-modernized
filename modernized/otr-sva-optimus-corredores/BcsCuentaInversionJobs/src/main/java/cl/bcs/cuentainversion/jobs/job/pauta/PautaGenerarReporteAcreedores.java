package cl.bcs.cuentainversion.jobs.job.pauta;

import java.util.ArrayList;
import java.util.List;
import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;
import cl.bcs.cuentainversion.jobs.runtime.SalidaPauta;
import cl.bcs.cuentainversion.port.out.Colaboradores;
import java.util.Map;
import cl.bcs.cuentainversion.jobs.job.Llamada;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.PautaGenerarReporteAcreedores} (76 lines): pauta: enrolls the creditors report as a batch program.
 */
public final class PautaGenerarReporteAcreedores implements ProcesoPauta {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.PautaGenerarReporteAcreedores";

    private final ConfiguracionJobs configuracion;
    private final Colaboradores colaboradores;

    public PautaGenerarReporteAcreedores(ConfiguracionJobs configuracion, Colaboradores colaboradores) {
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.colaboradores = Objects.requireNonNull(colaboradores, "colaboradores");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public SalidaPauta transaction(Map<String, Object> parametros) throws Exception {
        // PautaGenerarReporteAcreedores.java:21-53. CIJ-DEF-11: (Long) cast, String.valueOf(null) = "null", IdUsuario always 0.
        Long idEmpresa = (Long) parametros.get("IdEmpresa");
        String fechaSistema = Llamada.parametro(configuracion, idEmpresa, "PLATAFORMA", "PLA.FechaSistema");
        List<InscribirProcesoBatch.Parametro> lista = new ArrayList<>();
        lista.add(new InscribirProcesoBatch.Parametro(0L, "IdEmpresa", String.valueOf(parametros.get("IdEmpresa")), null));
        lista.add(new InscribirProcesoBatch.Parametro(0L, "IdUsuario", String.valueOf(0L), null));
        lista.add(new InscribirProcesoBatch.Parametro(0L, "P_FechaSolicitud", fechaSistema, null));
        lista.add(new InscribirProcesoBatch.Parametro(0L, "P_Moneda", "CLP", null));
        lista.add(new InscribirProcesoBatch.Parametro(0L, "P_Consolidar", "S", null));
        lista.add(new InscribirProcesoBatch.Parametro(0L, "NombreInternoArchivo", InscribirProcesoBatch.nombreRandom(), null));
        return InscribirProcesoBatch.inscribir(colaboradores,
                InscribirProcesoBatch.entrada((Long) parametros.get("IdEmpresa"), "GenerarReporteAcreedores", lista));
    }
}
