SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Rol"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Rol" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdRol"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdRol" AS IDROL, --BIGINT
			"IdEmpresa" AS IDEMPRESA, --BIGINT
			"Nombre" AS NOMBRE, --NVARCHAR(100)
			"IdEstado" AS IDESTADO --NVARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.Rol" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Rol' AND WST."Key" = "IdRol"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Rol" TO  BCS_DESARROLLADOR WITH GRANT OPTION;