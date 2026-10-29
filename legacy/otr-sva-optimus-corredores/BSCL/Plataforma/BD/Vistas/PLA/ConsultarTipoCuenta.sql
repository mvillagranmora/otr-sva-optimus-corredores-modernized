SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta"  
AS
SELECT 	 "TC"."IdModulo",
		 "TC"."IdTipoCuenta",
		 "TC"."Nombre",
		 "TC"."Uso",
		 "TC"."IdEstado"		
FROM "BSCL.Plataforma.BD::PLA.TipoCuenta" AS "TC";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoCuenta" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

