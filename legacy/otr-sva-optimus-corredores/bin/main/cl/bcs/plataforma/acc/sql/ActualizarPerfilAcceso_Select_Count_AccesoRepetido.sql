SELECT COALESCE(COUNT(pa."IdAccesoPerfil"),0) AS "COUNT"
FROM "BSCL"."BSCL.Plataforma.BD::ACC.PerfilAcceso" pa
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" a 
    ON pa."IdAcceso" = a."IdAcceso"
WHERE pa."IdAcceso" = :IdAcceso 
  AND pa."IdEmpresa" = :IdEmpresa 
  AND a."TipoAcceso" != 'MENU1'