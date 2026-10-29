SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTTRAS"; 
-- select  *  from "BSCL.Replica.BD.Vistas::ReplicaTBCTTRAS" where SEC_TRASPASO = 12799
CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTTRAS" AS
	
	SELECT
			WST."IdStack" AS IDSTACK,"Action" AS ACTION,
			MERCADO || ':' ||  rtrim(ltrim(NEMO)) || ':' ||  cast(SEC_TRASPASO as varchar) AS TABLE_KEY,
			*

		FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCTTRAS" TRA
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTTRAS' AND WST."Key" = TRA.SEC_TRASPASO

WITH READ ONLY; 

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTTRAS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

