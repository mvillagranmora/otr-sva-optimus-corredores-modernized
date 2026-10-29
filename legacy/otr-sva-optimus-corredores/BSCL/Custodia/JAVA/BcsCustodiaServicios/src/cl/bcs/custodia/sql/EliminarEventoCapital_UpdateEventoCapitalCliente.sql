UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET "IdEstado"                      = 'ANU',
    "FechaHoraUltimaActualizacion"  = :p_FechaHoraUltimaActualizacion
WHERE "IdEventoCapital" in (:p_IdEventoCapital);