INSERT INTO "BSCL.Plataforma.BD::INS.Instrumento" ("IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial","FechaEmision",
	"FechaVencimiento","IdEmisor","IdTipoReajuste","IdMoneda","IdTipoInstrumento","IdEstado","Tamano",
	"Precio","PrecioReferencia","FechaReferencia","FrecuenciaCupon","NumeroCupones","InstrumentoAcreditado","ValorizaPor","IdInstrumentoRelacionado","EsPutEsCall",
	"PrecioStrike","TipoBonoIRF", "OrigenInstrumento", "TipoRenta", "RelacionadoGrupo", "FormadorMercado", "PactoAndino", "FactorExposicion", "LargoPlazo", "Tasa", "FechaInicioVigencia", "TipoValorizacion",
	"IdPaisEmisor","Serie","ModalidadLiquidacion", "ModalidadEjercicio", "EstiloOpcion", "FechaHastaAcreditado", "AfectoArt107","AfectoArt104", "CobroComisionesCustodia", "EsInstrumentoCalificado")
VALUES (
	:IdInstrumento,:Nemotecnico,:ISIN,:CorteMinimo,:EstaInscrito,:RazonSocial,:FechaEmision,
	:FechaVencimiento,:IdEmisor,:IdTipoReajuste,:IdMoneda,:IdTipoInstrumento,:IdEstado,:Tamano,
	:Precio,:PrecioReferencia,:FechaReferencia,:FrecuenciaCupon,:NumeroCupones,:InstrumentoAcreditado,:ValorizaPor,:IdInstrumentoRelacionado,:EsPutEsCall,
	:PrecioStrike,:TipoBonoIRF, :OrigenInstrumento, :TipoRenta, :RelacionadoGrupo, :FormadorMercado, :PactoAndino, :FactorExposicion, :LargoPlazo, :Tasa, :FechaInicioVigencia, :TipoValorizacion,
	:IdPaisEmisor,:Serie,:ModalidadLiquidacion,:ModalidadEjercicio,:EstiloOpcion,:FechaHastaAcreditado, :AfectoArt107,:AfectoArt104, :CobroComisionesCustodia, :EsInstrumentoCalificado
);