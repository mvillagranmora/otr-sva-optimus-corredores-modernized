SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOrdenesRF_Insert";
CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de ORDENES
--  renta variable
--  Autor: - EAR

 "BSCL.Homologacion.BD.Servicios::CargarOrdenesRF_Insert"(
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
			   SELECT "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL	as "IdOrden" 							
					   ,0 as "Folio"							
					   ,OPE_ANT."orf_fec_orden" as "FechaRecepcion" 					
                       ,OPE_ANT."orf_fec_orden" as "FechaIngresoRegistro"				
					   ,TO_VARCHAR(TO_DATE(OPE_ANT."orf_fec_orden"),'YYYY-MM-DD') as "FechaIngresoRegistroCorta"		
					   ,'0001-01-01' as "FechaSolicitudAnulacion"			
					 	,'0001-01-01' as "FechaAnulacion" 					
						,TO_VARCHAR(TO_DATE(OPE_ANT."orf_fec_orden"),'YYYY-MM-DD')  as "FechaOperacion"					
						,TO_VARCHAR(TO_DATE(OPE_ANT."orf_fec_orden"),'YYYY-MM-DD') as "FechaVigencia" 					
						,TO_VARCHAR(TO_DATE(CASE WHEN OPE_ANT."orf_fec_vcto" = '1900-01-01 0:00:00.0' THEN '0001-01-01' ELSE OPE_ANT."orf_fec_vcto" END),'YYYY-MM-DD') as "FechaVencimiento"-- se ocupa simultanea  				
						,'0001-01-01' as "FechaEmision" 					
						,TO_VARCHAR(TO_DATE(CASE WHEN OPE_ANT."orf_fec_liquidacion" = '1900-01-01 0:00:00.0' THEN '0001-01-01' ELSE OPE_ANT."orf_fec_liquidacion" END),'YYYY-MM-DD') as "FechaLiquidacion"
						,'0001-01-01' as "FechaAceptacion" 					
						,'0001-01-01' as "FechaAutorizacion" 				
						, SOLI_ANT."sol_glosa" as "Glosa" 							
						, 'AUT'	as "IdEstadoOrden" 					
						, OPE_ANT."orf_cant_total" as "Cantidad" 						
						, OPE_ANT."orf_tir" as "Tasa" 							
						, 0 as "Plazo" 							
						, 0 as "ValorRescate" 					
						, TipoReajuste."ValorDestino" as "IdTipoReajuste" 					
						, 0 as "PrecioOrden" 						
						, 0	as "PrecioMercado" 					
						, 0 as "TasaAgente" 						
						, '' as "IndicadorFacturacionInmediata" 	
						, 0	as "ValorCaptacion" 					
						, CASE WHEN SOLI_ANT."sol_ind_liquid" = 'PH' THEN 'T0'
											WHEN SOLI_ANT."sol_ind_liquid"   = 'PM' THEN 'T1'
											WHEN SOLI_ANT."sol_ind_liquid"   = 'CN' THEN 'T2'
											WHEN SOLI_ANT."sol_ind_liquid"   = 'T3' THEN 'T3'
											ELSE 'T0'
						 END as "IdCondicionDeLiquidacion" 		
						,CASE WHEN ORD_ANT."ord_tipo_operac" = 'C' THEN 'COMRF' ELSE 'VENRF' END as "IdTipoOperacion"
						,COALESCE(Cliente."IdCliente",0) as "IdCliente" 						
						,COALESCE(Portafolio."IdPortafolio",0)	as "IdPortafolioCliente" 				
						,SOLI_ANT."cca_codigo" as "IdTipoCanalIngreso" 				
						,COALESCE(ConsultarLibroOrdenes."IdLibroOrdenes",0) as "IdLibroOrdenes" 					
						,'' as "IdEmisor" 						
						,p_IdEmpresa as "IdEmpresa" 						
						,SOLI_ANT."sol_bolsa_destino" as "IdBolsa" 							
						,SOLI_ANT."sol_bolsa_destino" as "IdBolsaDistribucion"				
						,'S' as "EspecificaBolsa"					
						,SOLI_ANT."cca_codigo" as "IdTipoCanalDistribucion"			
						,Instrumento."IdInstrumento" as "IdInstrumento" 					
						,Instrumento."IdTipoInstrumento" as "IdTipoInstrumento" 				
						,TipoInstrumento."IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento" 	
						,SAP_TIP_OPE."IdTipoProducto" as "IdProducto"  						
						,Instrumento."Nemotecnico"	as "Nemotecnico" 									
						 ,v_GrupoCobro	as "IdGrupoCobro" 					
						 ,p_IdCarga	as "IdCarga"                          
						 ,'N' as "EsVentaCorta"							
						 , ORD_ANT."ord_sec" as "SecOrdenMigracion"                
						 , 'N' as "Reservado"	    				
						 , 0 as "IdUsuarioReserva" 				
						 , '' as "MotivoReserva"					
						 , 'N'as "Bloqueado"		   				
						 , 0 as "IdUsuarioBloqueo" 				
						 , '' as "MotivoBloqueo"					
						 , 0 as "IdInstrumentoSubyacente"          
						 , 0 as "IdEmpleadoPortafolio"				
						 , 0 as "IdEmpleadoDigitador" 				
						 , '' as "IdTipoPrecio" 					
						 , '' as "IdTipoTasa" 						
					     , 100 as "IdPorcentajeVisibilidad" 			
						 , CASE WHEN SOLI_ANT."sol_ind_permanencia" = 'P' THEN 'OP'
						     WHEN SOLI_ANT."sol_ind_permanencia" = 'D' THEN 'SH'
						   END as "IdTipoVigenciaOrden" 				
						,CASE WHEN (OPE_ANT."tipo_reaj" = 'IV') THEN 'UF' 
			                                     ELSE
			                                        CASE WHEN (OPE_ANT."tipo_reaj" = 'NR') THEN 'CLP'
			                                        ELSE OPE_ANT."tipo_reaj" 
			                                        END
			                                     END  as "IdMonedaOrden" 		
						, CASE WHEN (OPE_ANT."tipo_reaj" = 'UF' OR OPE_ANT."tipo_reaj" = 'IV' OR OPE_ANT."tipo_reaj" = 'NR') 
						   THEN 'CLP' 
						  ELSE  OPE_ANT."tipo_reaj" 
						  END as "IdMonedaLiquidacionOrden"	
						,0 as "MontoLocal" 						
						,OPE_ANT."orf_mto_total" as "MontoOrden" 						
						,OPE_ANT."orf_mto_total" as "MontoLiquidacion"					
						,0 as "TipoCambio"						
						,OPE_ANT."orf_tra_boveda" as "IdTipoCustodia" 					
						,CASE WHEN SOLI_ANT."sol_ind_tit_cus" = 'C' THEN 'CUS'
							  ELSE 'NOM'
						 END as "IdFormaCustodia" 					
						,SOLI_ANT."sol_sec"	as "sol_sec"							
						,ORD_ANT."ord_sec" as "ord_sec"							
						,ORD_ANT."ord_folio" as "ord_folio"						
						,SOLI_ANT."RUT_CLI" as "RUT_CLI"							
						,SOLI_ANT."CUENTA" as "SEC_RUT_CLI"                      
						,ORD_ANT."ord_tipo_operac" as "TIPO_OPERAC" 						
						,SOLI_ANT."ope_id" as "ope_id"							
						,'RF' as "MERCADO"							
						,SOLI_ANT."sol_tipo_gasto" as "sol_tipo_gasto" 					
						,SOLI_ANT."sol_tipo_comision" as "sol_tipo_comision" 				
						,SOLI_ANT."sol_tipo_derecho" as "sol_tipo_derecho" 				
						,SOLI_ANT."sol_val_gastos" as "sol_val_gastos" 					
						,SOLI_ANT."sol_val_comision" as "sol_val_comision" 				
						,SOLI_ANT."sol_val_derechos" as "sol_val_derechos" 				
						,OPE_ANT."orf_mto_gastos" as "orv_mto_gastos" 					
						,OPE_ANT."orf_mto_comision"	as "orv_mto_comision" 				
						,OPE_ANT."orf_mto_derechos"	as "orv_mto_derechos"					
						,'N' as "sol_ind_vta_corta"				
						,'N' as "sol_ind_simul"					       
				FROM "BSCL_VIRTUAL"."GES_OPERACION_RF" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')--VERIFICAR ESTOS ESTADOS
					--AND SOLI_ANT."sol_fecha" >=p_FechaInicial
					--AND SOLI_ANT."sol_fecha" <=p_FechaFinal
					AND SOLI_ANT."MERCADO" = 'RF'
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON Instrumento."Nemotecnico" = trim(OPE_ANT."orf_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = CASE WHEN p_RutCliente ='        0-0' THEN SOLI_ANT."RUT_CLI" ELSE p_RutCliente END
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC
   					ON DOC."DocumentoIdentidad" = TRIM(SOLI_ANT."RUT_CLI")
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
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_INS
		  			ON TIPO_INS."TipoValor" = 'TipoIntrumento'
		  			AND TIPO_INS."Entidad"  = 'FACTURA'
		  			AND TIPO_INS."ValorOrigen" = SOLI_ANT."MERCADO"
		  		LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TipoReajuste
		  			ON TipoReajuste."TipoValor" = 'TipoReajuste'
		  			AND TipoReajuste."Entidad"  = 'FACTURA'
		  			AND TipoReajuste."ValorOrigen" = OPE_ANT."tipo_reaj"	
		  		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as SAP_TIP_OPE
		  			ON SAP_TIP_OPE."IdTipoOperacion" =	CASE WHEN  ORD_ANT."ord_tipo_operac" = 'C' THEN 'COMRF'
											  				  WHEN ORD_ANT."ord_tipo_operac" = 'V' THEN 'VENRF'
														END
				LEFT JOIN "BSCL.Homologacion.BD::HOM.Orden" as HOM_ORD
					ON HOM_ORD."sol_sec" = OPE_ANT."sol_sec"
				WHERE HOM_ORD."sol_sec" IS NULL
				  AND OPE_ANT."orf_fec_orden" >=p_FechaInicial
				  AND OPE_ANT."orf_fec_orden" <=p_FechaFinal;

END;

 
