SET SCHEMA "BSCL";

DROP VIEW "BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica"; 
 
CREATE VIEW "BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica"  AS 

SELECT 
	"PER"."IdPersona",
	"PER"."IdDocumentoIdentidad",
	"PER"."Tipo",
	"PER"."Alias",
	"PER"."Nombres",
	"PER"."ApellidoPaterno",
	"PER"."ApellidoMaterno",
	"PER"."RazonSocial",		  		
	"PER"."NombreCompleto",
	"PER"."IdGiroPrincipal",
	"PER"."IdPaisNacimiento",
	"PER"."IdPaisResidencia",
	"PER"."IdPaisResidenciaSecundaria",
	"PER"."IdPaisNacionalidadPrincipal",
	"PER"."IdPaisNacionalidadSecundaria",
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
	"PER"."FechaInicioActividad",
	"PER"."FechaIngreso",
	"PER"."FechaCreacion",
	"PER"."FechaModificacion",
	"PER"."EsEnrolado",
	"PER"."IdEstado",
	"PER"."IdCarga",
	"PER"."DocumentoIdentidad",
	"PER"."TipoDocumentoIdentidad",
	"PER"."DocumentoIdentidadAdicional",
	"PER"."TipoDocumentoIdentidadAdicional",	    
	"PER"."PersonaBusqueda",
	"PER"."IdOrigen",
	"PER"."IdPersonaConyuge",
	"PER"."IdPersonaRepresentante",
	"PER"."FechaInicioConyuge",
	"PER"."FechaInicioRepresentante", 
	"PER"."GlobalLegalEntity",
	"PER"."FechaBloqueo",
 	"PER"."ComentarioBloqueo",
 	"PER"."IdPersonaEmpleador",
 	"PER"."CodigoTipoCargoEmpleado",
 	"PER"."DetalleCargo",
 	"PER"."IdNombreFantasiaPrincipal",
 	"PER"."IdRangoPatrimonio",
 	"PER"."IdGradoInstruccion",
 	"PER"."AgnosServicio",
 	"PER"."IngresoPromedioMensual",
 	"PER"."IdGiroCIIU"
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER"
INNER JOIN (
	SELECT "IdPersona" FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona"
	WHERE "GrupoIndicador" = 'DATOS EMPLEADOR'
	AND "ValorIndicador" != ''
	GROUP BY "IdPersona"
) AS "IND" 
ON "IND"."IdPersona" = "PER"."IdPersona";
	   
GRANT SELECT ON "BSCL.APV.BD.Vistas::REC.ConsultarPersonaJuridica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 