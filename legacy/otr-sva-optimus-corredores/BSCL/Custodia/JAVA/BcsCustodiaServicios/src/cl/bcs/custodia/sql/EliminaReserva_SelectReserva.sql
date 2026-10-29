SELECT RES."IdReserva"
FROM "BSCL.Custodia.BD::CUS.Reserva" AS RES
WHERE RES."IdReserva" IN (:IdReserva);

