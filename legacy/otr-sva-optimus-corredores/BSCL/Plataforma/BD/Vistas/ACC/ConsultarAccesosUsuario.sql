SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosUsuario";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosUsuario" AS

	SELECT  US."IdUsuario" AS "IdUsuario" , AU."IdElementoHtml" AS "IdElementoHtml" , 'Usuario' AS "Permiso"
	FROM "BSCL.Plataforma.BD::ACC.Usuario" as US
	LEFT JOIN  "BSCL.Plataforma.BD::ACC.RolUsuario" AS RU ON US."IdUsuario" = RU."IdUsuario" 
	LEFT join "BSCL.Plataforma.BD::ACC.Rol" as ROL on ROL."IdRol" = RU."IdRol"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.RolPerfil" RolPer ON Rol."IdRol" = RolPer."IdRol" 
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Perfil" Per ON RolPer."IdPerfil" = Per."IdPerfil"
	left JOIN "BSCL.Plataforma.BD::ACC.PerfilAcceso" as PA ON PA."IdPerfil" = Per."IdPerfil"
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Acceso" AS AU ON (AU."IdAcceso" = PA."IdAcceso" AND AU."CanalAcceso" = 'AGE' )
	GROUP BY US."IdUsuario" , AU."IdElementoHtml" 
	UNION (
	SELECT -1 AS "IdUsuario", AU."IdElementoHtml" AS "IdElementoHtml" , 'Control' AS "Permiso" FROM "BSCL.Plataforma.BD::ACC.Acceso" AU
	WHERE AU."CanalAcceso" = 'AGE' ) 
	
	ORDER BY "IdUsuario" DESC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ACC.ConsultarAccesosUsuario" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 