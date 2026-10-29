-- Inserta datos de la ruebas en tabla global temporal

INSERT INTO "BSCL.Plataforma.BD::GBL.CargarMarketMakerDesdeBursatil_MarketMarket"
(
	"MERCADO",
	"NEMO",
	"COD_CORR",
	"ESTADO",
	"FEC_INI_MKT",
	"FEC_TERM_MKT"   										  		  	
)
VALUES 
(
	:MERCADO,
	:NEMO,
	:COD_CORR,
	:ESTADO,
	:FEC_INI_MKT,
	:FEC_TERM_MKT
);