SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAccesoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAccesoAnalitica"  
AS
SELECT 
			"IdAccesoPerfil",
			"IdEmpresa",
			"IdPerfil",
			"IdAcceso",
			"IdEstado",
			"FechaAsignacion",
			"AsignoUsuario"
		FROM "BSCL.Plataforma.BD::ACC.PerfilAcceso" PerAcc;
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAccesoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
