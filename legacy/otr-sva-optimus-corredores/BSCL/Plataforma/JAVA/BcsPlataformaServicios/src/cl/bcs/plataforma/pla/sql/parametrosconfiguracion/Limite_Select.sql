/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: Marcelo Yévenes - BCS
fecha		: 07/12/2023
***********************************************************/
SELECT "Valor" FROM "BSCL.Plataforma.BD::LIM.Configuracion" 
WHERE "Codigo" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa