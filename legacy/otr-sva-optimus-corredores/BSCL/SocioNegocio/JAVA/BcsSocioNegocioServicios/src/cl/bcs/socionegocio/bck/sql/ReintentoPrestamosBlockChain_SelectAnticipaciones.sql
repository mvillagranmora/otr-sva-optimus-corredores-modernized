SELECT car."FolioSistemaExterno"
	, car."IdEmpresa"
	, car."FolioCartera"
	, car."IdEstado"
	, car."CantidadRestante" as "Cantidad"
	, car."IdTipoOperacion"
	, car."FechaLiquidacion"
	, car."FechaOperacion"
	, car."IdCliente" 
	, cli."DocumentoIdentidad"
FROM (
	SELECT PRT."FolioSistemaExterno"
		, PRTANT."IdEmpresa"
		, PRT."FolioCartera"
		, PRTANT."IdEstado"
		, CASE WHEN PRTANT."IdTipoInstrumento" = 'EMP' 
			THEN EMP."Cantidad" - SUM( PRTANT."EGRCantidad" )
			ELSE PRT."Cantidad" - SUM( PRTANT."EGRCantidad" )
		  END AS "CantidadRestante"
		, PRTANT."IdTipoOperacion"
		, PRTANT."FechaLiquidacion"
		, PRTANT."FechaOperacion"
		, PRTANT."IdCliente" 
		, RANK() OVER (PARTITION BY PRT."FolioCartera" ORDER BY PRTANT."FolioCarteraRelacionada" ASC) AS "Rank"
	FROM "BSCL.Custodia.BD::CUS.Cartera" PRTANT 
	INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" PRT ON PRT."IdTipoInstrumento" = 'PRT' 
														AND PRT."IdEmpresa" = PRTANT."IdEmpresa" 
														AND ( PRT."IdEstado" IN ('VIG') OR ( PRT."IdEstado" = 'ANT' AND PRT."EGRCantidad" = PRT."INGCantidad" ) )
														AND ( PRT."FolioCartera" = PRTANT."FolioCarteraRelacionada" AND PRTANT."IdTipoInstrumento" = 'EMP' OR PRT."FolioCartera" = PRTANT."FolioCartera" AND PRTANT."IdTipoInstrumento" = 'PRT' )
	LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" EMP ON EMP."IdTipoInstrumento" = 'EMP' 
														AND EMP."IdEmpresa" = PRTANT."IdEmpresa" 
														AND ( EMP."IdEstado" = 'VIG' OR ( EMP."IdEstado" = 'ANT' AND EMP."EGRCantidad" = EMP."INGCantidad" ) )
														AND EMP."FolioCartera" = PRTANT."FolioCartera"
	WHERE PRTANT."IdEstado" = 'ANT'
		AND PRTANT."IdTipoInstrumento" IN ('PRT', 'EMP')
		AND PRTANT."IdEmpresa" = 1
		AND '2023-03-31' BETWEEN PRTANT."INGFechaOperacion" AND PRTANT."FechaVencimientoOperacion"
		AND ( PRTANT."EstadoAnticipo" = 'RZD' OR IFNULL(PRTANT."EstadoAnticipo", '') = '' )
	GROUP BY PRTANT."IdEmpresa", PRT."FolioCartera", PRTANT."IdEstado", PRTANT."IdTipoOperacion", PRTANT."FechaLiquidacion", PRTANT."FechaOperacion", PRTANT."IdCliente", EMP."Cantidad", PRTANT."IdTipoInstrumento", PRT."Cantidad"
		, PRT."FolioSistemaExterno", PRTANT."FolioCarteraRelacionada"
) car
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = car."IdCliente"
WHERE "Rank" = 1;