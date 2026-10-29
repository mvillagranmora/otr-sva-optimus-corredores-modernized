SELECT "IdUsuario"
,"IdEmpresa"
,"CodigoUsuario"
,"IdEstado"
FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE "IdEmpresa" = :IdEmpresa
AND "IdUsuario" IN (:IdsUsuarios);