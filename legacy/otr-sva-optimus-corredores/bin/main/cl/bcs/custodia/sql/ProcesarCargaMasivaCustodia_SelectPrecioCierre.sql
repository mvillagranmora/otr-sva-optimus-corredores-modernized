SELECT BOL."PrecioCierre", INS."Nemotecnico"  
FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" AS BOL
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (BOL."IdInstrumento" = INS."IdInstrumento")
WHERE INS."Nemotecnico" = :Nemotecnico AND BOL."Fecha" = :Fecha AND BOL."IdBolsa" = 'BCS';