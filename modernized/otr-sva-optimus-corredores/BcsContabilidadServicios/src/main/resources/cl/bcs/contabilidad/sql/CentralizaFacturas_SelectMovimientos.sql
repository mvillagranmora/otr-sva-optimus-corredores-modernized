SELECT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
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
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"
WHERE "FechaSistema" = :FechaContable    AND 
"IdEstadoComprobante" <> 'ANU' AND
"DocumentoIdentidad" != :DocumentoIdentidad AND 
"EsVentaCorta" <> 'S'
UNION ALL
SELECT 
TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",
Cmbte."IdComprobante"                                   AS "IdMovimiento",
concat('A',"IdTipoComprobante")                         AS "IdTipoMovimiento",              
Cmbte."IdEmpresa"                                       AS "IdEmpresa",                                     
Cmbte."IdComprobante"                                   AS "Numero",
TO_BIGINT(0)                                            AS "Linea",
YEAR(Cmbte."FechaSistema")      ||'-'|| RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2)  ||'-'|| RIGHT('0' || 
DAYOFMONTH(Cmbte."FechaSistema"),2)                     AS "Fecha",
Cmbte."DocumentoIdentidad"                              AS "DocumentoIdentidad",  
Cmbte."IdPortafolio"                                    AS "IdPortafolio",
TO_BIGINT(YEAR(Cmbte."FechaSistema"))                   AS "Ejercicio",
RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2) ||'-'|| 
YEAR(Cmbte."FechaSistema")                              AS "Periodo",
Cmbte."IdMonedaComprobante"                             AS "IdMoneda",
Cmbte."MontoComprobante"                                AS "Monto",
Cmbte."IdEstadoComprobante"                             AS "Estado"         
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" Cmbte
INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Movimiento" Mov
ON (Cmbte."IdTipoComprobante"   = Mov."IdTipoMovimiento" AND
    Cmbte."IdComprobante"       = Mov."Numero")
INNER JOIN  "BSCL.Contabilidad.BD::ITZ.Centralizacion" Cent
ON (Cent."IdCentralizacion"     = Mov."IdCentralizacion" AND 
    Cent."FechaContable"        = Mov."Fecha")  
WHERE   Cmbte."FechaSistema"        = :FechaContable AND 
        Cmbte."IdEstadoComprobante" = 'ANU' AND
        Cent."IdTipoCentralizacion" = :IdTipoCentralizacion AND 
        Cent."FechaContable"        = :FechaContable AND 
        Cent."Estado"               = 'V' AND
		Cmbte."DocumentoIdentidad" != :DocumentoIdentidad AND 
		Cmbte."EsVentaCorta" <> 'S';