SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionFWDOptimus";
CREATE PROCEDURE
"BSCL.Homologacion.BD.Servicios::CargarOperacionFWDOptimus"(
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
			--Error interno no manejado  (El log serÃ¡ captado por la capa de XS control)		
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
	   DECLARE v_Total_Operaciones_fwd BIGINT:=0;
	   DECLARE v_Total_fwd_Cargados BIGINT :=0;
	   DECLARE v_Quantity  BIGINT :=0;
	   
/*	   select COUNT(1) INTO v_Quantity FROM :p_Personas;
	   
       IF v_Quantity > 0 THEN 
        
           SELECT count(*) into v_Total_Operaciones_fwd
           FROM "BSCL.Homologacion.BD::HOM.Operacion" as HOM
				INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RUT_CLI" 
				  AND PER."SEC_RUT_CLI" = HOM."SEC_RUT"
				LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
				  ON OPE."IdOperacion" = HOM."IdOperacion"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND OPE."IdOperacion"  IS NULL
			      AND HOM."IdTipoProducto" = 'MEFW';

		    insert into "BSCL"."BSCL.Mesa.BD::MES.Operacion"
				("IdOperacion","FechaOperacion","FolioOperacion","IdTipoProducto","IdTipoOperacion","IdOperacionRelacionada","IdInstrumento","IdMonedaPrincipal"
				,"IdMonedaSecundaria","IdModalidadLiquidacion","IdReferenciaMercado","IdPlaza","Intermesa","IdMesaOrigen","IdMesaDestino","IdCliente"
				,"IdPortafolio","IdClienteCorredor","IdPortafolioCorredor","IdTipoReajuste","MontoOperacion","MontoOperacionInicial","MontoOperacionFinal"
				,"IdMoneda","Tasa","MontoOperacionMoneda","MontoOperacionInicialMoneda","MontoOperacionFinalMoneda","IdMonedaLiquidacion","TCCierre","ParidadCierre"
				,"TCCosto","PuntosTasasForward","PuntosForward","TCForward","TasasMonedaPrincipal","TasasMonedaSecundaria","MontoDiferido","MontoMargen","IdFixing"
				,"FechaFixing","HoraFixing","IdSpotAsociado","IdClasificacionCartera","AfectaCustodia","PlazoEnDias","FechaVencimiento","FechaLiquidacion"
				,"IdCondicionLiquidacion","FechaCondicionLiquidacion","IdAgente","TasaAgente","IdEstado","FechaCreacion","IdUsuarioCreacion","IdTipoCanalIngreso"
				,"Glosa","SecuenciaCompromisoMigracion","IdCarga","MontoOperacionAnticipo","MontoOperacionInicialAnticipo","MontoOperacionFinalAnticipo","TasaAnticipo"
				,"MontoOperacionMonedaAnticipo","MontoOperacionInicialMonedaAnticipo","MontoOperacionFinalMonedaAnticipo","IdMonedaLiquidacionAnticipo","IdCondicionLiquidacionAnticipo"
				,"FechaCondicionLiquidacionAnticipo","IdEmpresa","IdUsuarioEdicion","FechaEdicion")
            select  
            	 HOM."IdOperacion",HOM."FechaOperacion",HOM."FolioOperacion",HOM."IdTipoProducto",HOM."IdTipoOperacion",HOM."IdOperacionRelacionada",HOM."IdInstrumento",HOM."IdMonedaPrincipal"
				,HOM."IdMonedaSecundaria",HOM."IdModalidadLiquidacion",HOM."IdReferenciaMercado",HOM."IdPlaza",HOM."Intermesa",HOM."IdMesaOrigen",HOM."IdMesaDestino",HOM."IdCliente"
				,HOM."IdPortafolio",HOM."IdClienteCorredor",HOM."IdPortafolioCorredor",HOM."IdTipoReajuste",HOM."MontoOperacion",HOM."MontoOperacionInicial",HOM."MontoOperacionFinal"
				,HOM."IdMoneda",HOM."Tasa",HOM."MontoOperacionMoneda",HOM."MontoOperacionInicialMoneda",HOM."MontoOperacionFinalMoneda",HOM."IdMonedaLiquidacion",HOM."TCCierre",HOM."ParidadCierre"
				,HOM."TCCosto",HOM."PuntosTasasForward",HOM."PuntosForward",HOM."TCForward",HOM."TasasMonedaPrincipal",HOM."TasasMonedaSecundaria",HOM."MontoDiferido",HOM."MontoMargen",HOM."IdFixing"
				,HOM."FechaFixing",HOM."HoraFixing",HOM."IdSpotAsociado",HOM."IdClasificacionCartera",HOM."AfectaCustodia",HOM."PlazoEnDias",HOM."FechaVencimiento",HOM."FechaLiquidacion"
				,HOM."IdCondicionLiquidacion",HOM."FechaCondicionLiquidacion",HOM."IdAgente",HOM."TasaAgente",HOM."IdEstado",HOM."FechaCreacion",HOM."IdUsuarioCreacion",HOM."IdTipoCanalIngreso"
				,HOM."Glosa",HOM."SecuenciaCompromisoMigracion",HOM."IdCarga",HOM."MontoOperacionAnticipo",HOM."MontoOperacionInicialAnticipo",HOM."MontoOperacionFinalAnticipo",HOM."TasaAnticipo"
				,HOM."MontoOperacionMonedaAnticipo",HOM."MontoOperacionInicialMonedaAnticipo",HOM."MontoOperacionFinalMonedaAnticipo",HOM."IdMonedaLiquidacionAnticipo",HOM."IdCondicionLiquidacionAnticipo"
				,HOM."FechaCondicionLiquidacionAnticipo",HOM."IdEmpresa",HOM."IdUsuarioEdicion",HOM."FechaEdicion"
            FROM "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM
            INNER JOIN :p_Personas AS PER
				   ON PER."RUT_CLI" = HOM."RUT_CLI" 
				  AND PER."SEC_RUT_CLI" = HOM."SEC_RUT" 
             LEFT JOIN "BSCL"."BSCL.Mesa.BD::MES.Operacion" as OPE
            	   ON OPE."IdOperacion" = HOM."IdOperacion"
            WHERE OPE."IdOperacion" IS NULL
              AND HOM."IdCarga" = p_IdCarga
              AND HOM."IdTipoProducto" = 'MEFW';    
                
		ELSE*/
           
           SELECT count(*) into v_Total_Operaciones_fwd
           FROM "BSCL.Homologacion.BD::HOM.Operacion" as HOM
		   LEFT JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE
				  ON OPE."IdOperacion" = HOM."IdOperacion"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND OPE."IdOperacion"  IS NULL
			      AND HOM."IdTipoProducto" = 'MEFW';

		   insert into "BSCL"."BSCL.Mesa.BD::MES.Operacion"
				("IdOperacion","FechaOperacion","FolioOperacion","IdTipoProducto","IdTipoOperacion","IdOperacionRelacionada","IdInstrumento","IdMonedaPrincipal"
				,"IdMonedaSecundaria","IdModalidadLiquidacion","IdReferenciaMercado","IdPlaza","Intermesa","IdMesaOrigen","IdMesaDestino","IdCliente"
				,"IdPortafolio","IdClienteCorredor","IdPortafolioCorredor","IdTipoReajuste","MontoOperacion","MontoOperacionInicial","MontoOperacionFinal"
				,"IdMoneda","Tasa","MontoOperacionMoneda","MontoOperacionInicialMoneda","MontoOperacionFinalMoneda","IdMonedaLiquidacion","TCCierre","ParidadCierre"
				,"TCCosto","PuntosTasasForward","PuntosForward","TCForward","TasasMonedaPrincipal","TasasMonedaSecundaria","MontoDiferido","MontoMargen","IdFixing"
				,"FechaFixing","HoraFixing","IdSpotAsociado","IdClasificacionCartera","AfectaCustodia","PlazoEnDias","FechaVencimiento","FechaLiquidacion"
				,"IdCondicionLiquidacion","FechaCondicionLiquidacion","IdAgente","TasaAgente","IdEstado","FechaCreacion","IdUsuarioCreacion","IdTipoCanalIngreso"
				,"Glosa","SecuenciaCompromisoMigracion","IdCarga","MontoOperacionAnticipo","MontoOperacionInicialAnticipo","MontoOperacionFinalAnticipo","TasaAnticipo"
				,"MontoOperacionMonedaAnticipo","MontoOperacionInicialMonedaAnticipo","MontoOperacionFinalMonedaAnticipo","IdMonedaLiquidacionAnticipo","IdCondicionLiquidacionAnticipo"
				,"FechaCondicionLiquidacionAnticipo","IdEmpresa","IdUsuarioEdicion","FechaEdicion","IdEmisor","FolioCartera")
            select  
            	 HOM."IdOperacion",HOM."FechaOperacion",HOM."FolioOperacion",HOM."IdTipoProducto",HOM."IdTipoOperacion",HOM."IdOperacionRelacionada",HOM."IdInstrumento",HOM."IdMonedaPrincipal"
				,HOM."IdMonedaSecundaria",HOM."IdModalidadLiquidacion",HOM."IdReferenciaMercado",HOM."IdPlaza",HOM."Intermesa",HOM."IdMesaOrigen",HOM."IdMesaDestino",HOM."IdCliente"
				,HOM."IdPortafolio",HOM."IdClienteCorredor",HOM."IdPortafolioCorredor",HOM."IdTipoReajuste",HOM."MontoOperacion",HOM."MontoOperacionInicial",HOM."MontoOperacionFinal"
				,HOM."IdMoneda",HOM."Tasa",HOM."MontoOperacionMoneda",HOM."MontoOperacionInicialMoneda",HOM."MontoOperacionFinalMoneda",HOM."IdMonedaLiquidacion",HOM."TCCierre",HOM."ParidadCierre"
				,HOM."TCCosto",HOM."PuntosTasasForward",HOM."PuntosForward",HOM."TCForward",HOM."TasasMonedaPrincipal",HOM."TasasMonedaSecundaria",HOM."MontoDiferido",HOM."MontoMargen",HOM."IdFixing"
				,HOM."FechaFixing",HOM."HoraFixing",HOM."IdSpotAsociado",HOM."IdClasificacionCartera",HOM."AfectaCustodia",HOM."PlazoEnDias",HOM."FechaVencimiento",HOM."FechaLiquidacion"
				,HOM."IdCondicionLiquidacion",HOM."FechaCondicionLiquidacion",HOM."IdAgente",HOM."TasaAgente",HOM."IdEstado",HOM."FechaCreacion",HOM."IdUsuarioCreacion",HOM."IdTipoCanalIngreso"
				,HOM."Glosa",HOM."SecuenciaCompromisoMigracion",HOM."IdCarga",HOM."MontoOperacionAnticipo",HOM."MontoOperacionInicialAnticipo",HOM."MontoOperacionFinalAnticipo",HOM."TasaAnticipo"
				,HOM."MontoOperacionMonedaAnticipo",HOM."MontoOperacionInicialMonedaAnticipo",HOM."MontoOperacionFinalMonedaAnticipo",HOM."IdMonedaLiquidacionAnticipo",HOM."IdCondicionLiquidacionAnticipo"
				,HOM."FechaCondicionLiquidacionAnticipo",HOM."IdEmpresa",HOM."IdUsuarioEdicion",HOM."FechaEdicion",HOM."IdEmisor",HOM."FolioCartera"

            FROM "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM
             LEFT JOIN "BSCL"."BSCL.Mesa.BD::MES.Operacion" as OPE
            	   ON OPE."IdOperacion" = HOM."IdOperacion"
            WHERE OPE."IdOperacion" IS NULL
              AND HOM."IdCarga" = p_IdCarga
              AND HOM."IdTipoProducto" = 'MEFW';  
		--END IF;

		insert into "BSCL.Mesa.BD::MES.ComprobantePagoOperacion"
				   ("IdComprobantePagoOperacion","IdEmpresa","IdOperacion","IdIngresoEgreso"
					,"IdAbono","IdCargo","IdTipoProducto","IdFormaDePago","RecibePaga"
					,"IdModalidadPago","IdMoneda","Monto"
					,"FechaIngresoComprobantePagoOperacion"
					,"FechaLiquidacionComprobantePagoOperacion"
					,"IdEstado"
					,"AfectarTesoreria"
					,"IdIngresoEgresoAnticipo"
					,"IdAbonoAnticipo"
					,"IdCargoAnticipo"
					)	
		
		SELECT	     HOM."IdComprobantePagoOperacion"
				   	,HOM."IdEmpresa"
				   	,HOM."IdOperacion"
				   	,HOM."IdIngresoEgreso"
					,HOM."IdAbono"
					,HOM."IdCargo"
					,HOM."IdTipoProducto"
					,HOM."IdFormaDePago"
					,HOM."RecibePaga"
					,HOM."IdModalidadPago"
					,HOM."IdMoneda"
					,HOM."Monto"
					,HOM."FechaIngresoComprobantePagoOperacion"
					,HOM."FechaLiquidacionComprobantePagoOperacion"
					,HOM."IdEstado"
					,HOM."AfectarTesoreria"
					,HOM."IdIngresoEgresoAnticipo"
					,HOM."IdAbonoAnticipo"
					,HOM."IdCargoAnticipo"
		
		FROM "BSCL"."BSCL.Homologacion.BD::HOM.ComprobantePagoOperacion" as HOM
		WHERE HOM."IdCarga" = p_IdCarga
		AND  HOM."IdTipoProducto" = 'MEFW';
		
		-- contamos los cargados
        select count(*) into v_Total_fwd_Cargados
        from "BSCL.Mesa.BD::MES.Operacion"
        where "IdCarga" = p_IdCarga
        and "IdTipoProducto" = 'MEFW';
	
        
        -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	    p_ResumenDatosProcesados =  select 'CARGAOPERACIONESFWD' as "CodigoProceso" 
	   			  									,'FWD' as "Modulo" 
	   			 									,v_Total_Operaciones_fwd as "TotalRegistrosSebra"
	   			  									,v_Total_fwd_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

          IF v_Total_Operaciones_fwd <> v_Total_fwd_Cargados THEN 
			 p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_FWD' as "Codigo",'falla en la carga fwd' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   	  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
