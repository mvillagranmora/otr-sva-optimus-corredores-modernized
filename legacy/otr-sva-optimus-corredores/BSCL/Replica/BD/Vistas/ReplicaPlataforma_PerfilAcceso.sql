SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilAcceso";  

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilAcceso" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdAccesoPerfil"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdAccesoPerfil" AS IDACCESOPERFIL, --BIGINT
			"IdEmpresa" AS IDEMPRESA, --BIGINT
			"IdPerfil" AS IDPERFIL, --BIGINT
			"IdAcceso" AS IDACCESO, --BIGINT
			"IdEstado" AS IDESTADO, --NVARCHAR(3)
			COALESCE("FechaAsignacion",'1900-01-01 00:00:00') AS FECHAASIGNACION, --SECONDDATE
			COALESCE("AsignoUsuario",0) AS ASIGNOUSUARIO --BIGINT
		FROM "BSCL.Plataforma.BD::ACC.PerfilAcceso" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_PerfilAcceso' AND WST."Key" = "IdAccesoPerfil"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_PerfilAcceso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;