SELECT    
   "IdCartera",
   "IdPortafolio",
   "IdCliente",
   "IdClasificacionTipoInstrumento",
   "IdTipoInstrumento",
   "IdInstrumento",
   "IdEmisor",
   "IdMonedaInstrumento",
   "IdMonedaEmision",
   "IdMonedaAdicional",
   "IdTipoReajuste",
   "IdBoveda",
   "CuentaBoveda",
   "IdCuentaCustodioPortafolio",
   "IdBolsa",
   "IdTipoProducto",
   "IdCondicionLiquidacion",
   "IdTipoCanalIngreso",
   "IdTipoComprobante",
   "IdAgente",
   "IdSucursal",
   "IdUsuario",
   "IdOperacion",
   "IdRegimenFondoMutuo",
   "Nemotecnico",
   "IdEstado",
   "NemoSVS",
   "CodigoIFRS",
   "FolioCartera",
   "FolioComprobante",
   "FolioExternoReferencia",
   "Glosa",
   "EsFungible",
   "CorteMinimo",
   "Cantidad",
   "ValorRescate",
   "PrecioCompra",
   "TasaCompra",
   "TasaAgente",
   "MontoCompraMonedaInstrumento",
   "INGCantidad",
   "INGMontoCompraActualMonedaInstrumento",
   "INGValorRescate",
   "PlazoEnDiasOperacion",
   "FechaVencimientoInstrumento",
   "FechaEmisionInstrumento",
   "FechaLiquidacion",
   "FechaOperacion",
   "FechaComprobante"
FROM "BSCL.Custodia.BD::CUS.Cartera"
WHERE   "IdEstado" = 'VIG'
AND     "IdPortafolio" = :Portafolio
AND     "IdClasificacionTipoInstrumento" = :Mercado 
AND		"FechaLiquidacion" 	<= :FechaSistema                
AND     "FechaOperacion" 	<= :FechaSistema 
AND     "IdConcepto" NOT IN ('DIEVAC','EMILIB') -- CAEMPA, SE DEBE PREGUNTAR
AND 	"IdTipoInstrumento" NOT IN ('PAC', 'SIM', 'PRT')
AND     "IdCartera" NOT IN (SELECT "IdCarteraOrigen" FROM "BSCL.Custodia.BD::GTA.Garantia"  WHERE "IdEstado" = 'VIG' )
AND     "IdCartera" NOT IN (SELECT "IdCarteraOrigen" FROM "BSCL.Custodia.BD::CUS.Reserva"  WHERE "IdEstado" = 'VIG' )
AND (
  		("IdClasificacionTipoInstrumento" = 'IF' AND "EsFungible" = 'S')
  	OR
  		"IdClasificacionTipoInstrumento" <> 'IF'
	)
AND (:Nemotecnico IS NULL
     OR (:Mercado = 'IF' AND "NemoSVS" = :Nemotecnico)
     OR (:Mercado <> 'IF' AND "Nemotecnico" = :Nemotecnico)
    )
ORDER BY :Orden ;
  