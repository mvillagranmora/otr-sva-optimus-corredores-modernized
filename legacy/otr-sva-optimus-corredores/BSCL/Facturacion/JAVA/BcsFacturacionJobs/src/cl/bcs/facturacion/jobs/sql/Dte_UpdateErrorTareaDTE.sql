
-- Actualiza Tarea DTE con respuesta del SII y nuevos estados del DTE
UPDATE "BSCL.Facturacion.BD::FAC.TareaDTE" 
SET                               
	 "IdEstadoTareaDTE" 	= :idestadotareadte
	,"IdEstadoDTE" 			= :idestadodte
	,"IdEstadoSII" 			= :idestadosii
	,"IdRespuestaDTE" 		= :idrespuestadte
	,"RespuestaDTEEnvioSII" = :respuestadteenviosii
	,"RespuestaLocationDTE" = :respuestalocationdte
	,"RespuestaPdfDTE" 		= :respuestapdfdte
	,"RespuestaXmlDTE" 		= :respuestaxmldte
	
WHERE "IdEstadoTareaDTE"    = 'PEN'
AND   "IdTareaDTE" 		    = (CASE WHEN (:actualizarPorCampo = 'IdTareaDTE')      THEN :idtareadte      ELSE "IdTareaDTE" END)       -- Para TareaDteLibro
AND   "IdTareaDTEGrupo"     = (CASE WHEN (:actualizarPorCampo = 'IdTareaDTEGrupo') THEN :idtareadtegrupo ELSE "IdTareaDTEGrupo" END)
AND   "IdComprobante"       = (CASE WHEN (:actualizarPorCampo2 = 'IdComprobante') THEN :idcomprobante ELSE "IdComprobante" END); -- Para TareaDteCmbte

    
