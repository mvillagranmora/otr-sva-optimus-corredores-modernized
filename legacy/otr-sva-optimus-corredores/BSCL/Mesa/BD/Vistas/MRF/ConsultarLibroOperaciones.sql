SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones";

CREATE VIEW "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones" AS
(((SELECT
		OPER."FechaCreacion",
		OPER."FechaOperacion" 					AS "FechaMovimiento",
		OPER."FechaLiquidacion",
		OPER."FolioOperacion" 					AS "FolioTransaccion",
		OPER."FolioOperacion" 					AS "FolioComprobante",
		OPER."IdOperacion"						AS "IdOperacion",
		OPER."IdTipoOperacion" 					AS "IdTipoOperacion",
		0 AS "IdOperadorContraparte", 
		0 AS "IdOperadorCorredor",
		OPER."IdCliente",
		IDENT."DocumentoIdentidad",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		OPER."IdPortafolio",
		TIPOOPERACION."Accion" 					AS "AccionOperacion",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN RES."MontoValorizado"  ELSE 0 END AS "MontoCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN RES."MontoValorizado"  ELSE 0 END AS "MontoVenta",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN RES."Tasa"  ELSE 0 END AS "TasaCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN RES."Tasa"  ELSE 0 END AS "TasaVenta",
		RES."IdEmisor" AS "IdEmisor",
		CASE WHEN OPER."IdInstrumento" = 0 AND OPER."AfectaCustodia"='N' THEN RES."Nemotecnico"
		    ELSE CASE WHEN RES."IdClasificacionTipoInstrumento" = 'IF' THEN CAR."NemoSVS" ELSE CAR."Nemotecnico" END
		END AS "Nemotecnico",
		RES."MontoValorizado",
		RES."MontoValorizado" AS "ValorPresenteCompraInst",
		INS."FechaVencimiento" AS "FechaVencimiento",
		CASE COALESCE(RES."IdClasificacionTipoInstrumento",'') WHEN 'IF'  THEN RES."IngValorRescate" ELSE RES."IngCantidad" END  				AS "Cantidad",
		RES."Precio" as "Precio",
		RES."Tasa" 											as "Tasa",
		RES."IdTipoReajuste" AS "IdMoneda",
		OPER."IdCondicionLiquidacion",
		OPER."FechaVencimiento" AS "FechaVencimientoPacto",
		0 AS "PrecioPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."Tasa" 
		ELSE 0 END AS "TasaPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."IdTipoReajuste"
		ELSE '' END AS "IdMonedaPlazo",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN OPER."MontoOperacionFinalMonedaAnticipo" 
		ELSE 0 END AS "TotalCompraVencimiento",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN OPER."MontoOperacionFinalMonedaAnticipo" 
		ELSE 0 END AS  "TotalVentaVencimiento", 
		'CUS' as  "Custodia",
		RES."IdBolsa" AS "IdBolsa",
		RES."IdBoveda" AS "IdBoveda",
		0 AS "CantidadGarantia",
		CASE WHEN OPER."IdInstrumento" = 0 AND  OPER."AfectaCustodia"='N' THEN RES."Nemotecnico"
		    ELSE CAR."NemoSVS"
		END AS "NemoSVS",
		RES."MontoValorizado" AS "ValorPresente",
		CASE TIPOOPERACION."Accion" 		 WHEN 'COMPRA'  THEN (RES."ValorRescateInicial" - RES."MontoValorizado") 
											 WHEN 'VENTA'   THEN (RES."MontoValorizado" - RES."ValorRescateInicial") 
											 ELSE 0 END AS "UtilidadPerdida",
		'' AS "FechaOrigenPacto",
		COALESCE(TIPOOPERACION."Nombre",'') AS "DescripcionOperacion",
		''  AS "IdEstadoFactura",
		OPER."IdTipoCanalIngreso" AS "Canal",
		0 AS "ValorIVA",
		0 AS "FolioComprobanteAsociado",
		RES."IdInstrumento",
		OPER."IdTipoProducto",
		COALESCE(RES."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
		COALESCE(RES."IdTipoInstrumento",'') AS "IdTipoInstrumento",
		'' AS "TipoOperacionSecundaria",
		RES."EsFungible" AS "EsFungible",
		RES."ValorRescateInicial" AS "ValorRescateInicial",
		OPER."IdEstado",
		OPER."MontoOperacionFinalAnticipo",
		OPER."IdOperacionOrigenReasignacion",
		--campos nuevos
		OPER."IdEstado" AS "IdEstadoOperacion",
	    RES."ValorRescateInicial" AS "ValorRescateInicialReserva",
	    RES."IdEmisor" AS "IdEmisorReserva",
	    RES."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoReserva",
        RES."CantidadInicial" AS "CantidadInicialReserva",
        RES."MontoLiquidacionOperacion" AS "MontoLiquidacionOperacionReserva",
        RES."EgrMontoValorizado" AS "EgrMontoValorizadoReserva",
        RES."IngMontoValorizado" AS "IngMontoValorizadoReserva",
        RES."Tasa" AS "TasaReserva",
        RES."INGMontoAPlazo" AS "INGMontoAPlazoReserva",
        OPER."IdOperacionRelacionada",
        RES."IdCarteraOrigen"  
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OPER."IdOperacion" = RES."IdOperacion"
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON RES."IdCarteraOrigen" = CAR."IdCartera"
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON RES."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE OPER."IdTipoProducto" IN ('MERF', 'IF')
	AND OPER."IdTipoOperacion" NOT IN ('COMPAC', 'VENPAC')
	AND OPER."IdEstado" != 'PEN') 
UNION ALL	
	(-- Detalles Pactos Compras y Ventas Nominal / custodia
	SELECT
		OPER."FechaCreacion",
		OPER."FechaOperacion" 					AS "FechaMovimiento",
		OPER."FechaLiquidacion",
		OPER."FolioOperacion" 					AS "FolioTransaccion",
		OPER."FolioOperacion" 					AS "FolioComprobante",
		OPER."IdOperacion" 						AS "IdOperacion",
		OPER."IdTipoOperacion" 					AS "IdTipoOperacion",
		0 AS "IdOperadorContraparte",
		0 AS "IdOperadorCorredor",
		OPER."IdCliente",
		IDENT."DocumentoIdentidad",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		OPER."IdPortafolio",
		TIPOOPERACION."Accion" 					AS "AccionOperacion",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN RES."MontoValorizado"  ELSE 0 END AS "MontoCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN RES."MontoValorizado"  ELSE 0 END AS "MontoVenta",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN RES."Tasa" ELSE 0 END AS "TasaCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN RES."Tasa" ELSE 0 END AS "TasaVenta",	
		RES."IdEmisor" AS "IdEmisor",
		CASE WHEN OPER."IdInstrumento" = 0 AND OPER."AfectaCustodia"='N' THEN INS."Nemotecnico"
		    ELSE RES."Nemotecnico"
		END AS "Nemotecnico",
		OPER."MontoOperacionMoneda" AS "MontoValorizado",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN RES."MontoLiquidacion"
			WHEN 'VIG' THEN RES."MontoValorizado"
			ELSE SUB."INGMontoCompraActualMonedaInstrumento"  END AS "ValorPresenteCompraInst",
		INS."FechaVencimiento" AS "FechaVencimiento",
		1 AS "Cantidad",
		0 As "Precio",
		OPER."Tasa" as "Tasa",
		OPER."IdTipoReajuste" AS "IdMoneda",
		OPER."IdCondicionLiquidacion",
		OPER."FechaVencimiento" AS "FechaVencimientoPacto",
		0 AS "PrecioPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."Tasa" 
		ELSE 0 END AS "TasaPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."IdTipoReajuste"
		ELSE '' END AS "IdMonedaPlazo",
		CASE WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'VIG' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" NOT IN ('ANT','LIQ','VIG') THEN OPER."MontoOperacionFinalMoneda"
			 ELSE 0 END AS "TotalCompraVencimiento",
		CASE WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'VIG' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" NOT IN ('ANT','LIQ','VIG') THEN OPER."MontoOperacionFinalMoneda"
		ELSE 0 END AS  "TotalVentaVencimiento", 
		'CUS' as  "Custodia",
		'' AS "IdBolsa",
		RES."IdBoveda" AS "IdBoveda",
		0 AS "CantidadGarantia",
		CASE WHEN OPER."IdInstrumento" = 0 AND OPER."AfectaCustodia"='N' THEN INS."Nemotecnico"
		    ELSE  SUB."NemoSVS" --RES."Nemotecnico"
		END AS "NemoSVS",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN RES."MontoLiquidacion"
			WHEN 'VIG' THEN RES."MontoValorizado"
			ELSE SUB."INGMontoATasaCompraActual" END AS "ValorPresente",
		0 AS "UtilidadPerdida",
		'' AS "FechaOrigenPacto",
		COALESCE(TIPOOPERACION."Nombre",'') AS "DescripcionOperacion",
		''  AS "IdEstadoFactura",
		'' AS "Canal",
		0 AS "ValorIVA",
		0 AS "FolioComprobanteAsociado",
		OPER."IdInstrumento",
		OPER."IdTipoProducto",
		COALESCE(RES."IdClasificacionTipoInstrumento",'')  AS "IdClasificacionTipoInstrumento",
		RES."IdTipoInstrumento" AS "IdTipoInstrumento",
		CASE OPER."IdTipoOperacion"
			WHEN 'COMPAC' THEN 'RETVEN'
			WHEN 'VENPAC' THEN 'RETCOM'
		END AS "TipoOperacionSecundaria",
		'N' AS "EsFungible",
		OPER."MontoOperacionInicialMoneda" AS "ValorRescateInicial",
		OPER."IdEstado",
		OPER."MontoOperacionFinalMonedaAnticipo",
		OPER."IdOperacionOrigenReasignacion",
		--campos nuevos
		OPER."IdEstado" AS "IdEstadoOperacion",
	    RES."ValorRescateInicial" AS "ValorRescateInicialReserva",
	    RES."IdEmisor" AS "IdEmisorReserva",
	    RES."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumentoReserva",
        RES."CantidadInicial" AS "CantidadInicialReserva",
        RES."MontoLiquidacionOperacion" AS "MontoLiquidacionOperacionReserva",
        RES."EgrMontoValorizado" AS "EgrMontoValorizadoReserva",
        RES."IngMontoValorizado" AS "IngMontoValorizadoReserva",
        RES."Tasa" AS "TasaReserva",
        RES."INGMontoAPlazo" AS "INGMontoAPlazoReserva",
        OPER."IdOperacionRelacionada",
        RES."IdCarteraOrigen" 
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
	INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OPER."IdOperacion" = RES."IdOperacion"
	LEFT  JOIN "BSCL.Custodia.BD::CUS.Subyacente"  AS SUB ON SUB."FolioSubyacente"=RES."FolioSubyacente"	
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
	LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON OPER."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
		ON TIPOOPERACION."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE OPER."IdTipoProducto" IN ('MERF', 'IF')
	AND OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
	AND OPER."IdEstado" !='PEN')
UNION ALL
(SELECT
		OPER."FechaCreacion",
		OPER."FechaOperacion" 					AS "FechaMovimiento",
		OPER."FechaLiquidacion",
		OPER."FolioOperacion" 					AS "FolioTransaccion",
		OPER."FolioOperacion" 					AS "FolioComprobante",
		OPER."IdOperacion" 						AS "IdOperacion",
		OPER."IdTipoOperacion" 					AS "IdTipoOperacion",
		0 AS "IdOperadorContraparte",
		0 AS "IdOperadorCorredor",
		OPER."IdCliente",
		IDENT."DocumentoIdentidad",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		OPER."IdPortafolio",
		TIPOOPERACION."Accion" 					AS "AccionOperacion",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoVenta",
		CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN OPER."Tasa" ELSE 0 END AS "TasaCompra",
		CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN OPER."Tasa" ELSE 0 END AS "TasaVenta",	
		INS."IdEmisor" AS "IdEmisor",
		INS."Nemotecnico",
		OPER."MontoOperacionMoneda" AS "MontoValorizado",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			WHEN 'VIG' THEN 0
			ELSE 0 END AS "ValorPresenteCompraInst",
		INS."FechaVencimiento" AS "FechaVencimiento",
		1 AS "Cantidad",
		0 As "Precio",
		OPER."Tasa" as "Tasa",
		OPER."IdTipoReajuste" AS "IdMoneda",
		OPER."IdCondicionLiquidacion",
		OPER."FechaVencimiento" AS "FechaVencimientoPacto",
		0 AS "PrecioPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."Tasa" 
		ELSE 0 END AS "TasaPlazo",
		CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') THEN OPER."IdTipoReajuste"
		ELSE '' END AS "IdMonedaPlazo",
		--------------TotalCompraVencimiento
		CASE WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" = 'VIG' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'COMPRA' AND OPER."IdEstado" NOT IN ('ANT','LIQ','VIG') THEN OPER."MontoOperacionFinalMoneda"
			 ELSE 0 END AS "TotalCompraVencimiento",
		CASE WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" = 'VIG' THEN OPER."MontoOperacionFinalMoneda"
			 WHEN TIPOOPERACION."Accion" = 'VENTA' AND OPER."IdEstado" NOT IN ('ANT','LIQ','VIG') THEN OPER."MontoOperacionFinalMoneda"
		ELSE 0 END AS  "TotalVentaVencimiento", 
		'CUS' as  "Custodia",
		'' AS "IdBolsa",
		'' AS "IdBoveda",
		0 AS "CantidadGarantia",
		INS."Nemotecnico" AS "NemoSVS",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			WHEN 'VIG' THEN 0
			ELSE 0 END AS "ValorPresente",
		0 AS "UtilidadPerdida",
		'' AS "FechaOrigenPacto",
		COALESCE(TIPOOPERACION."Nombre",'') AS "DescripcionOperacion",
		''  AS "IdEstadoFactura",
		'' AS "Canal",
		0 AS "ValorIVA",
		0 AS "FolioComprobanteAsociado",
		OPER."IdInstrumento",
		OPER."IdTipoProducto",
		'IF' AS "IdClasificacionTipoInstrumento",
		INS."IdTipoInstrumento" AS "IdTipoInstrumento",
		CASE OPER."IdTipoOperacion"
			WHEN 'COMPAC' THEN 'RETVEN'
			WHEN 'VENPAC' THEN 'RETCOM'
		END AS "TipoOperacionSecundaria",
		'N' AS "EsFungible",
		OPER."MontoOperacionInicialMoneda" AS "ValorRescateInicial",
		OPER."IdEstado",
		OPER."MontoOperacionFinalMonedaAnticipo",
		OPER."IdOperacionOrigenReasignacion",
	    --campos nuevos
		OPER."IdEstado" AS "IdEstadoOperacion",
	    0 AS "ValorRescateInicialReserva",
	    '' AS "IdEmisorReserva",
	    '' AS "IdClasificacionTipoInstrumentoReserva",
        0 AS "CantidadInicialReserva",
        0 AS "MontoLiquidacionOperacionReserva",
        0 AS "EgrMontoValorizadoReserva",
        0 AS "IngMontoValorizadoReserva",
        0 AS "TasaReserva",
        0 AS "INGMontoAPlazoReserva",
        OPER."IdOperacionRelacionada",
        0 AS "IdCarteraOrigen"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
	LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON OPER."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
		ON TIPOOPERACION."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE OPER."IdTipoProducto" IN ('MERF', 'IF')
	AND OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC'))) 
UNION ALL
(SELECT
		OPER."FechaCreacion",
		OPER."FechaLiquidacion" 				AS "FechaMovimiento",
		OPER."FechaLiquidacion",
		OPER."FolioOperacion" 					AS "FolioTransaccion",
		OPER."FolioOperacion" 					AS "FolioComprobante",
		OPER."IdOperacion" 						AS "IdOperacion",
		CAR."EGRIdTipoOperacion" 				AS "IdTipoOperacion",
		0 AS "IdOperadorContraparte",
		0 AS "IdOperadorCorredor",
		OPER."IdCliente",
		IDENT."DocumentoIdentidad",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		OPER."IdPortafolio",
		TIPOOPERACION_OPE."Accion" 					AS "AccionOperacion",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'COMPRA' THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoCompra",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'VENTA'  THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoVenta",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'COMPRA' THEN OPER."Tasa" ELSE 0 END AS "TasaCompra",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'VENTA'  THEN OPER."Tasa" ELSE 0 END AS "TasaVenta",	
		INS."IdEmisor" AS "IdEmisor",
		INS."Nemotecnico",
		OPER."MontoOperacionMoneda" AS "MontoValorizado",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			WHEN 'VIG' THEN CAR."INGMontoCompraActualMonedaInstrumento"
			ELSE CAR."INGMontoCompraActualMonedaInstrumento" END AS "ValorPresenteCompraInst",
		INS."FechaVencimiento" AS "FechaVencimiento",
		1 AS "Cantidad",
		0 As "Precio",
		OPER."Tasa" AS "Tasa",
		OPER."IdTipoReajuste" AS "IdMoneda",
		OPER."IdCondicionLiquidacion",
		OPER."FechaVencimiento" AS "FechaVencimientoPacto",
		0 AS "PrecioPlazo",
		OPER."Tasa"	 AS "TasaPlazo",
		OPER."IdTipoReajuste" AS "IdMonedaPlazo",
		CASE WHEN TIPOOPERACION_OPE."Accion" = 'COMPRA' THEN OPER."MontoOperacionFinalMonedaAnticipo"
			 ELSE 0 END AS "TotalCompraVencimiento",
		CASE WHEN TIPOOPERACION_OPE."Accion" = 'VENTA' THEN OPER."MontoOperacionFinalMonedaAnticipo"
		ELSE 0 END AS  "TotalVentaVencimiento", 
		'CUS' as  "Custodia",
		'' AS "IdBolsa",
		'' AS "IdBoveda",
		0 AS "CantidadGarantia",
		INS."Nemotecnico" AS "NemoSVS",
		OPER."MontoOperacionMonedaAnticipo" AS "ValorPresente",
		0 AS "UtilidadPerdida",
		'' AS "FechaOrigenPacto",
		COALESCE(TIPOOPERACION_CAR."Nombre",'') AS "DescripcionOperacion",
		''  AS "IdEstadoFactura",
		'' AS "Canal",
		0 AS "ValorIVA",
		0 AS "FolioComprobanteAsociado",
		OPER."IdInstrumento",
		CAR."IdTipoProducto",
		'IF' AS "IdClasificacionTipoInstrumento",
		CAR."IdTipoInstrumento" AS "IdTipoInstrumento",
		CASE OPER."IdTipoOperacion"
			WHEN 'COMPAC' THEN 'RETVEN'
			WHEN 'VENPAC' THEN 'RETCOM'
		END AS "TipoOperacionSecundaria",
		'N' AS "EsFungible",
		OPER."MontoOperacionInicialMoneda" AS "ValorRescateInicial",
		OPER."IdEstado",
		OPER."MontoOperacionFinalMonedaAnticipo",
		OPER."IdOperacionOrigenReasignacion",
		--campos nuevos
		OPER."IdEstado" AS "IdEstadoOperacion",
	    0 AS "ValorRescateInicialReserva",
	    '' AS "IdEmisorReserva",
	    '' AS "IdClasificacionTipoInstrumentoReserva",
        0 AS "CantidadInicialReserva",
        0 AS "MontoLiquidacionOperacionReserva",
        0 AS "EgrMontoValorizadoReserva",
        0 AS "IngMontoValorizadoReserva",
        0 AS "TasaReserva",
        0 AS "INGMontoAPlazoReserva",
        OPER."IdOperacionRelacionada",
        0 AS "IdCarteraOrigen"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" AS CAR ON OPER."FolioCartera" = CAR."FolioCartera"
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
	LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON OPER."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION_OPE ON TIPOOPERACION_OPE."IdTipoOperacion" = OPER."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION_CAR ON TIPOOPERACION_CAR."IdTipoOperacion" = CAR."EGRIdTipoOperacion"
	WHERE OPER."IdTipoProducto" IN ('MERF', 'IF')
	AND OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') 
	AND  OPER."IdEstado" IN ( 'ANT','LIQ'))
UNION ALL
    (SELECT
		OPER."FechaCreacion",
		OPER."FechaLiquidacion" 				AS "FechaMovimiento",
		OPER."FechaLiquidacion",
		OPER."FolioOperacion" 					AS "FolioTransaccion",
		OPER."FolioOperacion" 					AS "FolioComprobante",
		OPER."IdOperacion" 						AS "IdOperacion",
		'' 				                        AS "IdTipoOperacion",
		0 AS "IdOperadorContraparte",
		0 AS "IdOperadorCorredor",
		OPER."IdCliente",
		IDENT."DocumentoIdentidad",
		CASE PER."Tipo" 
			WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
			ELSE PER."RazonSocial"
		END  as "NombreCompleto",
		OPER."IdPortafolio",
		TIPOOPERACION_OPE."Accion" 					AS "AccionOperacion",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'COMPRA' THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoCompra",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'VENTA'  THEN OPER."MontoOperacionMoneda"  ELSE 0 END AS "MontoVenta",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'COMPRA' THEN OPER."Tasa" ELSE 0 END AS "TasaCompra",
		CASE TIPOOPERACION_OPE."Accion" WHEN 'VENTA'  THEN OPER."Tasa" ELSE 0 END AS "TasaVenta",	
		INS."IdEmisor" AS "IdEmisor",
		INS."Nemotecnico",
		OPER."MontoOperacionMoneda" AS "MontoValorizado",
		CASE OPER."IdEstado" WHEN 'ANT' THEN OPER."MontoOperacionMonedaAnticipo"
			WHEN 'LIQ' THEN OPER."MontoOperacionFinalMoneda"
			WHEN 'VIG' THEN 0
			ELSE 0 END AS "ValorPresenteCompraInst",
		INS."FechaVencimiento" AS "FechaVencimiento",
		1 AS "Cantidad",
		0 As "Precio",
		OPER."Tasa" AS "Tasa",
		OPER."IdTipoReajuste" AS "IdMoneda",
		OPER."IdCondicionLiquidacion",
		OPER."FechaVencimiento" AS "FechaVencimientoPacto",
		0 AS "PrecioPlazo",
		OPER."Tasa"	 AS "TasaPlazo",
		OPER."IdTipoReajuste" AS "IdMonedaPlazo",
		CASE WHEN TIPOOPERACION_OPE."Accion" = 'COMPRA' THEN OPER."MontoOperacionFinalMonedaAnticipo"
			 ELSE 0 END AS "TotalCompraVencimiento",
		CASE WHEN TIPOOPERACION_OPE."Accion" = 'VENTA' THEN OPER."MontoOperacionFinalMonedaAnticipo"
		ELSE 0 END AS  "TotalVentaVencimiento", 
		'CUS' as  "Custodia",
		'' AS "IdBolsa",
		'' AS "IdBoveda",
		0 AS "CantidadGarantia",
		INS."Nemotecnico" AS "NemoSVS",
		OPER."MontoOperacionMonedaAnticipo" AS "ValorPresente",
		0 AS "UtilidadPerdida",
		'' AS "FechaOrigenPacto",
		'' AS "DescripcionOperacion",
		''  AS "IdEstadoFactura",
		'' AS "Canal",
		0 AS "ValorIVA",
		0 AS "FolioComprobanteAsociado",
		OPER."IdInstrumento",
		OPER."IdTipoProducto",
		'IF' AS "IdClasificacionTipoInstrumento",
		INS."IdTipoInstrumento" AS "IdTipoInstrumento",
		CASE OPER."IdTipoOperacion"
			WHEN 'COMPAC' THEN 'RETVEN'
			WHEN 'VENPAC' THEN 'RETCOM'
		END AS "TipoOperacionSecundaria",
		'N' AS "EsFungible",
		OPER."MontoOperacionInicialMoneda" AS "ValorRescateInicial",
		OPER."IdEstado",
		OPER."MontoOperacionFinalMonedaAnticipo",
		OPER."IdOperacionOrigenReasignacion",
		--campos nuevos
		OPER."IdEstado" AS "IdEstadoOperacion",
	    0 AS "ValorRescateInicialReserva",
	    '' AS "IdEmisorReserva",
	    '' AS "IdClasificacionTipoInstrumentoReserva",
        0 AS "CantidadInicialReserva",
        0 AS "MontoLiquidacionOperacionReserva",
        0 AS "EgrMontoValorizadoReserva",
        0 AS "IngMontoValorizadoReserva",
        0 AS "TasaReserva",
        0 AS "INGMontoAPlazoReserva",
        OPER."IdOperacionRelacionada",
        0 AS "IdCarteraOrigen"
	FROM "BSCL.Mesa.BD::MES.Operacion" AS OPER
	INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
	INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
	LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON OPER."IdInstrumento" = INS."IdInstrumento" 
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION_OPE ON TIPOOPERACION_OPE."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE OPER."IdTipoProducto" IN ('MERF', 'IF')
	AND OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') 
	AND  OPER."IdEstado" = 'ANT'));
	
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::MRF.ConsultarLibroOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;
