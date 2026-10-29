/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CGUZMAN - BCS
fecha		: 04/02/2021
***********************************************************/
UPDATE "BSCL.Plataforma.BD::PRO.Configuracion" 
SET "Valor" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa