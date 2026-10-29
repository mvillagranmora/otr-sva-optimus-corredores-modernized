UPDATE "BSCL.Mesa.BD::SPT.Puntas"
SET
    "Fecha" = :Fecha,
    "IdUsuario" = :IdUsuario,
    "AperturaPrecio" = :AperturaPrecio,
    "PuntaCompra" = :PuntaCompra,
    "PuntaVenta" = :PuntaVenta,
    "CheckPrecioPorTramo" = :CheckPrecioPorTramo,
    "Origen" = :Origen
WHERE "IdPuntas" = :IdPuntas AND "IdEmpresa" = :IdEmpresa;