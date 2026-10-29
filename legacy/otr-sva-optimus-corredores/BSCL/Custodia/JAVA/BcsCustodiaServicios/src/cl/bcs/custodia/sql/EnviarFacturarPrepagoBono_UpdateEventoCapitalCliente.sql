UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET  "IdEstado" = :IdEstado
    ,"IdMovimientoComprobanteFac" = :IdMovimientoComprobanteFac
WHERE "IdEventoCapitalCliente" = :IdEventoCapitalCliente;