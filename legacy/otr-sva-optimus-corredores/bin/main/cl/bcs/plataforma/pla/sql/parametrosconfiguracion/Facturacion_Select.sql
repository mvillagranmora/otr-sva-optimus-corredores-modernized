/********************************************************** 
Proposito	: Actualizar parametros de configuracion
autor		: CHERNANDEZ - BCS
fecha		: 31/07/2017
***********************************************************/
SELECT "Valor" FROM "BSCL.Facturacion.BD::FAC.ConfiguracionFacturacion" 
WHERE "CodigoParametro" = :p_Codigo
AND "IdEmpresa" = :p_IdEmpresa