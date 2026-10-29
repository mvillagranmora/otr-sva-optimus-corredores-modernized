SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolUsuarioAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolUsuarioAnalitica"  
AS
SELECT 
			"IdRolUsuario", --BIGINT
			"IdUsuario", --BIGINT
			"IdRol", --BIGINT
			"EsAutorizado" --NVARCHAR(1)
		FROM "BSCL.Plataforma.BD::ACC.RolUsuario" USU;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolUsuarioAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
