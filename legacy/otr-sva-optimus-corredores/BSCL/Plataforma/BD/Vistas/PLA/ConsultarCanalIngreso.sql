SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCanalIngreso"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCanalIngreso"  
AS

SELECT 
"IdTipoCanalIngreso",
"Nombre" ,
"IdEstado" 
From "BSCL.Plataforma.BD::PLA.TipoCanalIngreso";
     
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarCanalIngreso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

