/********************************************************** 
Proposito	: Insertar nuevo registro para mensaje websocket
autor		: CSI - BCS
fecha		: 17/05/2017
***********************************************************/
INSERT INTO "BSCL.Plataforma.BD::PLA.MensajeWebsocket"(
  "IdEmpresa", 
  "FechaRegistro",
  "Evento",
  "DATA",
  "EsProcesado" 
)
VALUES(
    :IdEmpresa,
    CURRENT_UTCTIMESTAMP,
    :Evento,
    :DATA,
    'N'
)




