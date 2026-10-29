/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CHERNANDEZ - BCS
fecha		: 31/07/2017
***********************************************************/
UPDATE "BSCL.FondoMutuo.BD::FMM.Parametro" 
SET "ValorParametro" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "NombreTipoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa