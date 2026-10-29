SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" (
	"IdEmpresa"
	,"Numero"
	,"FolioCartera"
	,"IdFondo"
	,"Nemo"
	,"IdEmisor"
	,"RutEmisor"
	,"RutParticipe"
	,"RutFondo"
	,"RunFondo"
	,"TipoOperacion"
	,"FechaOperacion"
	,"FechaInversion"
--	,"MontoNominalInversion"
	,"MontoNominalInversionActualizado"
	,"MontoHistorico"
	,"FechaRescate"
	,"MontoNominalRescate"
--	,"MayorValor"
	,"MayorValorActualizado"
--	,"MenorValor"
	,"MenorValorActualizado"
	,"RutAdminitradoraFondoReceptor"
	,"MontoReinvertido"
	,"NumeroCertificado" 
	,"CostoUf"
	,"Cantidad"
	,"IdCliente"
	,"IdPortafolio"
	,"TipoDeclarante"
	,"IdSolicitudAporteRescate"
	,"IdSerieFondoDestino"
) AS

/**************************** RESCATES PROPORCIONAL ************************/
SELECT
"IdEmpresa"
, "Numero"
, "FolioCartera"
, "IdFondo"
, "Nemo"
, "IdEmisor"
, "RutEmisor"
, "RutParticipe"
, "RutFondo"
, "RunFondo"
, "TipoOperacion"
, "FechaOperacion"
, "FechaInversion"
, "MontoNominalInversionActualizado"
, "MontoHistorico"
, "FechaRescate"
, "MontoNominalRescate"
, "MayorValorActualizado"
, "MenorValorActualizado"
, "RutAdminitradoraFondoReceptor"
, "MontoReinvertido"
, "NumeroCertificado"
, "CostoUf"
, "Cantidad"
, "IdCliente"
, "IdPortafolio"
, "TipoDeclarante"
,"IdSolicitudAporteRescate"
,"IdSerieFondoDestino"
FROM(
	SELECT
		REG."IdEmpresa",
		REG."IdSolicitud" AS "Numero",
		REG."FolioCartera" AS "FolioCartera",
		REG."IdFondo" AS "IdFondo",
		REG."Nemo" AS "Nemo",
		'' AS "IdEmisor",
		REG."RutEmisor" AS "RutEmisor",
		REG."RutParticipe" AS "RutParticipe",
		REG."RutFondo" AS "RutFondo",
		REG."RunFondo" AS "RunFondo",
		REG."TipoOperacion" AS "TipoOperacion",
		REG."FechaOperacion",
		'1900-01-01' AS "FechaInversion",
		0 AS "MontoNominalInversionActualizado",
		SUM(REG."MontoHistorico") AS "MontoHistorico",
		REG."FechaRescate" AS "FechaRescate",
		SUM(REG."MontoNominalRescate") AS "MontoNominalRescate",
		SUM(REG."MayorValorActualizado") AS "MayorValorActualizado",
		SUM(REG."MenorValorActualizado") AS "MenorValorActualizado",
		REG."RutAdminitradoraFondoReceptor" AS  "RutAdminitradoraFondoReceptor",
		SUM(REG."MontoReinvertido") AS "MontoReinvertido",
		0 AS "NumeroCertificado",
		SUM(REG."CostoUf") AS "CostoUf",
		SUM(REG."Cantidad") AS "Cantidad",
		REG."IdCliente",
		REG."IdPortafolio",
		REG."TipoDeclarante",
		REG."IdSolicitudAporteRescate",
		REG."IdSerieFondoDestino"
	FROM 
		(SELECT 
			OPER."Numero" AS "IdSolicitud",
			OPER."FolioCartera",
			OPER."RutAdminitradoraFondoReceptor",
			OPER."TipoOperacion",
			OPER."IdFondo",
			OPER."Nemo",
			"MontoNominalRescateProporcional" AS "MontoNominalRescate",
			"MayorValorActualizadoProporcional" AS "MayorValorActualizado",
			"MenorValorActualizadoProporcional" AS "MenorValorActualizado",
			"MontoReinvertidoProporcional"AS "MontoReinvertido",
			"MontoHistoricoProporcional" AS "MontoHistorico",
			"CostoUf" AS "CostoUf",
			OPER."RutEmisor",
			OPER."RutParticipe",
			OPER."RutFondo" ,
			OPER."RunFondo",
			OPER."FechaOperacion",
			OPER."FechaRescate" ,
			OPER."Cantidad" AS "Cantidad",
			OPER."IdPortafolio",
			OPER."IdCliente",
			OPER."TipoDeclarante",
			OPER."IdEmpresa",
			OPER."IdSerieFondoDestino",
			OPER."IdSolicitudAporteRescate"
		FROM
			(SELECT 
				TRIM(LPAD(COALESCE(emi_rein."DocumentoIdentidad",''),11,' ')) AS "RutAdminitradoraFondoReceptor",
				CASE 
				WHEN rein_ser_fon_des."IdRegimenTributario" IN ('18T') THEN 
					CASE COALESCE(Hom."ValorDestino",6) WHEN 8 THEN 6 
														WHEN 9 THEN 7
					ELSE COALESCE(Hom."ValorDestino",6)
					END
				ELSE COALESCE(Hom."ValorDestino",6)
				END AS "TipoOperacion",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."MayorValorActualizado" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."MayorValorActualizado" END AS "MayorValorActualizadoProporcional",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."MenorValorActualizado" * (rein_ser_fon_des."Porcentaje"/100)
					ELSE REINT."MenorValorActualizado" END AS "MenorValorActualizadoProporcional",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."MontoNominalRescate" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."MontoNominalRescate" END AS "MontoNominalRescateProporcional",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."MontoReinvertido" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."MontoReinvertido" END AS "MontoReinvertidoProporcional",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."MontoHistorico" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."MontoHistorico" END AS "MontoHistoricoProporcional",
				REINT."Numero",
				REINT."FolioCartera",
				REINT."IdFondo",
				REINT."Nemo",
				REINT."RutEmisor",
				REINT."RutParticipe",
				REINT."RutFondo" ,
				REINT."RunFondo",
				REINT."FechaOperacion",
				REINT."FechaRescate",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."Cantidad" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."Cantidad" END AS "Cantidad",
				CASE WHEN rein_ser_fon_des."Porcentaje" <> 0 THEN REINT."CostoUf" * (rein_ser_fon_des."Porcentaje"/100)
					 ELSE REINT."CostoUf" END AS "CostoUf",
				REINT."IdPortafolio",
				REINT."IdCliente",
				REINT."TipoDeclarante",
				REINT."IdEmpresa",
				REINT."IdSerieFondoDestino",
				REINT."IdSolicitudAporteRescate"
			FROM 
				(SELECT
					sol."IdEmpresa" AS "IdEmpresa"
					,sol."IdSolicitudAporteRescate" AS "Numero" --C0
					,car."FolioCartera" AS "FolioCartera"
					,sol."IdSerieFondo" AS "IdFondo"
					,sol."IdSerieFondo" AS "Nemo"
					,'' AS "IdEmisor"
					,TRIM(per."DocumentoIdentidad") AS "RutEmisor" --C1
					,TRIM(cli."DocumentoIdentidad") AS "RutParticipe" --C3
					,CASE WHEN LENGTH (fdo."CodigoSVS")>11 THEN '' ELSE fdo."CodigoSVS" END AS "RutFondo" --C23
					,CASE WHEN LENGTH (fdo."CodigoSVS")>11 THEN '' ELSE fdo."CodigoSVS" END AS "RunFondo" --C4
					,sol_org."IdTipoMovimientoFondoMutuo"  AS "IdTipoMovimientoFondoMutuoOrigen"
					,sol."IdTipoMovimientoFondoMutuo"  AS "IdTipoMovimientoFondoMutuoDestino"
					,sol."FechaValorCuota" AS "FechaOperacion"
					,'1900-01-01' AS "FechaInversion" --C6
					,0 AS "MontoNominalInversionActualizado"--C7
					,COALESCE(ROUND( (car."EGRCostoInversionEnFactorAjuste" * uf_mov."Valor" ),0),0) AS "MontoHistorico"
					,sol."FechaValorCuota" AS "FechaRescate" --C8
					,ROUND(car."EGRMonto",0) AS "MontoNominalRescate" --C9
					,sol."FechaValorCuota"
					,
					       (CASE
					       WHEN sol."IdMoneda" = 'CLP' THEN
					                    (CASE
					                    WHEN COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) > 0 THEN
					                    COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ) * (IPCACTR."Valor"/IPCINVR."Valor"),0),0)
					                    ELSE 0
					                    END)
					       ELSE
					                    (CASE
					                    WHEN COALESCE(ROUND(((((((car."EGRCantidad" * car."EGRPrecio" * PAR."Valor") ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) > 0 THEN
					                    COALESCE(ROUND(((((((car."EGRCantidad" * car."EGRPrecio" * PAR."Valor") ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ) * (IPCACTR."Valor"/IPCINVR."Valor"),0),0)
					                    ELSE 0
					                    END)
					       END)
					 AS "MayorValorActualizado" --C10
					,
					       (CASE
					       WHEN sol."IdMoneda" = 'CLP' THEN
					                    (CASE
					                    WHEN COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) < 0 THEN
					                    COALESCE(ROUND((((((car."EGRMonto" ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ) * (IPCACTR."Valor"/IPCINVR."Valor"),0),0)*-1
					                    ELSE 0
					                    END)
					       ELSE
					                    (CASE
					                    WHEN COALESCE(ROUND(((((((car."EGRCantidad" * car."EGRPrecio" * PAR."Valor") ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ),0),0) < 0 THEN
					                    COALESCE(ROUND(((((((car."EGRCantidad" * car."EGRPrecio" * PAR."Valor") ))/ uf_mov."Valor") - (car."EGRCostoInversionEnFactorAjuste" )) * uf_mov."Valor" ) * (IPCACTR."Valor"/IPCINVR."Valor"),0),0)*-1
					                    ELSE 0
					                    END)
					       END)
					 AS "MenorValorActualizado" --C12
					,ROUND(car."EGRMonto",0) AS "MontoReinvertido" --C14
					,0 AS "NumeroCertificado"
					,ROUND(car."EGRCostoInversionEnFactorAjuste",7) AS "CostoUf"
					,car."EGRCantidad" AS "Cantidad"
					,sol."IdCliente" AS "IdCliente"
					,sol."IdPortafolio" AS "IdPortafolio"
					,PARAMETRO."Valor" AS "TipoDeclarante"
					,sol."IdSolicitudAporteRescate" AS "IdSolicitudAporteRescate"
					,ser_fdo_des."IdSerieFondo" AS "IdSerieFondoDestino"
				FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
					INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" car ON (sol."IdSolicitudAporteRescate"= car."EGRIdOperacion" AND car."IdClasificacionTipoInstrumento" = 'FM' 
						AND car."IdEstado" IN ('LIQ','MOD','RPA'))
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol_org ON (car."FolioComprobante" = sol_org."IdSolicitudAporteRescate") --Origen de cartera
					LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= sol."FechaValorCuota" ) AND uf_mov."IdParametroFinanciero" = 'UF'
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo ON (ser_fdo."IdSerieFondo" = sol."IdSerieFondo")
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo ON (fdo."IdFondo" = ser_fdo."IdFondo")
					LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo_des ON (ser_fdo_des."IdSerieFondo" = CASE WHEN sol."IdSerieFondoDestino" = '' THEN (SELECT max("IdSerieFondoDestino") FROM "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" WHERE "IdSolicitudAporteRescate" = sol."IdSolicitudAporteRescate" AND "IdRegimenTributario" IN ('18T','18Q')) ELSE sol."IdSerieFondoDestino" END) --Destino
					LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARAMETRO ON (PARAMETRO."CodigoParametro" = 'TIPO_DECLARANTE_DJ1894' AND PARAMETRO."IdEmpresa" = sol."IdEmpresa")
					LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARAMT ON (PARAMT."CodigoParametro" = 'OMITIR_FONDOS_0')
					LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARM ON (PARM."CodigoParametro" = 'OMITIR_MONTOS_0')
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.RegimenSerieDeFondo" reg_ser_des ON (ser_fdo_des."IdSerieFondo" =reg_ser_des."IdSerieFondo" AND reg_ser_des."IdRegimenFondoMutuo" IN ('18T','18Q','NINGUNO')) 
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = sol."IdCliente"
					INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio"= sol."IdPortafolio"
					INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" cons_fdo ON cons_fdo."IdSerieFondo" = sol."IdSerieFondo"
					INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per ON per."IdPersona" = cons_fdo."IdPersona"
					--LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."Nemotecnico") = trim(fdo."IdFondo")
					--LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."Nemotecnico") = TRIM(ins."Nemotecnico")
					LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCINVR ON (subsTRIng(ADD_MONTHS (TO_DATE (COALESCE(sol."FechaValorCuota",'1900-01-01'), 'YYYY-MM-DD'), -1),0,7) = IPCINVR."AgnoMes")
					AND IPCINVR."IdParametroFinanciero" = 'IPC'
					LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCACTR ON (SUBSTRING(COALESCE(sol."FechaValorCuota",'1900-01-01'),0,4) || '-11' = IPCACTR."AgnoMes")
					AND IPCACTR."IdParametroFinanciero" = 'IPC'
					LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" PAR ON car."FechaOperacion" = PAR."Fecha" AND PAR."IdParametroFinanciero" = 'DO'
				WHERE sol."IdTipoMovimientoFondoMutuo" IN ('RESNORM','RESREINV','RESCDF')
					AND sol."IdRegimenTributario" IN ('18Q','ART108') AND por."Proposito" NOT IN ('REGAPV')
					AND sol."IdSerieFondo" LIKE CASE WHEN PARAMT."Valor" = 'S' THEN '0%' ELSE sol."IdSerieFondo" END -- Si el parametro esta en S entonces habilita filtro LIKE "Solo para SURA debe estar en S y en Ambiente Desarrollo"
					AND ROUND(car."EGRMonto",0) NOT LIKE CASE WHEN PARM."Valor" = 'S' AND sol."FechaValorCuota" >= '2020-01-01' THEN '0%' ELSE '' END -- MontoNominalRescate "Si el parametro esta en S entonces OMITE montos en 0"
					AND sol."Estado" = 'CRT'
					AND fdo."FondoExtranjero" !='S'
					AND reg_ser_des."IdEstado" = 'ACT'
				) AS REINT
			LEFT JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" AS rein_ser_fon_des ON REINT."Numero"=rein_ser_fon_des."IdSolicitudAporteRescate"   -- PMM
			LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo_des ON (ser_fdo_des."IdSerieFondo" = rein_ser_fon_des."IdSerieFondoDestino")
			LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo_des ON (fdo_des."IdFondo" = ser_fdo_des."IdFondo") --Destino
			LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi_rein ON (emi_rein."IdEmisor" = fdo_des."IdEmisor") --Emisor destino
			LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" Hom ON Hom."ValorOrigen" = REINT."IdTipoMovimientoFondoMutuoDestino" || '-' || COALESCE (fdo_des."FondoExtranjero",'N') || '-' || REINT."IdTipoMovimientoFondoMutuoOrigen" AND Hom."Version" = left(REINT."FechaValorCuota",4)
			) AS OPER
		) AS REG
	GROUP BY REG."IdEmpresa",REG."IdSolicitudAporteRescate",REG."IdSerieFondoDestino",REG."RutAdminitradoraFondoReceptor",REG."TipoOperacion",REG."IdSolicitud",REG."FolioCartera",REG."IdFondo",REG."Nemo",REG."RutEmisor",REG."RutParticipe",REG."RutFondo" ,REG."RunFondo",REG."FechaOperacion",REG."FechaRescate",REG."IdPortafolio",REG."IdCliente",REG."TipoDeclarante"
	)

UNION ALL

/**************************** APORTES ************************/ 
SELECT 

	 sol."IdEmpresa"																								AS "IdEmpresa"
	,sol."IdSolicitudAporteRescate"																					AS "Numero"							--C0
	,car."FolioCartera"																								AS "FolioCartera"
	,sol."IdSerieFondo"																									AS "IdFondo"
	,sol."IdSerieFondo"																								AS "Nemo"
	,''																												AS "IdEmisor"
	,TRIM(per."DocumentoIdentidad") 																				AS "RutEmisor"						--C1
	,TRIM(cli."DocumentoIdentidad")																					AS "RutParticipe"					--C3
	--,COALESCE(inst."DocumentoIdentidadFondo",'') 																							AS "RutFondo"					--C23
	--,COALESCE(inst."DocumentoIdentidadFondo",'')																							AS "RunFondo"					--C4
	,CASE WHEN LENGTH (fdo."CodigoSVS")>11 THEN '' ELSE fdo."CodigoSVS" END											AS "RutFondo"						--C23
	,CASE WHEN LENGTH (fdo."CodigoSVS")>11 THEN '' ELSE fdo."CodigoSVS" END											AS "RunFondo"						--C4
	,CASE 
	  	WHEN sol."IdTipoMovimientoFondoMutuo" IN ('APONORM','APOREPRO','APOOVER') THEN 1
	  	WHEN sol."IdTipoMovimientoFondoMutuo" IN ('APOREINV','APOCDF') THEN
	 		 CASE WHEN sol."FechaValorCuota" <= '2001-04-19' THEN 2
	   		 ELSE 3
	   		 END
	    ELSE 1	
	END																												AS "TipoOperacion"					--C5
	,sol."FechaValorCuota"																							AS "FechaOperacion"	
	,sol."FechaValorCuota"																							AS "FechaInversion"					--C6
	
	,ROUND(sol."MontoMonedaSistema",0)																				AS "MontoNominalInversionActualizado"
	,0																												AS "MontoHistorico"
	,'1900-01-01'																									AS "FechaRescate"					--C8
	,0																												AS "MontoNominalRescate"
	,0																												AS "MayorValorActualizado"			--C10
	,0																												AS "MenorValorActualizado"			--C12
	,''																												AS "RutAdminitradoraFondoReceptor"
	,0																												AS "MontoReinvertido"				--C14
	
	,0																												AS "NumeroCertificado"				 
	,sol."CostoUF"																									AS "CostoUf"
	,sol."NumeroCuotas" 																							AS "Cantidad"
	,sol."IdCliente"																								AS "IdCliente"
	,sol."IdPortafolio"																								AS "IdPortafolio"
	,PARAMETRO."Valor"																								AS "TipoDeclarante"
	,sol."IdSolicitudAporteRescate"																					AS "IdSolicitudAporteRescate"
	,sf_destino."IdSerieFondoDestino"																				AS "IdSerieFondoDestino"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" sol
		---Cartera generada para calculo del monti historico
		INNER JOIN (          
					SELECT  car."IdOperacion", max(car."FolioCartera") AS "FolioCartera"
					FROM  "BSCL.Custodia.BD::CUS.Cartera" car
					WHERE car."IdClasificacionTipoInstrumento" = 'FM'
					--AND car."EGRIdOperacion"!=0
					--AND car."IdEstado" IN ('LIQ','MOD','RPA')
					GROUP BY car."IdOperacion"
					) AS car
			ON (sol."IdSolicitudAporteRescate"= car."IdOperacion")	
			
		LEFT OUTER JOIN "BSCL.Plataforma.BD.Vistas::MER.ConsultarEmisor" emi_rein  ON (emi_rein."IdEmisor" = sol."IdEmisorDestino")		--Emisor destino
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.ReinversionSerieFondoDestino" sf_destino ON sf_destino."IdSolicitudAporteRescate" = sol."IdSolicitudAporteRescate"  -- IdSolicitud
		LEFT OUTER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" uf_mov ON (uf_mov."Fecha"= sol."FechaValorCuota" )  AND uf_mov."IdParametroFinanciero" = 'UF'
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo ON (ser_fdo."IdSerieFondo" = sol."IdSerieFondo" )
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS fdo ON (fdo."IdFondo" = ser_fdo."IdFondo" )
		LEFT OUTER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS ser_fdo_des ON (ser_fdo_des."IdSerieFondo" = CASE WHEN sol."IdSerieFondoDestino" = '' THEN sf_destino."IdSerieFondoDestino" ELSE sol."IdSerieFondoDestino" END)  --Fondo Destino
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" cli ON cli."IdCliente" = sol."IdCliente" 
		INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" por ON por."IdPortafolio"= sol."IdPortafolio"
		INNER JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarFondo" cons_fdo ON cons_fdo."IdSerieFondo" = sol."IdSerieFondo"
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per ON per."IdPersona" = cons_fdo."IdPersona"
		--LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ins ON trim(ins."Nemotecnico") = trim(fdo."IdFondo")
		--LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" inst ON TRIM(inst."Nemotecnico") = TRIM(ins."Nemotecnico")
		LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARAMETRO ON (PARAMETRO."CodigoParametro" = 'TIPO_DECLARANTE_DJ1894' AND PARAMETRO."IdEmpresa" = sol."IdEmpresa")
		LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARAMT ON (PARAMT."CodigoParametro" = 'OMITIR_FONDOS_0')
		LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.Configuracion" PARM ON (PARM."CodigoParametro" = 'OMITIR_MONTOS_0')
		WHERE 	
				sol."IdTipoMovimientoFondoMutuo" IN  ('APONORM','APOREINV','APOCDF','APOOVER')
		AND 	sol."IdRegimenTributario" IN ('18Q','ART108') AND por."Proposito" NOT IN ('REGAPV')
		AND     sol."IdSerieFondo" LIKE CASE WHEN PARAMT."Valor" = 'S' THEN '0%' ELSE sol."IdSerieFondo" END	-- Si el parametro esta en S entonces habilita filtro LIKE "Solo para SURA debe estar en S y en Ambiente Desarrollo"
		AND     ROUND(sol."MontoMonedaSistema",0) NOT LIKE CASE WHEN PARM."Valor" = 'S' AND sol."FechaValorCuota" >= '2020-01-01' THEN '0%' ELSE '' END	-- MontoNominalRescate "Si el parametro esta en S entonces OMITE montos en 0"
		AND 	sol."Estado" = 'CRT'
		AND 	fdo."FondoExtranjero" !='S';
		--AND 	fdo."IdMoneda" ='CLP';

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::TRI.DJ1894.PreOrigen" TO bcs_desarrollador WITH GRANT OPTION;