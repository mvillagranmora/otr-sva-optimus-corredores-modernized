SELECT MCETP."IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransitoAnularUsuario"
	, MCETP."Cantidad" AS "CantidadAnularUsuario"
	, MCETP."MontoCompraMonedaInstrumento"
	, MCETP."EsVentaDescubierta"
	, MCETR."IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransitoFAERelacionada"
	, MCETR."Cantidad" AS "CantidadFAERelacionadaEgreso"
	, CR."IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransitoAnularPosterior"
	, MCETCR."IdEstado" AS "IdEstadoAnularPosterior"
	, CU."FolioCartera" AS "FolioCarteraUltimaRelacionada"
	, CU."IdEstado" AS "IdEstadoCarteraUltimaRelacionada"
	, MCETR."EsNominal"
	, MCETR."IdTipoOperacion"
	, MCETR."IdConcepto"
	, MCETR."IdTipoComprobante"
	, MCETR."FolioComprobante"
	, MCETR."IdAgente"
	, MCETR."IdTipoProducto"
	, MCETR."FolioCustodia"
FROM (
	SELECT C."IdCartera", M."IdMovimientoCarteraEnTransito", C."EGRIdMovimientoCarteraEnTransito", M."IdPortafolio"
		, M."IdInstrumento", M."Cantidad", M."MontoCompraMonedaInstrumento", M."EsVentaDescubierta"
	FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" M
	INNER JOIN	"BSCL.Custodia.BD::CUS.Cartera"	C ON (M."IdMovimientoCarteraEnTransito" = C."IdMovimientoCarteraEnTransito" OR M."IdMovimientoCarteraEnTransito" = C."EGRIdOperacion")
	WHERE 	M."IdMovimientoCarteraEnTransito" 	IN (:p_IdMovimientoCarteraEnTransito)
	AND 	C."IdEstado"						IN  ('LIQ', 'RPA') 
	AND 	M."IdEstado" = 'CRT'
) MCETP -- Movimiento base a anular
INNER JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCETR ON MCETR."IdMovimientoCarteraEnTransito" = MCETP."EGRIdMovimientoCarteraEnTransito" AND MCETR."IdEstado" = 'CRT' AND MCETR."FolioExternoReferencia" = '' AND MCETP."IdPortafolio" = MCETR."IdPortafolio" AND  MCETP."IdInstrumento" = MCETR."IdInstrumento" -- Movimiento relacionado por neteo
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCETF ON MCETF."FolioComprobante" = MCETR."FolioComprobante" AND MCETF."Cantidad" < MCETR."Cantidad" AND MCETF."IdEstado" = 'CRT' AND MCETF."FolioExternoReferencia" = '' AND MCETF."IdPortafolio" = MCETR."IdPortafolio" AND  MCETF."IdInstrumento" = MCETR."IdInstrumento" -- Movimientos por neteo parcial relacionados a un mismo comprobante neteado
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CR ON CR."EGRIdMovimientoCarteraEnTransito" = MCETF."IdMovimientoCarteraEnTransito" AND CR."IdMovimientoCarteraEnTransito" <> MCETP."IdMovimientoCarteraEnTransito" AND CR."IdPortafolio" = MCETF."IdPortafolio" AND  CR."IdInstrumento" = MCETF."IdInstrumento" -- Si los movimientos que netean un mismo comprobante poseen cartera relacionada, es porque son un neteo posterior
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" MCETCR ON MCETCR."IdMovimientoCarteraEnTransito" = CR."IdMovimientoCarteraEnTransito" AND MCETCR."IdPortafolio" = CR."IdPortafolio" AND  MCETCR."IdInstrumento" = CR."IdInstrumento"
INNER JOIN (
		SELECT M."IdMovimientoCarteraEnTransito", C."FolioCartera", C."IdEstado"
		, RANK() OVER(PARTITION BY M."IdMovimientoCarteraEnTransito" ORDER BY C."IdCartera" DESC) AS "Rank"
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" M
		INNER JOIN	"BSCL.Custodia.BD::CUS.Cartera" C ON (M."IdMovimientoCarteraEnTransito" = C."IdMovimientoCarteraEnTransito" OR M."IdMovimientoCarteraEnTransito" = C."EGRIdOperacion")
		WHERE 	M."IdMovimientoCarteraEnTransito" 	IN (:p_IdMovimientoCarteraEnTransito)
		AND 	C."IdEstado"						IN  ('LIQ', 'RPA', 'VIG')
) CU ON CU."IdMovimientoCarteraEnTransito" = MCETP."IdMovimientoCarteraEnTransito" AND CU."Rank" = 1
ORDER BY MCETP."IdMovimientoCarteraEnTransito" ASC, MCETF."IdMovimientoCarteraEnTransito" ASC;