SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquema";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquema"
AS 

SELECT
	ESQ."Esquema",
	ESQ."Version",
	ESQ."Modulo",
	ESQ."Periodicidad",
	ESQ."Objetivo", 
	ESQ."PeriodoInicio", 
	ESQ."IdEntidadReguladora", 
	ESQ."FechaPresentacionLimite", 
	ESQ."FechaDatosDesde",
    ESQ."FechaDatosHasta",
    ESQ."CodigoVersionSII",
    ESQ."HabilitadoMantenedor",
    ESQ."TieneCertificado",
	ESQ."TieneEnvio",
    COALESCE(MOD."Nombre",'') AS "NombreModulo"
FROM "BSCL.Normativa.BD::DOC.Esquema" ESQ
LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModulo" MOD
ON (ESQ."Modulo"=MOD."IdModulo")
ORDER BY ESQ."Esquema",	ESQ."Version";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarEsquema" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
