SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarTransaccionIF";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de TRASACCION A FACTURAR IF 
--  renta variable
--  Autor: EAR


"BSCL.Homologacion.BD.Servicios::CargarTransaccionIF"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, R --REFRESCO
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
     IN  p_fechaCambio         NVARCHAR(20),   --'YYYY-MM-DD HH:MM:SS' 
    OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" NVARCHAR(40),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  NVARCHAR(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_ErroresContador		:= 0;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	

	-----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	   
	-- declaracion variables
	    DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	    DECLARE v_total_mov_cargadas DECIMAL(26,8):=0; 
	    DECLARE v_Quantity BIGINT:=0;
		DECLARE v_Ciclo BIGINT := 1;
		DECLARE v_FechaSistemaCreacion DATE;
		DECLARE v_IdTipoOperacion NVARCHAR(6);
		DECLARE v_IdCondicionDeLiquidacion NVARCHAR(2);
		DECLARE v_P_OUT_FECHA_LIQUIDACION NVARCHAR(10);
		DECLARE ARR_FechaSistemaCreacion     DATE ARRAY;
		DECLARE ARR_IdTipoOperacion          NVARCHAR ARRAY;
		DECLARE ARR_IdCondicionDeLiquidacion NVARCHAR ARRAY;
		DECLARE v_i BIGINT;
		DECLARE v_in table(
							"IdInterno"			BIGINT,
							"IdEmisor"			NVARCHAR(40),
							"Fecha"				NVARCHAR(10),
							"Plazo"				BIGINT,
							"Nemotecnico"		NVARCHAR(40),
							"IdTipoReajuste"	NVARCHAR(3),
							"PagaderoEn"        NVARCHAR(3)
						);
		DECLARE v_out_NemoSvs table (
								"IdInterno"		BIGINT,
								"NemoSVS"		NVARCHAR(40)
								);

		----------------------------------------------------
		-- Declaracion de Tabla
	    ----------------------------------------------------
	  	  	
        -- dependiendo del modo podemos borrar la informacion anterior cargada
	   IF P_Modalidad = 'R' THEN --REPROCESO
		 
		 DELETE FROM "BSCL.Orden.BD::ORD.Transaccion"
         WHERE "IdCarga" = 	p_IdCarga;
		 		 
	  END IF;
       
	        --CARGAR TRANSACCION IF
			INSERT INTO "BSCL.Homologacion.BD::HOM.Transaccion"
			(  "IdTransaccion" ,"Folio" ,"FechaSistemaCreacion" ,"HoraSistemaCreacion" ,"FechaOperacion" ,"FechaLiquidacion" ,"FechaEmision" ,"FechaVencimiento" 
				,"FechaIngresoRegistro" ,"IdSistemaBursatil" ,"EsOD" ,"EsCuentaPropia" ,"EsFungible" ,"EsAPlazo" ,"IdEmisor" ,"Cantidad" ,"Monto" ,"Precio" 
				,"Tasa" ,"Nemotecnico" ,"NemotecnicoSVS" ,"ValorRescate" ,"ValorCaptacion" ,"IdEstado" ,"IdTransaccionPlazo" ,"IdTransaccionOd" ,"IdTipoInstrumento" 
				,"IdClasificacionTipoInstrumento" ,"IdTipoReajuste" ,"IdEmpleado" ,"IdTipoOperacion" ,"IdCondicionDeLiquidacion" ,"IdMoneda" ,"IdEmpresa" 
				,"IdBolsa" ,"IdInstrumento" ,"NemotecnicoSubyacente" ,"IdInstrumentoSubyacente" ,"FolioCartera" ,"FechaParidad" ,"ValorParidad" ,"FechaTipoCambio" 
				,"ValorTipoCambio" ,"IdMonedaLiquidacion" ,"IdMonedaSubyacente" ,"EsVentaCorta" ,"Plazo" ,"FechaIngresoOferta" ,"IdCarga" ,"CodigoOperadorContraparte" 
				,"FechaModificacionOperacion" ,"MontoValorizado" ,"IdLibroOrdenes" ,"Reservado" ,"IdUsuarioReserva" ,"MotivoReserva" ,"Bloqueado" ,"IdUsuarioBloqueo" 
				,"MotivoBloqueo" ,"IdComprobanteTesoreria" ,"MarcaNegociacion" ,"IndicadorDCV" ,"IdCorredorLocal" ,"IdProgramaProcesaAutomatico" ,"EsBilateral" 
				,"IdCamara" ,"TipoCambioCorresponsal" ,"IdCorredorCorresponsal" ,"MontoLiquidacionCorresponsal" ,"IdMonedaLiquidacionCorresponsal" ,"IdCorredorContraparte" 
				,"CodigoOperadorCorredor" ,"MERCADO","FEC_TRANS","FOLIO_TRANS","IND_BOLSA","TIPO_OPERAC","FOLIO_SIMUL","IND_SIMUL","IND_OD","RUEDA","SEC_MOVTO_FAC"
				,"TIPO_REAJ"
			)
			SELECT 
			"BSCL.Orden.BD.Secuencias::Transaccion".NEXTVAL  as "IdTransaccion" 
			, GES."FOLIO_TRANS" as "Folio" 
			, GES."FECHA" as "FechaSistemaCreacion" 
			, SUBSTRING (GES."FEC_TRANS", 12, 8) as "HoraSistemaCreacion" 
			, CONCAT (GES."FECHA", CONCAT(' ',SUBSTRING (GES."FEC_TRANS", 12, 8))) as "FechaOperacion" 
			, GES."FEC_VCTO_TP" as "FechaLiquidacion" 
			, GES."FEC_EMI" as "FechaEmision" 
			, GES."FEC_VCTO" as "FechaVencimiento" 
			, GES."FEC_DIG"  as "FechaIngresoRegistro" 
			, 0 as "IdSistemaBursatil" 
			, CASE WHEN GES."IND_OD" = 'OD' OR GES."IND_OD" = 'OE' THEN 'S'
									     ELSE 'N'
									END as "EsOD" 
			, GES."IND_CTA_PRO" as "EsCuentaPropia" 
			, GES."IND_FUNGIBILIDAD" as "EsFungible" 
			, CASE WHEN GES."IND_LIQUID" ='TP' THEN 'S'
									     ELSE 'N'
									END as "EsAPlazo" 
			, TRIM(GES."EMISOR") as "IdEmisor" 
			, CASE WHEN GES."TIPO_OPERAC" = 'C' then GES."CAN_COM" else GES."CAN_VEN" end as "Cantidad" 
			, GES."MONTO" as "Monto" 
			, 0 as "Precio" 
			, GES."TASA" as "Tasa" 
			, trim(GES."NEMO") as "Nemotecnico" 
			, '' as "NemotecnicoSVS" 
			, GES."VAL_RESC" as "ValorRescate" 
			, 0 as "ValorCaptacion" 
			, 'ING' as "IdEstado" 
			, 0 as "IdTransaccionPlazo" 
			, 0 as "IdTransaccionOd" 
			, i."IdTipoInstrumento" as "IdTipoInstrumento" 
			, i."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento" 
			, TipoReajuste."ValorDestino" as "IdTipoReajuste" 
			, 0 as "IdEmpleado" 
			, CASE WHEN GES."TIPO_OPERAC" = 'C' THEN 'COMIIF' ELSE 'VENIIF' END as "IdTipoOperacion" 
			, CASE WHEN GES."IND_LIQUID" = 'PH' THEN 'T0'
									     WHEN GES."IND_LIQUID" = 'PM' THEN 'T1'
									     WHEN GES."IND_LIQUID" = 'CN' THEN 'T2'
									     WHEN GES."IND_LIQUID" = 'T3' THEN 'T3'
									     ELSE 'T0'--revisar      
						  END as "IdCondicionDeLiquidacion" 
			, CASE WHEN (GES."TIPO_REAJ" = 'IV') THEN 'UF' 
						                                     ELSE
						                                        CASE WHEN (GES."TIPO_REAJ" = 'NR') THEN 'CLP'
						                                        ELSE CASE WHEN GES."TIPO_REAJ" ='US' THEN 'USD'
						                                             	ELSE GES."TIPO_REAJ"
						                                             END
						                                        END
						                                     END  as "IdMoneda" 
			, p_IdEmpresa as "IdEmpresa" 
			, GES."IND_BOLSA" as "IdBolsa" 
			, i."IdInstrumento" as "IdInstrumento" 
			, '' as "NemotecnicoSubyacente" 
			, 0 as "IdInstrumentoSubyacente" 
			, 0 as "FolioCartera" 
			, '0001-01-01' as "FechaParidad" 
			, 0 as "ValorParidad" 
			, '0001-01-01' as "FechaTipoCambio" 
			, 0 as "ValorTipoCambio" 
			, CASE WHEN (GES."TIPO_REAJ" = 'UF' OR GES."TIPO_REAJ" = 'IV' OR GES."TIPO_REAJ" = 'NR') 
						   THEN 'CLP' 
						  ELSE CASE WHEN GES."TIPO_REAJ" ='US' THEN 'USD'
						        ELSE GES."TIPO_REAJ"
						       END
						  END as "IdMonedaLiquidacion" 
			, '' as "IdMonedaSubyacente" 
			, '' as "EsVentaCorta" 
			, GES."PLAZO" as "Plazo" 
			, CASE WHEN GES."TIPO_OPERAC" = 'C'  then
						                                        CASE when trim(GES."FCREOF_COM") = '' then '0001-01-01' else substring(to_varchar(GES."FCREOF_COM"),1,10) ||' '||substring(to_varchar(GES."FCREOF_COM"),12,26) end 
						                                     else
						                                        CASE when trim(GES."FCREOF_VEN") = '' then '0001-01-01' else substring(to_varchar(GES."FCREOF_COM"),1,10) ||' '||substring(to_varchar(GES."FCREOF_COM"),12,26) end  
						                                     end as "FechaIngresoOferta" 
			, p_IdCarga as "IdCarga" 
			, CASE WHEN GES."IND_OD" = 'OD' THEN GES."COD_OPE_EXT"
										 ELSE 0
						  END  as "CodigoOperadorContraparte" 
			, '0001-01-01' as "FechaModificacionOperacion" 
			, GES."MONTO" as "MontoValorizado" 
			, 0 as "IdLibroOrdenes" 
			, 0 as "Reservado" 
			, 0 as "IdUsuarioReserva" 
			, '' as "MotivoReserva" 
			, '' as "Bloqueado" 
			, 0 as "IdUsuarioBloqueo" 
			, '' as "MotivoBloqueo" 
			, 0 as "IdComprobanteTesoreria" 
			, '' as "MarcaNegociacion" 
			, '' as "IndicadorDCV" 
			, 0 as "IdCorredorLocal" 
			, 0 as "IdProgramaProcesaAutomatico" 
			, '' as "EsBilateral" 
			, 0 as "IdCamara" 
			, 0 as "TipoCambioCorresponsal" 
			, 0 as "IdCorredorCorresponsal" 
			, 0 as "MontoLiquidacionCorresponsal" 
			, '' as "IdMonedaLiquidacionCorresponsal" 
			, 0 as "IdCorredorContraparte" 
			, '' as "CodigoOperadorCorredor"  
			, 'IF' as "MERCADO"						
			, GES."FEC_TRANS" as "FEC_TRANS"                      
			, GES."FOLIO_TRANS" as "FOLIO_TRANS"						
			, GES."IND_BOLSA" as "IND_BOLSA"                      
			, GES."TIPO_OPERAC" as "TIPO_OPERAC" 						
			, GES."FOLIO_SIMUL" as "FOLIO_SIMUL"                    
			, GES."IND_SIMUL" as "IND_SIMUL"                      
			, GES."IND_OD" as "IND_OD"							
			, GES."RUEDA" as "RUEDA"							
			, GES."SEC_MOVTO_FAC" as "SEC_MOVTO_FAC"  
			, GES."TIPO_REAJ" AS "TIPO_REAJ"                
			from "BSCL_VIRTUAL"."GES_TBPLTBIF" as GES 
			inner join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as i 
				on i."Nemotecnico" = trim(GES."NEMO")                    	 
			left join "BSCL.Homologacion.BD::HOM.Homologacion" as Homologacion
		  		on Homologacion."TipoValor" = 'TipoIntrumento'
		  		and Homologacion."Entidad"  = 'FACTURA'
		  		and Homologacion."ValorOrigen" ='IF'
		  	left join "BSCL.Homologacion.BD::HOM.Homologacion" as TipoReajuste
		  		on TipoReajuste."TipoValor" = 'ReajusteTransaccionIF'
		  		and TipoReajuste."Entidad"  = 'ReajusteTransaccionIF'
		  		and TipoReajuste."ValorOrigen" = GES."TIPO_REAJ"
		  	left join "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" as HOM_TRAN
		  		on HOM_TRAN."FEC_TRANS"   = GES."FEC_TRANS"
		  		and HOM_TRAN."FOLIO_TRANS" = GES."FOLIO_TRANS"
		  		and HOM_TRAN."TIPO_OPERAC" = GES."TIPO_OPERAC"
		  		and HOM_TRAN."IND_BOLSA" = GES."IND_BOLSA"
		  		and HOM_TRAN."MERCADO" ='IF'
		  	WHERE GES."FEC_DIG" BETWEEN p_FechaInicial AND p_FechaFinal
		  	AND HOM_TRAN."FOLIO_TRANS" IS NULL;
                
                    
            select count(*) into v_total_mov_gestion 
            from "BSCL_VIRTUAL"."GES_TBPLTBIF"
            WHERE "FEC_DIG" BETWEEN p_FechaInicial AND p_FechaFinal;
            
          ---------------------------------
          -- ahora recupero el nemo svs ---
          ---------------------------------

          v_in = SELECT HOM."IdTransaccion" as "IdInterno"
                       ,ifnull(trim(HOM."IdEmisor"),'') as "IdEmisor"
                       ,HOM."FechaEmision" as "Fecha"
                       ,HOM."Plazo"
                       ,HOM."Nemotecnico"
                       ,TIPO_REAJUSTE."ValorDestino" as "IdTipoReajuste"
                       ,TIPO_REAJUSTE."ValorOrigen2" as "PagaderoEn"
                 FROM "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" AS HOM
			     INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_REAJUSTE
			     	ON TIPO_REAJUSTE."TipoValor" = 'ReajusteTransaccionIF'
			     	AND TIPO_REAJUSTE."ValorOrigen" = HOM."TIPO_REAJ"
                 WHERE "IdCarga" = p_IdCarga 
                   AND "MERCADO" = 'IF';

          --------------------------------------------------
          -- llamamos a servicio que genera los nemos svs  -
          --------------------------------------------------
   
          call "BSCL"."BSCL.Plataforma.BD.Servicios::ObtenerNemoSvsMM" (:v_in,v_out_NemoSvs);
 
          ---------------------------------
          -- actualizamos los nemos svs  --
          ---------------------------------

          update TRA SET TRA."NemotecnicoSVS" = coalesce(SVS."NemoSVS",'') 
		  from "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" as TRA
		  inner join :v_out_NemoSvs  as SVS
		     on SVS."IdInterno" = TRA."IdTransaccion";

	       -------------------------------
	       -- ahora las transacciones od 
	       -------------------------------       
	       update TRA set TRA."IdTransaccionOd" = TRA2."IdTransaccion"
	                     ,TRA."IdTransaccionPlazo" = 0
	       from "BSCL.Homologacion.BD::HOM.Transaccion" as TRA
	           ,"BSCL.Homologacion.BD::HOM.Transaccion" as TRA2
	       where TRA."IND_SIMUL" = ''
	       and   TRA."IND_OD" in ('OD','OE')
	       and   TRA."FOLIO_SIMUL" = ''
	       and   TRA."MERCADO" = 'IF'
	       and   TRA."FOLIO_TRANS" = TRA2."FOLIO_TRANS"
	       and   TRA."IND_BOLSA" = TRA2."IND_BOLSA"
	       and   TRA."Nemotecnico"  = TRA2."Nemotecnico"
	       and   TRA."TIPO_OPERAC" = CASE WHEN TRA2."TIPO_OPERAC" = 'V' then 'C' else 'V' end
	       and   TRA."IdCarga" = p_IdCarga;      
	
     	SELECT COUNT("IdTransaccion") INTO v_Quantity
		FROM "BSCL.Homologacion.BD::HOM.Transaccion";
		
		-- debe existir el instrumento
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'TRANSACCIONESIF' as "Entidad"
                    ,'NEMOTECNICO de la TRANSACCION IF no existe en OPTIMUS' as "Mensaje"
                    , '' as "NombreCampoGenerico1"
                    , 'IF' as "ValorCampoGenerico1" 
                    , 'FEC_TRANS' as "NombreCampoGenerico2" 
                    , GES."FEC_TRANS" as "ValorCampoGenerico2"
                    , 'IND_BOLSA' as "NombreCampoGenerico3"
                    , GES."IND_BOLSA" as "ValorCampoGenerico3"
                    , 'TIPO_OPERAC' as "NombreCampoGenerico4"
                    , GES."TIPO_OPERAC" as "ValorCampoGenerico4"
                    , 'NEMO' as "NombreCampoGenerico5"
                    , GES."NEMO" as "ValorCampoGenerico5"
                    ,'FOLIO_TRANS' as "NombreCampoGenerico6"
                    , GES."FOLIO_TRANS" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              from "BSCL_VIRTUAL"."GES_TBPLTBIF" as GES 
			  left join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as i 
				on i."Nemotecnico" = trim(GES."NEMO")
			  where i."Nemotecnico" IS NULL;

		IF (:v_Quantity > 0) THEN 
		
            select count(*) into v_total_mov_cargadas
            from  "BSCL.Homologacion.BD::HOM.Transaccion"
            where "IdCarga" = p_IdCarga
             and "MERCADO" = 'IF';
                
            p_ResumenDatosProcesados =  select 'CARGATRANSACCION' as "CodigoProceso" 
             									,'ORD' as "Modulo" 
             									,v_total_mov_gestion as "TotalRegistrosSebra"
             									,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
       							   		 union all
       							    	 select * from :p_ResumenDatosProcesados;  

			--ACTUALIZAR FECHA DE LIQUIDACION
			ref_transaccion = SELECT DISTINCT "FechaSistemaCreacion", "IdTipoOperacion", "IdCondicionDeLiquidacion"
			                  FROM "BSCL.Homologacion.BD::HOM.Transaccion"
			                  where "IdCarga" = p_IdCarga
			                   and "MERCADO"  = 'IF'
			                   and "EsAPlazo" = 'N';
			
			ARR_FechaSistemaCreacion     := ARRAY_AGG(:ref_transaccion."FechaSistemaCreacion" ORDER BY "FechaSistemaCreacion" DESC);
			ARR_IdTipoOperacion          := ARRAY_AGG(:ref_transaccion."IdTipoOperacion" ORDER BY "FechaSistemaCreacion" DESC);
			ARR_IdCondicionDeLiquidacion := ARRAY_AGG(:ref_transaccion."IdCondicionDeLiquidacion" ORDER BY "FechaSistemaCreacion" DESC);
	
			 FOR v_i IN 1 .. CARDINALITY(:ARR_FechaSistemaCreacion) DO 
				v_FechaSistemaCreacion := :ARR_FechaSistemaCreacion[:v_i];
				v_IdTipoOperacion := :ARR_IdTipoOperacion[:v_i];
				v_IdCondicionDeLiquidacion := :ARR_IdCondicionDeLiquidacion[:v_i];
			
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion"(:v_FechaSistemaCreacion,
																			   :v_IdTipoOperacion,
																			   :v_IdCondicionDeLiquidacion,
																			   1,
																			   v_P_OUT_FECHA_LIQUIDACION);
				UPDATE "BSCL.Homologacion.BD::HOM.Transaccion" SET "FechaLiquidacion" = v_P_OUT_FECHA_LIQUIDACION
				WHERE "FechaSistemaCreacion" = :v_FechaSistemaCreacion
				  AND "IdTipoOperacion" = :v_IdTipoOperacion
				  AND "IdCondicionDeLiquidacion" = :v_IdCondicionDeLiquidacion
				  AND "IdCarga" = p_IdCarga;																				  			
			END FOR; 
           
		END IF;
       
		--------------------
		--FIN SP 
		--------------------
	END;
END;
