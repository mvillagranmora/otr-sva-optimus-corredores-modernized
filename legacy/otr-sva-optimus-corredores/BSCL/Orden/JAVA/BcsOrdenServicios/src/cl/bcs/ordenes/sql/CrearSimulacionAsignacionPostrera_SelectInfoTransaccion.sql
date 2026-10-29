SELECT 	
	p_TRA."IdMovimiento"
	, TRA."IdTransaccion"
	, TRA."FolioTransaccion"
	, TRA."FechaOperacionTransaccion"
	, TRA."FechaSistemaCreacion" as "FechaTransaccion"
	, TRA."EsFungible"	
	, TRA."Tasa"
	, TRA."IdInstrumento"
	, TRA."PrecioTransaccion" AS "Precio"
	, TRA."Instrumento"				
	, TRA."IdTipoInstrumento"
	, TRA."IdClasificacionTipoInstrumento"
	, TRA."IdMonedaInstrumento"
	, TRA."IdTipoOperacion"	
	, TRA."IdTipoProducto"
	, TRA."IdCondicionDeLiquidacion"
	, TRA."IdBolsa"
	, TRA."IdLibroOrdenes"
	, TRA."CantidadTransaccion" AS  "CantidadTotalTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN p_TRA."Cantidad" 	ELSE TRA."CantidadDisponibleTransaccion" 	 	END AS "CantidadDisponibleTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN 0 					ELSE TRA."CantidadAsignadaTransaccion" 		 	END AS "CantidadAsignadaTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN p_TRA."ValorRescate" ELSE TRA."ValorRescateDisponibleTransaccion" 	END AS "ValorRescateDisponibleTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN 0 					ELSE TRA."ValorRescateAsignadoTransaccion" 	 	END AS "ValorRescateAsignadoTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN p_TRA."Monto" 		ELSE TRA."MontoDisponibleTransaccion" 		 	END AS "MontoDisponibleTransaccion"
	, CASE WHEN :p_IdTipo = 'REFACTURACION' or :p_IdTipo = 'REASIGNACION' THEN 0 					ELSE TRA."MontoAsignadoTransaccion" 		 	END AS "MontoAsignadoTransaccion"
	, TRA."CorteMinimo"
	
	,BOL."EsExtranjera"
	,CURRENT_UTCTIMESTAMP AS "FechaIngresoRegistro"	
	,COALESCE(MLIQ."IdMonedaLiquidacion", TRA."IdMonedaInstrumento") AS "IdMonedaLiquidacionOrden"
	,TRA."Tamano" 
	,TRA."MontoTransaccion" 
	,TRA."ValorRescateTransaccion" 
	,inst."PrecioStrike"
	
	,CASE 
		WHEN inst."EstaInscrito" = 'A' THEN 'AMERICANA' 
		WHEN inst."EstaInscrito" = 'E' THEN 'EUROPEA' 
		ELSE '' 
	END AS "EstiloOpcion"
	
	,CASE 
		WHEN inst."LargoPlazo" = 'A'   THEN 'AUTOMATICA' 
		WHEN inst."LargoPlazo" = 'M'   THEN 'MANUAL' 
		ELSE '' 
	END AS "ModalidadEjercicio"
	
	,CASE 
		WHEN inst."EsDivisible" = 'P' 	THEN 'FISICA' 
		WHEN inst."EsDivisible" = 'C' 	THEN 'FINANCIERA' 
		ELSE '' 
	END AS "ModalidadLiquidacion"
	
	,CASE 
		WHEN inst."ISIN" = 'C' 			THEN 'CALL' 
		WHEN inst."ISIN" = 'P' 			THEN 'PUT' 
		ELSE '' 
	END AS "TipoOpcion"
	,COALESCE(TRA."InstrumentoNegociacion",'') AS "InstrumentoNegociacionTran"
	
FROM "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS TRA 
INNER JOIN "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones" p_TRA ON (TRA."IdTransaccion" = p_TRA."IdTransaccion")
LEFT JOIN "BSCL.Plataforma.BD::INS.Bolsa" AS BOL ON  ( BOL."IdBolsa" = TRA."IdBolsa" )
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" 	inst 	ON TRA."IdInstrumento" 	= inst."IdInstrumento"
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumentoMonedaLiquidacion" AS MLIQ ON (TRA."IdMonedaInstrumento" = MLIQ."IdMoneda" 
																			 AND TRA."IdTipoInstrumento"   = MLIQ."IdTipoInstrumento" 
								        									 AND MLIQ."EsPorDefecto" = 'S')
								
								
ORDER BY TRA."FechaOperacionTransaccion";

