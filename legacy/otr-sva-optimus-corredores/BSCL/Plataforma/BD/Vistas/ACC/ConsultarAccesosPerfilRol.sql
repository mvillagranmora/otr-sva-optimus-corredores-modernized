SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfilRol"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfilRol"  
AS
Select
	rp."IdRolPerfil", 
	r."IdEmpresa", 
	r."IdRol",
	r."Nombre" as "NombreRol",
	r."IdEstado" as "IdEstadoRol",
	p."IdPerfil",
	p."Nombre" as "NombrePerfil",
	p."IdEstado" as "IdEstadoPerfil",
	A."IdAcceso",
	A."NombreAcceso",
	A."TipoAcceso",
	A."Padre",
	A."Orden",
	A."IdServicio",
	A."IdElementoHtml",
	A."EsVisibleMenu",
	PA."IdEstado" as "IdEstadoAccesoPerfil"
	 
From "BSCL.Plataforma.BD::ACC.Rol" as r
inner join "BSCL.Plataforma.BD::ACC.RolPerfil" as rp on r."IdRol" = rp."IdRol"
inner join "BSCL.Plataforma.BD::ACC.Perfil" as p on rp."IdPerfil" = p."IdPerfil"
inner join "BSCL.Plataforma.BD::ACC.PerfilAcceso" as PA on PA."IdPerfil" = p."IdPerfil" 
inner join "BSCL.Plataforma.BD::ACC.Acceso" as A on PA."IdAcceso" = A."IdAcceso";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfilRol" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 