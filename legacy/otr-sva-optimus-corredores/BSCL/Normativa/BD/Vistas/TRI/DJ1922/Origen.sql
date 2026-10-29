SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Origen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Origen" (
     "IdEmpresa"
    ,"IdOperacion"
    ,"RutAdministradora"
    ,"IdEmisor"								
	,"RutFondo"										
	,"RunFondo"
	,"Fondo"									
	,"TipoFondo"	
	,"RutCliente"									
	,"NombreParticipe"								
	,"TaxId"											
	,"CodigoPais"									
	,"TipoOperacion"									
	,"FechaInversion"								
	,"MonedaReajuste"								
	,"MontoNominalAportes"	
	,"MontoNominalAportesActualizado"						
	,"NumeroSerieCuotas"								
	,"CantidadCuotasParticipe"						
	,"ParticipeCumpleRequisitos"						
	,"CuotasCumplenRequisitos"						
	,"ValorCuotaPesos"
	,"NumCuotasSerieParticipe"
	,"PorcentajeParticipacion31Dic"					
	,"ValNominalCuotasRescatadas"					
	,"CantidadCuotasRescatadas"			
	,"SecEvento"
	,"DiferenciaPorRescateOEnajenacion"
	,"DiferenciaPorRescateOEnajenacionActualizado"				
	,"DifPorRescateOEnajenacionCFIAcogido107"
	,"DifPorRescateOEnajenacionCFIAcogido107Actualizado"		
	,"ValNominalInversionaFechaLiquidacion"			
	,"CantCuotasCedidasPorLiquidacion"				
	,"DiferenciaValorCuotasFechaLiquidacion"
	,"DiferenciaValorCuotasFechaLiquidacionActualizado"			
	,"DividendosAfectosIGCoAdicional"
	,"DividendosAfectosIGCoAdicionalActualizado"				
	,"DividendosExentosIGCoAdicional"
	,"DividendosExentosIGCoAdicionalActualizado"				
	,"DividendosNoConstitutivosDeRenta"
	,"DividendosNoConstitutivosDeRentaActualizado"				
	,"DividendosAfectosImptoUnicoTasa10"
	,"DividendosAfectosImptoUnicoTasa10Actualizado"				
	,"DividendosNoAfectosImptoUnicoTasa10"
	,"DividendosNoAfectosImptoUnicoTasa10Actualizado"			
	,"DividendosAfectosImptoUnicoTasa4"	
	,"DividendosAfectosImptoUnicoTasa4Actualizado"			
	,"DistribucionXDiminucionNoImputadaCapital"		
	,"DevolucionesDeCapital"
	,"DevolucionesDeCapitalActualizado"							
	,"IncrementoPorImpuesto"							
	,"CreditoImpto1CatConDevolucionAfectas"			
	,"CreditoImpto1CatSinDevolucionAfectas"			
	,"CreditoImpto1CatConDevolucionExentas"			
	,"CreditoImpto1CatSinDevolucionExentas"			
	,"CreditoPorImpuestosExternos"					
	,"ImpuestosTasaAdicionalExArticulo21"			
	,"MontoInversionenCuotasCFIAnterior93"			
	,"NumeroCertificado"
	,"FactorActualizacion"
	,"FechaOrigenInversion"	
	,"IdCliente"
	,"IdPortafolio"
	,"ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"Numero"
   	,"TipoDocumento"
	,"NumeroDocumento"
	,"FechaDocumento"
	,"IdBaseDocumento"
	,"TipoTransaccion"
	,"IdTipoOperacion"
	,"IdTipoProducto"
	,"TributaSa"
	,"DivRemDistAfectGCConIDPC"  --DivRemDistAfectGCConIDPC
	,"DivRemDistAfectGCConIDPCal31122016"  --DivRemDistAfectGCConIDPCal31122016
	,"DivRemDistAfectGCConIDPCVOL"  --DivRemDistAfectGCConIDPCVOL
	,"DivRemDistAfectGCSINDC"  --DivRemDistAfectGCSINDC
	,"DivRemDistNoRentaFUT"  --DivRemDistNoRentaFUT
	,"DivRemDistNoRentaFE"  --DivRemDistNoRentaFE
	,"CredImpGloCompNSRSD"  --CredImpGloCompNSRSD
	,"CredImpGloCompNSRCD"  --CredImpGloCompNSRCD
	,"CredImpGloCompSRSD"  --CredImpGloCompSRSD
	,"CredImpGloCompSRCD"  --CredImpGloCompSRCD
	,"CredImpGloCompCTDCIF"  --CredImpGloCompCTDCIF
	,"CredImpGloCompCDD31122016"  --CredImpGloCompCDD31122016
	,"CredImpGloCompSDD31122016"  --CredImpGloCompSDD31122016
	,"CredImpGloCompCTDCIF31122016"  --CredImpGloCompCTDCIF31122016
	,"CredImpGloCompCITAExArt21"  --CredImpGloCompCITAExArt21                
	,"DisRemRentasExIGCAfecImptoAdi_C100"  --DisRemRentasExIGCAfecImptoAdi_C100
	,"CredIGCAcumD01012017RANSRD01012020SDD_C101"  --CredIGCAcumD01012017RANSRD01012020SDD_C101
	,"CredIGCAcumD01012017RANSRD01012020CDD_C102"  --CredIGCAcumD01012017RANSRD01012020CDD_C102
	,"CredIGCAcumD01012017RXSRSDD_C103"  --CredIGCAcumD01012017RXSRSDD_C103
	,"CredIGCAcumD01012017RXSRCDD_C104"  --CredIGCAcumD01012017RXSRCDD_C104
	,"CredIGCAcumH31122016RXSDD_C105"  --CredIGCAcumH31122016RXSDD_C105
	,"CredIGCAcumH31122016RXCDD_C106"  --CredIGCAcumH31122016RXCDD_C106
	,"DisRemRentasTribCProvRap_C96"  --DisRemRentasTribCProvRap_C96
	,"DisRemRentasTribCORP_C97"  --DisRemRentasTribCORP_C97
	,"DisRemRentasTribCExDD_C98"  --DisRemRentasTribCExDD_C98
	,"DisRemRentasTribCUtAFFut_C99"  --DisRemRentasTribCUtAFFut_C99			
	,"DetBenNetoPerRutFdo_C107" --DetBenNetoPerRutFdo_C107
	,"DetBenNetoPerPOS_C113"  --DetBenNetoPerPOS_C113
	,"DetBenNetoPerNEG_C114"  --DetBenNetoPerNEG_C114
	,"DetBenNetoUtilPer_C133"  --DetBenNetoUtilPer_C133
	,"DetBenNetoIntPer_C134"  --DetBenNetoIntPer_C134
	,"DetBenNetoDivPer_C135"  --DetBenNetoDivPer_C135
	,"DetBenNetoGanCapPer_C136"  --DetBenNetoGanCapPer_C136
	,"DetBenNetoPerGastDev_C137"  --DetBenNetoPerGastDev_C137
	,"NumSerieCuotasParticipe_C138" --"NumSerieCuotasParticipe_C138"
	,"AcogidoISFUT"
	,"RunNemotecnico"	--C139
	,"TipoContribuyente" --C138
) AS
SELECT
     LG."IdEmpresa" AS "IdEmpresa"	
    ,LG."SecMovto" AS "IdOperacion"						
	,COALESCE(ltrim(RTRIM(peremi."DocumentoIdentidad")),'') AS "RutAdministradora"
	,COALESCE(emi."IdEmisor",'') AS "IdEmisor"
	,COALESCE(Rutfondos."ValorDestino",'') AS "RutFondo"
	,CASE LG."OrigenDet" WHEN 'OPT_FM' THEN fondo."CodigoSVS" ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END  AS "RunFondo" 	
	,LG."Fondo" AS "Fondo"									
	,LG."TipoFondo"	 AS "TipoFondo"				
	,cli."DocumentoIdentidad" AS "RutCliente"						
	,cli."NombreCompleto" AS "NombreParticipe"								
	,'CL' AS "TaxId"											
	,COALESCE (pais."CodigoPais", 'CL') AS "CodigoPais"									
	,LG."TipoOperacion" as "TipoOperacion" 			
	,LG."FechaInversion" AS "FechaInversion"								
	,LG."MonedaReajuste" AS "MonedaReajuste"								
	,LG."Monto" AS "MontoNominalAportes"
	,LG."MontoActualizado"  AS "MontoNominalAportesActualizado"							
	,LG."NumSeriesCuotasParticipe" AS "NumeroSerieCuotas" --pendiente 	--lectura interna							
	,LG."NumCuotasSerieParticipe" AS "CantidadCuotasParticipe"						
	,LG."ParticipeCumpleReq"  AS "ParticipeCumpleRequisitos" --pendiente		--lectura interna		
	,LG."CuotasCumplenReq" AS "CuotasCumplenRequisitos" --pendiente	 			
	,LG."ValorCuotaPesos" AS "ValorCuotaPesos"	
	,LG."NumCuotasSerieParticipe" AS "NumCuotasSerieParticipe"
	,0 AS "PorcentajeParticipacion31Dic"					
	,LG."ValNominalCuotasResc" AS "ValNominalCuotasRescatadas"					
	,LG."CantCuotasResc" AS "CantidadCuotasRescatadas"				
	,LG."SecEvento"
	,0 AS "DiferenciaPorRescateOEnajenacion"
	,CASE WHEN COALESCE(ins."AfectoArt107",'N')='S' AND pres."NEMO" IS NOT NULL THEN  0 ELSE LG."DifRescateCuotasAct" END  AS "DiferenciaPorRescateOEnajenacionActualizado"				
	,0 AS "DifPorRescateOEnajenacionCFIAcogido107"
	,CASE WHEN COALESCE(ins."AfectoArt107",'N')='S' AND pres."NEMO" IS NOT NULL THEN  LG."DifRescateCuotasActFi" ELSE 0 END  AS "DifPorRescateOEnajenacionCFIAcogido107Actualizado"		
	,COALESCE(LG."ValNominalInversion",0) AS "ValNominalInversionaFechaLiquidacion"			
	,LG."CantCuotasCedidas" AS "CantCuotasCedidasPorLiquidacion"				
	,0 AS "DiferenciaValorCuotasFechaLiquidacion"
	,LG."DifValCuotas" AS "DiferenciaValorCuotasFechaLiquidacionActualizado"			
	,0 AS "DividendosAfectosIGCoAdicional"
	,0 AS "DividendosAfectosIGCoAdicionalActualizado"				
	,0 AS "DividendosExentosIGCoAdicional"
	,0 AS "DividendosExentosIGCoAdicionalActualizado"				
	,0 AS "DividendosNoConstitutivosDeRenta"
	,0 AS "DividendosNoConstitutivosDeRentaActualizado"				
	,0 AS "DividendosAfectosImptoUnicoTasa10"
	,0 AS "DividendosAfectosImptoUnicoTasa10Actualizado"				
	,0 AS "DividendosNoAfectosImptoUnicoTasa10"
	,0 AS "DividendosNoAfectosImptoUnicoTasa10Actualizado"			
	,0 AS "DividendosAfectosImptoUnicoTasa4"
	,0 AS "DividendosAfectosImptoUnicoTasa4Actualizado"				
	,0 AS "DistribucionXDiminucionNoImputadaCapital"		
	,0 AS "DevolucionesDeCapital"
	,0 AS "DevolucionesDeCapitalActualizado"							
	,0 AS "IncrementoPorImpuesto"							
	,0 AS "CreditoImpto1CatConDevolucionAfectas"			
	,0 AS "CreditoImpto1CatSinDevolucionAfectas"			
	,0 AS "CreditoImpto1CatConDevolucionExentas"			
	,0 AS "CreditoImpto1CatSinDevolucionExentas"			
	,0 AS "CreditoPorImpuestosExternos"					
	,0 AS "ImpuestosTasaAdicionalExArticulo21"			
	,0 AS "MontoInversionenCuotasCFIAnterior93"			
	,0 AS "NumeroCertificado"
	,0 AS "FactorActualizacion"
	,LG."FechaInversion" AS "FechaOrigenInversion"
	,LG."IdCliente" 		AS "IdCliente"
    ,LG."IdPortafolio" 		AS "IdPortafolio"
    ,0 AS "ID"
    ,0 AS "IdDocumento"
    ,'' AS "VboUsuario"
    ,'' AS "Observaciones"
    ,'' AS "FuenteDatos"
   	,0 AS "Numero"
   	,'' AS "TipoDocumento"
	,0 AS "NumeroDocumento"
	,'1900-01-01' AS "FechaDocumento"
	,0 AS "IdBaseDocumento"
	,'' AS "TipoTransaccion"
	,'' AS "IdTipoOperacion"
	,'' AS "IdTipoProducto"
	,LG."TributaSa" AS "TributaSa"
	,0 as "DivRemDistAfectGCConIDPC"  --DivRemDistAfectGCConIDPC
	,0 as "DivRemDistAfectGCConIDPCal31122016"  --DivRemDistAfectGCConIDPCal31122016
	,0 as "DivRemDistAfectGCConIDPCVOL"  --DivRemDistAfectGCConIDPCVOL
	,0 as "DivRemDistAfectGCSINDC"  --DivRemDistAfectGCSINDC
	,0 as "DivRemDistNoRentaFUT"  --DivRemDistNoRentaFUT
	,0 as "DivRemDistNoRentaFE" --DivRemDistNoRentaFE
	,0 as "CredImpGloCompNSRSD"  --CredImpGloCompNSRSD
	,0 as "CredImpGloCompNSRCD"  --CredImpGloCompNSRCD
	,0 as "CredImpGloCompSRSD"  --CredImpGloCompSRSD
	,0 as "CredImpGloCompSRCD"  --CredImpGloCompSRCD
	,0 as "CredImpGloCompCTDCIF"  --CredImpGloCompCTDCIF
	,0 as "CredImpGloCompCDD31122016"  --CredImpGloCompCDD31122016
	,0 as "CredImpGloCompSDD31122016"  --CredImpGloCompSDD31122016
	,0 as "CredImpGloCompCTDCIF31122016"  --CredImpGloCompCTDCIF31122016
	,0 as "CredImpGloCompCITAExArt21"  --CredImpGloCompCITAExArt21                
	,0 as  "DisRemRentasExIGCAfecImptoAdi_C100"  --DisRemRentasExIGCAfecImptoAdi_C100
	,0 as  "CredIGCAcumD01012017RANSRD01012020SDD_C101"  --CredIGCAcumD01012017RANSRD01012020SDD_C101
	,0 as  "CredIGCAcumD01012017RANSRD01012020CDD_C102"  --CredIGCAcumD01012017RANSRD01012020CDD_C102
	,0 as  "CredIGCAcumD01012017RXSRSDD_C103"  --CredIGCAcumD01012017RXSRSDD_C103
	,0 as  "CredIGCAcumD01012017RXSRCDD_C104"  --CredIGCAcumD01012017RXSRCDD_C104
	,0 as  "CredIGCAcumH31122016RXSDD_C105"  --CredIGCAcumH31122016RXSDD_C105
	,0 as  "CredIGCAcumH31122016RXCDD_C106"  --CredIGCAcumH31122016RXCDD_C106
	,0 as  "DisRemRentasTribCProvRap_C96"  --DisRemRentasTribCProvRap_C96
	,0 as  "DisRemRentasTribCORP_C97"  --DisRemRentasTribCORP_C97
	,0 as  "DisRemRentasTribCExDD_C98"  --DisRemRentasTribCExDD_C98
	,0 as  "DisRemRentasTribCUtAFFut_C99"  --DisRemRentasTribCUtAFFut_C99			
	,CASE LG."OrigenDet" WHEN 'OPT_FM' THEN fondo."CodigoSVS" ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END  AS "DetBenNetoPerRutFdo_C107" --DetBenNetoPerRutFdo_C107
	,0 as  "DetBenNetoPerPOS_C113"  --DetBenNetoPerPOS_C113
	,0 as  "DetBenNetoPerNEG_C114"  --DetBenNetoPerNEG_C114
	,0 as "DetBenNetoUtilPer_C133"  --DetBenNetoUtilPer_C133
	,0 as "DetBenNetoIntPer_C134"  --DetBenNetoIntPer_C134
	,0 as "DetBenNetoDivPer_C135"  --DetBenNetoDivPer_C135
	,0 as "DetBenNetoGanCapPer_C136"  --DetBenNetoGanCapPer_C136
	,0 as "DetBenNetoPerGastDev_C137"  --DetBenNetoPerGastDev_C137
	,TO_NVARCHAR(LG."NumSeriesCuotasParticipe") as "NumSerieCuotasParticipe_C138"
	,LG."AcogidoISFUT" AS "AcogidoISFUT"
	,LEFT(IFNULL((CASE LG."OrigenDet" WHEN 'OPT_FM' THEN fondo."CodigoSVS" ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END),''),21) AS "RunNemotecnico", -- C139
	'2' --Tipo Contribuyente C138
