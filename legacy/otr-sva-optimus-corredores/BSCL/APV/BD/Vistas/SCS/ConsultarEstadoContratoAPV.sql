SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarEstadoContratoAPV"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::SCS.ConsultarEstadoContratoAPV"  AS 

SELECT 	"Codigo"
	 	,"Nombre"
FROM "BSCL.APV.BD::APV.Estado"
WHERE "NombreEntidad" = 'CONTRATO';
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::SCS.ConsultarEstadoContratoAPV" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 