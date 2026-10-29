SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOrdenesRV_Insert";
CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de ORDENES
--  renta variable
--  Autor: - EAR

 "BSCL.Homologacion.BD.Servicios::CargarOrdenesRV_Insert"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_fechaCambio         NVARCHAR(20)   --'YYYY-MM-DD HH:MM:SS' 	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
      DECLARE v_GrupoCobro bigint;

			   SELECT "BSCL.Orden.BD.Secuencias::GrupoCobroOrden".NEXTVAL INTO v_GrupoCobro FROM DUMMY;
			   INSERT INTO "BSCL.Homologacion.BD::HOM.Orden"							
			   ( "IdOrden","Folio","FechaRecepcion","FechaIngresoRegistro","FechaIngresoRegistroCorta","FechaSolicitudAnulacion","FechaAnulacion"
				,"FechaOperacion","FechaVigencia","FechaVencimiento","FechaEmision","FechaLiquidacion","FechaAceptacion","FechaAutorizacion"
				,"Glosa","IdEstadoOrden","Cantidad","Tasa","Plazo","ValorRescate","IdTipoReajuste","PrecioOrden","PrecioMercado","TasaAgente"
				,"IndicadorFacturacionInmediata","ValorCaptacion","IdCondicionDeLiquidacion","IdTipoOperacion","IdCliente","IdPortafolioCliente"
				,"IdTipoCanalIngreso","IdLibroOrdenes","IdEmisor","IdEmpresa","IdBolsa","IdBolsaDistribucion","EspecificaBolsa"
				,"IdTipoCanalDistribucion","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdProducto","Nemotecnico"
				,"IdGrupoCobro","IdCarga","EsVentaCorta","SecOrdenMigracion","Reservado","IdUsuarioReserva","MotivoReserva"
				,"Bloqueado","IdUsuarioBloqueo","MotivoBloqueo","IdInstrumentoSubyacente","IdEmpleadoPortafolio","IdEmpleadoDigitador","IdTipoPrecio"
				,"IdTipoTasa","IdPorcentajeVisibilidad","IdTipoVigenciaOrden","IdMonedaOrden","IdMonedaLiquidacionOrden","MontoLocal","MontoOrden"
				,"MontoLiquidacion","TipoCambio","IdTipoCustodia","IdFormaCustodia","sol_sec","ord_sec","ord_folio","RUT_CLI","SEC_RUT_CLI"                          
				,"TIPO_OPERAC","ope_id","MERCADO","sol_tipo_gasto","sol_tipo_comision","sol_tipo_derecho","sol_val_gastos","sol_val_comision"
				,"sol_val_derechos","orv_mto_gastos","orv_mto_comision","orv_mto_derechos","sol_ind_vta_corta","sol_ind_simul"
			   ) 							
			   SELECT "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL																	 as "IdOrden" 							
						,0 																		 									 as "Folio"							
						,CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN SOLI_ANT."sol_fecha_digitacion" ELSE p_fechaCambio END as "FechaRecepcion" 					
						,CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN SOLI_ANT."sol_fecha_digitacion" ELSE p_fechaCambio END as "FechaIngresoRegistro"				
						,CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN TO_VARCHAR(TO_DATE(SOLI_ANT."sol_fecha_digitacion"),'YYYY-MM-DD') ELSE TO_VARCHAR(TO_DATE(p_fechaCambio),'YYYY-MM-DD') END as "FechaIngresoRegistroCorta"		
						,'0001-01-01' 																								 as "FechaSolicitudAnulacion"			
						,'0001-01-01'																								 as "FechaAnulacion" 					
						, CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN OPE_ANT."orv_fec"  ELSE p_fechaCambio END 			 as "FechaOperacion"					
						, CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' THEN TO_VARCHAR(TO_DATE(OPE_ANT."orv_fec_permanencia"),'YYYY-MM-DD')
						  ELSE 
						      CASE WHEN SOLI_ANT."sol_ind_permanencia" = 'D' THEN p_fechaCambio
						      ELSE ADD_DAYS(TO_DATE(TO_CHAR(p_fechaCambio,'YYYY-MM-DD')),DAYS_BETWEEN(SUBSTRING(ORD_ANT."ord_fec",1,10),SUBSTRING(ORD_ANT."ord_fec_permanencia",1,10)))
						      END
						  END																										as "FechaVigencia" 					
						, TO_VARCHAR(TO_DATE(CASE WHEN OPE_ANT."orv_fec_vcto" = '1900-01-01 0:00:00.0' THEN '0001-01-01' ELSE OPE_ANT."orv_fec_vcto" END),'YYYY-MM-DD') as "FechaVencimiento"-- se ocupa simultanea  				
						, '0001-01-01'																								as "FechaEmision" 					
						, CASE WHEN p_fechaCambio = '0001-01-01 00:00:00' 
						       THEN TO_VARCHAR(TO_DATE(OPE_ANT."orv_fec_liquidacion"),'YYYY-MM-DD')
						       ELSE 
						       ADD_DAYS(TO_DATE(TO_CHAR(p_fechaCambio,'YYYY-MM-DD')),CASE WHEN SOLI_ANT."sol_ind_liquid"   = 'PH' THEN 0
																						  WHEN SOLI_ANT."sol_ind_liquid"   = 'PM' THEN 1
							                                                              WHEN SOLI_ANT."sol_ind_liquid"   = 'CN' THEN 2
							                                                              WHEN SOLI_ANT."sol_ind_liquid"   = 'T3' THEN 3
							                                                              ELSE 0
						                                                              END)
						 END																				        			    as "FechaLiquidacion" 				
						, '0001-01-01'																								as "FechaAceptacion" 					
						, '0001-01-01'																								as "FechaAutorizacion" 				
						, SOLI_ANT."sol_glosa"																						as "Glosa" 							
						, 'AUT'																										as "IdEstadoOrden" 					
						, OPE_ANT."orv_cant_total"																					as "Cantidad" 						
						, 0																											as "Tasa" 							
						, 0																											as "Plazo" 							
						, 0																											as "ValorRescate" 					
						, COALESCE(Instrumento."IdMoneda",'')																		as "IdTipoReajuste" 					
						, OPE_ANT."orv_val_tipo_precio"																				as "PrecioOrden" 						
						, 0																											as "PrecioMercado" 					
						, OPE_ANT."orv_tasa_operacion"																				as "TasaAgente" 						
						, OPE_ANT."orv_ind_factura_imnediata"																		as "IndicadorFacturacionInmediata" 	
						, 0																											as "ValorCaptacion" 					
						, CASE WHEN SOLI_ANT."sol_ind_liquid" = 'PH' THEN 'T0'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'PM' THEN 'T1'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'CN' THEN 'T2'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'T3' THEN 'T3'
							ELSE 'T0'
						  END 																										as "IdCondicionDeLiquidacion" 		
						,CASE WHEN ORD_ANT."ord_tipo_operac" = 'C' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'COMSIM'
			  				  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'VENSIM'
			    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'COMFUT' 
			     			  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'VENFUT'                                  
			    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' THEN  CONCAT ('COM',TIPO_INS."ValorDestino")
			   				  WHEN ORD_ANT."ord_tipo_operac" = 'V' THEN  CONCAT ('VEN',TIPO_INS."ValorDestino")
						END  																										as "IdTipoOperacion" 					
						,COALESCE(Cliente."IdCliente",0)																			as "IdCliente" 						
						,COALESCE(Portafolio."IdPortafolio",0)																		as "IdPortafolioCliente" 				
						,SOLI_ANT."cca_codigo" 																						as "IdTipoCanalIngreso" 				
						,COALESCE(ConsultarLibroOrdenes."IdLibroOrdenes",0)															as "IdLibroOrdenes" 					
						,COALESCE(Instrumento."IdEmisor",'')																		as "IdEmisor" 						
						,p_IdEmpresa 																								as "IdEmpresa" 						
						,SOLI_ANT."sol_bolsa_destino"																				as "IdBolsa" 							
						,SOLI_ANT."sol_bolsa_destino"																				as "IdBolsaDistribucion"				
						,'S'																										as "EspecificaBolsa"					
						,SOLI_ANT."cca_codigo"																						as "IdTipoCanalDistribucion"			
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN COALESCE(i2."IdInstrumento",0) ELSE COALESCE(Instrumento."IdInstrumento",0) END  	as "IdInstrumento" 					
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN 'SIM' ELSE COALESCE(Instrumento."IdTipoInstrumento",'') END as "IdTipoInstrumento" 				
						,COALESCE(TipoInstrumento."IdClasificacionTipoInstrumento",'')												as "IdClasificacionTipoInstrumento" 	
						,''																											as "IdProducto" 						
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN COALESCE(i2."Nemotecnico",'') ELSE COALESCE(Instrumento."Nemotecnico",'') END 	as "Nemotecnico" 										
						,v_GrupoCobro																								as "IdGrupoCobro" 					
						,p_IdCarga																									as "IdCarga"                          
						,SOLI_ANT."sol_ind_vta_corta"																				as "EsVentaCorta"							
						, ORD_ANT."ord_sec"																							as "SecOrdenMigracion"                
						, 'N'																										as "Reservado"	    				
						, 0																											as "IdUsuarioReserva" 				
						, ''																										as "MotivoReserva"					
						, 'N'																										as "Bloqueado"		   				
						, 0																											as "IdUsuarioBloqueo" 				
						, ''																										as "MotivoBloqueo"					
						, CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN COALESCE(Instrumento."IdInstrumento",0) ELSE 0 END  		as "IdInstrumentoSubyacente"          
						, 0																											as "IdEmpleadoPortafolio"				
						, 0																											as "IdEmpleadoDigitador" 				
						, OPE_ANT."orv_tipo_precio"																					as "IdTipoPrecio" 					
						, ''																										as "IdTipoTasa" 						
						, 100																										as "IdPorcentajeVisibilidad" 			
						, CASE WHEN SOLI_ANT."sol_ind_permanencia" = 'P' THEN 'OP'
							   WHEN SOLI_ANT."sol_ind_permanencia" = 'D' THEN 'SH'
						 END																										as "IdTipoVigenciaOrden" 				
						,SOLI_ANT."mon_id"																							as "IdMonedaOrden"					
						,SOLI_ANT."mon_id"																							as "IdMonedaLiquidacionOrden"			
						,0																											as "MontoLocal" 						
						,OPE_ANT."orv_mto_bruto"																					as "MontoOrden" 						
						,OPE_ANT."orv_mto_bruto"																					as "MontoLiquidacion"					
						,0																											as "TipoCambio"						
						,OPE_ANT."orv_tra_boveda"																					as "IdTipoCustodia" 					
						,CASE WHEN SOLI_ANT."sol_ind_tit_cus" = 'C' THEN 'CUS'
							  ELSE 'NOM'
						 END																										as "IdFormaCustodia" 					
						,SOLI_ANT."sol_sec"																							as "sol_sec"							
						,ORD_ANT."ord_sec"																							as "ord_sec"							
						,ORD_ANT."ord_folio"																						as "ord_folio"						
						,SOLI_ANT."RUT_CLI"																							as "RUT_CLI"							
						,SOLI_ANT."CUENTA"																							as "SEC_RUT_CLI"                      
						,ORD_ANT."ord_tipo_operac" 																					as "TIPO_OPERAC" 						
						,SOLI_ANT."ope_id" 																							as "ope_id"							
						,ORD_ANT."MERCADO" 																							as "MERCADO"							
						,SOLI_ANT."sol_tipo_gasto" 																					as "sol_tipo_gasto" 					
						,SOLI_ANT."sol_tipo_comision" 																				as "sol_tipo_comision" 				
						,SOLI_ANT."sol_tipo_derecho" 	 																			as "sol_tipo_derecho" 				
						,SOLI_ANT."sol_val_gastos" 	 																				as "sol_val_gastos" 					
						,SOLI_ANT."sol_val_comision" 																				as "sol_val_comision" 				
						,SOLI_ANT."sol_val_derechos" 																				as "sol_val_derechos" 				
						,OPE_ANT."orv_mto_gastos" 																					as "orv_mto_gastos" 					
						,OPE_ANT."orv_mto_comision"																					as "orv_mto_comision" 				
						,OPE_ANT."orv_mto_derechos"																					as "orv_mto_derechos"					
						,SOLI_ANT."sol_ind_vta_corta"																				as "sol_ind_vta_corta"				
						,SOLI_ANT."sol_ind_simul" 																					as "sol_ind_simul"					       
				FROM "BSCL_VIRTUAL"."GES_OPERACION_RV" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >=p_FechaInicial
					--AND SOLI_ANT."sol_fecha" < ADD_DAYS(TO_DATE (p_FechaFinal, 'YYYY-MM-DD'), 1)
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					--ON Instrumento."Nemotecnico" = OPE_ANT."orv_nemo"
					ON trim(Instrumento."Nemotecnico") = trim(OPE_ANT."orv_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(OPE_ANT."RUT_CLI")
   				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as PER
    				 ON PER."IdDocumentoIdentidad" = DOC."IdDocumentoIdentidad"		
				INNER JOIN  "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" as Cliente
    				ON Cliente."IdPersona" = PER."IdPersona"
    			INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio 
					ON Portafolio."IdCliente" = Cliente."IdCliente"
					AND Portafolio."CodigoPortafolio" = TO_VARCHAR(CUENTA."CUENTA")	
				LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenesSegmento"  as ConsultarLibroOrdenes 
					ON ConsultarLibroOrdenes."IdSegmentoCliente" = Cliente."IdSegmentoCliente"
					AND ConsultarLibroOrdenes."IdEmpresa" = p_IdEmpresa
			    left join "BSCL.Plataforma.BD::INS.TipoOperacionTipoInstrumento" as i2
	       		on i2."IdTipoOperacion" = case when (SOLI_ANT."sol_ind_simul" = 'TS' and ORD_ANT."ord_tipo_operac" = 'C' ) then 'COMSIM'  
	       	                                   when (SOLI_ANT."sol_ind_simul" = 'TS'  and ORD_ANT."ord_tipo_operac" = 'V' ) then 'VENSIM'
	       	                                  else ''
	        	                          end		               	 
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_INS
		  			ON TIPO_INS."TipoValor" = 'TipoIntrumento'
		  			AND TIPO_INS."Entidad"  = 'FACTURA'
		  			AND TIPO_INS."ValorOrigen" = SOLI_ANT."MERCADO"
		  		LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
		  			ON GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
		  			AND GRUPO_INS."IdTipoInstrumento" = TIPO_INS."ValorDestino"
		  		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as SAP_TIP_OPE
		  			ON SAP_TIP_OPE."IdTipoOperacion" =	CASE WHEN  ORD_ANT."ord_tipo_operac" = 'C' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'COMSIM'
											  				  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'VENSIM'
											    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'COMFUT' 
											     			  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'VENFUT'                                  
											    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' THEN  CONCAT ('COM',TIPO_INS."ValorDestino")
											   				  WHEN ORD_ANT."ord_tipo_operac" = 'V' THEN  CONCAT ('VEN',TIPO_INS."ValorDestino")
				 										END
				 LEFT JOIN "BSCL.Homologacion.BD::HOM.Orden" AS HOM_ORD
                  ON HOM_ORD."sol_sec" = OPE_ANT."sol_sec"
                 WHERE HOM_ORD."sol_sec" IS NULL
                   AND OPE_ANT."orv_fec" >=p_FechaInicial
				   AND OPE_ANT."orv_fec" < ADD_DAYS(TO_DATE (p_FechaFinal, 'YYYY-MM-DD'), 1);														
						
	
END;

 
