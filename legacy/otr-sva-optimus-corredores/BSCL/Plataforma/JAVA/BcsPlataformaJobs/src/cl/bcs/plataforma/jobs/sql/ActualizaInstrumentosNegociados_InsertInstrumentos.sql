
INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","FechaEmision",
	"IdInstrumentoRelacionado","FechaVencimiento","IdEmisor","IdTipoReajuste","Reajustable","IdMoneda","IdTipoInstrumento", "IdTipoInstrumentoOrigen","IdEstado", "DecimalesPrecio","Tamano", "FechaInicioVigencia","IndArt104",
	"TasaFiscal", "TipoValorizacion","ValorizaPor","Precio","Tasa","TieneCupon","NumeroCuotasCirculacionMesActual","NumeroAccionesCirculacion", "IndicadorLiquidaDCV","IdPaisEmisor",
    "FrecuenciaCupon","Serie","PrecioReferencia","IndicadorBolson","FechaReferencia","NumeroCupones","CodigoRubro","PresenciaBursatilAjustada", "InstrumentoAcreditado","ModalidadLiquidacion",
	"PrecioStrike","LargoPlazo","EsPutEsCall","FactorExposicion", "OrigenInstrumento", "EstiloOpcion", "PactoAndino", "FormadorMercado", "PorcentajeCastigo", "FechaHastaAcreditado", "RelacionadoGrupo",
    "ModalidadEjercicio",  "TipoRenta","TipoBonoIRF",
	"IdMonedaEmision", "NumeroAmortizaciones", "IdOrdenSerie", "IdCarga", "Tera",
    "AfectoArt107", "EsAutorizado", "AfectoArt104", "CobroComisionesCustodia", "IdInstrumentoRaiz", "IdCondicionLiquidacion", "EsInstrumentoCalificado")
VALUES (
	:IdInstrumento, :Nemotecnico, :ISIN, :CorteMinimo, :EstaInscrito, :EsDivisible, :RazonSocial, :FechaEmision,
	:IdInstrumentoRelacionado, :FechaVencimiento, :IdEmisor, :IdTipoReajuste, :Reajustable, :IdMoneda, :IdTipoInstrumento, :IdTipoInstrumentoOrigen, :IdEstado, :DecimalesPrecio, :Tamano, :FechaInicioVigencia, :IndArt104,
	:TasaFiscal, :TipoValorizacion, :ValorizaPor, :Precio, :Tasa, :TieneCupon, :NumeroCuotasCirculacionMesActual, :NumeroAccionesCirculacion, :IndicadorLiquidaDCV, :IdPaisEmisor,
    :FrecuenciaCupon, :Serie, :PrecioReferencia, :IndicadorBolson, :FechaReferencia, :NumeroCupones, :CodigoRubro, :PresenciaBursatilAjustada, :InstrumentoAcreditado, :ModalidadLiquidacion,
	:PrecioStrike, :LargoPlazo, :EsPutEsCall, :FactorExposicion, :OrigenInstrumento, :EstiloOpcion, :PactoAndino, :FormadorMercado, :PorcentajeCastigo, :FechaHastaAcreditado, :RelacionadoGrupo,
    :ModalidadEjercicio, :TipoRenta, :TipoBonoIRF,
    :IdMonedaEmision, :NumeroAmortizaciones, :IdOrdenSerie, :IdCarga, :Tera,
    :AfectoArt107, :EsAutorizado, :AfectoArt104, :CobroComisionesCustodia, :IdInstrumentoRaiz, :IdCondicionLiquidacion, :EsInstrumentoCalificado
);
