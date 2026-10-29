UPDATE "BSCL.Plataforma.BD::ACC.Usuario"
SET "ClaveProvisoria" = :p_ClaveProvisoria,
"FechaClaveProvisoria" = CURRENT_UTCTIMESTAMP,
"DebeCambiarClave" ='S',
"UsuarioClientes"='S',
"EsAutorizado"='S'
WHERE "CodigoUsuario" = :p_IdUsuario;