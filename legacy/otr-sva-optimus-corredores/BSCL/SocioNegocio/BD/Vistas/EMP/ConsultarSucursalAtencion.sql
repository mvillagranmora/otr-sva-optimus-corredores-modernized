SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarSucursalAtencion"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarSucursalAtencion"  AS 

SELECT
	SA."IdSucursalAtencion",
	SA."IdEmpresa",
	SA."IdEmpleado",
	SA."IdSucursal",
	S."NombreSucursal",
	S."EstadoSucursal",
	SA."Borrado"        
FROM "BSCL.Plataforma.BD::Empleado.SucursalAtencion" AS SA
INNER JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS S ON SA."IdSucursal" = S."IdSucursal" AND SA."IdEmpresa" = S."IdEmpresa";


GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarSucursalAtencion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
