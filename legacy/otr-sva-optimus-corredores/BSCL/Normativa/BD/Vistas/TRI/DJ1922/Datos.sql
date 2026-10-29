SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos" (
    "ID"
    ,"IdDocumento"
    ,"VboUsuario"
    ,"Observaciones"
    ,"FuenteDatos"
   	,"Numero"		
   	,"IdOperacion"								
	,"RutAdministradora"
	,"IdEmisor"								
	,"RutFondo"	
	,"RunFondo"
	,"Fondo"									
	,"TipoFondo"
	,"TipoFondoDescripcion"
	,"RutCliente"										
	,"NombreParticipe"								
	,"TaxId"											
	,"CodigoPais"	
	,"CodigoPaisDescripcion"
	,"TipoOperacion"	
	,"TipoOperacionDescripcion"
	,"FechaInversion"								
	,"MonedaReajuste"
	,"MonedaReajusteDescripcion"
	,"MontoNominalAportes"
	,"MontoNominalAportesActualizado"							
	,"NumeroSerieCuotas"								
	,"CantidadCuotasParticipe"						
	,"ParticipeCumpleRequisitos"	
	,"ParticipeCumpleRequisitosDescripcion"
	,"CuotasCumplenRequisitos"
	,"CuotasCumplenRequisitosDescripcion"
	,"ValorCuotaPesos"	
	,"NumCuotasSerieParticipe"
	,"PorcentajeParticipacion31Dic"					
	,"ValNominalCuotasRescatadas"					
	,"CantidadCuotasRescatadas"						
	,"DiferenciaPorRescateOEnajenacion"
	,"DiferenciaPorRescateOEnajenacionActualizado"				
	,"DifPorRescateOEnajenacionCFIAcogido107"
	,"DifPorRescateOEnajenacionCFIAcogido107Actualizado"		
	,"ValNominalInversionaFechaLiquidacion"			
	,"CantCuotasCedidasPorLiquidacion"				
	,"DiferenciaValorCuotasFechaLiquidacion"
	,"DiferenciaValorCuotasFechaLiquidacionActualizado"			
	--,"DividendosAfectosIGCoAdicional"
	--,"DividendosAfectosIGCoAdicionalActualizado"				
	,"DividendosExentosIGCoAdicional"
	,"DividendosExentosIGCoAdicionalActualizado"				
	,"DividendosNoConstitutivosDeRenta"
	,"DividendosNoConstitutivosDeRentaActualizado"				
	,"DividendosAfectosImptoUnicoTasa10"
	,"DividendosAfectosImptoUnicoTasa10Actualizado"				
	--,"DividendosNoAfectosImptoUnicoTasa10"
	--,"DividendosNoAfectosImptoUnicoTasa10Actualizado"			
	,"DividendosAfectosImptoUnicoTasa4"
	,"DividendosAfectosImptoUnicoTasa4Actualizado"				
	,"DistribucionXDiminucionNoImputadaCapital"		
	,"DevolucionesDeCapital"
	,"DevolucionesDeCapitalActualizado"							
	--,"IncrementoPorImpuesto"							
	--,"CreditoImpto1CatConDevolucionAfectas"			
	--,"CreditoImpto1CatSinDevolucionAfectas"			
	--,"CreditoImpto1CatConDevolucionExentas"			
	--,"CreditoImpto1CatSinDevolucionExentas"			
	--,"CreditoPorImpuestosExternos"					
	--,"ImpuestosTasaAdicionalExArticulo21"			
	,"MontoInversionenCuotasCFIAnterior93"			
	,"NumeroCertificado"
	,"FactorActualizacion"
	,"FechaOrigenInversion"																														
	,"TipoDocumento"
	,"NumeroDocumento"
	,"FechaDocumento"
	,"IdBaseDocumento"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoTransaccion"
	,"IdTipoOperacion"
	,"IdTipoProducto"
	,"TributaSa"
	,"TributaSaDescripcion"
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
	,"SecEvento"
	,"AcogidoISFUT"
	,"RunNemotecnico"
	,"TipoContribuyente"
	,"TipoContribuyenteDescripcion"
) AS
SELECT
    a."ID"
    ,a."IdDocumento"
    ,a."VboUsuario"
    ,COALESCE(VAL."CodigoValidacion" ,a."Observaciones") AS "Observaciones"
    ,a."FuenteDatos"
   	,a."Numero"		
   	,a."IdOperacion"								
	,a."RutAdministradora"
	,Case 
	    WHEN (a."IdEmisor" IS NULL OR a."IdEmisor" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN COALESCE(emi."IdEmisor",'') 
	    Else COALESCE(a."IdEmisor",'')
	 End As "IdEmisor" 								
	,Case 
	    WHEN (a."RutFondo" IS NULL OR a."RutFondo" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN  
	    	CASE WHEN a."TipoFondo"='F' THEN COALESCE(fondo."CodigoSVS",'') ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END
	    ELSE COALESCE(a."RutFondo",'')
	 End As "RutFondo" 
	,Case 
	    WHEN (a."RunFondo" IS NULL OR a."RunFondo" = '') AND a."FuenteDatos" = 'EXTERNA'  THEN 
	    	CASE WHEN a."TipoFondo"='F' THEN COALESCE(fondo."CodigoSVS",'') ELSE COALESCE(inst."DocumentoIdentidadFondo",'') END
	    ELSE COALESCE(a."RunFondo", '') 
	 End As "RunFondo"
	,a."Fondo"									
	,COALESCE(a."TipoFondo",'')
	,COALESCE(g."Descripcion",'') as "TipoFondoDescripcion"
	,a."RutCliente" AS "RutCliente"									
	,a."NombreParticipe"								
	,a."TaxId"											
	,a."CodigoPais"									
	,COALESCE(h."Descripcion",'') as "CodigoPaisDescripcion"
	,a."TipoOperacion"									
	,COALESCE(i."Descripcion",'') as "TipoOperacionDescripcion"
	,a."FechaInversion"								
	,COALESCE(a."MonedaReajuste",'') AS "MonedaReajuste"			 					
	,COALESCE(j."Descripcion",'') as "MonedaReajusteDescripcion"
	,a."MontoNominalAportes"
	,COALESCE(a."MontoNominalAportesActualizado",0) AS "MontoNominalAportesActualizado"
	,COALESCE(a."NumeroSerieCuotas",900)	
	,COALESCE(a."CantidadCuotasParticipe",0) AS "CantidadCuotasParticipe"
	,COALESCE(a."ParticipeCumpleRequisitos",0) AS "ParticipeCumpleRequisitos"
	,COALESCE(k."Descripcion",'') as "ParticipeCumpleRequisitosDescripcion"
	,COALESCE(a."CuotasCumplenRequisitos",0) AS "CuotasCumplenRequisitos"
	,COALESCE(l."Descripcion",'') as "CuotasCumplenRequisitosDescripcion"
	,COALESCE(a."ValorCuotaPesos",0) AS "ValorCuotaPesos"
	,COALESCE(a."NumCuotasSerieParticipe",0)
	,COALESCE(f."PorcentajeAccionario",a."PorcentajeParticipacion31Dic",0) AS "PorcentajeParticipacion31Dic"					
	,a."ValNominalCuotasRescatadas"					
	,a."CantidadCuotasRescatadas"						
	,a."DiferenciaPorRescateOEnajenacion"
	,a."DiferenciaPorRescateOEnajenacionActualizado"				
	,a."DifPorRescateOEnajenacionCFIAcogido107"
	,a."DifPorRescateOEnajenacionCFIAcogido107Actualizado"		
	,a."ValNominalInversionaFechaLiquidacion"			
	,a."CantCuotasCedidasPorLiquidacion"				
	,a."DiferenciaValorCuotasFechaLiquidacion"
	,a."DiferenciaValorCuotasFechaLiquidacionActualizado"			
	--,a."DividendosAfectosIGCoAdicional"
	--,a."DividendosAfectosIGCoAdicionalActualizado"				
	,a."DividendosExentosIGCoAdicional"
	,a."DividendosExentosIGCoAdicionalActualizado"				
	,a."DividendosNoConstitutivosDeRenta"
	,a."DividendosNoConstitutivosDeRentaActualizado"				
	,a."DividendosAfectosImptoUnicoTasa10"
	,a."DividendosAfectosImptoUnicoTasa10Actualizado"				
	--,a."DividendosNoAfectosImptoUnicoTasa10"
	--,a."DividendosNoAfectosImptoUnicoTasa10Actualizado"			
	,a."DividendosAfectosImptoUnicoTasa4"
	,a."DividendosAfectosImptoUnicoTasa4Actualizado"				
	,a."DistribucionXDiminucionNoImputadaCapital"		
	,a."DevolucionesDeCapital"
	,a."DevolucionesDeCapitalActualizado"							
	--,a."IncrementoPorImpuesto"							
	--,a."CreditoImpto1CatConDevolucionAfectas"			
	--,a."CreditoImpto1CatSinDevolucionAfectas"			
	--,a."CreditoImpto1CatConDevolucionExentas"			
	--,a."CreditoImpto1CatSinDevolucionExentas"			
	--,a."CreditoPorImpuestosExternos"					
	--,a."ImpuestosTasaAdicionalExArticulo21"			
	,a."MontoInversionenCuotasCFIAnterior93"			
	,a."NumeroCertificado"
	,a."FactorActualizacion"
	,a."FechaOrigenInversion"																																												
	,a."TipoDocumento"
	,a."NumeroDocumento"
	,a."FechaDocumento"
	,a."IdBaseDocumento"
	,a."IdCliente"
	,a."IdPortafolio"
	,a."TipoTransaccion"
	,a."IdTipoOperacion"
	,a."IdTipoProducto"
	,COALESCE(a."TributaSa",'')
	,COALESCE(t."Descripcion",'') as "TributaSaDescripcion"
    ,a."DivRemDistAfectGCConIDPC"  --DivRemDistAfectGCConIDPC
	,a."DivRemDistAfectGCConIDPCal31122016"  --DivRemDistAfectGCConIDPCal31122016
	,a."DivRemDistAfectGCConIDPCVOL"  --DivRemDistAfectGCConIDPCVOL
	,a."DivRemDistAfectGCSINDC"  --DivRemDistAfectGCSINDC
	,a."DivRemDistNoRentaFUT"  --DivRemDistNoRentaFUT
	,a."DivRemDistNoRentaFE"  --DivRemDistNoRentaFE
	,a."CredImpGloCompNSRSD"  --CredImpGloCompNSRSD
	,a."CredImpGloCompNSRCD"  --CredImpGloCompNSRCD
	,a."CredImpGloCompSRSD"  --CredImpGloCompSRSD
	,a."CredImpGloCompSRCD"  --CredImpGloCompSRCD
	,a."CredImpGloCompCTDCIF"  --CredImpGloCompCTDCIF
	,a."CredImpGloCompCDD31122016"  --CredImpGloCompCDD31122016
	,a."CredImpGloCompSDD31122016"  --CredImpGloCompSDD31122016
	,a."CredImpGloCompCTDCIF31122016"  --CredImpGloCompCTDCIF31122016
	,a."CredImpGloCompCITAExArt21"  --CredImpGloCompCITAExArt21                
	,a."DisRemRentasExIGCAfecImptoAdi_C100"  --DisRemRentasExIGCAfecImptoAdi_C100
	,a."CredIGCAcumD01012017RANSRD01012020SDD_C101"  --CredIGCAcumD01012017RANSRD01012020SDD_C101
	,a."CredIGCAcumD01012017RANSRD01012020CDD_C102"  --CredIGCAcumD01012017RANSRD01012020CDD_C102
	,a."CredIGCAcumD01012017RXSRSDD_C103"  --CredIGCAcumD01012017RXSRSDD_C103
	,a."CredIGCAcumD01012017RXSRCDD_C104"  --CredIGCAcumD01012017RXSRCDD_C104
	,a."CredIGCAcumH31122016RXSDD_C105"  --CredIGCAcumH31122016RXSDD_C105
	,a."CredIGCAcumH31122016RXCDD_C106"  --CredIGCAcumH31122016RXCDD_C106
	,a."DisRemRentasTribCProvRap_C96"  --DisRemRentasTribCProvRap_C96
	,a."DisRemRentasTribCORP_C97"  --DisRemRentasTribCORP_C97
	,a."DisRemRentasTribCExDD_C98"  --DisRemRentasTribCExDD_C98
	,a."DisRemRentasTribCUtAFFut_C99"  --DisRemRentasTribCUtAFFut_C99			
	,COALESCE(a."DetBenNetoPerRutFdo_C107",'') --DetBenNetoPerRutFdo_C107
	,COALESCE(a."DetBenNetoPerPOS_C113",0)  --DetBenNetoPerPOS_C113
	,COALESCE(a."DetBenNetoPerNEG_C114",0)  --DetBenNetoPerNEG_C114
	,COALESCE(a."DetBenNetoUtilPer_C133",0)  --DetBenNetoUtilPer_C133
	,COALESCE(a."DetBenNetoIntPer_C134",0)  --DetBenNetoIntPer_C134
	,COALESCE(a."DetBenNetoDivPer_C135",0)   --DetBenNetoDivPer_C135
	,COALESCE(a."DetBenNetoGanCapPer_C136",0)  --DetBenNetoGanCapPer_C136
	,COALESCE(a."DetBenNetoPerGastDev_C137",0)  --DetBenNetoPerGastDev_C137
	,COALESCE(a."NumSerieCuotasParticipe_C138",TO_VARCHAR(a."NumeroSerieCuotas"),'900') --NumSerieCuotasParticipe_C138   	
	,COALESCE(a."SecEvento",0)
	,a."AcogidoISFUT"
	,a."RunNemotecnico" 	--C139
	,a."TipoContribuyente"
	,COALESCE(m."Descripcion",'') as "TipoContribuyenteDescripcion"
FROM "BSCL.Normativa.BD::TRI.DatosDJ1922" a
INNER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarDocumento" d ON (d."IdDocumento" = a."IdDocumento")
LEFT  JOIN "BSCL.Normativa.BD::TRI.Fondos" f ON (a."Fondo" = f."IdFondo" AND f."Esquema" = 'DJ1922' AND f."Version" = d."Version" AND f."IdEstado" = 'ACT')
--LEFT  JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" b ON (a."MonedaReajuste" = b."ValorOrigen" AND b."Esquema"='DJ1922' AND b."Tipo" = 'UNIDADMONEDA' AND a."FechaInversion" BETWEEN b."FechaDatosDesde" AND b."FechaDatosHasta")
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" t ON t."Tipo"='TributaSa1922' and a."TributaSa"=t."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" g    on g."Tipo"='TipoFondo1922' and a."TipoFondo"=g."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" h    on h."Tipo"='CodigoPais1922' and a."CodigoPais"=h."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" i    on i."Tipo"='TipoOperacionFondos1922' and a."TipoOperacion"=i."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" j    on j."Tipo"='TipoReajuste1922' and a."MonedaReajuste"=j."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" k    on k."Tipo"='ParticipeCumpleRequisitos1922' and a."ParticipeCumpleRequisitos"=k."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" l    on l."Tipo"='CuotasCumplenRequisitos1922' and a."CuotasCumplenRequisitos"=l."Codigo"
LEFT  JOIN "BSCL.Normativa.BD::DOC.Diccionario" m    on m."Tipo"='TipoContribuyente' and a."TipoContribuyente"=m."Codigo"
LEFT  JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" AS fondo ON trim(fondo."IdSerieFondo") = trim(a."Fondo")
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."Nemotecnico") = trim(a."Fondo") AND ins."IdClasificacionTipoInstrumento" = 'RV'
LEFT JOIN  "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."IdInstrumento") = TRIM(ins."IdInstrumento")
LEFT  JOIN "BSCL.Plataforma.BD::MER.Emisor" emi ON (COALESCE(ins."IdEmisor", fondo."IdEmisor") = emi."IdEmisor")
LEFT JOIN (
           	SELECT "CodigoValidacion", "IdDocumento" ,"ID"
			FROM (
			    SELECT 
			        R."CodigoValidacion", DT."IdDocumento", DT."ID", 
			        ROW_NUMBER() OVER (
			            PARTITION BY DT."ID" 
			            ORDER BY R."ID" ASC
			        ) AS rn
			    FROM "BSCL.Normativa.BD::TRI.ResumenValidacion" AS R
			    INNER JOIN "BSCL.Normativa.BD::TRI.DatosDJ1922" AS DT ON DT."IdDocumento" = R."IdDocumento"
			    INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" AS DV ON DV."IdResumenValidacion" = R."ID" AND DV."IdDetalleDocumento" = DT."ID" 
			) AS t WHERE t.rn = 1
        ) AS VAL ON  a."IdDocumento" = VAL."IdDocumento" AND a."ID" = VAL."ID"
WITH read ONLY;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1922.Datos" TO bcs_desarrollador WITH GRANT OPTION;
