package cl.bcs.plataforma.login.sesion;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import cl.bcs.plataforma.login.jdbc.SqlNombrado;

/**
 * The data of the session behind a token (legacy {@code Sesion.getSesionData}, {@code Sesion.java:204-302}): the user, the company and the broker of
 * the company, the default values of the system and four parameters of the platform. Answers an error (never an exception) when the session is
 * invalid, the user is blocked or expired, has no employee or client, or the company is badly configured. When it is valid it WRITES: one more use of
 * the token ({@code ACC.Sesion}) and the connected flag of the user ({@code ACC.Usuario}); the caller commits them, or rolls back on an error.
 *
 * <p>Faithful to the legacy: a user without employee row (client and person users) is a NullPointerException in the "is a valid user" test; the
 * bootstrap of the home is served to employees.
 */
@Component
public class SesionDatos {

    /** The session as the home page reads it ({@code BCS_SESION}); field order and names are the legacy JSON. */
    public static class SesionData {
        public Long IdEmpresa;
        public String IdTokenSesion;
        public Long IdUsuario;
        public String NombreEmpresa;
        public String NombreUsuario;
        public String CodigoMenuUsuario;
        public Long IdSocioDeNegocio;
        public Long IdPersonaCorredora;
        public String IdMonedaSistema;
        public Long IdPaisSistema;
        public String FechaSistema;
        public String BolsaDefectoSistema;
        public List<ValorDefectoSistema> ValoresDefectoSistema;
        public Long IdEmpleado;
        public Long IdCliente;
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
        public Long IdCorredoraSistema;
        public String IdEntidadSSO;
        public String SSORedirectURL;
    }

    public static class Mensaje {
        public String Codigo;
        public String Descripcion;

        public Mensaje(String codigo, String descripcion) {
            this.Codigo = codigo;
            this.Descripcion = descripcion;
        }
    }

    public static class ValorDefectoSistema {
        public String IdValorDefectoSistema;
        public String Nombre;
        public String Valor;
        public Long IdEmpresa;
        public String IdEstado;
    }

    /** Row of the session + user query. */
    public static class InformacionUsuario {
        public String IdTokenUsuario;
        public Long IdUsuario;
        public Long IdEmpresa;
        public java.sql.Date FechaVigencia;
        public String IdEstado;
        public String CodigoUsuario;
        public Long IdEntidad;
        public String CodigoMenu;
        public Long IdEmpleado;
        public Long IdCliente;
        public String IdEntidadSSO;
        public String RedirectURL;
    }

    public static class Configuracion {
        public String CodigoParametro;
        public String Valor;
    }

    public static class Empresa {
        public Long IdPais;
        public String Nombre;
        public Long IdSocioDeNegocio;
        public Long IdPersona;
        public Long IdCorredora;
    }

    /** Update of the token and the user (the legacy batch with the {@code InformacionUsuario} row). */
    static final String QUERY_USUARIO = "SELECT U.\"IdUsuario\",U.\"IdEmpresa\",\"FechaVigencia\",S.\"IdEntidadSSO\",SSO.\"RedirectURL\",U.\"IdEstado\",U.\"CodigoUsuario\","
            + "U.\"IdEntidad\",U.\"CodigoMenu\",EMP.\"IdEmpleado\",CLI.\"IdCliente\" FROM \"BSCL.Plataforma.BD::ACC.Sesion\" S "
            + "INNER JOIN \"BSCL.Plataforma.BD::ACC.Usuario\" U on U.\"IdUsuario\" = S.\"IdUsuario\" "
            + "LEFT JOIN \"BSCL.Plataforma.BD::ACC.ConfiguracionSSO\" SSO on SSO.\"IdEntidad\" = S.\"IdEntidadSSO\" "
            + "LEFT JOIN \"BSCL.SocioNegocio.BD::EMP.Empleado\" EMP ON U.\"TipoEntidad\" = 'EMP' AND U.\"IdEntidad\" = EMP.\"IdEmpleado\" "
            + "LEFT JOIN \"BSCL.SocioNegocio.BD::CLI.Cliente\" CLI ON U.\"TipoEntidad\" = 'CLI' AND U.\"IdEntidad\" = CLI.\"IdCliente\" "
            + "WHERE \"IdTokenSesion\" = :IdTokenSesion AND \"TokenValido\" = 'S'";

    static final String QUERY_PARAMETROS = "SELECT \"CodigoParametro\",\"Valor\" FROM \"BSCL.Plataforma.BD::PLA.Configuracion\" p "
            + "WHERE \"IdEmpresa\" = :IdEmpresa AND \"CodigoParametro\" IN (:Parametros)";

    static final String QUERY_EMPRESA = "SELECT e.\"IdPais\",e.\"Nombre\",e.\"IdSocioDeNegocio\",soc.\"IdPersona\",c.\"IdCorredora\" FROM \"BSCL.Plataforma.BD::PLA.Empresa\" e "
            + "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" soc on e.\"IdSocioDeNegocio\" = soc.\"IdSocioDeNegocio\" "
            + "LEFT JOIN \"BSCL.Plataforma.BD::INS.Corredora\" c ON c.\"IdPersona\" = soc.\"IdPersona\" WHERE e.\"IdEmpresa\" = :IdEmpresa";

    static final String QUERY_VALORES_DEFECTO = "SELECT \"IdValorDefectoSistema\",\"Nombre\",\"Valor\",\"IdEmpresa\",\"IdEstado\" FROM \"BSCL.Plataforma.BD::PLA.ValorDefectoSistema\" "
            + "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdEstado\" = 'ACT'";

