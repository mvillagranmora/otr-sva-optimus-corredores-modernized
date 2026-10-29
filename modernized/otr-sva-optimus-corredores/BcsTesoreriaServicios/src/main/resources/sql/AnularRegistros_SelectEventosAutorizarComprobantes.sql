SELECT 
	'ANCOTE' 				AS "IdTipoEvento"            -- Anulación Comprobante Tesorería Liquidado   		 
	,:p_IdModulo 	 		AS "IdModuloSolicita"
	,TO_BIGINT(:p_IdUsuario)AS "IdUsuarioSolicita"
	,:p_FechaMovimiento 	AS "Fecha"
	,'' 					AS "IdTipoOperacion"
	,'ANCOTE' 				AS	"IdConcepto"
	,TO_NVARCHAR(COM."IdComprobante") 	AS "NumeroOperacionConcepto"
	,TO_NVARCHAR(COM."FechaMovimiento", 'YYYY-MM-DD') 	AS "FechaOperacionConcepto"								    
	,TO_BIGINT(0)  AS "IdCliente"
	,TO_BIGINT(0)  AS "IdFuncionario"
	,TO_BIGINT(0)  AS "IdProveedor"
	,'' AS "IdTipoProducto"
	,'' AS "IdClasificacionTipoInstrumento"
	,'' AS "IdTipoInstrumento"
	,TO_BIGINT(0)  AS "IdInstrumento"
	,'' AS "Nemo"
	,TO_BIGINT(0)  AS "Cantidad"
	,COM."Monto"   AS "Monto"
	,0.0  AS "Precio"
	,0.0 AS "Tasa"
	,COM."IdMoneda" AS "IdMoneda"
	,'ANULACION COMPROBANTE TESORERIA (' || CAST (COM."IdComprobante" AS NVARCHAR(15)) ||')' AS "Glosa"
FROM "BSCL.Tesoreria.BD::TES.Comprobante" AS COM 
INNER JOIN #AnularRegistros_p_RegistrosAProcesar as RP ON (RP."Id" = COM."IdComprobante")
WHERE COM."IdComprobante" IN (:Id)
AND RP."Entidad" = 'TES.Comprobante'
