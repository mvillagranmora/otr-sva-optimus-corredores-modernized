/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CHERNANDEZ - BCS
fecha		: 31/07/2017
***********************************************************/
SELECT "ValorParametro" AS "Valor" FROM "BSCL.FondoMutuo.BD::FMM.Parametro" 
WHERE "NombreTipoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa