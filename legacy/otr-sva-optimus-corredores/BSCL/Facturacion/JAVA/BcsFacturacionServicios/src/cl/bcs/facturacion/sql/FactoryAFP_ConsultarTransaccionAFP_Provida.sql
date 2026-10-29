 SELECT
	TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYYMMDD') AS "FechaTransaccion",
	"Transaccion"."Folio" as "FolioTransaccion",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN 
			CASE 
				WHEN "CorredoraLocalCodigos"."CodigoCorredoraFix" IS NULL THEN ''
				ELSE "CorredoraLocalCodigos"."CodigoCorredoraFix"
			END 
		ELSE CASE 
			WHEN "CorredoraContraparteCodigos"."CodigoCorredoraFix" IS NULL THEN ''
			ELSE "CorredoraContraparteCodigos"."CodigoCorredoraFix"
		END 
	END AS "CodigoCorredorVendedor",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN 
			CASE 
				WHEN "CorredoraLocal"."NombreCorto" IS NULL THEN ''
				ELSE SUBSTR("CorredoraLocal"."NombreCorto",0,10)
			END 
		ELSE CASE 
			WHEN "CorredoraContraparte"."NombreCorto" IS NULL THEN ''
			ELSE SUBSTR("CorredoraContraparte"."NombreCorto",0,10)
		END 
	END AS "NemotecnicoCorredorVendedor",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN 
			CASE 
				WHEN "CorredoraLocalCodigos"."CodigoCorredoraFix" IS NULL THEN ''
				ELSE "CorredoraLocalCodigos"."CodigoCorredoraFix"
			END 
		ELSE CASE 
			WHEN "CorredoraContraparteCodigos"."CodigoCorredoraFix" IS NULL THEN ''
			ELSE "CorredoraContraparteCodigos"."CodigoCorredoraFix"
		END 
	END AS "CodigoCorredorComprador",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN 
			CASE 
				WHEN "CorredoraLocal"."NombreCorto" IS NULL THEN ''
				ELSE SUBSTR("CorredoraLocal"."NombreCorto",0,10)
			END 
		ELSE CASE 
			WHEN "CorredoraContraparte"."NombreCorto" IS NULL THEN ''
			ELSE SUBSTR("CorredoraContraparte"."NombreCorto",0,10)
		END 
	END AS "NemotecnicoCorredorComprador",
	CAST(ROUND("Transaccion"."Cantidad",0) AS DECIMAL (20,0)) AS "CantidadTransada",
	CAST(ROUND("Transaccion"."Precio",4) AS DECIMAL (20,4)) AS "Precio",
	"Transaccion"."IdCondicionDeLiquidacion" AS "CondicionTransaccion",
	TO_VARCHAR("Transaccion"."HoraSistemaCreacion",'hh24miss') AS "HoraTransaccion",
	CASE 
		WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
		WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico"
	ELSE "Instrumento"."Nemotecnico" 
	END AS "NemotecnicoInstrumento",
	CASE 
		WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
		WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico"
	ELSE "Instrumento"."Nemotecnico" 
	END AS "NemotecnicoNoInscritos",
	CAST(ROUND("Transaccion"."Monto",2) AS DECIMAL (20,2)) AS "TotalTransado",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN 
			CASE 
				WHEN "Transaccion"."CodigoOperadorCorredor" < '100' THEN "Transaccion"."CodigoOperadorCorredor"
				ELSE ''
			END 
		ELSE CASE 
			WHEN "Transaccion"."CodigoOperadorContraparte" < '100' THEN "Transaccion"."CodigoOperadorContraparte"
			ELSE ''
		END 
	END AS "OperadorInternoVenta",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN 
			CASE 
				WHEN "Transaccion"."CodigoOperadorCorredor" < '100' THEN "Transaccion"."CodigoOperadorCorredor"
				ELSE ''
			END 
		ELSE CASE 
			WHEN "Transaccion"."CodigoOperadorContraparte" < '100' THEN "Transaccion"."CodigoOperadorContraparte"
			ELSE ''
		END 
	END AS "OperadorInternoCompra",	
	"Transaccion"."IndicadorDCV" AS "IndicadorDCV",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN "Transaccion"."CodigoOperadorCorredor"
		ELSE "Transaccion"."CodigoOperadorContraparte"
	END AS "CodigoOperadorVendedor",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN "Transaccion"."CodigoOperadorCorredor"
		ELSE "Transaccion"."CodigoOperadorContraparte"
	END AS "CodigoOperadorComprador",
	'' AS "Disponible",
	CASE
		WHEN  "Transaccion"."IndicadorOrdenDirecta" = 'OD' THEN 'S'
		ELSE 'N'	
	END AS "OrdenDirecta"
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
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS "CorredoraLocal" 
ON "CorredoraLocal"."IdCorredora" = "Transaccion"."IdCorredorLocal"
LEFT JOIN "BSCL.Plataforma.BD::INS.Corredora" AS "CorredoraContraparte" 
ON "CorredoraContraparte"."IdCorredora" = "Transaccion"."IdCorredorContraparte"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS "CorredoraLocalCodigos"
ON "CorredoraLocal"."IdCorredora" = "CorredoraLocalCodigos"."IdCorredora"
	AND "CorredoraLocalCodigos"."IdBolsa" = "Transaccion"."IdBolsa"
LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" AS "CorredoraContraparteCodigos"
ON "CorredoraContraparte"."IdCorredora" = "CorredoraContraparteCodigos"."IdCorredora"
	AND "CorredoraContraparteCodigos"."IdBolsa" = "Transaccion"."IdBolsa"
WHERE "AFP"."IdAFP" = :IdAFP
	AND TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYY-MM-DD') = :Fecha
GROUP BY "Transaccion"."FechaSistemaCreacion",
	"Transaccion"."Folio",
	"TipoOperacion"."AccionOperacion",
	"CorredoraLocalCodigos"."CodigoCorredoraFix",
	"CorredoraContraparteCodigos"."CodigoCorredoraFix",
	"CorredoraLocal"."NombreCorto",
	"CorredoraContraparte"."NombreCorto",
	"Transaccion"."Cantidad",
	"Transaccion"."Precio",
	"Transaccion"."IdCondicionDeLiquidacion",	
	"Transaccion"."HoraSistemaCreacion",
	"Instrumento"."Nemotecnico",
	"TipoInstrumento"."IdTipoInstrumento",
	"Instrumento_ASOC"."Nemotecnico",
	"TipoInstrumento"."IdClasificacionTipoInstrumento",	
	"Transaccion"."Monto",
	"Transaccion"."CodigoOperadorContraparte",
	"Transaccion"."CodigoOperadorCorredor",
	"Transaccion"."IndicadorDCV",
	"Transaccion"."IndicadorOrdenDirecta"
