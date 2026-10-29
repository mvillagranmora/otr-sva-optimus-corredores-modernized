SELECT
	'' AS "IdBolsa" ,
	'' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	'' AS "RelacionadoCorredora",
	'' AS "IdTipoOperacion" ,
	TO_VARCHAR(CAR."FolioCartera") AS "Folio",
	CAR."INGTasaCompra" AS "Tasa",
	CAR."IdClasificacionTipoInstrumento" ,
	CAR."IdTipoInstrumento" ,
	CAR."IdTipoReajuste" ,
	TO_VARCHAR(CAR."FechaVencimientoInstrumento") AS "FechaVencimiento" ,
	TO_VARCHAR(CAR."FechaOperacion") AS "FechaOperacion" ,
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, CAR."FechaVencimientoInstrumento")) AS "Plazo",
	CASE
		WHEN CAR."IdEmisor" IN ('CENTRAL', 'TESORERIA', 'ESTADO', 'BBSCOTIABA', 'DESARROLLO', 'BBVA', 'BHIF') THEN CAR."IdEmisor" 
		ELSE ''
	END AS "IdEmisor",
	CAR."IdEmisor" AS "Emisor",
	CASE
		WHEN CAR."Nemotecnico" IN ('PDBC', 'PRBC', 'PDT', 'PRT') THEN CAR."Nemotecnico" 
		ELSE ''
	END AS "Nemotecnico", 
	'LIBRE' AS "Grupo",
	'RF-IF' AS "Producto",
	CAR."IdMonedaInstrumento" ,
	ROUND(SUM(CAR."INGMontoATasaMercadoActual"), 0) AS "Monto",
	DI."DocumentoIdentidad" AS "DocumentoIdentidadEmisor"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON EMI."IdEmisor" = CAR."IdEmisor" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = EMI."IdPersona"
WHERE
	CAR."IdEstado" = 'VIG'
	AND "IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND "IdTipoInstrumento" NOT IN ('PRT', 'PRF', 'EMP', 'ERF', 'SIM', 'PAC', 'REP', 'RRV', 'RRF', 'FWD', 'FDO', 'FIP', 'FBC', 'FIC', 'FUF', 'IOA', 'IOE', 'IOI', 'IOD', 'IOB', 'IOC', 'IOU', 'IOM', 'IOF', 'FBO', 'DER')
	AND "IdCliente" = :IdCliente
	AND CAR."TipoCarteraCorredor" <> 'CC'
	AND CAR."INGFechaOperacion" <= :FechaSistema
GROUP BY 
	CAR."TipoCarteraCorredor" ,
	CAR."FolioCartera",
	CAR."INGTasaCompra",
	CAR."IdClasificacionTipoInstrumento" ,
	CAR."IdTipoInstrumento" ,
	CAR."IdTipoReajuste" ,
	CAR."FechaVencimientoInstrumento" ,
	CAR."FechaOperacion" ,
	CAR."IdEmisor",
	CAR."Nemotecnico",
	CAR."IdMonedaInstrumento" ,
	DI."DocumentoIdentidad"
UNION ALL
SELECT
	'' AS "IdBolsa" ,
	'' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	'' AS "RelacionadoCorredora",
	'' AS "IdTipoOperacion" ,
	TO_VARCHAR(CAR."FolioCartera") AS "Folio",
	CAR."INGTasaCompra" AS "Tasa",
	GAR."IdClasificacionTipoInstrumento" ,
	GAR."IdTipoInstrumento" ,
	GAR."IdTipoReajuste" ,
	TO_VARCHAR(GAR."FechaVencimientoInstrumento" ) AS "FechaVencimiento",
	TO_VARCHAR(GAR."FechaOperacion") AS "FechaOperacion",
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, GAR."FechaVencimientoInstrumento")) AS "Plazo",
	CASE
		WHEN CAR."IdEmisor" IN ('CENTRAL', 'TESORERIA', 'ESTADO', 'BBSCOTIABA', 'DESARROLLO', 'BBVA', 'BHIF') THEN CAR."IdEmisor" 
		ELSE ''
	END AS "IdEmisor",
	CAR."IdEmisor" AS "Emisor",
	CASE
		WHEN GAR."Nemotecnico" IN ('PDBC', 'PRBC', 'PDT', 'PRT') THEN GAR."Nemotecnico" 
		ELSE ''
	END AS "Nemotecnico",
	'GARANTIZADA' AS "Grupo",
	'RF-IF' AS "Producto",
	GAR."IdMonedaInstrumento" ,
	ROUND(SUM(GAR."INGMontoATasaMercadoActual"),0) AS "Monto",
	DI."DocumentoIdentidad" AS "DocumentoIdentidadEmisor"
FROM "BSCL.Custodia.BD::GTA.Garantia" GAR 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" 
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON EMI."IdEmisor" = CAR."IdEmisor" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = EMI."IdPersona"
WHERE
	CAR."IdCliente" = :IdCliente
	AND GAR."IdClasificacionTipoInstrumento" IN ('RF', 'IF')
	AND GAR."IdEstado" = 'VIG'
	AND CAR."INGFechaOperacion" <= :FechaSistema
GROUP BY 
	CAR."TipoCarteraCorredor" ,
	CAR."FolioCartera",
	CAR."INGTasaCompra",
	GAR."IdClasificacionTipoInstrumento" ,
	GAR."IdTipoInstrumento" ,
	GAR."IdTipoReajuste" ,
	GAR."FechaVencimientoInstrumento" ,
	GAR."FechaOperacion",
	CAR."IdEmisor" ,
	GAR."Nemotecnico" ,
	GAR."IdMonedaInstrumento" ,
	DI."DocumentoIdentidad";