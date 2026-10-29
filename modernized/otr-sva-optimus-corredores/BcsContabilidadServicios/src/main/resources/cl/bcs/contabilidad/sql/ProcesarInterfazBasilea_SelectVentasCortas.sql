SELECT
	'' AS "IdBolsa" ,
	CMB."EsVentaCorta",
	CAR."TipoCarteraCorredor",
	'' AS "RelacionadoCorredora",
	'' AS "IdTipoOperacion" ,
	CAR."IdClasificacionTipoInstrumento",
	CAR."IdTipoInstrumento",
	'' AS "IdEmisor" ,
	SBY."IdEmisor" AS "Emisor",
	'' AS "Nemotecnico",
	SBY."Nemotecnico" AS "NemoBasilea",
	'ACN' AS "Grupo",
	'ACCIONES' AS "Producto",
	CAR."IdMonedaInstrumento" ,
	SUM(CMB."Monto")  AS "Monto",
	DI."DocumentoIdentidad"  AS "DocumentoIdentidadEmisor"
FROM "BSCL.Facturacion.BD::FAC.Comprobante" CMB
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CAR ON CMB."IdComprobante" = CAR."FolioComprobante"
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SBY ON SBY."FolioCarteraRelacionada" = CAR."FolioCartera"
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON EMI."IdEmisor" = SBY."IdEmisor" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = EMI."IdPersona" 
WHERE
	"EsVentaCorta" = 'S'
	AND CMB."IdCliente" = :IdCliente
	AND CAR."IdEstado" = 'VIG'
	AND YEAR(CAR."FechaOperacion") = YEAR(:FechaSistema)
	AND  MONTH(CAR."FechaOperacion") = MONTH(:FechaSistema)
	AND CAR."IdTipoInstrumento" IN ('PRT', 'PRF')
GROUP BY 
	CMB."EsVentaCorta",
	CAR."TipoCarteraCorredor",
	CAR."IdClasificacionTipoInstrumento",
	CAR."IdTipoInstrumento",
	SBY."IdEmisor" ,
	SBY."Nemotecnico",
	CAR."IdMonedaInstrumento" ,
	DI."DocumentoIdentidad"
HAVING 
	SUM(CMB."Monto") > 0;