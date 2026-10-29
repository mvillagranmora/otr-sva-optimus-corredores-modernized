SELECT "DocumentoIdentidad","PortafolioDerivados","IdTipoOperacion","Serie","EsPutEsCall","CodigoCorredora"
	,"CodigoClasificacion","IdTipoInstrumento","ModalidadLiquidacion","ModalidadEjercicio","EstiloOpcion","Fecha"
	,"FechaVencimiento","Subyacente","Strike","PrecioSubyacente","PerdidaGanancia","SaldoEjercicio","SaldoNeto"
	,"SaldoPrima","SaldoSorteo","IdFondo","IdFondoCliente","LadoDiaAnterior","LadoDia","LadoNeto","LadoTr","LotePadron"
	,"ContratosAbiertosNeto","ContratosAbiertosDiaAnterior","ContratosAbiertosDia","ContratosEjercidos","ContratosSorteados"
	,"CantidadContratosTr","Observacion","NumeroReferencia","IdEstado","Sorteado"
FROM "BSCL.Custodia.BD::CUS.SorteoContratos"
WHERE "Sorteado" = upper('SI') AND ("ContratosSorteados" > 0 OR "ContratosEjercidos" > 0) AND "Serie" IN (:series) AND "Fecha" = :fechaSorteo