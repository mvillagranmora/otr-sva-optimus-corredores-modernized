UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
SET

    "FechaCierre"                   = :FechaCierre,
    "FechaPago"                     = :FechaPago,
    "FechaHoraUltimaActualizacion"  = :FechaHoraUltimaActualizacion,
    "Precio"						= :Precio

WHERE
    "IdEventoCapital" = :IdEventoCapital AND "IdEventoCapitalCliente" =:IdEventoCapitalCliente;