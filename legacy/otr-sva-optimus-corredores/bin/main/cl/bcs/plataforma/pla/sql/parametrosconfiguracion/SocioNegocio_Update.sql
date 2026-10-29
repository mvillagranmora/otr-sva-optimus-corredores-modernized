/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: SGONZALEZ - BCS
fecha		: 21/03/2018
***********************************************************/
UPDATE "BSCL.SocioDeNegocio.BD::SOC.ConfiguracionSocioNegocio" 
SET "Valor" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa
