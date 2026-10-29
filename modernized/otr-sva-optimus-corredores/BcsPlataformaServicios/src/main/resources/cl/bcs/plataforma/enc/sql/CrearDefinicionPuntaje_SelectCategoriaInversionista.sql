SELECT "IdCategoriaInversionista", "NombreCategoriaInversionista", "IdEmpresa", "Jerarquia"
FROM "BSCL.Plataforma.BD::COM.CategoriaInversionista"
WHERE "IdEmpresa" = :IdEmpresa
