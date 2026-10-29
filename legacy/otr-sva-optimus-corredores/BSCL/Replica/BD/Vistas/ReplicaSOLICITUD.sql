SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD"; 

-- select *  from BSCL."BSCL.Replica.BD.Vistas::ReplicaSOLICITUD" where   sol_sec = 234486

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD" AS

	SELECT
		"IdStack" AS "IDSTACK","Action" AS ACTION,
		cast(ORD.SOL_SEC AS varchar) AS TABLE_KEY,
		*
		
	FROM "BSCL.Replica.BD.Vistas::Sebra.SOLICITUD" ORD
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'SOLICITUD' AND WST."Key" = ORD.SOL_SEC

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaSOLICITUD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
