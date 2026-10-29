SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Origen" (
     "IdEmpresa"
	,"IdCartera"
	,"FechaOperacion"
	,"Numero"
	,"RutEmisor"
	,"RutTenedor"
	,"TipoContribuyenteRutTenedor"
	,"NemotecnicoSVS"
	,"InteresDevengado"
	,"RetencionImpuesto"
	,"IdCliente"
	,"IdPortafolio"
) AS
SELECT 
     CARTERA."IdEmpresa"
	,CARTERA."IdCartera"
	,CARTERA."INGFechaOperacion" AS "FechaOperacion"
	,CARTERA."IdCartera" AS "Numero"
	,COALESCE(EMISOR."DocumentoIdentidad",'') AS "RutEmisor"
	,COALESCE(CLIENTE."DocumentoIdentidad",'') AS "RutTenedor"
	,CASE WHEN PAIS$1."CodigoPais" = 'CL' OR PAIS$2."CodigoPais" = 'CL' THEN 1
	 ELSE 2
	 END AS "TipoContribuyenteRutTenedor"
	,CARTERA."NemoSVS" AS "NemotecnicoSVS"
	,CARTERA."INGMontoDevengoTipoCambio" AS "InteresDevengado"
	, 0 AS "RetencionImpuesto"
	,CARTERA."IdCliente"
	,CARTERA."IdPortafolio"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CARTERA
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = CARTERA."IdCliente")
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS$1 ON (PAIS$1."IdPais" = CLIENTE."IdPaisNacimiento")
LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.Pais" PAIS$2 ON (PAIS$2."IdPais" = CLIENTE."IdPaisResidencia")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMISOR ON (EMISOR."IdEmisor" = CARTERA."IdEmisor") 
WHERE CARTERA."IdClasificacionTipoInstrumento" IN ( 'RF', 'IF') 
	  AND CARTERA."IdEstado" = 'VIG'
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1849.Origen" TO bcs_desarrollador WITH GRANT OPTION;
