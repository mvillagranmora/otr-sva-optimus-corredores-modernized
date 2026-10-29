SELECT 
	SUBSTR("CorredoraCodigos"."CodigoCorredoraFix",0,4) AS "CodigoCorredor",
	TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYYMMDD') AS "Fecha",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN 'C'
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN 'V'
		ELSE 'N' 
	END AS "CompraVenta",
	"Multifondos"."NombreMultifondo" AS "Fondo",
	CASE 
		WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
		WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico"
		ELSE "Instrumento"."Nemotecnico" 
	END AS "Nemotecnico",
	"Transaccion"."Folio" as "FolioBolsa",
	CASE
		WHEN  "Transaccion"."IndicadorOrdenDirecta" = 'OD' THEN 'S'
		ELSE 'N'	
	END AS "OrdenDirecta",
	CAST(ROUND("Transaccion"."Cantidad",0) AS DECIMAL (20,0)) AS "Nominales",
	CAST(ROUND("Transaccion"."Precio",2) AS DECIMAL (20,2)) AS "Precio",
	CAST(ROUND("Transaccion"."Monto",2) AS DECIMAL (20,2)) AS "Monto"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS "MovimientoComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS "Comprobante"
ON "Comprobante"."IdComprobante" = "MovimientoComprobante"."IdComprobante"
LEFT  JOIN "BSCL.Orden.BD::ORD.Transaccion" AS "Transaccion"
ON "Transaccion"."IdTransaccion" = "MovimientoComprobante"."IdTransaccion" 
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento_ASOC" 
ON "Instrumento_ASOC"."IdInstrumento" = "Transaccion"."IdInstrumentoSubyacente"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS "Instrumento" 
ON "Instrumento"."IdInstrumento" = "Transaccion"."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS "TipoInstrumento" 
ON "TipoInstrumento"."IdTipoInstrumento" = "Instrumento"."IdTipoInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS "TipoOperacion" 
ON "TipoOperacion"."IdTipoOperacion" = "Transaccion"."IdTipoOperacion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" AS "ComprobanteDetalle"
ON "Transaccion"."Folio" = "ComprobanteDetalle"."FolioTransaccion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionAFP" AS "AFP"
ON "AFP"."IdPortafolio" = "Comprobante"."IdPortafolio"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Multifondos" AS "Multifondos"
ON "AFP"."IdMultifondo" = "Multifondos"."IdMultifondo"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS "Corredora"
ON "Corredora"."IdCorredora" = "Transaccion"."IdCorredorLocal"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS "CorredoraCodigos"
ON "Corredora"."IdCorredora" = "CorredoraCodigos"."IdCorredora"
	AND "CorredoraCodigos"."IdBolsa" = "Transaccion"."IdBolsa"
WHERE "AFP"."IdAFP" = :IdAFP
	AND TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYY-MM-DD') = :Fecha
	AND "Transaccion"."IndicadorOrdenDirecta" != 'OD'
GROUP BY "CorredoraCodigos"."CodigoCorredoraFix",
	"Transaccion"."FechaSistemaCreacion",
	"TipoOperacion"."AccionOperacion",
	"Multifondos"."NombreMultifondo",
	"Instrumento"."Nemotecnico" ,
	"TipoInstrumento"."IdTipoInstrumento",
	"TipoInstrumento"."IdClasificacionTipoInstrumento",
	"Instrumento_ASOC"."Nemotecnico",
	"Transaccion"."Folio",
	"Transaccion"."IndicadorOrdenDirecta",
	"Transaccion"."Cantidad",
	"Transaccion"."Precio",
	"Transaccion"."Monto"
