SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Usuario"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Usuario" AS

        SELECT 
			WST."IdStack" AS "IDSTACK", 
			"IdUsuario"  AS TABLE_KEY,
			"Action" AS ACTION,
			"IdUsuario" AS IDUSUARIO, --BIGINT
			"IdEmpresa" AS IDEMPRESA, --BIGINT
			"CodigoUsuario" AS CODIGOUSUARIO, --NVARCHAR(100)
			CASE "FechaIngreso" WHEN '0001-01-01' THEN '1970-01-01' ELSE "FechaIngreso" END AS FECHAINGRESO, --DATE
			CASE "FechaVigencia" WHEN '0001-01-01' THEN '1970-01-01' ELSE "FechaVigencia" END AS FECHAVIGENCIA, --DATE
			"IdEstado" AS IDESTADO, --NVARCHAR(3)
			"TipoEntidad" AS TIPOENTIDAD, --NVARCHAR(40)
			"IdEntidad" AS IDENTIDAD, --BIGINT
			"UsuarioClientes" AS USUARIOCLIENTES, --NVARCHAR(1)
			"UsuarioNegocios" AS USUARIONEGOCIOS, --NVARCHAR(1)
			"UsuarioServicios" AS USUARIOSERVICIOS, --NVARCHAR(1)
			"UsuarioCorredores" AS USUARIOCORREDORES --NVARCHAR(1)
		FROM "BSCL.Plataforma.BD::ACC.Usuario" USU 

        LEFT JOIN "BSCL.Replica.BD.Vistas::RegistrosPendientesReplicaActualizacion" WST ON  WST."Table" = 'Plataforma_Usuario' AND WST."Key" = "IdUsuario"

WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::ReplicaPlataforma_Usuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
