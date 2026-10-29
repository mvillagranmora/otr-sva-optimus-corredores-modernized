SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarArchivoCustodia";
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarArchivoCustodia" 
AS
SELECT
    ARC."IdArchivoCustodia",
	ARC."VersionArchivo",
	ARC."Esquema",
	ARC."Version",
	ARC."IdTipoDocumento",
	ARC."Descripcion",
	ARC."NombreArchivo",
	ARC."IdArchivo",
	ARC."TipoArchivo",
	ARC."ServicioArchivo",
	ARC."IdEmpresa",
	ARC."IdEstado"
FROM "BSCL.Custodia.BD::CUS.ArchivoCustodia" AS ARC;

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarArchivoCustodia" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
