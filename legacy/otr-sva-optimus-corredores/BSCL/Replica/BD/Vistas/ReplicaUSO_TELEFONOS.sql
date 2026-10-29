SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS" AS

        Select WST."IdStack" AS "IDSTACK", "Action" AS ACTION,
        	PER."TEL_ID" || ':' || PER."DIR_ID"  AS TABLE_KEY,
			PER."TEL_ID",
			PER."DIR_ID",
			PER."UTE_FEC_INICIO"

        FROM  "BSCL.Replica.BD.Vistas::Sebra.USO_TELEFONOS" PER
        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'USO_TELEFONOS' AND WST."Key" = PER."TEL_ID" || ':' || PER."DIR_ID"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaUSO_TELEFONOS" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
