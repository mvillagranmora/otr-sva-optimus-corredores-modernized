SELECT "IdPerfilComercialBase"
FROM "BSCL.Plataforma.BD::COM.PerfilComercialBase"
WHERE "IdCategoriaInversionista" = :IdCategoriaInversionista
  AND "IdEmpresa" = :IdEmpresa
  AND "TipoEnrolamiento" = :TipoEnrolamiento
  AND "PorDefecto" = 'S';
