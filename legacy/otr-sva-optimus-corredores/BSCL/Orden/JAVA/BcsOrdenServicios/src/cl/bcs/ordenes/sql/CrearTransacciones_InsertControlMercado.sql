INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","FechaProcesamiento","Procesado","Log")
VALUES 
(:TipoMensaje,:FechaSistemaCreacion,:IdTransaccion,CURRENT_UTCTIMESTAMP,'0001-01-01 00:00:00','N','');


