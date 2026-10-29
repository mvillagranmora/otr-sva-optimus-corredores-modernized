UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
SET "IdEstado"                      = 'ANU',
    "FechaHoraUltimaActualizacion"  = :p_FechaHoraUltimaActualizacion
WHERE
    "IdEventoCapital" in (:p_IdEventoCapital);