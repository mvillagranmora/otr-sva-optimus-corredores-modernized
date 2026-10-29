/**
 * Proposito: Guarda LOG con el mensaje de transacción recibido del BCP 
 * Autor: JVILLAGRAN - BCS
 * Fecha Creacion: 18/09/2017
 */ 

INSERT INTO "BSCL.Tesoreria.BD::LOG.MensajeTransaccionBCP" 
(
	"IdMensajeTransaccion",
	"RqUID",				  -- Id generado por BCP
	"TipoTrasaccion",         -- PAGO | EXTORNO | CONSULTA
	"MensajeEntrada",         -- XML recibido 
	"FechaHoraRecepcion",
   	"FechaSistema",
	"IdEmpresa"
)
VALUES
(
	"BSCL.Tesoreria.BD.Secuencias::LOG.MensajeTransaccionBCP".NEXTVAL,
	:RqUID,
	:TipoTrasaccion,
	:MensajeEntrada,
	CURRENT_UTCTIMESTAMP,   --  "FechaHoraRecepcion"
   	:FechaSistema,
    :IdEmpresa   	
);
