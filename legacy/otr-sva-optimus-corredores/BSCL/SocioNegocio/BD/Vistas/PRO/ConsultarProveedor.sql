SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor"  AS 

SELECT 
	PROVEEDOR.*,
	ESTPROVEEDOR."Nombre" as "NombreEstado"
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
			--Datos Proveedor
			"PRO"."IdProveedor",
			"PRO"."IdPersona",
			--"PRO"."IdEstado",
			"PRO"."EsEnrolado",
			-- Estado Operacional
			CASE "PRO"."IdEstado" WHEN 'ACT' THEN CASE "PRO"."EsEnrolado" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
			"PRO"."IdEmpresa",
			"PRO"."IdAreaProveedor",	
			"PAIS"."NombrePais" as "NombreAreaProveedor", -- actualmente se usa el pais.
			"PRO"."FechaIngreso",
			"PRO"."FechaCreacion",
			"PRO"."FechaModificacion"
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
	JOIN "BSCL.SocioNegocio.BD::PRO.Proveedor" AS "PRO"	ON ( "PER"."IdPersona" = "PRO"."IdPersona" ) 
	LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" AS "PAIS" ON ("PAIS"."IdPais" = "PRO"."IdAreaProveedor")
	WHERE "PRO"."Borrado"='N') PROVEEDOR
LEFT JOIN 
	"BSCL.Plataforma.BD::PLA.Estado" ESTPROVEEDOR 
		ON (ESTPROVEEDOR."IdEstado" = PROVEEDOR."IdEstado");
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PRO.ConsultarProveedor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 