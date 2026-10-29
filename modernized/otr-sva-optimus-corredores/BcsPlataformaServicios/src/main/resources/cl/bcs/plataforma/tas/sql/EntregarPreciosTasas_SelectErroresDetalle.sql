/********************************************************** 
Proposito	: select errores
autor		: RU
fecha		: 07/02/2017
***********************************************************/
SELECT "Codigo","Descripcion","IdTrabajo"
FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_ErroresDetalle"
WHERE "IdTrabajo" = :IdTrabajo;