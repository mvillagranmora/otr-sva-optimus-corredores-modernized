SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDiaFeriado";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDiaFeriado" AS
SELECT
	 TFE."IdTipoDiaFeriado"
	,TFE."Nombre"
	,TFE."IdPais"
	,TFE."IdEstado"
FROM "BSCL.Plataforma.BD::REG.TipoDiaFeriado" TFE;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoDiaFeriado" TO BCS_DESARROLLADOR WITH GRANT OPTION;
