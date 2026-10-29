SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD"; 
-- SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CAST(PCOM."SEC_CMBTE" AS varchar) AS TABLE_KEY,
            PCOM.*

        FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCOMSSD" PCOM
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCOMSSD' AND WST."Key" = PCOM."SEC_CMBTE"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCOMSSD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
