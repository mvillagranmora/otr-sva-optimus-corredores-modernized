SELECT	ECC."IdEventoCapitalCliente"					AS "NumeroReferencia"
,COALESCE(ECFP."IdEventoCapitalClienteFormaDePago",ECC."IdEventoCapitalCliente")		AS "NumeroReferenciaFormaDePago"
,ECFP."IdMedioDePago"							as "IdMedioDePago"
,ECFP."IdModalidadPago"							As "IdModalidadDePago"
,ECC."IdMoneda"									as "IdMoneda"
,CASE WHEN (ECC."ISFUT"='S' OR ECC."RetencionISFUT" = 'S') AND ECC."MontoRetencionISFUT" > 0
		THEN SUM(ECC."Total" - ECC."MontoRetencionISFUT" )
		ELSE SUM(ECC."Total") END											AS "Monto"
,TO_BIGINT(0)												as "IdCuentaBancariaCorredora"
,TO_BIGINT(0) 												as "IdTarjetaBancariaCorredora"
,TO_BIGINT(0) 												as "IdChequera"
,TO_BIGINT(0) 												as "NumeroDocumentoCorredora"
,'0001-01-01' 									as "FechaDocumentoPagoCorredora"
,ECFP."IdCuentaBancaria" 						as "IdCuentaBancariaSocio"
,ECFP."IdBanco" 								as "IdBancoSocio"
,ECFP."NombreCuentaBancaria" 					as "NombreCuentaBancariaSocio"
,ECFP."NumeroCuentaBancaria" 					as "NumeroCuentaBancariaSocio"
,ECFP."IdTipoCuentaBancaria" 					as "IdTipoCuentaBancariaSocio"
,ECFP."NombreTipoCuentaBancaria" 				as "NombreTipoCuentaBancariaSocio"
,TO_BIGINT(0) 												as "IdTarjetaBancariaSocio"
,ECFP."IdTipoTarjetaBancaria" 					as "IdTipoTarjetaBancariaSocio"
,'' 											as "NombreTipoTarjetaBancariaSocio"
,ECFP."NumeroTarjetaBancaria" 					as "NumeroTarjetaBancariaSocio"
,''												as "NumeroSerieSocio"
,TO_BIGINT(0)												as "NumeroDocumentoSocio"
,'0001-01-01'									as "FechaDocumentoPagoSocio"
FROM "BSCL.Custodia.BD::CUS.EventoCapitalCliente"								AS ECC
LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalClienteFormaDePago"			AS ECFP
ON ECC."IdEventoCapitalCliente" = ECFP."IdEventoCapitalCliente"
WHERE  	ECC."IdEventoCapital" = :IdEventoCapital
and ECC."Total" > 0
and ECFP."IdMedioDePago" <> 'CIN'
and (ECC."IdEstado" = 'PRO' OR ECC."IdEstado" = 'NOT')
group by 	ECC."IdEventoCapitalCliente",
    ECFP."IdEventoCapitalClienteFormaDePago",
	ECC."IdPortafolio", 
	ECFP."IdMedioDePago", 
	ECFP."IdModalidadPago",
	ECC."IdMoneda",
	ECFP."IdCuentaBancaria",
	ECFP."IdBanco",
	ECFP."NombreCuentaBancaria", 
	ECFP."NumeroCuentaBancaria",
	ECFP."IdTipoCuentaBancaria",
	ECFP."NombreTipoCuentaBancaria",
	ECFP."IdTipoTarjetaBancaria",
	ECFP."NumeroTarjetaBancaria",
	ECC."ISFUT",
	ECC."RetencionISFUT",
	ECC."MontoRetencionISFUT"