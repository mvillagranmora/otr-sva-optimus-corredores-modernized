SELECT 
  CASE
    WHEN PerAcc."IdAccesoPerfil" IS NOT NULL AND COALESCE(PerAcc."IdEstado",'') != :p_IdEstado THEN 'S'
    WHEN :p_Existe = 'S' AND PerAcc."IdAccesoPerfil" IS NULL THEN 'S'
    WHEN :p_Existe = 'N' AND PerAcc."IdAccesoPerfil" IS NULL THEN 'N'
    WHEN :p_Existe = 'S' AND PerAcc."IdAccesoPerfil" IS NOT NULL THEN 'N'
    WHEN :p_Existe = 'N' AND PerAcc."IdAccesoPerfil" IS NOT NULL THEN 'S'
    ELSE 'N'
  END AS "Modificado"
FROM DUMMY
LEFT JOIN "BSCL.Plataforma.BD::ACC.PerfilAcceso" AS PerAcc
  ON PerAcc."IdAcceso" = :IdAcceso
 AND PerAcc."IdPerfil" = :IdPerfil
 AND PerAcc."IdEmpresa" = :IdEmpresa;
