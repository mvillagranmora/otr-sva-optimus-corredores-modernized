SELECT 	Car."IdCartera"						AS "IdRegistro",
		'CUS'								AS "TipoCustodia",
		CASE Tran."IndicadorOrdenDirecta"
			WHEN ''	THEN 'N'
			ELSE 'S'
		END									AS "IndicadorOD"
FROM "BSCL.Custodia.BD::CUS.Cartera" Car
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
		ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
		AND Car."FolioComprobante" = CompDet."IdComprobante"
	INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
		ON CompDet."IdTransaccion" = Tran."IdTransaccion"
	   AND Tran."IdCorredorLocal" <> Tran."IdCorredorContraparte"
WHERE   Car."IdCartera" IN (:IdRegistro)
	AND Car."IdEstado" = 'VIG'
	AND Car."IdTipoComprobante" = 'FAE'
	
UNION ALL

SELECT 	Car."IdCartera"							AS "IdRegistro",
		ConAsi."IdFormaCustodia"				AS "TipoCustodia",
		CASE Tran."IndicadorOrdenDirecta"
			WHEN ''	THEN 'N'
			ELSE 'S'
		END										AS "IndicadorOD"
FROM "BSCL.Custodia.BD::CUS.Cartera" Car
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
		ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
		AND Car."FolioComprobante" = CompDet."IdComprobante"
	INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
		ON CompDet."IdTransaccion" = Tran."IdTransaccionOd"
	   AND Tran."IdCorredorLocal" = Tran."IdCorredorContraparte"
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ConAsi
		ON Tran."IdTransaccion" = ConAsi."IdTransaccion"
WHERE   Car."IdCartera" IN (:IdRegistro)
	AND Car."IdEstado" = 'VIG'
	AND Car."IdTipoComprobante" = 'FAE'
	AND ConAsi."IdFormaCustodia" = 'NOM'
	
UNION ALL

SELECT 	Car."IdCartera"							AS "IdRegistro",
		ConAsi."IdFormaCustodia"				AS "TipoCustodia",
		CASE Tran."IndicadorOrdenDirecta"
			WHEN ''	THEN 'N'
			ELSE 'S'
		END										AS "IndicadorOD"
FROM "BSCL.Custodia.BD::CUS.Cartera" Car
	INNER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CompDet
		ON Car."IdTipoComprobante" = CompDet."IdTipoComprobante"
		AND Car."FolioComprobante" = CompDet."IdComprobante"
	INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" Tran
		ON CompDet."IdTransaccion" = Tran."IdTransaccionOd"
	   AND Tran."IdCorredorLocal" = Tran."IdCorredorContraparte"
	LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" ConAsi
		ON Tran."IdTransaccion" = ConAsi."IdTransaccion"
WHERE   Car."IdCartera" IN (:IdRegistro)
	AND Car."IdEstado" = 'VIG'
	AND Car."IdTipoComprobante" = 'FAE'
	AND ConAsi."IdFormaCustodia" = 'CUS'