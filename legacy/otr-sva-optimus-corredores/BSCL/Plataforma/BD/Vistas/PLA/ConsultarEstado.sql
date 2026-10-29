SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado"  
AS
SELECT 
	  "IdEstado",
	  "Nombre"
From "BSCL.Plataforma.BD::PLA.Estado";
     

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
