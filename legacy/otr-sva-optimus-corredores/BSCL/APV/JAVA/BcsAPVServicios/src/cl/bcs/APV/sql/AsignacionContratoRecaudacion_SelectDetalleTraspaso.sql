SELECT contratoSAAPV."IdContratoSAAPV",detalleTraspaso."IdTraspaso", 
CASE WHEN COUNT(detalleTraspaso."IdTraspaso") >= 1 THEN 'S'
	ELSE 'N' END AS "TieneDetalle"
FROM "BSCL.APV.BD::SCS.ContratoSAAPV" AS contratoSAAPV
LEFT JOIN "BSCL.APV.BD::TRA.Traspaso" AS traspaso ON traspaso."IdContratoSAAPV" = contratoSAAPV."IdContratoSAAPV" AND traspaso."IdTraspaso" IS NOT NULL
LEFT JOIN "BSCL.APV.BD::TRA.DetalleTraspaso" AS detalleTraspaso ON traspaso."IdTraspaso" = detalleTraspaso."IdTraspaso" AND detalleTraspaso."IdTraspaso" IS NOT NULL
WHERE contratoSAAPV."IdContratoSAAPV" IN (:IdContratoSAAPV) AND contratoSAAPV."IdContratoSAAPV" IS NOT NULL
GROUP BY contratoSAAPV."IdContratoSAAPV",detalleTraspaso."IdTraspaso";