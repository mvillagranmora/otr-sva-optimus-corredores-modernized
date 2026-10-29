Select ConsIngEgr."IdComprobanteOrigen",
       (Case
          When Coalesce(ConsIngEgr."IdComprobante",0) = 0 Then ConsIngEgr."IdIngresoEgreso"
        Else ConsIngEgr."IdComprobante"
        End) As "Id",
       (Case
           When Coalesce(ConsIngEgr."IdComprobante",0) = 0 Then 'TES.IngresoEgreso'
        Else 'TES.Comprobante'
        End) As "Entidad",
        "NombreTipoMovimiento",
        "IdEstado"
FROM "BSCL.Tesoreria.BD.Vistas::ConsultarIngresoEgreso" AS ConsIngEgr    
WHERE 
ConsIngEgr."IdModulo" = 'FAC' 
AND ConsIngEgr."IdComprobanteOrigen" IN (:idsComprobante)
AND ConsIngEgr."IdModulo" = 'FAC'
And ConsIngEgr."IdEstado" NOT IN ('ANU','MOD');

