SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::APV.ConsultarEstado"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::APV.ConsultarEstado"  AS 

SELECT 	"Codigo"
	 	,"Nombre"
	 	,"NombreEntidad"
FROM "BSCL.APV.BD::APV.Estado";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::APV.ConsultarEstado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 