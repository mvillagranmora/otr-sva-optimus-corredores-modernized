SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" AS



        SELECT MVTO.SEC_MOVTO || MVTO.FEC_MVTO as TABLE_KEY,
	       WST."IdStack" AS "IDSTACK", "Action" AS ACTION,MVTO.*
        	
        	

     FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" MVTO
	LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCTMVTO' AND WST."Key" = MVTO.SEC_MOVTO || MVTO.FEC_MVTO

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" TO  BCS_DESARROLLADOR WITH GRANT OPTION;