package cl.bcs.plataforma.adapter.in.rest;

import org.springframework.web.bind.annotation.*;

/**
 * Stub controller for access/user management (ACC) operations.
 * Legacy package: cl.bcs.plataforma.acc (22 classes)
 *
 * Covers:
 *   - ActualizarPerfilAcceso, ActualizarRol, ActualizarUsuario
 *   - AgregarRolUsuario, AsignarPerfilRol, EliminarPerfilRol, EliminarRolUsuario
 *   - BloqueoUsuariosPorVigencia
 *   - CambiarClaveUsuario, CambiarClaveUsuarioReplica, CambiarClaveUsuarioWS
 *   - CrearAutorizadoOrden, CrearClaveICliente, CrearPerfil, CrearRol, CrearUsuario
 *   - ExportarPerfiles, ExportarRoles
 *   - ObtenerTokenOptimusClientes
 *   - ValidarClaveUsuario, ValidaSesionUsuarioCliente
 *
 * TODO: wire when PLA.Servicio DDL + SELECT * export is provided.
 */
@RestController
@RequestMapping("/api/plataforma/acceso")
public class AccesoController {

    @PostMapping("/usuario/crear")
    public Object crearUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Usuario not yet wired");
    }

    @PostMapping("/usuario/actualizar")
    public Object actualizarUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Usuario not yet wired");
    }

    @PostMapping("/usuario/bloqueo-vigencia")
    public Object bloqueoUsuariosPorVigencia(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Usuario not yet wired");
    }

    @PostMapping("/usuario/cambiar-clave")
    public Object cambiarClaveUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Clave not yet wired");
    }

    @PostMapping("/usuario/validar-clave")
    public Object validarClaveUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Clave not yet wired");
    }

    @PostMapping("/usuario/validar-sesion")
    public Object validaSesionUsuarioCliente(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Sesion not yet wired");
    }

    @PostMapping("/usuario/obtener-token-optimus")
    public Object obtenerTokenOptimusClientes(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Token not yet wired");
    }

    @PostMapping("/rol/crear")
    public Object crearRol(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Rol not yet wired");
    }

    @PostMapping("/rol/actualizar")
    public Object actualizarRol(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Rol not yet wired");
    }

    @PostMapping("/rol/agregar-usuario")
    public Object agregarRolUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.RolUsuario not yet wired");
    }

    @PostMapping("/rol/eliminar-usuario")
    public Object eliminarRolUsuario(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.RolUsuario not yet wired");
    }

    @PostMapping("/perfil/crear")
    public Object crearPerfil(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Perfil not yet wired");
    }

    @PostMapping("/perfil/actualizar")
    public Object actualizarPerfilAcceso(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Perfil not yet wired");
    }

    @PostMapping("/perfil/asignar-rol")
    public Object asignarPerfilRol(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.PerfilRol not yet wired");
    }

    @PostMapping("/perfil/eliminar-rol")
    public Object eliminarPerfilRol(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.PerfilRol not yet wired");
    }

    @PostMapping("/perfil/exportar")
    public Object exportarPerfiles(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Perfil not yet wired");
    }

    @PostMapping("/rol/exportar")
    public Object exportarRoles(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.Rol not yet wired");
    }

    @PostMapping("/autorizado-orden/crear")
    public Object crearAutorizadoOrden(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.AutorizadoOrden not yet wired");
    }

    @PostMapping("/clave-icliente/crear")
    public Object crearClaveICliente(@RequestBody Object request) {
        throw new UnsupportedOperationException("requires HANA — ACC.ClaveICliente not yet wired");
    }
}
