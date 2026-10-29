SET SCHEMA "BSCL";

DROP VIEW "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion";

CREATE VIEW  "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" AS
	SELECT max("IdStack") AS "IdStack" ,"Key", "Table","Action"
	from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" s1
	WHERE  "Status" = 0 AND "Action" in ('A','I') AND  "Table" != ''
	GROUP BY "Key", "Table","Action"
	ORDER BY "IdStack";

GRANT SELECT ON "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
	