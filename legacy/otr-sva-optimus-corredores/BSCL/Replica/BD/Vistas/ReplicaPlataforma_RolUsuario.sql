SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolUsuario"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolUsuario" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdRolUsuario"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdRolUsuario" AS IDROLUSUARIO, --BIGINT
			"IdUsuario" AS IDUSUARIO, --BIGINT
			"IdRol" AS IDROL, --BIGINT
			"EsAutorizado" AS ESAUTORIZADO --NVARCHAR(1)
		FROM "BSCL.Plataforma.BD::ACC.RolUsuario" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_RolUsuario' AND WST."Key" = "IdRolUsuario"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_RolUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;