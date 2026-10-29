
-- Actualiza Comprobante con respuesta del SII y nuevos estados del DTE
UPDATE "BSCL.Facturacion.BD::FAC.Comprobante" 
SET                                 
	 "IdEstadoDTE" 			= :idestadodte
	,"IdEstadoSII" 			= :idestadosii
	,"IdRespuestaDTE" 		= :idrespuestadte
	,"RespuestaLocationDTE" = :respuestalocationdte
	,"RespuestaPdfDTE" 		= :respuestapdfdte
	,"RespuestaXmlDTE" 		= :respuestaxmldte	
WHERE "IdComprobante"		= :idcomprobante;
