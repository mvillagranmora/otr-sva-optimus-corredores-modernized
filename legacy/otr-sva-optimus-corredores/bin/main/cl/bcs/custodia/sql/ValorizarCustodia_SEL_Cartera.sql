SELECT  DISTINCT
          cc."IdCartera"
		, "IdEmisor"
		,"INGCantidad"
		,"INGValorRescate"
        ,"INGMontoATasaCompraActual"
        ,"INGMontoATasaMercadoActual"
        ,"FechaOperacion"
        ,"FechaVencimientoOperacion"
        ,"FechaVencimientoInstrumento"
        ,"FechaEmisionInstrumento"
        ,"IdCondicionLiquidacion"
        ,"FechaLiquidacion"
FROM "BSCL.Custodia.BD::CUS.ValorizarCustodia_Cartera" cc
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" c on cc."IdCartera" = c."IdCartera";
