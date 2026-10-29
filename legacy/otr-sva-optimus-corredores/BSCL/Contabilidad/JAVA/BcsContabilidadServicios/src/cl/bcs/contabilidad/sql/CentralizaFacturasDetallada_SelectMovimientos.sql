SELECT DISTINCT 
TO_BIGINT(:IdTipoCentralizacion)									AS "IdTipoCentralizacion",
IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
--Cmbte."IdComprobante" 									AS "IdMovimiento",
Cmbte."IdTipoComprobante"                           AS "IdTipoMovimiento",
Cmbte."IdEmpresa"                                   AS "IdEmpresa",                                     
Cmbte."IdComprobante"                               AS "Numero", 
TO_BIGINT(0)                                        AS "Linea",
YEAR(Cmbte."FechaSistema")        ||'-'|| RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2)    ||'-'|| RIGHT('0' || 
DAYOFMONTH(Cmbte."FechaSistema"),2)                 AS "Fecha",
Cmbte."DocumentoIdentidad"                          AS "DocumentoIdentidad",  
Cmbte."IdPortafolio"                                AS "IdPortafolio",
TO_BIGINT(YEAR(Cmbte."FechaSistema"))               AS "Ejercicio",
RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2) ||'-'|| 
YEAR(Cmbte."FechaSistema")                          AS "Periodo",
Cmbte."IdMonedaComprobante"                         AS "IdMoneda",
Cmbte."MontoComprobante"                            AS "Monto",
Cmbte."IdEstadoComprobante"                         AS "Estado"         

FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"				Cmbte
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
WHERE 
	Cmbte."FechaSistema" 		= :FechaContable    AND 
	Cmbte."IdEstadoComprobante" <> 'ANU' AND
	Cmbte."DocumentoIdentidad" 	!= :DocumentoIdentidad
--	AND Cmbte."IdComprobante" IN ( 2436    

UNION ALL

SELECT DISTINCT 
TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",
IFNULL(MovCmbte."IdMovimientoComprobante",Cmbte."IdComprobante")	AS "IdMovimiento",
--Cmbte."IdComprobante" 									AS "IdMovimiento",
CONCAT('A',Cmbte."IdTipoComprobante")                   AS "IdTipoMovimiento",              
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
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" 	Cmbte
LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" 			MovCmbte
ON (Cmbte."IdComprobante" = MovCmbte."IdComprobante")
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
		Cmbte."DocumentoIdentidad" != :DocumentoIdentidad
	--	AND Cmbte."IdComprobante" = 2436