SELECT 
'S' AS "EntidadExiste"
, "IdProveedor" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::PRO.Proveedor" 
WHERE "IdPersona" = :IdPersona AND "IdEmpresa" = :IdEmpresa;