SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoBanco"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoBanco" AS

	SELECT 
	    TB."IdTipoBanco"
	   ,TB."Nombre" AS "NombreTipoBanco"
	   ,TB."IdEstado" AS "IdEstadoTipoBanco"
	FROM  "BSCL.Plataforma.BD::BAN.TipoBanco" AS TB;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::BAN.ConsultarTipoBanco" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
