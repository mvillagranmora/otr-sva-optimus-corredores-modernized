SELECT "IdTipoCliente"
FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente"
WHERE "IdEmpresa" = :IdEmpresa
AND "TipoEnrolamiento" = :TipoEnrolamiento
AND "PorDefecto" = 'S';