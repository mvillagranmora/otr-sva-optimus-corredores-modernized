SELECT 
	TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYYMMDD') AS "FechaTransaccion",
	CASE
  		WHEN LENGTH("Transaccion"."Folio") >= 6 THEN "Transaccion"."Folio"
  		ELSE LPAD("Transaccion"."Folio", 6, '0')
	END AS "FolioTransaccion",
	"CorredoraLocalCodigos"."CodigoCorredoraFix" AS "CodigoCorredor",	
	CASE 
		WHEN "AFP"."IdAFP" = 1 THEN '341'
		WHEN "AFP"."IdAFP" = 2 THEN '342'
		WHEN "AFP"."IdAFP" = 3 THEN '343'
		WHEN "AFP"."IdAFP" = 4 THEN '344'
		WHEN "AFP"."IdAFP" = 5 THEN '345'
		WHEN "AFP"."IdAFP" = 6 THEN '346'
		ELSE '347'
	END AS "Fondo",
	CASE 
		WHEN "TipoOperacion"."AccionOperacion" = 'COMPRA' THEN 'C'
		WHEN "TipoOperacion"."AccionOperacion" = 'VENTA' THEN 'V'
		ELSE 'N' 
	END AS "CompraVenta",
	CAST(ROUND("Transaccion"."Cantidad",0) AS DECIMAL (20,0)) AS "Cantidad",
	CAST(ROUND("Transaccion"."Precio",4) AS DECIMAL (20,4)) AS "Precio",
	TO_CHAR(UTCTOLOCAL ("Transaccion"."FechaOperacion", 'America/Santiago','platform'), 'hh24miss') AS "HoraTransaccion",
	CASE 
		WHEN "TipoInstrumento"."IdTipoInstrumento" = 'SIM' THEN "Instrumento_ASOC"."Nemotecnico"
		WHEN "TipoInstrumento"."IdClasificacionTipoInstrumento" = 'RE' THEN "Instrumento_ASOC"."Nemotecnico"
		ELSE "Instrumento"."Nemotecnico" 
	END AS "Nemotecnico",	
	CAST(ROUND("Transaccion"."Monto",2) AS DECIMAL (20,2)) AS "TotalTransado",
	'D' AS "IndicadorDCV",
	CASE WHEN "Transaccion"."IndicadorOrdenDirecta" IN ('OE','OD') THEN 'OC' ELSE ' ' END AS "IndicadorOrdenDirecta",
	CASE
		WHEN "Transaccion"."IdBolsa" = 'BCS' THEN '1'
		WHEN "Transaccion"."IdBolsa" = 'BEC' THEN '2'
		ELSE '3'
	END AS "Bolsa",
	CASE WHEN "CondLiquidacion"."Codigo" = 'CN' THEN 'PN' ELSE SUBSTR("CondLiquidacion"."Codigo",0,2) END  AS "CondicionPago",
	'C' AS "IdentificadorOperador",
	CASE
		WHEN  "Transaccion"."IndicadorOrdenDirecta" = 'OD' THEN 'S'
		ELSE 'N'	
	END AS "OrdenDirecta"
FROM "BSCL.Facturacion.BD::FAC.MovimientoComprobante" AS "MovimientoComprobante"
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" AS "Comprobante"
ON "Comprobante"."IdComprobante" = "MovimientoComprobante"."IdComprobante" 
	AND "Comprobante"."IdClasificacionTipoInstrumento" = 'RV'
	AND "Comprobante"."IdPortafolio" = "MovimientoComprobante"."IdPortafolio"
	AND "MovimientoComprobante"."IdEstadoFacturacion" NOT IN ('ANU')
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
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS "CondLiquidacion"
ON "Transaccion"."IdCondicionDeLiquidacion" = "CondLiquidacion"."IdCondicionDeLiquidacion"
LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS "Orden" ON "Orden"."IdOrden" = "MovimientoComprobante"."IdOrden" 
	AND "Orden"."IdClasificacionTipoInstrumento" = 'RV' 
	AND "Orden"."IdEstadoOrden" NOT IN('ANU') 
	AND "Orden"."IdCliente" = "Comprobante"."IdCliente"
LEFT JOIN BSCL."BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoDetalle" AS "EmpleadoDigitador" 
ON "EmpleadoDigitador"."IdEmpleado" = "Orden"."IdEmpleadoDigitador"
LEFT JOIN BSCL."BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuarioCompleto" AS "Usuario" 
ON "Usuario"."IdPersona" = "EmpleadoDigitador"."IdPersona" 
LEFT JOIN "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" AS "ConfiguracionFacturacion"
	ON "ConfiguracionFacturacion"."CodigoParametro" = 'USUARIOEXCLUIDOINTERFAZAFP'
WHERE "AFP"."IdAFP" = :IdAFP
AND COALESCE("Usuario"."CodigoUsuario", "EmpleadoDigitador"."CodigoEmpleado") <> "ConfiguracionFacturacion"."Valor"
AND TO_VARCHAR("Transaccion"."FechaSistemaCreacion",'YYYY-MM-DD') = :Fecha
AND "MovimientoComprobante"."IdClasificacionTipoInstrumento" = 'RV'
GROUP BY "Transaccion"."FechaSistemaCreacion",
	"Transaccion"."Folio",
	"CorredoraLocalCodigos"."CodigoCorredoraFix",
	"AFP"."IdAFP",
	"TipoOperacion"."AccionOperacion",
	"Transaccion"."Cantidad",
	"Transaccion"."Precio",
	"Transaccion"."FechaOperacion",
	"TipoInstrumento"."IdTipoInstrumento",
	"TipoInstrumento"."IdClasificacionTipoInstrumento",
	"Instrumento_ASOC"."Nemotecnico",
	"Instrumento"."Nemotecnico",
	"Transaccion"."Monto",
	"Transaccion"."IndicadorDCV",
	"Transaccion"."IndicadorOrdenDirecta",
	"Transaccion"."IdBolsa",
	"CondLiquidacion"."Codigo";