SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV"; 

-- select  count(1)  from "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV" AS
SELECT 
    WST."IdStack" AS "IDSTACK",
    "Action" AS ACTION,
    cast(ASIG.SEC_ASIGNACION  AS varchar) AS TABLE_KEY,
	*
FROM "BSCL.Replica.BD.Vistas::Sebra.ASIGNACION_RV" ASIG
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'ASIGNACION_RV' AND WST."Key" = ASIG.SEC_ASIGNACION


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaASIGNACION_RV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
