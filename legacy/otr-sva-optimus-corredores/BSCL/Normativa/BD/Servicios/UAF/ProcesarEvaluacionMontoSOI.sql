SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::UAF.ProcesarEvaluacionMontoSOI";
GO

CREATE PROCEDURE 
--
--  Proposito: Servicio Invocado desde el JOB para evaluar el monto operado de los cliente versus el monto promedio calculado, patrimonio y monto maximo.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 12/01/2017

"BSCL.Normativa.BD.Servicios::UAF.ProcesarEvaluacionMontoSOI" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  BIGINT,	
	IN p_IdSolicitudCircular 		  BIGINT, 
	IN p_IdProceso 		  			  BIGINT,
	IN p_FechaOperacion 		  	 NVARCHAR(10) DEFAULT ''--'YYYY-MM-DD'

  )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		
		DECLARE v_IdNormativa 								 NVARCHAR(15):='UAF';
		DECLARE v_IdTipoDocumento   		 	  			 NVARCHAR(10):='UAFSOIEST';--'UAFSOIEVA';
		DECLARE v_TipoTarea  	              				 NVARCHAR(40):='ProcesarEvaluacionMontoSOI';
		
		DECLARE v_procesoRecalculo 							 NVARCHAR(1):='N';
		DECLARE v_FechaOperacion,v_out_FechaAnterior		 NVARCHAR(10);--'YYYY-MM-DD'
		DECLARE v_out_MensajeSalida							 NVARCHAR(200):='EVALUACION INACTIVA';
  		DECLARE v_fechaHoraSistema, v_out_MensajeEval		 NVARCHAR(200):='';
		DECLARE v_EVAL_PLAPRO_UAFSOI,v_EVAL_CARABO_UAFSOI, v_PRO_HAB_EVALUACION_MONTOPROM, v_PRO_EVA_MONTOMAXIMO_FACTOR,v_PRO_EVA_MONTOPROMEDIO_FACTOR, v_PRO_EVA_PATRIMONIO_FACTOR	 NVARCHAR(520):='';
		DECLARE v_factor									 DECIMAL(26,8):=0;
		DECLARE v_out_error			  						 NVARCHAR(500);
		DECLARE	v_Count,v_IdPersona,v_IdDocumento, v_IdPais,v_NumeroDias,v_IdProceso,v_IdTrabajoParidad  BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
    	DECLARE v_out_EvaluacionSOI NVARCHAR(100);

		---Parametros


		
		--------------------------------------------------------
		-- Identificar fecha de trabajo
		--------------------------------------------------------
		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
		);

	
			IF p_FechaOperacion ='' THEN
			v_FechaOperacion=TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD');
			ELSE
			v_FechaOperacion=p_FechaOperacion;
			END IF;
		

			--------------------------------------------------------
			-- Determinar cantidad de dias para hacer calculo
			--------------------------------------------------------
			IF p_FechaOperacion ='' THEN
			    --Post cambio de fecha en pauta y post caluclo de estadistiva del cliente 'ESTADISTICAOPERACIONCLIENTE'
				v_NumeroDias=	0; -- Retroceder
				SELECT "IdPais" INTO  v_IdPais FROM  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
						WHERE "IdEmpresa"=p_IdEmpresa;
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior" ( p_Fecha=>v_FechaOperacion,p_NumeroDias=>v_NumeroDias, p_EsHabil=>'S',	p_IdPais=>v_IdPais,p_out_FechaAnterior=>v_out_FechaAnterior);
				v_FechaOperacion=v_out_FechaAnterior;
			END IF;
			--------------------------------------------------------		
			-- Obtencion de Operaciones 
			--------------------------------------------------------
	
			v_TablaModulo = Select 'FAC' as "IdModulo" from dummy;-- where 1=2;
			
			---Acotar busqueda servicio BASE
			v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacion AS "Valor" FROM DUMMY
						UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacion AS "Valor" FROM DUMMY;
						
				CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
				(
					p_IdEmpresa			=> p_IdEmpresa,
					p_IdNormativa		=> v_IdNormativa,
					p_IdTipoDocumento	=> v_IdTipoDocumento,
					p_tablaModulo		=> :v_TablaModulo,
					p_registroParametro	=> :v_registroParametro,				
					p_out_TablaSalida	=> v_SalidaConsultarOperaciones
				);
					
			
