SELECT 
TO_BIGINT(:IdTipoCentralizacion)					AS "IdTipoCentralizacion",
CmbteDet."IdComprobanteDetalle"                     AS "IdMovimiento",
CONCAT('C',Cmbte."IdTipoComprobante")               AS "IdTipoMovimiento",
Cmbte."IdEmpresa"                                   AS "IdEmpresa",                                     
Cmbte."IdComprobante"                               AS "Numero", 
TO_BIGINT(0)                                        AS "Linea",
YEAR(Cmbte."FechaSistema")        ||'-'|| RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2)    ||'-'|| RIGHT('0' || 
DAYOFMONTH(Cmbte."FechaSistema"),2)                 AS "Fecha",
Cmbte."DocumentoIdentidad"                          AS "DocumentoIdentidad",  
Cmbte."IdPortafolio"                                AS "IdPortafolio",
TO_BIGINT(YEAR("FechaSistema"))                     AS "Ejercicio",
RIGHT('0' || 
MONTH(Cmbte."FechaSistema"),2) ||'-'|| 
YEAR(Cmbte."FechaSistema")                          AS "Periodo",
Cmbte."IdMonedaComprobante"                         AS "IdMoneda",
COALESCE(CmbteDet."MontoComprobanteDetalle",0)      AS "Monto",
Cmbte."IdEstadoComprobante"                         AS "Estado"         
FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante"				Cmbte
LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" CmbteDet
ON (Cmbte."IdComprobante" = CmbteDet."IdComprobante")
WHERE
	Cmbte."FechaSistema" = :FechaContable AND 
	"IdEstadoDTE"  IN ('PEN','ENV','REC')	AND 
	"DocumentoIdentidad" = :DocumentoIdentidad AND 
	"IdEstadoComprobante" <> 'ANU';