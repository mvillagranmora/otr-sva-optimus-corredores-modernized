SELECT "IdUsuario"
FROM "BSCL.Plataforma.BD::ACC.Usuario"
WHERE UPPER("CodigoUsuario") = UPPER(:CodigoUsuario)
  AND "IdEmpresa" = :IdEmpresa;