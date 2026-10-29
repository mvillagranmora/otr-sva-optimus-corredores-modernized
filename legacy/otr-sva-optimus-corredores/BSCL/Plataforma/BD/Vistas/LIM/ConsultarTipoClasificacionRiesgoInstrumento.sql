SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoInstrumento"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoInstrumento"  
AS
SELECT INS."IdInstrumento"
,INS."IdEntidadClasificadoraRiesgo"
,INS."ValorClasificacionRiesgo"
,( CASE WHEN (LOCATE (INS."ValorClasificacionRiesgo", ';'))>0 THEN  TRIM(SUBSTR_BEFORE (INS."ValorClasificacionRiesgo",';')) ELSE TRIM(INS."ValorClasificacionRiesgo") END) AS "Valor1"
,TRIM(SUBSTR_AFTER ("ValorClasificacionRiesgo",';')) AS "Valor2"
,TIN."IdTipoInstrumento"
,TIN."IdClasificacionTipoInstrumento"
,TCL."IdTipoClasificacionRiesgo"
,TCL."Nombre"
 FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS
INNER JOIN  "BSCL.Plataforma.BD::INS.TipoInstrumento" TIN
  ON (INS."IdTipoInstrumento"=TIN."IdTipoInstrumento")
INNER JOIN "BSCL.Plataforma.BD::INS.TipoClasificacionRiesgo" TCL
  ON (TIN."IdClasificacionTipoInstrumento"=TCL."IdClasificacionTipoInstrumento");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoClasificacionRiesgoInstrumento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
