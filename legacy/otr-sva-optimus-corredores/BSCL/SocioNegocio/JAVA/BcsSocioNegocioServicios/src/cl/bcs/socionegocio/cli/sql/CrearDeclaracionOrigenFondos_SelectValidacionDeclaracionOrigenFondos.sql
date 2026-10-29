SELECT "IdDeclaracionOrigenFondos"
FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" 
WHERE "IdCliente"= :IdCliente
AND "IdTipoOperacionOrigenFondos" = :IdTipoOperacionOrigenFondos
AND TO_DATE("FechaDeclaracion") = TO_DATE(:FechaDeclaracion)