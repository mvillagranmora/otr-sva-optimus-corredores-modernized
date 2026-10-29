WITH validarContenido AS (
SELECT
	'IdRangoPatrimonio' AS "Campo",
	STRING_AGG("IdRangoPatrimonio", ',') AS "ValoresPosibles",
	'0' AS "ValorDefecto",
	'' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.RangoPatrimonio"
UNION ALL
SELECT
	'IdTipoGenero' AS "Campo",
	STRING_AGG("IdTipoGenero", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.TipoGenero"
UNION ALL
SELECT
	'IdPais' AS "Campo",
	STRING_AGG("IdPais", ',') AS "ValoresPosibles",
	'0' AS "ValorDefecto",
	'' AS "Prefijo"
FROM "BSCL.Plataforma.BD::REG.Pais"
UNION ALL 
SELECT
	'IdTipoEstadoCivil' AS "Campo",
	STRING_AGG("IdTipoEstadoCivil", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'' AS "Prefijo"
FROM "BSCL.Plataforma.BD::REG.TipoEstadoCivil"
UNION ALL 
SELECT
	'IdTipoRegimenConyugal' AS "Campo",
	STRING_AGG("IdTipoRegimenConyugal", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'' AS "Prefijo"
FROM "BSCL.Plataforma.BD::REG.TipoRegimenConyugal"
UNION ALL
SELECT 
	'IdGiro' AS "Campo",
	STRING_AGG("IdGiro", ',') AS "ValoresPosibles",
	'0' AS "ValorDefecto",
	'' AS "Prefijo" 
FROM "BSCL.Plataforma.BD::REG.Giro"
UNION ALL
SELECT 
	'IdProfesion' AS "Campo",
	STRING_AGG("IdProfesion", ',') AS "ValoresPosibles",
	'0' AS "ValorDefecto",
	'' AS "Prefijo"
FROM "BSCL.Plataforma.BD::REG.Profesion"
UNION ALL 
SELECT
	'CodigoSituacionLaboral' AS "Campo",
	STRING_AGG("CodigoSituacionLaboral", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.SituacionLaboral"
UNION ALL 
SELECT
	'IdTipoCanalIngreso' AS "Campo",
	STRING_AGG("IdTipoCanalIngreso", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'' AS "Prefijo"
FROM "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"
UNION ALL
SELECT
	'CodigoRangoSeleccion' AS "Campo",
	STRING_AGG("CodigoRangoSeleccion", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'RANGO_RENTA' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.RangoSeleccion"
WHERE "CodigoRangoSeleccion" LIKE 'RANGO_RENTA%'
UNION ALL 
SELECT
	'CodigoRangoSeleccion' AS "Campo",
	STRING_AGG("CodigoRangoSeleccion", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'RANGO_VENTA' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.RangoSeleccion"
WHERE "CodigoRangoSeleccion" LIKE 'RANGO_VENTA%'
UNION ALL 
SELECT
	'CodigoRangoSeleccion' AS "Campo",
	STRING_AGG("CodigoRangoSeleccion", ',') AS "ValoresPosibles",
	'' AS "ValorDefecto",
	'PROPOSITO' AS "Prefijo"
FROM
	"BSCL.SocioNegocio.BD::PER.RangoSeleccion"
WHERE "CodigoRangoSeleccion" LIKE 'PROPOSITO%'
)SELECT "Campo", "ValoresPosibles", "ValorDefecto", "Prefijo" FROM validarContenido
WHERE "Campo" IN (:FiltroValidacion);