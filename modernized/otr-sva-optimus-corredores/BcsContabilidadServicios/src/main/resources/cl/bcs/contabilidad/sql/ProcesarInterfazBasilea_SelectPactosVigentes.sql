SELECT 
	'' AS "IdBolsa" , 
	'' AS "EsVentaCorta",
	TO_VARCHAR(OP."IdOperacion") AS "Folio", --**
	CAR."TipoCarteraCorredor" ,
	'' AS "RelacionadoCorredora",
	'' AS "IdTipoOperacion" ,
	SUB."IdClasificacionTipoInstrumento" ,
	SUB."IdTipoInstrumento" ,
	OP."Tasa" AS "Tasa",
	CASE 
		WHEN SUB."IdEmisor" IN ('CENTRAL', 'TESORERIA', 'ESTADO', 'BBSCOTIABA', 'DESARROLLO', 'BBVA', 'BHIF') THEN SUB."IdEmisor" 
		ELSE ''
	END AS "IdEmisor",
	SUB."IdEmisor"  AS "Emisor" ,
	CASE
		WHEN SUB."Nemotecnico" IN ('PDBC', 'PRBC', 'PDT', 'PRT') THEN SUB."Nemotecnico" 
		ELSE ''
	END AS "Nemotecnico",
	DI."DocumentoIdentidad" AS "DocumentoIdentidadEmisor",
	'PACTEADA' AS "Grupo",
	'RF-IF' AS "Producto",
	SUB."IdTipoReajuste" ,
	TO_VARCHAR(CAR."FechaVencimientoInstrumento") AS "FechaVencimiento",
	TO_VARCHAR(TO_DATE(OP."FechaVencimiento")) AS "FechaVencimientoOperacion",--**
	TO_VARCHAR(CAR."FechaOperacion") AS "FechaOperacion",
	TO_VARCHAR(CAR."FechaLiquidacion" ) AS "FechaLiquidacion",
	SUB."IdMonedaInstrumento" ,
	TO_VARCHAR(DAYS_BETWEEN(:FechaSistema, CAR."FechaVencimientoInstrumento")) AS "Plazo",
	SUM(RE."MontoValorizado")  AS "Monto"
FROM "BSCL.Custodia.BD::CUS.Cartera"  CAR
LEFT JOIN BSCL."BSCL.Custodia.BD::CUS.Reserva" AS RE ON CAR."FolioCartera" = RE."FolioCarteraOrigen"
LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."IdCarteraRetirada" = CAR."IdCartera" 
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OP ON RE."IdOperacion" = OP."IdOperacion" AND OP."IdEstado" = 'VIG' AND OP."IdTipoOperacion" = 'VENPAC'
LEFT JOIN "BSCL.Plataforma.BD::MER.Emisor" EMI ON EMI."IdEmisor" = SUB."IdEmisor" 
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdPersona" = EMI."IdPersona"
WHERE
    CAR."TipoCarteraCorredor" <> 'CC'
    AND SUB."IdClasificacionTipoInstrumento" NOT IN('RV')
	AND SUB."IdEstado" IN('VIG', 'RPA')
GROUP BY 
	OP."IdOperacion" ,
	CAR."TipoCarteraCorredor" ,
	SUB."IdClasificacionTipoInstrumento" ,
	SUB."IdTipoInstrumento" ,
	OP."Tasa" ,
	DI."DocumentoIdentidad" ,
	SUB."IdEmisor",
	SUB."Nemotecnico" ,
	SUB."IdTipoReajuste" ,
	OP."FechaVencimiento",
	CAR."FechaOperacion",
	CAR."FechaLiquidacion",
	SUB."IdMonedaInstrumento",
	CAR."FechaVencimientoInstrumento"
HAVING SUM(re."MontoValorizado") > 0;