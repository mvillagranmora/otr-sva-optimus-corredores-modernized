UPDATE "BSCL.SocioNegocio.BD::PER.Persona"
SET "IdProfesion" = COALESCE(:IdTipoCargoPersona ,"IdProfesion")
WHERE "IdPersona" = :IdPersona;