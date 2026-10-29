UPDATE "BSCL.Custodia.BD::CUS.CarteraNegativa"
SET "IdEstado" = 'VIG'
WHERE "IdPortafolio" = :IdPortafolio AND "IdInstrumento" = :IdInstrumento AND "IdEstado" = 'PRO'