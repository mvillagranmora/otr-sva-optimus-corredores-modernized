SELECT 
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
	INS."IdClasificacionTipoInstrumento",
	INS."IdTipoInstrumento",
	CAR."IdInstrumento",
	INS."TipoValorizacion",
	'CUSTODIA_NEGATIVA' AS "ItemOrigen",
	'' AS "ItemCalculo", 
	'VENTA' AS "Accion", 
	CAR."IdTipoProducto",	
	CAR."IdEmisor",
	CAR."Nemotecnico",
	CAR."NemoSVS",
	CAR."CorteMinimo",
	CAR."IdMonedaInstrumento",
	CAR."IdMonedaEmision" AS "IdMonedaEmision",
	CAR."IdTipoReajuste",
	ins."FechaEmision" AS "FechaEmisionInstrumento",
	ins."FechaVencimiento" AS "FechaVencimientoInstrumento",
	CAR."CodigoIFRS",
	CAR."EsFungible",
	'' AS "TipoCarteraCorredor",
	CAR."IdBoveda",
	CAR."IdBolsa",				
	CAR."IdTipoOperacion",
	CAR."IdMesa",
	CAR."FechaLiquidacion" AS "FechaLiquidacion",
	-1 * CAR."Cantidad" AS "Cantidad",
	-1 * CAR."Cantidad" AS "CantidadProceso",
	-1 * CAR."ValorRescate" AS "ValorRescate",
	CAR."PrecioOperacion" AS "PrecioCompra",
	CAR."TasaOperacion" AS "TasaCompra",
	CAR."MontoOperacionMonedaInstrumento"  AS "MontoATasaCompra",
	CAR."PrecioMercado"  AS "PrecioMercado",
	CAR."TasaMercado"  AS "TasaMercado",
	CAR."MontoMercadoMonedaInstrumento" AS "MontoATasaMercado",
	CAR."IdCliente",
	CAR."IdPortafolio",
	CAR."IdCarteraNegativa" AS "IdRegistro",
	CAR."IdCarteraNegativa" AS "FolioRegistro",
	CAR."IdConcepto",
	'S' AS "RequiereValorizacion",
	'' AS "IdRegimenFondoMutuo",
	'' AS "GrupoRegimen",
	CAR."FechaOperacion",
	CAR."IdCorredoraContraparte" AS "IdCorredorContraparte"
FROM "BSCL.Custodia.BD::CUS.CarteraNegativa" AS CAR 
LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" AS INS ON INS."IdInstrumento" = CAR."IdInstrumento" 
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI ON TOTI."IdTipoOperacion" = :UsoTipoOperacion
WHERE CAR."IdEstado" = 'VIG'
AND INS."IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
    AND INS."IdTipoInstrumento" = :IdTipoInstrumento
    AND CAR."IdInstrumento" = :IdInstrumento
	AND CAR."IdEmpresa" = :IdEmpresa
	AND CAR."FechaLiquidacion" >= :FechaMaxima
	AND CAR."FechaOperacion" <= :FechaMaxima;