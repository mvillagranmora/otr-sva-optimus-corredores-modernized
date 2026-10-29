UPDATE "BSCL.SocioNegocio.BD::PER.NombreFantasia"
SET "DescripcionNombreFantasia" = :p_NombreFantasia, "IdEstado" = :p_IdEstado, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdNombreFantasia" = :p_IdNombreFantasia AND "IdPersona" = :p_IdPersona
