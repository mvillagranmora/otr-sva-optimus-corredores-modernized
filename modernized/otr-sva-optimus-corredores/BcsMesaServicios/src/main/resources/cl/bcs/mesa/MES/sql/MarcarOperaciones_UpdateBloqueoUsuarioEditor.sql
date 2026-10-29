UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET  "BloqueoPorEdicion"           = :BloqueoPorEdicion
    ,"CodigoUsuarioBloqueoEdicion" = :CodigoUsuarioBloqueoEdicion
    ,"FechaHoraInicioEdicion"      = :FechaHoraInicioEdicion
WHERE "IdOperacion" = :IdOperacion;