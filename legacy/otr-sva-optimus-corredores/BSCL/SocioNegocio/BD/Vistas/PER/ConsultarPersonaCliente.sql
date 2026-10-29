SET SCHEMA "BSCL";

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente";

CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente" AS
SELECT "PERSONA".*,
	"ESTADO"."Nombre" as "NombreEstado"
FROM
(SELECT -- Todos los datos de Persona
		"PER"."IdPersona",
		"PER"."IdDocumentoIdentidad",
		"PER"."Tipo",
		"PER"."Alias",
		"PER"."Nombres",
		"PER"."ApellidoPaterno",
		"PER"."ApellidoMaterno",
		"PER"."RazonSocial",		  		
		CASE "PER"."Tipo" 
			WHEN 'N' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
			--WHEN 'M' THEN "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno"  --http://jira.bcs.cl/browse/OPTIMUSPE-202
			ELSE "PER"."RazonSocial"
		END  as "NombreCompleto",
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
		--"PER"."FotoPersona",
		--"PER"."FotoFirmaPersona",
		"PER"."FechaInicioActividad",
		"PER"."FechaIngreso",
		"PER"."FechaCreacion",
		"PER"."FechaModificacion",
		--"PER"."IdEstado",		 
		"PER"."EsEnrolado",
		TO_NVARCHAR("PER"."FechaNacimiento") AS "FechaNacimientoString",
		-- Estado Operacional
		CASE WHEN "PER"."IdEstado" = 'ACT' THEN CASE WHEN "PER"."EsEnrolado"='P' THEN 'PEN' ELSE 'ACT' END ELSE 'BLO' END AS "IdEstado",
		"PER"."IdCarga",
  		  --Datos Necesarios de las FK
	    "IDENT"."DocumentoIdentidad"        AS "DocumentoIdentidad",
	    "IDENT"."IdTipoDocumentoIdentidad"  AS "TipoDocumentoIdentidad",
	    "IDENTADICIONAL"."DocumentoIdentidad"        AS "DocumentoIdentidadAdicional",
	    "IDENTADICIONAL"."IdTipoDocumentoIdentidad"  AS "TipoDocumentoIdentidadAdicional",	    
		-- Para búsqueda en combo-persona
		("IDENT"."DocumentoIdentidad" ||' '|| (CASE 
			WHEN "PER"."Tipo" = 'J' THEN "PER"."RazonSocial" 
			WHEN "PER"."Tipo" = 'M' THEN "PER"."RazonSocial" -- Proyecto Peru
			ELSE "PER"."Nombres" ||' '|| "PER"."ApellidoPaterno" || ' ' ||"PER"."ApellidoMaterno" 
			END)) AS "PersonaBusqueda",
	    --"SOC"."IdSocioDeNegocio" AS "IdSocioDeNegocioRelacionado",
		"PER"."IdOrigen",
	    "PER"."IdPersonaConyuge",
	    "PER"."IdPersonaRepresentante",
	    "PER"."FechaInicioConyuge",
		"PER"."FechaInicioRepresentante", 
	    "PER"."GlobalLegalEntity",
	    "PER"."FechaBloqueo",
	    "PER"."IdTipoBloqueo",
	    "BLO"."NombreTipoBloqueo",
 		"PER"."ComentarioBloqueo",
 		"PER"."IdPersonaEmpleador",
 		"PER"."CodigoTipoCargoEmpleado",
 		"PER"."DetalleCargo",
 		"PER"."IdNombreFantasiaPrincipal",
 		"PER"."IdRangoPatrimonio",
 		"PER"."IdGradoInstruccion",
 		"PER"."AgnosServicio",
 		"PER"."IngresoPromedioMensual",
 		"PER"."IdGiroCIIU",
 		"PAI"."EsOcde",
 		"PAI"."EsPaisUs",
 		"PER"."ResidenciaTributariaExtranjera",
 		"PER"."IdTransparenciaFiscal",
 		"CLI"."TipoEnrolamiento",
		"PER"."IdSituacionLaboral",
		"PER"."CodigoRangoRenta",
		"PER"."CodigoRangoNivelVenta",
		"PER"."CodigoPropositoTransaccion"
	FROM   "BSCL.SocioNegocio.BD::PER.Persona" AS "PER"
	INNER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENT" ON "PER"."IdDocumentoIdentidad" = "IDENT"."IdDocumentoIdentidad"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "IDENTADICIONAL" ON "PER"."IdDocumentoIdentidadAdicional" = "IDENTADICIONAL"."IdDocumentoIdentidad"
  --LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" as "SOC" ON (("PER"."IdPersonaConyuge" = "SOC"."IdPersona") OR ("PER"."IdPersonaRepresentante" = "SOC"."IdPersona"))
    LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoBloqueoSocioDeNegocio" AS "BLO" ON "PER"."IdTipoBloqueo" = "BLO"."IdTipoBloqueo" AND "BLO"."IdTipoEntidad" = 'PERSONA'
  	LEFT JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" AS "PAI" ON "PER"."IdPaisResidencia" = "PAI"."IdPais"
  	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS "CLI" ON "PER"."IdPersona" = "CLI"."IdPersona"
WHERE "PER"."Borrado"='N' AND "IDENT"."Borrado"='N'
--AND "PER"."IdCarga" = 0
) as "PERSONA"
JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "ESTADO" ON "PERSONA"."IdEstado" = "ESTADO"."IdEstado";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaCliente" TO BCS_DESARROLLADOR WITH GRANT OPTION;