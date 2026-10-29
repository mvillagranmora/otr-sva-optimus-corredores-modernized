SELECT INS."IdInstrumento", INS."Nemotecnico", COALESCE(EVP."Fecha",'') AS "Fecha"  FROM "BSCL.Plataforma.BD::INS.Instrumento"  AS INS 
LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.EntidadVectorPrecio"  AS EVP ON INS."Nemotecnico" = EVP."Nemotecnico"  AND EVP."Fecha" IN (:Fechas)
WHERE INS."Nemotecnico" IN (:Instrumentos) 