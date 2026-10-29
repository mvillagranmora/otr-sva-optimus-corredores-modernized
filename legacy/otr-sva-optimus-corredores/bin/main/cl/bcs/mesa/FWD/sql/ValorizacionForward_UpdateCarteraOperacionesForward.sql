UPDATE "BSCL.Custodia.BD::CUS.Cartera"
SET "IdEstado" = 'VCD'
WHERE "FechaVencimientoOperacion" < TO_DATE(:FechaProceso)
  AND "IdTipoProducto" = 'MEFW'
  AND "IdEmpresa" = :IdEmpresa
  AND "IdEstado" = 'VIG';