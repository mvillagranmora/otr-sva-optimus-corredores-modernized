SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosPerfil"  
AS
select 	PER."IdPerfil",
		PER."Nombre" as "NombrePerfil",
		PER."IdEstado" as "IdEstadoPerfil",
		PER."IdEmpresa" as "IdEmpresaPerfil",
		PA."IdAccesoPerfil",
		PA."IdEmpresa" as "IdEmpresaAcceso",
		PA."IdEstado" as "IdEstadoPerfilAcceso",
		A."CanalAcceso",
		A."IdAcceso",
		A."NombreAcceso",
		A."TipoAcceso",
		A."Padre",
		A."Orden",
		A."IdServicio",
		A."IdElementoHtml",
		A."EsVisibleMenu",
		(case when COALESCE (PA."IdAcceso",0) = 0 then 'N' else 'S' end) as "Existe"	
from "BSCL.Plataforma.BD::ACC.Perfil" as PER
cross join "BSCL.Plataforma.BD::ACC.Acceso" as A
left join "BSCL.Plataforma.BD::ACC.PerfilAcceso" as PA on PA."IdPerfil" = PER."IdPerfil" and PA."IdAcceso" = A."IdAcceso";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfiles" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
