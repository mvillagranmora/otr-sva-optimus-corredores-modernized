SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.ValidaValorizar";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1874.ValidaValorizar" (
   
	"Version",
	"IdEmpresa",
	"ID",  	
	"FuenteDatos",
	"Resultado"  	

) AS
SELECT 
		DOC."Version",
		DOC."IdEmpresa",
		DJ."ID",
		DJ."FuenteDatos",
		CASE WHEN DJ."NemotecnicoSVS"='' OR DJ."NemotecnicoSVS"=NULL THEN '<NemotecnicoSVS>' ELSE '' END ||
		CASE WHEN DJ."IdMonedaInstrumento"='' OR DJ."IdMonedaInstrumento"=NULL THEN '<IdMonedaInstrumento>' ELSE '' END ||
		CASE WHEN DJ."IdMonedaEmisionInstrumento"='' OR DJ."IdMonedaEmisionInstrumento"=NULL THEN '<IdMonedaEmisionInstrumento>' ELSE '' END ||
		CASE WHEN DJ."IdEmisor"='' OR DJ."IdEmisor"=NULL THEN '<IdEmisor>' ELSE '' END ||
		CASE WHEN DJ."IdClasificacionTipoInstrumento"='' OR DJ."IdClasificacionTipoInstrumento"=NULL THEN '<IdClasificacionTipoInstrumento>' ELSE ''  END ||
		CASE WHEN DJ."IdTipoInstrumento"='' OR DJ."IdTipoInstrumento"=NULL THEN '<IdTipoInstrumento>'ELSE ''  END ||
		CASE WHEN DJ."Cantidad"= NULL THEN '<Cantidad>' ELSE ''  END ||
		CASE WHEN DJ."Tasa"= NULL THEN '<Tasa>'ELSE ''  END ||
		'' AS "Resultado"
		--,* 
		FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1874.PreDatos"   AS DJ
		INNER JOIN "BSCL.Normativa.BD::DOC.Documento" DOC ON (DOC."IdDocumento"=DJ."IdDocumento")
		 WHERE     DJ."TipoDocumento" IN (1,2,4,6)
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1874.ValidaValorizar" TO bcs_desarrollador WITH GRANT OPTION;