SELECT DISTINCT CAR."IdInstrumento",INS."Nemotecnico",CAR."IdClasificacionTipoInstrumento" FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON CAR."IdInstrumento" = INS."IdInstrumento"
WHERE CAR."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','PAC','REP','RRV','RRF')