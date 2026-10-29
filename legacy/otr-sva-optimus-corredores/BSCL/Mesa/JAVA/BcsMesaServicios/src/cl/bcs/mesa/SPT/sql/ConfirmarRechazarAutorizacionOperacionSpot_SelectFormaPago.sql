SELECT
	ComprobantePago."IdOperacion" AS "NumeroOperacion"
	, FormaPagoMesa."IdTipoProducto"
	, CASE WHEN "RecibePaga" = 'P' THEN 'MOVEGR' WHEN "RecibePaga" = 'R' THEN 'MOVING' END AS "IdTipoMovimiento"
	, FormaPagoMesa."IdMedioDePago"
	, FormaPagoMesa."NombreMedioDePago"
	, ComprobantePago."IdModalidadPago"
	, ComprobantePago."IdMoneda"
	, ComprobantePago."Monto"
	, ComprobantePago."FechaIngresoComprobantePagoOperacion" AS "FechaIngresoRegistro"
	, ComprobantePago."FechaLiquidacionComprobantePagoOperacion" AS "FechaCondicionLiquidacion"
	, ComprobantePago."AfectarTesoreria"
FROM "BSCL.Mesa.BD.Vistas::ConsultarComprobantePagoOperacion" ComprobantePago
	INNER JOIN "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoMesa" FormaPagoMesa ON (
	    FormaPagoMesa."IdFormaDePago" = ComprobantePago."IdFormaDePago"
		AND FormaPagoMesa."IdMoneda" = ComprobantePago."IdMoneda"
		AND FormaPagoMesa."IdTipoProducto" = ComprobantePago."IdTipoProducto"
		AND FormaPagoMesa."IdEstado" = 'ACT'
	)
WHERE ComprobantePago."IdOperacion" IN (:IdOperacionSpot)