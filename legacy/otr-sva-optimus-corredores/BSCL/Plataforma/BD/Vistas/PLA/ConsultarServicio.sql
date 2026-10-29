SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarServicio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarServicio"  
AS
SELECT 
	  *
From "BSCL.Plataforma.BD::PLA.Servicio" order by "IdServicio";
     

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarServicio" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
