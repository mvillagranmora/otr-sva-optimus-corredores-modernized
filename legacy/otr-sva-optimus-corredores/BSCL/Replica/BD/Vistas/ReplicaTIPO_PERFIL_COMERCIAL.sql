SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL" AS

		SELECT WST."IdStack" AS "IDSTACK","Action" AS ACTION,
			PCO."TPE_ID"   AS TABLE_KEY,
			PCO.*
		FROM  "BSCL.Replica.BD.Vistas::Sebra.TIPO_PERFIL_COMERCIAL" PCO
		LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TIPO_PERFIL_COMERCIAL' AND WST."Key" = PCO."TPE_ID"
            
            
WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTIPO_PERFIL_COMERCIAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
