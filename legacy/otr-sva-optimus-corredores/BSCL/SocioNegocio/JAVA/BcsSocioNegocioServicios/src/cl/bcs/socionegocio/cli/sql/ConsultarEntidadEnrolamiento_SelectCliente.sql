SELECT 
'S' AS "EntidadExiste"
, "IdCliente" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::CLI.Cliente" 
WHERE "IdPersona" = :IdPersona AND "IdEmpresa" = :IdEmpresa;