SELECT DISTINCT( Doc."IdTipoDocumento") , max (Doc."IdUltimoAnexo" ) AS "Anexo", CDoc."IdTipoCanalIngreso" AS "CanalIngreso"   FROM  "BSCL.Plataforma.BD::DOC.Documento" Doc
INNER JOIN "BSCL.Plataforma.BD::DOC.CanalesDocumento" CDoc ON CDoc."IdTipoDocumento" = Doc."IdTipoDocumento" 
WHERE Doc."IdUltimoAnexo" = :AnexoDocumento 
AND CDoc."IdTipoCanalIngreso" = :Canal
GROUP BY Doc."IdTipoDocumento" , CDoc."IdTipoCanalIngreso" 