SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarPactosDetalleAnalitica";
 
CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarPactosDetalleAnalitica" AS
SELECT 	DET."IdReservaDetalle"
		,DET."IdConceptoDetalle"
		,DET."CarteraNominal"
		,DET."FolioCarteraDetalle"
		,DET."FolioCarteraCustodiaDetalle"
		,DET."IdEmisorDetalle"
		,DET."IdTipoReajusteDetalle"
		,DET."IdMonedaDetalle"
		,DET."IdMonedaEmisionInstrumentoDetalle"
		,DET."IdMonedaLiquidacionInstrumentoDetalle"
		,DET."CantidadDetalle"
		,DET."IdInstrumentoDetalle"
		,CASE DET."IdTipoInstrumentoDetalle" WHEN 'PAG' THEN SUB."NemoSVS" ELSE DET."NemotecnicoDetalle" END AS "NemotecnicoDetalle"
		,DET."IdClasificacionTipoInstrumentoDetalle"
		,DET."IdTipoInstrumentoDetalle"
		,DET."PrecioCompraDetalle"
		,DET."PrecioMercadoDetalle"
		,DET."TasaCompraDetalle"
		,DET."TasaMercadoDetalle"
		,DET."MontoCompraDetalle"
		,DET."MontoLiquidacionDetalle"
		,DET."EsFungibleDetalle"
		,DET."ValorRescateDetalle"
		,DET."IdOperacionDetalle"
		,DET."IdBolsaDetalle"
		,DET."IdBovedaDetalle"
		,DET."CodigoIFRSDetalle"
		,DET."CodigoDescIFRSDetalle"
		,DET."NombreBovedaDetalle"
		,DET."ClasificacionTipoInstrumentoDetalle"
		,DET."MontoValorizadoReserva"
		,SUB."NemoSVS"
		,RES."IdCarteraCustodia"
		,DET."MontoOperacionFinal"
	FROM "BSCL.Mesa.BD.Vistas::MRF.ConsultarDetalleOperacionesMesaRF" DET
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES on det."IdReservaDetalle" = res."IdReserva"
	--LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera"  CUS ON CUS."IdCartera" = res."IdCarteraCustodia"
	LEFT JOIN (
		select "FolioSubyacente","NemoSVS"
		from (
			select row_number() over (partition by "FolioSubyacente" order by "IdSubyacente" asc) as nn,"FolioSubyacente","NemoSVS" 
			FROM "BSCL.Custodia.BD::CUS.Subyacente" where "IdTipoInstrumento" = 'PAG'
		) WHERE nn = 1
	)	AS SUB ON RES."IdTipoInstrumento" = 'PAG'  AND SUB."FolioSubyacente" = RES."FolioSubyacente" 
	WHERE 	DET."IdTipoOperacion" IN ('VENPAC','COMPAC')
	AND 	DET."IdEstado" NOT IN ('ANU','PEN') ;


