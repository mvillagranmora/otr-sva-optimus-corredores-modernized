SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO"; 

--SELECT  *  FROM "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO" 
--- SELECT  *  FROM BSCL."BSCL.Mesa.BD::SPT.OperacionSpot" OS  WHERE "IdOperacionSpot" = 2090

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO" AS

	SELECT 
		WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		OS.SEC_MOVTO  AS TABLE_KEY,
		OS.*
		
	FROM "BSCL.Replica.BD.Vistas::Sebra.TBMOMVTO" OS
	LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBMOMVTO' AND WST."Key" = OS.SEC_MOVTO

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBMOMVTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

