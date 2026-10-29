SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado"
	,"IdEmisor","DocumentoIdentidad","IdTipoReajuste","Reajustable","IdMoneda","IdMonedaEmision","IdTipoInstrumento","IdEstado","DecimalesPrecio","NombreTipoInstrumento"
	,"IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento","NombreCompletoEmisor","Tamano","TipoValorizacion","IndicadorBolson","Tasa"
	,"Precio","ValorizaPor","TieneCupon","Cantidad","PorcentajeCastigo","FrecuenciaCupon","PrecioStrike","InstrumentoAcreditado","CallConfirmado"
 	,"TasaEmision","MercadoLocal","ValorClasificacionRiesgo","EsPutEsCall","IdTipoInstrumentoOrigen"
FROM "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento"
WHERE ("Nemotecnico" IN (:Nemotecnico) OR 1 = :FiltroNemotecnico )
	--AND "IdInstrumento" IN (COALESCE (:IdInstrumento,"IdInstrumento"))
	AND ("IdClasificacionTipoInstrumento" IN (:IdClasificacionTipoInstrumento)  OR 1 = :FiltroIdClasificacionTipoInstrumento )
	AND ("IdTipoInstrumento" IN (:IdTipoInstrumento) OR 1 = :FiltroIdTipoInstrumento)
	AND "IdEstado" = :IdEstado;