----------EVALUACION MOVIMIENTOS CTA . INVERSION SIN OPERACION RELACIONADA

				----Parametro de Evaluacion
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_CARABO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
			IF v_Count>0 THEN
				SELECT "Valor" INTO v_EVAL_CARABO_UAFSOI
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_CARABO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
				ELSE
					v_EVAL_CARABO_UAFSOI:='N';
			END IF;
	
				---------------------------------------------------
				--Parametro de Evaluacion PMOVIMIENTO_ASOCIADO' (S/N)
				---------------------------------------------------			

			IF v_EVAL_CARABO_UAFSOI ='S' THEN
			
				SELECT COUNT(1) INTO  v_Count   
						 FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
						 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion 
						 ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
						 WHERE cargoabonomov."IdEstado"='CRT'
						 AND cargoabonomov."IdMovimientoAsociado"=0
						 AND cargoabonomov."IdCarga"=0 ;
 
				IF v_Count>0 THEN  -- A) solo con operaciones
				
				
					v_TabEvalCargoAbono=		
						SELECT  
						cuentainversion."IdCliente",
						''	AS "IdTipoInstrumento",
						''	AS "IdClasificacionTipoInstrumento",
						TO_DATE(cargoabonomov."FechaCertificacion") AS "Fecha",
						 --VALORES DE CALCULO
						cargoabonomov."Monto"			AS "MontoUtilizado",
						0 								AS "MontoPromedioDiario",
						0							 	AS "Diferencia",
						0 								AS "Tolerancia",
						0								AS "Factor",
						0								AS"MontoTotalDias",
						0								AS"CantidadDias",
						cuentainversion."IdPortafolio",
						"IdCargoAbonoMovimiento" 		AS "IdOperacion",
						cuentainversion."IdMoneda"  	AS "IdMoneda",
						''								AS "IdTipoProducto",
						"IdTipoMovimiento" AS "IdTipoOperacion"
						FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
						 INNER JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion 
						 ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
						 WHERE cargoabonomov."IdEstado"='CRT'
						 AND cargoabonomov."IdMovimientoAsociado"=0
						 AND cargoabonomov."IdCarga"=0
						 AND TO_DATE(cargoabonomov."FechaCertificacion") >= TO_DATE(v_FechaOperacion);
						 
					

						SELECT COUNT(1) INTO v_Count FROM :v_TabEvalCargoAbono;
						IF v_Count>0 THEN	---Actualizar Evaluacion
								
							INSERT INTO "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"
							 (     "IdEvaluacion",
								   "IdEmpresa",
								   "IdModulo",
								   "IdCliente",
								   "IdPortafolio",
								   "IdTipoProducto",
								   "IdTipoOperacion",
								   "IdOperacion",
								   "MontoUtilizado",
								   "IdMoneda",
								   "IdTipoInstrumento",
								   "IdClasificacionTipoInstrumento",
								   "FechaOperacion",--'YYYY-MM-DD'
								    ---Evaluacion
									"TipoEvaluacion",
								    "EstMontoTotalDias",
								    "EstCantidadDias",
								    "EstMontoPromedioDiario",
								    ---Calculos
									"ConceptoEvaluacion",
									"CalDiferencia",
									"CalTolerancia",
									"Factor",
									---Auditoria
								    "FechaIngreso",
								    "FechaCreacion",
								    "IdUsuarioCreador"							 
							 )
							SELECT
								"BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion" .NEXTVAL AS "IdEvaluacion",	
								 p_IdEmpresa 			AS "IdEmpresa",
								 'NOR' 					AS "IdModulo",
								 EVA."IdCliente"		AS "IdCliente",
								 EVA."IdPortafolio"		AS "IdPortafolio",
								 EVA."IdTipoProducto"	AS "IdTipoProducto",
								 EVA."IdTipoOperacion"	AS "IdTipoOperacion",
								 EVA."IdOperacion"		AS "IdOperacion",
								 EVA."MontoUtilizado"	AS "MontoUtilizado",
								 EVA."IdMoneda"			AS "IdMoneda",
								 EVA."IdTipoInstrumento"				AS "IdTipoInstrumento", 
								 EVA."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
								 EVA."Fecha"							AS "FechaOperacion",
								 'MOVIMIENTO_ASOCIADO' 					AS "TipoEvaluacion",
								 EVA."MontoTotalDias"					AS "EstMontoTotalDias",
								 EVA."CantidadDias" 					AS "EstCantidadDias",
								 EVA."MontoPromedioDiario"				AS "EstMontoPromedioDiario",
								 'IdMovimientoAsociado'					AS "ConceptoEvaluacion",	
								 EVA."Diferencia"						AS "CalDiferencia",
								 EVA."Tolerancia"						AS "CalTolerancia",
								 EVA."Factor"							AS "Factor",
								 TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaIngreso",
								 CURRENT_UTCTIMESTAMP					AS "FechaCreacion",
								 0 										AS "IdUsuarioCreador"
							FROM :v_TabEvalCargoAbono EVA
							WHERE  NOT EXISTS
							(SELECT 1 FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
								WHERE   SOI."IdEmpresa"=p_IdEmpresa AND  SOI."IdModulo"='NOR' AND SOI."IdCliente"=EVA."IdCliente"
								    AND SOI."IdPortafolio"=EVA."IdPortafolio" AND SOI."IdOperacion"=EVA."IdOperacion" AND SOI."IdTipoOperacion"=EVA."IdTipoOperacion" AND SOI."TipoEvaluacion"='CARGO-ABONO');  	 	
										
							v_out_MensajeEval	= TRIM(v_out_MensajeEval) || '/CA-AB\';		
						END IF;
						
				
				
				
				
				
				END IF;
			END IF;--v_EVAL_CARABO_UAFSOI
						
			
