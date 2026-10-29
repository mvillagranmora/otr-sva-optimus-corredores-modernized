WITH cartera AS (
SELECT
	CAR."IdBolsa" ,
	'N' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	CAR."IdClasificacionTipoInstrumento" ,
	CAR."IdTipoInstrumento" ,
	CAR."IdEmisor" ,
	CAR."Nemotecnico", 
	'ACN' AS "Grupo",
	'ACCIONES' AS "Producto",
	CAR."IdMonedaInstrumento" ,
	SUM(CAR."INGCantidad") AS "Cantidad" ,
	SUM(CAR."INGMontoATasaMercadoActual") AS "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
WHERE
	CAR."IdEstado" = 'VIG'
	AND "IdClasificacionTipoInstrumento" = 'RV'
	AND "IdTipoInstrumento" NOT IN ('PRT', 'PRF', 'EMP', 'ERF', 'SIM', 'PAC', 'REP', 'RRV', 'RRF', 'FWD', 'FDO', 'FIP', 'FBC', 'FIC', 'FUF', 'IOA', 'IOE', 'IOI', 'IOD', 'IOB', 'IOC', 'IOU', 'IOM', 'IOF', 'FBO', 'DER')
	AND "IdCliente" = :IdCliente
	AND "IdConcepto" NOT IN ('CMSIMU','')
GROUP BY 
	CAR."IdBolsa",
	CAR."TipoCarteraCorredor",
	CAR."IdClasificacionTipoInstrumento",
	CAR."IdTipoInstrumento" ,
	CAR."IdEmisor" ,
	CAR."Nemotecnico",
	CAR."IdMonedaInstrumento"
), garantia AS (
SELECT
	CAR."IdBolsa" ,
	'N' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	GAR."IdClasificacionTipoInstrumento" ,
	GAR."IdTipoInstrumento" ,
	CAR."IdEmisor" ,
	GAR."Nemotecnico" ,
	'ACN' AS "Grupo",
	'ACCIONES' AS "Producto",
	GAR."IdMonedaInstrumento" ,
	SUM(GAR."INGCantidad") AS "Cantidad",
	SUM(GAR."INGMontoATasaMercadoActual") AS "Monto"
FROM "BSCL.Custodia.BD::GTA.Garantia" GAR 
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdCartera" = GAR."IdCarteraOrigen" 
WHERE
	CAR."IdCliente" = :IdCliente
	AND GAR."IdClasificacionTipoInstrumento" = 'RV'
	AND GAR."IdTipoInstrumento" = 'ACN'
	AND GAR."IdEstado" = 'VIG'
	AND CAR."IdConcepto" NOT IN ('CMSIMU','')
GROUP BY 
	CAR."IdBolsa" ,
	CAR."TipoCarteraCorredor" ,
	GAR."IdClasificacionTipoInstrumento" ,
	GAR."IdTipoInstrumento" ,
	CAR."IdEmisor" ,
	GAR."Nemotecnico" ,
	GAR."IdMonedaInstrumento"
), compra_plazo AS (
SELECT
	CAR."IdBolsa" ,
	'N' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	SBY."IdClasificacionTipoInstrumento" ,
	SBY."IdTipoInstrumento" ,
	SBY."IdEmisor" ,
	SBY."Nemotecnico" ,
	'ACN' AS "Grupo",
	'ACCIONES' AS "Producto",
	SBY."IdMonedaInstrumento" ,
	SUM(SBY."INGCantidad")  AS "Cantidad",
	SUM(SBY."INGMontoATasaMercadoActual" ) AS "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SBY ON SBY."FolioCarteraRelacionada" = CAR."FolioCartera" 
WHERE
	CAR."IdTipoOperacion" = 'VENSIM'
	AND CAR."IdTipoInstrumento" = 'SIM'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdCliente" = :IdCliente
	AND CAR."IdConcepto" NOT IN ('CMSIMU','')
GROUP BY 
	CAR."IdBolsa" ,
	CAR."TipoCarteraCorredor" ,
	SBY."IdClasificacionTipoInstrumento" ,
	SBY."IdTipoInstrumento" ,
	SBY."IdEmisor" ,
	SBY."Nemotecnico" ,
	SBY."IdMonedaInstrumento" 
), venta_plazo AS (
SELECT
	CAR."IdBolsa" ,
	'N' AS "EsVentaCorta",
	CAR."TipoCarteraCorredor" ,
	SBY."IdClasificacionTipoInstrumento" ,
	SBY."IdTipoInstrumento" ,
	SBY."IdEmisor" ,
	SBY."Nemotecnico" ,
	'ACN' AS "Grupo",
	'ACCIONES' AS "Producto",
	SBY."IdMonedaInstrumento" ,
	SUM(SBY."INGCantidad")  AS "Cantidad",
	SUM(SBY."INGMontoATasaMercadoActual" ) AS "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SBY ON SBY."FolioCarteraRelacionada" = CAR."FolioCartera" 
WHERE
	CAR."IdTipoOperacion" = 'COMSIM'
	AND CAR."IdTipoInstrumento" = 'SIM'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdConcepto" NOT IN ('CMSIMU','')
GROUP BY 
	CAR."IdBolsa" ,
	CAR."TipoCarteraCorredor" ,
	SBY."IdClasificacionTipoInstrumento" ,
	SBY."IdTipoInstrumento" ,
	SBY."IdEmisor" ,
	SBY."Nemotecnico" ,
	SBY."IdMonedaInstrumento" 
), datos_agrupados AS (
	SELECT 
		CAR."IdBolsa",
		CAR."EsVentaCorta",
		CAR."TipoCarteraCorredor",
		CAR."IdClasificacionTipoInstrumento",
		CAR."IdTipoInstrumento",
		CAR."IdEmisor",
		CAR."Nemotecnico",
		CAR."Grupo",
		CAR."Producto",
		CAR."IdMonedaInstrumento",
		CAR."Monto",
		'CAR' AS "TablaOrigen"
	FROM cartera CAR 
	UNION ALL
	SELECT 
		GAR."IdBolsa",
		GAR."EsVentaCorta",
		GAR."TipoCarteraCorredor",
		GAR."IdClasificacionTipoInstrumento",
		GAR."IdTipoInstrumento",
		GAR."IdEmisor",
		GAR."Nemotecnico",
		GAR."Grupo",
		GAR."Producto",
		GAR."IdMonedaInstrumento",
		GAR."Monto",
		'GAR' AS "TablaOrigen"
	FROM 
		garantia GAR
	UNION ALL
	SELECT 
		CP."IdBolsa",
		CP."EsVentaCorta",
		CP."TipoCarteraCorredor",
		CP."IdClasificacionTipoInstrumento",
		CP."IdTipoInstrumento",
		CP."IdEmisor",
		CP."Nemotecnico",
		CP."Grupo",
		CP."Producto",
		CP."IdMonedaInstrumento",
		CP."Monto",
		'CP' AS "TablaOrigen"
	FROM
		compra_plazo CP
	UNION ALL 
	SELECT 
		VP."IdBolsa",
		VP."EsVentaCorta",
		VP."TipoCarteraCorredor",
		VP."IdClasificacionTipoInstrumento",
		VP."IdTipoInstrumento",
		VP."IdEmisor",
		VP."Nemotecnico",
		VP."Grupo",
		VP."Producto",
		VP."IdMonedaInstrumento",
		VP."Monto" * -1 AS "Monto",
		'VP' AS "TablaOrigen"
	FROM
		venta_plazo VP
)
SELECT
	DA."IdBolsa",
	DA."EsVentaCorta",
	DA."TipoCarteraCorredor",
	DA."IdClasificacionTipoInstrumento",
	'' AS "IdTipoInstrumento",
	'' AS "IdEmisor",
	'' AS "RelacionadoCorredora",
	'' AS "IdTipoOperacion" ,
	DI."DocumentoIdentidad" AS "DocumentoIdentidadEmisor",
	'' AS "Nemotecnico",
	DA."Nemotecnico" AS "NemoBasilea",
	DA."Grupo",
	DA."Producto",
	DA."IdMonedaInstrumento",
	ROUND(SUM(DA."Monto"),0) AS "Monto"
FROM datos_agrupados DA 
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON EMI."IdEmisor" = DA."IdEmisor"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = EMI."IdPersona"
GROUP BY 
	DA."IdBolsa",
	DA."EsVentaCorta",
	DA."TipoCarteraCorredor",
	DA."IdClasificacionTipoInstrumento",
	DA."IdTipoInstrumento",
	DA."IdEmisor",
	DI."DocumentoIdentidad" ,
	DA."Nemotecnico",
	DA."Grupo",
	DA."Producto",
	DA."IdMonedaInstrumento"
HAVING SUM(DA."Monto") > 0;