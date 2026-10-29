SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones";
GO
-- OPERACIONES
--  Vista para Normativa.
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 27/09/2016  
-- 

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" AS
SELECT  
	SPT."FechaIngresoRegistro",
	SPT."FechaOperacion"						AS "FechaMovimiento",
	SPT."FechaLiquidacion",
	SPT."TipoComprobanteFacturacion" 			as "IdTipoComprobante",
	COALESCE(COMP."FolioComprobante",0) 		AS "FolioComprobante",
	COALESCE(COMP."IdComprobante",0) 			AS "IdComprobante",
	COALESCE(MOVCOM."IdTransaccion",0) 	  		AS "IdTransaccion",
	COALESCE(MOVCOM."FolioTransaccion",0)   			AS "FolioTransaccion",
	SPT."IdOperacionSpotOrigen"					AS "IdOperacion",
	SPT."IdComprobanteFacturacion",
	0 AS "IdContraparte",
	0 AS "IdOperadorContraparte",
	0 AS "IdOperadorCorredor",
	SPT."IdCliente" 							as "IdCliente",

	IDENT."DocumentoIdentidad",
	CASE PER."Tipo" 
		WHEN 'N' THEN PER."Nombres" ||' '|| PER."ApellidoPaterno" || ' ' || PER."ApellidoMaterno"
		ELSE PER."RazonSocial"
	END  as "NombreCompleto",
	
	SPT."IdPortafolio" 							as "IdPortafolio",
	TIPOOPERACION."Accion" 						AS "AccionOperacion",
	INS."Nemotecnico" as "Nemotecnico",
	SPT."MontoMonedaPrincipal"					as "Cantidad",
	SPT."TipoCambioCierre" 						as "Precio",
	SPT."MontoMonedaSecundaria",
	SPT."IdMonedaPrincipal" 					AS "IdMoneda",
	SPT."IdMonedaSecundaria" 					AS "IdMonedaSecundaria",
	SPT."Utilidad",
	SPT."ParidadCierre", 
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN  SPT."MontoMonedaSecundaria"  ELSE 0 END AS "MontoCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN  SPT."MontoMonedaSecundaria"  ELSE 0 END AS "MontoVenta",
	CASE TIPOOPERACION."Accion" WHEN 'COMPRA' THEN SPT."ParidadCierre"  ELSE 0 END AS "TasaCompra",
	CASE TIPOOPERACION."Accion" WHEN 'VENTA'  THEN SPT."ParidadCierre"  ELSE 0 END AS "TasaVenta",
	'CUS' as  "Custodia",
	'DOLAR' AS "NemoSVS",
	COALESCE(COMP."IdEstadoComprobante",'') 		AS "IdEstadoFactura",
	SPT."IdTipoCanalIngreso" 						AS "Canal",
	OPE."IdTipoProducto" 							AS "IdTipoProducto",
	TINS."IdClasificacionTipoInstrumento"			AS "IdClasificacionTipoInstrumento", 
	INS."IdTipoInstrumento" 						AS "IdTipoInstrumento",
	SPT."IdTipoOperacion" 							AS "IdTipoOperacion",
	INS."IdInstrumento" 							AS "IdInstrumento",
	TIPOOPERACION."Nombre" 							AS "TipoOperacion",
	SPT."Estado"									AS "IdEstadoOperacion",
	SPT."EstadoConfirmacion",
	COALESCE(MOVCOM."IdMovimientoComprobante",0)	AS "IdMovimientoComprobante",
	MOVCOM."IdEstadoFacturacion"					AS "IdEstadoMovimientoComprobante",
	MOVNCR."IdComprobanteReferencia"				AS "IdComprobanteReferenciaNCR",
	MOVNCR."IdComprobante"          				AS "IdComprobanteReferencia",
	SPT."OperacionFicticia"							AS "OperacionFicticia"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" AS SPT
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVCOM
	ON SPT."IdComprobanteFacturacion" = MOVCOM."IdMovimientoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVNCR
	ON (MOVCOM."IdComprobante" > 0 AND 
		MOVCOM."IdComprobante" = MOVNCR."IdComprobanteReferencia" AND 
		MOVNCR."IdTipoComprobante" IN ('NCE','NCR','NEO','NOE','NEE'))
INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP
	ON MOVCOM."IdComprobante" = COMP."IdComprobante"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" =  SPT."IdCliente")
INNER JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER   ON (PER."IdPersona" = CLI."IdPersona")
LEFT  JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad")
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON SPT."IdInstrumento" = INS."IdInstrumento"
LEFT  JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TINS ON TINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion" OPE ON SPT."IdTipoOperacion" = OPE."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOOPERACION
	ON TIPOOPERACION."IdTipoOperacion" = SPT."IdTipoOperacion";

GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperaciones" TO BCS_DESARROLLADOR WITH GRANT OPTION;