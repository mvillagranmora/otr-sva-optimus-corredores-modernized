SELECT "IdPreComprobante" FROM 
"BSCL.Contabilidad.BD::ITZ.PreComprobante" AS Precomprobante
INNER JOIN "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoCentralizacionTipo" AS ContabilidadTipoCentralizacionTipo ON Precomprobante."IdTipoContabilidad" = ContabilidadTipoCentralizacionTipo."IdTipoContabilidad"
WHERE 
ContabilidadTipoCentralizacionTipo."IdTipoCentralizacion" = :IdTipoCentralizacion 
AND Precomprobante."FechaContable" = :FechaContable 
AND Precomprobante."IdEmpresa" = :IdEmpresa 
AND Precomprobante."Estado" != 'A'