SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarComprobanteTesoreria";
CREATE PROCEDURE
--  Proposito: servicios de carga de movimientos de tesoreria 
--  renta variable
--  Autor: - EAR
"BSCL.Homologacion.BD.Servicios::CargarComprobanteTesoreria"(
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
	    DECLARE v_Total_Comprobantes_Test BIGINT:=0;
	    DECLARE v_Total_Comprobantes_Cargados BIGINT :=0;
		   	        
               --EGRESOS MONETARIOS  egresos en moneda extranjera dolar preferencialmente en IMTRUST
				INSERT INTO "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria"
				 ( 	"IdComprobante" ,"IdEmpresa" ,"IdUsuario" ,"TipoComprobante" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" 
					,"FechaLiquidacion" ,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona" ,"IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" 
					,"DocumentoIdentidad" ,"TitularPago" ,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" 
					,"SecuenciaAgrupacion" ,"EsBilateral" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" ,"GeneradoDesdeMovimientos" ,"IdEvento" 
					,"MueveCuentaInversion" ,"ComprobanteNeteado" ,"ComprobanteAgrupado" ,"SeparadoPorConcepto" 
					,"SEC_DOCUM"
				)	
               select 
					 "BSCL.Tesoreria.BD.Secuencias::Comprobante".NEXTVAL as "IdComprobante" 
					, p_IdEmpresa as "IdEmpresa" 
					, p_IdUsuario as "IdUsuario" 
					, '' as "TipoComprobante" 
					, MOVIMIENTO_TESORERIA."IdTipoMovimiento" as "IdTipoMovimiento" 
					, MOVIMIENTO_TESORERIA."IdConcepto" as "IdConcepto" 
					, MOVIMIENTO_TESORERIA."IdCaja" as "IdCaja" 
					, MOVIMIENTO_TESORERIA."IdMoneda" as "IdMoneda" 
					, MOVIMIENTO_TESORERIA."Monto" as "Monto" 
					, MOVIMIENTO_TESORERIA."FechaLiquidacion" as "FechaLiquidacion" 
					, MOVIMIENTO_TESORERIA."FechaIngresoRegistro" as "FechaIngresoRegistro" 
					, MOVIMIENTO_TESORERIA."FechaMovimiento" as "FechaMovimiento" 
					, MOVIMIENTO_TESORERIA."IdTipoPersona" as "IdTipoPersona" 
					, MOVIMIENTO_TESORERIA."IdPersona" as "IdPersona" 
					, MOVIMIENTO_TESORERIA."NombrePersona" as "NombrePersona" 
					, MOVIMIENTO_TESORERIA."IdTipoDocumentoIdentidad"  as "IdTipoDocumentoIdentidad" 
					, MOVIMIENTO_TESORERIA."DocumentoIdentidad" as "DocumentoIdentidad" 
					, 'S' as "TitularPago" 
					, MOVIMIENTO_TESORERIA."IdTipoPersonaTitularPago" as "IdTipoPersonaTitularPago" 
					, MOVIMIENTO_TESORERIA."IdPersona" as "IdPersonaTitularPago" 
					, MOVIMIENTO_TESORERIA."NombrePersona" as "NombrePersonaTitularPago" 
					, MOVIMIENTO_TESORERIA."IdPortafolioTitularPago" as "IdPortafolioTitularPago" 
					, '' as "SecuenciaAgrupacion" 
					, '' as "EsBilateral" 
					, MOVIMIENTO_TESORERIA."IdUsuarioBloqueo" as "IdUsuarioBloqueo" 
					, MOVIMIENTO_TESORERIA."FechaHoraBloqueo" as "FechaHoraBloqueo" 
					, p_IdCarga as "IdCarga" 
					, MOVIMIENTO_TESORERIA."IdEstado" as "IdEstado" 
					, '' as "GeneradoDesdeMovimientos" 
					, 0  as "IdEvento" -- es debido a evento de capital 
					, '' as "MueveCuentaInversion" 
					, '' as "ComprobanteNeteado" 
					, 'N' as "ComprobanteAgrupado" 
					, 'N' as "SeparadoPorConcepto"
					, MOVIMIENTO_TESORERIA."SEC_DOCUM"  as "SEC_DOCUM"
					FROM "BSCL.Homologacion.BD::HOM.IngresoEgreso" as MOVIMIENTO_TESORERIA
					LEFT JOIN "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" as COMPROBANTE_TESORERIA
						ON COMPROBANTE_TESORERIA."SEC_DOCUM" = MOVIMIENTO_TESORERIA."SEC_DOCUM"
					WHERE COMPROBANTE_TESORERIA."SEC_DOCUM" IS NULL
					  and MOVIMIENTO_TESORERIA."FechaIngresoRegistro" >= p_FechaInicial 
					  and MOVIMIENTO_TESORERIA."FechaIngresoRegistro" <= p_FechaFinal
					  and MOVIMIENTO_TESORERIA."IdCarga" = p_IdCarga;

             -- REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Comprobantes_Test
                  FROM "BSCL.Homologacion.BD::HOM.IngresoEgreso" as MOVIMIENTO_TESORERIA	
                  WHERE MOVIMIENTO_TESORERIA."FechaIngresoRegistro" >= p_FechaInicial
		        	AND MOVIMIENTO_TESORERIA."FechaIngresoRegistro" <= p_FechaFinal;

			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Comprobantes_Cargados FROM 
                 "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria"
                  WHERE "IdCarga" = p_IdCarga;
                  
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGACOMPTEST' as "CodigoProceso" 
	   			  									,'TES' as "Modulo" 
	   			 									,v_Total_Comprobantes_Test as "TotalRegistrosSebra"
	   			  									,v_Total_Comprobantes_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  IF v_Total_Comprobantes_Test <> v_Total_Comprobantes_Cargados THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_TESORERIA' as "Codigo",'falla en la carga tesoreria' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
