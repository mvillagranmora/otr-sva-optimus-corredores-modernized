SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAnalitica"  
AS
select 
       "IdPerfil",
       "IdEmpresa",
       "Nombre",
       "IdEstado"
	FROM "BSCL.Plataforma.BD::ACC.Perfil" Per;
	
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
