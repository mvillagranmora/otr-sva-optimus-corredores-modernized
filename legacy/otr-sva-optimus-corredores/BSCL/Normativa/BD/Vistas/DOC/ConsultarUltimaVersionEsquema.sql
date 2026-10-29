SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimaVersionEsquema";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimaVersionEsquema"
AS 
SELECT
ESQUEMA."Esquema"
, ESQUEMA."Modulo"
, ESQUEMA."Periodicidad"
, ESQUEMA."Objetivo" 
, ESQUEMA."PeriodoInicio" 
, ESQUEMA."IdEntidadReguladora" 
, ESQUEMA."FechaPresentacionLimite"
, ESQUEMA."FechaDatosDesde"
, ESQUEMA."FechaDatosHasta"
, ESQUEMA."CodigoVersionSII"
, ESQUEMA."HabilitadoMantenedor"
, ESQUEMA."Version"
, COALESCE(DOCUMENTO."IdEstadoDocumento",'') AS "IdEstadoDocumento"
, COALESCE(ESTADO."Nombre",'') AS "NombreEstado"
, '' AS "FlujoProceso"
FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
INNER JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONFIGURACION ON (CONFIGURACION."IdNormativa" = ESQUEMA."Esquema" AND CONFIGURACION."CodigoParametro" = 'PERIODO_TRIBUTARIO_' || ESQUEMA."Esquema" AND ESQUEMA."Version" = CONFIGURACION."Valor")
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Documento" DOCUMENTO ON (ESQUEMA."Esquema" = DOCUMENTO."Esquema" AND ESQUEMA."Version" = DOCUMENTO."Version")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" ESTADO ON (ESTADO."IdEstado" = DOCUMENTO."IdEstadoDocumento")
UNION ALL
SELECT
ESQUEMA."Esquema"
, ESQUEMA."Modulo"
, ESQUEMA."Periodicidad"
, ESQUEMA."Objetivo" 
, ESQUEMA."PeriodoInicio" 
, ESQUEMA."IdEntidadReguladora" 
, ESQUEMA."FechaPresentacionLimite"
, ESQUEMA."FechaDatosDesde"
, ESQUEMA."FechaDatosHasta"
, ESQUEMA."CodigoVersionSII"
, ESQUEMA."HabilitadoMantenedor"
, ESQUEMA."Version"
, COALESCE(DOCUMENTO."IdEstadoDocumento",'') AS "IdEstadoDocumento"
, COALESCE(ESTADO."Nombre",'') AS "NombreEstado"
, '' AS "FlujoProceso"
FROM (
	SELECT ESQUEMA."Esquema"
			, ESQUEMA."Modulo"
			, ESQUEMA."Periodicidad"
			, ESQUEMA."Objetivo" 
			, ESQUEMA."PeriodoInicio" 
			, ESQUEMA."IdEntidadReguladora" 
			, ESQUEMA."FechaPresentacionLimite"
			, ESQUEMA."FechaDatosDesde"
			, ESQUEMA."FechaDatosHasta"
			, ESQUEMA."CodigoVersionSII"
			, ESQUEMA."HabilitadoMantenedor"
			, CONFIGURACION$2."Valor" AS "Version"
	FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
	INNER JOIN "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION$2 ON (CONFIGURACION$2."CodigoParametro" = 'PERIODO_TRIBUTARIO' AND ESQUEMA."Version" = CONFIGURACION$2."Valor" AND ESQUEMA."Periodicidad" = 'A')
	LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONFIGURACION ON (CONFIGURACION."IdNormativa" = ESQUEMA."Esquema" AND CONFIGURACION."CodigoParametro" = 'PERIODO_TRIBUTARIO_' || ESQUEMA."Esquema")
	WHERE COALESCE(CONFIGURACION."CodigoParametro",'') = '' 
) AS ESQUEMA
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Documento" DOCUMENTO ON (ESQUEMA."Esquema" = DOCUMENTO."Esquema" AND ESQUEMA."Version" = DOCUMENTO."Version")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" ESTADO ON (ESTADO."IdEstado" = DOCUMENTO."IdEstadoDocumento")
UNION ALL
SELECT
ESQUEMA."Esquema"
, ESQUEMA."Modulo"
, ESQUEMA."Periodicidad"
, ESQUEMA."Objetivo" 
, ESQUEMA."PeriodoInicio" 
, ESQUEMA."IdEntidadReguladora" 
, ESQUEMA."FechaPresentacionLimite"
, ESQUEMA."FechaDatosDesde"
, ESQUEMA."FechaDatosHasta"
, ESQUEMA."CodigoVersionSII"
, ESQUEMA."HabilitadoMantenedor"
, ESQUEMA."Version"
, COALESCE(DOCUMENTO."IdEstadoDocumento",'') AS "IdEstadoDocumento"
, COALESCE(ESTADO."Nombre",'') AS "NombreEstado"
, '' AS "FlujoProceso"
FROM (
	SELECT ESQUEMA."Esquema"
			, ESQUEMA."Modulo"
			, ESQUEMA."Periodicidad"
			, ESQUEMA."Objetivo" 
			, ESQUEMA."PeriodoInicio" 
			, ESQUEMA."IdEntidadReguladora" 
			, ESQUEMA."FechaPresentacionLimite"
			, ESQUEMA."FechaDatosDesde"
			, ESQUEMA."FechaDatosHasta"
			, ESQUEMA."CodigoVersionSII"
			, ESQUEMA."HabilitadoMantenedor"
			, CONFIGURACION$2."Valor" AS "Version"
	FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
	INNER JOIN "BSCL.Normativa.BD::NOR.Configuracion" CONFIGURACION$2 ON (CONFIGURACION$2."CodigoParametro" = 'PERIODO_TRIBUTARIO_MENSUAL' AND ESQUEMA."Version" = CONFIGURACION$2."Valor" AND ESQUEMA."Periodicidad" = 'M')
	LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" AS CONFIGURACION ON (CONFIGURACION."IdNormativa" = ESQUEMA."Esquema" AND CONFIGURACION."CodigoParametro" = 'PERIODO_TRIBUTARIO_' || ESQUEMA."Esquema")
	WHERE COALESCE(CONFIGURACION."CodigoParametro",'') = '' 
) AS ESQUEMA
LEFT OUTER JOIN "BSCL.Normativa.BD::DOC.Documento" DOCUMENTO ON (ESQUEMA."Esquema" = DOCUMENTO."Esquema" AND ESQUEMA."Version" = DOCUMENTO."Version")
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstado" ESTADO ON (ESTADO."IdEstado" = DOCUMENTO."IdEstadoDocumento");

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarUltimaVersionEsquema" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
