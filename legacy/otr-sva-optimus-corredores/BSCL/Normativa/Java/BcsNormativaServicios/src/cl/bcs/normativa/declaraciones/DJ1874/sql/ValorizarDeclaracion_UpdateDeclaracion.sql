 UPDATE DJ
  SET
     DJ."ValIndicadorValorizado"			  	=	CASE WHEN LENGTH(TRIM(:MensajeErrorFuente))>0 THEN  'NO' ELSE 'SI' END 
    ,DJ."ValResultado"			  				=	CASE WHEN LENGTH(TRIM(:MensajeErrorFuente))>0 THEN  RPAD('ERROR: ' || TRIM(:MensajeErrorFuente),399,'') ELSE 'OK' END  
   	,DJ."ValMontoMonedaInstrumento"			  	=	:ValMontoMonedaInstrumento
   	,DJ."ValMontoMonedaSistema"			  		=	:ValMontoMonedaSistema
   	,DJ."ValMontoMonedaInstrumentoFechaVenta"  	=	:ValMontoMonedaInstrumentoFechaVenta
   	,DJ."ValMontoMonedaSistemaFechaVenta"		=	:ValMontoMonedaSistemaFechaVenta
   	,DJ."ValMontoDevengadoAMonedaInstrumento"	=	:ValMontoDevengadoAMonedaInstrumento
   	,DJ."ValMontoDevengadoAMonedaSistema"		=	:ValMontoDevengadoAMonedaSistema
   	,DJ."InteresDevengadoMonedaUnidadReajuste" 	=	coalesce(:ValMontoDevengadoAMonedaInstrumento,0)
   	,DJ."InteresDevengadoPesos"					= 	coalesce(:ValMontoDevengadoAMonedaSistema,0)
        FROM "BSCL.Normativa.BD::TRI.DatosDJ1874"   AS DJ
WHERE DJ."ID" IN (:IdValorizacion); 