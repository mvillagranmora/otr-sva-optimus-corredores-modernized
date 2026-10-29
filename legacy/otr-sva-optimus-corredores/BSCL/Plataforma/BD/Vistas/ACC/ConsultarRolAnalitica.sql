SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolAnalitica"  
AS
SELECT 
			"IdRol", --BIGINT
			"IdEmpresa", --BIGINT
			"Nombre", --NVARCHAR(100)
			"IdEstado" --NVARCHAR(3)
		FROM "BSCL.Plataforma.BD::ACC.Rol" USU;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
