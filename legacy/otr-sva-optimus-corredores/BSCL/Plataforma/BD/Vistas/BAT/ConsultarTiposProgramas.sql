SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarTiposProgramas"; 
 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAT.ConsultarTiposProgramas"
 ( "IdTipoPrograma",
	 "Descripcion",
	 "MaximoIntentos",
	 "Modulo",
	 "NombreClasePrograma",
	 "TipoClasePrograma",
	 "EsPauta") AS SELECT
	 "IdTipoPrograma",
	 "Descripcion",
	 "MaximoIntentos",
	 "Modulo",
	 "NombreClasePrograma",
	 "TipoClasePrograma",
	 "EsPauta" 
FROM "BSCL.Plataforma.BD::BAT.TipoPrograma";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAT.ConsultarTiposProgramas" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
