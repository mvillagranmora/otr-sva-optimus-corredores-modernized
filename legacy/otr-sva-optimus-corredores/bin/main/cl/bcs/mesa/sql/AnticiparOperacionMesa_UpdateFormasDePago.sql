UPDATE "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
SET "Monto"=:Monto
WHERE "IdComprobantePagoOperacion" IN (:IdComprobantePagoOperacion)