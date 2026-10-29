SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION"; 

-- select  *  from "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION" AS

		SELECT	DISTINCT WST."IdStack" AS "IDSTACK","Action" AS ACTION, TPO."OPE_ID"  AS TABLE_KEY,
		TPO.*
		
		FROM  "BSCL.Replica.BD.Vistas::Sebra.TIPO_OPERACION" TPO		
		LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TIPO_OPERACION' AND WST."Key" = TPO."OPE_ID"
		
			

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTIPO_OPERACION" TO  BCS_DESARROLLADOR WITH GRANT OPTION;