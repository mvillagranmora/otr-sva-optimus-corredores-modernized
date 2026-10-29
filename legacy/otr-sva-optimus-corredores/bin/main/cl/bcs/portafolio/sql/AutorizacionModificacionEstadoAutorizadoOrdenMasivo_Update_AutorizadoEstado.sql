UPDATE "BSCL.Portafolio.BD::POR.Autorizado"
SET "IdEstado" = :IdEstado,
    "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "IdAutorizado" = :IdAutorizado