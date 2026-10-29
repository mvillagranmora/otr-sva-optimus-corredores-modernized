UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET  "BloqueoPorEdicion"           = 'N'
    ,"CodigoUsuarioBloqueoEdicion" = ''
WHERE "IdOperacion" = :IdOperacion;