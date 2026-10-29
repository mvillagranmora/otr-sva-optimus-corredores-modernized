UPDATE "BSCL.Mesa.BD::MES.Operacion"
SET "IdEstado"='VIG'
WHERE "IdOperacion" IN (:IdOperacion)
  AND "IdTipoProducto" = 'MEFW'
  AND "IdEstado" IN ('ANT','LIQ')
  AND "IdEmpresa" = :IdEmpresa;
