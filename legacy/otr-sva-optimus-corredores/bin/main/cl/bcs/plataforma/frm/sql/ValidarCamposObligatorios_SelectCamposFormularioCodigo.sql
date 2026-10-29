SELECT CAMPO."CodigoCampoBackend"
,CAMPO."CodigoCampoFrontend"
,CAMPO."CodigoCampo"
,CAMPO."NombreCampo"
,CAMPO."TipoDato"
,CAMPO."ValorDefecto"
FROM "BSCL.Plataforma.BD::FRM.CampoFormulario" AS CAMPO_FORMULARIO
LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMPO ON (CAMPO_FORMULARIO."CodigoCampo" = CAMPO."CodigoCampo")
LEFT JOIN "BSCL.Plataforma.BD::FRM.Formulario" AS FORMULARIO ON (CAMPO_FORMULARIO."CodigoFormulario" = FORMULARIO."CodigoFormulario")
WHERE
FORMULARIO."IdEstado" = 'ACT'
AND CAMPO."IdEstado" = 'ACT' 
AND CAMPO."CodigoCampoBackend" = :CodigoCampoBackend
AND CAMPO_FORMULARIO."IdTipoCanal" = :IdTipoCanal
AND CAMPO_FORMULARIO."Requerido" = 'S'
AND CAMPO."TipoDato" != 'GRID'