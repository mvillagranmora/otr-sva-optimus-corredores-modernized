UPDATE "BSCL.Portafolio.BD::POR.Portafolio"
SET "IdNombreFantasia" = COALESCE(:IdNombreFantasia, 0)
WHERE "IdPortafolio" IN (:IdPortafolio);