UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET  "IdUsuarioEdicion" = :IdUsuarioEdicion
    ,"FechaEdicion"     = :FechaEdicion
WHERE "IdOperacion" = :IdOperacion;