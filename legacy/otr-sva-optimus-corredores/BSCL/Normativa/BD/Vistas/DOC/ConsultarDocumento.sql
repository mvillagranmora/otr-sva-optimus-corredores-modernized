SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento"
AS 

SELECT
    "IdDocumento",
    "IdTipoDocumento",
    "Esquema",
    "Version",
    "IdEmpresa",
    "FechaLimite",
    "IdEstadoDocumento",
    "FechaCreacion",
    "FechaPresentacion",
    "DocumentoIdentidadDeclarante",
    "DocumentoIdentidadRepresentante",
    "DireccionElectronicaDeclaracion",
    "DireccionFisicaDeclaracion",
    "ComunaDeclaracion",
    "TelefonoDeclaracion",
    "FolioDeclaracion",
    "EnProceso",
    "FechaInicioUltimoProceso",
	"AnoMes",
    "FechaCertificado",
    "DefineFechaCertificado"
FROM "BSCL.Normativa.BD::DOC.Documento";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
