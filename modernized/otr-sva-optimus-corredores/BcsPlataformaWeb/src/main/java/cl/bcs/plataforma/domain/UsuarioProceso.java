package cl.bcs.plataforma.domain;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * User data fetched from ACC.Usuario.
 * Source: CrearSesion.UsuarioProceso
 */
public class UsuarioProceso {

    public Long      IdUsuario;
    public String    Clave;
    public Date      FechaVigencia;
    public String    IdEstado;
    public Integer   IntentosConexion;
    public Timestamp FechaBloqueoTemporal;
    public String    EsAutorizado;
    public String    TipoEntidad;
    public Long      IdEntidad;
    public String    UsuarioClientes;
    public String    UsuarioNegocios;
    public String    UsuarioServicios;
    public String    UsuarioCorredores;
    public String    IdTokenSesion;
    public String    Param_FechaSistema;
    public String    Param_IntentosMaximo;
    public Timestamp CurrentTimestamp;
    public String    Param_ClaveUsuarioTiempoBloqueo;
    public String    IdCanal;
}
