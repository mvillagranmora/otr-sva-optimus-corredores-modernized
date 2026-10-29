SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::CIR1518.ConsultarCuadratura";
GO
CREATE PROCEDURE
--
--  Proposito: Servicio para consultar información de cuadratura custodia para la BCS.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 28/03/2017

 "BSCL.Normativa.BD.Servicios::CIR1518.ConsultarCuadratura" (
 	
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
 		DECLARE v_IdNormativa			NVARCHAR(15):= 'CIR1518';
 		DECLARE v_IdTipoDocumento   	NVARCHAR(10):= 'CIR1518';
 		DECLARE v_FechaOperacionDesde	NVARCHAR(10):= '';
 		DECLARE v_FechaOperacionHasta 	NVARCHAR(10):= '';
 		DECLARE v_IdPersonaCtaPropia	BIGINT:= 0;
 		DECLARE v_Count,v_IdCircular1518	BIGINT:= 0;
 		DECLARE v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  NVARCHAR(40):= '';
 		DECLARE	v_IdClienteCtaPropia 			 BIGINT:=0;
 		DECLARE v_Esquema,v_Version, v_FechaVersion			 NVARCHAR(10) DEFAULT '';
 		DECLARE v_out_error 			NVARCHAR(500);
 		DECLARE v_FechaAnterior			NVARCHAR(10);
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
			
			/*
			--Opcion Todos
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='TODOS_CLI'		THEN "Valor" ELSE NULL END AS "TODOS_CLI"
				, CASE WHEN SOL."TipoParametro"='TODOS_NEMO'	THEN "Valor" ELSE NULL END AS "TODOS_NEMO"
				, CASE WHEN SOL."TipoParametro"='TODOS_CLAINS'	THEN "Valor" ELSE NULL END AS "TODOS_CLAINS"
			FROM :v_GBLParametros SOL WHERE  SOL."TipoParametro" IN ('TODOS_CLI','TODOS_NEMO','TODOS_CLAINS');
		 	
			v_DatosAgrupado =SELECT  
					  MAX(SOL."TODOS_CLI")		AS "TODOS_CLI"
					, MAX(SOL."TODOS_NEMO")		AS "TODOS_NEMO"
					, MAX(SOL."TODOS_CLAINS")	AS "TODOS_CLAINS"															
					FROM :v_Datos SOL;
			SELECT TOP 1 "TODOS_CLI","TODOS_NEMO","TODOS_CLAINS" INTO v_TODOS_CLI,v_TODOS_NEMO,v_TODOS_CLAINS  FROM :v_DatosAgrupado;
			*/
			--Masivos
			--v_ParamNemos 			=  SELECT "Valor" AS "Nemo" 		 FROM :v_GBLParametros	WHERE "TipoParametro"='NEMO_MAS';
			
			
			
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
  		
		
					
		SELECT COUNT(1) INTO v_Count FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodia" OPE
		INNER JOIN :v_ParamNemos    		NEM ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
		INNER JOIN :v_ParamClasificacion    CLA ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL)
		INNER JOIN :v_ParamTipoInstrumento  TIP ON (OPE."IdTipoInstrumento"=TIP."TipoInstrumento" OR TIP."TipoInstrumento" IS NULL)
		WHERE OPE."FechaCuadraturaCustodia"=:v_FechaOperacionHasta;
 				 
		IF v_Count > 0 THEN	
			
			SELECT "IdPais" INTO v_IdPaisSistema FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
	  	
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior"
			 (
				 p_Fecha      		 => :v_FechaOperacionHasta
				,p_NumeroDias 		 => 1
				,p_EsHabil    		 => 'S'
				,p_IdPais     		 => v_IdPaisSistema
				,p_out_FechaAnterior => :v_FechaAnterior
			 );
					
					v_tablaCustodia=SELECT OPE.* FROM "BSCL.Normativa.BD::PRO.CuadraturaCustodia" OPE
					INNER JOIN :v_ParamNemos    		NEM ON (OPE."Nemotecnico"=NEM."Nemo" 	OR NEM."Nemo" IS NULL)
					INNER JOIN :v_ParamClasificacion    CLA ON (OPE."IdClasificacionTipoInstrumento"=CLA."Clasificacion" OR CLA."Clasificacion" IS NULL)
					INNER JOIN :v_ParamTipoInstrumento  TIP ON (OPE."IdInstrumento"=TIP."TipoInstrumento" OR TIP."TipoInstrumento" IS NULL)
					WHERE OPE."FechaCuadraturaCustodia"=:v_FechaOperacionHasta;
					
					SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdCircular1518".NEXTVAL INTO v_IdCircular1518 FROM DUMMY;
					
					INSERT INTO "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1518" (
						"IdInterno"	,
						"FechaCuadraturaCustodia",
						"Nemotecnico",
						"CantidadCustodia",
						"CantidadGarantia",
						"CantidadPlazoVenta",
						"CantidadPlazoCompra",
						"CantidadTotal",
						"CantidadDCVRelacionado",
						"CantidadDCVTerceros",
						"CantidadDCVCuentaPropia",
						"CantidadBCS",
						"CantidadBEC",
						"CantidadBOVALPO",
						"CantidadEmisores",
						"CantidadVentasEnTransito",
						"CantidadComprasEnTransito",
						"CantidadTransitoIngreso",
						"CantidadTransitoEgreso",
						"TotalCustodiosyEmisores",
						"Diferencia",
						"PrecioCierre",
						"Valorizacion",
						"DiferenciaValorizada",
						"ExplicacionDescuadratura",
						"IdCuadratura",
						"Mercado",
						"TipoInstrumento",
						"Tipo",
						"SorteoLetras",
						"CustodiaFisica"
						
					)
					Select 
						v_IdCircular1518 AS "IdInterno",
						OPE."FechaCuadraturaCustodia",
						CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' AND SF."ISIN" != '' AND F."IdMoneda"= 'USD' THEN  SF."ISIN"
							 WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' AND F."IdMoneda"= 'CLP' AND COALESCE(SF."NemoCMF",'') !='' THEN SF."NemoCMF"
						ELSE OPE."Nemotecnico"
						END	 AS "Nemotecnico",
						COALESCE(NOR."CantidadCustodia",OPE."CantidadCustodia"),
						COALESCE(NOR."CantidadGarantia",OPE."CantidadGarantia"),
						COALESCE(NOR."CantidadPlazoVenta",OPE."CantidadSimultaneaVenta" + OPE."CantidadPactoVenta"),
						COALESCE(NOR."CantidadPlazoCompra",OPE."CantidadSimultaneaCompra" + OPE."CantidadPactoCompra"),
						COALESCE(NOR."CantidadTotal",COALESCE(NOR."CantidadCustodia",OPE."CantidadCustodia") + COALESCE(NOR."CantidadGarantia",OPE."CantidadGarantia") - COALESCE(NOR."CantidadPlazoVenta",OPE."CantidadSimultaneaVenta" + OPE."CantidadPactoCompra") + COALESCE(NOR."CantidadPlazoCompra",OPE."CantidadSimultaneaCompra" + OPE."CantidadPactoVenta")),
						COALESCE(NOR."CantidadDCVRelacionado", COALESCE(POBLOREL."PosicionBloqueada",0) + OPE."CantidadDCVRelacionado"),
						COALESCE(NOR."CantidadDCVTerceros", COALESCE(POBLOTER."PosicionBloqueada",0) + OPE."CantidadDCVTerceros"),
						COALESCE(NOR."CantidadDCVCuentaPropia",COALESCE(POBLOCPRO."PosicionBloqueada",0) + OPE."CantidadDCVCuentaPropia"),
						COALESCE(NOR."CantidadBCS",OPE."CantidadBCS"),
						COALESCE(NOR."CantidadBEC",OPE."CantidadBEC"),
						COALESCE(NOR."CantidadBOVALPO",OPE."CantidadBOVALPO"),
						COALESCE(NOR."CantidadEmisores",CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN OPE."CantidadEmisores"
	   							 							ELSE COALESCE(NOR."CantidadTotal",OPE."CantidadEmisores")
						 END),
	   					
						COALESCE(OPE."VentaEnTransito",0) AS "CantidadVentasEnTransito",
						COALESCE(OPE."CompraEnTransito",0) AS "CantidadComprasEnTransito",
						ABS(COALESCE(NOR."CantidadTransitoIngreso",OPE."CantidadTransitoIngreso")),
						ABS(COALESCE(NOR."CantidadTransitoEgreso",OPE."CantidadTransitoEgreso")),						
						--sumatorias
						(   COALESCE(NOR."CantidadDCVTerceros",OPE."CantidadDCVTerceros") + COALESCE(POBLOTER."PosicionBloqueada",0) 
						  + COALESCE(NOR."CantidadDCVRelacionado",OPE."CantidadDCVRelacionado") + COALESCE(POBLOREL."PosicionBloqueada",0) 
						  + COALESCE(NOR."CantidadDCVCuentaPropia",OPE."CantidadDCVCuentaPropia") +	COALESCE(POBLOCPRO."PosicionBloqueada",0) 					  
						  + COALESCE(NOR."CantidadBCS",OPE."CantidadBCS") 
						  + COALESCE(NOR."CantidadBEC",OPE."CantidadBEC") 
						  + COALESCE(NOR."CantidadBOVALPO",OPE."CantidadBOVALPO")
						  + COALESCE(NOR."CantidadEmisores",CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN OPE."CantidadEmisores"
	   							 										ELSE COALESCE(OPE."CantidadEmisores",0) END)						  
	   					  + (ABS(COALESCE(OPE."CompraEnTransito",0))) - ABS(COALESCE(OPE."VentaEnTransito",0))
						) AS  "TotalCustodiosyEmisores",
						ROUND(COALESCE(NOR."Diferencia",
									(COALESCE(NOR."CantidadDCVTerceros",OPE."CantidadDCVTerceros") + COALESCE(POBLOTER."PosicionBloqueada",0)  
						  			+ COALESCE(NOR."CantidadDCVRelacionado",OPE."CantidadDCVRelacionado")  + COALESCE(POBLOREL."PosicionBloqueada",0) 
						  			+ COALESCE(NOR."CantidadDCVCuentaPropia",OPE."CantidadDCVCuentaPropia")	+ COALESCE(POBLOCPRO."PosicionBloqueada",0) 					  
						  			+ COALESCE(NOR."CantidadBCS",OPE."CantidadBCS") 
						  			+ COALESCE(NOR."CantidadBEC",OPE."CantidadBEC") 
						  			+ COALESCE(NOR."CantidadBOVALPO",OPE."CantidadBOVALPO")
						  			+ COALESCE(NOR."CantidadEmisores",CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN OPE."CantidadEmisores"
	   							 										ELSE COALESCE(OPE."CantidadEmisores",0) END)						  
	   					  												+ (ABS(COALESCE(OPE."CompraEnTransito",0))) - ABS(COALESCE(OPE."VentaEnTransito",0)))
									-ABS(COALESCE(NOR."CantidadCustodia",OPE."CantidadCustodia") 
									+ COALESCE(NOR."CantidadGarantia",OPE."CantidadGarantia") 
									- COALESCE(NOR."CantidadPlazoVenta",OPE."CantidadSimultaneaVenta" + OPE."CantidadPactoCompra")
									+ COALESCE(NOR."CantidadPlazoCompra",OPE."CantidadSimultaneaCompra" + OPE."CantidadPactoVenta"))
							    ),0) AS "Diferencia",
						
						COALESCE(CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN PREMER."PrecioCierre"
	   							 ELSE COALESCE(VC."ValorCuota",1) END,1) AS "PrecioCierre",
						ROUND(COALESCE(NOR."CantidadTotal",(COALESCE(NOR."CantidadCustodia",OPE."CantidadCustodia") + COALESCE(NOR."CantidadGarantia",OPE."CantidadGarantia") - COALESCE(NOR."CantidadPlazoVenta",OPE."CantidadSimultaneaVenta" + OPE."CantidadPactoVenta") + COALESCE(NOR."CantidadPlazoCompra",OPE."CantidadSimultaneaCompra" + OPE."CantidadPactoCompra"))) * COALESCE(PREMER."PrecioCierre",1),0) AS "Valorizacion", 
						---COALESCE(PREMER."PrecioCierre",1) * COALESCE(NOR."Diferencia",OPE."Diferencia") AS "DiferenciaValorizada",
						COALESCE(CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN PREMER."PrecioCierre"
											   							 ELSE COALESCE(VC."ValorCuota",1) END,1) * ((COALESCE(NOR."CantidadDCVTerceros",OPE."CantidadDCVTerceros") + COALESCE(POBLOTER."PosicionBloqueada",0)  
																  			+ COALESCE(NOR."CantidadDCVRelacionado",OPE."CantidadDCVRelacionado")  + COALESCE(POBLOREL."PosicionBloqueada",0) 
																  			+ COALESCE(NOR."CantidadDCVCuentaPropia",OPE."CantidadDCVCuentaPropia") + COALESCE(POBLOCPRO."PosicionBloqueada",0) 						  
																  			+ COALESCE(NOR."CantidadBCS",OPE."CantidadBCS") 
																  			+ COALESCE(NOR."CantidadBEC",OPE."CantidadBEC") 
																  			+ COALESCE(NOR."CantidadBOVALPO",OPE."CantidadBOVALPO")
																  			+ COALESCE(NOR."CantidadEmisores",CASE WHEN OPE."IdClasificacionTipoInstrumento" != 'FM' THEN OPE."CantidadEmisores"
											   							 										ELSE COALESCE(OPE."CantidadEmisores",0) END)						  
											   					  												+ (ABS(COALESCE(OPE."CompraEnTransito",0))) - ABS(COALESCE(OPE."VentaEnTransito",0)))
																			-ABS(COALESCE(NOR."CantidadCustodia",OPE."CantidadCustodia") 
																			+ COALESCE(NOR."CantidadGarantia",OPE."CantidadGarantia") 
																			- COALESCE(NOR."CantidadPlazoVenta",OPE."CantidadSimultaneaVenta" + OPE."CantidadPactoCompra")
																			+ COALESCE(NOR."CantidadPlazoCompra",OPE."CantidadSimultaneaCompra" +OPE."CantidadPactoVenta" ))) AS "DiferenciaValorizada",
						
						COALESCE(NOR."ExplicacionDescuadratura",OPE."ExplicacionDescuadratura"),
						OPE."IdCuadratura",
						COALESCE(CLAINS."Nombre",'') AS "Mercado",
						COALESCE(TIIN."Nombre",'') AS "TipoInstrumento",
						CASE WHEN  COALESCE(NOR."IdCuadratura",0) > 0 THEN 'MOD' ELSE 'ORI' END,
						OPE."CantidadSorteoLetras" AS "SorteoLetras",
						0 AS "CustodiaFisica"
					FROM :v_tablaCustodia as OPE
					 LEFT OUTER JOIN  "BSCL.Normativa.BD::NOR.DatosCIR1518" AS NOR
					 ON (OPE."IdCuadratura"=NOR."IdCuadratura")
					 LEFT JOIN (
							---Precio Mercado actual (monedad instrumento) x Instrumento
							SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha"
							FROM
								( 	
												
									SELECT row_number() over (partition by prec."IdInstrumento" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
									,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre"
									FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
									--INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
									--ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema' AND TO_DATE(prec."Fecha") <= TO_DATE(SIS."Valor")) 
									
									 WHERE  prec."IdBolsa" = 'BCS' AND  TO_DATE(prec."Fecha") <= TO_DATE(v_FechaOperacionHasta)
							  ) PRE	
							WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
					) PREMER ON (OPE."IdInstrumento"=PREMER."IdInstrumento")
					LEFT JOIN (
						SELECT VC."IdInstrumento",VC."ValorCuota",VC."FechaCierre"
						FROM
							( 	
											
								SELECT row_number() over (partition by SF."IdInstrumento" order by SF."IdInstrumento",VC."FechaCierre" desc) AS row_num,
									SF."IdInstrumento",VC."ValorCuota",VC."FechaCierre"
								FROM "BSCL.FondoMutuo.BD::FMM.ValorCuotaHistorico" VC 
									INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SF ON (SF."IdSerieFondo" = VC."IdSerieFondo")
								 WHERE  TO_DATE(VC."FechaCierre") <= TO_DATE(:v_FechaOperacionHasta)
						  ) VC	
						WHERE VC.row_num = 1 AND VC."ValorCuota" > 0
					) VC ON (OPE."IdInstrumento" = VC."IdInstrumento")
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SF 
						ON SF."IdInstrumento" = OPE."IdInstrumento"
					LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS F
						ON SF."IdFondo" = F."IdFondo"	
					LEFT JOIN (SELECT 
		 				 MAX(CC."IdCuadraturaCustodia")
		 				,SUM(CC."PosicionBloqueada") AS "PosicionBloqueada"
		 				,CC."Nemotecnico"
		 				,CC."IdClasificacionTipoInstrumento"
		 				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia" AS CC
		 				WHERE CC."FechaCuadraturaCustodia" = :v_FechaOperacionHasta  AND CC."IdEstado" = 'CER' AND CC."TipoCuenta"= 'TER'
		 				GROUP BY CC."Nemotecnico", CC."IdClasificacionTipoInstrumento"
		 			) AS POBLOTER ON POBLOTER."Nemotecnico"= OPE."Nemotecnico" AND POBLOTER."IdClasificacionTipoInstrumento"= OPE."IdClasificacionTipoInstrumento"
		 			LEFT JOIN (SELECT 
		 				 MAX(CC."IdCuadraturaCustodia")
		 				,SUM(CC."PosicionBloqueada") AS "PosicionBloqueada"
		 				,CC."Nemotecnico"
		 				,CC."IdClasificacionTipoInstrumento"
		 				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia" AS CC
		 				WHERE CC."FechaCuadraturaCustodia" = :v_FechaOperacionHasta  AND CC."IdEstado" = 'CER' AND CC."TipoCuenta"= 'REL'
		 				GROUP BY CC."Nemotecnico", CC."IdClasificacionTipoInstrumento"
		 			) AS POBLOREL ON POBLOREL."Nemotecnico"= OPE."Nemotecnico" AND POBLOREL."IdClasificacionTipoInstrumento"= OPE."IdClasificacionTipoInstrumento"
		 			LEFT JOIN (SELECT 
		 				 MAX(CC."IdCuadraturaCustodia")
		 				,SUM(CC."PosicionBloqueada") AS "PosicionBloqueada"
		 				,CC."Nemotecnico"
		 				,CC."IdClasificacionTipoInstrumento"
		 				FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia" AS CC
		 				WHERE CC."FechaCuadraturaCustodia" = :v_FechaOperacionHasta  AND CC."IdEstado" = 'CER' AND CC."TipoCuenta"= 'CPRO'
		 				GROUP BY CC."Nemotecnico", CC."IdClasificacionTipoInstrumento"
		 			) AS POBLOCPRO ON POBLOCPRO."Nemotecnico"= OPE."Nemotecnico" AND POBLOCPRO."IdClasificacionTipoInstrumento"= OPE."IdClasificacionTipoInstrumento"
		 			LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLAINS 
		 				ON CLAINS."IdClasificacionTipoInstrumento"= OPE."IdClasificacionTipoInstrumento"
		 			LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TIIN 
		 				ON TIIN."IdTipoInstrumento" = OPE."IdTipoInstrumento" AND TIIN."IdEstado" = 'VIG';
					
				select 
					sc."IdInterno"	,
					sc."FechaCuadraturaCustodia",
					sc."Nemotecnico",
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadCustodia"
					ELSE ROUND(sc."CantidadCustodia",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadGarantia"
					ELSE ROUND(sc."CantidadGarantia",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadPlazoVenta"
					ELSE ROUND(sc."CantidadPlazoVenta",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadPlazoCompra"
					ELSE ROUND(sc."CantidadPlazoCompra",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadTotal"
					ELSE ROUND(sc."CantidadTotal",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadDCVRelacionado"
					ELSE ROUND(sc."CantidadDCVRelacionado",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadDCVTerceros"
					ELSE ROUND(sc."CantidadDCVTerceros",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadDCVCuentaPropia"
					ELSE ROUND(sc."CantidadDCVCuentaPropia",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadBCS"
					ELSE ROUND(sc."CantidadBCS",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadBEC"
					ELSE ROUND(sc."CantidadBEC",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadBOVALPO"
					ELSE ROUND(sc."CantidadBOVALPO",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadEmisores"
					ELSE ROUND(sc."CantidadEmisores",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadVentasEnTransito"
					ELSE ROUND(sc."CantidadVentasEnTransito",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadComprasEnTransito"
					ELSE ROUND(sc."CantidadComprasEnTransito",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadTransitoIngreso"
					ELSE ROUND(sc."CantidadTransitoIngreso",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."CantidadTransitoEgreso"
					ELSE ROUND(sc."CantidadTransitoEgreso",0)  END,
					CASE WHEN OPE."IdClasificacionTipoInstrumento" = 'FM' THEN sc."TotalCustodiosyEmisores"
					ELSE ROUND(sc."TotalCustodiosyEmisores",0)  END,
					sc."Diferencia",
					sc."PrecioCierre",
					sc."Valorizacion",
					sc."DiferenciaValorizada",
					sc."ExplicacionDescuadratura",
					sc."IdCuadratura",
					sc."Mercado",
					sc."TipoInstrumento",
					sc."Tipo",
					sc."SorteoLetras",
					"CustodiaFisica"
					
				FROM  "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1518"  as sc
				LEFT OUTER JOIN :v_tablaCustodia as OPE  ON (OPE."IdCuadratura"=sc."IdCuadratura")
				WHERE sc."IdInterno"=v_IdCircular1518;
		/*		
		ELSE
				p_out_TablaSalida=SELECT
					0  AS	"IdInterno"	,
					'0001-01-01 00:00:00'  AS "FechaCuadraturaCustodia",
					'' AS 	"Nemotecnico",
					0  AS	"CantidadCustodia",
					0  AS	"CantidadGarantia",
					0  AS   "CantidadSimultaneaVenta",
					0  AS	"CantidadSimultaneaCompra",
					0  AS	"CantidadTotal",
					0  AS	"CantidadDCVRelacionado",
					0  AS	"CantidadDCVTerceros",
					0  AS	"CantidadDCVCuentaPropia",
					0  AS	"CantidadBCS",
					0  AS	"CantidadBEC",
					0  AS	"CantidadBOVALPO",
					0  AS	"CantidadOtros",
					0  AS	"CantidadTransitoIngreso",
					0  AS	"CantidadTransitoEgreso",
					'' AS 	"Diferencia",
					'' AS 	"ExplicacionDescuadratura"
					FROM DUMMY WHERE 1=2;	
					*/
		
		END IF;						
		
		p_out_IdInfoTranOfic =v_IdCircular1518;
		
 	END