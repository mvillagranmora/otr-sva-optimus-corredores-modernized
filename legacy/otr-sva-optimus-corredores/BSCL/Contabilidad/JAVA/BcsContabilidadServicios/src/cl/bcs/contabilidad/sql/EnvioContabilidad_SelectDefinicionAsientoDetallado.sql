SELECT DEF."IdCuentaContable",CTB_itzContabilidadTipo."Formato",SUM(DEF."Orden") AS "Suma",'' AS "ConRut"
FROM "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" DEF
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" CTB_itzContabilidadTipo
ON(DEF."IdTipoContabilidad" = CTB_ITZCONTABILIDADTIPO."IdTipoContabilidad")
LEFT JOIN "BSCL.Contabilidad.BD.Vistas::ITZ.ConsultarEstadoContabilidad"  EstadoContabilidad 
ON (EstadoContabilidad."IdEstadoContabilidad" = CTB_itzContabilidadTipo."Estado" AND (CTB_itzContabilidadTipo."IdEmpresa" = :IdEmpresa 	OR :IdEmpresa = 0))
INNER JOIN "BSCL.Contabilidad.BD::ITZ.PreComprobante" PRE ON (PRE."IdTipoContabilidad" = DEF."IdTipoContabilidad")
WHERE   CTB_itzContabilidadTipo."Estado" = 'V' AND  CTB_itzContabilidadTipo."Formato" ='D'   AND PRE."IdPreComprobante" IN (:IdPrecomprobante)
GROUP BY DEF."IdCuentaContable", CTB_itzContabilidadTipo."Formato", DEF."IdCuentaContable";

