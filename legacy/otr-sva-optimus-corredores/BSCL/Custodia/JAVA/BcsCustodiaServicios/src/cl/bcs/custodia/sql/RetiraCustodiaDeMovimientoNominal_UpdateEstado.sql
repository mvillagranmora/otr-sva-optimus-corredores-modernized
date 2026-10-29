UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" 
SET "IdEstado" = 'CRT' 
WHERE "IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito)