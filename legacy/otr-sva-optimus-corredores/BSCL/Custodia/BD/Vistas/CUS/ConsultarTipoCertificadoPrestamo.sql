SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCertificadoPrestamo";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCertificadoPrestamo"
AS 
SELECT
	CRT."IdTipoCertificadoPrestamo"
	,CRT."NombreTipoCertificadoPrestamo"
	,CRT."IdArchivoCustodia"
	,TEM."Id" AS "IdReporte"
	,TC."NombreArchivo"
	,TC."ServicioArchivo"	
	,SER."RutaServicio"
	,TC."Descripcion"
	,TC."IdEmpresa"
	,TC."IdEstado"	
	,TEM."ReportName"
	,TEM."ReportVersion"	
	,TC."TipoArchivo"  -- txt/pdf
FROM "BSCL.Custodia.BD::CUS.TipoCertificadoPrestamo" CRT
LEFT JOIN "BSCL.Custodia.BD::CUS.ArchivoCustodia"  AS TC ON (TC."IdArchivoCustodia" = CRT."IdArchivoCustodia")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTemplate" AS TEM ON (TC."IdArchivo" = TEM."Id")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Servicio" AS SER ON (SER."IdServicio" = TC."ServicioArchivo");
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTipoCertificadoPrestamo" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
GO



