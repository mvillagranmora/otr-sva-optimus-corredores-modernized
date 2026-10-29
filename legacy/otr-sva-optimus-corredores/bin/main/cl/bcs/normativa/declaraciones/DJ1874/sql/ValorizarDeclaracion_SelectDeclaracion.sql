  SELECT 
	DAT."Version",
	DAT."IdEmpresa",
	DAT."FuenteDatos",
	DAT."IdValorizacion",
	DAT."OrigenProcesamiento",
	DAT."FechaValorizacion",
	
	DAT."FechaOperacion", 						
	DAT."FechaVencimientoOperacion",    		
	DAT."PlazoEnDiasOperacion",							
	
	DAT."IdClasificacionTipoInstrumento", 		
	DAT."IdTipoInstrumento",					
	DAT."IdInstrumento",
	DAT."Nemotecnico",
	DAT."InsFechaVencimiento",
	DAT."InsFechaEmision",
	DAT."IdMonedaInstrumento",
	DAT."IdMonedaEmisionInstrumento",
	DAT."IdTipoReajusteInstrumento",
	DAT."Emisor",								
	DAT."IdCondicionDeLiquidacion",				
	DAT."FechaDeLiquidacion",
	DAT."Cantidad",
	DAT."TasaCompra",
	DAT."PrecioCompra",
	
	DAT."PrecioSubyacente",  
	DAT."TasaSubyacente",  	 
	DAT."PrecioGarantia",  	
	DAT."TasaGarantia",  	
	DAT."TasaTIF",  		
	DAT."IdPortafolio",		
	DAT."TablaOrigen",				
	DAT."IdTipoOperacion" 
 FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1874.DatosValorizar" DAT
 WHERE DAT."Version" = CAST(:Version AS NVARCHAR(4))AND DAT."IdEmpresa" = :IdEmpresa AND (DAT."FuenteDatos"=:FuenteDatos OR :FuenteDatos ='AMBAS') ;