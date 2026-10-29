package cl.bcs.socionegocio.per;

import cl.bcs.plataforma.pla.utils.PlataformaUtils;
import cl.bcs.socionegocio.per.AutorizacionModificacionPersonaCuentaBancariaMasivo.CuentaBancariaAutorizacion;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import cl.bcs.plataforma.aut.BcsAutorizaciones;
import cl.bcs.plataforma.aut.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.plataforma.aut.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntradaModificarEstado;
import cl.bcs.plataforma.pla.ModificarEstadoRegistroSolicitudCambioMasivo.EntidadOrigen;
import cl.bcs.utils.SQL;
import cl.bcs.utils.Utils;

public class AutorizacionModificacionPersonaCuentaBancariaMasivo implements BcsAutorizaciones {

    private static final Logger logger = LogManager.getLogger(AutorizacionModificacionPersonaCuentaBancariaMasivo.class);
    private final SQL sql = new SQL();
    private final String SQL_UPDATE_DATOS_CUENTA_BANCARIA =
        PlataformaUtils.loadResourceToString(
            "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaCuentaBancariaMasivo_Update_CuentaBancariaAutorizada.sql");        
    private final String SQL_RECHAZA_AUTORIZACION_CUENTA_BANCARIA = PlataformaUtils.loadResourceToString(
        "cl/bcs/socionegocio/per/sql/AutorizacionModificacionPersonaCuentaBancariaMasivo_Update_EstadoSolicitud.sql");

    public static class CuentaBancariaAutorizacion {
        public Long IdCuentaBancaria;
        public String IdEstadoAutorizacion;
        public String IdConcepto;
        public Long IdPersona;
        public String EsPac;
        public String IdBanco;
        public String IdPlaza;
        public String IdMoneda;
        public String IdEstado;
        public String CodigoAba;
        public String CodigoIban;
        public String IdentificadorCuenta;
        public String IdPersonaRelacionada;
        public String IdTipoCuentaBancaria;
        public String NumeroCuentaBancaria;
        public String CodigoCuentaInterbancario;
        public String IdTipoIdentificadorCuenta;
        public String OtroCodigoBancoExtranjero;
        public String OtroCodigoCuentaExtranjero;
    }

