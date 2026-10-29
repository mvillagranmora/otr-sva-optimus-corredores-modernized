/********************************************************** 
Proposito	: Busca operaciones fecha limite (Fecha operacion <= fecha limite  Y  Fecha liquidacion > Fecha limite)
              Aca calzan Nominales, Venta Descubierta, Prestamos  y Carteras en fecha limite
autor		: - BCS
fecha		: 10/06/2019
***********************************************************/

SELECT 
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
	*
FROM 
(
-- Operaciones Fecha Limite (ventas normales)
SELECT
	CAR."IdClasificacionTipoInstrumento",
	CAR."IdTipoInstrumento",
	CAR."IdInstrumento",
	INS."TipoValorizacion",
	'CUSTODIA' AS "ItemOrigen",
	CASE 
		WHEN TOPER."Accion" = 'COMPRA' THEN 'CARTERA_TRANSITO_COMPRA'
		WHEN TOPER."Accion" = 'VENTA'  THEN 'CARTERA_TRANSITO_VENTA'
		ELSE  ''
	END  AS "ItemCalculo", 
	
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
	,COALESCE(MovCom."IdCorredoraContraparte", 0) AS "IdCorredorContraparte"
	
FROM "BSCL.Custodia.BD::CUS.Cartera" AS CAR
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" as CMC
		ON CMC."IdTipoOperacion" = CAR."IdTipoOperacion"
			AND CMC."PoseeInstrumentoSubyacente" = 'N'
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = CAR."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = CAR."EGRIdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = :UsoTipoOperacion
			AND TOTI."IdTipoInstrumento" = CAR."IdTipoInstrumento"
				
		-- Vamos a la transaccion para obtener el corredor contraparte
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCom
			ON  CAR."IdInstrumento" = MovCom."IdInstrumento"
			AND CAR."EGRFolioComprobante" = MovCom."IdComprobante"
			AND CAR."EGRFolioExternoReferencia" = MovCom."FolioTransaccion"

	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
WHERE CAR."IdEstado" IN ('RPA','LIQ')
    AND CAR."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
    AND CAR."IdTipoInstrumento" = :IdTipoInstrumento
    AND CAR."IdInstrumento" = :IdInstrumento
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."EGRFechaLiquidacion" > :FechaMaxima
	AND CAR."EGRFechaOperacion" <= :FechaMaxima

UNION ALL

-- Operaciones Fecha Limite (Nominales y Venta Descubierta)
SELECT
	TRA."IdClasificacionTipoInstrumento",
	TRA."IdTipoInstrumento",
	TRA."IdInstrumento",
	INS."TipoValorizacion",
	'CUSTODIA' AS "ItemOrigen",
	CASE 
		WHEN TOPER."Accion" = 'COMPRA' THEN 'INFORMACION_TRANSITO_INGRESO'
		WHEN TOPER."Accion" = 'VENTA'  THEN 'INFORMACION_TRANSITO_RETIRO'
		ELSE  ''
	END  AS "ItemCalculo",	
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
	CASE 
		WHEN TOPER."Accion" = 'COMPRA' THEN TRA."Cantidad"
		WHEN TOPER."Accion" = 'VENTA'  THEN -1 * TRA."Cantidad"  
		ELSE  TRA."Cantidad"
	END  AS "CantidadProceso",	
		
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
	COALESCE(MovCom."IdCorredoraContraparte", 0) AS "IdCorredorContraparte"

FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS TRA
	INNER JOIN "BSCL.Custodia.BD::CUS.ConfiguracionMovimientoCustodia" AS CMC 
		ON CMC."IdTipoOperacion" = TRA."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = TRA."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = TRA."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = :UsoTipoOperacion
			AND TOTI."IdTipoInstrumento" = TRA."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON TRA."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
				
	 --Vamos a la transaccion para obtener el corredor contraparte
	LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MovCom
		ON  TRA."IdInstrumento" = MovCom."IdInstrumento"
		AND TRA."FolioComprobante" = MovCom."IdComprobante"
		AND TRA."FolioExternoReferencia" = MovCom."FolioTransaccion"	
	
	
WHERE TRA."IdEstado" = 'PEN'
    AND TRA."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
    AND TRA."IdTipoInstrumento" = :IdTipoInstrumento
    AND TRA."IdInstrumento" = :IdInstrumento
	AND TRA."IdEmpresa" = :IdEmpresa
	AND TRA."FechaOperacion"   <= :FechaMaxima
	AND TRA."FechaLiquidacion" > :FechaMaxima
	
	
UNION ALL

-- Operaciones Fecha Limite (Prestamos y prestamistas)
SELECT
	SUB."IdClasificacionTipoInstrumento",
	SUB."IdTipoInstrumento",
	SUB."IdInstrumento",
	'' AS "TipoValorizacion",
	'CUSTODIA' AS "ItemOrigen", 
	CASE 
		WHEN CAR."IdTipoOperacion" = 'PRESTA' THEN 'CARTERA_TRANSITO_VENTA'  -- Prestamo   --> Se trata como una VENTA limite
		WHEN CAR."IdTipoOperacion" = 'EMPRES' THEN 'CARTERA_TRANSITO_COMPRA' -- Emprestito --> Se trata como una COMPRA limite
		ELSE  'PRE'
	END  AS "ItemCalculo",
	'' AS "Accion",
	CAR."IdTipoProducto",
	SUB."IdEmisor",
	SUB."Nemotecnico",
	SUB."NemoSVS",
	SUB."CorteMinimo",
	SUB."IdMonedaInstrumento",
	SUB."IdMonedaEmision",
	SUB."IdTipoReajuste",
	SUB."FechaEmisionInstrumento",
	SUB."FechaVencimientoInstrumento",
	CAR."CodigoIFRS",
	SUB."EsFungible",
	'' AS "TipoCarteraCorredor",
	CAR."IdBoveda",
	CAR."IdBolsa",
	CAR."IdTipoOperacion",
	CAR."IdMesa",
	CAR."FechaLiquidacion",
	SUB."INGCantidad" as "Cantidad",
	CASE 
		WHEN CAR."IdTipoOperacion" = 'EMPRES' THEN -1 * SUB."INGCantidad"
		ELSE  SUB."INGCantidad"
	END  AS "CantidadProceso",
	SUB."INGValorRescate" as "ValorRescate",
	SUB."INGPrecioSubyacente" as "PrecioCompra",
	SUB."INGTasaSubyacente" as "TasaCompra",
	SUB."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra",
	SUB."INGPrecioMercadoActual" as "PrecioMercado",
	SUB."INGTasaMercadoActual" as "TasaMercado",
	SUB."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
	CAR."IdCliente",
	CAR."IdPortafolio",
	SUB."IdSubyacente" as "IdRegistro",
	SUB."FolioSubyacente" as "FolioRegistro",
	CAR."IdConcepto",
	'S' AS "RequiereValorizacion",
	CAR."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	CAR."FechaOrigenCompra",
	Tran."IdCorredorContraparte"

FROM "BSCL.Custodia.BD::CUS.Subyacente" AS SUB
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR
		ON (CAR."FolioCartera" = SUB."FolioCarteraRelacionada")
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'

			LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
				ON CAR."IdTipoComprobante" = CompDet."IdTipoComprobante"
				AND CAR."FolioComprobante" = CompDet."IdComprobante"
				
			LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
				ON CompDet."IdTransaccion" = Tran."IdTransaccion"
				AND Tran."IdCorredorLocal" <> Tran."IdCorredorContraparte"
			
WHERE CAR."IdTipoOperacion" IN ('PRESTA')
	AND CAR."IdEstado"  		= 'VIG'
	AND SUB."IdEstado"  		= 'VIG'
	AND SUB."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
    AND SUB."IdTipoInstrumento" = :IdTipoInstrumento
    AND SUB."IdInstrumento" = :IdInstrumento
	AND CAR."IdEmpresa" 		= :IdEmpresa
	AND CAR."FechaOperacion"   <= :FechaMaxima
	AND CAR."FechaLiquidacion" > :FechaMaxima

);