UPDATE  "BSCL.Custodia.BD::CUS.Reserva"
SET  "FolioCarteraCustodia"     = :FolioCartera,
    "IdCarteraCustodia" = :IdCartera,
    "IdEstado" = :IdEstado
WHERE   "IdReserva" IN (:IdReserva);