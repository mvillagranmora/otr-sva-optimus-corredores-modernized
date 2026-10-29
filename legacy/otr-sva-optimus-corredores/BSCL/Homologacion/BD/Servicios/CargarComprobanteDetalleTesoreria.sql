SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarComprobanteDetalleTesoreria";
CREATE PROCEDURE
--  Proposito: servicios de carga de movimientos de tesoreria 
--  renta variable
--  Autor: - EAR
"BSCL.Homologacion.BD.Servicios::CargarComprobanteDetalleTesoreria"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_RutCliente          NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	-- Salidas para mensajes de advertencias y errores
	OUT p_ResumenDatosProcesados Table ("CodigoProceso" nvarchar(40),"Modulo" nvarchar(3),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)),
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
	    DECLARE v_Total_ComprobantesDetalle_Test BIGINT:=0;
	    DECLARE v_Total_ComprobantesDetalle_Cargados BIGINT :=0;
		   	        
               --EGRESOS MONETARIOS  egresos en moneda extranjera dolar preferencialmente en IMTRUST
				INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteDetalleTesoreria"
				 ( 
				  "IdComprobanteDetalle" ,"IdComprobante" ,"IdConcepto" ,"NroLinea" ,"IdMoneda" ,"Monto" ,"SecuenciaAgrupacion" ,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" 
				  ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" ,"IdTipoProducto" ,"IdTipoOperacion" 
				  ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" ,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" 
				  ,"IdClasificacionTipoInstrumento" ,"IdCarga" ,"IdEstado" ,"IdTipoMovimiento" ,"SecuenciaRelacionada","SEC_DOCUM" 
				 )	
               select 
					  "BSCL.Tesoreria.BD.Secuencias::ComprobanteDetalle".NEXTVAL  as "IdComprobanteDetalle" 
					, MOVIMIENTO_COMPROBANTE."IdComprobante" as "IdComprobante" 
					, MOVIMIENTO_COMPROBANTE."IdConcepto" as "IdConcepto" 
					, 1 as "NroLinea" 
					, MOVIMIENTO_COMPROBANTE."IdMoneda" as "IdMoneda" 
					, MOVIMIENTO_COMPROBANTE."Monto" as "Monto" 
					, '' as "SecuenciaAgrupacion" 
					, 0  as "IdEmpleadoDigitador" 
					, 0 as "IdSucursalEmpleadoDigitador" 
					, '' as "IdTipoComprobanteOrigen" -- debo revisar
					, 0  as "IdComprobanteOrigen" -- debo revisar
					, '0001-01-01' as "FechaComprobanteOrigen" 
					, '0001-01-01' as "FechaLiquidacionComprobanteOrigen" 
					, '' as "IdTipoProducto" 
					, '' as "IdTipoOperacion" 
					, '' as "IdBolsa" 
					, '' as "IdMonedaOperacion" 
					, MOVIMIENTO_COMPROBANTE."Monto" as "MontoOperacion" 
					, '0001-01-01' as "FechaParidad" 
					, '' as "IdTipoReajuste" 
					, 0  as "ValorParidad" 
					, 0  as "FolioCartera" 
					, 0  as "IdInstrumento" 
					, '' as "IdTipoInstrumento" 
					, '' as "IdClasificacionTipoInstrumento" 
					, p_IdCarga as "IdCarga" 
					, MOVIMIENTO_COMPROBANTE."IdEstado" as "IdEstado"
					, MOVIMIENTO_COMPROBANTE."IdTipoMovimiento" as "IdTipoMovimiento" 
					, 0 as "SecuenciaRelacionada" 
					,MOVIMIENTO_COMPROBANTE."SEC_DOCUM" AS "SEC_DOCUM"
			    FROM "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" AS  MOVIMIENTO_COMPROBANTE
			    LEFT JOIN "BSCL.Homologacion.BD::HOM.ComprobanteDetalleTesoreria" AS DETALLE_COMPROBANTE
			    	ON DETALLE_COMPROBANTE."SEC_DOCUM" = MOVIMIENTO_COMPROBANTE."SEC_DOCUM"
			    WHERE DETALLE_COMPROBANTE."SEC_DOCUM" IS NULL
			     AND MOVIMIENTO_COMPROBANTE."FechaIngresoRegistro" >= p_FechaInicial
			     AND MOVIMIENTO_COMPROBANTE."FechaIngresoRegistro" <= p_FechaFinal;

             -- REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_ComprobantesDetalle_Test
                  FROM "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" as MOVIMIENTO_COMPROBANTE	
                  WHERE MOVIMIENTO_COMPROBANTE."FechaIngresoRegistro" >= p_FechaInicial
		        	AND MOVIMIENTO_COMPROBANTE."FechaIngresoRegistro" <= p_FechaFinal;

			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_ComprobantesDetalle_Cargados FROM 
                   "BSCL.Homologacion.BD::HOM.ComprobanteDetalleTesoreria"
                  WHERE "IdCarga" = p_IdCarga;
                  
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGACOMPDETTEST' as "CodigoProceso" 
	   			  									,'TES' as "Modulo" 
	   			 									,v_Total_ComprobantesDetalle_Test as "TotalRegistrosSebra"
	   			  									,v_Total_ComprobantesDetalle_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  IF v_Total_ComprobantesDetalle_Test <> v_Total_ComprobantesDetalle_Cargados THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_TESORERIA' as "Codigo",'falla en la carga tesoreria' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
