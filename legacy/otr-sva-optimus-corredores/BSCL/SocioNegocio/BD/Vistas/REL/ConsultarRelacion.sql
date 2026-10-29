SET SCHEMA "BSCL";
--BSCL.SocioNegocio.BD::REL.Servicios.ConsultarRelacion
GO 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion";
GO
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" AS 
SELECT
	"DOC_OR"."DocumentoIdentidad",
	"REL"."IdRelacionPersona" ,
	"TREL"."IdTipoRelacion" AS "IdTipoRelacion" ,
	"TREL"."NombreTipoRelacion" AS "NombreTipoRelacion" ,
	"TREL"."EsIndefinida" AS "EsIndefinida" ,
	"TREL"."UsaPorcentajeParticipacion" as "UsaPorcentajeParticipacion",
	"REL"."PorcentajeParticipacion" as "PorcentajeParticipacion",
	"PER_OR"."IdPersona" AS "IdPersonaOrigen" ,
	"PER_OR"."RazonSocial" AS "RazonSocialOrigen" ,
	"TREL"."IdTipoEntidadOrigen" AS "EntidadOrigen",
	--	 "PER_DE"."IdPersona" AS "IdPersonaDestino" ,
	"REL"."IdPersonaDestino" AS "IdPersonaDestino" ,
	"PER_DE"."RazonSocial" AS "RazonSocialDestino" ,
	"TPER"."Descripcion" AS "TipoPersonaDestino",
	"DOC_DE"."IdTipoDocumentoIdentidad" AS "IdTipoDocumentoIdentidadDestino",
	"TDOC"."NombreTipoDocumentoIdentidad" AS "NombreTipoDocumentoIdentidadDestino",
	"DOC_DE"."DocumentoIdentidad" AS "DocumentoIdentidadDestino",
	--"REL"."IdEstado" ,
	(CASE  "TREL"."EsIndefinida" WHEN 'S' THEN "REL"."IdEstado" ELSE 
		(CASE  WHEN ("FechaFinRelacion" < "FechaInicioRelacion") THEN 'VCD' ELSE "REL"."IdEstado" END)	
	END ) AS "IdEstado",
	"EST"."Nombre" AS "NombreEstado",
	"REL"."FechaIngreso" ,
	"REL"."FechaInicioRelacion" ,
	"REL"."FechaFinRelacion",
	"PER_DE"."NombreCompleto" AS "NombreCompletoDestino",
	"REL"."RetiraDocumentos",
	case "REL"."RetiraDocumentos" when 'S' then 'SI' when 'N' then 'NO' else '' end as "TextoRetiraDocumentos",
	"REL"."FinalidadRelacion",
	"REL"."CondicionesRelacion",
	"REL"."IdGradoVinculacionParental",
	"GVP"."Nombre" as "NombreGradoVinculacionParental",
	"REL"."FechaModificacion",
	"REL"."Ciudad",
	"REL"."Calle",
	"REL"."Numero",
	"REL"."Email",
	"REL"."Telefono",
	"REL"."Complemento",
	CONCAT(CONCAT(CONCAT(CONCAT(COALESCE("REL"."Calle",''),' '),COALESCE(CAST("REL"."Numero" AS VARCHAR),'')),' - '),COALESCE("REL"."Ciudad",''))  AS "NombreLargo",
    "TREL"."UsaFirmaDocumento" as "UsaFirmaDocumento"
	FROM  "BSCL.SocioNegocio.BD::REL.RelacionPersona" AS "REL" 
				LEFT JOIN "BSCL.SocioNegocio.BD::REL.GradoVinculacionParental" AS "GVP" ON "GVP"."IdGradoVinculacionParental" = "REL"."IdGradoVinculacionParental"
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER_OR" ON  "PER_OR"."IdPersona" = "REL"."IdPersonaOrigen" 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS "PER_DE" ON  "PER_DE"."IdPersona" = "REL"."IdPersonaDestino" 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarTipoPersona" AS "TPER" ON  "TPER"."IdTipoPersona" = "PER_DE"."Tipo" 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "TREL" ON  "TREL"."IdTipoRelacion" = "REL"."IdTipoRelacion"  
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC_OR" ON  "DOC_OR"."IdDocumentoIdentidad" = "PER_OR"."IdDocumentoIdentidad" 
				LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS "DOC_DE" ON  "DOC_DE"."IdDocumentoIdentidad" = "PER_DE"."IdDocumentoIdentidad" 
				LEFT OUTER JOIN "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" AS "TDOC" ON  "TDOC"."IdTipoDocumentoIdentidad" = "DOC_DE"."IdTipoDocumentoIdentidad" 
				LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON (  
					(CASE "TREL"."EsIndefinida" WHEN 'S' THEN "REL"."IdEstado" ELSE 
						(CASE WHEN ("FechaFinRelacion" < "FechaInicioRelacion") THEN 'VCD' ELSE "REL"."IdEstado" END) 
					END ) = "EST"."IdEstado" ) WITH READ ONLY;
GO
GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