    static final String UPDATE_SESION = "UPDATE \"BSCL.Plataforma.BD::ACC.Sesion\" SET \"FechaUltimoUso\" = CURRENT_UTCTIMESTAMP,\"UsoToken\" = \"UsoToken\" + 1 "
            + "WHERE \"IdTokenSesion\" = :IdTokenUsuario";

    static final String UPDATE_USUARIO = "UPDATE \"BSCL.Plataforma.BD::ACC.Usuario\" SET \"FechaUltimaConexion\" = CURRENT_UTCTIMESTAMP,\"Conectado\" = 'S',\"Hora\" = CURRENT_UTCTIMESTAMP "
            + "WHERE \"IdEmpresa\" = :IdEmpresa AND \"IdUsuario\" = :IdUsuario";

    public SesionData getSesionData(Connection conn, String token) throws Exception {
        SesionData s = new SesionData();

        Map<String, Object> params = new HashMap<>();
        params.put("IdTokenSesion", token);
        List<InformacionUsuario> informacion = SqlNombrado.consultar(conn, QUERY_USUARIO, params, InformacionUsuario.class);
        if (informacion.size() == 0) {
            return error(s, "SESION_INVALIDA", "Sesion invalida");
        }
        InformacionUsuario u = informacion.get(0);
        u.IdTokenUsuario = token;

        // blocked user
        if (!u.IdEstado.equalsIgnoreCase("ACT")) {
            return error(s, "USUARIO_BLOQUEADO", "El usuario " + u.CodigoUsuario + " se encuentra bloqueado.");
        }

        Map<String, Configuracion> parametros = new HashMap<>();
        {
            List<Object> lectura = new ArrayList<>(List.of("PLA.ZonaHoraria", "PLA.FechaSistema", "PLA.MonedaSistema", "PLA.BolsaDefectoSistema"));
            params.clear();
            params.put("IdEmpresa", u.IdEmpresa);
            params.put("Parametros", lectura);
            for (Configuracion c : SqlNombrado.consultar(conn, QUERY_PARAMETROS, params, Configuracion.class)) {
                if (parametros.containsKey(c.CodigoParametro)) {
                    throw new Exception("key field is not unique");   // legacy Utils.List2Hash
                }
                parametros.put(c.CodigoParametro, c);
            }
        }

        // expired user
        if (!fechaTexto(u.FechaVigencia).equalsIgnoreCase("0001-01-01")
                && u.FechaVigencia.getTime() < java.sql.Date.valueOf(java.time.LocalDate.parse(parametros.get("PLA.FechaSistema").Valor)).getTime()) {
            return error(s, "USUARIO_BLOQUEADO", "El usuario " + u.CodigoUsuario + " se encuentra vencido.");
        }

        // valid user (employee or client)
        if (u.IdEmpleado == 0 && u.IdCliente == 0) {
            return error(s, "USUARIO_NO_VALIDO", "El usuario " + u.CodigoUsuario + " no es valido.");
        }

        params.clear();
        params.put("IdEmpresa", u.IdEmpresa);
        List<Empresa> empresa = SqlNombrado.consultar(conn, QUERY_EMPRESA, params, Empresa.class);
        if (empresa.size() != 1) {
            return error(s, "EMPRESA_MAL_CONFIGURADA", "La empresa no se encuentra configurada correctamente.");
        }

        params.clear();
        params.put("IdEmpresa", u.IdEmpresa);
        List<ValorDefectoSistema> valoresDefecto = SqlNombrado.consultar(conn, QUERY_VALORES_DEFECTO, params, ValorDefectoSistema.class);

        s.BolsaDefectoSistema = parametros.get("PLA.BolsaDefectoSistema").Valor;
        s.CodigoMenuUsuario = u.CodigoMenu;
        s.FechaSistema = parametros.get("PLA.FechaSistema").Valor;
        s.IdCliente = u.IdCliente;
        s.IdEmpleado = u.IdEmpleado;
        s.IdEmpresa = u.IdEmpresa;
        s.IdMonedaSistema = parametros.get("PLA.MonedaSistema").Valor;
        s.IdPaisSistema = empresa.get(0).IdPais;
        s.IdPersonaCorredora = empresa.get(0).IdPersona;
        s.IdSocioDeNegocio = empresa.get(0).IdSocioDeNegocio;
        s.IdUsuario = u.IdUsuario;
        s.NombreEmpresa = empresa.get(0).Nombre;
        s.NombreUsuario = u.CodigoUsuario;
        s.IdCorredoraSistema = empresa.get(0).IdCorredora;
        s.IdEntidadSSO = u.IdEntidadSSO;
        s.SSORedirectURL = u.RedirectURL;
        s.ValoresDefectoSistema = valoresDefecto;

        // use of the token and connected flag
        Map<String, Object> pt = new HashMap<>();
        pt.put("IdTokenUsuario", token);
        SqlNombrado.actualizar(conn, UPDATE_SESION, pt);
        Map<String, Object> pu = new HashMap<>();
        pu.put("IdEmpresa", u.IdEmpresa);
        pu.put("IdUsuario", u.IdUsuario);
        SqlNombrado.actualizar(conn, UPDATE_USUARIO, pu);
        return s;
    }

    private static SesionData error(SesionData s, String codigo, String descripcion) {
        s.P_OUT_MENSAJES.add(new Mensaje(codigo, descripcion));
        s.P_OUT_ERRORESCONTADOR = 1L;
        return s;
    }

    private static String fechaTexto(java.sql.Date fecha) {
        return fecha == null ? "" : fecha.toString();
    }
}
