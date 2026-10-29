SELECT
	'D' AS "TipoRegistro" --Constante
	,'S80080' AS "CodigoAI" --Constante
	,COMPROBANTE."IdComprobante" AS "NumeroPoliza"
	,TO_CHAR(COMPROBANTE."FechaComprobante",'DDMMYYYY') AS "FechaOperacionPoliza"
	,TO_CHAR(COMPROBANTE."FechaLiquidacion",'DDMMYYYY') AS "FechaLiquidacion"
	,'' AS "ModalidadOperacion" --Pendiente Mapeo
	,CASE WHEN COALESCE(MOVIMIENTO."EsAPlazo",'N') = 'N' THEN 1 ELSE 2 END AS "FlagContadorPlazo"
	,COALESCE(HOMOLOGACION_MONEDA."ValorHomologacion",'') AS "Moneda"
	,'' AS "Mercado" --Pendiente Mapeo
	,TO_DECIMAL(COALESCE(MOVIMIENTO."Cantidad",0),26,8) AS "Cantidad"
	,TO_DECIMAL(COALESCE(MOVIMIENTO."Precio",0),26,8) AS "Precio"
	,TO_DECIMAL(ABS(COALESCE(MOVIMIENTO."Monto",0)),26,8) AS "MontoBruto"
	,TO_DECIMAL(0,26,8) AS "ComisionAgente" --Pendiente
	,TO_DECIMAL(0,26,8) AS "ComisionBolsa" --Pendiente
	,TO_DECIMAL(0,26,8) AS "FondoGarantia" --Pendiente
	,TO_DECIMAL(0,26,8) AS "ComisionCavali" --Pendiente
	,TO_DECIMAL(0,26,8) AS "FondoLiquidacion" --Pendiente
	,TO_DECIMAL(0,26,8) AS "Smv" --Pendiente
	,TO_DECIMAL(0,26,8) AS "Igv" --Pendiente
	,TO_DECIMAL(0,26,8) AS "Intereses" --Pendiente
	,CASE COMPROBANTE."IdEstadoComprobante" WHEN 'ANU' THEN 1
	 ELSE 0 END AS "EstadoPoliza"
	,TO_CHAR(COALESCE(MOVIMIENTO."FechaVencimiento",'0001-01-01'),'DDMMYYYY') AS "FechaVencimiento"
	,TO_DECIMAL(0,26,8) AS "BrokerExtranjero" --Pendiente
	,TO_DECIMAL(ABS(COALESCE(MOVIMIENTO."MontoMovimientoComprobante",0)),26,8) AS "MontoNeto"
	,'1' AS "TipoCodigoCliente"
	,COALESCE(DOCUMENTO_RUT."DocumentoIdentidad",'') AS "CodigoCliente"
	,COMPROBANTE."DocumentoIdentidad" AS "IdentificadorCliente"
	,CASE WHEN COALESCE(MOVIMIENTO."MontoMovimientoComprobante",0) < 0 THEN 'N' ELSE '' END AS "SignoMontoNeto"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COMPROBANTE
LEFT OUTER JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" MOVIMIENTO ON (MOVIMIENTO."IdComprobante" = COMPROBANTE."IdComprobante" AND MOVIMIENTO."IdEstadoFacturacion" IN ('FAC','ENV'))
LEFT OUTER JOIN "BSCL.Normativa.BD::InterfazHomologacion" HOMOLOGACION_MONEDA ON (HOMOLOGACION_MONEDA."IdCampo" = 'Moneda' AND HOMOLOGACION_MONEDA."Valor" = MOVIMIENTO."IdMoneda")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLIENTE ON (CLIENTE."IdCliente" = COMPROBANTE."IdCliente")
LEFT OUTER JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_RUT ON (CLIENTE."IdPersona" = DOCUMENTO_RUT."IdPersona" AND DOCUMENTO_RUT."IdTipoDocumentoIdentidad" = 'RUT')
WHERE (TO_CHAR(Comprobante."FechaComprobante",'YYYY-MM-DD') = TO_CHAR(:FechaDesde,'YYYY-MM-DD'))
AND (:EsTodosCliente = 'S' OR Comprobante."IdCliente" = :IdCliente)
AND Comprobante."IdEmpresa" = :IdEmpresa;