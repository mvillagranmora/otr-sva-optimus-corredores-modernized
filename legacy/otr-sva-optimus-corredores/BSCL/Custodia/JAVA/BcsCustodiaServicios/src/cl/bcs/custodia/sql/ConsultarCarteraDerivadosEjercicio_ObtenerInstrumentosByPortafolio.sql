SELECT 
CAR."IdClasificacionTipoInstrumento",
CAR."IdTipoInstrumento",
CAR."IdInstrumento",
CAR."Nemotecnico",
SUB_INS."IdInstrumento" AS "IdSubyacente",
SUB_INS."Nemotecnico" AS "NemotecnicoSubyacente"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" CAR
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON (INS."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarSubyacenteOpciones" AS SUB_OPC ON (SUB_OPC."Nemotecnico" = CAR."Nemotecnico")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentoSubyacente" AS SUB_INS ON (SUB_INS."Nemotecnico" = SUB_OPC."Nemo")
WHERE CAR."IdClasificacionTipoInstrumento" = 'DV'
AND CAR."IdTipoProducto" != 'MEFW'
AND (SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'COMO' OR SUBSTRING(CAR."IdTipoOperacion", 0, 4) = 'VENO')
AND CAR."FechaVencimientoOperacion" = :p_FechaConsulta
AND CAR."IdPortafolio" = :p_IdPortafolio
GROUP BY CAR."IdInstrumento", CAR."IdClasificacionTipoInstrumento", CAR."IdTipoInstrumento", CAR."Nemotecnico", SUB_INS."IdInstrumento", SUB_INS."Nemotecnico";