----------EVALUACIONES POR PLANTILLA


				----Parametro de Evaluacion
			SELECT COUNT(1) INTO v_Count
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
			IF v_Count>0 THEN
				SELECT "Valor" INTO v_EVAL_PLAPRO_UAFSOI
					FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='EVAL_PLAPRO_UAFSOI' AND "IdNormativa"=v_IdNormativa;
				ELSE
					v_EVAL_PLAPRO_UAFSOI:='N';
			END IF;
	
				---------------------------------------------------
				--Parametro de Evaluacion Plantilla Producto' (S/N)
				---------------------------------------------------			

			IF v_EVAL_PLAPRO_UAFSOI ='S' THEN
				SELECT COUNT(1) INTO  v_Count FROM :v_SalidaConsultarOperaciones;
				IF v_Count>0 THEN  -- A) solo con operaciones
				
				v_tab_plantilla=	SELECT  "IdCliente"
											, "IdPortafolio"
											, "IdTipoProducto"
											, "IdTipoOperacion"
											, "IdOperacion"
											, "Monto"  AS "MontoUtilizado"
											, "IdMoneda"
											, "IdTipoInstrumento"
											, "IdClasificacionTipoInstrumento"
											, TO_VARCHAR (TO_DATE("FechaMovimiento"), 'YYYY-MM-DD') AS "FechaOperacion" 
											FROM :v_SalidaConsultarOperaciones;
											
				
				
					CALL "BSCL.Normativa.BD.Servicios.UAF::EvaluacionSOI" (
							 p_IdEmpresa 				=> :p_IdEmpresa
							,p_IdUsuario 				=> 0
							,p_IdModulo 				=> 'NOR'
							,p_UsaReglasNegocio			=> 'S'
							,p_simularOperacion			=> 'N'
							,p_tablaOperacion 			=> :v_tab_plantilla
							,p_out_EvaluacionSOI 		=> v_out_EvaluacionSOI
							,p_out_EvaluacionDetalle 	=> v_out_EvaluacionDetalle 
							,p_out_EvaluacionResumen 	=> v_out_EvaluacionResumen 
							,p_out_AdvertenciasContador => v_out_AdvertenciasContador
							,p_out_AdvertenciasDetalle 	=> v_out_AdvertenciasDetalle
							,p_out_ErroresContador 		=> v_out_ErroresContador
							,p_out_ErroresDetalle 		=> v_out_ErroresDetalle);
				
				END IF;
			END IF;
			
			
