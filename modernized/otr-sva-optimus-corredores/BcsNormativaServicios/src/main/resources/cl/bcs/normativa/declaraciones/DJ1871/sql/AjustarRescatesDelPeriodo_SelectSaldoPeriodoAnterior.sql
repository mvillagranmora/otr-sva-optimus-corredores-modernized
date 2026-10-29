SELECT
	"DocumentoIdentidad" AS "RutCliente"
   ,"IdCliente"
   ,CASE
		WHEN SUM(TO_DECIMAL("Abono",26,8)) - SUM(TO_DECIMAL("Cargo",26,8)) < 0 THEN 0
		ELSE SUM(TO_DECIMAL("Abono",26,8)) - SUM(TO_DECIMAL("Cargo",26,8))
	END AS "AhorrosDeposito"
   ,TO_BIGINT(YEAR("FechaCertificacion"))  AS "AnoAhorro"
FROM
	"BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos"
WHERE
	"IdConcepto" IN ('ABOAPV','CARAPV')
AND "IdEstado" = 'CRT'
AND "RegimenMatrizAgrupador" = 'APV-A'
AND TO_BIGINT(YEAR("FechaCertificacion")) = (:p_AnoAhorro - 1)
GROUP BY
	"DocumentoIdentidad"
   ,"IdCliente"
   ,TO_BIGINT(YEAR("FechaCertificacion"))
ORDER BY
	"DocumentoIdentidad"
   ,TO_BIGINT(YEAR("FechaCertificacion"));