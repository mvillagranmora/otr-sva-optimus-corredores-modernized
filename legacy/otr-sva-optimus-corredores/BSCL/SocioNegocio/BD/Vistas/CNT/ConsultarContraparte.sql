SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"; 
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte"  AS 
SELECT 
	CONTRAPARTE.*,
	ESTCONTRAPARTE."Nombre" as "NombreEstado"
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
			"CNT"."IdContraparte",
			"CNT"."IdPersona",
			"CNT"."IdEmpresa",
			"CNT"."CodigoInscripcion", -- Para CL corresponde a Codigo definido por SVS.
			"CNT"."Emisor",
			"CNT"."Depositos",
			"CNT"."Bolsa",
			"CNT"."Corredora",
			"CNT"."CamaraDeCompensacion",
			"CNT"."Banco", 
			"CNT"."Boveda",
			"CNT"."Otros", 
			--"CNT"."IdEstado", 
			"CNT"."EsEnrolado",
			-- Estado Operacional
			CASE "CNT"."IdEstado" WHEN 'ACT' THEN CASE "CNT"."EsEnrolado" WHEN 'N' THEN 'PEN' WHEN 'N' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
			"CNT"."Borrado",
			"CNT"."FechaIngreso",
			"CNT"."FechaCreacion",
			"CNT"."FechaModificacion",
			"SOC"."IdSocioDeNegocio" 	
	FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"    
	JOIN "BSCL.SocioNegocio.BD::CNT.Contraparte" AS "CNT"	ON ( "PER"."IdPersona" = "CNT"."IdPersona" ) 	
	JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS "SOC"  ON ("SOC"."IdContraparte" = "CNT"."IdContraparte")
	WHERE "CNT"."Borrado"='N' ) CONTRAPARTE
LEFT JOIN 
	"BSCL.Plataforma.BD::PLA.Estado" ESTCONTRAPARTE 
		ON (ESTCONTRAPARTE."IdEstado" = CONTRAPARTE."IdEstado");
GO                      
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
