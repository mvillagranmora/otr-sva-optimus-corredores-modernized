INSERT INTO "BSCL.Orden.BD::ORD.Transaccion"
("IdTransaccion","Folio","FechaSistemaCreacion","HoraSistemaCreacion","FechaOperacion","FechaLiquidacion","FechaEmision","FechaVencimiento",
"FechaIngresoOferta","FechaIngresoRegistro","IdSistemaBursatil","EsCuentaPropia","EsFungible","EsAPlazo","EsVentaCorta",
"CodigoOperadorContraparte","CodigoOperadorCorredor","IdEmisor","Cantidad","Monto","Precio",
"Tasa","Nemotecnico","NemotecnicoSVS","ValorRescate","IdEstado","IdTransaccionPlazo","IdTransaccionOd","IdTipoInstrumento","IdClasificacionTipoInstrumento",
"IdTipoReajuste","IdEmpleado","IdTipoOperacion","IdCondicionDeLiquidacion","IdMoneda","IdCorredorContraparte","IdEmpresa","IdBolsa","IdLibroOrdenes","IdInstrumento",
"NemotecnicoSubyacente","IdInstrumentoSubyacente","IdMonedaSubyacente","FechaParidad","ValorParidad","FechaTipoCambio","ValorTipoCambio",
"Plazo","FechaModificacionOperacion","MarcaNegociacion","IndicadorDCV","EsBilateral","EsFueraRueda","IdCorredorLocal","IdProgramaProcesaAutomatico",
"TipoCambioCorresponsal","IdCorredorCorresponsal","MontoLiquidacionCorresponsal","IdMonedaLiquidacionCorresponsal","MejorPrecioCompra","MejorPrecioVenta",
"BaseTasa","TipoInteres","IndicadorOrdenDirecta","PagaderoEn","IdMonedaEmision","Intereses","Reajustes","GastosCorresponsal","FechaPago","IdBoveda","IdCamara","Reservado","IdUsuarioReserva", "FlujoOrigen",
"PrecioRaiz","MontoRaiz","IdMonedaRaiz","IdInstrumentoNegociacion","MotivoReserva")
VALUES
(:IdTransaccion,:Folio,:FechaSistemaCreacion,:HoraSistemaCreacion,:FechaOperacion,:FechaLiquidacion,:FechaEmision,:FechaVencimiento,
:FechaIngresoOferta,CURRENT_UTCTIMESTAMP,:IdSistemaBursatil,:EsCuentaPropia,:EsFungible,:EsAPlazo,:EsVentaCorta,
:CodigoOperadorContraparte,:CodigoOperadorCorredor,:IdEmisor,:Cantidad,:Monto,:Precio,
:Tasa,:Nemotecnico,:NemotecnicoSVS,:ValorRescate,:IdEstado,:IdTransaccionPlazo,:IdTransaccionOd,:IdTipoInstrumento,:IdClasificacionTipoInstrumento,
:IdTipoReajuste,:IdEmpleado,:IdTipoOperacion,:IdCondicionDeLiquidacion,:IdMoneda,:IdCorredoraContraparte,:IdEmpresa,:IdBolsa,:IdLibroOrdenes,:IdInstrumento,
:NemotecnicoSubyacente,:IdInstrumentoSubyacente,:IdMonedaSubyacente,:FechaParidad,:ValorParidad,:FechaTipoCambio,:ValorTipoCambio,
:Plazo,:FechaModificacionOperacion,:MarcaNegociacion,:IndicadorDCV,:EsBilateral,:EsFueraRueda,:IdCorredoraLocal,:IdProgramaProcesaAutomatico,
:TipoCambioCorresponsal,:IdCorredoraCorresponsal,:MontoLiquidacionCorresponsal,:IdMonedaLiquidacionCorresponsal,:MejorPrecioCompra,:MejorPrecioVenta,
:BaseTasa,:TipoInteres,:IndicadorOrdenDirecta,'XYZ',:IdMonedaEmision,:Intereses,:Reajustes,:GastosCorresponsal,:FechaLiquidacion,:IdBoveda,:IdCamara,:Reservado,:IdUsuarioReserva, :FlujoOrigen,
:PrecioRaiz,:MontoRaiz,:IdMonedaRaiz,:IdInstrumentoNegociacion,:MotivoReserva)