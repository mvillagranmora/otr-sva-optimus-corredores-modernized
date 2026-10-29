UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET "IdOperacionOrigenReasignacion" = :IdOperacionOriginal
WHERE "IdOperacion"= :IdOperacionNueva;