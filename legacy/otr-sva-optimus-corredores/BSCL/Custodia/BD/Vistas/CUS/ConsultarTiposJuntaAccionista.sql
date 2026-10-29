SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTiposJuntaAccionista";

CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.ConsultarTiposJuntaAccionista"
AS 
SELECT
	 "IdTipoJuntaAccionista",
	 "Codigo",
	 "Entidad",
	 "Descripcion"
FROM "BSCL.Custodia.BD::CUS.TipoJuntaAccionista";

GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.ConsultarTiposJuntaAccionista" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
