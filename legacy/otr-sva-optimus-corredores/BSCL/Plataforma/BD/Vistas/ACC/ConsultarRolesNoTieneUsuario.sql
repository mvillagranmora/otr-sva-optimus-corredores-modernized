SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesNoTieneUsuario";
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesNoTieneUsuario"  
AS
select USU."IdUsuario",
	   ROL."IdEmpresa",
       ROL."IdRol",
       ROL."Nombre",
       ROL."IdEstado",
       RUSU."EsAutorizado",
       (case when COALESCE (RUSU."IdRol",0) = 0 then 'N' else 'S' end) as "Existe"
from "BSCL.Plataforma.BD::ACC.Usuario" as USU
cross join "BSCL.Plataforma.BD::ACC.Rol" as ROL
LEFT JOIN "BSCL.Plataforma.BD::ACC.RolUsuario" as RUSU ON USU."IdUsuario" = RUSU."IdUsuario" and ROL."IdRol" = RUSU."IdRol";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarRolesNoTieneUsuario"TO  BCS_DESARROLLADOR WITH GRANT OPTION; 