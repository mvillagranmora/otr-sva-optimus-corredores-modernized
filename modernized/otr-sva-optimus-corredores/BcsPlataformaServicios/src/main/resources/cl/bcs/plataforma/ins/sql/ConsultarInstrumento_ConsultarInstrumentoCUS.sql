SELECT "IdInstrumento","Nemotecnico","ISIN","CorteMinimo","EstaInscrito","EsDivisible","RazonSocial","FechaEmision","FechaVencimiento","IdInstrumentoRelacionado"
	,"IdEmisor","DocumentoIdentidad","IdTipoReajuste","Reajustable","IdMoneda","IdMonedaEmision","IdTipoInstrumento","IdEstado","DecimalesPrecio","NombreTipoInstrumento"
	,"IdClasificacionTipoInstrumento", "NombreClasificacionTipoInstrumento","NombreCompletoEmisor","Tamano","TipoValorizacion","IndicadorBolson","Tasa"
	,"Precio","ValorizaPor","TieneCupon","Cantidad","PorcentajeCastigo","FrecuenciaCupon","PrecioStrike","InstrumentoAcreditado","CallConfirmado"
 	,"TasaEmision","MercadoLocal","ValorClasificacionRiesgo","EsPutEsCall","IdTipoInstrumentoOrigen"
FROM "BSCL.Plataforma.BD.Vistas::INS.UI.ConsultarInstrumento"
WHERE  
	"IdInstrumento" IN (:IdInstrumento)
	AND "IdEstado" = :IdEstado AND "IdTipoInstrumento" not in ('SIM','RRV','RRF','PAC','PRT');
