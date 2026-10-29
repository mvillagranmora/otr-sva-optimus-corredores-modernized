SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluirLimite"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluirLimite" 
AS
	SELECT 
		ELIM."IdModulo",
		ELIM."IdTipoInstrumento",
		ELIM."IdEstado",
		ELIM."TipoExclusion",
		ELIM."IdEmpresa",
		MO."Codigo",
		MO."IdEstado" AS "IdEstadoModulo"
	FROM "BSCL.Plataforma.BD::LIM.ExcluirLimite" ELIM
	INNER JOIN "BSCL.Plataforma.BD::PLA.Modulo" AS MO ON ELIM."IdModulo" = MO."IdModulo"
	WHERE ELIM."IdEstado" = 'ACT'
	  AND MO."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarExcluirLimite" TO BCS_DESARROLLADOR WITH GRANT OPTION;