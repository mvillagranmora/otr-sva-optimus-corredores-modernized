SELECT
	"IdUsuario"
	,"IdRol"
	,CONCAT("IdUsuario","IdRol") AS "KeyRolUsuario"
FROM "BSCL.Plataforma.BD::ACC.RolUsuario"
WHERE "IdUsuario" IN (:IdsUsuarios);