
SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarParametro";
          
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarParametro"
AS 
SELECT
	"IdEmpresa",
	"IdParametro",
	"NombreTipoParametro",
	"ValorParametro",
	"NombreParametro",
	"IdEstado",
	CASE WHEN "ValorParametro" = "NombreParametro"
	THEN "ValorParametro"
	ELSE
	 CONCAT(CONCAT("ValorParametro",' - '),"NombreParametro") 
	 END AS "ValorNombreParametro",
	-- MMACHMAR - Se usa alias 'NumeroDiaSemana' en combobox para que día semana comience desde 1 y asi usar opcion TODOS='S'
	CASE WHEN "NombreTipoParametro" = 'DIASEMANAESPANOL' THEN CAST("ValorParametro" AS INTEGER)+1 ELSE NULL END AS "NumeroDiaSemana"
FROM "BSCL.Mesa.BD::MES.Parametro";

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarParametro" TO BCS_DESARROLLADOR WITH GRANT OPTION;