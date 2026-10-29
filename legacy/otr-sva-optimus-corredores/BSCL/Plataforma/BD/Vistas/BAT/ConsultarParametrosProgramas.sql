SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas"  
AS
SELECT
		"IdSecuencia",
		pp."IdGrupoParametro",
		p."IdPrograma",
		"Orden",
		"NombreParametro",
		"Valor",
		"ValorBlob",
		"idEstado",
		"IdTipoPrograma",
		"fechaEjecucion"
FROM "BSCL.Plataforma.BD::BAT.ParametrosPrograma" pp
LEFT JOIN  "BSCL.Plataforma.BD::BAT.Programa" p
ON p."IdPrograma" = pp."IdPrograma" 
WHERE pp."IdPrograma" = p."IdPrograma";

 
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 


