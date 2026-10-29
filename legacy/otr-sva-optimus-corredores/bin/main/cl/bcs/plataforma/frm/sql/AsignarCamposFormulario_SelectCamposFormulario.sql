SELECT
CAMPO."CodigoCampoBackend"
,CAMPO."TipoDato"
,CAMPO_FORMULARIO."ValorDefecto" AS "ValorDefectoCampoFormulario"
,CAMPO."ValorDefecto"
,CAMPO."CodigoCampo"
FROM "BSCL.Plataforma.BD::FRM.CampoFormulario" AS CAMPO_FORMULARIO
LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMPO ON (CAMPO_FORMULARIO."CodigoCampo" = CAMPO."CodigoCampo")
LEFT JOIN  "BSCL.Plataforma.BD::FRM.Formulario" AS FORMULARIO ON (CAMPO_FORMULARIO."CodigoFormulario" = FORMULARIO."CodigoFormulario")
WHERE
FORMULARIO."IdEstado" = 'ACT'
AND CAMPO."IdEstado" = 'ACT' 
AND FORMULARIO."CodigoFormulario" = :CodigoFormulario
AND CAMPO_FORMULARIO."IdTipoCanal" = :IdTipoCanal
AND CAMPO_FORMULARIO."Requerido" = 'N'
AND CAMPO_FORMULARIO."ValorDefecto" != ''
AND CAMPO."TipoDato" != 'GRID'