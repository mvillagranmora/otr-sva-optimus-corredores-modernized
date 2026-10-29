SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaORDEN"; 

--select * from "BSCL.Replica.BD.Vistas::Sebra.ORDEN" S
--inner JOIN "BSCL.Plataforma.BD::PLA.Replica.WorkStack" s1 ON S1."Key" = S.SOL_SEC  AND S1."Table"= 'ORDEN'

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaORDEN" AS
SELECT 
    WST."IdStack" AS "IDSTACK","Action" AS ACTION,
    cast(ORD.ORD_SEC AS varchar) AS TABLE_KEY,
	*
FROM "BSCL.Replica.BD.Vistas::Sebra.ORDEN" ORD
LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'ORDEN' AND WST."Key" = ORD.SOL_SEC

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaORDEN" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
