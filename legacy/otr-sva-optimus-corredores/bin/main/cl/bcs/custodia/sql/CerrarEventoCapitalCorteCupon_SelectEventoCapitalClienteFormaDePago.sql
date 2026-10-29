

WITH EventoCapitalClienteFormaDePago AS
(
	SELECT
		 ECC."IdEventoCapitalCliente"
		,ECFP."IdMedioDePago"
		,ECFP."IdModalidadPago" AS "IdModalidadDePago"		       
		,ECC."IdMoneda"
		
		,CASE :LocalizacionSistema
			WHEN 'PE' THEN
				CASE ECC."IdBoveda"
					WHEN 'CAV' THEN
						CAST(ECC."MontoBeneficioArchivo" - ECC."MontoRetencionArchivo" AS DECIMAL(26,8))
					ELSE
						CAST(ECC."Total" AS DECIMAL(26,8))
				END
			ELSE
				CAST(ECC."Total" AS DECIMAL(26,8))					
		END AS "Monto"
		,ECC."MontoInteres"
		,CAST(0 AS DECIMAL(26,8)) AS "Retencion"	
		
		,ECFP."IdCuentaBancaria"  		 as "IdCuentaBancariaSocio"
		,ECFP."IdBanco"  				 as "IdBancoSocio"
		,ECFP."NombreCuentaBancaria" 	 as "NombreCuentaBancariaSocio"
		,ECFP."NumeroCuentaBancaria" 	 as "NumeroCuentaBancariaSocio"
		,ECFP."IdTipoCuentaBancaria" 	 as "IdTipoCuentaBancariaSocio"
		,ECFP."NombreTipoCuentaBancaria" as "NombreTipoCuentaBancariaSocio"
		,ECFP."IdTipoTarjetaBancaria" 	 as "IdTipoTarjetaBancariaSocio"
		,ECFP."NumeroTarjetaBancaria" 	 as "NumeroTarjetaBancariaSocio"
		
		
	FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					AS ECC  ON (ECC."IdEventoCapital" = EC."IdEventoCapital" AND ECC."IdTipoEventoCapital" = EC."IdTipoEventoCapital" )	
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP ON (ECFP."IdEventoCapitalCliente" = ECC."IdEventoCapitalCliente")
	
	
	WHERE EC."IdEventoCapital"      = :IdEventoCapital				
	AND   EC."IdTipoEventoCapital" 	= :IdTipoEventoCapital			 	
	AND   ECC."TipoSocioNegocio"    = 'CLI'   -- Cliente
	AND   ECC."Total" > 0
)



SELECT
	 "IdEventoCapitalCliente"		    	
	,"IdMedioDePago"
	,"IdModalidadDePago"
	,"IdMoneda"						
    ,"Monto"
	,"MontoInteres"
	,"Retencion"				        
	,"IdCuentaBancariaSocio"
	,"IdBancoSocio"
	,"NombreCuentaBancariaSocio"
	,"NumeroCuentaBancariaSocio"
	,"IdTipoCuentaBancariaSocio"
	,"NombreTipoCuentaBancariaSocio"
	,"IdTipoTarjetaBancariaSocio"
	,"NumeroTarjetaBancariaSocio"
FROM EventoCapitalClienteFormaDePago;

