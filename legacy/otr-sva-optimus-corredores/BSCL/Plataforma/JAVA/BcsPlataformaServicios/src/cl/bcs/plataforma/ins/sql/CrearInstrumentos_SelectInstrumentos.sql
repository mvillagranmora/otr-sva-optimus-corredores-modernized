SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","RazonSocial","FechaEmision","FechaVencimiento","IdEmisor"
	,"IdTipoReajuste","IdMoneda","IdTipoInstrumento","IdEstado","Tamano","Precio","PrecioReferencia","FechaReferencia","FrecuenciaCupon"
	,"NumeroCupones","InstrumentoAcreditado","ValorizaPor","IdInstrumentoRelacionado","EsPutEsCall","PrecioStrike","TipoBonoIRF"
	,"OrigenInstrumento","TipoRenta","RelacionadoGrupo","FormadorMercado","PactoAndino","FactorExposicion","LargoPlazo","Tasa","AfectoArt107","AfectoArt104","CobroComisionesCustodia"
FROM "BSCL.Plataforma.BD::INS.Instrumento" inst 
WHERE inst."Nemotecnico" IN (:Nemotecnico);