UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
SET "FechaVigencia" = :FechaVigencia, "IdEstado" = :IdEstado, "Clave" = :Clave,
"DebeCambiarClave" = :DebeCambiarClave, "UltimoCambioClave" = :UltimoCambioClave,
"UsuarioCorredores" = :UsuarioCorredores, "UsuarioNegocios" = :UsuarioNegocios,
"UsuarioClientes" = :UsuarioClientes, "UsuarioServicios" = :UsuarioServicios,
"IdTipoBloqueo" = :IdTipoBloqueo, "ComentarioBloqueo" = :ComentarioBloqueo
WHERE "IdUsuario" = :IdUsuario;
