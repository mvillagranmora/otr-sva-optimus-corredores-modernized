SELECT "FolioSistemaExterno"
	, "TasaCompra" AS "TasaOperacion"
	, "FechaVencimientoOperacion" AS "FechaVencimiento"
	, "IdTipoPrestamo"
	, "IdCliente"
FROM (
	SELECT PRT."FolioSistemaExterno"
		, PRT."TasaCompra"
		, PRT."FechaVencimientoOperacion"
		, PRT."IdTipoPrestamo"
		, PRT."IdCliente"
		, RANK() OVER (PARTITION BY PRT."FolioSistemaExterno" ORDER BY PRT."IdCartera" DESC) AS "Rank"
	FROM "BSCL.Custodia.BD::CUS.Cartera" PRT 
	WHERE PRT."IdEstado" = 'MOD'
		AND PRT."IdTipoInstrumento" = 'PRT'
		AND PRT."IdEmpresa" = :p_IdEmpresa
		AND PRT."INGFechaOperacion" = :p_FechaOperacion
)
WHERE "Rank" = 1;