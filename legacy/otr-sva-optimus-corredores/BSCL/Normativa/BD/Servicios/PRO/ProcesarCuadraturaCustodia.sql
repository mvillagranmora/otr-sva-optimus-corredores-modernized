SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::PRO.ProcesarCuadraturaCustodia";
GO
CREATE PROCEDURE 
--
--  Proposito: Servicio Invocado desde el JOB para procesar y cargar la cuadratura de custodia.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 22/03/2017

"BSCL.Normativa.BD.Servicios::PRO.ProcesarCuadraturaCustodia" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  BIGINT,	
	IN p_IdProceso 		  			  BIGINT		DEFAULT 0,
	IN p_FechaOperacion 		  	  NVARCHAR(10) 	DEFAULT ''--'YYYY-MM-DD'

  )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		
		DECLARE v_IdNormativa 								 NVARCHAR(15):='CIR1518';
		DECLARE v_IdTipoDocumento   		 	  			 NVARCHAR(10):='NORCUADCUS';
		DECLARE v_TipoTarea  	              				 NVARCHAR(40):='PROCESOCUADRATURACUSTODIA_NOR';
		
		DECLARE v_procesoRecalculo 							 NVARCHAR(1):='N';
		DECLARE v_FechaOperacion,v_out_FechaAnterior		 NVARCHAR(10);--'YYYY-MM-DD'
		DECLARE v_out_MensajeSalida							 NVARCHAR(200):='CUADRATURA CUSTODIA INACTIVA';
  		DECLARE v_fechaHoraSistema, v_out_MensajeEval		 NVARCHAR(40):='';
		DECLARE v_PRO_HAB_CUADRATURA_CUSTODIA				 NVARCHAR(520):='';
		DECLARE v_factor									 DECIMAL(26,8):=0;
		DECLARE v_out_error			  						 NVARCHAR(500);
		DECLARE	v_Count,v_IdPersona,v_IdDocumento, v_IdPais,v_NumeroDias,v_IdProceso,v_IdTrabajoParidad  BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
      

		---Parametros


		
		--------------------------------------------------------
		-- Identificar fecha de trabajo
		--------------------------------------------------------
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);

			
			IF p_FechaOperacion ='' AND p_IdProceso=0 THEN
				v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			ELSE
				IF p_FechaOperacion <>'' THEN
					v_FechaOperacion=p_FechaOperacion;
				ELSE
				
					SELECT COUNT(1) INTO v_Count FROM "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas"
					WHERE "IdPrograma"=	:p_IdProceso AND "NombreParametro"='FechaDesde';
					IF v_Count>0 THEN
						SELECT TOP 1 RTRIM("Valor") INTO v_FechaOperacion FROM "BSCL.Plataforma.BD.Vistas::BAT.ConsultarParametrosProgramas"
						WHERE "IdPrograma"=	:p_IdProceso AND "NombreParametro"='FechaDesde';
					ELSE
						v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
					END IF;
				END IF;	
			END IF;
		
			--INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor","Valor2")VALUES (p_IdProceso,v_FechaOperacion);
		--------------------------------------------------------
		-- Parametro de Habilitacion
		--------------------------------------------------------
		SELECT COUNT(1) INTO v_Count  -- PARAMETRO : RECALCULO
		FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_CUADRATURA_CUSTODIA' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN	
			SELECT "Valor" INTO v_PRO_HAB_CUADRATURA_CUSTODIA
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_CUADRATURA_CUSTODIA' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_PRO_HAB_CUADRATURA_CUSTODIA:= 'S';			

		END IF;		
		--------------------------------------------------------------------------------
		-- Cuadratura Habilitada 
		--------------------------------------------------------------------------------
	
		IF v_PRO_HAB_CUADRATURA_CUSTODIA='S' THEN-- PARAMETRO : EVALUACION

				--------------------------------------------------------		
				-- Obtencion de cuadratuta 
				--------------------------------------------------------
		
		
		

				SELECT COUNT(1) INTO  v_Count FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia"
												 WHERE "IdEstado"='CER'
												 AND "FechaCuadraturaCustodia" =:v_FechaOperacion;
				
				IF v_Count>0 THEN  -- solo con operaciones
					--------------------------------------------------------
					-- ACTUALIZAR  CUADRATURA EN NORMATIVA
					--------------------------------------------------------
					DELETE FROM  "BSCL.Normativa.BD::PRO.CuadraturaCustodia" NOR WHERE NOR."FechaCuadraturaCustodia"=:v_FechaOperacion;

 					v_Tab_Cuadratura=SELECT * FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCuadraturaCustodia"
												 WHERE "IdEstado"='CER'
												 AND "FechaCuadraturaCustodia" =:v_FechaOperacion;	
						
				   ---Obtener cantidades segun INSTRUMENTOS
				v_Tab_Cuadratura_Instrumentos=	
					SELECT 
						CUS."FechaCuadraturaCustodia",
					    CUS."IdInstrumento",
					    CUS."IdTipoInstrumento",
					    CUS."IdClasificacionTipoInstrumento",
						CUS."Nemotecnico",
						SUM(CAST (CUS."CantidadCustodia" AS DECIMAL(26,8))) 			AS "CantidadCustodia",	--1
						SUM(CAST (CUS."CantidadSimultaneaCompra" AS DECIMAL(26,8)))		AS "CantidadSimultaneaCompra",	--4
						SUM(CAST (CUS."CantidadSimultaneaVenta" AS DECIMAL(26,8)))		AS "CantidadSimultaneaVenta",	--3
						
						SUM(CAST (CUS."CantidadPactoCompra" AS DECIMAL(26,8)))		AS "CantidadPactoCompra",	--4
						SUM(CAST (CUS."CantidadPactoVenta" AS DECIMAL(26,8)))		AS "CantidadPactoVenta",	--3
						
						SUM(CAST (CUS."CantidadPrestamo" AS DECIMAL(26,8)))			AS "CantidadPrestamo",
						SUM(CAST (CUS."CantidadEmprestito" AS DECIMAL(26,8)))			AS "CantidadEmprestito",
						SUM(CAST (CUS."CantidadGarantia" AS DECIMAL(26,8)))				AS "CantidadGarantia",	--2
						SUM(CAST (CUS."CantidadTransitoIngreso" AS DECIMAL(26,8)))		AS "CantidadTransitoIngreso",
						SUM(CAST (CUS."CantidadTransitoEgreso" AS DECIMAL(26,8)))		AS "CantidadTransitoEgreso",
						SUM(CAST (CUS."CantidadTotal" AS DECIMAL(26,8)))				AS "CantidadTotal",	-- (1+2+3-4)  
						SUM(CAST (CUS."MontoATasaMercadoActual" AS DECIMAL(26,8)))		AS "MontoATasaMercadoActual",
						MAX(CUS."Diferencia")					AS "Diferencia",
						MAX(CUS."ExplicacionDescuadratura")		AS "ExplicacionDescuadratura"
					FROM :v_Tab_Cuadratura	CUS
				   GROUP BY  CUS."FechaCuadraturaCustodia",CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento" ,CUS."IdInstrumento",CUS."Nemotecnico";
				
				 ---Obtener cantidades segun INSTRUMENTOS/ TIPO DE CUENTA		
				v_Tab_Cuadratura_TipoCuenta_Prev=
					SELECT 
						CUS."FechaCuadraturaCustodia",
					    CUS."IdInstrumento",
					    CUS."IdTipoInstrumento",
					    CUS."IdClasificacionTipoInstrumento",
						CUS."Nemotecnico",
 						CUS."TipoCuenta",
 						SUM(CUS."CantidadCustodia") 			AS "CantidadTipoCuenta",
						SUM(CUS."CantidadGarantia")				AS "CantidadGarantia"
					FROM :v_Tab_Cuadratura	CUS
				   GROUP BY  CUS."FechaCuadraturaCustodia",CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento" ,CUS."IdInstrumento",CUS."Nemotecnico",CUS."TipoCuenta";
				
				
				---Obtener cantidades segun INSTRUMENTOS/ BOVEDA	
				v_Tab_Cuadratura_Boveda_Prev=
					SELECT 
						CUS."FechaCuadraturaCustodia",
					    CUS."IdInstrumento",
					    CUS."IdTipoInstrumento",
					    CUS."IdClasificacionTipoInstrumento",
						CUS."Nemotecnico",
 						CUS."IdBoveda",
						SUM(CUS."PosicionBoveda") AS "CantidadBoveda",
						SUM(CUS."CantidadGarantia")				AS "CantidadGarantia"
					FROM :v_Tab_Cuadratura	CUS
				   GROUP BY  CUS."FechaCuadraturaCustodia",CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento" ,CUS."IdInstrumento",CUS."Nemotecnico",CUS."IdBoveda";
				
						
					---Obtener cantidades segun INSTRUMENTOS/ TIPO DE CUENTA / BOVEDA	
				v_Tab_Cuadratura_TipoCuentaBoveda_Prev=
					SELECT 
						CUS."FechaCuadraturaCustodia",
					    CUS."IdInstrumento",
					    CUS."IdTipoInstrumento",
					    CUS."IdClasificacionTipoInstrumento",
						CUS."Nemotecnico",
						CUS."TipoCuenta",
 						CUS."IdBoveda",
						CASE
						  WHEN SUM(CUS."PosicionBoveda") > 0 THEN SUM(CUS."PosicionBoveda")
						ELSE 0
						END   AS "CantidadTipoCuentaBoveda",
						SUM(CUS."CantidadGarantia")				AS "CantidadGarantia"
					FROM :v_Tab_Cuadratura	CUS
				   GROUP BY  CUS."FechaCuadraturaCustodia",CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento" ,CUS."IdInstrumento",CUS."Nemotecnico",CUS."TipoCuenta",CUS."IdBoveda";
										
					/*TRANSPONER CANTIDADES*/			
				v_Tab_Cuadratura_Total=	
					SELECT
						INS."FechaCuadraturaCustodia",
					    INS."IdInstrumento",
					    INS."IdTipoInstrumento",
					    INS."IdClasificacionTipoInstrumento",
						INS."Nemotecnico",
						INS."CantidadCustodia",	--1
						INS."CantidadSimultaneaCompra",	--4
						INS."CantidadSimultaneaVenta",	--3
				
						INS."CantidadPactoCompra",	--4
						INS."CantidadPactoVenta",	--3
						
						
						INS."CantidadPrestamo",
						INS."CantidadEmprestito",
						INS."CantidadGarantia",	--2
						INS."CantidadTransitoIngreso",
						INS."CantidadTransitoEgreso",
						INS."CantidadTotal",	-- (1+2+3-4)  
						INS."Diferencia",
						INS."ExplicacionDescuadratura",
						INS."MontoATasaMercadoActual",
						COALESCE(TCTAREL."CantidadTipoCuenta",0) AS "CantidadRelacionado",
						COALESCE(TCTATER."CantidadTipoCuenta",0) AS "CantidadTerceros",						
						COALESCE(TCTAPRO."CantidadTipoCuenta",0) AS "CantidadCuentaPropia",												
						COALESCE(BOVCOR."CantidadBoveda",0) AS "CantidadIntermediario",												
						COALESCE(BOVBCS."CantidadBoveda",0) AS "CantidadBCS",
						COALESCE(BOVBEC."CantidadBoveda",0) AS "CantidadBEC",
						COALESCE(BOVBOVALPO."CantidadBoveda",0) AS "CantidadBOVALPO",
						COALESCE(BOVDCV."CantidadBoveda",0) AS "CantidadDCV",	
						COALESCE(BOVEMI."CantidadBoveda",0) AS "CantidadEmisores",																			
						COALESCE(BOVOTR."CantidadBoveda",0) AS "CantidadOtros",					
						/*SE AGREGA CANTIDAD GARANTIA POR TIPO*/
						COALESCE(BOVBCS."CantidadGarantia",0) AS "CantidadGarantiaBCS",
						COALESCE(BOVBEC."CantidadGarantia",0) AS "CantidadGarantiaBEC",
						COALESCE(BOVBOVALPO."CantidadGarantia",0) AS "CantidadGarantiaBOVALPO",
						COALESCE(BOVDCV."CantidadGarantia",0) AS "CantidadGarantiaDCV",	
						COALESCE(BOVEMI."CantidadGarantia",0) AS "CantidadGarantiaEmisores",																			
						COALESCE(BOVOTR."CantidadGarantia",0) AS "CantidadGarantiaOtros",
						/**/
						COALESCE(CTABOVDCVREL."CantidadTipoCuentaBoveda",0) AS "CantidadDCVRelacionado",
						COALESCE(CTABOVDCVTER."CantidadTipoCuentaBoveda",0) AS "CantidadDCVTerceros",
						COALESCE(CTABOVDCVPRO."CantidadTipoCuentaBoveda",0) AS "CantidadDCVCuentaPropia"
						
				FROM :v_Tab_Cuadratura_Instrumentos INS
			
				--TipoCuenta
				LEFT OUTER JOIN (SELECT "CantidadTipoCuenta","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuenta_Prev WHERE "TipoCuenta" ='REL') AS TCTAREL
				ON (INS."IdClasificacionTipoInstrumento"=TCTAREL."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =TCTAREL."IdTipoInstrumento"AND INS."IdInstrumento"= TCTAREL."IdInstrumento" AND INS."Nemotecnico"= TCTAREL."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadTipoCuenta","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuenta_Prev WHERE "TipoCuenta" ='TER') AS TCTATER
				ON (INS."IdClasificacionTipoInstrumento"=TCTATER."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =TCTATER."IdTipoInstrumento"AND INS."IdInstrumento"= TCTATER."IdInstrumento" AND INS."Nemotecnico"= TCTATER."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadTipoCuenta","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuenta_Prev WHERE "TipoCuenta" ='CPRO') AS TCTAPRO
				ON (INS."IdClasificacionTipoInstrumento"=TCTAPRO."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =TCTAPRO."IdTipoInstrumento"AND INS."IdInstrumento"= TCTAPRO."IdInstrumento" AND INS."Nemotecnico"= TCTAPRO."Nemotecnico")
				--Boveda
				LEFT OUTER JOIN (SELECT "CantidadBoveda","CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" ='COR') AS BOVCOR
				ON (INS."IdClasificacionTipoInstrumento"=BOVCOR."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVCOR."IdTipoInstrumento"AND INS."IdInstrumento"= BOVCOR."IdInstrumento" AND INS."Nemotecnico"= BOVCOR."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadBoveda","CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" ='BCS') AS BOVBCS
				ON (INS."IdClasificacionTipoInstrumento"=BOVBCS."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVBCS."IdTipoInstrumento"AND INS."IdInstrumento"= BOVBCS."IdInstrumento" AND INS."Nemotecnico"= BOVBCS."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadBoveda","CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" ='BEC') AS BOVBEC
				ON (INS."IdClasificacionTipoInstrumento"=BOVBEC."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVBEC."IdTipoInstrumento"AND INS."IdInstrumento"= BOVBEC."IdInstrumento" AND INS."Nemotecnico"= BOVBEC."Nemotecnico")	
				LEFT OUTER JOIN (SELECT "CantidadBoveda","CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" ='BOV') AS BOVBOVALPO
				ON (INS."IdClasificacionTipoInstrumento"=BOVBOVALPO."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVBOVALPO."IdTipoInstrumento"AND INS."IdInstrumento"= BOVBOVALPO."IdInstrumento" AND INS."Nemotecnico"= BOVBOVALPO."Nemotecnico")	
				LEFT OUTER JOIN (SELECT "CantidadBoveda","CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" ='DCV') AS BOVDCV
				ON (INS."IdClasificacionTipoInstrumento"=BOVDCV."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVDCV."IdTipoInstrumento"AND INS."IdInstrumento"= BOVDCV."IdInstrumento" AND INS."Nemotecnico"= BOVDCV."Nemotecnico")
				LEFT OUTER JOIN (SELECT SUM("CantidadBoveda") AS "CantidadBoveda",SUM("CantidadGarantia") AS "CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda"  IN ('CIA', 'COM')
									 GROUP BY  "FechaCuadraturaCustodia","IdClasificacionTipoInstrumento","IdTipoInstrumento" ,"IdInstrumento","Nemotecnico"										   ) AS BOVEMI
 				ON (INS."IdClasificacionTipoInstrumento"=BOVEMI."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVEMI."IdTipoInstrumento"AND INS."IdInstrumento"= BOVEMI."IdInstrumento" AND INS."Nemotecnico"= BOVEMI."Nemotecnico")

				LEFT OUTER JOIN (SELECT SUM("CantidadBoveda") AS "CantidadBoveda",SUM("CantidadGarantia") AS "CantidadGarantia","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_Boveda_Prev WHERE "IdBoveda" NOT IN ('COR','BCS','BEC','BOVALPO','DCV','CIA', 'COM')
									 GROUP BY  "FechaCuadraturaCustodia","IdClasificacionTipoInstrumento","IdTipoInstrumento" ,"IdInstrumento","Nemotecnico"										   ) AS BOVOTR
				ON (INS."IdClasificacionTipoInstrumento"=BOVOTR."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =BOVOTR."IdTipoInstrumento"AND INS."IdInstrumento"= BOVOTR."IdInstrumento" AND INS."Nemotecnico"= BOVOTR."Nemotecnico")
				-- TipoCuenta/Boveda
				LEFT OUTER JOIN (SELECT "CantidadTipoCuentaBoveda","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuentaBoveda_Prev WHERE  "IdBoveda" ='DCV' AND "TipoCuenta" ='REL') AS CTABOVDCVREL
				ON (INS."IdClasificacionTipoInstrumento"=CTABOVDCVREL."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =CTABOVDCVREL."IdTipoInstrumento"AND INS."IdInstrumento"= CTABOVDCVREL."IdInstrumento" AND INS."Nemotecnico"= CTABOVDCVREL."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadTipoCuentaBoveda","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuentaBoveda_Prev WHERE  "IdBoveda" ='DCV' AND "TipoCuenta" ='TER') AS CTABOVDCVTER
				ON (INS."IdClasificacionTipoInstrumento"=CTABOVDCVTER."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =CTABOVDCVTER."IdTipoInstrumento"AND INS."IdInstrumento"= CTABOVDCVTER."IdInstrumento" AND INS."Nemotecnico"= CTABOVDCVTER."Nemotecnico")
				LEFT OUTER JOIN (SELECT "CantidadTipoCuentaBoveda","IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdInstrumento","Nemotecnico" FROM :v_Tab_Cuadratura_TipoCuentaBoveda_Prev WHERE  "IdBoveda" ='DCV' AND "TipoCuenta" ='CPRO') AS CTABOVDCVPRO
				ON (INS."IdClasificacionTipoInstrumento"=CTABOVDCVPRO."IdClasificacionTipoInstrumento" AND INS."IdTipoInstrumento" =CTABOVDCVPRO."IdTipoInstrumento"AND INS."IdInstrumento"= CTABOVDCVPRO."IdInstrumento" AND INS."Nemotecnico"= CTABOVDCVPRO."Nemotecnico");
	

				INSERT INTO "BSCL.Normativa.BD::PRO.CuadraturaCustodia"(
				  	"IdCuadratura",
				    "IdEmpresa",
				    "FechaCuadraturaCustodia",
				    "IdInstrumento",
				    "IdTipoInstrumento",
				    "IdClasificacionTipoInstrumento",
					"Nemotecnico",
					"CantidadCustodia",
					"CantidadSimultaneaCompra",
					"CantidadSimultaneaVenta",
					"CantidadPactoCompra",
					"CantidadPactoVenta",
					"CantidadPrestamo",
					"CantidadEmprestito",
					"CantidadGarantia",
					"CantidadTransitoIngreso",
					"CantidadTransitoEgreso",
					"CantidadTotal",
					"CantidadRelacionado",
					"CantidadTerceros",
					"CantidadCuentaPropia",
					"CantidadIntermediario",
					"CantidadBCS",
					"CantidadBEC",
					"CantidadBOVALPO",
					"CantidadDCV",
					"CantidadEmisores",
					"CantidadOtros",
					/**/
					"CantidadGarantiaBCS",
					"CantidadGarantiaBEC",
					"CantidadGarantiaBOVALPO",
					"CantidadGarantiaDCV",	
					"CantidadGarantiaEmisores",																			
					"CantidadGarantiaOtros",
					/**/
					"CantidadDCVRelacionado",
					"CantidadDCVTerceros",
					"CantidadDCVCuentaPropia",
					"Diferencia",
					"ExplicacionDescuadratura",
					"Valorizacion",
				    "FechaIngreso",
				    "FechaCreacion"
				    )
				  SELECT 
				   "BSCL.Normativa.BD.Secuencias::PRO.IdCuadratura".NEXTVAL AS  "IdCuadratura",
				    :p_IdEmpresa AS "IdEmpresa",
					CUA."FechaCuadraturaCustodia",
				    CUA."IdInstrumento",
				    CUA."IdTipoInstrumento",
				    CUA."IdClasificacionTipoInstrumento",
					CUA."Nemotecnico",
					CUA."CantidadCustodia",
					CUA."CantidadSimultaneaCompra",
					CUA."CantidadSimultaneaVenta",
					CUA."CantidadPactoCompra",
					CUA."CantidadPactoVenta",					
					CUA."CantidadPrestamo",
					CUA."CantidadEmprestito",
					CUA."CantidadGarantia",
					CUA."CantidadTransitoIngreso",
					CUA."CantidadTransitoEgreso",
					CUA."CantidadTotal",
					CUA."CantidadRelacionado",
					CUA."CantidadTerceros",
					CUA."CantidadCuentaPropia",
					CUA."CantidadIntermediario",
					CUA."CantidadBCS",
					CUA."CantidadBEC",	
					CUA."CantidadBOVALPO",				
					CUA."CantidadDCV",
					CUA."CantidadEmisores",
					CUA."CantidadOtros",
					/**/
					CUA."CantidadGarantiaBCS",
					CUA."CantidadGarantiaBEC",
					CUA."CantidadGarantiaBOVALPO",
					CUA."CantidadGarantiaDCV",	
					CUA."CantidadGarantiaEmisores",																			
					CUA."CantidadGarantiaOtros",
					/**/
					CUA."CantidadDCVRelacionado",
					CUA."CantidadDCVTerceros",
					CUA."CantidadDCVCuentaPropia",
					CUA."Diferencia",
					CUA."ExplicacionDescuadratura",
					CUA."MontoATasaMercadoActual",
				    TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaIngreso",
				    CURRENT_UTCTIMESTAMP AS "FechaCreacion"
					FROM :v_Tab_Cuadratura_Total CUA;
					
 				END IF;
 				v_out_MensajeSalida	='CUADRATURA CUSTODIA ACTUALIZADA FECHA :' || :v_FechaOperacion;
 		ELSE
 			v_out_MensajeSalida	='CUADRATURA CUSTODIA SIN REGISTROS FECHA :' || :v_FechaOperacion;
 		END IF;
		
		
	
			--------------------------------------------------------------------------------
		-- Registrar tabla Procesos
		--------------------------------------------------------------------------------

		SELECT "BSCL.Normativa.BD.Secuencias::PRO.IdProceso".NEXTVAL INTO v_IdProceso FROM DUMMY;
		
		INSERT INTO "BSCL.Normativa.BD::PRO.Proceso" (
				   "IdProceso",
				   "IdEmpresa",
				   "IdModulo",
				   "IdNormativa",
				   "IdTipoDocumento",
				   "IdTipoTarea", --Tarea de inscripcion en BAT
				   "IdSolicitudCircular",
				   "IdProcesoBatch",
				   "IdEstado",
				   "Descripcion",
				   "FechaIngreso",
				   "FechaCreacion"
				   )
					SELECT
					v_IdProceso,
					p_IdEmpresa,
					'NOR',
					v_IdNormativa,
					v_IdTipoDocumento,
					v_TipoTarea,
					0,
					p_IdProceso,
					'TER',
					v_out_MensajeSalida,
					TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD'),
					CURRENT_UTCTIMESTAMP
					FROM DUMMY;		
				


END; 
