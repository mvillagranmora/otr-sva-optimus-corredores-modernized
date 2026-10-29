
SELECT
DISTINCT 
	TO_BIGINT(:IdTipoCentralizacion)				AS "IdTipoCentralizacion",
	CA."IdCargoAbonoMovimiento" 					AS "IdMovimiento", 
  	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'ABONO_CTA_INVERSION'
		ELSE 'CARGO_CTA_INVERSION'
	END												AS "IdEvento",
  	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'Abono Cuenta Inversión'
 		ELSE 'Cargo Cuenta Inversión' 
 	END												AS "Nombre",
  	CA."Monto"	 									AS "MontoEvento",
  	CA."IdMoneda" 									AS "CodigoMoneda"
  	
  FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" AS CA
  LEFT JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" AS CIV
	 ON CIV."IdCuentaInversion" = CA."IdCuentaInversion"
  LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS POR
	 ON POR."IdPortafolio"	= CIV."IdPortafolio" 
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI
	 ON CLI."IdCliente"		= POR."IdCliente" 
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER
	 ON PER."IdPersona"		= CLI."IdPersona"
 WHERE CA."FechaSistema" = :FechaContable
   AND CA."SaldoLibroInventario" = 0
   AND CA."IdEstado" IN ('CRT')

UNION 

SELECT 
DISTINCT
	TO_BIGINT(:IdTipoCentralizacion)				AS "IdTipoCentralizacion",
	CA."IdCargoAbonoMovimiento" 					AS "IdMovimiento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'MOROSIDAD_ABONO_CTA_INVERSION'
		ELSE 'MOROSIDAD_CARGO_CTA_INVERSION'
	END												AS "IdEvento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'Morosidad Abono Cuenta Inversión'
		ELSE 'Morosidad Cargo Cuenta Inversión' 
	END												AS "Nombre",
	CA."Monto"	 									AS "MontoEvento",	
	CA."IdMoneda" 									AS "CodigoMoneda"	

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
	
	

UNION 

SELECT
DISTINCT
	TO_BIGINT(:IdTipoCentralizacion)				AS "IdTipoCentralizacion",
	CA."IdCargoAbonoMovimiento" 					AS "IdMovimiento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'MOROSIDAD_ABONO_CTA_INVERSION'
		ELSE 'MOROSIDAD_CARGO_CTA_INVERSION'
	END												AS "IdEvento",
	CASE CA."IdTipoMovimiento" 		
		WHEN 'MOVABN' THEN 'Morosidad Abono Cuenta Inversión'
		ELSE 'Morosidad Cargo Cuenta Inversión' 
	END												AS "Nombre",
	CA."Monto"	 									AS "MontoEvento",
	CA."IdMoneda" 									AS "CodigoMoneda"
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
	
	
	