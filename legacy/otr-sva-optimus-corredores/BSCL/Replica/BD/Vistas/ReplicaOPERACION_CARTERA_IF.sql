SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF"; 

--SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	ASIG.OCI_SOL_SEC || ':' || ASIG.OCI_SEC_CAR_INVER AS TABLE_KEY,
            *
        FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_IF" ASIG
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'OPERACION_CARTERA_IF' AND WST."Key" = ASIG.OCI_SOL_SEC || ':' || ASIG.OCI_SEC_CAR_INVER

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_IF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
