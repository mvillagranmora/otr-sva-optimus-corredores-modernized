SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso"  
AS
select  Usu."IdUsuario",
		Usu."CodigoUsuario",
       Acc."IdAcceso",
       Acc."NombreAcceso",
       Acc."TipoAcceso",
       Acc."Padre",
   	   Acc."Orden",
   	   Acc."IdServicio",
   	   Acc."IdElementoHtml",
       Acc."EsVisibleMenu",
	   (case when COALESCE (PA."IdAcceso",0) = 0 then 'N' else 'S' end) as "Existe",
	   (case when COALESCE (PER."IdEstado",'') != 'INA' then 'S' else 'N' end) as "MostrarAcceso",
	   Usu."IdEmpresa",
	   PA."AsignoUsuario" AS "IdAsignoUsuario",
	   USUASIG."CodigoUsuario" AS "CodigoAsignoUsuario",
	   PA."FechaAsignacion",
	   CASE
	   	WHEN Acc."TipoAcceso"='MENU1' OR Acc."TipoAcceso"='MENU2' OR Acc."TipoAcceso"='MENU3' THEN 'ACCESO VENTANA' 
	   	WHEN Acc."TipoAcceso"='BTN' THEN 'ACCESO BOTON'
		WHEN Acc."TipoAcceso"='CHK' THEN 'ACCESO CHECKBOX'
	   	ELSE ''
	   END AS "NombreTipoAcceso"
from "BSCL.Plataforma.BD::ACC.Usuario" as USU
CROSS join "BSCL.Plataforma.BD::ACC.Acceso" AS ACC
LEFT JOIN "BSCL.Plataforma.BD::ACC.RolUsuario" as RUSU ON USU."IdUsuario" = RUSU."IdUsuario" and RUSU."EsAutorizado" = 'S'
LEFT join "BSCL.Plataforma.BD::ACC.Rol" as ROL on ROL."IdRol" = RUSU."IdRol"
LEFT JOIN "BSCL.Plataforma.BD::ACC.RolPerfil" RolPer ON Rol."IdRol" = RolPer."IdRol" 
LEFT JOIN "BSCL.Plataforma.BD::ACC.Perfil" Per ON RolPer."IdPerfil" = Per."IdPerfil"
left JOIN "BSCL.Plataforma.BD::ACC.PerfilAcceso" as PA ON PA."IdPerfil" = Per."IdPerfil"  and PA."IdAcceso" = ACC."IdAcceso"
left join "BSCL.Plataforma.BD::ACC.Usuario" AS USUASIG ON USUASIG."IdUsuario" = PA."AsignoUsuario";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioAcceso" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
