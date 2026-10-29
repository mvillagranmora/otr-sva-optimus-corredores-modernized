/********************************************************** 
Proposito	: Script para actualizar transaccion
autor		: RU
fecha		: 01/02/2017
***********************************************************/
UPDATE "BSCL.Orden.BD::ORD.Transaccion" 
SET 
"MarcaNegociacion" = UPPER(:MarcaNegociacion)
WHERE "IdTransaccion" = :IdTransaccion;
