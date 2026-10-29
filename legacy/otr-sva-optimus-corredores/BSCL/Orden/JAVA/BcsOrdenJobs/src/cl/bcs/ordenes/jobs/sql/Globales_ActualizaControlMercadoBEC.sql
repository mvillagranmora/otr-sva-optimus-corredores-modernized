UPDATE "BSCL.Orden.BD::ORD.ControlMercado" 
SET 
"Procesado" = :Procesado,
"FechaProcesamiento" = CURRENT_UTCTIMESTAMP, 
"Log" = :Log 
WHERE "IdMensaje1" = :IdMensaje1 AND "IdMensaje2" = :IdMensaje2;
