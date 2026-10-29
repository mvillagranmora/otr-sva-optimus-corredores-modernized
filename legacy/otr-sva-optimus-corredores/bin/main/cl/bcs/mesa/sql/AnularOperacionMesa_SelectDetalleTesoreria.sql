Select ConsIngEgr."IdComprobanteOrigen",
       ConsIngEgr."IdIngresoEgreso" As "Id",
       'TES.IngresoEgreso' As "Entidad",
        "NombreTipoMovimiento",
        "IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS ConsIngEgr    
WHERE 
ConsIngEgr."IdModulo" = 'MRF' 
--AND ConsIngEgr."IdComprobanteOrigen" IN (:IdOperacion)
AND ConsIngEgr."IdOperacion" IN (:IdOperacion)
AND ConsIngEgr."IdModulo" = 'MRF'
And ConsIngEgr."IdEstado" NOT IN ('ANU','MOD')

UNION ALL 

Select ConsIngEgr."IdComprobanteOrigen",
       ConsIngEgr."IdComprobante" As "Id",
       'TES.Comprobante' As "Entidad",
        "NombreTipoMovimiento",
        "IdEstadoComprobante" AS "IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS ConsIngEgr    
WHERE 
ConsIngEgr."IdModulo" = 'MRF' 
AND ConsIngEgr."IdOperacion" IN (:IdOperacion)
AND ConsIngEgr."IdModulo" = 'MRF'
And ConsIngEgr."IdEstadoComprobante" NOT IN ('ANU','MOD')
AND Coalesce(ConsIngEgr."IdComprobante",0) != 0;
