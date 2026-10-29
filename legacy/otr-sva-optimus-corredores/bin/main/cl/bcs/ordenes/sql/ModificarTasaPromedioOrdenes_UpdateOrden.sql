
/********************************************************** 
Proposito	: MODIFICA TASA PROMEDIO REPORTADO EN ORDEN
autor		: CSI
fecha		: 01/05/2017
***********************************************************/

UPDATE "BSCL.Orden.BD::ORD.Orden" 
SET "TasaPromedioReporte" = :TasaPromedioReporte
WHERE "IdOrden" = :IdOrden
