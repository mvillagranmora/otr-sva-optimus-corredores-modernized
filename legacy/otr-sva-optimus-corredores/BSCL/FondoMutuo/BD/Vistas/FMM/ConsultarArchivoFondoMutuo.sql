SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarArchivoFondoMutuo";
GO
CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarArchivoFondoMutuo"
AS
SELECT
    ARC."IdArchivoFondoMutuo",
	ARC."VersionArchivo",
	ARC."Esquema",
	ARC."Version",
	ARC."IdTipoDocumento",
	ARC."Descripcion",
	ARC."NombreArchivo",
	--ARC."IdArchivo",
	COALESCE(TEM."Id",ARC."IdArchivo") AS "IdArchivo",
	--COALESCE(TEM."Id",'X') AS "IdArchivo",
	ARC."TipoArchivo",
	ARC."ServicioArchivo",
	ARC."IdEmpresa",
	ARC."IdEstado"
FROM "BSCL.FondoMutuo.BD::FMM.ArchivoFondoMutuo" AS ARC
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" TEM
ON (UPPER(TEM."ReportName")=UPPER(ARC."NombreArchivo") AND TEM."ReportVersion"=ARC."VersionArchivo"  AND ARC."IdEmpresa"=TEM."CompanyId");
GO
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarArchivoFondoMutuo" TO BCS_DESARROLLADOR WITH GRANT OPTION;