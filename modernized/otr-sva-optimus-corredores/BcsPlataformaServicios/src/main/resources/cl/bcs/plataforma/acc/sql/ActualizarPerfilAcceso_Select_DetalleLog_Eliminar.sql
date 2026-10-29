SELECT A."NombreAcceso" AS "SubRegistro", 
       'IdAccesoPerfil' AS "CampoRegistro", 
       CAST(PA."IdAccesoPerfil" AS NVARCHAR) AS "ValorAntiguo", 
       '' AS "ValorNuevo"
FROM "BSCL"."BSCL.Plataforma.BD::ACC.PerfilAcceso" AS PA
INNER JOIN "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" AS A 
    ON A."IdAcceso" = PA."IdAcceso"
WHERE PA."IdAcceso" = :IdAcceso 
  AND PA."IdPerfil" = :IdPerfil 
  AND PA."IdEmpresa" = :IdEmpresa