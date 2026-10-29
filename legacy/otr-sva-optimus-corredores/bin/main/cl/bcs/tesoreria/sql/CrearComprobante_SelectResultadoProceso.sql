
SELECT  'IdComprobante' as "Campo",
		"IdComprobante" as "Id",
		'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
		'ING' as "Resultado",
		"IdTipoMovimiento" AS "IdTipoMovimiento",	
		 CASE WHEN "IdTipoMovimiento" = 'MOVEGR' THEN 'UI_CREADO_COMPROBANTE_EGRESO' 
	   		  WHEN "IdTipoMovimiento" = 'MOVING' THEN 'UI_CREADO_COMPROBANTE_INGRESO'
	   	 END as "CodigoMensaje",
	   	 "NumeroReferencia" AS "NumeroReferencia"
FROM "BSCL.Tesoreria.BD::GBL.CrearComprobante_Comprobante"		--> Comprobantes generados desde movimientos o comprobantes

UNION ALL

SELECT 'IdIngresoEgreso' as "Campo",
		"IdIngresoEgreso" as "Id",
		'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
		'EMI' as "Resultado",
		'' AS "IdTipoMovimiento",
		'' AS "CodigoMensaje",
	   	 "NumeroReferencia" AS "NumeroReferencia"
FROM "BSCL.Tesoreria.BD::GBL.IngresoEgresoGenerarComprobante_Proceso";

/*************
 * IMPORTANTE: esto se debe activar una vez el servicio java incluya la generacion de comprobantes a partir de comprobantes
**************
UNION ALL
	 
SELECT 'IdComprobante' as "Campo",
		"IdComprobante" as "Id",
		'BSCL.Tesoreria.BD.Servicios::CrearComprobante' as "Servicio",
		'MOD' as "Resultado",
		'' AS "IdTipoMovimiento",
		'' AS "CodigoMensaje"
FROM :v_ComprobanteAGenerar; se debe cambiar por tabla global de proceso comprobantes
****/