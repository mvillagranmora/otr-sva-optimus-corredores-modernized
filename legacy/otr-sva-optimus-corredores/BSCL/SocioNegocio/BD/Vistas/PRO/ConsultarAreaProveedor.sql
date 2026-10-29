SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarAreaProveedor"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarAreaProveedor"  AS 
SELECT 
	"AREA"."IdAreaProveedor", 
	"AREA"."NombreArea", 
	"AREA"."IdEstado"  
FROM 
"BSCL.SocioNegocio.BD::PRO.AreaProveedor" AS "AREA"
WHERE "AREA"."Borrado"='N';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarAreaProveedor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