    @Override
    public SalidaAutorizacion autorizarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionPersonaCuentaBancariaMasivo.autorizarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<CuentaBancariaAutorizacion> cuentas = entrada.p_Lista.stream()
                .map(evento -> {
                    CuentaBancariaAutorizacion c = new CuentaBancariaAutorizacion();
                    c.IdCuentaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
                    c.IdEstadoAutorizacion = "AUT";
                    c.IdConcepto = evento.IdTipoEvento;
                    // Los demás campos quedan en null (no se actualizan)
                    return c;
                })
                .distinct()
                .collect(Collectors.toList());
            sql.batch(conn, SQL_UPDATE_DATOS_CUENTA_BANCARIA, cuentas, CuentaBancariaAutorizacion.class);
            for (CuentaBancariaAutorizacion cuenta : cuentas) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cuenta;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en autorizarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }
        logger.info("FIN::AutorizacionModificacionPersonaCuentaBancariaMasivo.autorizarEvento");
        return salida;
    }

    @Override
    public SalidaAutorizacion rechazarEvento(Connection conn, EntradaAutorizacion entrada) {
        logger.info("INICIO::AutorizacionModificacionPersonaCuentaBancariaMasivo.rechazarEvento");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        try {
            List<CuentaBancariaAutorizacion> cuentas = entrada.p_Lista.stream()
                .map(evento -> {
                    CuentaBancariaAutorizacion c = new CuentaBancariaAutorizacion();
                    c.IdCuentaBancaria = Long.valueOf(evento.NumeroOperacionConcepto);
                    c.IdEstadoAutorizacion = "RZD";
                    c.IdConcepto = evento.IdTipoEvento;
                    // Para el SQL, IdEstado solo se cambia si el concepto es NUECTB
                    if ("NUECTB".equals(c.IdConcepto)) {
                        c.IdEstado = "INA";
                    } else {
                        c.IdEstado = null; // No se actualiza
                    }
                    return c;
                })
                .distinct()
                .collect(Collectors.toList());
            // Usar un SQL que no tenga CASE ni parámetros en el CASE
            // El SQL debe ser:
            // UPDATE ... SET IdEstadoAutorizacion = 'RZD', IdEstado = :IdEstado WHERE IdCuentaBancaria = :IdCuentaBancaria
            sql.batch(conn, SQL_RECHAZA_AUTORIZACION_CUENTA_BANCARIA, cuentas, CuentaBancariaAutorizacion.class);
            List<EntidadOrigen> entidades = cuentas.stream().map(u -> {
                EntidadOrigen eo = new EntidadOrigen();
                eo.IdEntidadOrigen = String.valueOf(u.IdCuentaBancaria);
                eo.IdTipoEntidad = "CUENTABANCARIA";
                return eo;
            }).collect(Collectors.toList());
            EntradaModificarEstado entradaSolicitud = new EntradaModificarEstado();
            entradaSolicitud.p_IdEmpresa = entrada.p_IdEmpresa;
            entradaSolicitud.p_IdUsuario = entrada.p_IdUsuario;
            entradaSolicitud.p_IdModulo = entrada.p_IdModulo;
            entradaSolicitud.p_EntidadOrigen = entidades;
            entradaSolicitud.p_IdEstadoAutorizacion = "RZD";
            try {
                new ModificarEstadoRegistroSolicitudCambioMasivo().logica(conn, entradaSolicitud);
            } catch (Exception ex) {
                logger.error("Error en ModificarEstadoRegistroSolicitudCambioMasivo (rechazo): " + ex.getMessage(), ex);
                salida.P_OUT_ERRORESCONTADOR = 1L;
            }
            for (CuentaBancariaAutorizacion cuenta : cuentas) {
                BcsAutorizaciones.ResultadoAutorizacion resultado = new BcsAutorizaciones.ResultadoAutorizacion();
                resultado.evento = cuenta;
                resultado.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(resultado);
            }
        } catch (Exception e) {
            logger.error("Error en rechazarEvento: " + e.getMessage(), e);
            salida.P_OUT_ERRORESCONTADOR = 1L;
        }
        logger.info("FIN::AutorizacionModificacionPersonaCuentaBancariaMasivo.rechazarEvento");
        return salida;
    }
    
    public static void main(String[] args) {
        System.out.println("== INICIO TEST ==");
        try (Connection conn = PlataformaUtils.getPropertiesConnection("desa01", "connections.properties")) {
            try {
                String json = "{\"p_IdEmpresa\":1,\"p_IdUsuario\":76816,\"p_IdModulo\":\"AUT\",\"p_simularOperacion\":\"N\",\"p_UsaReglasNegocio\":\"N\",\"p_Comentario\":\"\",\"p_Lista\":[{\"IdEvento\":832489,\"NumeroOperacionConcepto\":\"679718\",\"IdTipoEvento\":\"MODCTB\"}]}";
                EntradaAutorizacion entrada = Utils.getGsonBuilder().fromJson(json, EntradaAutorizacion.class);
                AutorizacionModificacionPersonaCuentaBancariaMasivo servicio = new AutorizacionModificacionPersonaCuentaBancariaMasivo();
                // Probar AUTORIZAR
                System.out.println("\n=== Probando AUTORIZAR ===");
                SalidaAutorizacion salidaAutorizar = servicio.autorizarEvento(conn, entrada);
                System.out.println("Salida autorizar: " + salidaAutorizar);
                System.out.println("Errores: " + salidaAutorizar.P_OUT_ERRORESCONTADOR);
                conn.rollback();
                // Probar RECHAZAR
                System.out.println("\n=== Probando RECHAZAR ===");
                SalidaAutorizacion salidaRechazar = servicio.rechazarEvento(conn, entrada);
                System.out.println("Salida rechazar: " + salidaRechazar);
                System.out.println("Errores: " + salidaRechazar.P_OUT_ERRORESCONTADOR);
                conn.rollback();
            } catch (Exception e) {
                e.printStackTrace();
                conn.rollback();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("== FIN TEST ==");
    }
}
