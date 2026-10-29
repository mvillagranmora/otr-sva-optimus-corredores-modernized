SELECT "IdTipoCliente"
,"TipoEnrolamiento"
FROM "BSCL.SocioNegocio.BD::CLI.TipoCliente"
WHERE "IdEmpresa" = :IdEmpresa
AND "PorDefecto" = 'S';