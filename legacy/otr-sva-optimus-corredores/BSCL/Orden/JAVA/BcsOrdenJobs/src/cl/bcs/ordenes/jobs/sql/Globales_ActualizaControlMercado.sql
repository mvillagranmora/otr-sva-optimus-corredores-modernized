/********************************************************** 
Proposito	: Script para actualizar registro de control mercado
autor		: RU
fecha		: 01/02/2017
***********************************************************/
UPDATE "BSCL.Orden.BD::ORD.ControlMercado" 
SET 
"Procesado" = :Procesado,
"FechaProcesamiento" = CURRENT_UTCTIMESTAMP, 
"Log" = :Log 
WHERE "IdMensaje1" = :IdMensaje1 AND "IdMensaje2" = :IdMensaje2;
