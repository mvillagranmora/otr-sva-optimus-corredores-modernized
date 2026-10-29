UPDATE "BSCL.SocioNegocio.BD::ENR.Enrolamiento" E
SET E."CLI_IdCategoriaInversionista"   = :IdCategoriaInversionista
  , E."POR_IdPerfilComercial"          = :IdPerfilComercial
WHERE E."IdEnrolamiento" = :IdEnrolamiento;