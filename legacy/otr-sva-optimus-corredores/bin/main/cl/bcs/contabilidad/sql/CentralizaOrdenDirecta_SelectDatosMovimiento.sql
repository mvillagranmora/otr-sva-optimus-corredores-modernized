		SELECT 
			 Tran."IdTransaccion" 													AS "IdMovimiento"
			,CONCAT(Tran."IdClasificacionTipoInstrumento",Tran."IdTipoOperacion") 	AS "TipoDocumento"			 
			,Tran."FolioTransaccion"												AS "FolioTransaccion"
			,Tran."AccionOrden"														AS "CompraVenta"
			,Tran."IdBolsa"															AS "CodigoBolsa"
			,Tran."IdClasificacionTipoInstrumento"									AS "TipoInstrumento"
			,CASE Tran."EsCuentaPropia"
				WHEN 'N' THEN 'NO'
				ELSE 'SI'
			END 																	AS "EsCuentaPropia"
			,Tran."IdTipoInstrumento"												AS "FamiliaInstrumento"
			,Tran."FechaLiquidacion"												AS "FechaLiquidacion"
			,Tran."InstrumentoUI"													AS "Nemo"
			,Tran."IdMonedaTransaccion"												AS "Moneda"
			,Tran."IdTipoProducto"													AS "Producto"
			,Tran."IdEstadoTransaccion"												AS "Estado"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionUI"		Tran  
		WHERE 
				Tran."IdEmpresa" 			= :IdEmpresa
			AND Tran."FechaOperacion" 		= :FechaContable
			AND Tran."IndicadorOrdenDirecta" IN ('OD','OE')
			AND Tran."IdEstadoTransaccion"	= 'ING'
		UNION ALL
		SELECT 
			 Tran."IdTransaccion" 													AS "IdMovimiento"
			,CONCAT(Tran."IdClasificacionTipoInstrumento",Tran."IdTipoOperacion") 	AS "TipoDocumento"			 
			,Tran."FolioTransaccion"												AS "FolioTransaccion"
			,Tran."AccionOrden"														AS "CompraVenta"
			,Tran."IdBolsa"															AS "CodigoBolsa"
			,Tran."IdClasificacionTipoInstrumento"									AS "TipoInstrumento"
			,CASE Tran."EsCuentaPropia"
				WHEN 'N' THEN 'NO'
				ELSE 'SI'
			END 																	AS "EsCuentaPropia"
			,Tran."IdTipoInstrumento"												AS "FamiliaInstrumento"
			,Tran."FechaLiquidacion"												AS "FechaLiquidacion"
			,Tran."InstrumentoUI"													AS "Nemo"
			,Tran."IdMonedaTransaccion"												AS "Moneda"
			,Tran."IdTipoProducto"													AS "Producto"
			,Tran."IdEstadoTransaccion"												AS "Estado"
		FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccionReporteUI"		Tran  
		WHERE 
				Tran."IdEmpresa" 			= :IdEmpresa
			AND Tran."FechaOperacion" 		= :FechaContable
			AND Tran."IndicadorOrdenDirecta" IN ('OD','OE')
			AND Tran."IdEstadoTransaccion"	= 'ING'	;
			
