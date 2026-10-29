SELECT
	INS."IdInstrumento",
	INS."Nemotecnico"
FROM "BSCL.Plataforma.BD::INS.Instrumento" INS
WHERE INS."IdEstado"='ACT';