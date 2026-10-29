SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	(TIPO_EVE_CAP || MERCADO || NEMO || EC.SEC_EVE)  AS TABLE_KEY,
        	*
		FROM "BSCL.Replica.BD.Vistas::Sebra.TBSILEVC" EC
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBSILEVC' AND WST."Key" = (TIPO_EVE_CAP || MERCADO || NEMO || EC.SEC_EVE)

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBSILEVC" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
