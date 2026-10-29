SELECT
	CASE WHEN count(0) > 0 THEN 'S' ELSE 'N' END AS "MovimientosRetiroEstadoCertificado"
	, MOVRET."IdMovimientoCarteraEnTransito" AS "IdMovimientoCarteraEnTransitoRetiro", MOVRET."ExcluyeMovimientoEnCartola"
FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
LEFT JOIN "BSCL.Custodia.BD::GTA.Garantia" AS RETG ON RETG."IdMovimientoCarteraEnTransito" = MOV."IdMovimientoCarteraEnTransito" AND RETG."EGRIdTipoOperacion" = 'RETGTA'
LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOVRET ON RETG."EGRIdMovimientoCarteraEnTransito" = MOVRET."IdMovimientoCarteraEnTransito"
WHERE MOV."IdMovimientoCarteraEnTransito" = :idsMovimientoCarteraTransito AND MOVRET."IdEstado" = 'CRT' AND MOVRET."IdConcepto" != 'REGGTA' AND MOVRET."ExcluyeMovimientoEnCartola" != 'S'
GROUP BY MOVRET."IdMovimientoCarteraEnTransito",MOVRET."ExcluyeMovimientoEnCartola";