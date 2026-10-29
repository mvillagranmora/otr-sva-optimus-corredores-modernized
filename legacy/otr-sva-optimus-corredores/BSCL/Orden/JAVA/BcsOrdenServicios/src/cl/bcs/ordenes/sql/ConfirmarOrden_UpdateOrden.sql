
/********************************************************** 
Proposito	: CONFIRMA/DESCONFIRMA ORDEN
autor		: CSI
fecha		: 01/05/2017
***********************************************************/

UPDATE "BSCL.Orden.BD::ORD.Orden" 
SET "EsConfirmada" = :EsConfirmada
WHERE "IdOrden" = :IdOrden
