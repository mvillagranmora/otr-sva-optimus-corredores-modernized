UPDATE CFP
SET 
	CFP."SaldoDisponible" = :SaldoDisponible,
	CFP."SaldoContable" = :SaldoPendiente
FROM "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP
WHERE "IdComprobanteFormaDePago" IN (:IdComprobanteFormaDePago);