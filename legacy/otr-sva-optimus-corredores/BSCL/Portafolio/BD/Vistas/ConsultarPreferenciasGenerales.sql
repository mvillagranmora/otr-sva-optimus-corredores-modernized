SET SCHEMA "BSCL";
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasGenerales"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasGenerales"  AS

	SELECT   "IdPortafolio"
			,"IdCliente"
			,"IdTipoCliente"
		    ,MAX(CASE WHEN "Codigo" = 'GEN_SOBCTA' THEN "Valor" ELSE null END) AS "GEN_SOBCTA"
		    ,MAX(CASE WHEN "Codigo" = 'GEN_OCCINV' THEN "Valor" ELSE null END) AS "GEN_OCCINV"		    
	  FROM (
			SELECT
				PREFGEN."IdPortafolio",
				PREFGEN."IdCliente",
				PREFGEN."IdTipoCliente",
				PREFGEN."NombreTipoIndicador" as "Nombre",
				PREFGEN."CodigoTipoIndicador" as "Codigo",
				PREFGEN."ValorIndicador" as "Valor",
				PREFGEN."CodigoValorIndicador" as "CodigoValor",
				PREFGEN."NombreValorIndicador" as "NombreValor"
			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  PREFGEN
			WHERE "GrupoIndicador" = 'GRUPOGENERAL'
	  	   )
	 GROUP BY "IdPortafolio","IdCliente","IdTipoCliente";
	

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasGenerales" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
