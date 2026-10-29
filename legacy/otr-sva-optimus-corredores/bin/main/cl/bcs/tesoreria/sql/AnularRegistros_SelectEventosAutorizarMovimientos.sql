SELECT 
	'ANMOTE' 				AS "IdTipoEvento"            -- Anulación Movimiento Tesorería		 
	,:p_IdModulo 	 		AS "IdModuloSolicita"
	,TO_BIGINT(:p_IdUsuario)AS "IdUsuarioSolicita"
	,:p_FechaMovimiento 	AS "Fecha"
	,'' 					AS "IdTipoOperacion"
	,'ANMOTE' 				AS	"IdConcepto"
	,TO_NVARCHAR(IE."IdIngresoEgreso") 	AS "NumeroOperacionConcepto"
	,TO_NVARCHAR(IE."FechaMovimiento", 'YYYY-MM-DD') 	AS "FechaOperacionConcepto"								    
	,TO_BIGINT(0)  AS "IdCliente"
	,TO_BIGINT(0)  AS "IdFuncionario"
	,TO_BIGINT(0)  AS "IdProveedor"
	,'' AS "IdTipoProducto"
	,'' AS "IdClasificacionTipoInstrumento"
	,'' AS "IdTipoInstrumento"
	,TO_BIGINT(0)  AS "IdInstrumento"
	,'' AS "Nemo"
	,TO_BIGINT(0)  AS "Cantidad"
	,IE."Monto"    AS "Monto"
	,0.0  AS "Precio"
	,0.0  AS "Tasa"
	,IE."IdMoneda" AS "IdMoneda"
	,'ANULACION MOVIMIENTO TESORERIA (' || CAST (IE."IdIngresoEgreso" AS NVARCHAR(15)) ||')' AS "Glosa"
FROM "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS IE 
INNER JOIN #AnularRegistros_p_RegistrosAProcesar as RP ON (RP."Id" = IE."IdIngresoEgreso")
WHERE IE."IdIngresoEgreso" IN (:Id)
AND RP."Entidad" = 'TES.IngresoEgreso';