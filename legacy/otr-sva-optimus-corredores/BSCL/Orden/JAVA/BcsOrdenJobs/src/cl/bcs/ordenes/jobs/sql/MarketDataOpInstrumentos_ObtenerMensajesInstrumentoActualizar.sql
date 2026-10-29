WITH dat AS 
(SELECT ROW_NUMBER() OVER ( PARTITION BY "IdInstrumento" ORDER BY "IdInstrumento","timeTicker" DESC  ) AS "Id", "IdInstrumento", "codTicker","timeTicker","codIsin","action","instruments","imbalance","tradeVolume","avgPrice","tradeAmount"
	,"trend","cfiCode","maturityDate","issuer","eventType","settlement","exercise"
	,"openContract","StrikePrice","minPrice","highPrice","roundLot", "securitySubType" 
FROM "BSCL.Orden.BD::MOP.INSTRUMENTOS_OP"
	INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" ON "instruments" = "Nemotecnico" AND "OrigenInstrumento"='O')

SELECT "IdInstrumento", "codTicker","timeTicker","codIsin","action","instruments","imbalance","tradeVolume","avgPrice","tradeAmount"
	,"trend","cfiCode","maturityDate","issuer","eventType","settlement","exercise"
	,"openContract","StrikePrice","minPrice","highPrice","roundLot", "securitySubType"
FROM dat
WHERE "Id" = 1;
--WHERE 
--WHERE "instruments" IN (:Nemotecnico);