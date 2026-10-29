SELECT 
	CARABO."IdCargoAbonoMovimiento"
	,CTAINV."IdCliente"
	,CTAINV."IdPortafolio"
	,CTAINV."IdMoneda"
	,CTAINV."IdCuentaInversion"
	,CARABO."Monto"
	,CASE
		WHEN CARABO."IdModulo" = 'FAC' AND COMP."IdClasificacionTipoInstrumento" = 'RV' THEN 'I'
		ELSE 'G'
	END AS "TipoOperacion"
	,TO_DATE(CARABO."FechaCertificacion") AS "FechaMovimiento"
	,CARABO."FechaCertificacion"
	,CARABO."FechaEfectivaMovimiento"
FROM  "BSCL.CuentaInversion.BD::GBL.ConsultaReporteAcreedores_Cuenta_Paso" TABPAS
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTAINV
	ON(TABPAS."IdCuentaInversion" = CTAINV."IdCuentaInversion" AND TABPAS."IdTrabajo"  = :IdTrabajo)
INNER JOIN  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CARABO
	ON(CTAINV."IdCuentaInversion" = CARABO."IdCuentaInversion" AND CARABO."IdEstado" = 'CRT' AND TO_DATE(CARABO."FechaCertificacion") <= TO_DATE(:FechaBusqueda))
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP
	ON (CARABO."IdComprobante" = COMP."IdComprobante")
WHERE CARABO."IdTipoMovimiento" = 'MOVABN'