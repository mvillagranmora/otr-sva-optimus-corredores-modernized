SELECT "IdInstrumento","Nemotecnico" , "codTicker"/*,"timeTicker"*/,"codIsin","action","instruments","imbalance","tradeVolume","avgPrice","tradeAmount"
	,"trend","cfiCode","maturityDate","issuer","eventType","settlement","exercise"
	,"openContract","StrikePrice","minPrice","highPrice","roundLot", "securitySubType" 
FROM "BSCL.Plataforma.BD::INS.Instrumento"
	LEFT JOIN "BSCL.Orden.BD::MOP.INSTRUMENTOS_OP" ON "instruments" = "Nemotecnico" 
WHERE "OrigenInstrumento"='O' AND "IdEstado" = 'ACT' AND "instruments" is null