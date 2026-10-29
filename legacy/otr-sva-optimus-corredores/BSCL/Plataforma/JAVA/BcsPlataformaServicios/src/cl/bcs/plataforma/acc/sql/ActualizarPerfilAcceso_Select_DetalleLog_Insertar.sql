SELECT Acc."NombreAcceso" AS "SubRegistro", 
       'IdAccesoPerfil' AS "CampoRegistro", 
       '' AS "ValorAntiguo", 
       CAST(:IdAccesoPerfil AS NVARCHAR) AS "ValorNuevo"
FROM "BSCL"."BSCL.Plataforma.BD::ACC.Acceso" AS Acc
WHERE Acc."IdAcceso" = :IdAcceso