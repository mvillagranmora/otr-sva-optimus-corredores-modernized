SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles"  
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
	EPl."Descripcion" as "EstadoPerfil"
From "BSCL.Plataforma.BD::ACC.Rol" as r
inner join "BSCL.Plataforma.BD::ACC.RolPerfil" as rp on r."IdRol" = rp."IdRol"
inner join "BSCL.Plataforma.BD::ACC.Perfil" as p on 	rp."IdPerfil" = p."IdPerfil"
left JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoPla" EPL on EPL."IdEstado" = p."IdEstado" and EPL."NombreEntidad" = 'PERFIL';


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesPerfiles" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 