SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1949.Origen" (

		"IdEmpresa",
		"IdEventoCapitalCliente",
		"Mercado",
		"Nemo",
		"TipoRegistro",
		"FecMovto",
		"SecEvento",
		"RutSociedad",
		"TipoSociedad",
		"RutCliente",
		"Cuenta",
		"CantCuotas3112",
		"Monto",
		"MontoActualizado",
		"MtoDivReaAfecGCCCIDPC_C6" ,
		"MtoDivReaAfecGCCCIDPCAL311216_C7",
		"MtoDivReaAfecGCCDIDPCVol_C8",
		"MtoDivReaAfecGCSinDer_C9",
		"MtoDivReaREXTribCumpRentasRap_C10",
		"MtoDivReaREXTribCumpOtrasRentas_C11",
		"MtoDivReaREXTribCumpExcDist_C12" ,
		"MtoDivReaREXTribCumpUtiISFUT_C13" ,
		"MtoDivReaREXTribCumpRentasISFUT_C14",
		"MtoDivReaREXRentExIGCAfecIAart11_C15",
		"MtoDivReaREXRentExIGCIA_C16",
		"MtoDivReaREXNoConstRenta_C17" ,
		"CIGCDesde01012017RentAfecNoRestSDD_C18",
		"CIGCDesde01012017RentAfecNoRestCDD_C19",
		"CIGCDesde01012017RentAfecNoRestSDD_C20",
		"CIGCDesde01012017RentAfecNoRestCDD_C21",
		"CIGCDesde01012017RentAfecRestSDD_C22",
		"CIGCDesde01012017RentAfecRestCDD_C23",
		"CIGCDesde01012017RentExenRestSDD_C24",
		"CIGCDesde01012017RentExenRestCDD_C25",
		"CIGCDesde01012017CredIPE_C26",
		"CIGCHasta31122016RentAfecSDD_C27",
		"CIGCHasta31122016RentAfecCDD_C28",
		"CIGCHasta31122016RentExenSDD_C29",
		"CIGCHasta31122016RentExenCDD_C30",
		"CIGCHasta31122016CredIPE_C31",
		"CIGCImptoTasaAdicional_C32",
		"TasaEfectCredFUT",
		"TasaEfectCredFUNT",
		"DevCapArt17N7LIR_C33",
		"NumCertificado_C34",
		"IncluirEnDJ",
		"VBUsuario",
		"Observacion",
		"UsrUltMod",
		"FecUltMod",				
		"FechaPago",
		"Emisor"
		,"AcogidoISFUT"
	
) AS

	SELECT
		LG."IdEmpresa" AS "IdEmpresa",
		LG."IdEventoCapitalCliente",
		'AC' AS "Mercado",
		LG."Nemo" AS "Nemo",--Nemo
		'P' AS "TipoRegistro",  --- TipoRegistro Pagado
		LG."FecMovto" AS "FecMovto",
		LG."SecEvento" AS "SecEvento",  --- SecEvento
		 COALESCE(ltrim(RTRIM(peremi."DocumentoIdentidad")),'') AS "RutSociedad", --RutSociedad
		CASE WHEN INS."EstaInscrito"='S' THEN 'A' ELSE 'B' END AS "TipoSociedad", -- Se corroboro con ruebas, los Inscritos estan en tbassoc, los otros no
		trim(LPAD(LG."RutCliente",11,' ')) AS "RutCliente",
		CASE WHEN LG."CodigoPortafolio" = '110' THEN '50' ELSE LG."CodigoPortafolio" END AS "Cuenta",
		LG."CantCuotas3112",
		LG."Monto", ----Monto
		0 AS    "MontoActualizado",--MontoActualizado
		0 AS 	"MtoDivReaAfecGCCCIDPC_C6" ,
		0 AS 	"MtoDivReaAfecGCCCIDPCAL311216_C7",
		0 AS 	"MtoDivReaAfecGCCDIDPCVol_C8",
		0 AS 	"MtoDivReaAfecGCSinDer_C9",
		0 AS 	"MtoDivReaREXTribCumpRentasRap_C10",
		0 AS 	"MtoDivReaREXTribCumpOtrasRentas_C11",
		0 AS 	"MtoDivReaREXTribCumpExcDist_C12" ,
		0 AS 	"MtoDivReaREXTribCumpUtiISFUT_C13" ,
		0 AS 	"MtoDivReaREXTribCumpRentasISFUT_C14",
		0 AS 	"MtoDivReaREXRentExIGCAfecIAart11_C15",
		0 AS 	"MtoDivReaREXRentExIGCIA_C16",
		0 AS 	"MtoDivReaREXNoConstRenta_C17" ,
		0 AS 	"CIGCDesde01012017RentAfecNoRestSDD_C18",
		0 AS 	"CIGCDesde01012017RentAfecNoRestCDD_C19",
		0 AS 	"CIGCDesde01012017RentAfecNoRestSDD_C20",
		0 AS 	"CIGCDesde01012017RentAfecNoRestCDD_C21",
		0 AS 	"CIGCDesde01012017RentAfecRestSDD_C22",
		0 AS 	"CIGCDesde01012017RentAfecRestCDD_C23",
		0 AS 	"CIGCDesde01012017RentExenRestSDD_C24",
		0 AS 	"CIGCDesde01012017RentExenRestCDD_C25",
		0 AS 	"CIGCDesde01012017CredIPE_C26",
		0 AS 	"CIGCHasta31122016RentAfecSDD_C27",
		0 AS 	"CIGCHasta31122016RentAfecCDD_C28",
		0 AS 	"CIGCHasta31122016RentExenSDD_C29",
		0 AS 	"CIGCHasta31122016RentExenCDD_C30",
		0 AS 	"CIGCHasta31122016CredIPE_C31",
		0 AS 	"CIGCImptoTasaAdicional_C32",
		0 AS 	"TasaEfectCredFUT",
		0 AS 	"TasaEfectCredFUNT",
		0 AS 	"DevCapArt17N7LIR_C33",
		0 AS 	"NumCertificado_C34",
		'S' AS "IncluirEnDJ",--IncluirEnDJ			char
		'' AS "VBUsuario",--VBUsuario				varchar
		'' AS "Observacion",--Observacion			varchar
		'' AS "UsrUltMod", --UsrUltMod		varchar
		CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime				
		LG."FechaPago" AS "FechaPago", -- Fecha de Pago del Evento
		emi."IdEmisor" AS "Emisor"
		--'' AS "CodMovto"--- CodMovto
		,LG."AcogidoISFUT"
	FROM 
 
