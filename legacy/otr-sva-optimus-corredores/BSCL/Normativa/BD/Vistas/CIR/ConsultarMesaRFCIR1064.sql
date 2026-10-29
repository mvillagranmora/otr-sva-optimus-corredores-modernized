SET SCHEMA "BSCL"; 
GO 
DROP VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarMesaRFCIR1064";
GO
/*
 * -----------------------------------------------------------
 * Proposito : Vista de operaciones MRF para la circular 1064.
 *             Para los Pactos devuelve la operacion en T0
 *             ++ EL VENCIMIENTO/ANTICIPO  de este
 * Autor ....: Gat-Blac
 * Fecha ....: 2025-11-14
 * -----------------------------------------------------------
 */

CREATE VIEW "BSCL.Normativa.BD.Vistas::CIR.ConsultarMesaRFCIR1064" AS

	--*	
	--* TODAS LAS OPERACIONES VIGENTES (DATA de Cuando se crean en T0)
	--* ---------------------------------------------------------------------------------
	SELECT
			OPER."FechaCreacion",
			OPER."FechaOperacion" 									AS "FechaMovimiento",
			OPER."FechaLiquidacion",
			OPER."FolioOperacion" 									AS "FolioTransaccion",
			OPER."FolioOperacion" 									AS "FolioComprobante",
			OPER."IdOperacion"										AS "IdOperacion",
			OPER."IdTipoOperacion" 									AS "IdTipoOperacion",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			OPER."IdCliente",
			IDENT."DocumentoIdentidad",
			CASE PER."Tipo" 
				 WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
				 ELSE PER."RazonSocial"
			END  													AS "NombreCompleto",
			OPER."IdPortafolio",
			TIPOOPERACION."Accion"									AS "AccionOperacion",
			CASE WHEN TIPOOPERACION."Accion" = 'COMPRA' 
				 THEN RES."IngMontoValorizado" ELSE 0 END 			AS "MontoCompra",
			CASE WHEN TIPOOPERACION."Accion" = 'VENTA' 
				 THEN RES."IngMontoValorizado" ELSE 0 END 			AS "MontoVenta",
			CASE WHEN TIPOOPERACION."Accion" = 'COMPRA' 
				 THEN RES."Tasa"  ELSE 0 END 						AS "TasaCompra",
			CASE WHEN TIPOOPERACION."Accion" = 'VENTA' 
				 THEN RES."Tasa"  ELSE 0 END 						AS "TasaVenta",
			RES."IdEmisor" 											AS "IdEmisor",
			RES."Nemotecnico",
			RES."MontoValorizado",
			RES."MontoValorizado" 									AS "ValorPresenteCompraInst",
			RES."FechaVencimiento" 									AS "FechaVencimiento",
			CASE COALESCE(RES."IdClasificacionTipoInstrumento",'') 
				WHEN 'IF'  THEN RES."IngValorRescate" 
				ELSE RES."IngCantidad" END  						AS "Cantidad",
			RES."Precio" 											AS "Precio",
			RES."Tasa" 												AS "Tasa",
			RES."IdTipoReajuste" 									AS "IdMoneda",
			OPER."IdCondicionLiquidacion",
			OPER."FechaVencimiento" 								AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC') 
				THEN OPER."Tasa" ELSE 0 END 						AS "TasaPlazo",
			CASE WHEN OPER."IdTipoOperacion" IN ('COMPAC', 'VENPAC')
				THEN OPER."IdTipoReajuste"
				ELSE '' END 										AS "IdMonedaPlazo",
			CASE TIPOOPERACION."Accion" WHEN 'COMPRA' 
				THEN RES."INGMontoAPlazo" 
				ELSE 0 END 											AS "TotalCompraVencimiento",
			CASE TIPOOPERACION."Accion" WHEN 'VENTA'  
				THEN RES."INGMontoAPlazo" 
				ELSE 0 END 											AS  "TotalVentaVencimiento", 
			'CUS' 													AS "Custodia",
			RES."IdBolsa" 											AS "IdBolsa",
			'' 														AS "IdBoveda",
			0 														AS "CantidadGarantia",
			RES."Nemotecnico"										AS "NemoSVS",
			RES."MontoValorizado" 									AS "ValorPresente",
			CASE TIPOOPERACION."Accion" 		 
				WHEN 'COMPRA'  THEN (RES."ValorRescateInicial" - RES."MontoValorizado") 
				WHEN 'VENTA'   THEN (RES."MontoValorizado" - RES."ValorRescateInicial") 
				ELSE 0 END 											AS "UtilidadPerdida",
			'' 														AS "FechaOrigenPacto",
			COALESCE(TIPOOPERACION."Nombre",'') 					AS "DescripcionOperacion",
			''  													AS "IdEstadoFactura",
			OPER."IdTipoCanalIngreso" 								AS "Canal",
			0 														AS "ValorIVA",
			0 														AS "FolioComprobanteAsociado",
			RES."IdInstrumento",
			OPER."IdTipoProducto",
			COALESCE(RES."IdClasificacionTipoInstrumento",'') 		AS "IdClasificacionTipoInstrumento",
			COALESCE(RES."IdTipoInstrumento",'') 					AS "IdTipoInstrumento",
			'' 														AS "TipoOperacionSecundaria",
			RES."EsFungible" 										AS "EsFungible",
			RES."ValorRescateInicial" 								AS "ValorRescateInicial",
			'VIG'													AS "IdEstado",  -- EN T0 SIEMPRE ES VIGENTE 
			OPER."MontoOperacionFinalAnticipo",
			OPER."FechaOperacion" 									AS "FechaIngresoRegistro",
			OPER."IdOperacionOrigenReasignacion",
			OPER."ValorizacionManual"
	FROM 	"BSCL.Mesa.BD::MES.Operacion" AS OPER
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
			LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
			INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OPER."IdOperacion" = RES."IdOperacion"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE 	OPER."IdTipoProducto" = 'MERF'
			AND OPER."IdEstado" IN ( 'VIG' , 'LIQ', 'ANT' )


	--*			
	--* ---------------------------------------			
	UNION ALL
	--* ---------------------------------------
	--*
	
	--*	
	--* REVERSAS Y ANTICIPOS
	--* SE INFORMA AL CONTRARIO DE LA OPERACION SI ES VENTA > SE INFORMA COMPRA
	--* ---------------------------------------------------------------------------------
	SELECT
			OPER."FechaCreacion",
			--* --------------------			
			OPER."FechaLiquidacion" 								AS "FechaMovimiento",
			--* --------------------									
			OPER."FechaLiquidacion",
			OPER."FolioOperacion" 									AS "FolioTransaccion",
			OPER."FolioOperacion" 									AS "FolioComprobante",
			OPER."IdOperacion"										AS "IdOperacion",
			OPER."IdTipoOperacion" 									AS "IdTipoOperacion",
			0 AS "IdOperadorContraparte",
			0 AS "IdOperadorCorredor",
			OPER."IdCliente",
			IDENT."DocumentoIdentidad",
			CASE PER."Tipo" 
				WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
				ELSE PER."RazonSocial"
			END  													AS "NombreCompleto",
			OPER."IdPortafolio",
			CASE WHEN OPER."IdTipoOperacion" = 'COMPAC'  
				 THEN 'VENTA' ELSE 'COMPRA' END 					AS "AccionOperacion",
			CASE WHEN TIPOOPERACION."Accion" = 'VENTA' 
				 THEN RES."INGMontoAPlazo" ELSE 0 END 			    AS "MontoCompra",	
			CASE WHEN TIPOOPERACION."Accion" = 'COMPRA'  
				 THEN RES."INGMontoAPlazo" ELSE 0 END 				AS "MontoVenta",	
			CASE WHEN TIPOOPERACION."Accion" = 'VENTA'
				 THEN RES."Tasa"  ELSE 0 END 						AS "TasaCompra",
			CASE WHEN TIPOOPERACION."Accion" = 'COMPRA'
				 THEN RES."Tasa"  ELSE 0 END 						AS "TasaVenta",
			RES."IdEmisor" 											AS "IdEmisor",
			RES."Nemotecnico",
			RES."INGMontoAPlazo"									AS "MontoValorizado",
			RES."INGMontoAPlazo" 									AS "ValorPresenteCompraInst",
			RES."FechaVencimiento" 									AS "FechaVencimiento",
			CASE COALESCE(RES."IdClasificacionTipoInstrumento",'') 
				WHEN 'IF'  THEN RES."IngValorRescate" 
				ELSE RES."IngCantidad" END  						AS "Cantidad",
			RES."Precio" 											AS "Precio",
			RES."Tasa" 												AS "Tasa",
			RES."IdTipoReajuste" 									AS "IdMoneda",
			'T0' 													AS "IdCondicionLiquidacion",  -- REV/ANT MISMO 
			OPER."FechaLiquidacion" 								AS "FechaVencimientoPacto",
			0 AS "PrecioPlazo",
			OPER."Tasa" 											AS "TasaPlazo",
			OPER."IdTipoReajuste"									AS "IdMonedaPlazo",
			CASE TIPOOPERACION."Accion" WHEN 'VENTA' 
				THEN RES."MontoLiquidacion" 
				ELSE 0 END 											AS "TotalCompraVencimiento",
			CASE TIPOOPERACION."Accion" WHEN 'COMPRA'  
				THEN RES."MontoLiquidacion"
				ELSE 0 END 											AS  "TotalVentaVencimiento", 
			'CUS' 													AS "Custodia",
			RES."IdBolsa" 											AS "IdBolsa",
			'' 														AS "IdBoveda",
			0 														AS "CantidadGarantia",
			RES."Nemotecnico"										AS "NemoSVS",
			RES."MontoValorizado" 									AS "ValorPresente",
			CASE TIPOOPERACION."Accion" 		 
				WHEN 'COMPRA'  THEN (RES."MontoLiquidacion" - RES."MontoValorizado") 
				WHEN 'VENTA'   THEN (RES."MontoValorizado" - RES."MontoLiquidacion") 
				ELSE 0 END 											AS "UtilidadPerdida",
			'' 														AS "FechaOrigenPacto",
			COALESCE(TIPOOPERACION."Nombre",'') 					AS "DescripcionOperacion",
			''  													AS "IdEstadoFactura",
			OPER."IdTipoCanalIngreso" 								AS "Canal",
			0 														AS "ValorIVA",
			0 														AS "FolioComprobanteAsociado",
			RES."IdInstrumento",
			OPER."IdTipoProducto",
			COALESCE(RES."IdClasificacionTipoInstrumento",'') 		AS "IdClasificacionTipoInstrumento",
			COALESCE(RES."IdTipoInstrumento",'') 					AS "IdTipoInstrumento",
			'' 														AS "TipoOperacionSecundaria",
			RES."EsFungible" 										AS "EsFungible",
			RES."ValorRescateInicial" 								AS "ValorRescateInicial",
			OPER."IdEstado",
			OPER."MontoOperacionFinalAnticipo",
			OPER."FechaOperacion" 									AS "FechaIngresoRegistro",
			OPER."IdOperacionOrigenReasignacion",
			OPER."ValorizacionManual"
	FROM 	"BSCL.Mesa.BD::MES.Operacion" AS OPER
			INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPER."IdCliente")
			INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
			LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
			INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" RES ON OPER."IdOperacion" = RES."IdOperacion"
			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION	ON TIPOOPERACION."IdTipoOperacion" = OPER."IdTipoOperacion"
	WHERE 	OPER."IdTipoProducto" = 'MERF'
			AND OPER."IdTipoOperacion" IN ( 'COMPAC', 'VENPAC' )
			AND OPER."IdEstado" IN ( 'LIQ', 'ANT' );

GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::CIR.ConsultarMesaRFCIR1064" TO BCS_DESARROLLADOR WITH GRANT OPTION;