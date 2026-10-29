
/**************************************************************** 
Proposito	: Borra registros procesados de la tabla global de proceso
autor		: JVILLAGRAN - BCS
fecha		: 04/07/2018
*****************************************************************/

DELETE 
FROM "BSCL.Plataforma.BD::GBL.EntregarPreciosTasas_DatosInstrumentosProceso"
WHERE "IdRegistro" = :IdRegistro;