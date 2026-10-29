SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarDataPruebaOrdenesYAsignacionDesdeSebra";
CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de ORDENES - COBROS ORDENES - ASIGNACION 
--  renta variable
--  Autor: - EAR
--   
 "BSCL.Homologacion.BD.Servicios::CargarDataPruebaOrdenesYAsignacionDesdeSebra"(
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdCarga                   BIGINT,         --ID de la carga
	IN  p_Modulo                    NVARCHAR(3),          -- modulo 
	IN  p_Modo                      NVARCHAR(1),     --"R" reproceso borra todos lo cargado y vuelve hacer la carga desde cero "N" nueva carga
    IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
	IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
	-- Parametros salida del servicio publico
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
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
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
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
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	BEGIN	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_ord_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_ord_cargadas DECIMAL(26,8):=0; 
	   DECLARE v_ins_simul_venta BIGINT:=0;
	   DECLARE v_ins_simul_compra BIGINT:=0;
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_Total_operaciones BIGINT:=0;
	   DECLARE v_Total_Ord_Cargados BIGINT:=0;
	   DECLARE v_Total_Asig_Cargados BIGINT:=0;
	   DECLARE v_Total_Operaciones_Asig BIGINT:=0;
	   DECLARE v_Total_Cobros_Ordenes BIGINT:= 0;
	   DECLARE v_Total_Cobros_Cargados BIGINT:=0; 
       DECLARE v_GrupoCobro BIGINT:=0;
       DECLARE v_i BIGINT:=0;
       DECLARE v_P_OUT_FECHA_LIQUIDACION NVARCHAR(10);
 
	   DECLARE ARR_IdOrden     				BIGINT ARRAY;
	   DECLARE ARR_IdTipoOperacion          NVARCHAR(6) ARRAY;
	   DECLARE ARR_IdCondicionDeLiquidacion NVARCHAR(2) ARRAY;
	   DECLARE v_IdOrden					BIGINT;
	   DECLARE v_IdTipoOperacion			NVARCHAR(6);
	   DECLARE v_IdCondicionLiquidacion		NVARCHAR(2);
       DECLARE v_FechaSistema				NVARCHAR(10);
       

			
	-- creo tabla temporal para cargar los datos
	   CREATE LOCAL TEMPORARY TABLE #t_MovtosOrden(
	             "IdOrden" BIGINT
				,"FechaRecepcion" SECONDDATE
				,"FechaIngresoRegistro" TIMESTAMP
				,"FechaCortaIngreso" TIMESTAMP
				,"FechaSolicitudAnulacion" DATE
				,"FechaAnulacion" SECONDDATE
				,"FechaOperacion" SECONDDATE
				,"FechaVigencia" DATE
				,"FechaVencimiento" DATE
				,"FechaEmision" DATE
				,"FechaLiquidacion" DATE
				,"FechaAceptacion" DATE
				,"FechaAutorizacion" DATE
				,"IdTipoVigenciaOrden" NVARCHAR(2)
				,"Glosa" NVARCHAR(520)
				,"IdEstadoOrden" NVARCHAR(3)
				,"Cantidad" DECIMAL(26,8)
				,"MontoOrden" DECIMAL(26,8)
				,"IdTipoCustodia" NVARCHAR(3)
				,"IdFormaCustodia" NVARCHAR(3)
				,"IdPorcentajeVisibilidad" BIGINT
				,"ValorRescate" DECIMAL(26,8)
				,"IdTipoReajuste" NVARCHAR(2)
				,"PrecioOrden" DECIMAL(26,8)
				,"PrecioMercado" DECIMAL(26,8)
				,"TasaAgente" DECIMAL(26,8)
				,"IndicadorFacturacionInmediata" NVARCHAR(1)
				,"IdTipoPrecio" NVARCHAR(1)--
				,"IdTipoTasa" NVARCHAR(1)
				,"ValorCaptacion" DECIMAL(26,8)
				,"IdCondicionLiquidacion" NVARCHAR(2)
				,"IdTipoOperacion" NVARCHAR(6)
				,"IdEmpleadoPortafolio" NVARCHAR(40)
				,"IdEmpleadoDigitador" NVARCHAR(40)
				,"IdTipoCanalIngreso" NVARCHAR(3)
				,"IdEmisor" NVARCHAR(40)
				,"IdEmpresa" BIGINT
				,"IdBolsa" NVARCHAR(3)
				,"Nemotecnico" NVARCHAR(40)
				,"IdTipoClasificacionInstrumento" NVARCHAR(4)
				,"IdEstadoAsignacion" NVARCHAR(3)
				,"IdGrupoCobro" BIGINT
				,"IdInstrumentoSubyacente" BIGINT
				,"IdPortafolio" BIGINT
				,"IdCliente" BIGINT
				,"IdLibroOrdenes" BIGINT
				,"IdInstrumento" BIGINT
				,"IdTipoInstrumento" NVARCHAR(3)
				,"GES_operacion" NVARCHAR(40)
				,"GES_producto" NVARCHAR(40)
				,"GES_Mercado_opt" NVARCHAR(40)
				,"GES_tipooperac" NVARCHAR(40)
				,"GES_RutCli" NVARCHAR(40)
				,"GES_Cuenta" BIGINT
				,"GES_ind_ventacorta" NVARCHAR(40)
				,"GES_ind_simultanea" NVARCHAR(40)
				,"GES_Sol_Sec" BIGINT
				,"GES_Ord_Sec" BIGINT
				,"GES_Mercado" NVARCHAR(40)
				,"GES_TIPO_GASTO" NVARCHAR(40)
				,"GES_TIPO_COMISION" NVARCHAR(40)
				,"GES_TIPO_DERECHO" NVARCHAR(40)
				,"GES_VAL_GASTO" DECIMAL(26,8)
				,"GES_VAL_COMISION" DECIMAL(26,8)
				,"GES_VAL_DERECHO" DECIMAL(26,8)
				,"GES_MONTO_GASTO" DECIMAL(26,8)
				,"GES_MONTO_COMISION" DECIMAL(26,8)
				,"GES_MONTO_DERECHO" DECIMAL(26,8)
				,"GES_PER_ID" BIGINT
			);	
  	CREATE LOCAL TEMPORARY TABLE #t_MovtosAsignacion(
 				"IdAsignacion" BIGINT 
				,"FechaAsignacion" TIMESTAMP
 				,"FechaIngresoRegistro" TIMESTAMP
 				,"CantidadAsignada" DECIMAL(26,8)
				,"MontoAsignado" DECIMAL(26,8)
 				,"ValorRescateAsignado" DECIMAL(26,8)
 				,"IdEstadoAsignacion" NVARCHAR(40)
 				,"IdEstadoFacturacion" NVARCHAR(40)
 				,"IdTransaccion" BIGINT
 				,"IdOrden" BIGINT
 				,"IdCarga" BIGINT
 				,"SecAsignacionMigracion" BIGINT);
 				
 	 CREATE LOCAL TEMPORARY TABLE #t_MovtosCobros(
				 "IdCobroOrden" BIGINT
				 ,"IdGrupoCobro" BIGINT
				 ,"IdOrden" BIGINT
				 ,"IdCobro" NVARCHAR(6)
				 ,"ValorCobro" DECIMAL(26,8)
				 ,"ValorCobroAplicado" DECIMAL(26,8)
				 ,"MontoCobroCalculado" DECIMAL(26,8)
				 ,"FormaCobro" NVARCHAR(100)
				 ,"EsComprobante" NVARCHAR(3)
				 ,"IdPerfilCobro" NVARCHAR(40)
				 ,"FechaIngresoCobro" TIMESTAMP
				 ,"IdCarga" BIGINT
				 );
				 	 
		--------------------
		--Negocio Aqui
		--------------------
		IF p_Modo = 'R' THEN --REPROCESO
		 DELETE FROM "BSCL.Orden.BD::ORD.Orden"
         WHERE "IdCarga" = 	p_IdCarga;	
		 
		 DELETE FROM "BSCL.Orden.BD::ORD.Asignacion"
		 WHERE "IdCarga" = 	p_IdCarga;
		 
		 DELETE FROM "BSCL.Orden.BD::ORD.CobroOrden"
		 WHERE "IdCarga" = 	p_IdCarga;

		END IF;


        CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
		
		SELECT "BSCL.Orden.BD.Secuencias::GrupoCobroOrden".NEXTVAL INTO v_GrupoCobro FROM DUMMY;
		INSERT INTO #t_MovtosOrden ( 		"IdOrden"
		                                    ,"FechaRecepcion"
											,"FechaIngresoRegistro"
											,"FechaCortaIngreso"
											,"FechaSolicitudAnulacion"
											,"FechaAnulacion"
											,"FechaOperacion"
											,"FechaVigencia"
											,"FechaVencimiento"
											,"FechaEmision"
											,"FechaLiquidacion"
											,"FechaAceptacion"
											,"FechaAutorizacion"
											,"IdTipoVigenciaOrden"
											,"Glosa"
											,"IdEstadoOrden"
											,"Cantidad"
											,"MontoOrden"
											,"IdTipoCustodia"
											,"IdFormaCustodia"
											,"IdPorcentajeVisibilidad"
											,"ValorRescate"
											,"IdTipoReajuste"
											,"PrecioOrden"
											,"PrecioMercado"
											,"TasaAgente"
											,"IndicadorFacturacionInmediata"
											,"IdTipoPrecio"
											,"IdTipoTasa"
											,"ValorCaptacion"
											,"IdCondicionLiquidacion"
											,"IdTipoOperacion"
											,"IdEmpleadoPortafolio"
											,"IdEmpleadoDigitador"
											,"IdTipoCanalIngreso"
											,"IdEmisor"
											,"IdEmpresa"
											,"IdBolsa"
											,"Nemotecnico"
											,"IdTipoClasificacionInstrumento"
											,"IdEstadoAsignacion"
											,"IdGrupoCobro"
											,"IdInstrumentoSubyacente"
											,"IdPortafolio"
											,"IdCliente"
											,"IdLibroOrdenes"
											,"IdInstrumento"
											,"IdTipoInstrumento"
											,"GES_operacion"
											,"GES_producto"
											,"GES_Mercado_opt"
											,"GES_tipooperac"
											,"GES_RutCli"
											,"GES_Cuenta"
											,"GES_ind_ventacorta"
											,"GES_ind_simultanea"
											,"GES_Sol_Sec" 
											,"GES_Ord_Sec"
											,"GES_Mercado"
											,"GES_TIPO_GASTO" 
											,"GES_TIPO_COMISION"
											,"GES_TIPO_DERECHO" 
											,"GES_VAL_GASTO" 
											,"GES_VAL_COMISION" 
											,"GES_VAL_DERECHO" 
											,"GES_MONTO_GASTO"
											,"GES_MONTO_COMISION" 
											,"GES_MONTO_DERECHO"
											,"GES_PER_ID"
											)
				SELECT  "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL
						,v_FechaSistema
						,v_FechaSistema
						,v_FechaSistema 
						,'0001-01-01'
						,'0001-01-01'
						,v_FechaSistema
						,v_FechaSistema
						,v_FechaSistema
						,'0001-01-01' as "FechaEmision"
						,v_FechaSistema
						,'0001-01-01'
						,'0001-01-01'
						, CASE WHEN SOLI_ANT."sol_ind_permanencia" = 'P' THEN 'OP'
							   WHEN SOLI_ANT."sol_ind_permanencia" = 'D' THEN 'SH'
						 END as "IdTipoVigenciaOrden" 
						,SOLI_ANT."sol_glosa"
						,'AUT',OPE_ANT."orv_cant_total"
						,OPE_ANT."orv_mto_bruto"
						,OPE_ANT."orv_tra_boveda"
						,CASE WHEN SOLI_ANT."sol_ind_tit_cus" = 'C' THEN 'CUS'
							  ELSE 'NOM'
						 END as "IdFormaCustodia"
						,1 as "IdPorcentajeVisibilidad"
						, 0 as "ValorRescate"
						,Instrumento."IdTipoReajuste" as  "IdTipoReajuste"
						,OPE_ANT."orv_val_tipo_precio" as "PrecioOrden"
						,0 as "PrecioMercado"
						,0 as "TasaAgente"
						,OPE_ANT."orv_ind_factura_imnediata" as "IndicadorFacturacionInmediata"
						,OPE_ANT."orv_tipo_precio" as "IdTipoPrecio"
						,'' as "IdTipoTasa"
						,0  as "ValorCaptacion"
						,CASE WHEN SOLI_ANT."sol_ind_liquid" = 'PH' THEN 'T0'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'PM' THEN 'T1'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'CN' THEN 'T2'
							WHEN SOLI_ANT."sol_ind_liquid"   = 'T3' THEN 'T3'
							ELSE 'T0'
						END as "IdCondicionLiquidacion"
						,CASE WHEN ORD_ANT."ord_tipo_operac" = 'C' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'COMSIM'
			  				  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  SOLI_ANT."sol_ind_simul" = 'TS' THEN 'VENSIM'
			    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'COMFUT' 
			     			  WHEN ORD_ANT."ord_tipo_operac" = 'V' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND SOLI_ANT."sol_ind_simul" <> 'TS' THEN 'VENFUT'                                  
			    			  WHEN ORD_ANT."ord_tipo_operac" = 'C' THEN  CONCAT ('COM',TIPO_INS."ValorDestino")
			   				  WHEN ORD_ANT."ord_tipo_operac" = 'V' THEN  CONCAT ('VEN',TIPO_INS."ValorDestino")
						END  as "IdTipoOperacion"
						,SOLI_ANT."sol_cod_age_due" as "IdEmpleadoPortafolio"
						,SOLI_ANT."sol_cod_age_ate" as "IdEmpleadoDigitador"
						,SOLI_ANT."cca_codigo" as "IdTipoCanalIngreso"
						,'' as "IdEmisor"
						,p_IdEmpresa  as "IdEmpresa"
						,SOLI_ANT."sol_bolsa_destino" as "IdBolsa"		
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN i2."Nemotecnico" ELSE Instrumento."Nemotecnico" END  as "Nemotecnico"
						,TipoInstrumento."IdClasificacionTipoInstrumento" as "IdTipoClasificacionInstrumento"
						,CASE WHEN OPE_ANT."orv_can_asignac_acum" = 0 THEN 'SAS'
							  WHEN OPE_ANT."orv_can_asignac_acum" < OPE_ANT."orv_cant_total"  THEN 'ASP'
							  WHEN OPE_ANT."orv_can_asignac_acum" = OPE_ANT."orv_cant_total"  THEN 'ASC'
						ELSE 'ERR'	END AS "IdEstadoAsignacion"
						,v_GrupoCobro as "IdGrupoCobro"
						,Instrumento."IdInstrumento" as "IdInstrumentoSubyacente"
						,Portafolio."IdPortafolio" as "IdPortafolio"
						,Cliente."IdCliente" as "IdCliente"
						,ConsultarLibroOrdenes."IdLibroOrdenes" as "IdLibroOrdenes"
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN i2."IdInstrumento" ELSE Instrumento."IdInstrumento" END as "IdInstrumento"
						,CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' THEN i2."IdTipoInstrumento" ELSE Instrumento."IdTipoInstrumento" END as "IdTipoInstrumento"
						,SOLI_ANT."ope_id" as "GES_operacion"
						,SAP_TIP_OPE."IdTipoProducto" as "GES_producto"
						,SOLI_ANT."MERCADO" as "GES_Mercado_opt"
						,ORD_ANT."ord_tipo_operac" as "GES_tipooperac"
						,SOLI_ANT."RUT_CLI" as "GES_RutCli"
						,SOLI_ANT."CUENTA"  as "GES_Cuenta"
						,SOLI_ANT."sol_ind_vta_corta" as "GES_ind_ventacorta"
						,SOLI_ANT."sol_ind_simul" as "GES_ind_simultanea"
						,SOLI_ANT."sol_sec" as "GES_Sol_Sec"
						,ORD_ANT."ord_sec"  as "GES_Ord_Sec"
						,ORD_ANT."MERCADO" as  "GES_Mercado"
						,SOLI_ANT."sol_tipo_gasto" as "GES_TIPO_GASTO" 
						,SOLI_ANT."sol_tipo_comision" as "GES_TIPO_COMISION" 
						,SOLI_ANT."sol_tipo_derecho" as "GES_TIPO_DERECHO"
						,SOLI_ANT."sol_val_gastos" as "GES_TIPO_GASTO" 
						,SOLI_ANT."sol_val_comision" as "GES_TIPO_COMISION" 
						,SOLI_ANT."sol_val_derechos" as "GES_TIPO_DERECHO"	                			
						,OPE_ANT."orv_mto_gastos" as "GES_TIPO_GASTO" 
						,OPE_ANT."orv_mto_comision" as "GES_TIPO_COMISION" 
						,OPE_ANT."orv_mto_derechos" as "GES_TIPO_DERECHO"
						,SOLI_ANT."per_id"         
				FROM "BSCL_VIRTUAL"."GES_OPERACION_RV" AS OPE_ANT 
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" AS  SOLI_ANT 
					ON SOLI_ANT."sol_sec" = OPE_ANT."sol_sec"
					AND SOLI_ANT."sol_estado" not in ('NN','SR')
					AND SOLI_ANT."sol_ind_vta_corta" ='N'
					AND SOLI_ANT."sol_fecha" >=p_FechaInicial
					AND SOLI_ANT."sol_fecha" < ADD_DAYS (TO_DATE (p_FechaFinal, 'YYYY-MM-DD'), 1)
				INNER JOIN "BSCL_VIRTUAL"."GES_ORDEN" AS ORD_ANT 
					ON ORD_ANT."sol_sec" = SOLI_ANT."sol_sec"
				LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS Instrumento 
					ON rtrim(ltrim(Instrumento."Nemotecnico")) = TRIM(OPE_ANT."orv_nemo")
				INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
				    ON TipoInstrumento."IdTipoInstrumento" = Instrumento."IdTipoInstrumento"
				INNER JOIN "BSCL_VIRTUAL"."GES_CUENTA" AS CUENTA 
					ON CUENTA."RUT_CLI" = SOLI_ANT."RUT_CLI"
					AND CUENTA."CUENTA" =  SOLI_ANT."CUENTA"
				LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cliente 
					ON TRIM(Cliente."DocumentoIdentidad") = TRIM(CUENTA."RUT_CLI")         
				LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Portafolio 
					ON Portafolio."IdCliente" = Cliente."IdCliente"
					AND Portafolio."CodigoPortafolio" = TO_VARCHAR(CUENTA."CUENTA")
				LEFT JOIN "BSCL.Orden.BD.Vistas::ConsultarLibroOrdenes" as ConsultarLibroOrdenes 
					ON ConsultarLibroOrdenes."IdSegmentoCliente" = Cliente."IdSegmentoCliente"
					AND ConsultarLibroOrdenes."IdEmpresa" = p_IdEmpresa
					AND ConsultarLibroOrdenes."IdCanal"   = SOLI_ANT."cca_codigo"
				LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as i2
				 	ON i2."IdTipoInstrumento" = CASE WHEN SOLI_ANT."sol_ind_simul" = 'TS' 
			   								 	THEN 'SIM'
											 		ELSE ''
											 	END
					AND i2."Nemotecnico"  = CASE WHEN ORD_ANT."ord_tipo_operac" = 'C' THEN 'SIMULTÁNEA COMPRA'
			                    	 ELSE 'SIMULTÁNEA VENTA'
			                    	 END
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
														END; 
 			
                -- BORRAMOS TODAS AQUELLAS QUE TENGAN DATOS NULOS
               DELETE FROM #t_MovtosOrden
                WHERE "IdCliente" IS NULL
	              OR "IdPortafolio" IS  NULL
	              OR "IdInstrumento" IS NULL
	              OR "IdTipoInstrumento" IS NULL
	              OR "GES_producto" IS NULL
	              OR "IdTipoOperacion" IS NULL
	              OR "IdTipoReajuste" IS NULL
                  OR "IdLibroOrdenes" IS NULL
                  OR "IdTipoClasificacionInstrumento" IS NULL;
                  			
                -- CONTABILIZAMOS EL MONTO DE OPERACIONES
             	 SELECT COUNT(*) INTO v_Total_operaciones FROM #t_MovtosOrden;

                -- CARGAR LAS ASIGNACIONES ASOCIADAS A LAS ORDENES CARGADAS 
                -- REVISAMOS SI HUBO MOVIMIENTOS PARA CARGAR
                -- SI NO HAY ORDENES CON ESTADOS VALIDOS SE SALE

                IF v_Total_operaciones = 0 THEN
                  	    v_hash= select 'ORD_CARGA_CERO' as "Codigo", '' as "Valor" FROM DUMMY;
				        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_CERO',:v_hash,:v_erroroutmsg);
				        p_out_ErroresDetalle =  select 'ORD_CARGA_CERO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
						        				select * from :p_out_ErroresDetalle;
					    DROP TABLE #t_MovtosOrden;
		                DROP TABLE #t_MovtosAsignacion;
		                DROP TABLE #t_MovtosCobros;
						SIGNAL SALIDA_CONTROLADA; 	  
                END IF;
  
				--- RECUPERO LA INFORMACION Y LA PASO A UN ARREGLO 	
				--- las variables del arreglo deben declararse arriba
                --- se puede crear el arreglo solo con variables tipo tabla 
                
                MOV_ORDENES = select * from #t_MovtosOrden order by "IdOrden" ASC;

				ARR_IdOrden					 := ARRAY_AGG(:MOV_ORDENES."IdOrden" order by "IdOrden" ASC);
				ARR_IdCondicionDeLiquidacion := ARRAY_AGG(:MOV_ORDENES."IdCondicionLiquidacion" order by "IdOrden" ASC);
				ARR_IdTipoOperacion  		 := ARRAY_AGG(:MOV_ORDENES."IdTipoOperacion" order by "IdOrden" ASC);
					
			    -- RECORRO EL ARREGLO

				FOR v_i IN 1 .. CARDINALITY(:ARR_IdOrden) DO 
					v_IdOrden := :ARR_IdOrden[:v_i];
					v_IdCondicionLiquidacion := :ARR_IdCondicionDeLiquidacion[:v_i];
					v_IdTipoOperacion := :ARR_IdTipoOperacion[:v_i];
				
					CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion"(:v_FechaSistema,
																				   :v_IdTipoOperacion,
																				   :v_IdCondicionLiquidacion,
																				    44,
																				    v_P_OUT_FECHA_LIQUIDACION);
					UPDATE #t_MovtosOrden SET 	
						                        "FechaLiquidacion"			= v_P_OUT_FECHA_LIQUIDACION
						                        ,"FechaVencimiento"         = case when #t_MovtosOrden."IdTipoInstrumento" = 'SIM' 
						                        							  then  ADD_YEARS (TO_DATE (v_FechaSistema, 'YYYY-MM-DD'), 1)
						                        							  else #t_MovtosOrden."FechaVencimiento"
						                                 					  end 
					WHERE "IdOrden" = v_IdOrden;												  			
				END FOR;
                
				INSERT INTO "BSCL.Orden.BD::ORD.Orden" ("IdOrden" ,"FechaRecepcion" ,"FechaIngresoRegistro" ,"FechaIngresoRegistroCorta"			
				 			 ,"FechaSolicitudAnulacion" ,"FechaAnulacion" ,"FechaOperacion"	 ,"FechaVigencia" 						
				 			 ,"FechaVencimiento" ,"FechaEmision" ,"FechaLiquidacion" ,"FechaAceptacion" 						
							 ,"FechaAutorizacion","IdTipoVigenciaOrden"					
							 ,"Glosa" ,"IdEstadoOrden" ,"Cantidad" ,"MontoOrden"  ,"IdTipoCustodia" 				
							 ,"IdFormaCustodia" ,"IdPorcentajeVisibilidad" ,"ValorRescate" ,"IdTipoReajuste" 						
							 ,"PrecioOrden" ,"PrecioMercado" ,"TasaAgente" ,"IndicadorFacturacionInmediata" 		
							 ,"IdTipoPrecio" ,"IdTipoTasa" ,"ValorCaptacion" ,"IdCondicionDeLiquidacion" 			
							 ,"IdTipoOperacion"  ,"IdEmpleadoPortafolio" 						
							 ,"IdEmpleadoDigitador" ,"IdCliente" ,"IdPortafolioCliente" 					
							 ,"IdTipoCanalIngreso" ,"IdLibroOrdenes" ,"IdEmisor" ,"IdEmpresa" ,"IdBolsa" 								
							 ,"IdInstrumento" ,"IdTipoInstrumento"  ,"IdClasificacionTipoInstrumento" ,"IdProducto" 							
							 ,"Nemotecnico" ,"IdEstadoAsignacion" ,"IdGrupoCobro" ,"IdInstrumentoSubyacente"                 
							 ,"IdCarga","SecOrdenMigracion")
					SELECT "IdOrden"
					       ,"FechaRecepcion","FechaIngresoRegistro","FechaCortaIngreso" 
					       ,"FechaSolicitudAnulacion","FechaAnulacion","FechaOperacion","FechaVigencia" 
	                       ,"FechaVencimiento","FechaEmision","FechaLiquidacion","FechaAceptacion"
	                       ,"FechaAutorizacion","IdTipoVigenciaOrden"
						   ,"Glosa","IdEstadoOrden","Cantidad","MontoOrden","IdTipoCustodia"
						   ,"IdFormaCustodia","IdPorcentajeVisibilidad","ValorRescate","IdTipoReajuste"					
						   ,"PrecioOrden","PrecioMercado","TasaAgente","IndicadorFacturacionInmediata"
						   ,"IdTipoPrecio","IdTipoTasa","ValorCaptacion","IdCondicionLiquidacion"
						   ,"IdTipoOperacion","IdEmpleadoPortafolio"
						   ,"IdEmpleadoDigitador","IdCliente","IdPortafolio"
						   ,"IdTipoCanalIngreso","IdLibroOrdenes","IdEmisor","IdEmpresa","IdBolsa"
						   ,"IdInstrumento","IdTipoInstrumento","IdTipoClasificacionInstrumento","GES_producto"
						   ,"Nemotecnico","IdEstadoAsignacion","IdGrupoCobro","IdInstrumentoSubyacente"
	                       ,p_IdCarga,"GES_Ord_Sec"
	                FROM #t_MovtosOrden;
                              
                  --REVISAMOS CUANTOS FUERON CARGADOS
	   			  SELECT COUNT(*) INTO v_Total_Ord_Cargados FROM 
	   			  "BSCL.Orden.BD::ORD.Orden" where "IdCarga" = p_IdCarga;
	   			  
	   			  
	   			  IF v_Total_Ord_Cargados <> v_Total_operaciones THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_ORD' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_ORD',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ORD' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;
					 DROP TABLE #t_MovtosOrden;
	               	 DROP TABLE #t_MovtosAsignacion;
	               	 DROP TABLE #t_MovtosCobros;
					 SIGNAL SALIDA_CONTROLADA; 	
	   			  END IF;

	   			  -- ASIGNAMOS LOS COBROS  
                  -- por separado
                  -- primero cobros 
                  -- gastos
                  -- derechos
                  -- impuesto
                  -- como se si el cobro es variable o fijo
         
         

         
         
         
                  INSERT INTO #t_MovtosCobros( "IdCobroOrden" ,"IdGrupoCobro" ,"IdOrden" ,"IdCobro" 
				 								,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" ,"FormaCobro" 
												,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" ,"IdCarga")
                  SELECT
					   0
					   ,mvto."IdGrupoCobro"
					   ,mvto."IdOrden"
				       ,mvto."GES_TIPO_GASTO"
				       ,COBROS."ValorCobro" 
				       ,mvto."GES_VAL_GASTO" AS "ValorCobroAplicado"
				       ,mvto."GES_MONTO_GASTO" AS "MontoCobroCalculado"
				       ,COBROS."FormaCobro"
				       ,'S'
				       ,'COBROS_MIGRACION'
				       ,mvto."FechaRecepcion"
				       ,p_IdCarga
                  FROM #t_MovtosOrden AS mvto
                  INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBROS
                  	ON COBROS."IdCobro" = mvto."GES_TIPO_GASTO"
                  UNION ALL
				  SELECT
					   0
					   ,mvto."IdGrupoCobro"
					   ,mvto."IdOrden"
				       ,mvto."GES_TIPO_DERECHO"
				       ,COBROS."ValorCobro" 
				       ,mvto."GES_VAL_DERECHO" AS "ValorCobroAplicado"
				       ,mvto."GES_MONTO_DERECHO" AS "MontoCobroCalculado"
				       ,COBROS."FormaCobro"
				       ,'S'
				       ,'COBROS_MIGRACION'
				       ,mvto."FechaRecepcion"
				       ,p_IdCarga
                  FROM #t_MovtosOrden AS mvto
                  INNER JOIN "BSCL.Plataforma.BD::COM.Cobro"	AS COBROS
                  	ON COBROS."IdCobro" = mvto."GES_TIPO_DERECHO"
                  UNION ALL
                  SELECT
					    0
					   ,mvto."IdGrupoCobro"
					   ,mvto."IdOrden"
				       ,mvto."GES_TIPO_COMISION"
				       ,COBROS."ValorCobro" 
				       ,mvto."GES_VAL_COMISION" AS "ValorCobroAplicado"
				       ,mvto."GES_MONTO_COMISION" AS "MontoCobroCalculado"
				       ,COBROS."FormaCobro"
				       ,'S'
				       ,'COBROS_MIGRACION'
				       ,mvto."FechaRecepcion"
				       ,p_IdCarga
                  FROM #t_MovtosOrden AS mvto
                  INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBROS
                  	ON COBROS."IdCobro" = mvto."GES_TIPO_COMISION";

                  SELECT COUNT(*) INTO v_Total_Cobros_Ordenes FROM #t_MovtosCobros;


                 --INSERTAMOS LOS COBROS

                 INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden" ("IdCobroOrden" ,"IdGrupoCobro" ,"IdOrden"  ,"IdCobro" 								
 															  ,"ValorCobro"  ,"ValorCobroAplicado" ,"MontoCobroCalculado" ,"FormaCobro" 							
 															  ,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" ,"IdCarga")
                 
                 SELECT  "BSCL.Orden.BD.Secuencias::CobrosOrden".NEXTVAL ,"IdGrupoCobro" ,"IdOrden" ,"IdCobro"
				 ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado"
				 ,"FormaCobro" ,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" 
				 ,"IdCarga"  FROM #t_MovtosCobros;

                 -- LOS COBROS CARGADOS DEBEN SER IGUAL A LOS RECUPERADOS
				 SELECT COUNT(*) INTO v_Total_Cobros_Cargados FROM "BSCL.Orden.BD::ORD.CobroOrden" WHERE "IdCarga" = p_IdCarga;                 
				 IF v_Total_Cobros_Ordenes <> v_Total_Cobros_Cargados THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_COB' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_COB',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_COB' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;
					  DROP TABLE #t_MovtosOrden;
	               	  DROP TABLE #t_MovtosAsignacion;
	               	  DROP TABLE #t_MovtosCobros;
					 SIGNAL SALIDA_CONTROLADA; 	
	   			  END IF;

	    		  -- COMENZAMOS A SELECCIONAR LOS DATOS DE LA ASIGNACION
				  INSERT INTO #t_MovtosAsignacion (
					                 "IdAsignacion" 
									,"FechaAsignacion"
					 				,"FechaIngresoRegistro"
					 				,"CantidadAsignada" 
									,"MontoAsignado" 
					 				,"ValorRescateAsignado" 
					 				,"IdEstadoAsignacion" 
					 				,"IdEstadoFacturacion" 
					 				,"IdTransaccion" 
					 				,"IdOrden"
					 				,"IdCarga"
					 				,"SecAsignacionMigracion")
					 SELECT
					     "BSCL"."BSCL.Orden.BD.Secuencias::Asignacion".NEXTVAL 
					 	, v_FechaSistema 	
					 	, v_FechaSistema
					 	,ASIGNACION."CAN_ASIGNAC"
					 	,ASIGNACION."MONTO"
					 	, 0 AS "ValorRescateAsignado" 
					 	,'ING' AS "IdEstadoAsignacion" 
					 	, CASE WHEN (ASIGNACION."SEC_MOVTO_FAC" = 0 OR IFNULL(MOVFACTURARV."SEC_MOVTO_FAC",0) = 0 OR MOVFACTURARV.EST_MOVTO_FAC ='A') THEN 'ANF'
					 	  ELSE 'AEF' 
					 	  END AS "IdEstadoFacturacion"
					 	, Transaccion."IdTransaccion"
					 	, mvto."IdOrden"
					 	, p_IdCarga
					 	, ASIGNACION."SEC_ASIGNACION"     	
					 FROM #t_MovtosOrden as mvto
					 INNER JOIN "BSCL_VIRTUAL"."GES_ASIGNACION_RV" AS ASIGNACION 
					 	ON ASIGNACION."sol_sec" = mvto."GES_Sol_Sec"
					 INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as INS
					 	ON INS."Nemotecnico" = ASIGNACION."NEMO"
					 INNER JOIN "BSCL_VIRTUAL"."GES_TBPLTBRV" AS PALOS_RV
					    ON  PALOS_RV."FOLIO_TRANS" = ASIGNACION."FOLIO_TRANS"
					    AND PALOS_RV."IND_BOLSA" = ASIGNACION."IND_BOLSA"
					    AND PALOS_RV."TIPO_OPERAC" = ASIGNACION."TIPO_OPERAC"
					    AND PALOS_RV."FEC_TRANS" = ASIGNACION."FEC_TRANS"
					    AND PALOS_RV."MERCADO" = ASIGNACION."MERCADO"
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_INS
			  			ON TIPO_INS."TipoValor" = 'TipoIntrumento'
			  			AND TIPO_INS."Entidad"  = 'FACTURA'
			  			AND TIPO_INS."ValorOrigen" = ASIGNACION."MERCADO"
			  		 LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GRUPO_INS 
			  			ON GRUPO_INS."CodigoClasificacion" = 'FUTUROS'
			  			AND GRUPO_INS."IdTipoInstrumento" = TIPO_INS."ValorDestino"
					 INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" AS Transaccion
					 	ON  Transaccion."Folio" = TRIM(PALOS_RV."FOLIO_TRANS")
					 	AND Transaccion."FechaOperacionTransaccion" = CONCAT(SUBSTRING(v_FechaSistema,1,10), CONCAT(' ',SUBSTRING (ASIGNACION."FEC_TRANS", 12, 8)))
					 	AND Transaccion."IdBolsa" = TRIM(PALOS_RV."IND_BOLSA")
					 	AND Transaccion."IdTipoOperacion" = 	CASE WHEN  ASIGNACION."TIPO_OPERAC" = 'C' AND  ASIGNACION."IND_SIMUL" = 'TS' THEN 'COMSIM'
												  				  WHEN ASIGNACION."TIPO_OPERAC" = 'V' AND  ASIGNACION."IND_SIMUL" = 'TS' THEN 'VENSIM'
												    			  WHEN ASIGNACION."TIPO_OPERAC" = 'C' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND ASIGNACION."IND_SIMUL" <> 'TS' THEN 'COMFUT' 
												     			  WHEN ASIGNACION."TIPO_OPERAC" = 'V' AND  TIPO_INS."ValorDestino" = GRUPO_INS."IdTipoInstrumento" AND ASIGNACION."IND_SIMUL" <> 'TS' THEN 'VENFUT'                                  
												    			  WHEN ASIGNACION."TIPO_OPERAC" = 'C' THEN  CONCAT ('COM',TIPO_INS."ValorDestino")
												   				  WHEN ASIGNACION."TIPO_OPERAC" = 'V' THEN  CONCAT ('VEN',TIPO_INS."ValorDestino")
															END
					 LEFT JOIN  "BSCL_VIRTUAL"."GES_TBCTMFRV" AS MOVFACTURARV
					 	ON MOVFACTURARV."SEC_MOVTO_FAC" = ASIGNACION."SEC_MOVTO_FAC";

                  --REVISAMOS CUANTAS ASIGNACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Operaciones_Asig FROM 
                  #t_MovtosAsignacion;

 				  INSERT INTO "BSCL.Orden.BD::ORD.Asignacion"("IdAsignacion" ,"FechaAsignacion" ,"FechaIngresoRegistro" ,"CantidadAsignada"                    
 												,"MontoAsignado" ,"ValorRescateAsignado" ,"IdEstadoAsignacion" ,"IdEstadoFacturacion"                 
 												,"IdTransaccion" ,"IdOrden","IdCarga","SecAsignacionMigracion")
				  SELECT "IdAsignacion" ,"FechaAsignacion" ,"FechaIngresoRegistro"	,"CantidadAsignada" 
						 ,"MontoAsignado","ValorRescateAsignado" ,"IdEstadoAsignacion" ,"IdEstadoFacturacion" 
					 	 ,"IdTransaccion","IdOrden","IdCarga","SecAsignacionMigracion"
				  FROM #t_MovtosAsignacion;
				  
				  -- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Asig_Cargados FROM 
                  "BSCL.Orden.BD::ORD.Asignacion" WHERE "IdCarga" = p_IdCarga;
                  
                  IF v_Total_Operaciones_Asig <> v_Total_Asig_Cargados THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_ASIG' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_ASIG',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ASIG' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;
					  DROP TABLE #t_MovtosOrden;
	               	  DROP TABLE #t_MovtosAsignacion;
	               	  DROP TABLE #t_MovtosCobros;
					 SIGNAL SALIDA_CONTROLADA; 	
	   			  END IF;
                  -------------------------------------------------
                  -- cargamos los cobros de la asignacion----------
				  -------------------------------------------------
                  -- cargamos los gastos
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )		
                   SELECT
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,v_FechaSistema as "FechaIngresoCobro"
					,v_FechaSistema as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."GASTOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."GASTOS" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
						ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
						ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
						ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
						ON COBRO."IdCobro" = SOLI."sol_tipo_gasto"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
						ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";
			
				  -- cargamos los derechos
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )
                   SELECT 
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,v_FechaSistema as "FechaIngresoCobro"
					,v_FechaSistema as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."DERECHOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."DERECHOS" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
						ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
						ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
						ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
						ON COBRO."IdCobro" = SOLI."sol_tipo_derecho"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
						ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";
				--comision
                  INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"(
                     "IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 )		
                   SELECT
					 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
					,"IdAsignacion" as "IdAsignacion"
					,COBRO."IdCobro"
					,v_FechaSistema as "FechaIngresoCobro"
					,v_FechaSistema as "FechaIngresoRegistro"
					,'COBROS_MIGRACION' as "IdPerfilCobro" 
					, COBRO."FormaCobro" as "FormaCobro"
					, 0 as "ValorCobro"
					,ROUND((MOV_FAC_RV."COMISION" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
					,MOV_FAC_RV."COMISION" as  "MontoCobroCalculado"
					,p_IdCarga
					FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
					INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
						ON SOLI."sol_sec" =  ASIG."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
						ON OPE_RV."sol_sec" = SOLI."sol_sec"
					INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
						ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
					INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
						ON COBRO."IdCobro" = SOLI."sol_tipo_comision"
					INNER JOIN "BSCL.Orden.BD::ORD.Asignacion" SAP_ASIG
						ON SAP_ASIG."SecAsignacionMigracion" = ASIG."SEC_ASIGNACION";			
               	  DROP TABLE #t_MovtosOrden;
               	  DROP TABLE #t_MovtosAsignacion;
               	  DROP TABLE #t_MovtosCobros;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
