SELECT 'Agregar Rol' as "SubRegistro", 'Rol' as "CampoRegistro", '' as "ValorAntiguo", R."Nombre" as "ValorNuevo"
FROM "BSCL.Plataforma.BD::ACC.Rol" R 
WHERE R."IdRol" = :IdRol
UNION ALL
SELECT 'Agregar Perfil' as "SubRegistro", 'Perfil' as "CampoRegistro", '' as "ValorAntiguo", P."Nombre" as "ValorNuevo"
FROM "BSCL.Plataforma.BD::ACC.RolPerfil" RP
INNER JOIN "BSCL.Plataforma.BD::ACC.Perfil" P ON RP."IdPerfil" = P."IdPerfil" 
WHERE RP."IdRol" = :IdRol
