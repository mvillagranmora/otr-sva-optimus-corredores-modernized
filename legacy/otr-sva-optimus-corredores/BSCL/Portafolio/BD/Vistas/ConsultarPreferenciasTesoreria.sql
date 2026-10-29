SET SCHEMA "BSCL";
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria"  AS

	SELECT  "IdPortafolio",
			"IdCliente",
			"IdTipoCliente",
		    MAX(CASE WHEN "Codigo" = 'TES_NIE' THEN "Valor" ELSE null END) AS "TES_NIE"
	  FROM (
			SELECT
				PREFTES."IdPortafolio",
				PREFTES."IdCliente",
				PREFTES."IdTipoCliente",
				PREFTES."NombreTipoIndicador" as "Nombre",
				PREFTES."CodigoTipoIndicador" as "Codigo",
				PREFTES."ValorIndicador" as "Valor",
				PREFTES."CodigoValorIndicador" as "CodigoValor",
				PREFTES."NombreValorIndicador" as "NombreValor"
			FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"  PREFTES
			WHERE "GrupoIndicador" = 'GRUPOTESORERIA'
	  	   )
	 GROUP BY "IdPortafolio","IdCliente","IdTipoCliente";
	

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
