/********************************************************** 
Proposito	: Elimina DatosFlujoCustodia
autor		: RFCS
fecha		: 12/06/2017
***********************************************************/

DELETE FROM "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" WHERE "IdTrabajo" = :IdTrabajo;
