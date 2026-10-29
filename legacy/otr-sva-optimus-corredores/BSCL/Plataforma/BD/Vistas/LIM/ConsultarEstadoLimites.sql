SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEstadoLimites"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEstadoLimites"
AS
SELECT "el"."IdEstadoLim"
	  ,"el"."IdEstado"
	  ,"el"."NombreEntidad"
	  ,"el"."Descripcion"
FROM "BSCL.Plataforma.BD::LIM.EstadoLim" AS "el";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarEstadoLimites" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
