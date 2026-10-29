package cl.bcs.socionegocio.emp;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.utils.SQL;

public class AutorizarEnrolamientoEmpleadoMasivo implements BcsAutorizaciones {

    private static final Logger logger = LogManager.getLogger(AutorizarEnrolamientoEmpleadoMasivo.class);

    private final SQL sql = new SQL();

    private final String SQL_UPDATE_EMPLEADO_ENROLAMIENTO =
        PlataformaUtils.loadResourceToString(
            "cl/bcs/socionegocio/emp/sql/AutorizarEnrolamientoEmpleadoMasivo_Update_EmpleadoEnrolamiento.sql"
        );

    private final String SQL_UPDATE_PERSONA_ENROLAMIENTO =
        PlataformaUtils.loadResourceToString(
            "cl/bcs/socionegocio/emp/sql/AutorizarEnrolamientoEmpleadoMasivo_Update_PersonaEnrolamiento.sql"
        );

    public static class Empleado {
        public Long IdEmpleado;
        public String EsEnrolado;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizarEnrolamientoEmpleadoMasivo.autorizarEvento");
        long inicio = System.currentTimeMillis();

        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<Empleado> empleados = entrada.p_Lista.stream()
                .map(evento -> {
                    Empleado e = new Empleado();
                    e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
                    e.EsEnrolado = "S";
                    return e;
                })
                .distinct()
                .collect(Collectors.toList());

            sql.batch(conn, SQL_UPDATE_EMPLEADO_ENROLAMIENTO, empleados, Empleado.class);
            sql.batch(conn, SQL_UPDATE_PERSONA_ENROLAMIENTO, empleados, Empleado.class);

            for (Empleado empleado : empleados) {
                BcsAutorizaciones.ResultadoAutorizacion resultado =
                    new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = empleado;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }

        } catch (Exception e) {
            logger.error("Error en autorizarEvento: {}", e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::AutorizarEnrolamientoEmpleadoMasivo.autorizarEvento {}", fin - inicio);

        return salida;
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizarEnrolamientoEmpleadoMasivo.rechazarEvento");
        long inicio = System.currentTimeMillis();

        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<Empleado> empleados = entrada.p_Lista.stream()
                .map(evento -> {
                    Empleado e = new Empleado();
                    e.IdEmpleado = Long.valueOf(evento.NumeroOperacionConcepto);
                    e.EsEnrolado = "N";
                    return e;
                })
                .distinct()
                .collect(Collectors.toList());

            sql.batch(conn, SQL_UPDATE_EMPLEADO_ENROLAMIENTO, empleados, Empleado.class);

            for (Empleado empleado : empleados) {
                BcsAutorizaciones.ResultadoAutorizacion resultado =
                    new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = empleado;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }

        } catch (Exception e) {
            logger.error("Error en rechazarEvento: {}", e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }

        long fin = System.currentTimeMillis();
        logger.info("FIN::AutorizarEnrolamientoEmpleadoMasivo.rechazarEvento {}", fin - inicio);

        return salida;
    }

    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");

        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            AutorizarEnrolamientoEmpleadoMasivo servicio = new AutorizarEnrolamientoEmpleadoMasivo();
            EntradaAutorizacion entrada = new EntradaAutorizacion();
            entrada.p_IdEmpresa = 1L;
            entrada.p_IdUsuario = 76770L;
            entrada.p_IdModulo = "AUT";

            cl.bcs.plataforma.aut.pojo.Evento evento = new cl.bcs.plataforma.aut.pojo.Evento();
            evento.NumeroOperacionConcepto = "1000";
            evento.IdEmpresa = 1L;
            evento.IdUsuario = 76770L;
            evento.IdModulo = "AUT";
            entrada.p_Lista.add(evento);

            System.out.println("\n=== Probando CONFIRMAR ===");
            SalidaAutorizacion salidaConfirmar = servicio.autorizarEvento(conn, entrada);
            System.out.println("Errores: " + salidaConfirmar.P_OUT_ERRORESCONTADOR);
            System.out.println("Resultados: " + salidaConfirmar.P_OUT_RESULTADO.size());

            System.out.println("\n=== Probando RECHAZAR ===");
            SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
            System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
            System.out.println("Resultados: " + salidaRechazar.P_OUT_RESULTADO.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("== FIN TEST ==");
    }
}
