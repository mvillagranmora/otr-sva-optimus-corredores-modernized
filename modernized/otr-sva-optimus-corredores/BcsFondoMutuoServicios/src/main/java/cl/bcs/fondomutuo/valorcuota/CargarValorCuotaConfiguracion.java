package cl.bcs.fondomutuo.valorcuota;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.port.out.http.LogPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;
import cl.bcs.fondomutuo.requisitos.SecuenciaPort;

/**
 * Spring wiring of {@link CargarValorCuotaPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 G0 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default; the owner opens it after sign-off, see RUNBOOK_LISTAS.md). It WRITES
 * ({@code FMM.ValorCuota}, {@code FMM.ValorCuotaHistorico}); no lock, no Tesoreria, no custody, no gate G1..G4.
 */
@Configuration
public class CargarValorCuotaConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CARGAR_VALOR_CUOTA";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.CargarValorCuota";

    @Bean
    public CargarValorCuotaPort cargarValorCuotaPort(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, RevisarRequisitosBasicosPort basicos,
            CalendarioPort calendario, SeriesFondoPort series, CargarValorCuotaDatos datos, CargarValorCuotaEscritura escritura, SecuenciaPort secuencias, LogPort log) {
        CargarValorCuotaFabrica.Dependencias d = new CargarValorCuotaFabrica.Dependencias();
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = basicos;
        d.calendario = calendario;
        d.series = series;
        d.datos = datos;
        d.escritura = escritura;
        d.secuencias = secuencias;
        d.log = log;
        return CargarValorCuotaFabrica.crear(d);
    }
}
