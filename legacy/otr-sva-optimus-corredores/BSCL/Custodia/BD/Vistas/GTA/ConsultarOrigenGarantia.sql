SET SCHEMA "BSCL";
DROP VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarOrigenGarantia";

CREATE VIEW "BSCL.Custodia.BD.Vistas::GTA.ConsultarOrigenGarantia" AS 
SELECT  A."IdOrigenGarantia",
		A."Nombre" as "NombreOrigenGarantia"
  FROM  "BSCL.Custodia.BD::GTA.OrigenGarantia" AS A
WITH READ ONLY;	 	
 
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::GTA.ConsultarOrigenGarantia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
