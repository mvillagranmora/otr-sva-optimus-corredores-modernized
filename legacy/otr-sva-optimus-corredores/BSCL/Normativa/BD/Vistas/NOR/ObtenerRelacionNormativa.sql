SET SCHEMA "BSCL";
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa"
AS
--MetodoRelacionado:Clasificacion
SELECT TIP."IdNormativa"
, TIP."CodigoRelacionNorma"
, TIP."DescripcionRelacionNorma"
, TIP."IdEstado"
,REL."IdTipoRelacion"
FROM "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion" TIP
INNER JOIN "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion" REL
ON (TIP."IdClasificacionRelacion"=REL."IdClasificacionRelacion")
--WHERE REL."IdTipoRelacion" <> 'OTRARE'

UNION ALL
--MetodoRelacionado:Directa
SELECT REL."IdNormativa"
, REL."CodigoRelacionNorma"
, REL."DescripcionRelacionNorma"
, REL."IdEstado"
,REL."IdTipoRelacion"
FROM "BSCL.Normativa.BD::NOR.NormativaRelacion" REL;
--WHERE REL."IdTipoRelacion" <> 'OTRARE';
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ObtenerRelacionNormativa" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
