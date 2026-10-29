UPDATE "BSCL.Custodia.BD::CUS.CarteraNegativa"
SET "IdEstado" = 'PRO'
WHERE "IdPortafolio" = :IdPortafolio AND "IdInstrumento" = :IdInstrumento AND "IdEstado" = 'VIG'