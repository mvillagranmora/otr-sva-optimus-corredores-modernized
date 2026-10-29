SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD"; 

-- SELECT * FROM "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD" 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	 COALESCE (CAST(trad."TIPO_TRAD" AS VARCHAR(4)),'') || ':' ||  COALESCE (CAST(trad."COD_TRAD" AS VARCHAR(100)),'0') || ':' ||  trad."SEC_SINONIMO" AS TABLE_KEY,
        	 trad.*

		FROM  "BSCL.Replica.BD.Vistas::Sebra.TBSITRAD" trad
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'TBSITRAD' AND WST."Key" = COALESCE (CAST(trad."TIPO_TRAD" AS VARCHAR(4)),'') || ':' ||  COALESCE (CAST(trad."COD_TRAD" AS VARCHAR(100)),'0') || ':' ||  trad."SEC_SINONIMO"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaTBSITRAD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
