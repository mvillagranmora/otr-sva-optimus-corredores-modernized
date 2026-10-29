SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV"; 

--SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV" 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV" AS
		
	SELECT 
		"IdStack" AS "IDSTACK","Action" AS ACTION
		,tab."TIPO_REG" || ':' || tab."CODIGO"  AS TABLE_KEY
		,tab.*
		
	FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLTMOV" tab
	LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLTMOV' AND WST."Key" = tab."TIPO_REG" || ':' || tab."CODIGO"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLTMOV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
