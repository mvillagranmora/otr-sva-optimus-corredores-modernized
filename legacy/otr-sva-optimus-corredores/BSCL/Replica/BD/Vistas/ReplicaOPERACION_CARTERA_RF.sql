SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_RF"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_RF" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	ASIG.OCR_SOL_SEC || ':' || ASIG."IdAsignacion" || ':' || ASIG.OCR_SEC_CAR_INVER AS TABLE_KEY,
        	*

        FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_CARTERA_RF" ASIG
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'OPERACION_CARTERA_RF' AND WST."Key" = ASIG."IdAsignacion"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOPERACION_CARTERA_RF" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                   
