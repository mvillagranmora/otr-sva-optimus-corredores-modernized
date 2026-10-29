SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado"; 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado"  AS 
SELECT 
	"TCEMP"."IdTipoCargoEmpleado", 
	"TCEMP"."CodigoTipoCargoEmpleado", 
	"TCEMP"."NombreTipoCargoEmpleado",
	"TCEMP"."IdEmpresa",
	"TCEMP"."UtilizaDetalle"  
FROM 
"BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS "TCEMP"
WHERE "TCEMP"."Borrado"='N';
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 