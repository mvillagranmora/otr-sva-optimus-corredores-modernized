SELECT 
		EvCapCli."IdMoneda" 													as "IdMoneda",
		EvCapCli."Total"														AS "MontoMonedaInstrumento",
		EvCapCli."Total"														AS "MontoMonedaSistema",
		ConPortafolio."IdCliente" 												as "IdCliente", 
		EvCapCli."IdPortafolio" 												as "IdPortafolio",
		EvCapCli."IdTipoInstrumento" 											as "IdTipoInstrumento",
		EvCapCli."IdTipoInstrumento" 											as "IdBolsa",
		EvCapCli."IdInstrumento"												as "IdInstrumentoGarantizado",	
		--						'DIEVAC' 							as "IdConcepto",
		'CIECDV' 																as "IdConcepto",
		COALESCE(Gtia."IdContraparteGarantia",0)								as "IdContraparteGarantia",
		'INGGTA'																as "IdTipoOperacion",
		TO_BIGINT(0)															as "IdOperacion",
		(EvCapCli."MontoBeneficioArchivo"- EvCapCli."MontoRetencionArchivo")	AS "MontoMenosMontoRetenido",
		EvCapCli."MontoBeneficioArchivo"										AS "MontoBeneficioArchivo"
FROM "BSCL.Custodia.BD::CUS.EventoCapital"										AS EvCap
	INNER JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" 					AS EvCapCli
		ON EvCap."IdEventoCapital" = EvCapCli."IdEventoCapital" 
	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 				AS ConPortafolio
		ON EvCapCli."IdPortafolio" = ConPortafolio."IdPortafolio"
	LEFT JOIN "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantias"				as Gtia
		ON EvCapCli."IdCartera" = Gtia."IdGarantia"
	   AND EvCapCli."TipoCartera" = 'GAR'
WHERE  	EvCap."IdEventoCapital" = :IdEventoCapital
	and EvCapCli."Total" > 0
	and EvCapCli."TipoCartera" = 'GAR'
	AND (EvCapCli."IdEstado" = 'PRO' OR EvCapCli."IdEstado" = 'NOT')