SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Perfil"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Perfil" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdPerfil"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdPerfil" AS IDPERFIL, --BIGINT
			"IdEmpresa" AS IDEMPRESA, --BIGINT
			"Nombre" AS NOMBRE, --NVARCHAR(100)
			"IdEstado" AS IDESTADO --NVARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.Perfil" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Perfil' AND WST."Key" = "IdPerfil"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Perfil" TO  BCS_DESARROLLADOR WITH GRANT OPTION;