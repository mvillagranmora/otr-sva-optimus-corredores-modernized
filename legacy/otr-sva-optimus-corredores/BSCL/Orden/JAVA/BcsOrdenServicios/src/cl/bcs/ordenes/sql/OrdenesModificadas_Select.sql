SELECT  TMP."IdOrden",TMP."Cantidad",TMP."PrecioOrden",TMP."FechaOperacion",
        TMP."IdEstadoOrden",TMP."IdMonedaOrden",TMP."TipoCambio",TMP."IdClasificacionTipoInstrumento",
        TMP."IdEstadoFacturacion",TMP."Nemotecnico",
        SUM(TMP."Comision") AS "Comision",
       
        SUM(TMP."Gasto") AS "Gasto",
        SUM(TMP."Impuesto") AS "Impuesto"
        
        
FROM (     
      SELECT  DISTINCT ORD."IdOrden",
        ORD."Cantidad",
        ORD."PrecioOrden",
        TO_VARCHAR (TO_DATE(ORD."FechaOperacion"), 'YYYY-MM-DD') AS "FechaOperacion",
        ORD."IdEstadoOrden",
        ORD."IdMonedaOrden",
        ORD."TipoCambio",
        ORD."IdClasificacionTipoInstrumento",
        MOV."IdEstadoFacturacion",
        CASE WHEN COB."CodigoCobroClasificacion" = 'COMISION' THEN COB."MontoCobroCalculado" ELSE 0 END AS "Comision",
        CASE WHEN COB."CodigoCobroClasificacion" = 'GASTO' THEN COB."MontoCobroCalculado" ELSE 0 END AS "Gasto",
        
       
        CASE WHEN COB."CodigoCobroClasificacion" = 'IMPUESTO' THEN COB."MontoCobroCalculado" ELSE 0 END AS "Impuesto",
        
        
        
        ORD."Nemotecnico",
        MOV."IdTipoInstrumento",
        COB."CodigoCobroClasificacion"
     FROM "BSCL.Orden.BD::ORD.Orden" ORD 

     LEFT JOIN "BSCL.Orden.BD::ORD.CobroOrden" COB ON COB."IdOrden" = ORD."IdOrden"  AND COB."CodigoCobroClasificacion" IN ('COMISION','GASTO','IMPUESTO')
     LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOV ON (ORD."IdOrden" = MOV."IdOrden" AND MOV."IdTipoInstrumento" = 'ACN' AND  MOV."IdEstadoFacturacion" = 'FAC')
     WHERE ORD."IdClasificacionTipoInstrumento" = 'RV'
     AND ORD."IdTipoCanalIngreso" = 'OWP'
     AND ORD."IdOrden" IN(:p_IdOrdenes)
     ORDER BY ORD."IdOrden") AS TMP
        
        
GROUP BY  "IdOrden", "Cantidad", "PrecioOrden", "FechaOperacion","IdEstadoOrden", "IdMonedaOrden", 
          "TipoCambio", "IdClasificacionTipoInstrumento","IdEstadoFacturacion","Nemotecnico";