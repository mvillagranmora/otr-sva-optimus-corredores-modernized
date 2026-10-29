SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO"; 

-- SELECT  *  FROM  "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CAST (OPE.SOL_SEC AS VARCHAR)  AS TABLE_KEY,
        	*

        FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_PACTO" OPE
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'OPERACION_PACTO' AND WST."Key" = OPE.SOL_SEC

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOPERACION_PACTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
