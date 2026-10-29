/********************************************************** 
Proposito	: Obtener parametros de configuracion
autor		: SGONZALEZ - BCS
fecha		: 21/03/2018
***********************************************************/
SELECT "Valor" FROM "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" 
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa