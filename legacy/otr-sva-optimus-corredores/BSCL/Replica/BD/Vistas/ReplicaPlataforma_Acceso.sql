SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Acceso"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Acceso" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdAcceso"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdAcceso" AS IDACCESO, --BIGINT
			"NombreAcceso" AS NOMBREACCESO, --NVARCHAR(100)
			"TipoAcceso" AS TIPOACCESO, --NVARCHAR(5)
			"Padre" AS PADRE, --BIGINT
			"Orden" AS ORDEN, --INTEGER
			"IdServicio" AS IDSERVICIO, --BIGINT
			"IdElementoHtml" AS IDELEMENTOHTML, --NVARCHAR(200)
			"EsVisibleMenu" AS ESVISIBLEMENU, --NVARCHAR(1)
			"CanalAcceso" AS CANALACCESO --VARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.Acceso" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Acceso' AND WST."Key" = "IdAcceso"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Acceso" TO  BCS_DESARROLLADOR WITH GRANT OPTION;