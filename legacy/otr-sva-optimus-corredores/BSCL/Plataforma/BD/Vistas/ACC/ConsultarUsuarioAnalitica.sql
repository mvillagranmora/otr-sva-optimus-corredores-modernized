SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAnalitica"  
AS
 SELECT 
			"IdUsuario", --BIGINT
			"IdEmpresa", --BIGINT
			"CodigoUsuario", --NVARCHAR(100)
			CASE "FechaIngreso" WHEN '0001-01-01' THEN '1970-01-01' ELSE "FechaIngreso" END AS "FechaIngreso", --DATE
			CASE "FechaVigencia" WHEN '0001-01-01' THEN '1970-01-01' ELSE "FechaVigencia" END AS "FechaVigencia", --DATE
			"IdEstado", --NVARCHAR(3)
			"TipoEntidad", --NVARCHAR(40)
			"IdEntidad", --BIGINT
			"UsuarioClientes", --NVARCHAR(1)
			"UsuarioNegocios", --NVARCHAR(1)
			"UsuarioServicios", --NVARCHAR(1)
			"UsuarioCorredores" --NVARCHAR(1)
		FROM "BSCL.Plataforma.BD::ACC.Usuario" USU ;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
