/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: Marcelo Yévenes - BCS
fecha		: 07/12/2023
***********************************************************/
UPDATE "BSCL.Plataforma.BD::LIM.Configuracion" 
SET "Valor" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "Codigo" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa