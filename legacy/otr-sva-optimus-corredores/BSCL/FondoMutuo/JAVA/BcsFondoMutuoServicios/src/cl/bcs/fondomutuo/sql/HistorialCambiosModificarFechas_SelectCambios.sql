SELECT 
"IdEvento",
"IdUsuario" AS "UsuarioModificador"
FROM "BSCL.Plataforma.BD::AUT.Evento"
WHERE "IdEvento" = :IdEvento 