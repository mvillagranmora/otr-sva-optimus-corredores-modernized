SELECT 
bolins."IdBolsaInstrumento"
,bolins."IdInstrumento"
,bolins."IdBolsa"
,ins."Nemotecnico"
FROM BSCL."BSCL.Plataforma.BD::INS.BolsaInstrumento" bolins
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS ins ON bolins."IdInstrumento" = ins."IdInstrumento";
