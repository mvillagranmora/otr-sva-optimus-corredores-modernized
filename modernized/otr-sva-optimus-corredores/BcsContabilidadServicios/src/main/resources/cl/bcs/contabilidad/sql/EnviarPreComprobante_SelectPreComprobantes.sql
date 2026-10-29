SELECT 
	PC."IdPreComprobante", 
	PC."Estado",
	CEC."NombreEstado"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" PC
INNER JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad" CEC ON PC."Estado" = CEC."IdEstadoContabilidad"
WHERE PC."IdPreComprobante" IN (:IdPreComprobantes);