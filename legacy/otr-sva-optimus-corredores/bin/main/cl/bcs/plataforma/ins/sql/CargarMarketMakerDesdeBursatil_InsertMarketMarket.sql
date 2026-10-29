-- Inserta MarketMarket en optimus 
INSERT INTO "BSCL.Plataforma.BD::INS.MarketMaker"
(
	"IdInstrumento",
	"IdEstado",
	"IdCorredora",
	"FechaInicio",
	"FechaFin"
)
SELECT
	INS."IdInstrumento",
	CASE
		WHEN MKT."ESTADO" = 'V' THEN 'ACT'
		ELSE 'INA'
	END AS "IdEstado",	
	MKT."COD_CORR" AS "IdCorredora",	
	MKT."FEC_INI_MKT" AS "FechaInicio",
	MKT."FEC_TERM_MKT" 	AS "FechaFin"	
FROM "BSCL.Plataforma.BD::GBL.CargarMarketMakerDesdeBursatil_MarketMarket" AS MKT
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."Nemotecnico" = MKT."NEMO")
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON (TINS."IdTipoInstrumento" = INS."IdTipoInstrumento" AND "IdClasificacionTipoInstrumento" <> 'FM');


