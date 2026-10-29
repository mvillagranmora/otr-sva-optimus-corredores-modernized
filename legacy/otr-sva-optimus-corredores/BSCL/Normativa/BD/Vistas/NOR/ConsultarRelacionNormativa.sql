SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa"
AS

SELECT REL."IdNormativa"
, REL."CodigoRelacionNorma"
, REL."DescripcionRelacionNorma"
, REL."IdEstado"
, REL."IdTipoRelacion"
, TREL."NombreTipoRelacion"
FROM "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa" REL
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TREL
ON (TREL."IdTipoRelacion"=REL."IdTipoRelacion")

UNION ALL
--OTRA RELACION (Distinta a la señalada en la Norma)

SELECT 
 		  OTR."IdNormativa"
		, OTR."CodigoRelacionNorma"
		, OTR."DescripcionRelacionNorma"
		, OTR."IdEstado"
		, OTR."IdTipoRelacion"
		, OTR."NombreTipoRelacion"
		
FROM 
		(
		SELECT NRE."IdNormativa"
		, NRE."CodigoRelacionNorma"
		, NRE."DescripcionRelacionNorma"
		, NRE."IdEstado"
		,TREL."IdTipoRelacion"
		,TREL."NombreTipoRelacion"
		 FROM 
		"BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TREL
		, "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa" NRE
		WHERE  NRE."IdTipoRelacion"='OTRARE'
		) OTR
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa" REL
ON ( OTR."IdNormativa" =REL."IdNormativa" AND OTR."IdTipoRelacion"=REL."IdTipoRelacion")		
WHERE REL."IdTipoRelacion" IS NULL;--Que no existan homologados en la Norma


GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarRelacionNormativa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
