  SELECT 
	DAT."FuenteDatos",
	CAST(DAT."IdValorizacion" AS BIGINT) AS "IdValorizacion",
	DAT."OrigenProcesamiento",
	DAT."FechaValorizacion",
	DAT."FechaOperacion", 						
	DAT."FechaVencimientoOperacion",    		
	DAT."PlazoEnDiasOperacion",							
	DAT."IdClasificacionTipoInstrumento", 		
	DAT."IdTipoInstrumento",					
	DAT."IdInstrumento",
	COALESCE(INS."Nemotecnico", DAT."Nemotecnico") AS "Nemotecnico",
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
	DAT."IdTipoOperacion",
	DAT."Situacion"
 FROM "BSCL.Custodia.BD.Vistas::CUS.DatosValorizarNCG499" DAT
 LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON INS."IdInstrumento" = DAT."IdInstrumento"
 WHERE DAT."FechaValorizacion" = :p_Fecha 
 AND DAT."IdClasificacionTipoInstrumento" IN ('RF', 'IF') 
 AND DAT."FuenteDatos" = 'MANT'
 AND DAT."IdEstado" NOT IN ('NEG')
 ORDER BY DAT."IdValorizacion";