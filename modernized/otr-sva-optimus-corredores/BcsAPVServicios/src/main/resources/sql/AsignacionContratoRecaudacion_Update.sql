UPDATE "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" 
SET "IdContratoSAAPV" = :IdContratoSAAPV,
    "FechaModificacion" = :FechaModificacion
WHERE "IdRecaudacionCliente" = :IdRecaudacionCliente;