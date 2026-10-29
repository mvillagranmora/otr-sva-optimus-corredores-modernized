SELECT 
	INS."IdMoneda", 
	INS."IdTipoInstrumento",
	INS."IdTipoReajuste", 
	INS."IdEmisor", 
	INS."FechaEmision", 
	INS."FechaVencimiento", 
	INS."CorteMinimo", 
	INS."Nemotecnico"
FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS
WHERE INS."IdInstrumento" = :IdInstrumento;