FROM 
(

		--	CFI TIPO 1
					SELECT  ec."IdEmpresa",
					---ec."IdInstrumento",
					ec."IdCliente",
					ec."IdPortafolio",
					--COALESCE(soc."RUT_ADMIN",'') AS "RutSociedadAdm", --RutSociedad
					--COALESCE(soc."RUT", '') AS "RutFondo" , --RutFondo           
					ec."Nemo" AS "Fondo",--Fondo
					'I' AS "TipoFondo", -- TipoFondo
					0 AS "TaxId",   --TaxId
					--COALESCE (pais."CodigoPais", 'CL') AS "CodPais", --CodPais
					1 AS "TipoOperacion",    --TipoOperacion
					ec."FechaPago" AS "FechaInversion", -- Fecha de Pago del Evento
					CASE 	WHEN ec."IdMoneda" = 'USD' THEN 2
							WHEN ec."IdMoneda" = 'EUR' THEN 3
							WHEN ec."IdMoneda" = 'UF'  THEN 4
							WHEN ec."IdMoneda" = 'IVP' THEN 5
							WHEN ec."IdMoneda" = 'CLP' THEN 1
							ELSE 6 -- Otros
					END AS "MonedaReajuste" , --MonedaReajuste
					CASE WHEN ec."IdMoneda"='USD' THEN (ec."Total" * COALESCE(DOLAR."Valor",1))
					ELSE ec."Total"
					END AS "Monto", ----Monto //NADA
					0 AS "MontoActualizado",--MontoActualizado
					900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
					COALESCE(HIS."INGCantidad",0) as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
					3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
					3 AS "CuotasCumplenReq",--CuotasCumplenReq
					COALESCE(HIS."INGPrecioMercadoActual", HISPRECIO."INGPrecioMercadoActual",0) as "ValorCuotaPesos",--ValorCuotaPesos
					0 as "PorParticipacionFondo",--PorParticipacionFondo
					0 as "ValNominalCuotasResc",--ValNominalCuotasResc
					0 as "CantCuotasResc",--CantCuotasResc
					0 as "DifRescateCuotasAct",--DifRescateCuotasAct
					0 as "DifRescateCuotasActFi",--DifRescateCuotasActFi
					0 as "ValNominalInversion",--ValNominalInversion
					0 as "CantCuotasCedidas",--CantCuotasCedidas
					0 as "DifValCuotas",--DifValCuotas
					0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
					0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
					0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
					0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
					0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
					0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
					0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
					0 as "DevolucionCap",--DevolucionCap			decimal
					0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
					0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
					0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
					0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
					0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
					0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
					0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
					0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
					0 AS "Orden",
					'CARGAINI' AS "Origen" , 
					'OPT_CFI' AS "OrigenDet",
					'S' AS "IncluirEnDJ",--IncluirEnDJ			char
					'' AS "VBUsuario",--VBUsuario				varchar
					'' AS "Observacion",--Observacion			varchar
					'' AS "UsrUltMod", --UsrUltMod		varchar
					CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
					ec."Nemo" AS "CodigoFondo",--CodigoFondo,	varchar
					ec."Secuencia" AS "SecEvento",  --- SecEvento
					'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
					COALESCE(HIS."INGCantidad",0) as "cantidad",	          --- cantidad
					CASE WHEN ec."IdTipoInstrumento" = 'CFI' THEN 'CF' ELSE 'EN' END AS "Mercado",  --- Mercado
					'' AS "CodMovto",--- CodMovto
					'' AS "FecMovto", --- FecMovto	
					ec."IdEventoCapitalCliente" AS "SecMovto" , --- SecMovto
		            1 AS "TributaSa"--,         --Tributa SA   
					,ec."ISFUT" AS "AcogidoISFUT"
					,ec."IdInstrumento"
					,ec."Nemo" AS "RunNemotecnico"
				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente"  ec  
				--INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = ec."IdCliente"
				--LEFT  JOIN "BSCL_VIRTUAL"."TBASSOC" soc ON trim(soc."NEMO") = trim(ec."Nemo")
				--LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais" pais ON pais."IdPais" = cli."IdPaisResidencia"
				LEFT  JOIN 
					  (SELECT
						"IdCliente",
						"IdPortafolio",
						"Nemotecnico",
						sum("INGCantidad") AS "INGCantidad",
						CASE WHEN "IdMonedaInstrumento" = 'USD' THEN max(round(("INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max("INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
						,"FechaRespaldo"
						FROM "BSCL.Custodia.BD::HIS.Cartera"
						LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR 	 ON "FechaRespaldo" = PAR."Fecha"      AND PAR."IdParametroFinanciero" = 'DO'							
						WHERE 	--"FechaRespaldo" = :FechaRespaldo
						 		"IdClasificacionTipoInstrumento" IN ('RV')
						AND 	"IdTipoInstrumento" IN ('CFI','ETN')
						AND 	"IdEstado" = 'VIG'
						AND 	"INGMontoATasaMercadoActual" > 0
						AND 	"IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
						GROUP BY "IdCliente","IdPortafolio","Nemotecnico","IdMonedaInstrumento","FechaRespaldo"
					 ) HIS ON HIS."IdCliente" = ec."IdCliente" AND HIS."IdPortafolio" = ec."IdPortafolio" AND HIS."Nemotecnico" = ec."Nemo"  AND HIS."FechaRespaldo"=ec."FechaPago"
				LEFT  JOIN 
					  (SELECT
						"Nemotecnico",
						CASE WHEN "IdMonedaInstrumento" = 'USD' THEN max(round(("INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max("INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
						,"FechaRespaldo"
						FROM "BSCL.Custodia.BD::HIS.Cartera"
						LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR 	 ON "FechaRespaldo" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'							
						WHERE 	--"FechaRespaldo" = :FechaRespaldo
						 	"IdClasificacionTipoInstrumento" IN ('RV')
						AND 	"IdTipoInstrumento" IN ('CFI','ETN')
						AND 	"IdEstado" = 'VIG'
						AND 	"INGMontoATasaMercadoActual" > 0
						AND 	"IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
						GROUP BY "Nemotecnico","IdMonedaInstrumento","FechaRespaldo"
					 ) HISPRECIO ON HISPRECIO."Nemotecnico" = ec."Nemo" AND HISPRECIO."FechaRespaldo"=ec."FechaPago"
				LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" AS DOLAR ON DOLAR."Fecha" = ec."FechaPago" AND DOLAR."IdParametroFinanciero" = 'DO'	 
				WHERE 	--ec."FechaPago" BETWEEN :FechaInicioCFI AND :FechaTerminoCFI
						ec."IdClasificacionTipoInstrumento" = 'RV' 
				AND 	ec."IdTipoInstrumento" IN ('CFI','ETN')
				AND 	ec."IdTipoEventoCapital" in('DV','RC')
				AND 	ec."Total" > 0
				AND 	(ec."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND ec."Portafolio" NOT IN ('REGAPV'))
				AND 	ec."TipoSocioNegocio" = 'CLI'
				AND 	ec."IdEstado" = 'CER'
				
		UNION ALL		
		--	CFI TIPO 3
		
			SELECT  cartera."IdEmpresa",
				cartera."IdCliente",
				cartera."IdPortafolio",           
				cartera."Nemotecnico" AS "Fondo",--Fondo
				'I' AS "TipoFondo", -- TipoFondo
				0 AS "TaxId",   --TaxId
				3 AS "TipoOperacion",    --TipoOperacion
				cartera."EGRFechaOperacion" AS "FechaInversion", -- Fecha de Pago del Evento
				CASE 	WHEN cartera."IdMonedaInstrumento" = 'USD' THEN 2
						WHEN cartera."IdMonedaInstrumento" = 'CLP' THEN 1
						WHEN cartera."IdMonedaInstrumento" = 'EUR' THEN 3
						WHEN cartera."IdMonedaInstrumento" = 'UF'  THEN 4
						WHEN cartera."IdMonedaInstrumento" = 'IVP' THEN 5
						ELSE 6 -- otro
				END AS "MonedaReajuste" , --MonedaReajuste
				cartera."EGRMonto" AS "Monto", ----Monto
				0 AS "MontoActualizado",--MontoActualizado
				900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
				0 as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
				3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
				3 AS "CuotasCumplenReq",--CuotasCumplenReq
				0 as "ValorCuotaPesos",--ValorCuotaPesos
				0 as "PorParticipacionFondo",--PorParticipacionFondo
				0 as "ValNominalCuotasResc",--ValNominalCuotasResc
				0 as "CantCuotasResc",--CantCuotasResc
				0 as "DifRescateCuotasAct",--DifRescateCuotasAct
				0 as "DifRescateCuotasActFi",--DifRescateCuotasActFi
				cartera."MontoInversionActualizado" as "ValNominalInversion",--INFORMAR MONTO DEL RESCATE
				cartera."EGRCantidad" as "CantCuotasCedidas",--INFORMAR CUOTAS DEL RESCATE
				cartera."MayorMenorValorActualizado" as "DifValCuotas",--DifValCuotas
				0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
				0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
				0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
				0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
				0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
				0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
				0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
				0 as "DevolucionCap",--DevolucionCap			decimal
				0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
				0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
				0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
				0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
				0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
				0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
				0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
				0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
				0 AS "Orden",
				'CARGAINI' AS "Origen" , 
				'OPT_CFI' AS "OrigenDet",
				'S' AS "IncluirEnDJ",--IncluirEnDJ			char
				'' AS "VBUsuario",--VBUsuario				varchar
				'' AS "Observacion",--Observacion			varchar
				'' AS "UsrUltMod", --UsrUltMod		varchar
				CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
				cartera."Nemotecnico" AS "CodigoFondo",--CodigoFondo,	varchar
				0 AS "SecEvento",  --- SecEvento
				'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
				COALESCE(cartera."INGCantidad",0) as "cantidad",	          --- cantidad
				CASE WHEN cartera."IdTipoInstrumento" = 'CFI' THEN 'CF' ELSE 'EN' END AS "Mercado",  --- Mercado
				'' AS "CodMovto",--- CodMovto
				'' AS "FecMovto", --- FecMovto	
				0 AS "SecMovto" , --- SecMovto
				1 AS "TributaSa"--,         --Tributa SA      
				,'N' AS "AcogidoISFUT"
				,cartera."IdInstrumento"
				,cartera."Nemotecnico" AS "RunNemotecnico"--C139
			FROM (
				SELECT 
				"IdEmpresa",
				"IdCliente",
				"IdPortafolio",
				"IdTipoInstrumento",
				"DocumentoIdentidad", 
				"EGRFechaOperacion",
				"INGCantidad",
				"INGFechaOperacion", 
				sum("EGRCantidad") AS "EGRCantidad",
				sum("MontoInversionActualizado") AS "MontoInversionActualizado",
				"IdMonedaInstrumento",
				"Nemotecnico",
				"EGRPrecio",
				sum("EGRMonto") AS "EGRMonto",
				sum("MayorMenorValorActualizado") AS "MayorMenorValorActualizado",
				"EGRIdOperacion",
				"EGRIdTipoOperacion",
				"IPCDic",
				"IPCInv",  
				"Factor",
				"IdInstrumento"
				FROM 
				(
					SELECT  car."IdEmpresa",car."IdCliente",car."INGCantidad",car."IdPortafolio",car."IdMonedaInstrumento",car."INGFechaOperacion" ,car."IdTipoInstrumento",por."DocumentoIdentidad","FechaOperacion","Nemotecnico", "Cantidad","PrecioCompra","MontoCompra", "EGRFechaOperacion","EGRCantidad","EGRPrecio","EGRMonto",
					COALESCE(round(round("EGRMonto" - (("EGRCantidad"/ "Cantidad")* "MontoCompra"),0) * (IPCACT."Valor"/IPCINV."Valor"),0),0) as "MayorMenorValorActualizado", (("EGRCantidad"/ "Cantidad")* "MontoCompra") * (IPCACT."Valor"/IPCINV."Valor") AS "MontoInversionActualizado" 
					,"EGRIdOperacion","EGRIdTipoOperacion",IPCACT."Valor" AS "IPCDic", IPCINV."Valor" AS "IPCInv", round(IPCACT."Valor"/IPCINV."Valor",3) AS "Factor", car."IdInstrumento"
					FROM "BSCL.Custodia.BD::CUS.Cartera" car
					LEFT JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" por ON por."IdPortafolio" = car."IdPortafolio"
					LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINV  ON (substring(TO_DATE (car."EGRFechaOperacion", 'YYYY-MM-DD'),0,7) = IPCINV."AgnoMes")  
																							AND IPCINV."IdParametroFinanciero" = 'IPC'
					LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACT  ON (year(car."EGRFechaOperacion") || '-12' = IPCACT."AgnoMes")  		
																							AND IPCACT."IdParametroFinanciero" = 'IPC'

					--WHERE "EGRFechaOperacion" BETWEEN '2021-01-01' AND '2021-12-31'
					WHERE "EGRIdTipoOperacion" = 'RETTIT'
					AND "EGRIdConcepto" IN ('RTDICP','REDICA')
					AND (por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
					AND "EGRCantidad" > 0
					ORDER BY "EGRIdOperacion" ) todo  
				GROUP BY "IdEmpresa","IdCliente","DocumentoIdentidad","IdMonedaInstrumento","INGFechaOperacion","INGCantidad","IdTipoInstrumento","IdPortafolio", "Nemotecnico","EGRFechaOperacion","EGRIdOperacion","EGRIdTipoOperacion" ,"EGRPrecio","IPCDic","IPCInv","Factor","IdInstrumento"
				ORDER BY "EGRIdOperacion"	
			) cartera
				
		UNION ALL 
				--	FFMM TIPO 1
				SELECT  sol."IdEmpresa",
					sol."IdCliente",
					sol."IdPortafolio",
    				sol."IdSerieFondo" AS "Fondo",--Fondo
					'F' AS "TipoFondo", -- TipoFondo
					0 AS "TaxId",   --TaxId
					1 AS "TipoOperacion",    --TipoOperacion
					sol."FechaValorCuota" AS "FechaInversion", -- Fecha de Pago del Evento
					CASE 	WHEN sol."IdMoneda" = 'USD' THEN 2
							WHEN sol."IdMoneda" = 'EUR' THEN 3
							WHEN sol."IdMoneda" = 'UF'  THEN 4
							WHEN sol."IdMoneda" = 'IVP' THEN 5
							WHEN sol."IdMoneda" = 'CLP' THEN 1
							ELSE 6 -- Otros
					END AS "MonedaReajuste" , --MonedaReajuste
					sol."MontoMonedaSistema" AS "Monto", ----Monto
					0 AS "MontoActualizado",--MontoActualizado
					900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
					COALESCE(HIS."INGCantidad",0) as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
					3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
					3 AS "CuotasCumplenReq",--CuotasCumplenReq
					COALESCE(HIS."INGPrecioMercadoActual", HISPRECIO."INGPrecioMercadoActual",0) as "ValorCuotaPesos",--ValorCuotaPesos
					0 as "PorParticipacionFondo",--PorParticipacionFondo
					0 as "ValNominalCuotasResc",--ValNominalCuotasResc
					0 as "CantCuotasResc",--CantCuotasResc
					0 as "DifRescateCuotasAct",--DifRescateCuotasAct
					0 as "DifRescateCuotasActFi",--DifRescateCuotasActFi
					0 as "ValNominalInversion",--ValNominalInversion
					0 as "CantCuotasCedidas",--CantCuotasCedidas
					0 as "DifValCuotas",--DifValCuotas
					0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
					0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
					0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
					0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
					0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
					0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
					0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
					0 as "DevolucionCap",--DevolucionCap			decimal
					0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
					0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
					0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
					0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
					0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
					0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
					0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
					0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
					0 AS "Orden",
					'CARGAINI' AS "Origen" , 
					'OPT_FM' AS "OrigenDet",
					'S' AS "IncluirEnDJ",--IncluirEnDJ			char
					'' AS "VBUsuario",--VBUsuario				varchar
					'' AS "Observacion",--Observacion			varchar
					'' AS "UsrUltMod", --UsrUltMod		varchar
					CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
					sol."IdSerieFondo" AS "CodigoFondo",--CodigoFondo,	varchar
					COALESCE(EC."Secuencia", 0) AS "SecEvento",  --- SecEvento /Consultar: Modificar IdDividendo por ec.Secuencia o crear nueva propiedad
					'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
					COALESCE(HIS."INGCantidad",0) as "cantidad",	  --- cantidad
					'FM' AS "Mercado",  --- Mercado
					'CIFM' AS "CodMovto",--- CodMovto
					sol."FechaValorCuota" AS "FecMovto", --- FecMovto	
					sol."IdSolicitudAporteRescate" AS "SecMovto" , --- SecMovto
		            1 AS "TributaSa"--         --Tributa SA            
					,'N' AS "AcogidoISFUT"
					,ins."IdInstrumento"
					,sol."IdSerieFondo" AS "RunNemotecnico"
				FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON por."IdPortafolio" = sol."IdPortafolio"
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" serie ON serie."IdSerieFondo" = sol."IdSerieFondo" 
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" ins ON ins."IdInstrumento" = serie."IdInstrumento"
				LEFT JOIN
				   (SELECT
				   		"Secuencia",
				   		"IdEventoCapital"
				   		FROM "BSCL.Custodia.BD::CUS.EventoCapital"
					) EC ON EC."IdEventoCapital" = sol."IdDividendo"
				LEFT  JOIN 
				  (SELECT
						"IdCliente",
						"IdPortafolio",
						"Nemotecnico",
						sum("INGCantidad") AS "INGCantidad",
						CASE WHEN "IdMonedaInstrumento" = 'USD' THEN max(round(("INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max("INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
						,"FechaRespaldo"
						FROM "BSCL.Custodia.BD::HIS.Cartera"
						LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR 	 ON "FechaRespaldo" = PAR."Fecha"      AND PAR."IdParametroFinanciero" = 'DO'							
						WHERE 	--"FechaRespaldo" = :FechaRespaldo
						 		"IdClasificacionTipoInstrumento" IN ('RV')
						AND 	"IdTipoInstrumento" IN ('CFI','ETN')
						AND 	"IdEstado" = 'VIG'
						AND 	"INGMontoATasaMercadoActual" > 0
						AND 	"IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
						GROUP BY "IdCliente","IdPortafolio","Nemotecnico","IdMonedaInstrumento","FechaRespaldo"
					 ) HIS ON HIS."IdCliente" = sol."IdCliente" AND HIS."IdPortafolio" = sol."IdPortafolio" AND HIS."Nemotecnico" = sol."IdSerieFondo"   AND HIS."FechaRespaldo"=sol."FechaValorCuota" 
			LEFT  JOIN 
					 (SELECT
						"Nemotecnico",
						CASE WHEN "IdMonedaInstrumento" = 'USD' THEN max(round(("INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max("INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
						,"FechaRespaldo"
						FROM "BSCL.Custodia.BD::HIS.Cartera"
						LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR 	 ON "FechaRespaldo" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'							
						WHERE 	--"FechaRespaldo" = :FechaRespaldo
						 	"IdClasificacionTipoInstrumento" IN ('RV')
						AND 	"IdTipoInstrumento" IN ('CFI','ETN')
						AND 	"IdEstado" = 'VIG'
						AND 	"INGMontoATasaMercadoActual" > 0
						AND 	"IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
						GROUP BY "Nemotecnico","IdMonedaInstrumento","FechaRespaldo"
					 ) HISPRECIO ON HISPRECIO."Nemotecnico" =sol."IdSerieFondo"  AND HISPRECIO."FechaRespaldo"=sol."FechaValorCuota"					 
				WHERE 	
					   	sol."IdTipoMovimientoFondoMutuo" IN ('APODIV','APODIVCFI')
				AND 	sol."MontoMonedaSistema" > 0
				AND 	(sol."IdRegimenTributario" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
				AND 	sol."Estado" = 'CRT'	
				
			
			UNION ALL 
			
			--	FFMM TIPO 4
			SELECT 				
				"IdEmpresa",
				"IdCliente",
				"IdPortafolio",
				"IdSerieFondo" AS "Fondo",--Fondo
				'F' AS "TipoFondo", -- TipoFondo
				0 AS "TaxId",   --TaxId
				4 AS "TipoOperacion",    --TipoOperacion
				"FechaValorCuota" AS "FechaInversion", -- Fecha de Pago del Evento
				CASE 	WHEN "IdMoneda" = 'USD' THEN 2
						WHEN "IdMoneda" = 'EUR' THEN 3
						WHEN "IdMoneda" = 'UF'  THEN 4
						WHEN "IdMoneda" = 'IVP' THEN 5
						WHEN "IdMoneda" = 'CLP' THEN 1
						ELSE 6 -- Otros
				END AS "MonedaReajuste" , --MonedaReajuste
				"MontoInversion" AS "Monto", ----Monto
				"MontoInversionActualizado" AS "MontoActualizado",--MontoActualizado
				900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
				0 as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
				3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
				3 AS "CuotasCumplenReq",--CuotasCumplenReq
				0 as "ValorCuotaPesos",--ValorCuotaPesos
				0 as "PorParticipacionFondo",--PorParticipacionFondo
				"MontoMonedaSistemaActualizado" as "ValNominalCuotasResc",--ValNominalCuotasResc
				"NumeroCuotas" as "CantCuotasResc",--CantCuotasResc
				"MayorMenorValorActualizado" as "DifRescateCuotasAct",--ESTE CAMPO ES SOLO PARA CFI
				"MayorMenorValorActualizado" as "DifRescateCuotasActFi",--DifRescateCuotasActFi
				0 as "ValNominalInversion",--INFORMAR MONTO DEL RESCATE
				0 as "CantCuotasCedidas",--INFORMAR CUOTAS DEL RESCATE
				0 as "DifValCuotas",--DifValCuotas
				0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
				0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
				0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
				0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
				0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
				0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
				0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
				0 as "DevolucionCap",--DevolucionCap			decimal
				0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
				0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
				0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
				0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
				0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
				0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
				0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
				0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
				0 AS "Orden",
				'CARGAINI' AS "Origen" , 
				'OPT_FM' AS "OrigenDet",
				'S' AS "IncluirEnDJ",--IncluirEnDJ			char
				'' AS "VBUsuario",--VBUsuario				varchar
				'' AS "Observacion",--Observacion			varchar
				'' AS "UsrUltMod", --UsrUltMod		varchar
				CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
				"IdSerieFondo" AS "CodigoFondo",--CodigoFondo,	varchar
				0 AS "SecEvento",  --- SecEvento
				'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
				COALESCE("INGCantidad",0) as "cantidad",	          --- cantidad
				'FM' AS "Mercado",  --- Mercado
				'CIFM' AS "CodMovto",--- CodMovto
				"FechaValorCuota" AS "FecMovto", --- FecMovto	
				"IdSolicitudAporteRescate" AS "SecMovto" , --- SecMovto
				1 AS "TributaSa"--,         --Tributa SA
				,'N' AS "AcogidoISFUT"	
				,"IdInstrumento"
				,"IdSerieFondo" AS "RunNemotecnico"
			FROM (	
					SELECT
						"IdEmpresa",
						"EGRIdOperacion",
						"IdCliente",
						"IdPortafolio",
						--sum("MayorMenorValorActualizado") AS "MayorMenorValorActualizado",
						COALESCE(round((sum("MontoVenta")-sum(("NumeroCuotas"/ "Cantidad")* "MontoCompra")) * UF31DIC."Valor",0),0) AS "MayorMenorValorActualizado",
						round(sum("MontoInversionActualizado") * MONREAJUSTE."Valor",0) AS "MontoInversionActualizado", -- PESOS
						sum("MontoInversion") as "MontoInversion",
						"IdSerieFondo",
						"FechaValorCuota",
						"IdMoneda",
						"MontoMonedaSistema",
						"MontoMonedaSistemaActualizado",
						"Monto",
						sum("NumeroCuotas") AS "NumeroCuotas",
						"IdDividendo",
						sum("INGCantidad") AS "INGCantidad",
						"IdSolicitudAporteRescate",
						"IdInstrumento"
						FROM (
								SELECT
									sol."IdEmpresa",		
									car."EGRIdOperacion",
									sol."IdCliente",
									sol."IdPortafolio",
									--COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) ),0),0) AS "MayorMenorValorActualizado",
									round("MontoCompra" / uf_movCompra."Valor",3) AS "MontoCompra" ,
									COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") )) ,3),0) as "MontoVenta",
									sol."IdSerieFondo",
									sol."FechaValorCuota",
									sol."IdMoneda",
									sol."MontoMonedaSistema",
									sol."MontoMonedaSistema"  AS "MontoMonedaSistemaActualizado",
									sol."Monto",
									(("EGRCantidad"/ "Cantidad")* "MontoCompra")  / mon_movCompra."Valor" AS "MontoInversionActualizado", 
									(("EGRCantidad"/ "Cantidad")* "MontoCompra") AS "MontoInversion",
									car."EGRCantidad" AS "NumeroCuotas",
									sol."IdDividendo",
									car."INGCantidad",
									car."Cantidad",
									sol."IdSolicitudAporteRescate",
									car."IdInstrumento"
								FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
								INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car
								ON (sol."IdSolicitudAporteRescate" = car."EGRIdOperacion"
									AND car."IdClasificacionTipoInstrumento" = 'FM'
									AND car."IdEstado" IN ('LIQ','MOD','RPA')
									)
								LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR ON (subsTRIng(ADD_MONTHS (TO_DATE (COALESCE(sol."FechaValorCuota",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")
									AND IPCINVR."IdParametroFinanciero" = 'IPC'
								LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR ON (SUBSTRING(COALESCE(sol."FechaValorCuota",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")
									AND IPCACTR."IdParametroFinanciero" = 'IPC'
								LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= sol."FechaValorCuota" ) AND uf_mov."IdParametroFinanciero" = 'UF'
								LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_movCompra ON (uf_movCompra."Fecha"= car."FechaOperacion" ) AND uf_movCompra."IdParametroFinanciero" = 'UF' --se mantiene porque existen otros campos que usan el uf_movCompra
								LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" mon_movCompra ON mon_movCompra."Fecha" = ( CASE car."IdMonedaInstrumento" WHEN 'CLP' THEN  (SELECT MIN("Fecha") 
																																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																																													WHERE "IdParametroFinanciero" = 'IPC'
																																													AND "AgnoMes" = TO_VARCHAR("FechaOperacion", 'YYYY-MM'))
																																							WHEN 'IVP' THEN
																																											(SELECT MIN("Fecha") 
																																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																																													WHERE "IdParametroFinanciero" = 'IPC'
																																													AND "AgnoMes" = TO_VARCHAR("FechaOperacion", 'YYYY-MM'))
																																							ELSE  car."FechaOperacion" 
																																								END) 
																												AND mon_movCompra."IdParametroFinanciero" = (CASE sol."IdMoneda" WHEN 'USD'  THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE sol."IdMoneda" END) --por idmoneda
					

								WHERE 	
										sol."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV','RESCDF')
								AND 	"EGRCantidad" > 0		      
								AND 	sol."MontoMonedaSistema" > 0
								AND 	"IdRegimenTributario" IN ('18T', 'ART107')
								AND 	sol."Estado" = 'CRT'
						)
						LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" UF31DIC ON (UF31DIC."Fecha"= YEAR (TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) ||'-12-31' ) AND UF31DIC."IdParametroFinanciero" = 'UF' --se mantiene porque existen otros campos que usan el uf31dic
						LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTE ON MONREAJUSTE."Fecha" = ( CASE "IdMoneda"
																											WHEN  'USD' THEN
																													(SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'DO'
																													AND YEAR("Fecha") = YEAR(TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) + 1)
																											WHEN  'EUR' THEN
																													(SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'EUR'
																													AND YEAR("Fecha") = YEAR(TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) + 1)
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    (SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'IPC'
																													AND "AgnoMes" = (YEAR(TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) ||'-12'))
																											ELSE
																												    (SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'IPC'
																													AND "AgnoMes" = (YEAR(TO_DATE("FechaValorCuota", 'YYYY-MM-DD')) ||'-12'))
																										END) AND MONREAJUSTE."IdParametroFinanciero" = (CASE "IdMoneda" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMoneda" END) -- por IdMoneda
						GROUP BY "IdEmpresa",UF31DIC."Valor",MONREAJUSTE."Valor","EGRIdOperacion","IdCliente","IdPortafolio","IdSerieFondo","FechaValorCuota","IdMoneda","MontoMonedaSistema","Monto","IdDividendo","IdSolicitudAporteRescate","MontoMonedaSistemaActualizado", "IdInstrumento"
			)
UNION ALL 

	--CFI TIPO 4
	SELECT  cartera."IdEmpresa",
				cartera."IdCliente",
				cartera."IdPortafolio",           
				cartera."Nemotecnico" AS "Fondo",--Fondo
				'I' AS "TipoFondo", -- TipoFondo
				0 AS "TaxId",   --TaxId
				4 AS "TipoOperacion",    --TipoOperacion
				cartera."EGRFechaOperacion" AS "FechaInversion", -- Fecha de Pago del Evento
				CASE 	WHEN cartera."IdMonedaInstrumento" = 'USD' THEN 2
						WHEN cartera."IdMonedaInstrumento" = 'CLP' THEN 1
						WHEN cartera."IdMonedaInstrumento" = 'EUR' THEN 3
						WHEN cartera."IdMonedaInstrumento" = 'UF'  THEN 4
						WHEN cartera."IdMonedaInstrumento" = 'IVP' THEN 5
						ELSE 6 -- otro
				END AS "MonedaReajuste" , --MonedaReajuste
				cartera."MontoInversion" AS "Monto", ----Monto
				"MontoInversionActualizado" AS "MontoActualizado",--MontoActualizado
				900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
				0 as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
				3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
				3 AS "CuotasCumplenReq",--CuotasCumplenReq
				0 as "ValorCuotaPesos",--ValorCuotaPesos
				0 as "PorParticipacionFondo",--PorParticipacionFondo
				"EGRMontoActualizado" as "ValNominalCuotasResc",--ValNominalCuotasResc
				cartera."EGRCantidad" as "CantCuotasResc",--CantCuotasResc
				cartera."MayorMenorValorActualizado" as "DifRescateCuotasAct",--DifRescateCuotasAct
				cartera."MayorMenorValorActualizado" as "DifRescateCuotasActFi",-- ESTE CAMPO ES SOLO PARA FFMM
				0 as "ValNominalInversion",--INFORMAR MONTO DEL RESCATE
				0 as "CantCuotasCedidas",--INFORMAR CUOTAS DEL RESCATE
				0 as "DifValCuotas",--DifValCuotas
				0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
				0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
				0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
				0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
				0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
				0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
				0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
				0 as "DevolucionCap",--DevolucionCap			decimal
				0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
				0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
				0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
				0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
				0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
				0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
				0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
				0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
				0 AS "Orden",
				'CARGAINI' AS "Origen" , 
				'OPT_CFI' AS "OrigenDet",
				'S' AS "IncluirEnDJ",--IncluirEnDJ			char
				'' AS "VBUsuario",--VBUsuario				varchar
				'' AS "Observacion",--Observacion			varchar
				'' AS "UsrUltMod", --UsrUltMod		varchar
				CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
				cartera."Nemotecnico" AS "CodigoFondo",--CodigoFondo,	varchar
				0 AS "SecEvento",  --- SecEvento
				'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
				COALESCE(cartera."INGCantidad",0) as "cantidad",	          --- cantidad
				CASE WHEN cartera."IdTipoInstrumento" = 'CFI' THEN 'CF' ELSE 'EN' END AS "Mercado",  --- Mercado
				'' AS "CodMovto",--- CodMovto
				'' AS "FecMovto", --- FecMovto	
				"EGRFolioComprobante" AS "SecMovto" , --- SecMovto
				1 AS "TributaSa"--,         --Tributa SA         
				,'N' AS "AcogidoISFUT"  
				,cartera."IdInstrumento"
				,cartera."Nemotecnico" AS "RunNemotecnico" --C139
			FROM (
				SELECT  
				"IdEmpresa",
				"IdCliente",
				"IdPortafolio",
				"IdTipoInstrumento",
				"DocumentoIdentidad", 
				"EGRFechaOperacion",
				"INGCantidad",
				"INGFechaOperacion",
				sum("MontoInversion") as "MontoInversion", 
				sum("EGRCantidad") AS "EGRCantidad",
				round(sum("MontoInversionActualizado") * MONREAJUSTE."Valor",0) AS "MontoInversionActualizado",
				"IdMonedaInstrumento",
				"Nemotecnico",
				"EGRPrecio",
				sum("EGRMonto") AS "EGRMonto",
				sum("EGRMontoActualizado") AS "EGRMontoActualizado",
				COALESCE(round((sum("MontoVenta")-sum(("EGRCantidad"/ "Cantidad")* "MontoCompra")) * UF31DIC."Valor",0),0) AS "MayorMenorValorActualizado",
				 UF31DIC."Valor",
				"EGRFolioComprobante",
				"EGRIdTipoOperacion",
				"IPCDic",
				"IPCInv",  
				"Factor",
				"IdInstrumento"

				FROM 
					(
					SELECT por."DocumentoIdentidad",car."EGRIdOperacion",car."IdMonedaInstrumento",car."INGFechaOperacion",car."INGCantidad",car."IdTipoInstrumento",car."IdPortafolio",car."IdCliente",car."IdEmpresa","FechaOperacion","Nemotecnico", "Cantidad","PrecioCompra", "EGRFechaOperacion","EGRCantidad","EGRPrecio","EGRMonto",
					
					
					round("MontoCompra" / uf_movCompra."Valor",3) AS "MontoCompra" ,
					COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") )) ,3),0) as "MontoVenta",
					--COALESCE(round(round("EGRMonto" - (("EGRCantidad"/ "Cantidad")* "MontoCompra"),0) * (IPCACT."Valor"/IPCINV."Valor"),0),0) as "MayorMenorValorActualizado"
					
					("EGRCantidad"/ "Cantidad")* "MontoCompra" as "MontoInversion", 
					(("EGRCantidad"/ "Cantidad")* "MontoCompra") / mon_movCompra."Valor" AS "MontoInversionActualizado", --mismo cambio que FFMM 
					
					"EGRMonto" AS "EGRMontoActualizado"
					
					,"EGRFolioComprobante","EGRIdTipoOperacion",IPCACTR."Valor" AS "IPCDic", IPCINVR."Valor" AS "IPCInv", round(IPCACTR."Valor"/IPCINVR."Valor",3) AS "Factor"
					,car."IdInstrumento"
					FROM "BSCL.Custodia.BD::CUS.Cartera" car 
					INNER JOIN "BSCL.Portafolio.BD.Vistas::UI.ConsultarClientePortafolio" por ON por."IdPortafolio" = car."IdPortafolio"
					
					LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR ON (subsTRIng(ADD_MONTHS (TO_DATE (COALESCE(car."EGRFechaOperacion",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")
						AND IPCINVR."IdParametroFinanciero" = 'IPC'
					
					LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR ON (SUBSTRING(COALESCE(car."EGRFechaOperacion",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")
						AND IPCACTR."IdParametroFinanciero" = 'IPC'
					
					LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= car."EGRFechaOperacion" ) AND uf_mov."IdParametroFinanciero" = 'UF'
					LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_movCompra ON (uf_movCompra."Fecha"= car."FechaOperacion" ) AND uf_movCompra."IdParametroFinanciero" = 'UF'
					LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" mon_movCompra ON mon_movCompra."Fecha" = ( CASE car."IdMonedaInstrumento" WHEN 'CLP' THEN  (SELECT MIN("Fecha") 
																																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																																													WHERE "IdParametroFinanciero" = 'IPC'
																																													AND "AgnoMes" = TO_VARCHAR("EGRFechaOperacion", 'YYYY-MM'))
																																							WHEN 'IVP' THEN (SELECT MIN("Fecha") 
																																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																																													WHERE "IdParametroFinanciero" = 'IPC'
																																													AND "AgnoMes" = TO_VARCHAR("EGRFechaOperacion", 'YYYY-MM'))
																																							ELSE  car."EGRFechaOperacion" 
																															END) 
																												AND mon_movCompra."IdParametroFinanciero" = (CASE car."IdMonedaInstrumento" WHEN 'USD'  THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE car."IdMonedaInstrumento" END) --por idmoneda

					WHERE "EGRCantidad" > 0
					AND  "EGRIdTipoOperacion" IN ('VENCFI','VENETN')
					AND (por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
					ORDER BY "EGRFolioComprobante") total 
				LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" UF31DIC ON (UF31DIC."Fecha"= YEAR (TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) ||'-12-31' ) AND UF31DIC."IdParametroFinanciero" = 'UF'
				LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTE ON MONREAJUSTE."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													(SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'DO'
																													AND YEAR("Fecha") = YEAR(TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) + 1)
																											WHEN  'EUR' THEN
																													(SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'EUR'
																													AND YEAR("Fecha") = YEAR(TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) + 1)
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    (SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'IPC'
																													AND "AgnoMes" = (YEAR(TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) ||'-12'))
																											ELSE
																												    (SELECT MIN("Fecha") 
																													FROM "BSCL.Plataforma.BD::MER.ParametroFinanciero" 
																													WHERE "IdParametroFinanciero" = 'IPC'
																													AND "AgnoMes" = (YEAR(TO_DATE("EGRFechaOperacion", 'YYYY-MM-DD')) ||'-12'))
																										END) AND MONREAJUSTE."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) -- por IdMoneda
				GROUP BY "DocumentoIdentidad", UF31DIC."Valor",MONREAJUSTE."Valor","IdEmpresa","EGRIdOperacion","IdMonedaInstrumento","INGFechaOperacion","IdCliente","INGCantidad","IdTipoInstrumento","IdPortafolio","Nemotecnico","EGRFechaOperacion","EGRFolioComprobante","EGRIdTipoOperacion" ,"EGRPrecio","IPCDic","IPCInv","Factor","IdInstrumento"
				ORDER BY "EGRFolioComprobante"	
			) cartera

			UNION ALL
		
			-- CFI TIPO 4 NOMINALES

		SELECT  cartera."IdEmpresa",
				cartera."IdCliente",
				cartera."IdPortafolio",           
				cartera."Nemotecnico" AS "Fondo",--Fondo
				'I' AS "TipoFondo", -- TipoFondo
				0 AS "TaxId",   --TaxId
				4 AS "TipoOperacion",    --TipoOperacion
				cartera."EGRFechaOperacion" AS "FechaInversion", -- Fecha de Pago del Evento
				CASE 	WHEN cartera."IdMonedaInstrumento" = 'USD' THEN 2
						WHEN cartera."IdMonedaInstrumento" = 'CLP' THEN 1
						WHEN cartera."IdMonedaInstrumento" = 'EUR' THEN 3
						WHEN cartera."IdMonedaInstrumento" = 'UF'  THEN 4
						WHEN cartera."IdMonedaInstrumento" = 'IVP' THEN 5
						ELSE 6 -- otro
				END AS "MonedaReajuste" , --MonedaReajuste
				0 AS "Monto", ----Monto
				0 AS "MontoActualizado",--MontoActualizado
				900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
				0 as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
				3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
				3 AS "CuotasCumplenReq",--CuotasCumplenReq
				0 as "ValorCuotaPesos",--ValorCuotaPesos
				0 as "PorParticipacionFondo",--PorParticipacionFondo
				"EGRMontoActualizado" as "ValNominalCuotasResc",--ValNominalCuotasResc
				cartera."EGRCantidad" as "CantCuotasResc",--CantCuotasResc
				0 as "DifRescateCuotasAct",--DifRescateCuotasAct
				0 as "DifRescateCuotasActFi",-- ESTE CAMPO ES SOLO PARA FFMM
				0 as "ValNominalInversion",--INFORMAR MONTO DEL RESCATE
				0 as "CantCuotasCedidas",--INFORMAR CUOTAS DEL RESCATE
				0 as "DifValCuotas",--DifValCuotas
				0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
				0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
				0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
				0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
				0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
				0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
				0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
				0 as "DevolucionCap",--DevolucionCap			decimal
				0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
				0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
				0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
				0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
				0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
				0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
				0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
				0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
				0 AS "Orden",
				'CARGAINI' AS "Origen" , 
				'OPT_CFI' AS "OrigenDet",
				'S' AS "IncluirEnDJ",--IncluirEnDJ			char
				'' AS "VBUsuario",--VBUsuario				varchar
				'' AS "Observacion",--Observacion			varchar
				'' AS "UsrUltMod", --UsrUltMod		varchar
				CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
				cartera."Nemotecnico" AS "CodigoFondo",--CodigoFondo,	varchar
				0 AS "SecEvento",  --- SecEvento
				'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
				0 as "Cantidad",	          --- cantidad
				CASE WHEN cartera."IdTipoInstrumento" = 'CFI' THEN 'CF' ELSE 'EN' END AS "Mercado",  --- Mercado
				'' AS "CodMovto",--- CodMovto
				'' AS "FecMovto", --- FecMovto	
				"FolioComprobante" AS "SecMovto" , --- SecMovto
				1 AS "TributaSa"--,         --Tributa SA         
				,'N' AS "AcogidoISFUT"  
				,cartera."IdInstrumento"
				,cartera."Nemotecnico" AS "RunNemotecnico" --C139
			FROM (
				SELECT  
				"IdEmpresa",
				"IdCliente",
				"IdPortafolio",
				"IdTipoInstrumento",
				"DocumentoIdentidad", 
				"FechaLiquidacion" AS "EGRFechaOperacion",
				0 as "MontoInversion", 
				"CantidadTotal" AS "EGRCantidad",
				0 AS "MontoInversionActualizado",
				"IdMonedaNegociacion" AS "IdMonedaInstrumento",
				"Nemotecnico",
				"Precio" AS "EGRPrecio",
				"MontoComprobante",
				"EGRMontoActualizado" AS "EGRMontoActualizado",
				0 AS "MayorMenorValorActualizado",
				 0 AS "Valor",
				"FolioComprobante",
				"CodigoTipoOperacion",
				"IdInstrumento"

				FROM 
					(
					SELECT 
					COM."DocumentoIdentidad"
					,COM."CodigoTipoOperacion"
					,COM."IdMonedaNegociacion"
					,COM."IdTipoInstrumento"
					,COM."IdPortafolio"
					,COM."IdCliente"
					,COM."IdEmpresa"
					,COM."FechaComprobante" AS "FechaLiquidacion"
					,DET."NemotecnicoInstrumento" AS "Nemotecnico"
					,DET."Cantidad" AS "CantidadTotal"
					,ROUND(COM."Precio"*COM."ValorTipoCambio",0) AS "Precio",
					"MontoComprobante",
					
					
					0 AS "MontoCompra" ,
					0 as "MontoVenta",
					--COALESCE(round(round("EGRMonto" - (("EGRCantidad"/ "Cantidad")* "MontoCompra"),0) * (IPCACT."Valor"/IPCINV."Valor"),0),0) as "MayorMenorValorActualizado"
					
					0 as "MontoInversion", 
					0 AS "MontoInversionActualizado", --mismo cambio que FFMM 
					
					DET."MontoComprobanteDetalle" AS "EGRMontoActualizado"
					
					,COM."FolioComprobante"
					,COM."IdInstrumento"
					FROM "BSCL.Facturacion.BD.Vistas::ConsultarComprobante" COM
					LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteDetalle" DET ON COM."IdComprobante"=DET."IdComprobante"
					INNER JOIN "BSCL.Portafolio.BD.Vistas.UI::ConsultarClientePortafolio" por ON por."IdPortafolio" = COM."IdPortafolio"
					
					WHERE DET."Cantidad" > 0
					AND  COM."CodigoTipoOperacion" IN ('VENCFI','VENETN') AND COM."IdEstadoDTE"='REC' AND COM."IdTipoComprobanteReferencia" NOT IN ('NCE','NEE','NEO','NCR')
					AND COM."IdFormaCustodia"='NOM'
					AND (por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
					ORDER BY COM."FolioComprobante")
				)cartera
				
UNION ALL     ---custodias

			SELECT  HIS."IdEmpresa",
					HIS."IdCliente",
					HIS."IdPortafolio",
					HIS."Nemotecnico" AS "Fondo",--Fondo
					'I' AS "TipoFondo", -- TipoFondo
					0 AS "TaxId",   --TaxId
					7 AS "TipoOperacion",    --TipoOperacion
					CONCAT(YEAR(HIS."FechaRespaldo"), '-12-31')  AS "FechaInversion", -- Fecha de Pago del Evento
					CASE 	WHEN HIS."IdMonedaInstrumento" = 'USD' THEN 2
							WHEN HIS."IdMonedaInstrumento" = 'EUR' THEN 3
							WHEN HIS."IdMonedaInstrumento" = 'UF'  THEN 4
							WHEN HIS."IdMonedaInstrumento" = 'IVP' THEN 5
							WHEN HIS."IdMonedaInstrumento" = 'CLP' THEN 1
							ELSE 6 -- Otros
					END AS "MonedaReajuste" , --MonedaReajuste
					HIS."INGMontoATasaMercadoActual" AS "Monto", ----Monto
					COALESCE(HIS."MtoNominalAporteActualizado", 0) AS "MontoActualizado",--MontoActualizado
					900 AS "NumSeriesCuotasParticipe",--NumSeriesCuotasParticipe
					HIS."INGCantidad" as "NumCuotasSerieParticipe",--NumCuotasSerieParticipe
					3 AS "ParticipeCumpleReq",--ParticipeCumpleReq
					3 AS "CuotasCumplenReq",--CuotasCumplenReq
					HIS."INGPrecioMercadoActual" as "ValorCuotaPesos",--ValorCuotaPesos
					0 as "PorParticipacionFondo",--PorParticipacionFondo
					0 as "ValNominalCuotasResc",--ValNominalCuotasResc
					0 as "CantCuotasResc",--CantCuotasResc
					0 as "DifRescateCuotasAct",--DifRescateCuotasAct
					0 as "DifRescateCuotasActFi",--DifRescateCuotasActFi
					0 as "ValNominalInversion",--ValNominalInversion
					0 as "CantCuotasCedidas",--CantCuotasCedidas
					0 as "DifValCuotas",--DifValCuotas
					0 as "DivRemDistAfectaGC",--DivRemDistAfectaGC				-- Factor 06
					0 as "DivRemDistExentaGC",--DivRemDistExentaGC		decimal -- Factor 07 
					0 as "DivRemDistNoRenta",--DivRemDistNoRenta		decimal -- Factor 08
					0 as "DisRemAfeImptoUnico10",--DisRemAfeImptoUnico10	decimal 
					0 as "DisRemNoAfeImptoUnico10",--DisRemNoAfeImptoUnico10	decimal
					0 as "DisRemAfeImptoUnico4",--DisRemAfeImptoUnico4	decimal
					0 as "DisDismValCuoNoCap",--DisDismValCuoNoCap		decimal
					0 as "DevolucionCap",--DevolucionCap			decimal
					0 as "IncremImpuestoPriCat",--IncremImpuestoPriCat	decimal -- Factor 11
					0 as "Cred1CatConDerDevolAfe",--Cred1CatConDerDevolAfe	decimal -- Factor 09
					0 as "Cred1CatSinDerDevolAfe",--Cred1CatSinDerDevolAfe	decimal -- Factor 13
					0 as "Cred1CatConDerDevolExe",--Cred1CatConDerDevolExe	decimal -- Factor 12
					0 as "Cred1CatSinDerDevolExe",--Cred1CatSinDerDevolExe	decimal -- Factor 14
					0 as "CredImpuExt",--CredImpuExt				decimal -- Factor 15
					0 as "ImpTasaAdic",--ImpTasaAdic				decimal -- Factor 10
					0 as "MtoInvCuotasLey18815",--MtoInvCuotasLey18815	decimal
					0 AS "Orden",
					'CARGAINI' AS "Origen" , 
					'OPT_CUS' AS "OrigenDet",
					'S' AS "IncluirEnDJ",--IncluirEnDJ			char
					'' AS "VBUsuario",--VBUsuario				varchar
					'' AS "Observacion",--Observacion			varchar
					'' AS "UsrUltMod", --UsrUltMod		varchar
					CURRENT_TIMESTAMP AS "FecUltMod",--FecUltMod		datetime
					HIS."Nemotecnico" AS "CodigoFondo",--CodigoFondo,	varchar
					0 AS "SecEvento",  --- SecEvento
					'P' AS "TipoRegistro",		  --- TipoRegistro Pagado
					HIS."INGCantidad" as "cantidad",	          --- cantidad
					CASE WHEN HIS."IdTipoInstrumento" = 'CFI' THEN 'CF'	 ELSE 'EN'	END AS "Mercado",  --- Mercado
					'' AS "CodMovto",--- CodMovto
					HIS."FechaRespaldo" AS "FecMovto", --- FecMovto	
					0 AS "SecMovto" , --- SecMovto
      	            1 AS "TributaSa"         --Tributa SA                
					,'N' AS "AcogidoISFUT"
					,HIS."IdInstrumento"
					,HIS."Nemotecnico" AS "RunNemotecnico"
					FROM 
					(
						SELECT HISFEC."FechaRespaldo",
						HISFEC."IdEmpresa",
						HISFEC."FechaOperacion",
						HISFEC."IdCliente",
						HISFEC."IdPortafolio", 
						HISFEC."INGCantidad", 
						HISFEC."Cantidad",
						HISFEC."Nemotecnico",
						HISFEC."IdRegimenFondoMutuo",
						HISFEC."MontoCompra",
						HISFEC."IdMonedaInstrumento",
						HISFEC."IdTipoInstrumento",
						HISFEC."IdEstado",
						HISFEC."MtoNominalAporteActualizado",
						HISFEC."INGMontoATasaMercadoActual",
						HISFEC."INGPrecioMercadoActual",
						HISFEC."IdInstrumento"
								FROM (SELECT H."IdEmpresa",H."IdCliente", H."FechaOperacion", H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento", H."IdInstrumento",
											H."IdTipoInstrumento",H."IdEstado",COALESCE(H."IdRegimenFondoMutuo",'') AS "IdRegimenFondoMutuo",
											H."Cantidad",H."MontoCompra",H."FechaRespaldo",
											sum(round(H."INGMontoATasaMercadoActual",0)) AS "INGMontoATasaMercadoActual",
											sum(round(((H."INGCantidad" / NULLIF(H."Cantidad", 0)) * H."MontoCompra") * (NULLIF(MONREAJUSTEACT."Valor", 0) / NULLIF(MONREAJUSTEINV."Valor", 0)), 0)) as "MtoNominalAporteActualizado",
											sum(H."INGCantidad") AS "INGCantidad"
											--	Asi debiese ser pero esta mal cargado el valor del dolar en Optimus
											,CASE WHEN H."IdMonedaInstrumento" = 'USD' THEN max(round((H."INGPrecioMercadoActual" * PAR."Valor"),2)) ELSE 	max(H."INGPrecioMercadoActual") END as "INGPrecioMercadoActual"
											,RANK() over (partition by  H."FolioCartera" order by H."IdCartera" desc) as RANK ---Solo ultimo reg de cartera (Saldo final del periodo)
										FROM "BSCL.Custodia.BD::HIS.Cartera" H
										INNER JOIN
											(			
												SELECT TOP 5 "FechaRespaldoFinal" FROM ---Fechas finales de respaldo por periodo (acotado a 5 periodos 
												(
													SELECT  MAX("FechaRespaldo") AS "FechaRespaldoFinal" ,YEAR("FechaRespaldo") FROM  "BSCL.Custodia.BD::HIS.Cartera" 
													WHERE   "IdClasificacionTipoInstrumento"='RV' 
													AND "IdTipoInstrumento" IN ('CFI','ETN') 
													AND MONTH("FechaRespaldo")='12'
													AND "IdEstado" = 'VIG'
													AND "INGMontoATasaMercadoActual" > 0
													AND "IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
													GROUP BY YEAR("FechaRespaldo")
													ORDER BY YEAR("FechaRespaldo") DESC
												)
											)GRFECHA	
										ON 	H."FechaRespaldo"=GRFECHA."FechaRespaldoFinal"
										AND H."IdClasificacionTipoInstrumento"='RV' 
										AND H."IdTipoInstrumento" IN ('CFI','ETN') 
										AND MONTH(H."FechaRespaldo")='12'
										AND H."IdEstado" = 'VIG'
										AND H."INGMontoATasaMercadoActual" > 0
										AND H."IdRegimenFondoMutuo" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T')
										
										LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEINV ON MONREAJUSTEINV."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END) 
																													AND MONREAJUSTEINV."Fecha" = 
																									( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'EUR' THEN
																													TO_DATE("FechaOperacion", 'YYYY-MM-DD')
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
									  
									    
									    		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONREAJUSTEACT ON MONREAJUSTEACT."IdParametroFinanciero" = (CASE "IdMonedaInstrumento" WHEN 'USD' THEN 'DO' WHEN 'CLP' THEN 'IPC' WHEN 'IVP' THEN 'IPC' ELSE "IdMonedaInstrumento" END)
									    													AND MONREAJUSTEACT."Fecha" = ( CASE "IdMonedaInstrumento"
																											WHEN  'USD' THEN
																													TO_CHAR(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) + 1) || '-01-01'
																											WHEN  'EUR' THEN
																													TO_CHAR(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) + 1) || '-01-01'
																											WHEN  'UF' THEN
																													(YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-31')
																											WHEN 'CLP' THEN
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																											ELSE
																												    (YEAR(TO_DATE("FechaOperacion", 'YYYY-MM-DD')) ||'-12-01')
																										END) 
										
									 LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON H."FechaOperacion" = PAR."Fecha"     AND PAR."IdParametroFinanciero" = 'DO'
									 GROUP BY H."IdCliente",H."IdPortafolio",H."Nemotecnico",H."IdClasificacionTipoInstrumento",H."IdMonedaInstrumento",H."IdInstrumento",H."IdTipoInstrumento",H."IdRegimenFondoMutuo",H."FechaRespaldo",H."IdEmpresa",H."FechaOperacion"
											,H."IdEstado",H."Cantidad",H."MontoCompra",H."FolioCartera",H."IdCartera",H."IdCliente"				  
									 )HISFEC WHERE HISFEC.RANK = 1


									
					) HIS 
				INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio"= HIS."IdPortafolio" 
 				WHERE HIS."MtoNominalAporteActualizado" > 0
				AND (por."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
				AND  NOT EXISTS  (
							SELECT	1 FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarEventoCapitalCliente" ec
							WHERE ec."IdClasificacionTipoInstrumento" = 'RV' 
							AND 	ec."IdTipoInstrumento" IN ('CFI','ETN')
							AND 	ec."IdTipoEventoCapital" = 'DV'
							AND 	ec."Total" > 0
							AND 	(ec."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND ec."Portafolio" NOT IN ('REGAPV'))
							AND 	ec."TipoSocioNegocio" = 'CLI'
							AND 	ec."IdEstado" = 'CER'
							AND  	HIS."IdCliente"= ec."IdCliente"
							AND  	year(HIS."FechaRespaldo") =year(ec."FechaPago")
							)
				
				AND  NOT EXISTS  (
								SELECT	1 FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol 
								LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" por ON por."IdPortafolio" = sol."IdPortafolio"
								WHERE sol."IdTipoMovimientoFondoMutuo" IN ('APODIV','APODIVCFI')
									AND 	sol."MontoMonedaSistema" > 0
									AND 	(sol."IdRegimenTributario" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R','57B','57T') AND por."Proposito" NOT IN ('REGAPV'))
									AND 	sol."Estado" = 'CRT'	
									AND  	HIS."IdCliente"= sol."IdCliente"
									AND  	year(HIS."FechaRespaldo") =year(sol."FechaValorCuota")
					)
				AND year(HIS."FechaRespaldo") <= 2024
	
								
) AS LG
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = LG."IdCliente"
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."IdInstrumento") = trim(LG."IdInstrumento")
LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" fondo ON trim(fondo."IdSerieFondo") = trim(LG."Fondo")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
LEFT  JOIN "BSCL.Plataforma.BD::MER.Emisor" emi ON (ins."IdEmisor"=emi."IdEmisor")
LEFT  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" peremi ON (peremi."IdPersona"=emi."IdPersona")
LEFT  JOIN "BSCL.Plataforma.BD::REG.Pais"  				pais ON (pais."IdPais" = cli."IdPaisResidencia")
LEFT  JOIN "BSCL.Normativa.BD::DOC.Homologacion"  Rutfondos on (Rutfondos."Esquema"='0' AND Rutfondos."Tipo"='RutFondo'  AND (inst."DocumentoIdentidadFondo"=Rutfondos."ValorOrigen" OR fondo."CodigoSVS" = RutFondos."ValorOrigen" ))
LEFT  JOIN "BSCL.Normativa.BD.Vistas::TRI.DJ1922.PresenciaBursatil" pres ON (trim(LG."Fondo")=trim(pres."NEMO") AND LG."FechaInversion" BETWEEN  pres."FEC_DESDE" AND pres."FEC_HASTA")
WHERE NOT (
    LG."TipoRegistro" = '7'
    AND LG."FechaInversion" >= '2025-01-01'
)

WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Origen" TO bcs_desarrollador WITH GRANT OPTION;

