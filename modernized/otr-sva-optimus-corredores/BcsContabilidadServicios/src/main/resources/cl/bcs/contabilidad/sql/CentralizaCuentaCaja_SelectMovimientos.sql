SELECT
DISTINCT 
	TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",
	CA."IdCargoAbonoMovimiento"								AS "IdMovimiento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'ABONO'
		ELSE 'CARGO' 
	 END													AS "IdTipoMovimiento",
	CA."IdEmpresa"											AS "IdEmpresa",
	CA."IdCargoAbonoMovimiento"								AS "Numero",
	TO_BIGINT(0)                                        	AS "Linea",
	YEAR(CA."FechaSistema") 	||'-'|| RIGHT('0' || 
	MONTH(CA."FechaSistema"),2) ||'-'|| RIGHT('0' || 
	DAYOFMONTH(CA."FechaSistema"),2) 
															AS "Fecha",
	PER."DocumentoIdentidad"								AS "DocumentoIdentidad",
	POR."IdPortafolio"										AS "IdPortafolio",
	TO_BIGINT(YEAR(CA."FechaSistema"))						AS "Ejercicio",
	RIGHT('0' || MONTH(CA."FechaSistema"),2) ||'-'|| 
	YEAR(CA."FechaSistema")									AS "Periodo",
	CA."Monto"												AS "Monto",
	CA."IdMoneda"											AS "IdMoneda",
	CA."IdEstado"											AS "Estado"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" 			AS CA
LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" 	AS CIV
	ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 			AS POR
	ON POR."IdPortafolio"	= CIV."IdPortafolio" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" 			AS CLI
	ON CLI."IdCliente"		= POR."IdCliente" 
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" 			AS PER
	ON PER."IdPersona"		= CLI."IdPersona"

WHERE   CA."FechaSistema" = :FechaContable
	AND CA."EsIngresoManual" 	  <> 'S'
	AND CA."SaldoLibroInventario" = 0
	AND CA."IdEstado" IN ('CRT')
		   
UNION ALL

-- CARGOS
SELECT   
DISTINCT
	TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",		
	CA."IdCargoAbonoMovimiento"								AS "IdMovimiento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'MOABONO'
		ELSE 'MOCARGO' 
	 END													AS "IdTipoMovimiento",
	:IdEmpresa												AS "IdEmpresa",
	CA."IdCargoAbonoMovimiento"								AS "Numero",
	TO_BIGINT(0)                                        	AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 
															AS "Fecha",
	PER."DocumentoIdentidad"								AS "DocumentoIdentidad",
	POR."IdPortafolio"										AS "IdPortafolio",
	TO_BIGINT(YEAR(:FechaContable))                     	AS "Ejercicio",
	RIGHT('0' || 
	MONTH(:FechaContable),2) ||'-'|| 
	YEAR(:FechaContable)									AS "Periodo",
	CA."Monto"												AS "Monto",
	CA."IdMoneda"											AS "IdMoneda",
	CA."IdEstado"											AS "Estado"
FROM (	SELECT   	 
			CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"
			,CPC."NombreConcepto"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
			,CPC."IdEstado"
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" as CPC
		WHERE CPC."SaldoLibroInventario" > 0
		AND CPC."IdTipoMovimiento" = 'MOVCAR' 	   
		AND CPC."IdEstado" = 'CRT'
		AND CPC."FechaEfectivaMovimiento" 			= :FechaContable
		AND (NULL IS NULL OR CPC."IdCliente" 		= NULL)
		AND (NULL IS NULL OR CPC."IdPortafolio"	= NULL)
	) AS CA 								
 	LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS CIV
				 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
		  LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
				 ON POR."IdPortafolio"	= CIV."IdPortafolio" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				 ON CLI."IdCliente"		= POR."IdCliente" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
				 ON PER."IdPersona"		= CLI."IdPersona"
	WHERE 
		   	   CA."AntiguedadDeuda" > 0
		   AND CA."IdEstado" IN ('CRT')
		   
-- ABONOS
UNION ALL

SELECT   
DISTINCT
	TO_BIGINT(:IdTipoCentralizacion)						AS "IdTipoCentralizacion",		
	CA."IdCargoAbonoMovimiento"								AS "IdMovimiento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'MOABONO'
		ELSE 'MOCARGO' 
	 END													AS "IdTipoMovimiento",
	:IdEmpresa												AS "IdEmpresa",
	CA."IdCargoAbonoMovimiento"								AS "Numero",
	TO_BIGINT(0)                                        	AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2) 
															AS "Fecha",
	PER."DocumentoIdentidad"								AS "DocumentoIdentidad",
	POR."IdPortafolio"										AS "IdPortafolio",
	TO_BIGINT(YEAR(:FechaContable))                     	AS "Ejercicio",
	RIGHT('0' || 
	MONTH(:FechaContable),2) ||'-'|| 
	YEAR(:FechaContable)									AS "Periodo",
	CA."Monto"												AS "Monto",
	CA."IdMoneda"											AS "IdMoneda",
	CA."IdEstado"											AS "Estado"
FROM (	SELECT   	 
			CPC."IdCargoAbonoMovimiento"
			,CPC."IdTipoMovimiento"
			,CPC."IdConcepto"
			,CPC."NombreConcepto"
			,CPC."IdCuentaInversion"
			,CPC."TipoCliente"
			,CPC."IdCliente"
			,CPC."DocumentoIdentidadCliente"
			,CPC."NombreCliente"
			,CPC."RazonSocial"
			,CPC."IdPortafolio"
			,CPC."CodigoPortafolio"
			,CPC."Proposito"
			,CPC."CodigoProposito"
			,CPC."IdMoneda"
			,CPC."CodigoMoneda"
			,CPC."NombreMoneda"
			,CPC."CodigoNombreMoneda"
			,CPC."IdCondicionDeLiquidacion"
			,CPC."FechaEfectivaMovimiento"
			,CPC."Monto"
			,CPC."SaldoLibroInventario"
			,CPC."AntiguedadDeuda"
			,CPC."IdEstado"
		FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentasPorPagarCobrar" as CPC
		WHERE CPC."SaldoLibroInventario" > 0 
		AND CPC."IdTipoMovimiento" = 'MOVABN' 	   
		AND CPC."IdEstado" = 'CRT'
		AND CPC."FechaEfectivaMovimiento" 			= :FechaContable
		AND (NULL IS NULL OR CPC."IdCliente" 		= NULL)
		AND (NULL IS NULL OR CPC."IdPortafolio"	= NULL)
	) AS CA 								
 	LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS CIV
				 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
		  LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
				 ON POR."IdPortafolio"	= CIV."IdPortafolio" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
				 ON CLI."IdCliente"		= POR."IdCliente" 
		  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
				 ON PER."IdPersona"		= CLI."IdPersona"
	WHERE 
		   	   CA."AntiguedadDeuda" > 0
		   AND CA."IdEstado" IN ('CRT')						   