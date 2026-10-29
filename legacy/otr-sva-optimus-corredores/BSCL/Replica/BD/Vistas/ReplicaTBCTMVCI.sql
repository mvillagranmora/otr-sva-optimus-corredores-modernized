SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMVCI"; 
		


CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMVCI"  AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
     	   CAST(SEC_MOVTO AS varchar(30)) || CAST(SEC_CAR_INVER AS varchar(30)) AS TABLE_KEY,
        	mv.*
        FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCTMVCI"  mv
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTMVCI' AND WST."Key" =
        				CAST(SEC_MOVTO AS varchar(30)) || CAST(SEC_CAR_INVER AS varchar(30))
        

WITH READ ONLY;

GRANT SELECT ON  "BSCL.Replica.BD.Vistas::ReplicaTBCTMVCI" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
