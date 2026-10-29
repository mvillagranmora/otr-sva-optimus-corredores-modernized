
/********************************************************** 
Proposito	: DELETE ASOCIACIONES
autor		: CSI
fecha		: 01/05/2017
***********************************************************/

DELETE FROM "BSCL.Orden.BD::ORD.OrdenAsociada" 
WHERE "IdOrdenAsociada" = :IdOrdenAsociada
