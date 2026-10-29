SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::EJORD.ProcesarEjecucionOrdenes";
GO


CREATE PROCEDURE
--
--  Proposito: Servicio que retorna informacion de ejecución de ordenes para confeccionar Reporte CIR1985.
--  Autor: 
--  Fecha Creacion: 08/02/2017


 "BSCL.Normativa.BD.Servicios::EJORD.ProcesarEjecucionOrdenes" (
 	
 	--VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_IdEmpresa					  BIGINT DEFAULT 0,
	IN p_IdSolicitudCircular 		  BIGINT, 
	IN p_IdProceso 		  			  BIGINT,
	IN p_FechaOperacion 		  	  NVARCHAR(10) DEFAULT ''--'YYYY-MM-DD'
 )
 
 	LANGUAGE SQLSCRIPT
 	SQL SECURITY INVOKER
 	DEFAULT SCHEMA "BSCL"
 AS
 	BEGIN
 		--------------------
		--Declare Variables
		--------------------
		DECLARE v_IdNormativa 								 NVARCHAR(15):='EJECUCIONORDEN';
		DECLARE v_TipoTarea  	              				 NVARCHAR(40):='PROCESOLECTURAEJECUCIONORDENES';
		DECLARE v_out_MensajeSalida							 NVARCHAR(200):='LECTURA INACTIVA';
		DECLARE v_CondOrdenes,v_CondCliCalificados,v_CondCliEnvio,v_IdProceso BIGINT:=0;
 		DECLARE v_fechaHoraSistema, v_out_MensajeEval		 NVARCHAR(40):='';
 		DECLARE v_FechaOperacion,v_out_FechaAnterior		 NVARCHAR(10);--'YYYY-MM-DD'
		DECLARE v_FechaUltimoDiaHabilSemana DATE;
		DECLARE v_FechaUltimoDiaHabilMes    DATE;
		DECLARE v_IdPais                NVARCHAR(10) := '';
		DECLARE v_CantDiasHabilesSemana INTEGER      := 0;
		DECLARE v_CantDiasHabilesMes    INTEGER      := 0;
		DECLARE v_EsDiaHabil            INTEGER      := 0;
 		
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

		v_FechaUltimoDiaHabilSemana := TO_DATE(v_FechaOperacion);
		v_FechaUltimoDiaHabilMes    := TO_DATE(v_FechaOperacion);

		SELECT "IdPais" INTO v_IdPais
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
		WHERE "IdEmpresa" = p_IdEmpresa;

		-- Verificar que p_Fecha es día hábil
		SELECT COUNT(1) INTO v_EsDiaHabil
		FROM DUMMY
		WHERE DAYOFWEEK(TO_DATE(v_FechaOperacion)) BETWEEN 2 AND 6
		  AND NOT EXISTS (
		      SELECT 1
		      FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado"
		      WHERE "IdPais" = v_IdPais
		        AND "Fecha" = TO_DATE(v_FechaOperacion)
		  );

		IF v_EsDiaHabil <> 0 THEN

			-- Último día hábil de la SEMANA
			SELECT COUNT(1) INTO v_CantDiasHabilesSemana
			FROM SERIES_GENERATE_INTEGER(1, 1, 7) S
			WHERE WEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = WEEK(TO_DATE(v_FechaOperacion))
			  AND YEAR(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = YEAR(TO_DATE(v_FechaOperacion))
			  AND DAYOFWEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) BETWEEN 2 AND 6
			  AND NOT EXISTS (
			      SELECT 1
			      FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" F
			      WHERE F."IdPais" = v_IdPais
			        AND F."Fecha" = ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)
			  );

			IF v_CantDiasHabilesSemana = 0 THEN
			    v_FechaUltimoDiaHabilSemana := TO_DATE(v_FechaOperacion);
			ELSE
			    SELECT MAX(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER))
			    INTO v_FechaUltimoDiaHabilSemana
			    FROM SERIES_GENERATE_INTEGER(1, 1, 7) S
			    WHERE WEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = WEEK(TO_DATE(v_FechaOperacion))
			      AND YEAR(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = YEAR(TO_DATE(v_FechaOperacion))
			      AND DAYOFWEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) BETWEEN 2 AND 6
			      AND NOT EXISTS (
			          SELECT 1
			          FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" F
			          WHERE F."IdPais" = v_IdPais
			            AND F."Fecha" = ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)
			      );
			END IF;

			-- Último día hábil del MES
			SELECT COUNT(1) INTO v_CantDiasHabilesMes
			FROM SERIES_GENERATE_INTEGER(1, 1, 32) S
			WHERE MONTH(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = MONTH(TO_DATE(v_FechaOperacion))
			  AND YEAR(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER))  = YEAR(TO_DATE(v_FechaOperacion))
			  AND DAYOFWEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) BETWEEN 2 AND 6
			  AND NOT EXISTS (
			      SELECT 1
			      FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" F
			      WHERE F."IdPais" = v_IdPais
			        AND F."Fecha" = ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)
			  );

			IF v_CantDiasHabilesMes = 0 THEN
			    v_FechaUltimoDiaHabilMes := TO_DATE(v_FechaOperacion);
			ELSE
			    SELECT MAX(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER))
			    INTO v_FechaUltimoDiaHabilMes
			    FROM SERIES_GENERATE_INTEGER(1, 1, 32) S
			    WHERE MONTH(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) = MONTH(TO_DATE(v_FechaOperacion))
			      AND YEAR(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER))  = YEAR(TO_DATE(v_FechaOperacion))
			      AND DAYOFWEEK(ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)) BETWEEN 2 AND 6
			      AND NOT EXISTS (
			          SELECT 1
			          FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarDiaFeriado" F
			          WHERE F."IdPais" = v_IdPais
			            AND F."Fecha" = ADD_DAYS(TO_DATE(v_FechaOperacion), S.ELEMENT_NUMBER)
			      );
			END IF;

		END IF;

		DELETE FROM  "BSCL.Normativa.BD::EJORD.EjecucionOrdenes" WHERE TO_DATE("FechaEjecucion") = TO_DATE(v_FechaOperacion);


		v_tabla_ordenes=  SELECT CO.* FROM  "BSCL.Normativa.BD.Vistas::EJORD.ConsultarOrdenes" AS CO
							INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente"   		CL ON (CL."IdCliente" = CO."IdCliente")
							INNER JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" 	SN ON (CL."IdCliente" = SN."IdCliente")
							LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Empresa" 		EM ON (EM."IdSocioDeNegocio" = SN."IdSocioDeNegocio")
							WHERE CO."IdTipoInstrumento" = 'ACN'				   --acciones nacionales	
								AND CO."IdEstadoOrden" <> 'ANU'					   --que no esten anuladas 	
								AND CO."IdTipoCanalDistribucion" NOT IN ('RUR')
								AND COALESCE(CL."IdTipoCliente",'')<>'INSTITUCIONAL'
								AND COALESCE(EM."IdSocioDeNegocio",0) = 0  ---No Cuenta Propia
								AND TO_DATE(CO."FechaEjecucion")=TO_DATE(v_FechaOperacion);
				
				
		SELECT COUNT(1) INTO v_CondOrdenes	FROM  :v_tabla_ordenes;
		
		IF(v_CondOrdenes>0) THEN
				v_Ordenes_Filtradas = 
							select 
							 CO."FolioOrden" 
							,CO."IdOrden" 
							,CO."IdCliente" 
							,CO."TipoOperacion"
							,CO."IdTipoOperacion"
							,CO."IdClasificacionTipoInstrumento"      
							,CO."Instrumento"     
							,CO."IdInstrumento" 
							,CO."IdTipoInstrumento"
							,CO."MontoOrden" 
							,CO."CantidadOrden"            
							,CO."PrecioOrden"   
							,CO."IdMoneda"          
							,CO."IdCondicionDeLiquidacion"     
							,CO."CondicionLiquidacion"                
							,CO."FechaRecepcion"             
							,CO."FechaAceptacion" 
							,CO."TipoEjecucion"      
							,CO."FolioPalo"
							,CO."FechaEjecucion"
							,CO."BolsaEjecucion"	
							,CO."EsOD"
							,CO."ModalidadEjecucion"
							,CO."TipoContraparte" 
							,CO."PrecioEjecucion"
							,CO."MontoTransado"
							,CO."CantidadTransada"
							,CO."ComisionTransada"
							---MERCADO
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) >= COALESCE("PVEN_MAX_OTR"."Precio",0) THEN COALESCE("PVEN_MAX_MER"."Precio",0)
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) < COALESCE("PVEN_MAX_OTR"."Precio",0) THEN COALESCE("PVEN_MAX_OTR"."Precio",0)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) >= COALESCE("PCOM_MAX_OTR"."Precio",0) THEN COALESCE("PCOM_MAX_MER"."Precio",0)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) < COALESCE("PCOM_MAX_OTR"."Precio",0) THEN COALESCE("PCOM_MAX_OTR"."Precio",0)
								ELSE 0
							END AS "PrecioMaximoMercado"
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) >= COALESCE("PVEN_MAX_OTR"."Precio",0) THEN SUBSTRING(COALESCE("PVEN_MAX_MER"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) < COALESCE("PVEN_MAX_OTR"."Precio",0) THEN SUBSTRING(COALESCE("PVEN_MAX_OTR"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) >= COALESCE("PCOM_MAX_OTR"."Precio",0) THEN SUBSTRING(COALESCE("PCOM_MAX_MER"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) < COALESCE("PCOM_MAX_OTR"."Precio",0) THEN SUBSTRING(COALESCE("PCOM_MAX_OTR"."FechaHora",'0000-00-00 00:00:00'),12,5)
								ELSE ''
							END AS "HoraPrecioMaximoMercado"
							
							
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) >= COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN COALESCE("PVEN_MIN_OTR"."Precio",0)
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) < COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN COALESCE("PVEN_MIN_MER"."Precio",0)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) >= COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN COALESCE("PCOM_MIN_OTR"."Precio",0)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) < COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN COALESCE("PCOM_MIN_MER"."Precio",0)
								ELSE 0
							END AS "PrecioMinimoMercado"
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) >= COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN SUBSTRING(COALESCE("PVEN_MIN_OTR"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) < COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN SUBSTRING(COALESCE("PVEN_MIN_MER"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) >= COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN SUBSTRING(COALESCE("PCOM_MIN_OTR"."FechaHora",'0000-00-00 00:00:00'),12,5)
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) < COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN SUBSTRING(COALESCE("PCOM_MIN_MER"."FechaHora",'0000-00-00 00:00:00'),12,5)
								ELSE ''
							END AS "HoraPrecioMinimoMercado"
							
							--CORREDOR
							,CASE WHEN "TipoOperacion"  ='V' THEN COALESCE("PVEN_MAX_COR"."Precio",0) ELSE COALESCE("PCOM_MAX_COR"."Precio",0) END 	AS "MayorPrecioOperacionCorredor"
							,CASE WHEN "TipoOperacion"  ='V' THEN  SUBSTRING (COALESCE("PVEN_MAX_COR"."FechaHora",'0000-00-00 00:00:00'),12,5) ELSE SUBSTRING (COALESCE("PCOM_MAX_COR"."FechaHora",'0000-00-00 00:00:00'),12,5) END AS "HoraPrecioMayorCorredor"
							
					
							,CASE WHEN "TipoOperacion"  ='V' THEN COALESCE("PVEN_MIN_COR"."Precio",0) ELSE COALESCE("PCOM_MIN_COR"."Precio",0) END 	AS "MenorPrecioOperacionCorredor"
							,CASE WHEN "TipoOperacion"  ='V' THEN  SUBSTRING (COALESCE("PVEN_MIN_COR"."FechaHora",'0000-00-00 00:00:00'),12,5) ELSE SUBSTRING (COALESCE("PCOM_MIN_COR"."FechaHora",'0000-00-00 00:00:00'),12,5) END AS "HoraPrecioMenorCorredor"
						
							--OTRAS BOLSAS
							,CASE WHEN "TipoOperacion"  ='V' THEN COALESCE("MKD"."priceBuyingPrice",0) ELSE COALESCE("MKD"."priceSellingPrice",0) END 	AS "MejorPrecioOtraBolsa"
							,CO."CantidadMejorPrecioOtraBolsa"
							,CASE WHEN "TipoOperacion"  ='V' THEN COALESCE("PVEN_MAX_MER"."Precio",0) ELSE COALESCE("PCOM_MAX_MER"."Precio",0) END  AS "MejorPrecioMercado"				
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) >= COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN 'BEC'
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MIN_MER"."Precio",999999) < COALESCE("PVEN_MIN_OTR"."Precio",999999) THEN 'BCS'
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) >= COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN 'BEC'
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MIN_MER"."Precio",999999) < COALESCE("PCOM_MIN_OTR"."Precio",999999) THEN 'BCS'
								ELSE ''
							END AS "IdBolsaPrecioMinimoMercado" 		 
							,CASE
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) >= COALESCE("PVEN_MAX_OTR"."Precio",0) THEN 'BCS'
								WHEN "TipoOperacion"  ='V' AND COALESCE("PVEN_MAX_MER"."Precio",0) < COALESCE("PVEN_MAX_OTR"."Precio",0) THEN 'BEC'
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) >= COALESCE("PCOM_MAX_OTR"."Precio",0) THEN 'BCS'
								WHEN "TipoOperacion"  ='C' AND COALESCE("PCOM_MAX_MER"."Precio",0) < COALESCE("PCOM_MAX_OTR"."Precio",0) THEN 'BEC'
								ELSE ''
							END AS "IdBolsaPrecioMaximoMercado" 		 
							,CO."RutCliente"  
							,CO."Cuenta"        
							,CO."IdEmpleadoDigitador" 
							,CO."NombreCliente"
							,CO."CodigoAgente"
							,CO."NombreAgente"
							,CO."FolioComprobanteFacturacion"
							,CO."FolioExterno"
							,CO."FechaIngreso"
							FROM  :v_tabla_ordenes as CO
								--VENTA : MAXIMOS
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MAX_COR" ON ("CO"."Instrumento"="PVEN_MAX_COR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MAX_COR"."Fecha" AND "PVEN_MAX_COR"."Tipo"='MAXIMO' AND "PVEN_MAX_COR"."Ambito"='CORREDOR')
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MAX_MER" ON ("CO"."Instrumento"="PVEN_MAX_MER"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MAX_MER"."Fecha" AND "PVEN_MAX_MER"."Tipo"='MAXIMO' AND "PVEN_MAX_MER"."Ambito"='MERCADO' )
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MAX_OTR" ON ("CO"."Instrumento"="PVEN_MAX_OTR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MAX_OTR"."Fecha" AND "PVEN_MAX_OTR"."Tipo"='MAXIMO' AND "PVEN_MAX_OTR"."Ambito"='OTRASBOLSAS' )
								--VENTA : MINIMOS
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MIN_COR" ON ("CO"."Instrumento"="PVEN_MIN_COR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MIN_COR"."Fecha" AND "PVEN_MIN_COR"."Tipo"='MINIMO' AND "PVEN_MIN_COR"."Ambito"='CORREDOR')
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MIN_MER" ON ("CO"."Instrumento"="PVEN_MIN_MER"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MIN_MER"."Fecha" AND "PVEN_MIN_MER"."Tipo"='MINIMO' AND "PVEN_MIN_MER"."Ambito"='MERCADO' )
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosVenta" "PVEN_MIN_OTR" ON ("CO"."Instrumento"="PVEN_MIN_OTR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PVEN_MIN_OTR"."Fecha" AND "PVEN_MIN_OTR"."Tipo"='MINIMO' AND "PVEN_MIN_OTR"."Ambito"='OTRASBOLSAS' )
								
								--COMPRA : MAXIMOS
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MAX_COR" ON ("CO"."Instrumento"="PCOM_MAX_COR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MAX_COR"."Fecha" AND "PCOM_MAX_COR"."Tipo"='MAXIMO' AND "PCOM_MAX_COR"."Ambito"='CORREDOR')
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MAX_MER" ON ("CO"."Instrumento"="PCOM_MAX_MER"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MAX_MER"."Fecha" AND "PCOM_MAX_MER"."Tipo"='MAXIMO' AND "PCOM_MAX_MER"."Ambito"='MERCADO' )
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MAX_OTR" ON ("CO"."Instrumento"="PCOM_MAX_OTR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MAX_OTR"."Fecha" AND "PCOM_MAX_OTR"."Tipo"='MAXIMO' AND "PCOM_MAX_OTR"."Ambito"='OTRASBOLSAS' )
								--COMPRA : MINIMOS
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MIN_COR" ON ("CO"."Instrumento"="PCOM_MIN_COR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MIN_COR"."Fecha" AND "PCOM_MIN_COR"."Tipo"='MINIMO' AND "PCOM_MIN_COR"."Ambito"='CORREDOR')
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MIN_MER" ON ("CO"."Instrumento"="PCOM_MIN_MER"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MIN_MER"."Fecha" AND "PCOM_MIN_MER"."Tipo"='MINIMO' AND "PCOM_MIN_MER"."Ambito"='MERCADO' )
								LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::EJORD.ConsultarPreciosCompra" "PCOM_MIN_OTR" ON ("CO"."Instrumento"="PCOM_MIN_OTR"."Nemo" AND TO_DATE(TO_VARCHAR("CO"."FechaEjecucion", 'YYYY-MM-DD'))="PCOM_MIN_OTR"."Fecha" AND "PCOM_MIN_OTR"."Tipo"='MINIMO' AND "PCOM_MIN_OTR"."Ambito"='OTRASBOLSAS' )
		
								--MEJORES PRECIOS OTRAS BOLSAS
								LEFT OUTER JOIN "BSCL.Orden.BD::MRV.HIS_PALOS_RV" "MKD" ON ("CO"."FolioPalo" = "MKD"."tradeID" AND TO_DATE(CO."FechaEjecucion") = TO_DATE(SUBSTRING (MKD."tradeTime",1,8), 'YYYYMMDD') AND "MKD"."priceExchange"<>'XSGO')
							WHERE  TO_DATE(CO."FechaEjecucion")=TO_DATE(v_FechaOperacion);
					
							
							
							
					select count(1) into v_CondOrdenes from :v_Ordenes_Filtradas;
					IF(v_CondOrdenes>0)	THEN
								v_out_MensajeSalida='OPERACIONES ENCONTRADAS : ' || CAST (:v_CondOrdenes AS VARCHAR(15));
					
								INSERT INTO "BSCL.Normativa.BD::EJORD.EjecucionOrdenes"
								("IdEjecucion","IdEmpresa","FolioGeneracion","IdOrden","Folio","IdCliente","IdPortafolio","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdTipoOperacion","IdInstrumento"
								,"Cantidad","Monto","MontoValorizado","Precio","IdMoneda","IdCondicionDeLiquidacion","FechaRecepcion","FechaAceptacion","TipoEjecucion","FechaEjecucion","IdBolsa"
								,"EsOD","PrecioEjecucion","MontoTransado","CantidadTransada","ComisionTransada","MejorPrecioOtraBolsa","CantidadMejorPrecioOtraBolsa","MejorPrecioMercado","PrecioMinimoMercado"
								,"HoraPrecioMinimoMercado","IdBolsaPrecioMinimoMercado","PrecioMaximoMercado","HoraPrecioMaximoMercado","IdBolsaPrecioMaximoMercado","MayorPrecioOperacionCorredor","HoraPrecioMayorCorredor"
								,"MenorPrecioOperacionCorredor","HoraPrecioMenorCorredor","IdEmpleadoDigitador","FolioComprobanteFacturacion","FechaGeneracion","FolioExterno","IdEstadoNotificacion","IdEstadoCarta","FechaIngreso","FechaCreacion","IdUsuarioCreador","FechaReferencia")
								
								select 
								 "BSCL.Normativa.BD.Secuencias::EjecucionOrdenes".NEXTVAL  as "IdEjecucion"
								,COALESCE(p_IdEmpresa,0)				as "IdEmpresa"
								, 0 									as "FolioGeneracion"
								, ORD."IdOrden" 						as "IdOrden"
								, ORD."FolioPalo" 						as "Folio"
								, ORD."IdCliente"						as "IdCliente"
								, ORD."Cuenta"							as "IdPortafolio"
								, ORD."IdClasificacionTipoInstrumento" 	as "IdClasificacionTipoInstrumento"
								, ORD."IdTipoInstrumento" 				as "IdTipoInstrumento"
								, ORD."IdTipoOperacion"  				as "IdTipoOperacion"
								, ORD."IdInstrumento"					as "IdInstrumento"
								, ORD."CantidadOrden"   				as "Cantidad"
								, ORD."MontoOrden"						as "Monto"
								, 0										as "MontoValorizado"
								, ORD."PrecioOrden"   					as "Precio"
								, ORD."IdMoneda"						as "IdMoneda"
								, ORD."IdCondicionDeLiquidacion"  		as "IdCondicionDeLiquidacion"
								, ORD."FechaRecepcion" 					as "FechaRecepcion"
								, ORD."FechaAceptacion" 				as "FechaAceptacion"
								, ORD."TipoEjecucion"   				as "TipoEjecucion"
								, COALESCE(ORD."FechaEjecucion",'0001-01-01 00:00:01') as "FechaEjecucion"
								, COALESCE(ORD."BolsaEjecucion",'BCS')	as "IdBolsa"
								, COALESCE(ORD."EsOD",'N')				as "EsOD"
								, COALESCE(ORD."PrecioEjecucion",0)		as "PrecioEjecucion"
								, COALESCE(ORD."MontoTransado",0)		as "MontoTransado"
								, COALESCE(ORD."CantidadTransada",0)	as "CantidadTransada"
								, COALESCE(ORD."ComisionTransada",0)	as "ComisionTransada"
								, ORD."MejorPrecioOtraBolsa"      		as "MejorPrecioOtraBolsa"
								, ORD."CantidadMejorPrecioOtraBolsa" 	as "CantidadMejorPrecioOtraBolsa"
								, ORD."MejorPrecioMercado" 				as "MejorPrecioMercado"
								, ORD."PrecioMinimoMercado" 			as "PrecioMinimoMercado"
								, ORD."HoraPrecioMinimoMercado" 		as "HoraPrecioMinimoMercado"
								, ORD."IdBolsaPrecioMinimoMercado" 		as "IdBolsaPrecioMinimoMercado"
								, ORD."PrecioMaximoMercado" 			as "PrecioMaximoMercado"
								, ORD."HoraPrecioMaximoMercado" 		as "HoraPrecioMaximoMercado"
								, ORD."IdBolsaPrecioMaximoMercado" 		as "IdBolsaPrecioMaximoMercado"
								, ORD."MayorPrecioOperacionCorredor" 	as "MayorPrecioOperacionCorredor"
								, ORD."HoraPrecioMayorCorredor" 		as "HoraPrecioMayorCorredor"
								, ORD."MenorPrecioOperacionCorredor" 	as "MenorPrecioOperacionCorredor"
								, ORD."HoraPrecioMenorCorredor"			as "HoraPrecioMenorCorredor"
								, ORD."IdEmpleadoDigitador"  			as "IdEmpleadoDigitador"
								, ORD."FolioComprobanteFacturacion"		as "FolioComprobanteFacturacion"
								, ORD."FechaEjecucion"					as "FechaGeneracion"         
								,'' 									as "FolioExterno"
								,'PEN' 									as "IdEstadoNotificacion"
								,'PEN' 									as "IdEstadoCarta"		 
								--,v_fechaHoraSistema						as "FechaIngreso"
								,ORD."FechaIngreso"      				as "FechaIngreso"
								,CURRENT_UTCTIMESTAMP					as "FechaCreacion"     		
								,0 										as "IdUsuarioCreador"   
								,CASE COALESCE(IND."ValorIndicador", '1')
									WHEN '2' THEN v_FechaUltimoDiaHabilSemana
									WHEN '3' THEN v_FechaUltimoDiaHabilMes
									ELSE TO_DATE(v_FechaOperacion)
								END                                     as "FechaReferencia"
								from :v_Ordenes_Filtradas as ORD
								LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" TI
									ON TI."CodigoTipoIndicador" = 'CIRCFREC1985'
								LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" IND
									ON IND."IdEntidad" = ORD."Cuenta"
									AND IND."IdTipoIndicador" = TI."IdTipoIndicador";
					ELSE
					v_out_MensajeSalida='SIN OPERACIONES ENCONTRADAS';
					
					END IF;					
		
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
					'EJECORD',
					v_TipoTarea,
					p_IdSolicitudCircular,
					p_IdProceso,
					'TER',
					v_out_MensajeSalida,
					TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD'),
					CURRENT_UTCTIMESTAMP
					FROM DUMMY;		



END;
