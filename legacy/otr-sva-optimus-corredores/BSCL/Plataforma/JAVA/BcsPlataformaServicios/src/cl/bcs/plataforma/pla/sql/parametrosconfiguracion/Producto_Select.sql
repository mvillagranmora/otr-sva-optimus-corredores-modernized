/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CGUZMAN - BCS
fecha		: 04/02/2021
***********************************************************/
SELECT "Valor" FROM "BSCL.Plataforma.BD::PRO.Configuracion" 
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa