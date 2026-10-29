SELECT CAST(:IdEnrolamiento AS BIGINT) AS "IdEnrolamiento"
     , PCB."IdCategoriaInversionista"  AS "IdCategoriaInversionista"
     , PCB."IdPerfilComercialBase"     AS "IdPerfilComercial"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase" PCB
     LEFT JOIN "BSCL.Plataforma.BD::COM.CategoriaInversionista" CI
               ON (CI."IdCategoriaInversionista" = PCB."IdCategoriaInversionista" AND CI."IdEmpresa" = :IdEmpresa AND CI."IdCategoriaInversionista" IS NOT NULL)
WHERE PCB."IdCategoriaInversionista" = :IdCategoriaInversionista
  AND PCB."IdEmpresa" = :IdEmpresa
  AND PCB."TipoEnrolamiento" = (SELECT E."TipoEnrolamiento" FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" E WHERE E."IdEnrolamiento" = :IdEnrolamiento)
  AND PCB."PorDefecto" = 'S';