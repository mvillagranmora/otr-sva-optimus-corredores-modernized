SELECT ConsIngEgr."IdComprobanteOrigen",
       ConsIngEgr."IdIngresoEgreso" As "Id",
       'TES.IngresoEgreso' As "Entidad",
        "NombreTipoMovimiento",
        "IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS ConsIngEgr    
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" Ope ON Ope."IdOperacion" = ConsIngEgr."IdOperacion"
WHERE 
ConsIngEgr."IdModulo" = 'MRF' 
AND ConsIngEgr."IdConcepto" = 'ANPCME'
And ConsIngEgr."IdEstado" NOT IN ('ANU','MOD')
UNION ALL 
Select ConsIngEgr."IdComprobanteOrigen",
       ConsIngEgr."IdComprobante" As "Id",
       'TES.Comprobante' As "Entidad",
        "NombreTipoMovimiento",
        "IdEstadoComprobante" AS "IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS ConsIngEgr   
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" Ope ON Ope."IdOperacion" = ConsIngEgr."IdOperacion"
WHERE 
ConsIngEgr."IdModulo" = 'MRF' 
AND ConsIngEgr."IdConcepto" = 'ANPCME'
And ConsIngEgr."IdEstadoComprobante" NOT IN ('ANU','MOD')
AND Coalesce(ConsIngEgr."IdComprobante",0) != 0;