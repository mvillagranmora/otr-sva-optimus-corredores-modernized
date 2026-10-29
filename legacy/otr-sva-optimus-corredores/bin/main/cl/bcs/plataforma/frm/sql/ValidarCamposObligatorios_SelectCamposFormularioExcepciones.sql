SELECT 
CAMPO_FORMULARIO_EXCEPCION."CodigoCampo"
, CAMPO_FORMULARIO_EXCEPCION."CodigoCampoExcepcion"
, CAMPO_FORMULARIO_EXCEPCION."ValorCampoExcepcion"
, CAMPO."CodigoCampoBackend" AS "CodigoCampoBackendExcepcion"
, CAMPO."TipoDato"
FROM "BSCL.Plataforma.BD::FRM.CampoFormularioExcepcion" AS CAMPO_FORMULARIO_EXCEPCION
LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMPO ON (CAMPO_FORMULARIO_EXCEPCION."CodigoCampoExcepcion" = CAMPO."CodigoCampo")
WHERE 
CAMPO_FORMULARIO_EXCEPCION."CodigoFormulario" = :CodigoFormulario
AND CAMPO_FORMULARIO_EXCEPCION."IdTipoCanal" = :IdTipoCanal
AND CAMPO_FORMULARIO_EXCEPCION."CodigoCampo" IN (:CodigoCampo)
AND CAMPO."CodigoCampo" IS NOT NULL;
