SELECT pre."IdPreComprobante",
		pre."IdTipoContabilidad",
	   pre."FechaContable",
	   pre."Estado",
	   tip."IdTipoCentralizacion"
FROM "BSCL.Contabilidad.BD::ITZ.PreComprobante" pre
LEFT JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" tip on tip."IdTipoContabilidad" = pre."IdTipoContabilidad"
WHERE pre."IdPreComprobante" IN (:IdPrecomprobante);

