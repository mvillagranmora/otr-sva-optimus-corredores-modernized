SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM"; 

---select * from  "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM" AS

	SELECT
		WST."IdStack" AS "IDSTACK","Action" AS ACTION,
		cast(CO."CODOPE" AS varchar) AS TABLE_KEY,
	    CO.*
		
	FROM  "BSCL.Replica.BD.Vistas::Sebra.TBPLTRANPARAM" CO
    LEFT  JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBPLTRANPARAM' AND WST."Key" = CO."CODOPE"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBPLTRANPARAM" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
