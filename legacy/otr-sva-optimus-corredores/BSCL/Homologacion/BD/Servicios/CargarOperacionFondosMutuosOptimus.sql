SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionFondosMutuosOptimus";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de solicitudes FFMM 
--  FFMM
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarOperacionFondosMutuosOptimus"(
	-- Parametros entrada comunes en todos los servicios publicos
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_Personas			   table ("RUT_CLI"   NVARCHAR(11), "SEC_RUT_CLI" NVARCHAR(1)),    -- rut del clientes sebra
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
	   DECLARE v_Total_Operaciones_ffmm BIGINT:=0;
	   DECLARE v_Total_ffmm_Cargados BIGINT :=0;
	   DECLARE v_Quantity  BIGINT :=0;
	   
	   select COUNT(1) INTO v_Quantity FROM :p_Personas;
	   
       IF v_Quantity > 0 THEN 
              
           SELECT count(*) into v_Total_Operaciones_ffmm
           FROM "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as HOM
				INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RUT_CLI" 
				  AND PER."SEC_RUT_CLI" = HOM."SEC_RUT_CLI" 
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
				  ON FMM."IdSolicitudAporteRescate" = HOM."IdSolicitudAporteRescate"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND FMM."IdSolicitudAporteRescate"  IS NULL;

	       INSERT INTO "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"(
				"IdSolicitudAporteRescate","IdCliente","IdPortafolio","TipoSolicitud","IdSerieFondo","IdTipoMovimientoFondoMutuo","IdDividendo"
				,"IdSerieFondoDestino","IdEmisorDestino","IdClienteDestino","IdPortafolioDestino","IdSerieFondoOrigen","IdEmisorOrigen"
				,"MontoOrigen","IndicadorMLT","Overnight","IdSolicitudAporteRescateRelacionado","IdTipoCartera","CalculoPorMontoCuota","FechaEmision"
				,"FechaAceptacion","FechaCertificacion","FechaLiquidacion","FechaAnulacion","FechaOriginalInversion","FechaIngresoRegistro"
				,"FechaAnulacionRegistro","FechaAceptacionRegistro","FechaCertificacionRegistro","FechaLiquidacionRegistro","Monto","NumeroCuotas"
				,"ValorCuota","CostoUF","CostoMonedaLocal","ValorUFFechaInversion","ParidadFechaInversion","IdMoneda","MontoComision","MontoPago"
				,"Estado","Glosa","IdEmpresa","IdUsuario","IdModulo","EstadoLiquidacion","MueveCta","FolioCartaLiquidacion","IdMedioDePago"
				,"MontoInversion","NumeroCuotasInversion","ValorCuotaInversion","CostoMonedaLocalInversion","ParidadFechaCertificacion","MontoComisionLocal"
				,"MontoPagoLocal","FechaModificacion","FechaModificacionRegistro","OrigenOperacion","SolicitudBatch","ComisionInformada"
			    ,"IdCarga"  	
			)		
			SELECT 
				HOM."IdSolicitudAporteRescate",HOM."IdCliente",HOM."IdPortafolio",HOM."TipoSolicitud",HOM."IdSerieFondo",HOM."IdTipoMovimientoFondoMutuo"
			   ,HOM."IdDividendo",HOM."IdSerieFondoDestino",HOM."IdEmisorDestino",HOM."IdClienteDestino",HOM."IdPortafolioDestino",HOM."IdSerieFondoOrigen"
			   ,HOM."IdEmisorOrigen",HOM."MontoOrigen",HOM."IndicadorMLT",HOM."Overnight",HOM."IdSolicitudAporteRescateRelacionado",HOM."IdTipoCartera"
			   ,HOM."CalculoPorMontoCuota",HOM."FechaEmision",HOM."FechaAceptacion",HOM."FechaCertificacion",HOM."FechaLiquidacion",HOM."FechaAnulacion"
			   ,HOM."FechaOriginalInversion",HOM."FechaIngresoRegistro",HOM."FechaAnulacionRegistro",HOM."FechaAceptacionRegistro",HOM."FechaCertificacionRegistro"
			   ,HOM."FechaLiquidacionRegistro",HOM."Monto",HOM."NumeroCuotas",HOM."ValorCuota",HOM."CostoUF",HOM."CostoMonedaLocal",HOM."ValorUFFechaInversion"
			   ,HOM."ParidadFechaInversion",HOM."IdMoneda",HOM."MontoComision",HOM."MontoPago",HOM."Estado",HOM."Glosa",HOM."IdEmpresa",HOM."IdUsuario"
			   ,HOM."IdModulo",HOM."EstadoLiquidacion",HOM."MueveCta",HOM."FolioCartaLiquidacion",HOM."IdMedioDePago",HOM."MontoInversion",HOM."NumeroCuotasInversion"
			   ,HOM."ValorCuotaInversion",HOM."CostoMonedaLocalInversion",HOM."ParidadFechaCertificacion",HOM."MontoComisionLocal",HOM."MontoPagoLocal"
			   ,HOM."FechaModificacion",HOM."FechaModificacionRegistro",HOM."OrigenOperacion",HOM."SolicitudBatch",HOM."ComisionInformada"
			   ,HOM."IdCarga"
			 FROM "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as HOM
				INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RUT_CLI" 
				  AND PER."SEC_RUT_CLI" = HOM."SEC_RUT_CLI" 
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
				  ON FMM."IdSolicitudAporteRescate" = HOM."IdSolicitudAporteRescate"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND FMM."IdSolicitudAporteRescate"  IS NULL;
		ELSE
			
			SELECT count(*) into v_Total_Operaciones_ffmm
	        FROM "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as HOM
			LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
			    ON FMM."IdSolicitudAporteRescate" = HOM."IdSolicitudAporteRescate"
			 WHERE HOM."IdCarga" = p_IdCarga
			   AND FMM."IdSolicitudAporteRescate"  IS NULL;

			INSERT INTO "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"(
				"IdSolicitudAporteRescate","IdCliente","IdPortafolio","TipoSolicitud","IdSerieFondo","IdTipoMovimientoFondoMutuo","IdDividendo"
				,"IdSerieFondoDestino","IdEmisorDestino","IdClienteDestino","IdPortafolioDestino","IdSerieFondoOrigen","IdEmisorOrigen"
				,"MontoOrigen","IndicadorMLT","Overnight","IdSolicitudAporteRescateRelacionado","IdTipoCartera","CalculoPorMontoCuota","FechaEmision"
				,"FechaAceptacion","FechaCertificacion","FechaLiquidacion","FechaAnulacion","FechaOriginalInversion","FechaIngresoRegistro"
				,"FechaAnulacionRegistro","FechaAceptacionRegistro","FechaCertificacionRegistro","FechaLiquidacionRegistro","Monto","NumeroCuotas"
				,"ValorCuota","CostoUF","CostoMonedaLocal","ValorUFFechaInversion","ParidadFechaInversion","IdMoneda","MontoComision","MontoPago"
				,"Estado","Glosa","IdEmpresa","IdUsuario","IdModulo","EstadoLiquidacion","MueveCta","FolioCartaLiquidacion","IdMedioDePago"
				,"MontoInversion","NumeroCuotasInversion","ValorCuotaInversion","CostoMonedaLocalInversion","ParidadFechaCertificacion","MontoComisionLocal"
				,"MontoPagoLocal","FechaModificacion","FechaModificacionRegistro","OrigenOperacion","SolicitudBatch","ComisionInformada"
				,"IdCarga"
			)		
			SELECT 
				HOM."IdSolicitudAporteRescate",HOM."IdCliente",HOM."IdPortafolio",HOM."TipoSolicitud",HOM."IdSerieFondo",HOM."IdTipoMovimientoFondoMutuo"
			   ,HOM."IdDividendo",HOM."IdSerieFondoDestino",HOM."IdEmisorDestino",HOM."IdClienteDestino",HOM."IdPortafolioDestino",HOM."IdSerieFondoOrigen"
			   ,HOM."IdEmisorOrigen",HOM."MontoOrigen",HOM."IndicadorMLT",HOM."Overnight",HOM."IdSolicitudAporteRescateRelacionado",HOM."IdTipoCartera"
			   ,HOM."CalculoPorMontoCuota",HOM."FechaEmision",HOM."FechaAceptacion",HOM."FechaCertificacion",HOM."FechaLiquidacion",HOM."FechaAnulacion"
			   ,HOM."FechaOriginalInversion",HOM."FechaIngresoRegistro",HOM."FechaAnulacionRegistro",HOM."FechaAceptacionRegistro",HOM."FechaCertificacionRegistro"
			   ,HOM."FechaLiquidacionRegistro",HOM."Monto",HOM."NumeroCuotas",HOM."ValorCuota",HOM."CostoUF",HOM."CostoMonedaLocal",HOM."ValorUFFechaInversion"
			   ,HOM."ParidadFechaInversion",HOM."IdMoneda",HOM."MontoComision",HOM."MontoPago",HOM."Estado",HOM."Glosa",HOM."IdEmpresa",HOM."IdUsuario"
			   ,HOM."IdModulo",HOM."EstadoLiquidacion",HOM."MueveCta",HOM."FolioCartaLiquidacion",HOM."IdMedioDePago",HOM."MontoInversion",HOM."NumeroCuotasInversion"
			   ,HOM."ValorCuotaInversion",HOM."CostoMonedaLocalInversion",HOM."ParidadFechaCertificacion",HOM."MontoComisionLocal",HOM."MontoPagoLocal"
			   ,HOM."FechaModificacion",HOM."FechaModificacionRegistro",HOM."OrigenOperacion",HOM."SolicitudBatch",HOM."ComisionInformada"
			   ,HOM."IdCarga"	
				FROM "BSCL.Homologacion.BD::HOM.SolicitudAporteRescate" as HOM
				LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" AS FMM
				  ON FMM."IdSolicitudAporteRescate" = HOM."IdSolicitudAporteRescate"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND FMM."IdSolicitudAporteRescate"  IS NULL;
		END IF;
		
		-- contamos los cargados
        select count(*) into v_Total_ffmm_Cargados
        from "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
        where "IdCarga" = p_IdCarga;
		 
		
         -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	     p_ResumenDatosProcesados =  select 'CARGASOLICITUDFMM' as "CodigoProceso" 
	   			  									,'FMM' as "Modulo" 
	   			 									,v_Total_Operaciones_ffmm as "TotalRegistrosSebra"
	   			  									,v_Total_ffmm_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

          --IF v_Total_Operaciones_Forward <> v_Total_Forward_Cargados THEN 
          IF v_Total_Operaciones_ffmm <> v_Total_ffmm_Cargados THEN 
			 p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_FMM' as "Codigo",'falla en la carga spot' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   	  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;