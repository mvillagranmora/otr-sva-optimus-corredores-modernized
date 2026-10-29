SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF"
AS

	SELECT	Vista_Org.*,
			COALESCE(PP."IdBoveda", '') AS "IdBoveda",
			COALESCE(PP."NombreBoveda", '')  AS "NombreBoveda"
	FROM	(
		SELECT "IdPortafolio"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'MES_CUSTRF' THEN "ValorIndicador" END),'N') AS "OperaCustodia"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'MES_SECECO' THEN "ValorIndicador" END),'') AS "SectorEconomico"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'MES_CODOTC' THEN "ValorIndicador" END),'') AS "CodigoOTC"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'MES_DATTEC' THEN "ValorIndicador" END),'') AS "DataTec"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_OCCINV' THEN "ValorIndicador" END),'N') AS "OperaCuentaInversion"
			,COALESCE(MAX(CASE WHEN "CodigoTipoIndicador" = 'GEN_SOBCTA' THEN "ValorIndicador" END),'N') AS "SobregiroCuentaInversion"		
			
		FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
		WHERE "GrupoIndicador" in ('GRUPOMESA','GRUPOGENERAL')  AND "IdUso"='PRE'
		GROUP BY "IdPortafolio"	) Vista_Org
	--* ----------------------------------------------------------------------------------------------------
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" PP
	ON PP."IdPortafolio" = Vista_Org."IdPortafolio" AND PP."IdEstado" = 'ACT' AND PP."IdEstadoAutorizacion" IN ('AUT','ACT')
	--* ----------------------------------------------------------------------------------------------------	
	;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarPreferenciaPortafolioMesaRF" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
