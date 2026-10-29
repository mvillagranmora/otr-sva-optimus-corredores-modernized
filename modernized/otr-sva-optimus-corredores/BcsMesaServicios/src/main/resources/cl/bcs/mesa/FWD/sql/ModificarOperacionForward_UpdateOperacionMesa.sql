UPDATE "BSCL.Mesa.BD::MES.Operacion" 
SET
	 "FechaOperacion"		= :FechaOperacion
	,"IdCliente" 			= :IdCliente
	,"IdPortafolio" 		= :IdPortafolio
	,"MontoOperacion"		= :MontoOperacion
	,"MontoOperacionInicial"= :MontoOperacionInicial
	,"MontoOperacionFinal"	= :MontoOperacionFinal
	,"TCCierre"				= :TCCierre
    ,"ParidadCierre"		= :ParidadCierre
    ,"TCCosto"				= :TCCosto             
    ,"PuntosTasasForward"	= :PuntosTasasForward  
    ,"PuntosForward"		= :PuntosForward           
    ,"TCForward"			= :TCForward               
    ,"TasasMonedaPrincipal"	= :TasasMonedaPrincipal    
    ,"TasasMonedaSecundaria"= :TasasMonedaSecundaria   
    ,"MontoDiferido"		= :MontoDiferido           
    ,"MontoMargen"			= :MontoMargen
    ,"IdModalidadLiquidacion" = :IdModalidadLiquidacion
    ,"FechaVencimiento"		= :FechaVencimiento
    , "TipoDocumento" = :TipoDocumento
    , "IdContacto" = :IdContacto
    , "FechaEdicion" = :FechaOperacion
    --,"IdEstado"				= 'MOD'
WHERE "IdOperacion" IN (:IdOperacion);
