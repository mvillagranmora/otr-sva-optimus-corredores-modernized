SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarRazonRetiro";
 
CREATE VIEW "BSCL.APV.BD.Vistas::TRA.ConsultarRazonRetiro"  AS 

SELECT
	 "Codigo"
	,"Nombre"
FROM "BSCL.APV.BD::TRA.RazonRetiro";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::TRA.ConsultarRazonRetiro" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 