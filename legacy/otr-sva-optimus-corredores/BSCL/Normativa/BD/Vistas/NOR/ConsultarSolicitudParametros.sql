SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSolicitudParametros"; 
 		
CREATE VIEW "BSCL.Normativa.BD.Vistas::NOR.ConsultarSolicitudParametros" AS

SELECT
	 PAR."IdSolicitudParametros",
	 PAR."IdSolicitudCircular",
	 PAR."TipoParametro",
	 TRIM(PAR."Valor") AS "Valor"
FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" AS PAR 
ORDER BY PAR."IdSolicitudCircular";

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NOR.ConsultarSolicitudParametros" TO  BCS_DESARROLLADOR WITH GRANT OPTION;  
