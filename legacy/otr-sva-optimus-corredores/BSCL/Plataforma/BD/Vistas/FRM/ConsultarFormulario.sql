SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarFormulario"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::FRM.ConsultarFormulario"
AS

SELECT
FORMULARIO."CodigoFormulario"
,FORMULARIO."NombreFormulario"
FROM "BSCL.Plataforma.BD::FRM.Formulario" AS FORMULARIO
WHERE FORMULARIO."IdEstado" = 'ACT';


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::FRM.ConsultarFormulario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 