SET SCHEMA "BSCL"; 
  
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI"; 
--	Solo para UI no usar en NEGOCIO,
--  Proposito: Informacion del empleado + persona, tambien trae indicadores de si es empleado o supervisor.
--  Autor: CHERNANDEZ
--  Fecha Creacion: 06/02/2017
--   
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI"  AS 
SELECT 
	--Datos Persona
	CE."IdDocumentoIdentidad",
	CE."Tipo",
	CE."NombreCompleto",
	CE."Nombres",
	CE."ApellidoPaterno",
	CE."ApellidoMaterno",
	CE."RazonSocial",
	CE."IdGiroPrincipal",
	CE."IdPaisNacimiento",
	CE."IdPaisResidencia",
	CE."FechaNacimiento",
	CE."FechaDefuncion",
	CE."Sexo",
	CE."EsExtranjero",
	CE."EstadoCivil",
	CE."RegimenConyugal",
	CE."Cargo",
	CE."Empleador",
	CE."IdDireccionFisicaPrincipal",
	CE."IdDireccionElectronicaPrincipal",
	CE."IdTelefonoPrincipal",
	CE."IdProfesion",
	CE."DocumentoIdentidad",
	CE."TipoDocumentoIdentidad",
	--Datos Empleado
	CE."IdEmpleado",
	CE."IdPersona",
	CE."CodigoEmpleado",
	CE."IdEmpresa",
	CE."CodigoTipoCargoEmpleado",
	CE."NombreTipoCargoEmpleado",
	CE."IdEmpleadoSupervisor",
	CE."CodigoEmpleado" as "CodigoEmpleadoSupervisor",
	CE."IdSucursal",
	CE."NombreSucursal",
	CE."UtilizaPlantillaEmpleado",
	CE."IdPlantillaEmpleado",
	CE."FechaInicioCargo",
	CE."FechaTerminoCargo",
	CE."FechaIngreso",
	CE."FechaCreacion",
	CE."FechaModificacion",
	CE."CodigoAgenteMigracion",
	CE."CodigoSupervisorAgenteMigracion",
	CE."CodigoYNombreEmpleado",
	CE."EsEnrolado",
	CE."IdEstado",
	CE."DetalleCargo",
	CE."IdDireccionElectronica",
	CE."CargoIndefinido",
	CE."RestringeCarteraCliente",
	CE."RestringeOrdenCliente",
	CASE WHEN COALESCE(E."IdEmpleadoEjecutivo",0)=0 THEN 'N' ELSE 'S' END AS "EsEjecutivo",
	CASE WHEN COALESCE(S."IdEmpleadoSupervisor",0)=0 THEN 'N' ELSE 'S' END AS "EsSupervisor"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" CE
LEFT JOIN (SELECT DISTINCT "IdEmpleadoEjecutivo" FROM "BSCL.SocioNegocio.BD::EJE.Ejecutivo") E ON E."IdEmpleadoEjecutivo" = CE."IdEmpleado"
LEFT JOIN (SELECT DISTINCT "IdEmpleadoSupervisor" FROM "BSCL.SocioNegocio.BD::EJE.Supervisor") S ON S."IdEmpleadoSupervisor" = CE."IdEmpleado";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoUI" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 