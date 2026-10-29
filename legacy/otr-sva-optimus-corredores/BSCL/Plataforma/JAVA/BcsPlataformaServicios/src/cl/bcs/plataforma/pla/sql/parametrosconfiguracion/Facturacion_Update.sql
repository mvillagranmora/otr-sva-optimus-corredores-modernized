/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CHERNANDEZ - BCS
fecha		: 31/07/2017
***********************************************************/
UPDATE "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" 
SET "Valor" = :p_Valor, "ModificadorParametro" = :p_Usuario, "FechaModificacion" = CURRENT_UTCTIMESTAMP
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa