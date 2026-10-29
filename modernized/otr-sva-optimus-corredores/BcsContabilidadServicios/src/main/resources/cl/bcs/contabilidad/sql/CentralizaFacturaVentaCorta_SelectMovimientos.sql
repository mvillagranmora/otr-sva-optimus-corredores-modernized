SELECT 
TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",
"IdComprobante"                                     AS "IdMovimiento",
"IdTipoComprobante"                                 AS "IdTipoMovimiento",
"IdEmpresa"                                         AS "IdEmpresa",                                     
"IdComprobante"                                     AS "Numero", --"FolioComprobante"
TO_BIGINT(0)                                        AS "Linea",
YEAR("FechaSistema")        ||'-'|| RIGHT('0' || 
MONTH("FechaSistema"),2)    ||'-'|| RIGHT('0' || 
DAYOFMONTH("FechaSistema"),2)                       AS "Fecha",
"DocumentoIdentidad"                                AS "DocumentoIdentidad",  
"IdPortafolio"                                      AS "IdPortafolio",
TO_BIGINT(YEAR("FechaSistema"))                     AS "Ejercicio",
RIGHT('0' || 
MONTH("FechaSistema"),2) ||'-'|| 
YEAR("FechaSistema")                                AS "Periodo",
"IdMonedaComprobante"                               AS "IdMoneda",
"MontoComprobante"                                  AS "Monto",
"IdEstadoComprobante"                               AS "Estado"         
FROM "BSCL.Facturacion.BD::FAC.Comprobante"
WHERE "FechaSistema" = :FechaContable AND 
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"IdEstadoComprobante" <> 'ANU' AND
"EsVentaCorta" = 'S' AND "IdClasificacionTipoInstrumento" = :IdClasificacionTipoInstrumento
