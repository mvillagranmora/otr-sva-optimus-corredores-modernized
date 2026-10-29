/********************************************************** 
Proposito	: Busca cartera ventas transito
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

SELECT 
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
	*
FROM 
(
SELECT
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
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
	CAR."EGRFechaLiquidacion" AS "FechaLiquidacion",
	CAR."EGRCantidad" AS "Cantidad",
	CAR."EGRCantidad" AS "CantidadProceso",
	CAR."EGRValorRescate" AS "ValorRescate",
	CAR."INGPrecioCompra" AS "PrecioCompra",
	CAR."INGTasaCompra" AS "TasaCompra",
	CAR."INGMontoCompraActualMonedaInstrumento" AS "MontoATasaCompra",
	CAR."INGPrecioMercadoActual" AS "PrecioMercado",
	CAR."INGTasaMercadoActual" AS "TasaMercado",
	CAR."INGMontoMercadoActualMonedaInstrumento" AS "MontoATasaMercado",
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."IdCartera" AS "IdRegistro",
	CAR."FolioCartera" AS "FolioRegistro",
	CAR."IdConcepto",
	'S' AS "RequiereValorizacion",
	CAR."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	CAR."FechaOrigenCompra"
	,Tran."IdCorredorContraparte"
	
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
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

			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON CAR."EGRIdTipoComprobante" = CompDet."IdTipoComprobante"
				AND CAR."EGRFolioComprobante" = CompDet."IdComprobante"
			LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccion"
				AND Tran."IdCorredorLocal" <> Tran."IdCorredorContraparte"

	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
WHERE CAR."IdEstado" IN ('RPA','LIQ')
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."EGRFechaLiquidacion" > :FechaMaxima
	AND CAR."EGRFechaOperacion" <= :FechaMaxima
AND MZU."IdUsoFlujoCustodia" IS NOT NULL

UNION ALL


SELECT
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
	TRA."IdClasificacionTipoInstrumento",
	TRA."IdTipoInstrumento",
	TRA."IdInstrumento",
	INS."TipoValorizacion",
	TOPER."Accion",
	TRA."IdTipoProducto",
	TRA."IdEmisor",
	TRA."Nemotecnico",
	TRA."NemoSVS",
	TRA."CorteMinimo",
	TRA."IdMonedaInstrumento",
	'' AS "IdMonedaEmision",
	TRA."IdTipoReajuste",
	TRA."FechaEmisionInstrumento",
	TRA."FechaVencimientoInstrumento",
	TRA."CodigoIFRS",
	TRA."EsFungible",
	'' AS "TipoCarteraCorredor",
	TRA."IdBoveda",
	TRA."IdBolsa",
	TRA."IdTipoOperacion",
	TRA."IdMesa",
	TRA."FechaLiquidacion",
	TRA."Cantidad",
	TRA."Cantidad" AS "CantidadProceso",	
	TRA."ValorRescate",
	TRA."PrecioCompra" AS "PrecioCompra",
	TRA."TasaCompra" AS "TasaCompra",
	TRA."MontoCompraMonedaInstrumento" AS "MontoATasaCompra",
	0.0 AS "PrecioMercado",
	0.0 AS "TasaMercado",
	0.0 AS "MontoATasaMercado",
	TRA."IdCliente",
	TRA."IdPortafolio",
	TRA."IdMovimientoCarteraEnTransito" AS "IdRegistro",
	CAST(0 AS BIGINT) AS "FolioRegistro",
	TRA."IdConcepto",	
	'S' AS "RequiereValorizacion",
	TRA."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	TRA."FechaOrigenCompra",
	Tran."IdCorredorContraparte" AS "IdCorredorContraparte"

FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC 
		ON CMC."IdTipoOperacion" = TRA."IdTipoOperacion"
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" AS MZU
		ON (MZU."IdCliente" IS NULL OR TRA."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR TRA."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR TRA."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR TRA."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR TRA."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR TRA."IdEmisor" = MZU."IdEmisor")
			AND (MZU."FechaVencimiento" IS NULL OR TRA."FechaVencimientoInstrumento" = MZU."FechaVencimiento")
			AND (MZU."IdTipoReajuste" IS NULL OR TRA."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR TRA."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR TRA."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR TRA."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = TRA."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = TRA."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = MZU."UsoTipoOperacion"
			AND TOTI."IdTipoInstrumento" = TRA."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON TRA."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
			
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
	ON  TRA."IdTipoComprobante" = CompDet."IdTipoComprobante"
	AND TRA."FolioComprobante" = CompDet."IdComprobante"
				
LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
	ON CompDet."IdTransaccion" = Tran."IdTransaccion"
	
	
WHERE TRA."IdEstado" = 'PEN'
	AND TRA."IdEmpresa" = :IdEmpresa
	AND TRA."FechaOperacion"   <= :FechaMaxima
	AND TRA."FechaLiquidacion" > :FechaMaxima
		
);



