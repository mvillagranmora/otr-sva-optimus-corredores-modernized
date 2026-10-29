SELECT "IdComprobantePagoOperacion" 
FROM "BSCL.Mesa.BD::MES.ComprobantePagoOperacion" 
WHERE "IdOperacion" IN (:IdOperacion);  