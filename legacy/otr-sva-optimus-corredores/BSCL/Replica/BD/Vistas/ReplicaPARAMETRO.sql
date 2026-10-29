SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO" AS


	SELECT 
		"IdStack" "IDSTACK" ,"Action" AS ACTION,
		 "PAR_ID" AS TABLE_KEY,
		 PAR.*
		 
		FROM  "BSCL.Replica.BD.Vistas::Sebra.PARAMETRO" PAR
	    LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'PARAMETRO' AND WST."Key" = "PAR_ID"
	

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPARAMETRO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
