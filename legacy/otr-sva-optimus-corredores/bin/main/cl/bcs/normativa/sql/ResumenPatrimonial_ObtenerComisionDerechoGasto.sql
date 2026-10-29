SELECT CC."IdPortafolio", 
CASE 
	WHEN CC."IdTipoComprobante" = 'NCE' THEN CC."TotalComis"*-1
	ELSE CC."TotalComis"
END AS "TotalComisiones",
CASE 
	WHEN CC."IdTipoComprobante" = 'NCE' THEN CC."TotalImpto"*-1
	ELSE CC."TotalImpto"
END AS "TotalImpuestos",
CASE 
	WHEN CC."IdTipoComprobante" = 'NCE' THEN CC."TotalDere"*-1
	ELSE CC."TotalDere"
END AS "TotalDerechos",
CASE 
	WHEN CC."IdTipoComprobante" = 'NCE' THEN CC."TotalGasto"*-1
	ELSE CC."TotalGasto"
END AS "TotalGastos",
CASE 
	WHEN CC."IdTipoComprobante" = 'NCE' THEN CC."TotalRetencion"*-1
	ELSE CC."TotalRetencion"
END AS "TotalRetencion",
CC."FechaComprobante",
CC."IdEstadoComprobante"
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" CC
WHERE CC."IdEstadoComprobante" IN ('FAC', 'ENV')
AND CC."IdPortafolio" = :p_IdPortafolio 
AND CC."FechaComprobante" BETWEEN :p_FechaDesde AND :p_FechaHasta
AND CC."IdEmpresa" = :p_IdEmpresa;