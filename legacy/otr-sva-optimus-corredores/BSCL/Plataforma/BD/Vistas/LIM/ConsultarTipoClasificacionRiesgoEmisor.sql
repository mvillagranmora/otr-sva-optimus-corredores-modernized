SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoEmisor"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoEmisor"  
AS
SELECT EMI."IdEmisor"
,EMI."IdEntidadClasificadoraRiesgo"
,EMI."ValorClasificacionRiesgoIIF" AS "ValorClasificacionRiesgo"
,( CASE WHEN (LOCATE (EMI."ValorClasificacionRiesgoIIF", ';'))>0 THEN  TRIM(SUBSTR_BEFORE (EMI."ValorClasificacionRiesgoIIF",';')) ELSE TRIM(EMI."ValorClasificacionRiesgoIIF") END) AS "Valor1"
,TRIM(SUBSTR_AFTER (EMI."ValorClasificacionRiesgoIIF",';')) AS "Valor2"
,TCL."IdTipoClasificacionRiesgo"
,TCL."Nombre"
 FROM "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" EMI
 , "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" TCL
 WHERE TCL."IdTipoClasificacionRiesgo"='EMI';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoEmisor" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 