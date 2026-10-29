SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolPerfil"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolPerfil" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdRolPerfil"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdRolPerfil" AS IDROLPERFIL, --BIGINT
			"IdRol" AS IDROL, --BIGINT
			"IdPerfil" AS IDPERFIL, --BIGINT
			"IdEstado" AS IDESTADO --NVARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.RolPerfil" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_RolPerfil' AND WST."Key" = "IdRolPerfil"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolPerfil" TO  BCS_DESARROLLADOR WITH GRANT OPTION;