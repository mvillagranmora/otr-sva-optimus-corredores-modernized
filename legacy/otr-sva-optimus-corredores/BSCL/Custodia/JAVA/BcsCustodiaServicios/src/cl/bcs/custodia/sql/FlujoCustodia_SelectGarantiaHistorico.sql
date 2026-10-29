SELECT
	"BSCL.Custodia.BD.Secuencias::DatosFlujoCustodia".NEXTVAL AS "IdDatosFlujoCustodia",
--	:p_out_KeyFlujoCustodia AS "KeyFlujoCustodia",
	MZU."IdUsoFlujoCustodia",
	MZU."UsoTipoOperacion",
	MZU."BusquedaEspecifica",
	MZU."IdTipoInstrumentoEspecifico",
--	I."ItemCalculo",
--	I."ItemOrigen",
--	:p_FechaSaldo AS "FechaEjecucion",
	GAR."IdClasificacionTipoInstrumento",
	GAR."IdTipoInstrumento",
	GAR."IdInstrumento",
	GAR."IdEmisor",
	GAR."Nemotecnico",
	GAR."NemoSVS",
	GAR."CorteMinimo",
	GAR."IdMonedaInstrumento",
	GAR."IdTipoReajuste",
	GAR."FechaEmisionInstrumento",
	GAR."FechaVencimientoInstrumento",
	GAR."CodigoIFRS",
	GAR."EsFungible",
	'' AS "TipoCarteraCorredor",
	CAR."IdBoveda",
	CAR."IdBolsa",
	CAR."IdTipoOperacion",
	CAR."IdMesa",
	GAR."INGFechaLiquidacion" as "FechaLiquidacion",
	GAR."INGCantidad" as "Cantidad",
	GAR."INGValorRescate" as "ValorRescate",
	GAR."INGPrecioGarantia" as "PrecioCompra",
	GAR."INGTasaGarantia" as "TasaCompra",
	GAR."INGMontoCompraActualMonedaInstrumento" as "MontoATasaCompra",
	GAR."INGPrecioMercadoActual" as "PrecioMercado",
	GAR."INGTasaMercadoActual" as "TasaMercado",
	GAR."INGMontoMercadoActualMonedaInstrumento" as "MontoATasaMercado",
	case when  CLIGta."IdCliente" is null then GAR."IdCliente" else CLIGta."IdCliente" 		END AS "IdCliente" ,
	case when  CAR."IdPortafolio" is null then GAR."IdPortafolio" else CAR."IdPortafolio" 	END AS "IdPortafolio" ,
--	(CASE WHEN GAR."INGFechaLiquidacion" >= :v_FechaMinima THEN GAR."INGFechaLiquidacion" ELSE :v_FechaMinima END) AS "FechaLiquidacionProceso",
	GAR."IdGarantia" as "IdRegistro",
	GAR."FolioGarantia" as "FolioRegistro",
	GAR."IdConcepto",
--	:p_ValorizarFlujo AS "ValorizarFlujo",
	'S' AS "RequiereValorizacion", -- CASE WHEN GAR."IdClasificacionTipoInstrumento" = 'RV' THEN 'N' ELSE 'S' END AS "RequiereValorizacion"
	CAR."IdRegimenFondoMutuo",
	RFM."GrupoRegimen",
	CAR."FechaOrigenCompra",
	GAR."IdContraparteGarantia" AS "IdContraparte"
FROM "BSCL.Custodia.BD::HIS.Garantia" AS GAR
	INNER JOIN "BSCL.Custodia.BD::HIS.Cartera" AS CAR
		ON CAR."IdCartera" = GAR."IdCarteraOrigen" AND CAR."FechaRespaldo" = GAR."FechaRespaldo"
	INNER JOIN "BSCL.Custodia.BD::CUS.FlujoCustodia_MatrizUso" as MZU
		ON ((MZU."IdCliente" IS NULL OR GAR."IdCliente" = MZU."IdCliente")
			AND (MZU."IdPortafolio" IS NULL OR GAR."IdPortafolio" = MZU."IdPortafolio")
			AND (MZU."IdClasificacionTipoInstrumento" IS NULL OR GAR."IdClasificacionTipoInstrumento" = MZU."IdClasificacionTipoInstrumento")
			AND (MZU."IdTipoInstrumento" IS NULL OR GAR."IdTipoInstrumento" = MZU."IdTipoInstrumento")
			AND (MZU."IdInstrumento" IS NULL OR GAR."IdInstrumento" = MZU."IdInstrumento")
			AND (MZU."IdEmisor" IS NULL OR GAR."IdEmisor" = MZU."IdEmisor")
			AND (MZU."FechaVencimiento" IS NULL OR GAR."FechaVencimientoInstrumento" = MZU."FechaVencimiento")
			AND (MZU."IdTipoReajuste" IS NULL OR GAR."IdTipoReajuste" = MZU."IdTipoReajuste")
			AND (MZU."EsFungible" IS NULL OR GAR."EsFungible" = MZU."EsFungible")
			AND (MZU."IdBoveda" IS NULL OR CAR."IdBoveda" = MZU."IdBoveda")
			AND (MZU."IdRegimenFondoMutuo" IS NULL OR CAR."IdRegimenFondoMutuo" = MZU."IdRegimenFondoMutuo")
		   )
	LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS
		ON INS."IdInstrumento" = GAR."IdInstrumento"
	INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TOPER
		ON TOPER."IdTipoOperacion" = CAR."IdTipoOperacion"
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" AS TOTI
		ON TOTI."IdTipoOperacion" = MZU."UsoTipoOperacion"
			AND TOTI."IdTipoInstrumento" = GAR."IdTipoInstrumento"
	LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenFondoMutuo" RFM
		ON CAR."IdRegimenFondoMutuo" = RFM."IdRegimenFondoMutuo"
			AND RFM."IdEstado" = 'ACT'
  	LEFT join "BSCL.SocioNegocio.BD::CLI.Cliente" CLIGta ON CLIGta."IdCliente" = GAR."IdCliente"
WHERE GAR."IdEstado" = 'VIG'
	AND GAR."IdOrigenGarantia" = 'CUS'
	AND GAR."IdEmpresa" = :IdEmpresa			  
	AND GAR."INGFechaLiquidacion" <= :FechaMaxima
	AND GAR."FechaRespaldo" = :FechaMaxima
	AND MZU."IdTrabajo" = :IdTrabajo;