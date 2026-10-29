/********************************************************** 
Proposito	: call entregar precios y tasas
autor		: RU
fecha		: 07/02/2017
***********************************************************/
INSERT INTO "BSCL.Plataforma.BD::TAS.EntregarPreciosTasas_Entrada_DatosInstrumentos"
("IdRegistro","IdTrabajo","TipoCartera","IdUso","IdTipoCriterioTasa","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento","Nemotecnico","IdMoneda","FechaConsulta","PlazoResidual")
VALUES (:IdRegistro,:IdTrabajo,:TipoCartera,:IdUso,:IdTipoCriterioTasa,:IdClasificacionTipoInstrumento,:IdTipoInstrumento,:IdInstrumento,:Nemotecnico,:IdMoneda,:FechaConsulta,:PlazoResidual)