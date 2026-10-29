SELECT TOP 1 "IdArchivo" as "Id"
FROM 
(
SELECT 1 AS "ORDEN", "IdArchivo", "VersionArchivo" FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" AR
WHERE "IdTipoDocumento"=:IdTipoDocumento AND "VersionArchivo"=:VersionArchivo  AND "IdEmpresa"=:IdEmpresa
AND "IdArchivo" <>'X'
UNION ALL 

SELECT TOP 1  2 AS "ORDEN", "IdArchivo", "VersionArchivo"  FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" AR
WHERE "IdTipoDocumento"=:IdTipoDocumento AND "VersionArchivo" < :VersionArchivo  AND "IdEmpresa"=:IdEmpresa
ORDER BY "VersionArchivo" DESC
);
