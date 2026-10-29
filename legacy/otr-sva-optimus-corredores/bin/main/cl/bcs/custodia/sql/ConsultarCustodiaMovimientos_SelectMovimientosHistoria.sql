-- Consulta de historico de movimientos Sebra de la tabla BSCL.Sebra.BD::MIG.TBCTMVTO 
-- referenciada en la vista BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos, 
-- previo a la fecha del parametro PLA.FechaGoLive

SELECT 
	   CUS."DocumentoIdentidad"
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
	  ,CUS."Monto" 						AS "MontoMercadoMonedaInstrumento"
	  ,CUS."FechaCertificacion"
	  ,'CLP' 							AS "IdMonedaInstr"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCustodiaMovimientosHistoricos" CUS
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIPOPER ON TIPOPER."IdTipoOperacion" = CUS."IdTipoOperacion"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto" PRODUCTO ON PRODUCTO."IdTipoProducto" = TIPOPER."IdTipoProducto"
WHERE CUS."IdCliente" 							= :IdCliente
AND   CUS."IdPortafolio" 						= :IdPortafolio
AND  (CUS."IdClasificacionTipoInstrumento" 		= :IdClasificacionTipoInstrumento OR :IdClasificacionTipoInstrumento = '')
AND  (CUS."IdTipoInstrumento" 					= :IdTipoInstrumento 			  OR :IdTipoInstrumento = '')
AND  (CUS."IdInstrumento" 						= :IdInstrumento     			  OR :IdInstrumento = 0)
AND  (CUS."Nemotecnico" 						= :Nemotecnico 			 		  OR :Nemotecnico = '')
AND  (CUS."FechaOperacion"						>= :FechaInicio 	 			  AND CUS."FechaOperacion" <= :FechaTermino)
ORDER BY CUS."FolioCartera" DESC;
