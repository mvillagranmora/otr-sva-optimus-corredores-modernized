SET SCHEMA "BSCL";
--BSCL.SocioNegocio.BD::REL.Servicios.UIConsultarTipoRelacion

DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.UIConsultarTipoRelacion";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.UIConsultarTipoRelacion" AS 
	SELECT 
		"REL"."IdTipoRelacion",
		"REL"."IdEmpresa",
		"REL"."EsPersonaNatural",
		CASE "REL"."EsPersonaNatural" WHEN 'S' THEN 'SI' ELSE 'NO' END AS "EsPersonaNaturalGrilla",
		"REL"."IdTipoEntidadOrigen",
		"TSOC"."IdTipoSocioDeNegocio",
		"REL"."NombreTipoRelacion",
		"REL"."IdTipoRelacionDestino",
		"TREL"."NombreTipoRelacion" AS "NombreTipoRelacionDestino",
		"REL"."EsIndefinida",
		CASE "REL"."EsIndefinida" WHEN 'S' THEN 'SI' ELSE 'NO' END AS "EsIndefinidaGrilla",
		"REL"."IdEstado",
		"EST"."Nombre" AS "NombreEstado",
		"REL"."Administracion",
		"REL"."UsaPorcentajeParticipacion",
		CASE "REL"."UsaPorcentajeParticipacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaPorcentajeParticipacion",
		"REL"."RetiraDocumentos",
		CASE "REL"."RetiraDocumentos" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoRetiraDocumentos",
		"REL"."UsaFinalidadRelacion",
	  	CASE "REL"."UsaFinalidadRelacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaFinalidadRelacion",
	  	"REL"."UsaCondicionesRelacion",
	  	CASE "REL"."UsaCondicionesRelacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaCondicionesRelacion",
	  	"REL"."UsaGradoVinculacionParental",
	  	CASE "REL"."UsaGradoVinculacionParental" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaGradoVinculacionParental",
	  	"REL"."UsaFirmaDocumento",
  		CASE "REL"."UsaFirmaDocumento" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaFirmaDocumento",
		"REL"."AccesoOptimusCliente",
  		CASE "REL"."AccesoOptimusCliente" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoAccesoOptimusCliente"
	FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "REL"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" AS "TSOC" ON "REL"."IdTipoEntidadOrigen" = UPPER("TSOC"."NombreTipoSocioDeNegocio")
	LEFT JOIN "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "TREL" ON "REL"."IdTipoRelacionDestino" = "TREL"."IdTipoRelacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS "EST" ON "REL"."IdEstado" = "EST"."IdEstado";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.UIConsultarTipoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