----------EVALUACIONES POR MONTO			
			
			
		--------------------------------------------------------
		-- Parametro de Habilitacion
		--------------------------------------------------------
		SELECT COUNT(1) INTO v_Count  
		FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
		WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_EVALUACION_MONTOPROM' AND "IdNormativa"=v_IdNormativa;
		
		IF v_Count>0 THEN	
			SELECT "Valor" INTO v_PRO_HAB_EVALUACION_MONTOPROM
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
			WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_HAB_EVALUACION_MONTOPROM' AND "IdNormativa"=v_IdNormativa;
			ELSE
			v_PRO_HAB_EVALUACION_MONTOPROM:= 'N';			

		END IF;		
		
	
		--------------------------------------------------------------------------------
		-- Evaluacion Habilitada 
		--------------------------------------------------------------------------------
	
		IF v_PRO_HAB_EVALUACION_MONTOPROM='S' THEN-- PARAMETRO : EVALUACION
	
				SELECT COUNT(1) INTO  v_Count FROM :v_SalidaConsultarOperaciones;
				
				IF v_Count>0 THEN  -- A) solo con operaciones
					--------------------------------------------------------
					-- PARIDAD MONETARIA
					--------------------------------------------------------
						SELECT COUNT(1) INTO  v_Count FROM :v_SalidaConsultarOperaciones WHERE "IdMoneda"<> 'CLP';
						IF v_Count>0 THEN
						
							SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
							
							v_TablaInterparidades= SELECT ROW_NUMBER() OVER() AS "IdInterno",V.* FROM :v_SalidaConsultarOperaciones V
													WHERE "IdMoneda"<> 'CLP';
							
							INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
							SELECT :v_IdTrabajoParidad,Tab."IdInterno",TO_VARCHAR(Tab."FechaMovimiento",'YYYY-MM-DD') AS "Fecha", Tab."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
							0 as "ValorCambioLocalModificado",Tab."MontoCompra" as "MontoOrigen",'ORDENES' as "IdUso"
							FROM :v_TablaInterparidades Tab;
				
							call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
								p_IdEmpresa => :p_IdEmpresa,
								p_IdTrabajo => :v_IdTrabajoParidad
							);
							
							v_OperacionesResultado=
									SELECT DISTINCT COALESCE(outt."MontoDestino",0) AS  "MontoParidad", par.*
									FROM :v_TablaInterparidades par
									LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
									on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad
									WHERE par."IdMoneda"<> 'CLP'
								UNION ALL
									 SELECT OP."MontoCompra" AS "MontoParidad", 0 AS "IdInterno" ,OP.*
								    FROM :v_SalidaConsultarOperaciones  OP WHERE OP."IdMoneda"= 'CLP';
							
				
						ELSE
							v_OperacionesResultado=
							 	SELECT OP."MontoCompra" AS "MontoParidad", 0 AS "IdInterno" ,OP.*
								    FROM :v_SalidaConsultarOperaciones OP;
						END IF;
						
				
						--------------------------------------------------------
						-- EVALUACION: MONTO VERSUS MONTO MAXIMO
						--------------------------------------------------------

						---Parametro : Factor de Calculo

						SELECT COUNT(1) INTO v_Count
							FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_MONTOMAXIMO_FACTOR' AND "IdNormativa"=v_IdNormativa;
						IF v_Count>0 THEN	
							SELECT "Valor" INTO v_PRO_EVA_MONTOMAXIMO_FACTOR
								FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_MONTOMAXIMO_FACTOR' AND "IdNormativa"=v_IdNormativa;
							ELSE
							v_PRO_EVA_MONTOMAXIMO_FACTOR:= '0';			
				
						END IF;
						--v_NumeroDias=	CAST(TRIM(v_PRO_CAL_ACTMONTOPROM_CANTDIAS) AS BIGINT);
						v_factor=  TO_DECIMAL(TRIM(v_PRO_EVA_MONTOMAXIMO_FACTOR), 26, 8) ;
						
						v_TabEvalMontoMaximo=		
						SELECT  
						OP."IdCliente",
						OP."IdTipoInstrumento",
						OP."IdClasificacionTipoInstrumento",
						OP."Fecha",
						 --VALORES DE CALCULO
						OP."MontoDiario" 						AS "MontoUtilizado",
						EST."MontoMaximo",
						(OP."MontoDiario"-EST."MontoMaximo") 	AS "Diferencia",
						(EST."MontoMaximo" * v_factor) 			AS "Tolerancia",
						v_factor AS "Factor",
						OP."IdPortafolio",
						OP."IdOperacion",
						OP."IdMoneda",
						OP."IdTipoProducto",
						OP."IdTipoOperacion"
						FROM 
						 (SELECT "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD') AS "Fecha","IdTipoInstrumento","IdClasificacionTipoInstrumento", SUM("MontoParidad") AS "MontoDiario"
						 ,MAX("IdPortafolio") AS "IdPortafolio",MAX("IdOperacion") AS "IdOperacion"
						 ,MAX("IdMoneda") AS "IdMoneda",MAX("IdTipoProducto") AS "IdTipoProducto",MAX("IdTipoOperacion") AS "IdTipoOperacion"
						 FROM  :v_OperacionesResultado  GROUP BY "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD'),"IdTipoInstrumento","IdClasificacionTipoInstrumento") OP
						 INNER JOIN "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" EST
						 ON (EST."IdCliente"=OP."IdCliente" AND EST."IdTipoInstrumento" =OP."IdTipoInstrumento" AND EST."IdClasificacionTipoInstrumento"=OP."IdClasificacionTipoInstrumento"
						 	 AND OP."MontoDiario">EST."MontoMaximo")
						 WHERE  (OP."MontoDiario"-EST."MontoMaximo") > (EST."MontoMaximo" * v_factor); --Diferencia Mayor a la Tolerancia
												
												
										
						SELECT COUNT(1) INTO v_Count FROM :v_TabEvalMontoMaximo;
						IF v_Count>0 THEN	---Actualizar Evaluacion
								
							INSERT INTO "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"
							 (     "IdEvaluacion",
								   "IdEmpresa",
								   "IdModulo",
								   "IdCliente",
								   "IdPortafolio",
								   "IdTipoProducto",
								   "IdTipoOperacion",
								   "IdOperacion",
								   "MontoUtilizado",
								   "IdMoneda",
								   "IdTipoInstrumento",
								   "IdClasificacionTipoInstrumento",
								   "FechaOperacion",--'YYYY-MM-DD'
								    ---Evaluacion
									"TipoEvaluacion",
								    "EstMontoMaximo",
								    ---Calculos
									"ConceptoEvaluacion",
									"CalDiferencia",
									"CalTolerancia",
									"Factor",
									---Auditoria
								    "FechaIngreso",
								    "FechaCreacion",
								    "IdUsuarioCreador"							 
							 )
							SELECT
								"BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion" .NEXTVAL AS "IdEvaluacion",	
								 p_IdEmpresa 			AS "IdEmpresa",
								 'NOR' 					AS "IdModulo",
								 EVA."IdCliente"		AS "IdCliente",
								 EVA."IdPortafolio"		AS "IdPortafolio",
								 EVA."IdTipoProducto"	AS "IdTipoProducto",
								 EVA."IdTipoOperacion"	AS "IdTipoOperacion",
								 EVA."IdOperacion"		AS "IdOperacion",
								 EVA."MontoUtilizado"	AS "MontoUtilizado",
								 EVA."IdMoneda"			AS "IdMoneda",
								 EVA."IdTipoInstrumento"				AS "IdTipoInstrumento", 
								 EVA."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
								 EVA."Fecha"							AS "FechaOperacion",
								 'MONTO_MAXIMO' 						AS "TipoEvaluacion",
								 EVA."MontoMaximo"						AS "EstMontoMaximo",
								 'MontoUtilizado, MontoMaximo'			AS "ConceptoEvaluacion",	
								 EVA."Diferencia"						AS "CalDiferencia",
								 EVA."Tolerancia"						AS "CalTolerancia",
								 EVA."Factor"							AS "Factor",
								 TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaIngreso",
								 CURRENT_UTCTIMESTAMP					AS "FechaCreacion",
								 0 										AS "IdUsuarioCreador"
							FROM :v_TabEvalMontoMaximo EVA
							WHERE  NOT EXISTS
							(SELECT 1 FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
								WHERE   SOI."IdEmpresa"=p_IdEmpresa AND  SOI."IdModulo"='NOR' AND SOI."IdCliente"=EVA."IdCliente"
								    AND SOI."IdPortafolio"=EVA."IdPortafolio" AND SOI."IdOperacion"=EVA."IdOperacion" AND SOI."IdTipoOperacion"=EVA."IdTipoOperacion" AND SOI."TipoEvaluacion"='MONTO_MAXIMO'); 	
							
										
							v_out_MensajeEval	= TRIM(v_out_MensajeEval) || '/MAX\';		
						END IF;
						
						--------------------------------------------------------
						-- EVALUACION: MONTO VERSUS MONTO PROMEDIO DIARIO
						--------------------------------------------------------						
						---Parametro : Factor de Calculo

						SELECT COUNT(1) INTO v_Count
							FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_MONTOPROMEDIO_FACTOR' AND "IdNormativa"=v_IdNormativa;
						IF v_Count>0 THEN	
							SELECT "Valor" INTO v_PRO_EVA_MONTOPROMEDIO_FACTOR
								FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_MONTOPROMEDIO_FACTOR' AND "IdNormativa"=v_IdNormativa;
							ELSE
							v_PRO_EVA_MONTOPROMEDIO_FACTOR:= '0';			
				
						END IF;
						--v_NumeroDias=	CAST(TRIM(v_PRO_CAL_ACTMONTOPROM_CANTDIAS) AS BIGINT);
						v_factor=  TO_DECIMAL(TRIM(v_PRO_EVA_MONTOPROMEDIO_FACTOR), 26, 8) ;


						v_TabEvalMontoPromedio=		
						SELECT  
						OP."IdCliente",
						OP."IdTipoInstrumento",
						OP."IdClasificacionTipoInstrumento",
						OP."Fecha",
						 --VALORES DE CALCULO
						OP."MontoDiario" 								AS "MontoUtilizado",
						EST."MontoPromedioDiario",
						(OP."MontoDiario"-EST."MontoPromedioDiario") 	AS "Diferencia",
						(EST."MontoPromedioDiario" * v_factor) 			AS "Tolerancia",
						v_factor AS "Factor",
						EST."MontoTotalDias",
						EST."CantidadDias",
						OP."IdPortafolio",
						OP."IdOperacion",
						OP."IdMoneda",
						OP."IdTipoProducto",
						OP."IdTipoOperacion"
						FROM 
						 (SELECT "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD') AS "Fecha","IdTipoInstrumento","IdClasificacionTipoInstrumento", SUM("MontoParidad") AS "MontoDiario"
						 ,MAX("IdPortafolio") AS "IdPortafolio",MAX("IdOperacion") AS "IdOperacion"
						 ,MAX("IdMoneda") AS "IdMoneda",MAX("IdTipoProducto") AS "IdTipoProducto",MAX("IdTipoOperacion") AS "IdTipoOperacion"
						 FROM  :v_OperacionesResultado  GROUP BY "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD'),"IdTipoInstrumento","IdClasificacionTipoInstrumento") OP
						 INNER JOIN "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" EST
						 ON (EST."IdCliente"=OP."IdCliente" AND EST."IdTipoInstrumento" =OP."IdTipoInstrumento" AND EST."IdClasificacionTipoInstrumento"=OP."IdClasificacionTipoInstrumento"
						 	 AND OP."MontoDiario">EST."MontoPromedioDiario")
						 WHERE  (OP."MontoDiario"-EST."MontoPromedioDiario") > (EST."MontoPromedioDiario" * v_factor); --Diferencia Mayor a la Tolerancia


						SELECT COUNT(1) INTO v_Count FROM :v_TabEvalMontoPromedio;
						IF v_Count>0 THEN	---Actualizar Evaluacion
								
							INSERT INTO "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"
							 (     "IdEvaluacion",
								   "IdEmpresa",
								   "IdModulo",
								   "IdCliente",
								   "IdPortafolio",
								   "IdTipoProducto",
								   "IdTipoOperacion",
								   "IdOperacion",
								   "MontoUtilizado",
								   "IdMoneda",
								   "IdTipoInstrumento",
								   "IdClasificacionTipoInstrumento",
								   "FechaOperacion",--'YYYY-MM-DD'
								    ---Evaluacion
									"TipoEvaluacion",
								    "EstMontoTotalDias",
								    "EstCantidadDias",
								    "EstMontoPromedioDiario",
								    ---Calculos
									"ConceptoEvaluacion",
									"CalDiferencia",
									"CalTolerancia",
									"Factor",
									---Auditoria
								    "FechaIngreso",
								    "FechaCreacion",
								    "IdUsuarioCreador"							 
							 )
							SELECT
								"BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion" .NEXTVAL AS "IdEvaluacion",	
								 p_IdEmpresa 			AS "IdEmpresa",
								 'NOR' 					AS "IdModulo",
								 EVA."IdCliente"		AS "IdCliente",
								 EVA."IdPortafolio"		AS "IdPortafolio",
								 EVA."IdTipoProducto"	AS "IdTipoProducto",
								 EVA."IdTipoOperacion"	AS "IdTipoOperacion",
								 EVA."IdOperacion"		AS "IdOperacion",
								 EVA."MontoUtilizado"	AS "MontoUtilizado",
								 EVA."IdMoneda"			AS "IdMoneda",
								 EVA."IdTipoInstrumento"				AS "IdTipoInstrumento", 
								 EVA."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
								 EVA."Fecha"							AS "FechaOperacion",
								 'MONTO_PROMEDIO' 						AS "TipoEvaluacion",
								 EVA."MontoTotalDias"					AS "EstMontoTotalDias",
								 EVA."CantidadDias" 					AS "EstCantidadDias",
								 EVA."MontoPromedioDiario"				AS "EstMontoPromedioDiario",
								 'MontoUtilizado, MontoMaximo'			AS "ConceptoEvaluacion",	
								 EVA."Diferencia"						AS "CalDiferencia",
								 EVA."Tolerancia"						AS "CalTolerancia",
								 EVA."Factor"							AS "Factor",
								 TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaIngreso",
								 CURRENT_UTCTIMESTAMP					AS "FechaCreacion",
								 0 										AS "IdUsuarioCreador"
							FROM :v_TabEvalMontoPromedio EVA
							WHERE  NOT EXISTS
							(SELECT 1 FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
								WHERE   SOI."IdEmpresa"=p_IdEmpresa AND  SOI."IdModulo"='NOR' AND SOI."IdCliente"=EVA."IdCliente"
								    AND SOI."IdPortafolio"=EVA."IdPortafolio" AND SOI."IdOperacion"=EVA."IdOperacion" AND SOI."IdTipoOperacion"=EVA."IdTipoOperacion" AND SOI."TipoEvaluacion"='MONTO_PROMEDIO');  	 	
										
							v_out_MensajeEval	= TRIM(v_out_MensajeEval) || '/PRO\';		
						END IF;
						
						
						--------------------------------------------------------
						-- EVALUACION: MONTO VERSUS PATRIMONIO
						--------------------------------------------------------
						--------------------------------------------------------						
						---Parametro : Factor de Calculo
						--------------------------------------------------------						

						SELECT COUNT(1) INTO v_Count
							FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_PATRIMONIO_FACTOR' AND "IdNormativa"=v_IdNormativa;
						IF v_Count>0 THEN	
							SELECT "Valor" INTO v_PRO_EVA_PATRIMONIO_FACTOR
								FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
							WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_EVA_PATRIMONIO_FACTOR' AND "IdNormativa"=v_IdNormativa;
							ELSE
							v_PRO_EVA_PATRIMONIO_FACTOR:= '0';			
				
						END IF;
						--v_NumeroDias=	CAST(TRIM(v_PRO_CAL_ACTMONTOPROM_CANTDIAS) AS BIGINT);
						v_factor=  TO_DECIMAL(TRIM(v_PRO_EVA_PATRIMONIO_FACTOR), 26, 8) ;
					
						--------------------------------------------------------						
						---Parametro : Buscar Patrimonio y pasar a CLP
						--------------------------------------------------------							
					
						SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
								
						v_TablaPatrimonioParidades= 
							SELECT ROW_NUMBER() OVER() AS "IdInterno", PAR."IdCliente",PAR."Patrimonio"
							FROM(
								SELECT DISTINCT SOC."IdCliente",COALESCE(PAT."Maximo",0) AS "Patrimonio"
										FROM :v_OperacionesResultado OPE
										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
										ON (SOC."IdCliente"=OPE."IdCliente")
										INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" PER	
										ON (SOC."IdPersona"=PER."IdPersona")
										LEFT OUTER  JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPatrimonio" PAT
										ON (PAT."IdRangoPatrimonio"=PER."IdRangoPatrimonio" AND PAT."Maximo" >=0 )
									) AS PAR;	 
							
							
							INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
							SELECT :v_IdTrabajoParidad,Tab."IdInterno",v_FechaOperacion AS "Fecha", 'UF' as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
							0 as "ValorCambioLocalModificado",Tab."Patrimonio" as "MontoOrigen",'ORDENES' as "IdUso"
							FROM :v_TablaPatrimonioParidades Tab;
				
							call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
								p_IdEmpresa => :p_IdEmpresa,
								p_IdTrabajo => :v_IdTrabajoParidad
							);
							
							v_PatrimoniosResultado=
									SELECT  DISTINCT COALESCE(outt."MontoDestino",0) AS  "PatrimonioParidad", par.*
									FROM :v_TablaPatrimonioParidades par
									INNER JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
									on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad;
									
							/*
								BEGIN
									INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor","Valor2")
									SELECT "IdInterno" ,"IdUso"
									--FROM :v_TablaPatrimonioParidades;
									
									FROM "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" 
															WHERE "IdTrabajo" = :v_IdTrabajoParidad;
			
									END;
							*/
					---Evaluacion
						v_TabEvalPatrimonio=		
						SELECT  
						OP."IdCliente",
						OP."IdTipoInstrumento",
						OP."IdClasificacionTipoInstrumento",
						OP."Fecha",
						 --VALORES DE CALCULO
						OP."MontoDiario" 								AS "MontoUtilizado",
						PATR."PatrimonioParidad",
						(OP."MontoDiario"-PATR."PatrimonioParidad") 	AS "Diferencia",
						(PATR."PatrimonioParidad" * v_factor) 			    AS "Tolerancia",
						v_factor AS "Factor",
						OP."IdPortafolio",
						OP."IdOperacion",
						OP."IdMoneda",
						OP."IdTipoProducto",
						OP."IdTipoOperacion"
						FROM 
						 (SELECT "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD') AS "Fecha","IdTipoInstrumento","IdClasificacionTipoInstrumento", SUM("MontoCompra") AS "MontoDiario"
						 ,MAX("IdPortafolio") AS "IdPortafolio",MAX("IdOperacion") AS "IdOperacion"
						 ,MAX("IdMoneda") AS "IdMoneda",MAX("IdTipoProducto") AS "IdTipoProducto",MAX("IdTipoOperacion") AS "IdTipoOperacion"
						 FROM  :v_OperacionesResultado  GROUP BY "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD'),"IdTipoInstrumento","IdClasificacionTipoInstrumento") OP
						 INNER JOIN (SELECT "PatrimonioParidad","IdCliente"
						 				FROM :v_PatrimoniosResultado
								 ) AS  PATR
						 ON (PATR."IdCliente"=OP."IdCliente" AND OP."MontoDiario">PATR."PatrimonioParidad")
						 WHERE  ((OP."MontoDiario"-PATR."PatrimonioParidad") > (PATR."PatrimonioParidad" * v_factor)) AND PATR."PatrimonioParidad" >0 ; --Diferencia Mayor a la Tolerancia

						SELECT COUNT(1) INTO v_Count FROM :v_TabEvalPatrimonio;
						IF v_Count>0 THEN	---Actualizar Evaluacion
								
							INSERT INTO "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI"
							 (     "IdEvaluacion",
								   "IdEmpresa",
								   "IdModulo",
								   "IdCliente",
								   "IdPortafolio",
								   "IdTipoProducto",
								   "IdTipoOperacion",
								   "IdOperacion",
								   "MontoUtilizado",
								   "IdMoneda",
								   "IdTipoInstrumento",
								   "IdClasificacionTipoInstrumento",
								   "FechaOperacion",--'YYYY-MM-DD'
								    ---Evaluacion
									"TipoEvaluacion",
								    "Patrimonio",
								    ---Calculos
									"ConceptoEvaluacion",
									"CalDiferencia",
									"CalTolerancia",
									"Factor",
									---Auditoria
								    "FechaIngreso",
								    "FechaCreacion",
								    "IdUsuarioCreador"							 
							 )
							SELECT
								"BSCL.Normativa.BD.Secuencias::UAF.IdEvaluacion" .NEXTVAL AS "IdEvaluacion",	
								 p_IdEmpresa 			AS "IdEmpresa",
								 'NOR' 					AS "IdModulo",
								 EVA."IdCliente"		AS "IdCliente",
								 EVA."IdPortafolio"		AS "IdPortafolio",
								 EVA."IdTipoProducto"	AS "IdTipoProducto",
								 EVA."IdTipoOperacion"	AS "IdTipoOperacion",
								 EVA."IdOperacion"		AS "IdOperacion",
								 EVA."MontoUtilizado"	AS "MontoUtilizado",
								 EVA."IdMoneda"			AS "IdMoneda",
								 EVA."IdTipoInstrumento"				AS "IdTipoInstrumento", 
								 EVA."IdClasificacionTipoInstrumento"	AS "IdClasificacionTipoInstrumento",
								 EVA."Fecha"							AS "FechaOperacion",
								 'PATRIMONIO' 							AS "TipoEvaluacion",
								 EVA."PatrimonioParidad"				AS "Patrimonio",
								 'MontoUtilizado, Patrimonio'			AS "ConceptoEvaluacion",	
								 EVA."Diferencia"						AS "CalDiferencia",
								 EVA."Tolerancia"						AS "CalTolerancia",
								 EVA."Factor"							AS "Factor",
								 TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') AS "FechaIngreso",
								 CURRENT_UTCTIMESTAMP					AS "FechaCreacion",
								 0 										AS "IdUsuarioCreador"
							FROM :v_TabEvalPatrimonio EVA 
							WHERE  NOT EXISTS
							(SELECT 1 FROM "BSCL.Normativa.BD::UAF.EvaluacionOperacionSOI" SOI
								WHERE   SOI."IdEmpresa"=p_IdEmpresa AND  SOI."IdModulo"='NOR' AND SOI."IdCliente"=EVA."IdCliente"
								    AND SOI."IdPortafolio"=EVA."IdPortafolio" AND SOI."IdOperacion"=EVA."IdOperacion" AND SOI."IdTipoOperacion"=EVA."IdTipoOperacion" AND SOI."TipoEvaluacion"='PATRIMONIO');  
									
							v_out_MensajeEval	= TRIM(v_out_MensajeEval) || '/PAT\';		
						END IF;
						
					

						v_out_MensajeSalida='FECHA : ' || TRIM(v_FechaOperacion) || ' EVAL : ' || TRIM(v_out_MensajeEval);
					ELSE
						v_out_MensajeSalida :='SIN OPERACIONES FECHA : ' || v_FechaOperacion;	
					END IF;	-- A) solo con operaciones				
		
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
					'UAFSOIEVA',
					v_TipoTarea,
					p_IdSolicitudCircular,
					p_IdProceso,
					'TER',
					v_out_MensajeSalida,
					TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD'),
					CURRENT_UTCTIMESTAMP
					FROM DUMMY;		
				


END; 
