SELECT ROW_NUMBER() OVER(ORDER BY SALDOS."FechaSistema" ASC,TABPAS."IdCliente" ASC,TABPAS."IdPortafolio") AS "NumeroRegistro"
	,SALDOS."FechaSistema", SALDOS."IdCliente", SALDOS."IdPortafolio", SALDOS."IdMoneda", SALDOS."TipoSaldo", SALDOS."Saldo" 
	FROM "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" AS SALDOS
	INNER JOIN "BSCL.CuentaInversion.BD::GBL.ConsultarAntiguedadDeudasCuentaInversion_Cuenta_Paso" AS TABPAS
		ON TABPAS."IdCliente" =  SALDOS."IdCliente" AND TABPAS."IdMoneda" = SALDOS."IdMoneda" 
		AND TABPAS."IdPortafolio" = SALDOS."IdPortafolio" AND TABPAS."IdTrabajo"  = :IdTrabajo
	WHERE SALDOS."FechaSistema" BETWEEN TO_DATE(:FechaInicio) AND TO_DATE(:FechaFin) ORDER BY "FechaSistema" ASC,"IdCliente" ASC,"IdPortafolio" ASC;