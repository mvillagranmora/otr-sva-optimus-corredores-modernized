SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLCLPR"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLCLPR" AS

        Select WST."IdStack" AS "IDSTACK", ''  AS TABLE_KEY,
        TRA.*
		
		FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLCLPR" TRA
        INNER JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLCLPR' --AND WST."Key" = DUM."Key"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLCLPR" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    