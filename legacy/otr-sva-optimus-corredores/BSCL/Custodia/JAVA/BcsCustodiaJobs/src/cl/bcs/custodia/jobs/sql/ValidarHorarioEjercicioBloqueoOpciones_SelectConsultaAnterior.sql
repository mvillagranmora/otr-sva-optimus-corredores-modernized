SELECT "IdEmpresa",	"FechaRegistro",	"Evento",	"DATA" AS "Data",	"EsProcesado" 
FROM "BSCL.Plataforma.BD::PLA.MensajeWebsocket" 
WHERE "IdEmpresa" = :IdEmpresa AND "FechaRegistro" >= :Fecha AND "Evento" IN (:CodigoEvento);