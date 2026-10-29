SELECT  
	CASE 
		WHEN CAA."IdTipoMovimiento" = 'MOVCAR' THEN 'ANUCAR'
		WHEN CAA."IdTipoMovimiento" = 'MOVABN' THEN 'ANUABO'
	 END as "IdTipoEvento"
	,:p_IdModulo 	as "IdModuloSolicita"
	,TO_BIGINT(:p_IdUsuario)	as "IdUsuarioSolicita"
	,TO_NVARCHAR(CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD') as "Fecha"
	,CAA."IdTipoOperacion"
	,CAA."IdConcepto" as "IdConcepto"
	,TO_NVARCHAR(CAA."IdCargoAbonoMovimiento") as "NumeroOperacionConcepto"
	,TO_NVARCHAR(CAA."FechaSistema", 'YYYY-MM-DD') as "FechaOperacionConcepto"
	,CAA."IdCliente"
	,TO_BIGINT(0) as "IdFuncionario"
	,TO_BIGINT(0) as "IdProveedor"
	,CAA."IdTipoProducto"
	,'' as "IdClasificacionTipoInstrumento"
	,CAA."IdTipoInstrumento"
	,CAA."IdInstrumento"
	,'' as "Nemo"
	,TO_BIGINT(0) as "Cantidad"
	,CAA."Monto"
	,TO_DECIMAL(0) as "Precio"
	,TO_DECIMAL(0) as "Tasa"
	,CAA."IdMoneda"
	,CAA."GlosaAnulacion" as "Glosa"
FROM "BSCL.CuentaInversion.BD::CTA.AnularCargoAbono_SRV_Movimientos_Anular" as CAA 
WHERE CAA."IdTipoMovimiento" IS NOT NULL AND CAA."IdInterno" IN (:IdInterno) 
AND (CAA."EsCargoAbonoPorTraspaso" = 'N' OR (CAA."EsCargoAbonoPorTraspaso" = 'S' AND CAA."IdTipoMovimiento" = 'MOVCAR'));