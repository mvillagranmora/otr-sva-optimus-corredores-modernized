SELECT 
'S' AS "EntidadExiste"
, "IdContraparte" AS "IdEntidad"
FROM "BSCL.SocioNegocio.BD::CNT.Contraparte" 
WHERE "IdPersona" = :IdPersona AND "IdEmpresa" = :IdEmpresa;