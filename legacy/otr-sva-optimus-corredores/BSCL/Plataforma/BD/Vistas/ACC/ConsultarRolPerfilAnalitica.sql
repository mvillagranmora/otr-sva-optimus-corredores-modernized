SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolPerfilAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolPerfilAnalitica"  
AS
SELECT 
			"IdRolPerfil", --BIGINT
			"IdRol", --BIGINT
			"IdPerfil", --BIGINT
			"IdEstado" --NVARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.RolPerfil" USU;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolPerfilAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
