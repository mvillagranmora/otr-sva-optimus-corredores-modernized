SET SCHEMA "BSCL"; 
   
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado"  AS 

SELECT 
	EMPLEADO.*,
	ESTEMPLEADO."Nombre" as "NombreEstado"
FROM (
	SELECT 
			-- Datos de Personas
			"PER"."IdDocumentoIdentidad",
			"PER"."Tipo",
			"PER"."NombreCompleto",
			"PER"."Nombres",
			"PER"."ApellidoPaterno",
			"PER"."ApellidoMaterno",
			"PER"."RazonSocial",
			"PER"."IdGiroPrincipal",
			"PER"."IdPaisNacimiento",
			"PER"."IdPaisResidencia",
			"PER"."FechaNacimiento",
			"PER"."FechaDefuncion",
			"PER"."Sexo",
			"PER"."EsExtranjero",
			"PER"."EstadoCivil",
			"PER"."RegimenConyugal",
			"PER"."Cargo",
			"PER"."Empleador",
			"PER"."IdDireccionFisicaPrincipal",
			"PER"."IdDireccionElectronicaPrincipal",
			"PER"."IdTelefonoPrincipal",
			"PER"."IdProfesion",
			"PER"."DocumentoIdentidad",
			"PER"."TipoDocumentoIdentidad",
			--Datos Empleado
			"EMP"."IdEmpleado",
			"EMP"."IdPersona",
			"EMP"."CodigoEmpleado",
			"EMP"."IdEmpresa",
			"EMP"."CodigoTipoCargoEmpleado",
			"TCEMP"."NombreTipoCargoEmpleado",
			"EMP"."IdEmpleadoSupervisor",
			"SUP"."CodigoEmpleado" as "CodigoEmpleadoSupervisor",
			"EMP"."IdSucursal",
			"SUC"."NombreSucursal",
			"EMP"."UtilizaPlantillaEmpleado",
			"EMP"."IdPlantillaEmpleado",
			"EMP"."FechaInicioCargo",
			"EMP"."FechaTerminoCargo",
			"EMP"."FechaIngreso",
			"EMP"."FechaCreacion",
			"EMP"."FechaModificacion",
			"EMP"."CodigoAgenteMigracion",
			"EMP"."CodigoSupervisorAgenteMigracion",
			CONCAT(CONCAT("EMP"."CodigoEmpleado",' - '),"PER"."NombreCompleto") as "CodigoYNombreEmpleado",
			--"EMP"."IdEstado",
			"EMP"."EsEnrolado",
			-- Estado Operacional
			CASE "EMP"."IdEstado" WHEN 'ACT' THEN CASE "EMP"."EsEnrolado" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
			"EMP"."DetalleCargo",
			"EMP"."IdDireccionElectronica",
			"EMP"."CargoIndefinido",
			"EMP"."RestringeCarteraCliente",
			"EMP"."RestringeOrdenCliente"
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
	JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "EMP"	ON ( "PER"."IdPersona" = "EMP"."IdPersona" ) 
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarSucursal" "SUC" ON "EMP"."IdSucursal" = "SUC"."IdSucursal"
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.TipoCargoEmpleado" AS "TCEMP" ON ("TCEMP"."CodigoTipoCargoEmpleado" = "EMP"."CodigoTipoCargoEmpleado")
	LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS "SUP"	ON ( "SUP"."IdEmpleado" = "EMP"."IdEmpleadoSupervisor" AND "SUP"."IdEmpresa" = "EMP"."IdEmpresa" ) 		
	WHERE "EMP"."Borrado"='N') EMPLEADO
LEFT JOIN 
	"BSCL.Plataforma.BD::PLA.Estado" ESTEMPLEADO 
		ON (ESTEMPLEADO."IdEstado" = EMPLEADO."IdEstado");

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
