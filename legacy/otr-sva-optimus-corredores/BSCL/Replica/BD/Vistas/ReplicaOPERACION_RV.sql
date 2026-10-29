SET SCHEMA "BSCL";
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV";
--SELECT * FROM BSCL."BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV" where  sol_sec = 234486
--delete FROM BSCL."BSCL.Orden.BD::ORD.Orden" WHERE "IdOrden" = 13846

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV" AS

SELECT 
    WST."IdStack" AS "IDSTACK","Action" AS ACTION,
    cast(ORD.SOL_SEC AS varchar) AS TABLE_KEY,
	*

FROM "BSCL.Replica.BD.Vistas::Sebra.OPERACION_RV" AS ORD 
LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'OPERACION_RV' AND WST."Key" = ORD.SOL_SEC

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaOPERACION_RV" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
