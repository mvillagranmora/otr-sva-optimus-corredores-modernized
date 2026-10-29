SELECT 'Eliminar Rol' AS "SubRegistro", 
       'Rol' AS "CampoRegistro", 
       R."Nombre" AS "ValorAntiguo", 
       '' AS "ValorNuevo"
FROM "BSCL.Plataforma.BD::ACC.Rol" R 
WHERE R."IdRol" = :IdRol
UNION
SELECT 'Eliminar Perfil' AS "SubRegistro", 
       'Perfil' AS "CampoRegistro",
       P."Nombre" AS "ValorAntiguo",
       '' AS "ValorNuevo"
FROM "BSCL.Plataforma.BD::ACC.RolPerfil" RP
INNER JOIN "BSCL.Plataforma.BD::ACC.Perfil" P 
  ON RP."IdPerfil" = P."IdPerfil" 
WHERE RP."IdRol" = :IdRol
