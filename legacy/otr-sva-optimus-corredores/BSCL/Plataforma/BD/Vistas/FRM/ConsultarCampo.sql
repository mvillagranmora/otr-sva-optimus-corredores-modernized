SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampo"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampo"
AS

SELECT
CAMPO."CodigoCampo"
,CAMPO."CodigoCampoFrontend"
,CAMPO."CodigoCampoBackend"
,CAMPO."NombreCampo"
,CAMPO."TipoDato"
,CAMPO."ValorDefecto"
FROM "BSCL.Plataforma.BD::FRM.Campo" AS CAMPO
WHERE CAMPO."IdEstado" = 'ACT';


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::FRM.ConsultarCampo" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 