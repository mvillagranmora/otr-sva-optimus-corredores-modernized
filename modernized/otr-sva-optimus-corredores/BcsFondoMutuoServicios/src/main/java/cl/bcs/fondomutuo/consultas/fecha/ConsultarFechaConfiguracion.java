package cl.bcs.fondomutuo.consultas.fecha;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cl.bcs.fondomutuo.cierre.SeriesFondoPort;
import cl.bcs.fondomutuo.fechas.FechasPort;
import cl.bcs.fondomutuo.perfil.PerfilFondoMutuoPort;
import cl.bcs.fondomutuo.port.out.kit.CalendarioPort;
import cl.bcs.fondomutuo.port.out.kit.ConfiguracionSistemaPort;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.EmpresaPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;

/**
 * Spring wiring of {@link ConsultarFechaPort}. Being wired does NOT mean being served: the JavaCall entry answers 503 unless
 * {@code fondomutuo.solicitud.listas-servidas} names {@link #LISTA} (closed by default; the user opens it after PROVEN and sign-off). Read only: no lock, no
 * Tesoreria, no custody, no gate G1..G5.
 */
@Configuration
public class ConsultarFechaConfiguracion {

    /** Name in {@code fondomutuo.solicitud.listas-servidas} that opens the entry (closed when absent). */
    public static final String LISTA = "CONSULTAR_FECHA_SOLICITUD";
    /** Legacy FQN this module can answer. */
    public static final String FQN = "cl.bcs.fondomutuo.ConsultarFechaSolicitudAporteRescate";

    @Bean
    public ConsultarFechaPort consultarFechaPort(ConfiguracionSistemaPort configuracion, DiccionarioMensajesPort diccionario, RevisarRequisitosBasicosPort basicos,
            CalendarioPort calendario, EmpresaPort empresas, SeriesFondoPort series, FechasPort fechasPort, PerfilFondoMutuoPort perfilPort,
            ConsultarFechaDatos datos) {
        ConsultarFechaFabrica.Dependencias d = new ConsultarFechaFabrica.Dependencias();
        d.configuracion = configuracion;
        d.diccionario = diccionario;
        d.basicos = basicos;
        d.calendario = calendario;
        d.empresas = empresas;
        d.series = series;
        d.fechasPort = fechasPort;
        d.perfilPort = perfilPort;
        d.datos = datos;
        return ConsultarFechaFabrica.crear(d);
    }
}
