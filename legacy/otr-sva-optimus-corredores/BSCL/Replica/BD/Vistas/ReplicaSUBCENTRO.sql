SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO"; 
-- SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO"
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO" AS

        SELECT WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CCO."SUB_ID" || ':' || CCO."CEN_ID"  AS TABLE_KEY,
			CCO.*
			
        FROM  "BSCL.Replica.BD.Vistas::Sebra.SUBCENTRO" CCO
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SUBCENTRO' --AND WST."Key" = DUM."Key"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSUBCENTRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
