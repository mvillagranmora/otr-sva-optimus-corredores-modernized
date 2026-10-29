SELECT * FROM "BSCL.CuentaInversion.BD::CTA.SaldosReporteAntiguedad" 
	WHERE "IdCliente" = :IdCliente	AND "IdPortafolio" = :IdPortafolio AND "IdMoneda" = :IdMoneda
	AND "FechaSistema" = TO_DATE(:FechaInicio);