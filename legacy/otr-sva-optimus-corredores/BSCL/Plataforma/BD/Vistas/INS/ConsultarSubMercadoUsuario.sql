SET SCHEMA "BSCL"; 
GO

DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubMercadoUsuario"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubMercadoUsuario"  
AS
	SELECT ARB."IdArbolClasificacionActivo", 
		   ARB."Descripcion", 
		   ARB."Padre",
		   ARB."Indice"
	FROM "BSCL.Plataforma.BD::INS.ArbolClasificacionActivos" AS ARB
	WHERE "Tipo"='U';
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubMercadoUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION;