SELECT 
	 "RutCliente"
	,"NombreCliente"
	,"GarantiaExigida"				    
	,"GarantiaEnterada"
	,"TipoLinea"
FROM "BSCL.Normativa.BD::GBL.GarantiasForwardEBox_TablaPaso"
WHERE "SecuenciaSolicitud" = :SecuenciaSolicitud;