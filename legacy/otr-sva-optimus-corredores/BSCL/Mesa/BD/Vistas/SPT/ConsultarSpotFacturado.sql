SET SCHEMA "BSCL";

DROP VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado";

CREATE VIEW "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado" AS
SELECT
	 SPOT."IdOperacionSpot"
	,SPOT."FechaOperacion"
	,SPOT."IdComprobanteFacturacion"
	,SPOT."Estado"
	,SPOT."IdEmpresa"
	,SPOT."EstadoConfirmacion"
	,MOVFAC."IdMovimientoComprobante"
	,MOVFAC."IdComprobante"
	,MOVFAC."IdEstadoFacturacion"
	,MOVNCR."IdComprobanteReferencia"
	,MOVNCR."IdTipoComprobante"
	,MOVNCR."IdEstadoFacturacion"	AS "IdEstadoFacturacionNotaCredito"
	,CASE WHEN COALESCE(SPOT."IdComprobanteFacturacion",0) = 0 OR MOVFAC."IdEstadoFacturacion" <> 'FAC' OR 
			   (MOVFAC."IdEstadoFacturacion" = 'FAC' AND MOVNCR."IdComprobanteReferencia" IS NOT NULL) THEN 1
		  ELSE 4 END				AS "IdResultado"
	,'OPERACION_SPOT_FACTURADA'		AS "Codigo"
	--Modificar-Anular
	,CASE WHEN SPOT."EstadoConfirmacion" = 'PEN' OR SPOT."IdTipoCanalIngreso" = 'FMM' THEN 1
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'ING' THEN 1
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'ANU' THEN 1
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'FAC' AND 
		  	   MOVNCR."IdComprobanteReferencia" IS NOT NULL THEN 1
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND COALESCE(CONMES."Valor",'N') = 'N' AND MOVFAC."IdEstadoFacturacion" <> 'FAC' THEN 1
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND CONMES."Valor" = 'S' AND MOVFAC."IdEstadoFacturacion" <> 'FAC' THEN 0
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'FAC' THEN 0
		  ELSE 1 END				AS "PuedeModificarAnular"
	,CASE WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'ANU' THEN 'OPERACION_SPOT_ANULADA'
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND CONMES."Valor" = 'S' AND MOVFAC."IdEstadoFacturacion" <> 'FAC' THEN 'OPERACION_CONFIRMADA'
		  WHEN SPOT."EstadoConfirmacion" = 'CNF' AND MOVFAC."IdEstadoFacturacion" = 'FAC' THEN 'OPERACION_SPOT_FACTURADA'
		  ELSE 'OK' END				AS "CodigoModificarAnular"
FROM "BSCL.Mesa.BD::SPT.OperacionSpot" SPOT
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVFAC
	ON SPOT."IdComprobanteFacturacion" = MOVFAC."IdMovimientoComprobante"
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVNCR
	ON (MOVFAC."IdComprobante" > 0 AND 
		MOVFAC."IdComprobante" = MOVNCR."IdComprobanteReferencia" AND 
		MOVNCR."IdTipoComprobante" IN ('NCE','NCR','NEO','NCO','NEE'))
LEFT JOIN "BSCL.Mesa.BD::MES.ConfiguracionMesa" CONMES
	ON (CONMES."CodigoParametro" = 'ENVIA_SPOT_A_CONFIRMAR_LIQUIDACION' AND
		CONMES."IdEmpresa" = SPOT."IdEmpresa")
WHERE SPOT."Estado" = 'VIG'
;

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::SPT.ConsultarSpotFacturado" TO BCS_DESARROLLADOR WITH GRANT OPTION;
