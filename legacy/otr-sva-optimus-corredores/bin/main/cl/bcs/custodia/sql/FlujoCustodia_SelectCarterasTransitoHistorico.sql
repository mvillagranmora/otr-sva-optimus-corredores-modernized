/********************************************************** 
Proposito	: Recuperar la configuración del flujo de custodia
autor		: JPZS
fecha		: Nov-2018.
***********************************************************/

SELECT
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
--	:p_out_KeyFlujoCustodia AS "KeyFlujoCustodia",
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
--	I."ItemCalculo",
--	I."ItemOrigen",
--	:p_FechaSaldo AS "FechaEjecucion",
	CAR."IdClasificacionTipoInstrumento",
	CAR."IdTipoInstrumento",
	CAR."IdInstrumento",
	INS."TipoValorizacion",
	TOPER."Accion",
	TOPER."IdTipoProducto",
	CAR."IdEmisor",
	CAR."Nemotecnico",
	CAR."NemoSVS",
	CAR."CorteMinimo",
	CAR."IdMonedaInstrumento",
	CAR."IdMonedaEmision" AS "IdMonedaEmision",
	CAR."IdTipoReajuste",
	CAR."FechaEmisionInstrumento",
	CAR."FechaVencimientoInstrumento",
	CAR."CodigoIFRS",
	CAR."EsFungible",
	CAR."TipoCarteraCorredor",
	CAR."IdBoveda",
	CAR."IdBolsa",				
	CAR."IdTipoOperacion",
	CAR."IdMesa",
	CAR."INGFechaLiquidacion" AS "FechaLiquidacion",
	CAR."INGCantidad" AS "Cantidad",
	CAR."INGValorRescate" AS "ValorRescate",
	CAR."INGPrecioCompra" AS "PrecioCompra",
	CAR."INGTasaCompra" AS "TasaCompra",
	CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoATasaCompra",

	CAR."INGPrecioMercadoActual" AS "PrecioMercado",
	CAR."INGTasaMercadoActual" AS "TasaMercado",
	CAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoATasaMercado",

	CAR."IdCliente",
	CAR."IdPortafolio",				
--	(CASE WHEN CAR."FechaLiquidacion" >= :v_FechaMinima THEN CAR."FechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",				
	CAR."IdCartera" AS "IdRegistro",
	CAR."FolioCartera" AS "FolioRegistro",
	CAR."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion",
	CAR."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	CAR."FechaOrigenCompra",
	COALESCE(CompDet."IdCorredoraContraparte", 0) AS "IdCorredorContraparte"
	
	
FROM "BSCL.Custodia.BD::HIS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC
		ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
			AND CMC."PoseeInstrumentoSubyacente" = 'N'
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU
		ON (MZU."IdCliente" IS NULL OR CAR."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR CAR."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR CAR."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR CAR."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR CAR."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR CAR."IdEmisor" = MZU."IdEmisor")
			AND (MZU."FechaVencimiento" IS NULL OR CAR."FechaVencimientoInstrumento" = MZU."FechaVencimiento")
			AND (MZU."IdTipoReajuste" IS NULL OR CAR."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR CAR."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR CAR."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR CAR."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = CAR."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = MZU."UsoTipoOperacion"
			AND TOTI."IdTipoInstrumento" = CAR."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
				
	-- Vamos a la transaccion para obtener el corredor contraparte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" CompDet
		ON  CAR."IdInstrumento" = CompDet."IdInstrumento"
		AND CAR."FolioComprobante" = CompDet."IdComprobante"
		AND CAR."FolioExternoReferencia" = CompDet."FolioTransaccion"
			
WHERE CAR."IdEstado" = 'VIG'
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."INGFechaLiquidacion" > :FechaMaxima
	AND MZU."IdUsoFlujoCustodia" IS NOT NULL
	AND TOPER."Accion" IN ('COMPRA', 'VENTA')
	AND CAR."FechaRespaldo" = :FechaMaxima
	AND MZU."IdTrabajo" = :IdTrabajo;
	
	