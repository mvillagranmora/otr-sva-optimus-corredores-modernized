SET SCHEMA "BSCL";

						
			 update INS
			  set    INS."RazonSocial"   = FU."RAZON_SOCIAL" 
			  	  	,INS."FechaVencimiento" = FU."FEC_VCTO" 				
			  		,INS."IdTipoInstrumento" = CASE WHEN CONCAT('F',FU."TIPO_INSTRU") = 'FD' THEN 'FDO'
										       WHEN CONCAT('F',FU."TIPO_INSTRU")  = 'FA' THEN 'FIC'
										       WHEN CONCAT('F',FU."TIPO_INSTRU")  = 'FI' THEN 'FIP'
										       WHEN CONCAT('F',FU."TIPO_INSTRU")  = 'FB' THEN 'FBC'
										       WHEN CONCAT('F',FU."TIPO_INSTRU")  = 'FC' THEN 'FUF' 
 			   								END 
			  		,INS."DecimalesPrecio" = FU."NUM_DEC_PRE"
			  		,INS."Tamano" =  FU."LOTE_PADRON"					
			  		,INS."DiasAlVencimiento" = FU."DIAS_AL_VCTO"
			  		,INS."FechaFijaCierre" = FU."FEC_FIJ_CIE"
			  		,INS."FechaInicioVigencia" = FU."FEC_INI_VIG"
			  		,INS."HoraFijaCierre" = to_time(substring(to_char(FU."HORA_FIJ_CIE"),12,8))
			  		,INS."IdEstado" = CASE WHEN ("ESTADO"= '1') THEN 'ACT' ELSE 'INA' END--interpretar el estado (listo)
 			 from "BSCL.Plataforma.BD::INS.Instrumento" INS
 			 inner join "BSCL_VIRTUAL"."TBFUMI" as FU
 			 	on FU."NEMO" = INS."Nemotecnico";
 			 	

			SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" IN('FDO','FIC','FIP','FBC','FUF')
			group by "IdEstado" order by 1; 											

			--------------------------------------------
			-- ACTUALIZAR ACCIONES NACIONALES
			--------------------------------------------						
				-- ACCIONES NACIONALES : Actualizar Existentes para no perder integridad
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=   ifnull(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						--, "IdEmisor" = ifnull(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'CLP'
						, "IdTipoInstrumento" = 'ACN'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = ifnull(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = 'A' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 0 -- 0 $ 1 USD
				--and RV."PAIS_EMISOR" = ''
				and RV."BOLSA" = ''
				and INS."Nemotecnico" = AC."NEMO"
				AND TRIM(INS."IdTipoInstrumento") != 'OSA'
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";

				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACN'-- IN('FDO','FIC','FIP','FBC','FUF')
				group by "IdEstado" order by 1;
			
							
				-- OSAs
				UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=   ifnull(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						--, "IdEmisor" = ifnull(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'CLP'
						, "IdTipoInstrumento" = 'ACN'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = ifnull(AC."NUM_DEC_PRE",0)
				--select INS."Nemotecnico"
				from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" 
				AND TRIM(INS."IdTipoInstrumento") = 'OSA'				
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
					SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACN'
				group by "IdEstado" order by 1;
				
	
			
				-- ACCIONES NACIONALES : Actualizar Existentes para no perder integridad
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=   ifnull(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						--, "IdEmisor" = ifnull(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'CLP'
						, "IdTipoInstrumento" = 'ACX'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = ifnull(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = 'A' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 0 -- 0 $ 1 USD
				--and RV."PAIS_EMISOR" = ''
				and RV."BOLSA" ='O' and RV."BOLSA_EX" = 'BCS'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACX'
				group by "IdEstado" order by 1;

				-- ACCIONES MILA : Actualizar Existentes para no perder integridad
				UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=   ifnull(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = ifnull(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'		
						, "IdMoneda" = (CASE WHEN ((RV."MONEDA" = 9) OR (RV."MONEDA" = 1)) THEN 'USD' ELSE 'CLP' END) --- VERIFICAR CONVERSION
						, "IdTipoInstrumento" = 'MIL'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = ifnull(AC."NUM_DEC_PRE",0)
				from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = 'A' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
                --and RV."MONEDA" = 0 -- 0 $ 1 USD
                --and RV."PAIS_EMISOR" = ''
				and RV."BOLSA" ='O' and RV."BOLSA_EX" <> 'BCS'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'MIL'
				group by "IdEstado" order by 1;
				
			--------------------------------------------
			-- ACTUALIZAR ACCIONES EXTRANJERAS (ADRs)
			--------------------------------------------
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=   ifnull(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = ifnull(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'USD'
						, "IdTipoInstrumento" = 'ACX'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = ifnull(AC."NUM_DEC_PRE",0)

				from  "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = ' ' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 1 -- 0 $ 1 USD
				--and RV."PAIS_EMISOR" <> ''
				and RV."BOLSA" ='O' and RV."BOLSA_EX" = 'BCS'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				

				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ACX'
				group by "IdEstado" order by 1;				

			--------------------------------------------
			-- ACTUALIZAR ETF EXTRANJEROS
			--------------------------------------------						
				---- Actualizar Existentes para no perder integridad
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=  IFNULL(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = IFNULL(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'USD'
						, "IdTipoInstrumento" = 'ETX'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = IFNULL(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = 'E' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 1 -- 0 $ 1 USD
				--and SOC."RUT_ADMIN" = ''
				and RV."BOLSA" = 'O'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
								SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ETX'
				group by "IdEstado" order by 1;

			--------------------------------------------
			-- ACTUALIZAR ETF NACIONAL
			--------------------------------------------						
				---- Actualizar Existentes para no perder integridad
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=  IFNULL(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = IFNULL(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'USD'
						, "IdTipoInstrumento" = 'ETN'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = IFNULL(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBACMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'AC' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = 'E' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 1 -- 0 $ 1 USD
				--and SOC."RUT_ADMIN" <> ''
				and RV."BOLSA" = ''
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
								SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'ETN'
				group by "IdEstado" order by 1;
			--------------------------------------------
			-- ACTUALIZAR CFI EXTRANJERO
			--------------------------------------------						
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=  IFNULL(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = IFNULL(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'USD'
						, "IdTipoInstrumento" = 'CFX'
						,  "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = IFNULL(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBCFMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'CF' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = ' ' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 1 -- 0 $ 1 USD
				--and SOC."RUT_ADMIN" <> ''
				and RV."BOLSA" = 'O'
                and RV."BOLSA_EX" = 'BCS'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
								SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'CFX'
				group by "IdEstado" order by 1;

			--------------------------------------------
			-- ACTUALIZAR CFI EXTRANJERO
			--------------------------------------------						
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set "ISIN"  		=  IFNULL(RV."CODIGO_ISIN",'')
						, "CorteMinimo" = 0
						, "EstaInscrito"='S'
						, "EsDivisible" ='N'
						, "RazonSocial" = AC."RAZON_SOCIAL"
						, "Lista"       = RV."LISTA"
						, "FechaEmision"= AC."FEC_INSCRIP"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor" = IFNULL(EMI."IdEmisor",'')
						, "IdTipoReajuste" = ''
						, "Reajustable" = 'N'
						, "IdMoneda" = 'USD'
						, "IdTipoInstrumento" = 'CFM'
						, "IdEstado" =  CASE WHEN AC."ESTADO" IN (1,3) THEN 'ACT' ELSE 'INA' END --AC."ESTADO"
						, "DecimalesPrecio" = IFNULL(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRVMI" as RV, "BSCL_VIRTUAL"."TBCFMI" as AC
								left outer join "BSCL_VIRTUAL"."TBASSOC" as SOC on (SOC."NEMO_SASA" = AC."NEMO_SASA" and SOC."NEMO" = AC."NEMO")
								--left outer join "BSCL"."TBUTEMIS" as EMI on(SOC."RUT" = EMI."RUT" and SOC."NEMO_SASA" = EMI."EMISOR")
								LEFT OUTER JOIN "BSCL.Mercado.BD::MER.Emisor" as EMI on (SOC."NEMO_SASA" = EMI."IdEmisor")
								--left outer join "BSCL"."TBUTUSUA" as USU on(SOC."RUT" = USU."RUT_CORR" and SOC."NEMO_SASA" = USU."NEMO")
				where RV."MERCADO" = 'CF' and RV."NEMO" = AC."NEMO" AND RV."TIPO_INSTRU" = ' ' --AND AC.FAC_IPSA > 0 --AND COD_IGPA <> ''
				--and RV."MONEDA" = 1 -- 0 $ 1 USD
				--and SOC."RUT_ADMIN" <> ''
				and RV."BOLSA" = ''
                and RV."BOLSA_EX" = 'BCS'
				and INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
				
			SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'CFM'
				group by "IdEstado" order by 1;
			
			--------------------------------------------
			-- ACTUALIZAR RENTA FIJA
			--------------------------------------------						
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
				set 	"FechaVencimiento" = MI."FEC_VCTO"
					, 	"IdEstado" = (case 
									when "ESTADO"= 1 or "ESTADO" = 3 then 'ACT'
									else 'INA'
								 end)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBRFMI" MI
					inner join "BSCL_VIRTUAL"."TBRFSE" SE on MI."FAMILIA" = SE."FAMILIA" and MI."COD_SERIE" = SE."COD_SERIE"
					left join "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" CR on trim(SUBSTR_BEFORE (SE."CLASIF_RIESGO",' ;')) = CR."ClasificacionRiesgo" and CR."IdTipoClasificacionRiesgo" = 'CLRF'
				where 
					INS."Nemotecnico" = MI."NEMO"
					and "BSCL.Plataforma.BD::INS.Instrumento"."IdInstrumento" = INS."IdInstrumento" ;
				
				
				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" IN(				
				'BBC'
											,'BCD'
											,'BCP'
											,'BCU'
											,'BEM'
											,'CER'
											,'LTH'
											,'PRC'
											,'ZER'
											,'BCA'
											,'BCX'
											,'BTP'
											,'BTU'
											,'BUS'
											,'BVL'
											,'COR'
											,'PCX'
											,'PRD'
											,'XER'
											,'BR'
											,'BH')
				group by "IdEstado" order by 1;
											


				insert into "BSCL.Plataforma.BD::INS.Instrumento" 
				("IdInstrumento"	,"Nemotecnico"	,"ISIN"	,"CorteMinimo"	,"EstaInscrito"	,"EsDivisible"	,"RazonSocial"	,"Lista"	,"FechaEmision"	,"FechaVencimiento"	,"IdInstrumentoRelacionado"
				,"IdEmisor"	,"IdTipoReajuste"	,"Reajustable"	,"IdMoneda"	,"IdTipoInstrumento"	,"IdEstado"	,"DecimalesPrecio"
				)
				select		
						"BSCL"."BSCL.Plataforma.BD.Secuencias::INSInstrumento".NEXTVAL as IdInstrumento 
						, AC."NEMO"			 as Nemotecnico
						, '' 				 as ISIN
						, 0					 as CorteMinimo
						, 'S'				 as EstaInscrito
						, 'N'				 as EsDivisible
						, AC."RAZON_SOCIAL"  as RazonSocial
						, ''			 	 as Lista
						, ''				 as FechaEmision
						, '0001-01-01'		 as FechaVencimiento
						, 0					 as IdInstrumentoRelacionado
						, '' 				 as IdEmisor -- LINKEAR ID EMISOR ANTERIORMENTE CREADO
						, ''				AS IdTipoReajuste
						, 'N'				AS Reajustable
						, 'CLP'				AS IdMoneda				--BUSCAR(RV.MONEDA_EX Y OBTENER ID MONEDA 	)
						, 'MON'				AS IdTipoInstrumento	-- Linkear Tipo Instrumentos
						, CASE WHEN (AC."ESTADO" IN(1,3)) THEN 'ACT' ELSE 'INA' END	AS IdEstado
						, AC."NUM_DEC_PRE"	AS DecimalesPrecio
				from "BSCL_VIRTUAL"."TBMOMI" as AC
				where not exists( select 1 from "BSCL.Plataforma.BD::INS.Instrumento" where "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = AC."NEMO");

				
			
			------------ MON
				UPDATE "BSCL.Plataforma.BD::INS.Instrumento"
					set   "RazonSocial" = AC."RAZON_SOCIAL"
						, "FechaVencimiento" = '0001-01-01'
						, "IdInstrumentoRelacionado"= 0
						, "IdEmisor"          = ''
						, "IdTipoReajuste"    = ''
						, "Reajustable"       = 'N'
						, "IdMoneda"          = 'CLP'
						, "IdTipoInstrumento" = 'MON'
						, "IdEstado"          = CASE WHEN (AC."ESTADO" IN(1,3)) THEN 'ACT' ELSE 'INA' END
						, "DecimalesPrecio"   = IFNULL(AC."NUM_DEC_PRE",0)
				--select count(*)
				from "BSCL.Plataforma.BD::INS.Instrumento" INS, "BSCL_VIRTUAL"."TBMOMI" as AC
				where INS."Nemotecnico" = AC."NEMO"
				and "BSCL.Plataforma.BD::INS.Instrumento"."Nemotecnico" = INS."Nemotecnico";
							
							
 				SELECT "IdEstado",COUNT(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "IdTipoInstrumento" = 'CFX'
				group by "IdEstado" order by 1;
							