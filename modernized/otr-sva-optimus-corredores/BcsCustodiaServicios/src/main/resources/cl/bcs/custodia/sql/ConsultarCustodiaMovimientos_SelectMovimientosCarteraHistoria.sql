SELECT 
	   TRIM( CUS."DocumentoIdentidad") AS  "DocumentoIdentidad"
      ,CUS."CodigoPortafolio"	   
	  ,CUS."IdCliente"
	  ,CUS."IdPortafolio"
	  ,CUS."NombreCliente"
      ,CUS."FechaOperacion"
      ,CUS."FechaLiquidacion"
	  ,CUS."Accion"
	  ,CUS."TipoRegistro"
	  ,CUS."IdTipoOperacion"
	  ,TIPOPER."Nombre" 				AS "TipoOperacion"
	  ,CUS."NombreConcepto"
	  ,CUS."Cantidad"
	  ,CUS."Cantidad"        			AS "CantidadNeg"
	  ,CUS."PrecioTir"
	  ,CUS."Precio"
	  ,CUS."Monto"		  
	  ,CUS."Nemotecnico"
	  ,CUS."ISIN"
	  ,CUS."IdEmisor"
	  ,CUS."IdClasificacionTipoInstrumento"
	  ,CUS."IdTipoInstrumento"
	  ,CUS."IdBoveda"
	  ,CUS."NumeroCuentaBoveda"	   
	  ,CUS."IdTipoCustodia"
	  ,CUS."NombreTipoCustodia"
	  ,CUS."FechaComprobante"
	  ,CUS."IdTipoComprobante"
	  ,CUS."FolioComprobante"
	  ,CUS."FolioCartera"
	  ,CUS."FolioCartera"				AS "IdCartera"
	  ,COALESCE(PRODUCTO."Nombre", '-') AS "Producto"
	  ,INS."FechaVencimiento"  AS "FechaVencimientoInstr"
	  ,INS."IdMoneda" AS "IdMonedaInstr"
	  ,CUS."Tasa"
	  , CAST(0 AS DECIMAL(26, 8)) AS "Spread"
	  , CAST(0 AS DECIMAL(26, 8)) AS "SpreadMercado"
	  , CAST(0 AS DECIMAL(26, 8)) AS "Saldo"
	  ,CUS."Monto" 						AS "MontoMercadoMonedaInstrumento"
	  ,CUS."FechaCertificacion"
	  ,'CLP' 							AS "IdMonedaInstr"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos" CUS
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOPER ON TIPOPER."IdTipoOperacion" = CUS."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRODUCTO ON PRODUCTO."IdTipoProducto" = TIPOPER."IdTipoProducto"
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON INS."IdInstrumento" = CUS."IdInstrumento"
WHERE CUS."IdCliente" 							= :IdCliente
AND  (CUS."IdPortafolio" 						= :IdPortafolio					  OR :IdPortafolio = 0 )
AND  (CUS."IdClasificacionTipoInstrumento" 		= :IdClasificacionTipoInstrumento OR :IdClasificacionTipoInstrumento = '')
AND  (CUS."IdTipoInstrumento" 					= :IdTipoInstrumento 			  OR :IdTipoInstrumento = '')
AND  (CUS."IdInstrumento" 						= :IdInstrumento     			  OR :IdInstrumento = 0)
AND  (CUS."Nemotecnico" 						= :Nemotecnico 			  		  OR :Nemotecnico = '')
AND  (CUS."FechaOperacion"						>= :FechaInicio 	 			  AND CUS."FechaOperacion" <= :FechaTermino)
ORDER BY CUS."FolioCartera" DESC;