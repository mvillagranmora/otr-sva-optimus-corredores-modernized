SELECT
	'' AS "IdBolsa",
	'' AS "EsVentaCorta",
	TO_VARCHAR(CAR."FolioCartera") AS "Folio",
	CAR."IdMonedaPrincipal" ,
	CAR."IdMonedaSecundaria" ,
	CAR."IdEmisor" ,
	'' AS "Nemotecnico" ,
	CAR."IdTipoOperacion" ,
	CIC."ValorIndicador" AS "RelacionadoCorredora",
	TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento" ,
	TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion" ,
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, OPE."FechaVencimiento")) AS "Plazo",
	OPE."IdMonedaPrincipal" ,
	OPE."IdMonedaSecundaria" ,
	CAR."IdTipoInstrumento",
	'' AS "IdClasificacionTipoInstrumento" , --original
	CAR."INGMontoMonedaPrincipal" AS "Monto",
	CAR."INGTasaValorRazonableActivo" AS "Tasa",
	'NOCIONAL' AS "Grupo",
	'FORWARD' AS "Producto"
FROM
	"BSCL.Mesa.BD::MES.Operacion" OPE
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC ON CIC."IdCliente" = OPE."IdCliente" AND CIC."CodigoTipoIndicador" = 'RELCORR'
LEFT JOIN
	"BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdOperacion" = OPE."IdOperacion" 
WHERE 
	OPE."IdTipoProducto" = 'MEFW'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdTipoInstrumento" = 'FWD'
UNION ALL
SELECT
	'' AS "IdBolsa",
	'' AS "EsVentaCorta",
	TO_VARCHAR(CAR."FolioCartera") AS "Folio",
	CAR."IdMonedaPrincipal" ,
	CAR."IdMonedaSecundaria" ,
	CAR."IdEmisor" ,
	'' AS "Nemotecnico" ,
	CAR."IdTipoOperacion" ,
	CIC."ValorIndicador" AS "RelacionadoCorredora", 
	TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento" ,
	TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion" ,
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, OPE."FechaVencimiento")) AS "Plazo",
	OPE."IdMonedaPrincipal" ,
	OPE."IdMonedaSecundaria" ,
	CAR."IdTipoInstrumento",
	'' AS "IdClasificacionTipoInstrumento" ,
	CAR."INGMontoValorRazonablePasivo" AS "Monto",
	CAR."INGTasaValorRazonablePasivo" AS "Tasa",
	'OBLIG' AS "Grupo",
	'FORWARD' AS "Producto"
FROM
	"BSCL.Mesa.BD::MES.Operacion" OPE
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC ON CIC."IdCliente" = OPE."IdCliente" AND CIC."CodigoTipoIndicador" = 'RELCORR'
LEFT JOIN
	"BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdOperacion" = OPE."IdOperacion" 
WHERE 
	OPE."IdTipoProducto" = 'MEFW'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdTipoInstrumento" = 'FWD'
UNION ALL
SELECT
	'' AS "IdBolsa",
	'' AS "EsVentaCorta",
	TO_VARCHAR(CAR."FolioCartera") AS "Folio",
	CAR."IdMonedaPrincipal" ,
	CAR."IdMonedaSecundaria" ,
	CAR."IdEmisor" ,
	'' AS "Nemotecnico" ,
	CAR."IdTipoOperacion" ,
	CIC."ValorIndicador" AS "RelacionadoCorredora", 
	TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento" ,
	TO_VARCHAR(OPE."FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion" ,
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, OPE."FechaVencimiento")) AS "Plazo",
	OPE."IdMonedaPrincipal" ,
	OPE."IdMonedaSecundaria" ,
	CAR."IdTipoInstrumento",
	'' AS "IdClasificacionTipoInstrumento" , --original
	CAR."INGMontoValorRazonableActivo" AS "Monto",
	CAR."INGTasaValorRazonablePasivo" AS "Tasa",
	'DERECHO' AS "Grupo",
	'FORWARD' AS "Producto"
FROM
	"BSCL.Mesa.BD::MES.Operacion" OPE
LEFT JOIN 
	"BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" CIC ON CIC."IdCliente" = OPE."IdCliente" AND CIC."CodigoTipoIndicador" = 'RELCORR'
LEFT JOIN
	"BSCL.Custodia.BD::CUS.Cartera" CAR ON CAR."IdOperacion" = OPE."IdOperacion" 
WHERE 
	OPE."IdTipoProducto" = 'MEFW'
	AND CAR."IdEstado" = 'VIG'
	AND CAR."IdTipoInstrumento" = 'FWD';