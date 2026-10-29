SET SCHEMA "BSCL"; 
	DROP PROCEDURE   "BSCL.Homologacion.BD.Servicios::CargarMovimientoCargoAbonoInicial";
	CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarMovimientoCargoAbonoInicial"
	(  
	 	IN  p_IdEmpresa		   			BIGINT,			-- ID Empresa destino
     	IN  p_IdUsuario		   			BIGINT,			-- ID del usuario que solicita el servicio
	 	IN  p_IdCarga 	       			BIGINT,          -- ID de la carga 
	 	IN  p_IdModulo         			NVARCHAR(3),     -- modulo 
     	IN  P_Modalidad        			NVARCHAR(1),     -- Ingreso I incremental, 
     	IN  p_RutCliente       			NVARCHAR(11) default '0-0',    -- rut del cliente sebra
	 	IN  p_FechaInicial              NVARCHAR(10),   --YYYY-MM-DD
		IN  p_FechaFinal				NVARCHAR(10),   --YYYY-MM-DD
		OUT p_out_ErroresContador 		int,
		OUT p_out_ErroresDetalle table ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(5000)),
		OUT p_ResumenDatosProcesados  table ("CodigoProceso" NVARCHAR(40),"IdModulo" NVARCHAR(40),"TotalRegistrosSebra" DECIMAL(26,8),"TotalRegistrosSap" DECIMAL(26,8)) 
    )    
    
		LANGUAGE SQLSCRIPT
		SQL SECURITY INVOKER 
		DEFAULT SCHEMA "BSCL"
		---READS SQL DATA 
	AS
	BEGIN
		-- VARIABLES VALIDACIONES BASICAS
		DECLARE v_out_Resultado  			CHAR(1); 	   --RESULTADO S/N/E
	    DECLARE v_out_Mensaje	 			NVARCHAR(5000); --MENSAJE SALIDA
	    
	    DECLARE v_erroroutmsg  				NVARCHAR(5000);    
	    DECLARE v_TotalMovimientos			BIGINT;	    
	    DECLARE v_TotalMovimientosGrabados	BIGINT;
	    
	    -- Para LOg   
	    DECLARE v_out_ErroresContador	BIGINT; 
	    DECLARE v_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000));	
	    DECLARE v_FechaSistema NVARCHAR(40):='0001-01-01';
	    DECLARE v_MaxPeriodo NVARCHAR(40);
	    DECLARE v_FechaInicioPeriodo NVARCHAR(10);
	     
		--------- CONTROL DE ERRORES
		--- Controlado
		DECLARE SALIDA_CONTROLADA CONDITION;    	
		DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
		BEGIN
			IF :p_out_ErroresContador = 0 THEN	 
		 	    p_out_ErroresDetalle       =  select 'INGRESO OK' as "Codigo",'LOG REGISTRADO' as "Descripcion" from dummy;-- UNION ALL
			END IF;		 					 	
		END;	
		--- Excepcion
		DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
			declare v_errorExt nvarchar(40);
			IF ::SQL_ERROR_CODE > 10000 THEN
				call "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
				p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
									   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
				select count(1) into p_out_ErroresContador 
				from :p_out_ErroresDetalle;										
			ELSE
				--Error interno no manejado  (El log será captado por la capa de XS control)
				p_out_ErroresDetalle       =  select 'ERROR' as "Codigo", ::SQL_ERROR_CODE as "Descripcion" from dummy;-- UNION ALL		
				RESIGNAL; 
			END IF;			
		END;
		
		p_out_ErroresContador := 0;
		p_out_ErroresDetalle = select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;   	
        
        SELECT CONCAT(SUBSTRING(p_FechaInicial,1,8),'01') INTO v_FechaInicioPeriodo FROM DUMMY; 		
        
        SELECT "Valor" INTO v_FechaSistema
	    FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	    WHERE "CodigoParametro" = 'PLA.FechaSistema'
	    AND "IdEmpresa" = p_IdEmpresa;
        
        -- recupero el maximo periodo anterior a la fecha inicio ingresada 
        select SUBSTRING(TO_VARCHAR(ADD_MONTHS(TO_DATE(p_FechaInicial,'YYYY-MM-DD'),-1),'YYYY-MM-DD'),1,7) INTO v_MaxPeriodo from dummy;

       -- insertamos movimientos en la tabla de movimientos de cuenta de inversion
       -- el periodo anterior a la fecha ingreso inicial 
       -- con esto generamos el cargo u abono en caso que sea negativo generamos un cargo
       -- en caso que sea posivo generamos un abono en caso que sea cero no insertamos 
       -- un movimiento evitamos que se ingresen nuevo abonos iniciales del mismo mes

      INSERT INTO "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"
      ("IdCargoAbonoMovimiento" ,"FechaSistema" ,"IdModulo" ,"IdTipoMovimiento" ,"EsIngresoManual" 
	  ,"IdCuentaInversion" ,"NombreCliente" ,"IdConcepto" ,"Descripcion" ,"IdCondicionDeLiquidacion" ,"FechaEfectivaMovimiento" 
	  ,"Monto" ,"Saldo" ,"IdEvento" ,"EsCertificadoAutomatico" ,"IdUsuarioCertificador" ,"FechaCertificacion" ,"IdTipoComprobante" 
	  ,"IdComprobante" ,"FechaComprobante"  ,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion"
	  ,"FechaLiquidacionOperacion" ,"FechaVencimientoOperacion" ,"IdMonedaOperacion" ,"MontoOperacion" ,"TipoCambioOperacion"
	  ,"IdTipoInstrumento" ,"IdInstrumento" ,"Nemo" ,"EsCargoAbonoPorTraspaso" ,"IdCargoAbonoRelacionadoTraspaso" ,"EsIndicadorBilateral" 
	  ,"IdContraparte" ,"IdPersonaPagoCobro" ,"FechaIngresoRegistro" ,"IdUsuario" ,"IdEmpresa" ,"IdEstado" ,"IdCarga" 
	  ,"SecuenciaMovimientoMigracion","RUT_CLI","SEC_RUT_CLI","FEC_MVTO","SEC_MOVTO"	,"COD_MOVTO","FOLIO_COMP_ADJ"
	  ,"TIPO_COMP_ADJ","LIN_COMP_ADJ","COD_BOVEDA","COD_SUC","FEC_TRANS","FEC_LIQUID","FEC_ING_MVTO","IND_TIT_CCTE","COD_PER")
       select 
             "BSCL.CuentaInversion.BD.Secuencias::CargoAbonoMovimiento".NEXTVAL as "IdCargoAbonoMovimiento"
             ,v_FechaSistema as "FechaSistema"
             ,'CTA' as "IdModulo"
            -- , CASE WHEN (GES_CTA."SALDO_DISP"+GES_CTA."SALDO_RETEN" + GES_CTA."SALDO_TP") < 0 THEN 'MOVCAR' ELSE 'MOVABN' END as "IdTipoMovimiento"
             , CASE WHEN GES_CTA."SALDO_DISP" < 0 THEN 'MOVCAR' ELSE 'MOVABN' END as "IdTipoMovimiento"
             , 'S' "EsIngresoManual"
             , SAP_CTA_INV."IdCuentaInversion" as "IdCuentaInversion"
             , PER."NombreCompleto" as "NombreCliente"
             -- , CASE WHEN (GES_CTA."SALDO_DISP"+GES_CTA."SALDO_RETEN" + GES_CTA."SALDO_TP") < 0 then 'CAMIGR' else 'ABMIGR' end as "IdConcepto"
			 , CASE WHEN GES_CTA."SALDO_DISP" < 0 then 'CAMIGR' else 'ABMIGR' end as "IdConcepto"
             , concat('Movimiento Inicial -', to_varchar(p_IdCarga)) as "Descripcion"
             , 'T0' as "IdCondicionLiquidacion"
             , v_FechaInicioPeriodo as "FechaEfectivaMovimiento"-- FECHA DEL SISTEMA
           --, (GES_CTA."SALDO_DISP"+GES_CTA."SALDO_RETEN" + GES_CTA."SALDO_TP") as "Monto"
             ,GES_CTA."SALDO_DISP" as "Monto"
           --  , (GES_CTA."SALDO_DISP"+GES_CTA."SALDO_RETEN" + GES_CTA."SALDO_TP") as "Saldo"
             ,GES_CTA."SALDO_DISP" as "Saldo"
             , 0 as "IdEvento"
             ,'S' as "EsCertificadoAutomatico"
             , 0 as "IdUsuarioCertificador"
             , v_FechaInicioPeriodo as "FechaCertificacion"-- FECHA DEL SISTEMA
             , '' as "IdTipoComprobante"
             , 0 as "IdComprobante"
             , '0001-01-01' as "FechaComprobante"
             ,'' as "IdTipoProducto"
             ,'' as "IdTipoOperacion"
             ,0 as "IdOperacion"
             ,v_FechaInicioPeriodo as "FechaOperacion"
             ,v_FechaInicioPeriodo as "FechaLiquidacionOperacion"
             ,'0001-01-01' as "FechaVencimiento"
             ,SAP_CTA_INV."IdMoneda" as "IdMonedaOperacion"
             ,0 as "MontoOperacion" 
             ,0 as "TipoCambioOperacion"
             ,'' as "IdTipoInstrumento"
             ,0 as "IdInstrumento"
             ,'' as "Nemo"
             ,'N' as "EsCargoAbonoPorTraspaso"
             , -1 as "IdCargoAbonoRelacionadoTraspaso"
             ,'N' as "EsIndicadorBilateral"
             ,0 as "IdContraparte"
             ,0 as "IdPersonaPagoCobro"
             ,v_FechaInicioPeriodo as "FechaIngresoRegistro"
             ,p_IdUsuario as "IdUsuario"
             ,p_IdEmpresa as "IdEmpresa"
             ,'CRT' as "IdEstado"
             ,p_IdCarga as "IdCarga"
             ,0 as "SecuenciaMovimientoMigracion"
             ,GES_CTA."RUT_CLI" as "RUT_CLI"
             ,GES_CTA."SEC_RUT_CLI" as "SEC_RUT_CLI"
             ,v_FechaInicioPeriodo as "FEC_MVTO"
             , 0 as "SEC_MOVTO"
             , '' as "COD_MOVTO"
             , 0 as "FOLIO_COMP_ADJ"
	         ,'' as "TIPO_COMP_ADJ"
	         , 0 as "LIN_COMP_ADJ"
	         , '' as "COD_BOVEDA"
	         , '' as "COD_SUC"
	         ,'0001-01-01' as "FEC_TRANS"
	         ,v_FechaInicioPeriodo as "FEC_LIQUID"
	         ,v_FechaInicioPeriodo as "FEC_ING_MVTO"
	         ,'' as "IND_TIT_CCTE"
	         ,SUBSTRING(v_FechaInicioPeriodo,1,7) as "COD_PER"
         from "BSCL_VIRTUAL"."GES_TBCTCTCL" as GES_CTA
         inner join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLI
          on CLI."DocumentoIdentidad" = TRIM(GES_CTA."RUT_CLI")
         inner join "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER
          on  PER."IdPersona" = CLI."IdPersona"
          and PER."DocumentoIdentidad" = case when p_RutCliente='0-0' then PER."DocumentoIdentidad" else TRIM(p_RutCliente) end 
          and PER."IdCarga" = P_IdCarga
         inner join "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as POR
          on POR."IdCliente" = CLI."IdCliente"
          and POR."CodigoPortafolio" = TO_VARCHAR(GES_CTA."SEC_RUT_CLI")
         inner join "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as SAP_CTA_INV
          on SAP_CTA_INV."IdPortafolio" = POR."IdPortafolio"
           and SAP_CTA_INV."IdMoneda" = CASE WHEN GES_CTA."CTA_CLI" = 'CCTE' THEN 'CLP'
                                             WHEN GES_CTA."CTA_CLI" = 'CDOL' THEN 'USD'
                                             WHEN GES_CTA."CTA_CLI" = 'EURO' THEN 'EUR'
                                        END
         left join "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS GES_CARABO
         on GES_CARABO."IdCuentaInversion" =  SAP_CTA_INV."IdCuentaInversion" 
         and GES_CARABO."IdConcepto" in ('CAMIGR','ABMIGR')
         and GES_CARABO."COD_PER" = SUBSTRING(p_FechaInicial,1,7)
         where GES_CTA."COD_PER" = v_MaxPeriodo
         and GES_CTA."SALDO_DISP" <> 0
         and GES_CARABO."IdConcepto" IS NULL;

		 -- contamos los registros que fueron seleccionados 
         SELECT COUNT(*) INTO v_TotalMovimientos 
         FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento"  	
         WHERE "IdCarga" = p_IdCarga
         AND "IdConcepto" in ('CAMIGR','ABMIGR');
         
     -- debo revisar los moviminetos de cargo y abono iniciales    
     INSERT INTO "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" 
       		 ("IdCargoAbonoMovimiento","FechaSistema" ,"IdModulo" ,"IdTipoMovimiento","EsIngresoManual" ,"IdCuentaInversion" ,"NombreCliente"
			,"IdConcepto" ,"Descripcion","IdCondicionDeLiquidacion" ,"FechaEfectivaMovimiento","Monto","Saldo","SaldoPorFechaMovimiento","IdEvento" 
			,"EsCertificadoAutomatico","IdUsuarioCertificador" ,"FechaCertificacion","IdTipoComprobante" ,"IdComprobante","FechaComprobante"
			,"IdTipoProducto","IdTipoOperacion" ,"IdOperacion","FechaOperacion","FechaLiquidacionOperacion","FechaVencimientoOperacion","IdMonedaOperacion" 
			,"MontoOperacion","TipoCambioOperacion" ,"IdTipoInstrumento" ,"IdInstrumento","Nemo","EsCargoAbonoPorTraspaso","IdCargoAbonoRelacionadoTraspaso"
			,"EsIndicadorBilateral","IdContraparte","IdPersonaPagoCobro","FechaIngresoRegistro","IdUsuario","IdEmpresa","IdEstado" ,"IdCarga","SecuenciaMovimientoMigracion" 
			,"FechaMovimientoMigracion","CodigoPeriodo") 
       SELECT HOM."IdCargoAbonoMovimiento",HOM."FechaSistema" ,HOM."IdModulo" ,HOM."IdTipoMovimiento",HOM."EsIngresoManual" ,HOM."IdCuentaInversion" ,HOM."NombreCliente"
			,HOM."IdConcepto" ,HOM."Descripcion",HOM."IdCondicionDeLiquidacion" ,HOM."FechaEfectivaMovimiento",HOM."Monto",HOM."Saldo",HOM."SaldoPorFechaMovimiento",HOM."IdEvento" 
			,HOM."EsCertificadoAutomatico",HOM."IdUsuarioCertificador" ,HOM."FechaCertificacion",HOM."IdTipoComprobante" ,HOM."IdComprobante",HOM."FechaComprobante"
			,HOM."IdTipoProducto",HOM."IdTipoOperacion" ,HOM."IdOperacion",HOM."FechaOperacion",HOM."FechaLiquidacionOperacion",HOM."FechaVencimientoOperacion",HOM."IdMonedaOperacion" 
			,HOM."MontoOperacion",HOM."TipoCambioOperacion" ,HOM."IdTipoInstrumento" ,HOM."IdInstrumento",HOM."Nemo",HOM."EsCargoAbonoPorTraspaso",HOM."IdCargoAbonoRelacionadoTraspaso"
			,HOM."EsIndicadorBilateral",HOM."IdContraparte",HOM."IdPersonaPagoCobro",HOM."FechaIngresoRegistro",HOM."IdUsuario",HOM."IdEmpresa",HOM."IdEstado" ,HOM."IdCarga",HOM."SecuenciaMovimientoMigracion" 
			,HOM."FechaMovimientoMigracion",HOM."COD_PER"
	    FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS HOM
	    LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CARABO
         ON CARABO."IdCuentaInversion" =  HOM."IdCuentaInversion" 
         AND CARABO."CodigoPeriodo" = HOM."COD_PER"
         AND CARABO."IdConcepto" IN ('CAMIGR','ABMIGR')
	    WHERE HOM."IdCarga" = p_IdCarga
	     AND  HOM."IdConcepto" in ('CAMIGR','ABMIGR')
		 AND  CARABO."CodigoPeriodo" IS NULL; 	
	     
	    -- contamos los que cargamos 
  		 SELECT COUNT(*) INTO v_TotalMovimientosGrabados 
  		 FROM "BSCL.Homologacion.BD::HOM.CargoAbonoMovimiento" AS HOM
         INNER JOIN  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CTA 	
         ON CTA."IdCargoAbonoMovimiento" = HOM."IdCargoAbonoMovimiento"
         WHERE HOM."IdCarga" = p_IdCarga
         AND HOM."IdConcepto" in ('CAMIGR','ABMIGR')
         AND HOM."COD_PER" = SUBSTRING(p_FechaInicial,1,7);
         
         IF v_TotalMovimientosGrabados <> v_TotalMovimientos THEN
	    	v_hash= select 'Valor1' as "Codigo", v_TotalMovimientosGrabados   as "Valor" FROM DUMMY UNION ALL
			        select 'Valor2' as "Codigo", v_TotalMovimientos as "Valor" FROM DUMMY;
			CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ERROR_CARGA_CTA_INV',:v_hash,:v_erroroutmsg);
			p_out_ErroresDetalle =  select 'ERROR_CARGA_CTA_INV' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy; 
			p_out_ErroresContador := p_out_ErroresContador + 1;
	    END IF;	    
	    
	    -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar

	   	p_ResumenDatosProcesados =  select 'CARGASALDOINICIAL' as "CodigoProceso" 
   			  							,'CTA' as "IdModulo" 
   			 							,v_TotalMovimientos as "TotalRegistrosSebra"
   			  							,v_TotalMovimientosGrabados as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;
   							    	
		SIGNAL SALIDA_CONTROLADA;         
	END;
