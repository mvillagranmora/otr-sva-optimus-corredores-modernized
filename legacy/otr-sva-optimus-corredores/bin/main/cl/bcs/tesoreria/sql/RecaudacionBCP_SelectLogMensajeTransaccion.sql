/**
 * Proposito: Consulta LOG con el mensaje de transacción recibido del BCP 
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */  

SELECT 
	 "IdMensajeTransaccion"
	,"RqUID"
	,"TipoTrasaccion"
	,CAST(BINTOSTR(CAST("MensajeEntrada" as binary)) as varchar) AS "MensajeEntrada"
	,"FechaHoraRecepcion"
	,"FechaSistema"	
	,"IdEmpresa"
FROM "BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP"
WHERE "IdEmpresa"      = :IdEmpresa
AND   "TipoTrasaccion" = :TipoTrasaccion  -- CONSULTA | PAGO | EXTORNO
AND   "RqUID"          IN (:RqUIDs)       -- Ej.: '54d461s7-74rd-426s-wa54-14a4c894gd6s'
ORDER BY "IdMensajeTransaccion" DESC;     -- Descendente por si alguna razon existe 2 RqUID iguales el servicio tome solo el primero (ultimo ingresado)