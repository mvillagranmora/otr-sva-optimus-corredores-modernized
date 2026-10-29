SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" AS

SELECT
    ARC."IdArchivoNormativa",
	ARC."VersionArchivo",
	ARC."Esquema",
	ARC."Version",
	ARC."IdTipoDocumento",
	ARC."Descripcion",
	ARC."NombreArchivo",
	COALESCE(TEM."Id",'X') AS "IdArchivo",
	ARC."TipoArchivo",
	ARC."ServicioArchivo",
	ARC."IdEmpresa",
	ARC."IdEstado"	
FROM "BSCL.Normativa.BD::NOR.ArchivoNormativa" AS ARC
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" TEM
ON (UPPER(TEM."ReportName")=UPPER(ARC."NombreArchivo") AND TEM."ReportVersion"=ARC."VersionArchivo"  AND ARC."IdEmpresa"=TEM."CompanyId");


GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarArchivoNormativa" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
