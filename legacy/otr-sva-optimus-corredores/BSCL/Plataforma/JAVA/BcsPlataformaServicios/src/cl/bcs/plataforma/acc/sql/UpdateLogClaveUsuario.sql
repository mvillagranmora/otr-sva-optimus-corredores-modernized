UPDATE "BSCL.Plataforma.BD::ACC.Usuario" 
SET "Clave" = :Clave,
"DebeCambiarClave" = 'N',
"UltimoCambioClave" = CURRENT_UTCTIMESTAMP
WHERE "CodigoUsuario" = :CodigoUsuario
AND "IdEmpresa" = :IdEmpresa;


