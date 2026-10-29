SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormulario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormulario"
AS

SELECT
FORMULARIO."CodigoFormulario"
,FORMULARIO."NombreFormulario"
,CAMPO."CodigoCampo"
,CAMPO."CodigoCampoFrontend"
,CAMPO."CodigoValorFrontend"
,CAMPO."CodigoCampoBackend"
,CAMPO."NombreCampo"
,CAMPO."TipoDato"
,CAMPO."ValorDefecto"
,CAMPO_FORMULARIO."CantidadMinima"
,CAMPO_FORMULARIO."Requerido"
,CAMPO_FORMULARIO."RegeneraContratos"
,CAMPO_FORMULARIO."IdTipoCanal"
,CANAL."Nombre" AS "NombreCanal"
,CAMPO."TipoCampoFrontend"
,CAMPO."ConfiguracionFrontend"
,CAMPO_FORMULARIO."ValorDefecto" AS "ValorDefectoCampoFormulario"
,CAMPO_FORMULARIO."ValorDefecto" AS "NombreValorDefectoCampoFormulario"
,SUBSTRING("ConfiguracionFrontend",LOCATE("ConfiguracionFrontend", 'combobox-') ,LOCATE(SUBSTRING("ConfiguracionFrontend",LOCATE("ConfiguracionFrontend", 'combobox-') ,LENGTH("ConfiguracionFrontend")),'"')-1) as "Campo"
,CAMPO_FORMULARIO."AplicaRequerido" AS  "AplicaRequerido"
FROM "BSCL.Plataforma.BD::FRM.CampoFormulario" AS CAMPO_FORMULARIO
LEFT JOIN "BSCL.Plataforma.BD::FRM.Campo" AS CAMPO ON (CAMPO."CodigoCampo" = CAMPO_FORMULARIO."CodigoCampo")
LEFT JOIN "BSCL.Plataforma.BD::FRM.Formulario" AS FORMULARIO ON (CAMPO_FORMULARIO."CodigoFormulario" = FORMULARIO."CodigoFormulario")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" AS CANAL ON (CANAL."IdTipoCanalIngreso" = CAMPO_FORMULARIO."IdTipoCanal")
WHERE FORMULARIO."IdEstado" = 'ACT' AND CAMPO."IdEstado" = 'ACT'

;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampoFormulario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 