UPDATE "BSCL.Mesa.BD::SPT.OperacionSpot"
SET "FechaLiquidacion" = :FechaOperacionNueva,
    "FechaOperacion"   = :FechaOperacionTimeStamp,
    "IdUsuarioModifica" = :IdUsuarioModifica,
    "FechaModifica"     = :FechaModifica
WHERE "IdOperacionSpot" = :IdOperacion