(	
		SELECT      ec."IdEmpresa" AS "IdEmpresa" ,
					ec."IdEventoCapitalCliente",
					'AC' AS "Mercado",
					ec."Nemo" AS "Nemo",--Nemo
					'P' AS "TipoRegistro",  --- TipoRegistro Pagado
					ec."FechaPago" AS "FecMovto",
					ec."Secuencia" AS "SecEvento",  --- SecEvento
					'B' AS "TipoSociedad",
					LPAD(ec."DocumentoIdentidad",11,' ') AS "RutCliente",
					ec."CodigoPortafolio",
					COALESCE(CUST."INGCantidad",0)  AS "CantCuotas3112",
					CASE WHEN ec."IdMoneda" = 'USD' THEN ROUND((ec."Total" * COALESCE(DOLAR."Valor", 1)),0) ELSE ec."Total" END AS "Monto", ----Monto
					ec."FechaPago" AS "FechaPago" -- Fecha de Pago del Evento
					,ec."ISFUT" AS "AcogidoISFUT"
					,ec."IdInstrumento"
	FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"  ec  
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = ec."IdCliente"
	LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais" pais ON pais."IdPais" = cli."IdPaisResidencia"
	LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS DOLAR ON DOLAR."Fecha" = ec."FechaPago" AND DOLAR."IdParametroFinanciero" = 'DO'	
	LEFT JOIN (
			SELECT HISFEC."FechaRespaldo", SUM(HISFEC."INGCantidad") AS "INGCantidad" ,HISFEC."IdPortafolio",HISFEC."Nemotecnico", HISFEC."IdCliente", HISFEC."IdInstrumento"
			FROM (
					SELECT H."FechaRespaldo", H."INGCantidad",H."IdPortafolio", H."Nemotecnico", H."IdCliente", H."IdInstrumento"
					,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (custodia final del periodo)
					FROM "BSCL.Custodia.BD::HIS.Cartera" H
					INNER JOIN
						(			
							SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos)
							(
								SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
								WHERE   "IdClasificacionTipoInstrumento"='RV' AND "IdTipoInstrumento" IN ('ACN') AND MONTH("FechaRespaldo")='12'
								GROUP BY YEAR("FechaRespaldo")
								ORDER BY YEAR("FechaRespaldo") DESC
							)
						)GRFECHA	
					ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
					AND 	"IdEstado" = 'VIG'
					AND 	"INGMontoATasaMercadoActual" > 0
					AND 	"IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') 
				 )HISFEC WHERE HISFEC.RANK = 1
				 GROUP BY HISFEC."FechaRespaldo", HISFEC."IdCliente", HISFEC."IdPortafolio", HISFEC."Nemotecnico", HISFEC."IdInstrumento") CUST
				 ON ec."IdCliente" = CUST."IdCliente"
				 	AND ec."IdPortafolio" = CUST."IdPortafolio"		
					AND ec."Nemo" = CUST."Nemotecnico"
					AND YEAR(ec."FechaPago") = YEAR(CUST."FechaRespaldo")
					AND ec."IdInstrumento" = CUST."IdInstrumento"
	WHERE 		ec."IdClasificacionTipoInstrumento" = 'RV' 
	AND 	ec."IdTipoInstrumento" IN ('ACN')
	AND 	ec."IdTipoEventoCapital"  in('DV','RC')
	AND 	ec."Total" > 0
	AND 	(ec."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R') AND ec."Portafolio" NOT IN ('REGAPV'))
	AND 	ec."TipoSocioNegocio" = 'CLI'
	AND 	ec."IdEstado" = 'CER'
	
) LG
	LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON ins."IdInstrumento" = trim(LG."IdInstrumento")
	LEFT  JOIN "BSCL.Plataforma.BD::MER.Emisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
	LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" peremi ON (peremi."IdPersona"=emi."IdPersona")
WHERE LG."Monto">0

	
WITH read ONLY;