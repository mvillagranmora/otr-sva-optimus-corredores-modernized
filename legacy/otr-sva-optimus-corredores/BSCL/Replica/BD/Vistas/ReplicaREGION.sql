SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaREGION"; 

-- SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaREGION"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaREGION" AS

SELECT 
		WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		cast(DP."REG_ID" AS varchar) "TABLE_KEY",
		DP.*

	FROM  "BSCL.Replica.BD.Vistas::Sebra.REGION" DP
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'REGION' AND WST."Key" = DP."REG_ID"



WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaREGION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

