/********************************************************** 
Proposito	: select salida
autor		: RU
fecha		: 07/02/2017
***********************************************************/
SELECT "IdRegistro","IdTrabajo","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdTipoTasa","Tasa","IdTipoPrecio","Precio","CodigoFuente","MensajeFuente",
"FechaFuente","MensajeError","IdMoneda"
FROM "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Salida_PreciosTasasInstrumentos"
WHERE "IdTrabajo" = :IdTrabajo;
	 