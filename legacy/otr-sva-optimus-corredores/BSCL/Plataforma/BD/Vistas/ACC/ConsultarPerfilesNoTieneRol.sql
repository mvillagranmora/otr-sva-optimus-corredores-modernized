SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilesNoTieneRol";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilesNoTieneRol"  
AS
select RPER."IdRolPerfil",
	   ROL."IdEmpresa",
	   ROL."IdRol",
	   ROL."Nombre" as "NombreRol",
	   PER."IdPerfil",
	   PER."Nombre" as "NombrePerfil",
	   PER."IdEstado" as "IdEstadoPerfil",
	   (case when COALESCE (RPER."IdRol",0) = 0 then 'N' else 'S' end) as "Existe"
from "BSCL.Plataforma.BD::ACC.Rol" as ROL
cross join "BSCL.Plataforma.BD::ACC.Perfil" as PER
left join "BSCL.Plataforma.BD::ACC.RolPerfil" as RPER on ROL."IdRol" = RPER."IdRol" and PER."IdPerfil" = RPER."IdPerfil";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarPerfilesNoTieneRol"TO  BCS_DESARROLLADOR WITH GRANT OPTION; 

