SELECT 
	CARABO."IdCargoAbonoMovimiento"
	,CTAINV."IdCliente"
	,CTAINV."IdPortafolio"
	,CTAINV."IdMoneda" 
	,CARABO."IdTipoMovimiento" 
	,CARABO."Monto"
	,CASE
		WHEN CARABO."IdModulo" = 'FAC' AND COMP."IdClasificacionTipoInstrumento" = 'RV' THEN 'I'
		ELSE 'G'
	END AS "TipoSaldo"
	,TO_DATE(CARABO."FechaCertificacion") AS "FechaMovimiento"
FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" CARABO
INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" CTAINV
	ON CARABO."IdCuentaInversion" = CTAINV."IdCuentaInversion"
LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COMP
	ON (CARABO."IdComprobante" = COMP."IdComprobante")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CONLIQ
	ON (COMP."IdCondicionLiquidacion" = CONLIQ."IdCondicionDeLiquidacion")
WHERE CARABO."IdEstado" = 'CRT' AND TO_DATE(CARABO."FechaCertificacion") BETWEEN TO_DATE(:FechaInicio) and TO_DATE(:FechaFin);