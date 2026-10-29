SELECT 
    "FolioCartera", 
    "IdCliente", 
    "IdPortafolio", 
    "IdBoveda", 
    "CuentaBoveda",
    "FechaOperacion", 
    "FechaLiquidacion", 
    "Cantidad", 
    "PrecioCompra", 
    "MontoCompra",
    "IdClasificacionTipoInstrumento",
    "IdTipoInstrumento",
    "IdInstrumento",
    "IdEmisor",
    "IdMonedaEmision",
    "IdMonedaInstrumento",
    "IdTipoProducto",
    "IdCondicionLiquidacion",
    "IdTipoCanalIngreso"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE "Nemotecnico" = :NemoOrigen
  AND "Cantidad" > 0 
  AND "IdEstado" = 'VIG';