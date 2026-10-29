UPDATE "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito"
SET "IdEstado" = 'ANU'
WHERE "IdMovimientoCarteraEnTransito" IN (:IdMovimientoCarteraEnTransito);