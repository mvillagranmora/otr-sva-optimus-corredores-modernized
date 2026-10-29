SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_IF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_IF" AS

        Select WST."IdStack" AS "IDSTACK","Action" AS ACTION,
        	CAST (ORD.SOL_SEC AS VARCHAR) AS TABLE_KEY,
        	*
        FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_IF" ORD
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'OPERACION_IF' AND WST."Key" = ORD.SOL_SEC

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOPERACION_IF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
