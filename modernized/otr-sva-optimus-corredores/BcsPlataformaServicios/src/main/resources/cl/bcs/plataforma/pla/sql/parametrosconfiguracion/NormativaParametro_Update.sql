/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CHERNANDEZ - BCS
fecha		: 31/07/2017
***********************************************************/
UPDATE "BSCL.Normativa.BD::DOC.Parametro" 
SET "Valor" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "Codigo" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa