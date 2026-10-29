SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR1720.ConsultarCuadratura";

CREATE PROCEDURE
--
--  Proposito: Servicio para consultar informaciÃ³n de cuadratura custodia para la SVS.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17/04/2017

 "BSCL.Normativa.BD.Servicios::CIR1720.ConsultarCuadratura" (
 	
 	--VARIABLES DE ENTRADA OBLIGATORIAS
 	IN p_IdEmpresa			BIGINT,
 	
 	--VARIABLES DEL PROCESO
	IN p_FechaOperacionDesde			NVARCHAR(10) 	DEFAULT '',
	IN p_FechaOperacionHasta			NVARCHAR(10) 	DEFAULT '',
	IN p_IdCliente 						BIGINT 			DEFAULT '0',
	IN p_Nemotecnico 					NVARCHAR(40)	DEFAULT '',
	
	IN p_IdTrabajoParametros			BIGINT DEFAULT 0, --Se asumen valores por defecto en los parametros
	
	-- VARIABLES DE SALIDA
	OUT p_out_IdInfoTranOfic				BIGINT 
 )
 
 	LANGUAGE SQLSCRIPT
 	SQL SECURITY INVOKER
 	DEFAULT SCHEMA "BSCL"
 AS
 	BEGIN
 		--------------------
		--Declare Variables
		--------------------
 		DECLARE v_IdNormativa			NVARCHAR(15):= 'CIR1720';
 		DECLARE v_IdTipoDocumento   	NVARCHAR(10):= 'CIR1720';
 		DECLARE v_FechaOperacionDesde	NVARCHAR(10):= '';
 		DECLARE v_FechaOperacionHasta 	NVARCHAR(10):= '';
 		DECLARE v_IdPersonaCtaPropia	BIGINT:= 0;
 		DECLARE v_Count,v_CountCir,v_IdCircular1720	BIGINT:= 0;
 		DECLARE v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  NVARCHAR(40):= '';
 		DECLARE	v_IdClienteCtaPropia 			 BIGINT:=0;
 		DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
 		DECLARE v_out_error 			NVARCHAR(500);
 		DECLARE v_FechaUltimoDiaMes 	DATE;
 		DECLARE v_FechaDolar		 	DATE;
 		DECLARE v_DiaHabilResultado     NVARCHAR(1);   
 		DECLARE v_IdPaisSistema 		BIGINT;
 		
 		v_FechaOperacionDesde := p_FechaOperacionDesde;
  		v_FechaOperacionHasta := p_FechaOperacionHasta;  	
  		
		--INICIO: DEFINICION GENERAL PARAMETROS MASIVOS  		
  		--PARAMETROS MASIVOS TABLA GLOBAL
		v_ParamNemos			=SELECT	'' 	AS "Nemo" 			FROM DUMMY WHERE 1=2;
		v_ParamClasificacion	=SELECT	'' 	AS "Clasificacion"	FROM DUMMY WHERE 1=2;
		
		----Obtener parametros Tabla Global
		SELECT  COUNt(1) INTO v_Count FROM "BSCL.Normativa.BD::GBL.Parametros" 
		WHERE   "IdInterno"=p_IdTrabajoParametros;
		
		IF v_Count > 0 THEN
		
			v_GBLParametros=SELECT "IdInterno","Tipo","NombreParametro" AS "TipoParametro","Valor"   FROM "BSCL.Normativa.BD::GBL.Parametros" WHERE  "IdInterno"=p_IdTrabajoParametros;
			
			v_ParamClasificacion	=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
			v_ParamTipoInstrumento	=SELECT	NULL AS "TipoInstrumento"	 FROM  DUMMY;
			v_ParamNemos			=SELECT	NULL AS "Nemo"	 		 FROM  DUMMY;
		
		ELSE
			---valores por defecto
			v_ParamNemos			=SELECT	NULL AS "Nemo"	 		 FROM  DUMMY;
			v_ParamTipoInstrumento	=SELECT	NULL AS "TipoInstrumento"	 FROM  DUMMY;
			v_ParamClasificacion	=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
		
		END IF;
		--FIN PARAMETROS MASIVOS TABLA GLOBAL

		---DEFINIR: TABLA DE BUSQUEDA
			IF p_Nemotecnico <>'' THEN --
					v_ParamNemos=SELECT	p_Nemotecnico AS "Nemo"
							FROM  DUMMY
							UNION ALL SELECT "Nemo" FROM :v_ParamNemos WHERE "Nemo" IS NOT NULL;
			ELSE				
					IF v_TODOS_NEMO	='S' THEN
							v_ParamNemos=SELECT	NULL AS "Nemo"	 FROM  DUMMY;
					END IF;			
			END IF;
			
			IF v_TODOS_CLAINS	='S' THEN
				--v_ParamClasificacion=SELECT	NULL AS "Clasificacion"	 FROM  DUMMY;
				--para este tipo documento se acota el todos solo para algunos mercados
				v_ParamClasificacion=  SELECT	'RV' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'RF' AS "Clasificacion"	 FROM  DUMMY
							UNION ALL  SELECT	'IF' AS "Clasificacion"	 FROM  DUMMY							
							UNION ALL  SELECT	'SP' AS "Clasificacion"	 FROM  DUMMY;														
			END IF;	
			SELECT COUNT(1) INTO v_Count FROM :v_GBLParametros	WHERE "TipoParametro"='CLAINS';
			IF v_Count > 0 THEN
				v_ParamClasificacion =  SELECT "Valor" AS "Clasificacion" FROM :v_GBLParametros	WHERE "TipoParametro"='CLAINS';
			END IF;			
			SELECT COUNT(1) INTO v_Count FROM :v_GBLParametros	WHERE "TipoParametro"='TIPINS';
			IF v_Count > 0 THEN
				v_ParamTipoInstrumento =  SELECT "Valor" AS "TipoInstrumento" FROM :v_GBLParametros	WHERE "TipoParametro"='TIPINS';
			END IF;		
			
			
			
			
		---FIN DEFINIR: TABLA DE BUSQUEDA

		--TERMINO: DEFINICION GENERAL PARAMETROS MASIVOS  		

	


		
		SELECT TOP 1 "Esquema" INTO v_Esquema FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
		WHERE "IdTipoDocumento" = v_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaOperacionDesde='' OR p_FechaOperacionDesde='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaOperacionDesde, 'DD-MM-YYYY');
		END IF;	
		
	  	SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:v_IdTipoDocumento,:v_FechaVersion);
  		
		
					
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodiaCircular1720" OPE
		INNER JOIN :v_ParamNemos    		NEM ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
		INNER JOIN :v_ParamClasificacion    CLA ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL)
		INNER JOIN :v_ParamTipoInstrumento  TIP ON (OPE."IdTipoInstrumento"=TIP."TipoInstrumento" OR TIP."TipoInstrumento" IS NULL)
		WHERE OPE."FechaCuadraturaCustodia"=:v_FechaOperacionHasta;

		SELECT COUNT(1) INTO v_CountCir FROM "BSCL.Custodia.BD::CUS.CuadraturaCircular1720" CuaCir1720
		INNER JOIN "BSCL.Normativa.BD::PRO.CuadraturaCustodiaCircular1720" OPE  ON OPE."FechaCuadraturaCustodia" = CuaCir1720."FechaCuadratura" 
																				AND OPE."Nemotecnico"= CuaCir1720."Nemotecnico"
																				AND OPE."IdClasificacionTipoInstrumento" = CuaCir1720."IdClasificacionTipoInstrumento"
		WHERE CuaCir1720."FechaCuadratura"=:v_FechaOperacionHasta;
 				 
		IF v_Count > 0  AND v_CountCir > 0 THEN	
		
							
					v_tablaCustodia= SELECT OPE.* 
					                 FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodiaCircular1720" OPE
					                   INNER JOIN :v_ParamNemos	NEM 
					                      ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
					                   INNER JOIN :v_ParamClasificacion CLA 
					                      ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL)
					                   INNER JOIN :v_ParamTipoInstrumento  TIP 
					                      ON (OPE."IdInstrumento"=TIP."TipoInstrumento" OR TIP."TipoInstrumento" IS NULL)
					                 WHERE OPE."FechaCuadraturaCustodia"=:v_FechaOperacionHasta;
					
					SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdCircular1720".NEXTVAL INTO v_IdCircular1720 FROM DUMMY;
					
					SELECT ADD_DAYS(ADD_MONTHS(ADD_DAYS(v_FechaOperacionHasta,-EXTRACT(DAY FROM v_FechaOperacionHasta) + 1),1),-1) INTO v_FechaUltimoDiaMes FROM DUMMY;
					
					SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
					
					CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(v_FechaUltimoDiaMes,v_IdPaisSistema,v_DiaHabilResultado);
					
					IF v_DiaHabilResultado = 'N' THEN
						CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior"
						 (
							 p_Fecha      		 => v_FechaUltimoDiaMes
							,p_NumeroDias 		 => 1
							,p_EsHabil    		 => 'S'
							,p_IdPais     		 => v_IdPaisSistema
							,p_out_FechaAnterior => v_FechaDolar
						 );
						 ELSE 
						 	v_FechaDolar = v_FechaUltimoDiaMes;
					 END IF;
			 
					
					--Acumula datos para insert
					ConsultarCuadCircular1720 = WITH ConsultarCircular AS (
					                                                        SELECT v_IdCircular1720 AS "IdInterno",
					                                                               tabCustodia."FechaCuadraturaCustodia",
					                                                               tabCustodia."Nemotecnico",
					                                                               CASE 
					                                                               	 WHEN  ConsIns."IdClasificacionTipoInstrumento" = 'FM' THEN COALESCE(SerFondo."IdSerieFondo",'')
					                                                               	 ELSE  tabCustodia."Nemotecnico"
					                                                               END  AS "NemoCMF",
					                                                               COALESCE (DatCir1720."NombreEmisor",
																					CASE 
																						WHEN tabCustodia."IdClasificacionTipoInstrumento" = 'IF' THEN 
																							COALESCE(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(CuaCir1720."IdEmisor",'Ñ','#'),'ñ','#'), 'Á','A'),'á','a'),'É','E'),'é','e'),'Í','I'),'í','i'),'Ó','O'),'ó','o'),'Ú','U'),'ú','u'), TO_VARCHAR(''))
																						ELSE 
																							COALESCE(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(ConsIns."RazonSocial",'Ñ','#'),'ñ','#'), 'Á','A'),'á','a'),'É','E'),'é','e'),'Í','I'),'í','i'),'Ó','O'),'ó','o'),'Ú','U'),'ú','u'), TO_VARCHAR('')) 
																					END )	
																					AS "NombreEmisor",
					                                                               COALESCE 
																						(DatCir1720."TipoInstrumento",
																							CASE 						  
																								WHEN Fondo."FondoExtranjero" = 'S' AND ConsIns."IdTipoInstrumento" = 'FFM' THEN 'CFME'
																								WHEN Fondo."FondoExtranjero" = 'N' AND ConsIns."IdTipoInstrumento" = 'FFM' THEN 'CFM'
																								WHEN tabCustodia."IdTipoInstrumento" = 'ACX' THEN 'ACE'
																								WHEN tabCustodia."IdTipoInstrumento" = 'ETX' THEN 'ETFA'
																								WHEN tabCustodia."IdTipoInstrumento" = 'ACN' THEN 'ACC'
		    																					WHEN tabCustodia."IdTipoInstrumento" = 'BEM' THEN 'BE'
		    																					WHEN tabCustodia."IdTipoInstrumento" = 'BBC' THEN 'BB'
		    																					WHEN tabCustodia."IdTipoInstrumento" = 'ETN' THEN 'ETFC'
		    																					WHEN TabCustodia."IdTipoInstrumento" = 'OSA' THEN 'DPSA'
		    																					WHEN TabCustodia."IdTipoInstrumento" = 'LTH' THEN 'LH'
		    																					WHEN TabCustodia."IdTipoInstrumento" = 'PAG' AND tabCustodia."Nemotecnico" LIKE 'BNPDBC%' THEN 'PDBC'
		    																					WHEN TabCustodia."IdTipoInstrumento" = 'PAG' AND tabCustodia."Nemotecnico" LIKE 'S%' THEN 'EC' 
																								WHEN tabCustodia."IdTipoInstrumento" = 'PAG'  AND LENGTH(SUBSTRING(tabCustodia."Nemotecnico", 7,6))= 6 THEN 
																									CASE 
																										WHEN (CAST (abs (DAYS_BETWEEN (CuaCir1720."FechaCuadratura",CAST(CAST((SUBSTRING(tabCustodia."Nemotecnico", 7,2)+ SUBSTRING(tabCustodia."Nemotecnico", 9,2)*100 + (SUBSTRING(tabCustodia."Nemotecnico", 11,2)+2000)*10000 )AS VARCHAR) AS date))
																										) AS BiGINT) ) > 365 THEN 'DPL'
																									ELSE 'DPC'
																									END 
																								ELSE tabCustodia."IdTipoInstrumento"
																					END      )                                                       AS "TipoInstrumento",	
					                                                               COALESCE(DatCir1720."CantidadUnidadesNominal",(tabCustodia."CantidadCustodia" + tabCustodia."CantidadGarantia"+ CuaCir1720."CantidadCompraPlazo" - CuaCir1720."CantidadVentaPlazo" + CuaCir1720."VentaEnTransito" - CuaCir1720."CompraEnTransito" + CuaCir1720."Diferencia"))  AS "CantidadUnidadesNominal",	
					                                                               COALESCE (DatCir1720."TipoUnidades",
																					CASE 
		    																			WHEN ConsIns."IdClasificacionTipoInstrumento" = 'FM' THEN 'CU'
																					ELSE 'CA' 
																					END     )                                                        AS "TipoUnidades",
					                                                               COALESCE(DatCir1720."Valorizacion",
					                                                                CASE WHEN  ConsIns."IdClasificacionTipoInstrumento" != 'FM' THEN 
					                                                               		ROUND((COALESCE(DatCir1720."CantidadUnidadesNominal",tabCustodia."CantidadCustodia")+  tabCustodia."CantidadGarantia") * COALESCE(PrecioCierreCircular."PrecioCierre",1),0 )
					                                                               		WHEN  Fondo."FondoExtranjero" = 'N'  THEN
					                                                               		ROUND((COALESCE(DatCir1720."CantidadUnidadesNominal",tabCustodia."CantidadCustodia")+  tabCustodia."CantidadGarantia") * COALESCE(CVCH."ValorCuota", 1),0) 
					                                                               		ELSE
					                                                                	ROUND((COALESCE(DatCir1720."CantidadUnidadesNominal",tabCustodia."CantidadCustodia")+  tabCustodia."CantidadGarantia") * COALESCE(CVCH."ValorCuota",1) * COALESCE(PYT."TipoCambioMercado", 1),0)  
					                                                                END	)
					                                                               AS "Valorizacion",
					                                                               COALESCE(DatCir1720."CarteraPropia",CuaCir1720."TotalCustodiaCuentaPropia")		   AS "CarteraPropia",
					                                                               COALESCE(DatCir1720."CustodiaTerceros",(tabCustodia."CantidadCustodia" + tabCustodia."CantidadGarantia"+ CuaCir1720."CantidadCompraPlazo" - CuaCir1720."CantidadVentaPlazo" + CuaCir1720."VentaEnTransito" - CuaCir1720."CompraEnTransito" + CuaCir1720."Diferencia" - CuaCir1720."TotalCustodiaCuentaPropia")) AS "CustodiaTerceros",
					                                                               COALESCE(DatCir1720."CustodiaPropiaDCV",tabCustodia."CantidadDCVCuentaPropia") AS "CustodiaPropiaDCV",
					                                                               COALESCE(DatCir1720."CustodiaTercerosDCV",(tabCustodia."CantidadDCVTerceros" + "CantidadDCVRelacionado"))                   AS "CustodiaTercerosDCV",
					                                                               CASE 
																					WHEN tabCustodia."IdTipoInstrumento" IN ('ACX','ACE','BEE','ADR','CFIE','ETX','ETFA') OR (Fondo."FondoExtranjero" = 'S' AND ConsIns."IdTipoInstrumento" = 'FFM') THEN 0
																				   ELSE 
																						COALESCE(DatCir1720."CustodiaSociedadEmisora",tabCustodia."CantidadEmisores")  
																				   END 																													AS "CustodiaSociedadEmisora",
					                                                               COALESCE(DatCir1720."CustodiaIntermediario",tabCustodia."CantidadIntermediario")   	                                     AS "CustodiaIntermediario",
					                                                               COALESCE(DatCir1720."CustodiaBolsas",(tabCustodia."CantidadBCS"+ tabCustodia."CantidadBEC"+ tabCustodia."CantidadBOVALPO"))              AS "CustodiaBolsas",
					                                                               CASE 
																					WHEN tabCustodia."IdTipoInstrumento" IN ('ACX','ACE','BEE','ADR','CFIE','ETX','ETFA') OR (Fondo."FondoExtranjero" = 'S' AND ConsIns."IdTipoInstrumento" = 'FFM') 
																					THEN COALESCE(DatCir1720."CustodiaOtros",((tabCustodia."CantidadCustodia" + tabCustodia."CantidadGarantia"+ CuaCir1720."CantidadCompraPlazo" - 
																					CuaCir1720."CantidadVentaPlazo" + CuaCir1720."VentaEnTransito" - CuaCir1720."CompraEnTransito" + CuaCir1720."Diferencia") - 
																					( tabCustodia."CantidadDCVCuentaPropia" + tabCustodia."CantidadDCVTerceros" + tabCustodia."CantidadEmisores" + tabCustodia."CantidadIntermediario" + 
																					"CantidadDCVRelacionado" ) -(tabCustodia."CantidadBCS"+ tabCustodia."CantidadBEC"+ tabCustodia."CantidadBOVALPO") + COALESCE(DatCir1720."CustodiaSociedadEmisora",tabCustodia."CantidadEmisores") ))	
																				   ELSE 
																					COALESCE(DatCir1720."CustodiaOtros",((tabCustodia."CantidadCustodia" + tabCustodia."CantidadGarantia"+ CuaCir1720."CantidadCompraPlazo" - 
																					CuaCir1720."CantidadVentaPlazo" + CuaCir1720."VentaEnTransito" - CuaCir1720."CompraEnTransito" + CuaCir1720."Diferencia") - 
																					( tabCustodia."CantidadDCVCuentaPropia" + tabCustodia."CantidadDCVTerceros" + tabCustodia."CantidadEmisores" + tabCustodia."CantidadIntermediario" + 
																					"CantidadDCVRelacionado" ) -(tabCustodia."CantidadBCS"+ tabCustodia."CantidadBEC"+ tabCustodia."CantidadBOVALPO") ))	
																				   END 																		AS "CustodiaOtros",
					                                                               tabCustodia."FechaCuadraturaCustodia"                                   AS "FechaRegistro",
					                                                               tabCustodia."IdCuadratura",
					                                                               CASE
					                                                               	WHEN COALESCE(DatCir1720."Habilitado",'S') = 'N' THEN 'DES'
					                                                                 WHEN COALESCE(DatCir1720."IdCuadratura",0) > 0 THEN 'MOD' 
					                                                               ELSE 'ORI' 
					                                                               END                                                             AS "Tipo",
					                                                               ''                                                              AS "NemotecnicoCMF",
					                                                               COALESCE(PrecioCierreCircular."PrecioCierre",1)                 AS "PrecioCierre",
					                                                               CASE 
       																				WHEN COALESCE(DatCir1720."Habilitado",'S') = 'S'THEN 'S' 
       																			   ELSE 'N' 
       																			   END 															AS "Habilitado"
					                                                        FROM :v_tablaCustodia as tabCustodia
					                                                          LEFT OUTER JOIN "BSCL.Normativa.BD::NOR.DatosCIR1720" AS DatCir1720
					                                                            ON tabCustodia."IdCuadratura"=DatCir1720."IdCuadratura"
					                                                          INNER JOIN "BSCL.Custodia.BD::CUS.CuadraturaCircular1720" AS CuaCir1720
																				ON tabCustodia."FechaCuadraturaCustodia" = CuaCir1720."FechaCuadratura" AND tabCustodia."Nemotecnico"= CuaCir1720."Nemotecnico" AND tabCustodia."IdClasificacionTipoInstrumento" = CuaCir1720."IdClasificacionTipoInstrumento"
					                                                          LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" ConsIns 
					                                                            ON tabCustodia."Nemotecnico" = ConsIns."Nemotecnico" AND tabCustodia."IdClasificacionTipoInstrumento" = ConsIns."IdClasificacionTipoInstrumento"
					                                                          LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SerFondo 
					                                                            ON SerFondo."IdInstrumento" = ConsIns."IdInstrumento"
					                                                          LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" Fondo
					                                                            ON Fondo."IdFondo" = SerFondo."IdFondo"
					                                                          ---Precio Para la valorizacion
					                                                          LEFT JOIN "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarValorCuotaHistorico" AS  CVCH
					                                                          	ON CVCH."IdInstrumento" = tabCustodia."IdInstrumento" and CVCH."FechaVigencia"=v_fechaultimodiames
					                                                          --Precio Dolar Observado
					                                                          LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" AS PYT 
																				ON (PYT."IdMoneda" = 'DO' AND
																					PYT."Fecha" = v_FechaDolar)
																			  ---Precio Mercado actual (monedad instrumento) x Instrumento  
					                                                          LEFT JOIN (SELECT PrecioCierre."IdInstrumento", PrecioCierre."PrecioCierre", PrecioCierre."Fecha"
					                                                                     FROM (
					                                                                             SELECT row_number() over (partition by BolsaInsPrec."IdInstrumento" order by BolsaInsPrec."IdInstrumento", BolsaInsPrec."Fecha" desc) AS row_num
					                                                                             		,BolsaInsPrec."IdInstrumento"
					                                                                             		,BolsaInsPrec."Fecha"
					                                                                             		,BolsaInsPrec."IdBolsa"
					                                                                             		,BolsaInsPrec."PrecioCierre"
					                                                                             FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" AS BolsaInsPrec
					                                                                             WHERE BolsaInsPrec."IdBolsa" = 'BCS' 
					                                                                               AND TO_DATE(BolsaInsPrec."Fecha") <= TO_DATE(v_FechaOperacionHasta)
					                                                                          ) PrecioCierre
					                                                                     WHERE PrecioCierre.row_num = 1 
					                                                                       AND PrecioCierre."PrecioCierre" > 0 
					                                                                    ) PrecioCierreCircular
					                                                            ON (tabCustodia."IdInstrumento" = PrecioCierreCircular."IdInstrumento")
					                                                        WHERE tabCustodia."Nemotecnico" NOT LIKE '*%'
					                                                     )					                                                  
					                             SELECT  ConsCuadratura."IdInterno",
					                                     ConsCuadratura."FechaCuadraturaCustodia",
					                                     ConsCuadratura."Nemotecnico",
					                                     CASE 
					                                       WHEN ConsCuadratura."TipoInstrumento" = 'CFME' THEN  'SN' || CASE
					                                                                                                      WHEN COALESCE(NRO,-1) = 1 THEN '' 
					                                                                                                    ELSE TRIM(NRO-1)
					                                                                                                    END
					                                     ELSE ConsCuadratura."NemoCMF"
					                                     END AS "NemotecnicoCMF",
					                                     ConsCuadratura."NombreEmisor",	
					                                     ConsCuadratura."TipoInstrumento",
					                                     ConsCuadratura."CantidadUnidadesNominal",
					                                     ConsCuadratura."TipoUnidades",
					                                     ConsCuadratura."Valorizacion",
					                                     ConsCuadratura."CarteraPropia",
					                                     ConsCuadratura."CustodiaTerceros",
					                                     ConsCuadratura."CustodiaPropiaDCV",
					                                     ConsCuadratura."CustodiaTercerosDCV", 
					                                     ConsCuadratura."CustodiaSociedadEmisora",
					                                     ConsCuadratura."CustodiaIntermediario",
					                                     ConsCuadratura."CustodiaBolsas",
					                                     ConsCuadratura."CustodiaOtros",
					                                     ConsCuadratura."FechaRegistro",
					                                     ConsCuadratura."IdCuadratura",
					                                     ConsCuadratura."Tipo",
					                                     ConsCuadratura."PrecioCierre",
					                                     ConsCuadratura."Habilitado"
					                             FROM ConsultarCircular AS ConsCuadratura
					                               LEFT JOIN ( SELECT "Nemotecnico", (ROW_NUMBER() OVER ()) AS "NRO" 
					                                           FROM ConsultarCircular
					                                           WHERE "TipoInstrumento" = 'CFME' ) AS ConsCuadraturaCMF
					                                 ON ConsCuadratura."Nemotecnico" = ConsCuadraturaCMF."Nemotecnico";   
					    
					    
					    
					
					INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1720" (
						"IdInterno"	,
						"FechaCuadraturaCustodia",
						"Nemotecnico",
						"NombreEmisor",
						"TipoInstrumento",
						"CantidadUnidadesNominal",
						"TipoUnidades",
						"Valorizacion",
						"CarteraPropia",
						"CustodiaTerceros",
						"CustodiaPropiaDCV",
						"CustodiaTercerosDCV",
						"CustodiaSociedadEmisora",
						"CustodiaIntermediario",
						"CustodiaBolsas",
						"CustodiaOtros",
						"FechaRegistro",
						"IdCuadratura",
						"Tipo",
						"NemotecnicoCMF",
						"PrecioCierre",
						"Habilitado"
					)					
					SELECT "IdInterno",
					       "FechaCuadraturaCustodia",
					       "Nemotecnico",
					       "NombreEmisor",	
					       "TipoInstrumento",
					       "CantidadUnidadesNominal",
					       "TipoUnidades",
					       "Valorizacion",
					       "CarteraPropia",
					       "CustodiaTerceros",
					       "CustodiaPropiaDCV",
					       "CustodiaTercerosDCV", 
					       "CustodiaSociedadEmisora",
					       "CustodiaIntermediario",
					       "CustodiaBolsas",
					       "CustodiaOtros",
					       "FechaRegistro",
					       "IdCuadratura",
					       "Tipo",
					       "NemotecnicoCMF",
					       "PrecioCierre",
					       "Habilitado"
					FROM :ConsultarCuadCircular1720;
					
					    
								    
				select * from "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1720" WHERE "IdInterno"=v_IdCircular1720;
		
		
		END IF;						
		
		p_out_IdInfoTranOfic =v_IdCircular1720;
		
			 
		

 	END
