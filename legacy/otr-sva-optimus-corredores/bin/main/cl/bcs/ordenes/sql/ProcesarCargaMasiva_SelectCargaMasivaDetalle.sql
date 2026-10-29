
SELECT
	CD."IdCargaMasivaDetalle",
	CD."IdCargaMasiva",
	CD."DocumentoIdentidad",
	CD."CodigoPortafolio",
	CD."TipoOperacion",
	CD."IdMecanismoCaptacion",
	CD."Cantidad",	
	CD."Nemotecnico",
	CD."Precio",
	CD."IdCondicionDeLiquidacion",
	CD."IdTipoVigenciaOrden",
	CD."FechaVigencia",
	CD."IdFormaCustodia",
	CD."IdBolsa",
	CD."IdBoveda",
	CD."IdTipoPrecioOrden",
	CD."IdPersona",
	CD."IdPortafolio",
	CD."TipoInstruccion",
	CD."Emisor",
	CD."FechaVencimiento",
	CD."ValorRescate",
	CD."FechaTrade",
	CD."Intermediario",
	CD."FolioFactura",
	CD."MontoLiquidacion",
	CD."Tasa",
	CM."CodigoTipoCargaMasiva",
	CD."TotalCobros",
	CD."IdTipoReajuste"
FROM "BSCL.Orden.BD::CAR.CargaMasivaDetalle" CD
LEFT JOIN "BSCL.Orden.BD::CAR.CargaMasiva" CM ON CM."IdCargaMasiva" = CD."IdCargaMasiva"
WHERE CD."IdCargaMasiva" IN (:IdsCargasMasivas);
			
