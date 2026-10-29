INSERT INTO "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso"
("IdTrabajo","IdInterno","IdTipoVigencia","IdBoveda","IdFormaCustodia",
"IdPorcentajeVisibilidad","IdTipoPrecio","IdPortafolio","IdTipoCanalIngreso","IdTipoReajuste",
"IdBolsa","IdInstrumento","IdInstrumentoSubyacente","IdTipoCanalDistribucion","IdTipoOperacion","IdCondicionDeLiquidacion")
VALUES (:IdTrabajo,:IdInterno,:IdTipoVigencia,:IdBoveda,:IdFormaCustodia,
:IdPorcentajeVisibilidad,:IdTipoPrecio,:IdPortafolio,:IdTipoCanalIngreso,:IdTipoReajuste,
:IdBolsa,:IdInstrumento,:IdInstrumentoSubyacente,:IdTipoCanalDistribucion,:IdTipoOperacion,:IdCondicionDeLiquidacion);

