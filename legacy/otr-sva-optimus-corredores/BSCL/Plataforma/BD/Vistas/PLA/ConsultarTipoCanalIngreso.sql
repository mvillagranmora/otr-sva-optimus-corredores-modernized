SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCanalIngreso"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCanalIngreso"  
AS
SELECT "TipoCanalIngreso_$0"."IdTipoCanalIngreso" , "TipoCanalIngreso_$0"."Codigo" , "TipoCanalIngreso_$0"."Nombre" , "TipoCanalIngreso_$0"."IdEstado", "TipoCanalIngreso_$0"."PermiteEnrolamiento", "TipoCanalIngreso_$0"."PermiteFirmaDocumento" 
FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS "TipoCanalIngreso_$0";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCanalIngreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
