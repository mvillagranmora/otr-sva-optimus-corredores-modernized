/********************************************************** 
Proposito	: Script para obtener mensajes de marca de negociacion de drop copy
autor		: RU
fecha		: 01/02/2017
***********************************************************/
SELECT "IdTransaccion"
FROM "BSCL.Orden.BD::ORD.Transaccion" tx
INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" toper on toper."IdTipoOperacion" = tx."IdTipoOperacion"
WHERE toper."AccionOrden" = :AccionOrden AND tx."Folio" = :Folio AND tx."FechaSistemaCreacion" = :FechaSistemaCreacion;