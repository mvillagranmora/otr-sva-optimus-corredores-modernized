
WITH EventoCapitalCliente AS
(
	SELECT	
	     EC."IdEventoCapital"
		,ECC."IdEventoCapitalCliente"
		,SN."IdPersona"				
		,ECC."IdPortafolio"				
		,ECC."IdMoneda"
		,ECC."FechaCierre"
		,ECC."IdInstrumento"
		,ECC."IdTipoInstrumento"
		,ECC."IdClasificacionTipoInstrumento"
		,ECC."IdBoveda"
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
		,ECC."Amortizacion"
		,CAST(0 AS DECIMAL(26,8)) AS "Retencion"
		,ECC."PagaClienteCorredor"
		,ECFP."IdMedioDePago"   -- Siempre traerá valor ya que el procesar la llena
				    
	FROM "BSCL.Custodia.BD::CUS.EventoCapital"	 					   			AS EC
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente"	 					AS ECC  ON (EC."IdEventoCapital" = ECC."IdEventoCapital" AND EC."IdTipoEventoCapital" = ECC."IdTipoEventoCapital" )
	INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 						AS SN   ON (SN."IdSocioDeNegocio" = ECC."IdSocioNegocio")
	LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP ON (ECC."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente")
				
	WHERE EC."IdEventoCapital"      = :IdEventoCapital				
 	AND   EC."IdTipoEventoCapital" 	= :IdTipoEventoCapital			 	
 	AND   ECC."TipoSocioNegocio"    = 'CLI'   -- Cliente
	AND   ECC."Total" > 0
)


SELECT
     "IdEventoCapital"
 	,"IdEventoCapitalCliente"
	,"IdPersona"
	,"IdPortafolio"
	,"IdMoneda"
	,"FechaCierre"
	,"IdInstrumento"
	,"IdTipoInstrumento"
	,"IdClasificacionTipoInstrumento"
	,"IdBoveda"
	,"Monto"
	,"MontoInteres"
	,"Amortizacion"
	,"Retencion"
	,"PagaClienteCorredor"
	,"IdMedioDePago"
FROM EventoCapitalCliente;


