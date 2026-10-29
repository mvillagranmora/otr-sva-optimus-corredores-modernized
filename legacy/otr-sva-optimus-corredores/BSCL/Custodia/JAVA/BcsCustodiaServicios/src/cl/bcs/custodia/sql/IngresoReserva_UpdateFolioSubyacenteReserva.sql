UPDATE  "BSCL.Custodia.BD::CUS.Reserva"
SET  "FolioSubyacente"     = :FolioSubyacente
WHERE   "IdReserva" IN (:IdReserva);