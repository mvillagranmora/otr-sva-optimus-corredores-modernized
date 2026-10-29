SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD"; 

-- select * from "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD"

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD" AS

        Select  WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	CAST( OPE.FOLIOTRANSACCION  AS varchar)  AS TABLE_KEY,
            *
        FROM "BSCL.Replica.BD.Vistas::Sebra.FMMOVTOSFORWARD" OPE
            LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'FMMOVTOSFORWARD' AND WST."Key" = CAST(OPE.FOLIOTRANSACCION  AS varchar)

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaFMMOVTOSFORWARD" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
