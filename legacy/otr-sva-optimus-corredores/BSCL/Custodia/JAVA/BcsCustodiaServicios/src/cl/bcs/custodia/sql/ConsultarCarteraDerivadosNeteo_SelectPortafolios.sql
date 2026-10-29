SELECT CAR."IdPortafolio"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB ON (SUB."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" AS OPE ON (OPE."IdTipoOperacion" = CAR."IdTipoOperacion")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND CAR."FechaOperacion" = :p_FechaOperacion
GROUP BY CAR."IdPortafolio";