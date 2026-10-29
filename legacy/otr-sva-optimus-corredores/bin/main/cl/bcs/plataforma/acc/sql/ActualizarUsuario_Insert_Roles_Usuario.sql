INSERT INTO "BSCL.Plataforma.BD::ACC.RolUsuario"("IdRolUsuario", "IdUsuario", "IdRol", "EsAutorizado")
SELECT
"BSCL.Plataforma.BD.Secuencias::RolUsuario".NEXTVAL AS "IdRolUsuario", :IdUsuario AS "IdUsuario", RU."IdRol", 'S' AS "EsAutorizado"
FROM  "BSCL.Plataforma.BD::ACC.RolUsuario" RU
WHERE RU."IdUsuario" = :HeredarRolesUsuario;
