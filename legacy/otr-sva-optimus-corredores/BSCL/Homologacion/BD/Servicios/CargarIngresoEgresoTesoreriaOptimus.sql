SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarIngresoEgresoTesoreriaOptimus";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de solicitudes FFMM 
--  TESORERIA
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarIngresoEgresoTesoreriaOptimus"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     ---IN  p_Personas			   table ("RUT_CLI"   NVARCHAR(11), "SEC_RUT_CLI" NVARCHAR(1)),    -- rut del clientes sebra
	 ---IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 ---IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
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
	   DECLARE v_Total_ingegr_test BIGINT:=0;
	   DECLARE v_Total_ingegr_Cargados BIGINT :=0;
	   DECLARE v_Quantity  BIGINT :=0;
	   
           SELECT count(*) into  v_Total_ingegr_test
           FROM  "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS HOM
				LEFT JOIN "BSCL.Tesoreria.BD::TES.IngresoEgreso"  AS TES
				  ON TES."IdIngresoEgreso" = HOM."IdIngresoEgreso"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND TES."IdIngresoEgreso" IS NULL;

			INSERT INTO "BSCL.Tesoreria.BD::TES.IngresoEgreso"
				(  "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
				  ,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
				  ,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
				  ,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
				  ,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
				  ,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
				  ,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
				  ,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
				)
			SELECT HOM."IdIngresoEgreso" ,HOM."IdEmpresa" ,HOM."IdModulo" ,HOM."IdUsuario" ,HOM."IdTipoMovimiento" ,HOM."IdConcepto" ,HOM."IdCaja" ,HOM."IdMoneda" ,HOM."Monto" ,HOM."FechaLiquidacion" 
				  ,HOM."FechaIngresoRegistro" ,HOM."FechaMovimiento" ,HOM."IdTipoPersona",HOM."IdPersona" ,HOM."NombrePersona" ,HOM."IdTipoDocumentoIdentidad" ,HOM."DocumentoIdentidad" 
				  ,HOM."IdTipoPersonaTitularPago" ,HOM."IdPersonaTitularPago" ,HOM."NombrePersonaTitularPago" ,HOM."IdPortafolioTitularPago" ,HOM."SecuenciaAgrupacion" ,HOM."Glosa" 
				  ,HOM."IdEmpleadoDigitador" ,HOM."IdSucursalEmpleadoDigitador" ,HOM."IdTipoComprobanteOrigen" ,HOM."IdComprobanteOrigen" ,HOM."FechaComprobanteOrigen" ,HOM."FechaLiquidacionComprobanteOrigen" 
				  ,HOM."IdTipoProducto" ,HOM."IdTipoOperacion" ,HOM."IdOperacion" ,HOM."FechaOperacion" ,HOM."IdCondicionLiquidacion" ,HOM."IdBolsa" ,HOM."IdMonedaOperacion" ,HOM."MontoOperacion" 
				  ,HOM."FechaParidad" ,HOM."IdTipoReajuste" ,HOM."ValorParidad" ,HOM."FolioCartera" ,HOM."IdInstrumento" ,HOM."IdTipoInstrumento" ,HOM."IdClasificacionTipoInstrumento" 
				  ,HOM."EsBilateral" ,HOM."TitularPago" ,HOM."EsIngresoAutomatico" ,HOM."AgrupaComprobante" ,HOM."IdUsuarioBloqueo" ,HOM."FechaHoraBloqueo" ,HOM."IdCarga" ,HOM."IdEstado" 
				  ,HOM."MueveCuentaInversion" ,HOM."SecuenciaRelacionada" ,HOM."NeteaComprobante" ,HOM."IdUsuarioModificacion" ,HOM."FechaHoraModificacion"
			FROM  "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS HOM
			LEFT JOIN  "BSCL.Tesoreria.BD::TES.IngresoEgreso" AS TES 	 
			  ON TES."IdIngresoEgreso" = HOM."IdIngresoEgreso"
			WHERE TES."IdIngresoEgreso" IS NULL
			  AND HOM."IdCarga" = 88;
			  
			-- contamos los cargados
		    select count(*) into v_Total_ingegr_Cargados
		    from "BSCL.Tesoreria.BD::TES.IngresoEgreso"
		    where "IdCarga" = p_IdCarga;
			 
	         -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
		     p_ResumenDatosProcesados =  select 'CARGACOMPROBANTE_TES' as "CodigoProceso" 
		   			  							,'TES' as "Modulo" 
		   			 							, v_Total_ingegr_test as "TotalRegistrosSebra"
		   			  							,v_Total_ingegr_Cargados as "TotalRegistrosSap"  FROM DUMMY
	       							     union all
	       							     select * from :p_ResumenDatosProcesados;

          IF  v_Total_ingegr_test <> v_Total_ingegr_Cargados THEN 
			 p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ING_EGR_TEST' as "Codigo",'falla en la carga movimientos tesoreria' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   	  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
