SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos" AS
    SELECT
	INS."IdInstrumento",
	INS."Nemotecnico",
 	BON."TasaEmision",
	TO_INT(INS."FrecuenciaCupon") AS "PeriodoPago",
	INS."NumeroCupones",
	INS."IdMoneda",
	BON."CuponeraVariable",
	BON."CallConfirmado",
 	BON."TipoCalculoInteresesCupon",
 	BON."TipoCalculoInteresesDevengados",
	INS."IdTipoReajuste",
	BON."TenorInflacion",
    BON."TipoLibor",
	BON."FechaDevengo",
	INS."FechaVencimiento",
    BON."IdCalendario",
    BON."ConvencionDias",
	BON."AjusteFinDeMes",
    BON."IdTipoInflacion",
	BON."FechaPrepago",
    BON."CUSIP",
    BON."COMMON",
    BON."SEDOL",
    BON."TickerBloomberg",
    BON."MercadoLocal",
    BON."MercadoExtrabursatil",
    BON."MercadoExtranjero",
 	BON."PresenciaBursatil",
    BON."ValorBolsa",
    BON."ValorBloomberg",
    BON."DurationBolsa",
 	BON."DurationBloomberg",
    BON."IdPaisTransaccion",
    BON."FechaValorizacion",
 	BON."ValorNominal",
    BON."PrecioValorizacion"
    FROM "BSCL.Plataforma.BD::INS.InstrumentoBono" BON
    LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = BON."IdInstrumento"
    LEFT JOIN "BSCL.Plataforma.BD::REG.Pais" PAI ON INS."IdPaisEmisor" = PAI."IdPais";;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::VAL.ConsultarBonos"
    TO  BCS_DESARROLLADOR WITH GRANT OPTION;
    
