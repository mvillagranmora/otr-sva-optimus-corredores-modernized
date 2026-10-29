-- ***************************************************************************************************************
-- Cartera Simultanea - Interfaz GenerarInterfazC08CarteraPacteada
-- ***************************************************************************************************************
SELECT 	CASE WHEN CAR."IdTipoOperacion" = 'COMSIM' THEN 'C'
        	 WHEN CAR."IdTipoOperacion" = 'VENSIM' THEN 'V'
             ELSE ' ' 
        END AS TIPO_COMPROM,
		COALESCE (TO_VARCHAR(TRA."FechaOperacion", 'DD-MM-YYYY'),'' )  AS FEC_TRANS,
		COALESCE (TO_VARCHAR(CAR."FechaVencimientoOperacion", 'DD-MM-YYYY'),'' )  AS FEC_VENC,		
		COALESCE (TO_VARCHAR(CAR."FechaLiquidacion", 'DD-MM-YYYY'),'' )  AS FEC_LIQUID,
	    COALESCE (CAR."PlazoEnDiasOperacion" , 0    ) AS PLAZO,
		ROUND(COALESCE (TRA."Precio" , 0) * COALESCE (CAR."INGCantidad", 0))  AS MONTO_CAPITAL,
		COALESCE (SUB."INGMontoATasaSubyacenteActual" , 0    )  AS VALOR_PTE,
		COALESCE (CAR."TasaCompra" , 0    )  AS TASA
	FROM "BSCL.Custodia.BD::CUS.Cartera" CAR 
		INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAR."FolioCartera" = SUB."FolioCarteraRelacionada" 
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COM ON COM."IdComprobante" = CAR."FolioComprobante" 
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON MOV."IdComprobante" = COM."IdComprobante" 
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."IdTransaccion" = MOV."IdTransaccion" 
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON  TOPE."IdTipoOperacion" = TRA."IdTipoOperacion" 
	WHERE CAR."IdEmpresa" 						 = :IdEmpresa
		AND CAR."IdEstado" 						 = 'VIG' 
		AND CAR."IdTipoProducto" 				 = 'INSI'
		AND TRA."FechaOperacion"                 <= :FiltroFecha
	ORDER BY
		FEC_VENC	   