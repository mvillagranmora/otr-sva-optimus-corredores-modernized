package cl.bcs.fondomutuo.adapter.out.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.kit.RevisarRequisitosBasicosSalida;
import cl.bcs.fondomutuo.port.out.kit.DiccionarioMensajesPort;
import cl.bcs.fondomutuo.port.out.kit.RevisarRequisitosBasicosPort;

/**
 * {@link RevisarRequisitosBasicosPort}: legacy {@code RevisarRequisitosBasicos.logica} (Plataforma {@code pla}), same checks in the same order, same message
 * codes, the 3 legacy SQL verbatim. Reads only. Message ids are 0 and the type {@code ERROR}, as the legacy. When {@code p_IdEmpresa} is null the legacy
 * returned right after {@code EMPRESA_VACIA} (user and module are not checked).
 */
@Component
public class RevisarRequisitosBasicosJdbc implements RevisarRequisitosBasicosPort {

    public static class EmpresaSocio {
        public Long IdEmpresa;
        public Long IdSocioDeNegocioEmpresa;
        public Long IdSocioDeNegocio;
        public Long IdContraparte;
        public Long IdCliente;
    }

    public static class Modulo {
        public String IdModulo;
    }

    public static class Usuario {
        public Long IdUsuario;
        public String IdEstado;
    }

    private static final String SQL_EMPRESA_SOCIO = SqlRecursos.kit("RevisarRequisitosBasicos_ConsultarSocioDeNegocio.sql");
    private static final String SQL_USUARIO = SqlRecursos.kit("RevisarRequisitosBasicos_ConsultarUsuario.sql");
    private static final String SQL_MODULO = SqlRecursos.kit("RevisarRequisitosBasicos_ConsultarModulo.sql");

    private final ConexionUnidadDeTrabajo conexion;
    private final DiccionarioMensajesPort diccionario;

    public RevisarRequisitosBasicosJdbc(ConexionUnidadDeTrabajo conexion, DiccionarioMensajesPort diccionario) {
        this.conexion = conexion;
        this.diccionario = diccionario;
    }

    private void error(RevisarRequisitosBasicosSalida salida, Long idEmpresa, String codigo) {
        salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, codigo, diccionario.mensaje(idEmpresa, codigo)));
    }

    @Override
    public RevisarRequisitosBasicosSalida revisar(Long idEmpresa, Long idUsuario, String idModulo) {
        RevisarRequisitosBasicosSalida salida = new RevisarRequisitosBasicosSalida();
        if (idEmpresa == null) {
            salida.p_out_Mensajes.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "EMPRESA_VACIA", "Empresa vacia"));
            return salida;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("IdEmpresa", idEmpresa);
        List<EmpresaSocio> datos = conexion.usar(con -> SqlNombrado.consultar(con, SQL_EMPRESA_SOCIO, params, EmpresaSocio.class));
        if (datos.isEmpty()) {
            error(salida, idEmpresa, "EMPRESA_NO_EXISTE");
        } else {
            EmpresaSocio d = datos.get(0);
            if (d.IdSocioDeNegocioEmpresa == null || d.IdSocioDeNegocioEmpresa == 0) {
                error(salida, idEmpresa, "SIN_DEFINIR_SOCIO_DE_NEGOCIO_PARA_EMPRESA");
            }
            if (d.IdSocioDeNegocio == null || d.IdSocioDeNegocio == 0) {
                error(salida, idEmpresa, "SOCIO_DE_NEGOCIO_DE_EMPRESA_NO_EXISTE");
            }
            if (d.IdCliente == null || d.IdCliente == 0) {
                error(salida, idEmpresa, "EMPRESA_NO_CONFIGURADA_COMO_CLIENTE");
            }
        }
        if (idUsuario == null) {
            error(salida, idEmpresa, "USUARIO_VACIO");
        } else {
            Map<String, Object> pu = new HashMap<>();
            pu.put("IdUsuario", idUsuario);
            pu.put("IdEmpresa", idEmpresa);
            List<Usuario> usuarios = conexion.usar(con -> SqlNombrado.consultar(con, SQL_USUARIO, pu, Usuario.class));
            if (usuarios.isEmpty()) {
                error(salida, idEmpresa, "USUARIO_NO_EXISTE");
            } else if (!usuarios.get(0).IdEstado.equalsIgnoreCase("ACT")) {
                error(salida, idEmpresa, "USUARIO_NO_VIGENTE");
            }
        }
        if (idModulo == null) {
            error(salida, idEmpresa, "MODULO_VACIO");
        } else {
            Map<String, Object> pm = new HashMap<>();
            pm.put("IdModulo", idModulo);
            List<Modulo> modulos = conexion.usar(con -> SqlNombrado.consultar(con, SQL_MODULO, pm, Modulo.class));
            if (modulos.isEmpty()) {
                error(salida, idEmpresa, "MODULO_NO_EXISTE");
            }
        }
        return salida;
    }
}
