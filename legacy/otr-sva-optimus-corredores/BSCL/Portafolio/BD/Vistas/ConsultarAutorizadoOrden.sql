SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden"; 
 
CREATE VIEW "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" AS 
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
		--Datos Autorizado
	 	"AUT"."IdAutorizado", 
	   	"AUT"."IdPortafolio",
	   	"AUT"."IdPersona",
	   	"AUT"."IdTipoAutorizado",
	   	"AUT"."FirmaOptimusCliente",
	   	"TPO"."NombreTipoAutorizado",
	   	"TPO"."IdTipoRelacion",
   		COALESCE("AUT"."IdEstado", '') AS "IdEstado",
		COALESCE("EST"."Nombre", '') AS "Estado",
	   	"AUT"."IdEstadoAutorizacion",
	   	"AUT"."FechaIngreso",
	   	"AUT"."FechaCreacion",
		"AUT"."FechaModificacion",
		"AUT"."IdUsuarioCreador",
		"AUT"."NumeroRegistroPublico",
		"AUT"."Asiento",
		"AUT"."FichaPartida",
		"AUT"."TipoFirma",
		"PORT"."IdCliente",
		"PORT"."CodigoPortafolio",
		"PORT"."DescripcionPortafolio",
		"PORT"."IdEmpleadoEjecutivo",
		CASE 
			WHEN "PORT"."IdPersona" =  "AUT"."IdPersona"
				THEN 'Clientes'				
				ELSE 'Relacionados'
		END AS "TipoAcceso",
		"CLI"."NombreCompleto" AS "AutorizadoPor",
		"CLI"."DocumentoIdentidad" AS "DocumentoIdentidadCLI",
		"PORT"."CodigoPropositoPortafolio"

FROM 
	"BSCL.Portafolio.BD::POR.Autorizado" AS "AUT"
--	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "PORT" ON "PORT"."IdPortafolio" = "AUT"."IdPortafolio"
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" AS "PORT" ON "PORT"."IdPortafolio" = "AUT"."IdPortafolio"
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CLI" ON ("CLI"."IdCliente" = "PORT"."IdCliente")	
	
	
	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarEstadoPortafolio" AS "ESTPORT" ON "AUT"."IdEstado" = "ESTPORT"."IdEstado" AND "ESTPORT"."NombreEntidad" = 'ORDENAUTORIZADO'
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "EST"."IdEstado" = "ESTPORT"."IdEstado"
	JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"  ON ( "PER"."IdPersona" = "AUT"."IdPersona" )
	LEFT JOIN "BSCL.Portafolio.BD::POR.TipoAutorizado" AS "TPO" ON "TPO"."IdTipoAutorizado" = "AUT"."IdTipoAutorizado"
	LEFT JOIN (
		SELECT MAX("IdEstado") AS "IdEstado", MAX("NombreEstado") AS "NombreEstado", "IdPersonaDestino", "IdTipoRelacion" 
		FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion"  
		WHERE "IdEstado" != 'VCD' GROUP BY "IdPersonaDestino", "IdTipoRelacion"
	) AS CR ON "AUT"."IdPersona" = CR."IdPersonaDestino" AND "TPO"."IdTipoRelacion" = CR."IdTipoRelacion"; 	  

GRANT SELECT ON "BSCL.Portafolio.BD.Vistas::ConsultarAutorizadoOrden" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
