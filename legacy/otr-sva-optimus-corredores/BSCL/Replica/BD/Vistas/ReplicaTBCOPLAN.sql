SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN" AS

        Select WST."IdStack" AS "IDSTACK", TB.CTA_CONTAB  AS TABLE_KEY,"Action" AS ACTION,
           TB.*
		   
			FROM  "BSCL.Replica.BD.Vistas::Sebra.TBCOPLAN" TB
            LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBCOPLAN' AND WST."Key" = TB.CTA_CONTAB
            WHERE LENGTH(TB.CTA_CONTAB) <= 12

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBCOPLAN" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
                                    
