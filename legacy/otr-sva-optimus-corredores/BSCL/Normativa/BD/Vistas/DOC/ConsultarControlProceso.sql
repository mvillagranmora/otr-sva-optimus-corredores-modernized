SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarControlProceso";
 
CREATE VIEW "BSCL.Normativa.BD.Vistas::DOC.ConsultarControlProceso"
AS 
/*SELECT
ESQUEMA."Esquema"
, ESQUEMA."Version"
, 'UI_NO' AS "Carga"
, 'UI_NO' AS "Validacion"
, 'UI_NO' AS "PreDeclaracion"
, 'UI_NO' AS "Certificacion"
FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA;
*/
SELECT 
  ESQUEMA."Esquema"
, ESQUEMA."Version"
, CASE WHEN Control."Carga" 			= 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Carga" --'UI_NO' AS "Carga" 
, CASE WHEN Control."Validacion" 		= 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Validacion" --'UI_NO' AS "Validacion"
, CASE WHEN Control."PreDeclaracion" 	= 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "PreDeclaracion" --'UI_NO' AS "PreDeclaracion"
, CASE WHEN Control."Certificacion" 	= 'S' THEN 'UI_SI' ELSE 'UI_NO' END AS "Certificacion" --'UI_NO' AS "Certificacion"
FROM "BSCL.Normativa.BD::DOC.Esquema" ESQUEMA
		LEFT OUTER JOIN "BSCL.Normativa.BD::TRI.ControlProceso" Control
					ON Control."Esquema" = ESQUEMA."Esquema"
				   AND Control."Version" = ESQUEMA."Version";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::DOC.ConsultarControlProceso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
