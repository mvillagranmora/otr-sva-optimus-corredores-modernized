SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOperacionMesaRF";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 


"BSCL.Homologacion.BD.Servicios::CargarOperacionMesaRF"(
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
	----------------------------------------------------
	--Declaraciones Variables
	-----------------------------------------------------
	-- declaracion variables
	   DECLARE v_total_mov_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_mov_cargadas DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;
	   DECLARE v_periodofinal DATE := CONCAT(SUBSTRING(p_FechaFinal,1,7),'-01'); 
	   DECLARE v_count_ope BIGINT :=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
	   DECLARE v_MonedaSistema NVARCHAR(40):='';
	   DECLARE v_RutCli NVARCHAR(11):='0-0';
    
		--------------------
		--Negocio Aqui
		--------------------

        						
        -----------------------------------------
        -- ELIMINO ERRORES DE ANTERIORES CARGAS--
        -----------------------------------------

        delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error"
        where "IdCarga" = p_IdCarga 
          and "Entidad" ='MESARF';

        if p_Modalidad ='R' then 
           delete from "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" 
           where "IdCarga" = p_IdCarga;
        end if;

        v_operaciones = SELECT * FROM 			       		 
							(			       		 
								select	 
								      RANK() over (partition by  "SEC_COMPROM" order by "COD_PER" desc ) as RANK
									 ,"SEC_COMPROM"
									 ,"COD_PER"
								from "BSCL_VIRTUAL"."GES_TBCPMSPC" where "FEC_OPERAC" BETWEEN p_FechaInicial AND p_FechaFinal
							)
						where RANK = 1;

        
        select count(1) into v_count_ope from :v_operaciones;
        
	    IF v_count_ope > 0 THEN
	 
	       insert into "BSCL"."BSCL.Homologacion.BD::HOM.Operacion"(
				"IdOperacion","FechaOperacion" ,"FolioOperacion" ,"IdTipoProducto" ,"IdTipoOperacion","IdOperacionRelacionada" ,"IdInstrumento" ,"IdMonedaPrincipal" ,"IdMonedaSecundaria" 
				,"IdModalidadLiquidacion" ,"IdReferenciaMercado" ,"IdPlaza","Intermesa","IdMesaOrigen","IdMesaDestino","IdCliente" ,"IdPortafolio" ,"IdClienteCorredor" ,"IdPortafolioCorredor" 
				,"IdTipoReajuste","MontoOperacion" ,"MontoOperacionInicial" ,"MontoOperacionFinal","IdMoneda","Tasa","MontoOperacionMoneda","MontoOperacionInicialMoneda","MontoOperacionFinalMoneda","IdMonedaLiquidacion" 
				,"TCCierre" ,"ParidadCierre" ,"TCCosto" ,"PuntosTasasForward" 
				,"PuntosForward" ,"TCForward" ,"TasasMonedaPrincipal" ,"TasasMonedaSecundaria" ,"MontoDiferido" ,"MontoMargen" ,"IdFixing" ,"FechaFixing" 
				,"HoraFixing" ,"IdSpotAsociado" ,"IdClasificacionCartera" ,"AfectaCustodia" ,"PlazoEnDias" ,"FechaVencimiento" ,"FechaLiquidacion" ,"IdCondicionLiquidacion" 
				,"FechaCondicionLiquidacion" ,"IdAgente" ,"TasaAgente" ,"IdEstado" ,"FechaCreacion" ,"IdUsuarioCreacion" ,"IdTipoCanalIngreso" ,"Glosa"
				,"SecuenciaCompromisoMigracion","IdCarga", "RUT_CLI","SEC_RUT","RUT_CORR","SEC_RUT_CORR","SEC_COMPROM","TIPO_COMPROM","COD_PER"
                )
			select 	"BSCL.Custodia.BD.Secuencias::Operacion".NEXTVAL as "IdOperacion" 
	 				,TBCPMSPC."FEC_OPERAC"   as "FechaOperacion" 
					, 0 as "FolioOperacion" 
					, 'MERF' as "IdTipoProducto" 
					, CASE WHEN TBCPMSPC."TIPO_COMPROM" = 'VP' THEN 'VENPAC'
					       WHEN TBCPMSPC."TIPO_COMPROM" = 'CP' THEN 'COMPAC'
					       WHEN TBCPMSPC."TIPO_COMPROM" = 'VT' THEN 'VENTER'
					       WHEN TBCPMSPC."TIPO_COMPROM" = 'CT' THEN 'COMTER'
					  END as "IdTipoOperacion" 
					, 0  as "IdOperacionRelacionada"
					, 0  as "IdInstrumento"  ---REVISAR
					, '' as "IdMonedaPrincipal" 
					, '' as "IdMonedaSecundaria" 
					, 0  as "IdModalidadLiquidacion" -- se utiliza para forward compensacion o entrega fisica
					, 0 as  "IdReferenciaMercado" --  
					, 0 as "IdPlaza"
					, '' as "Intermesa"
					, 1  as "IdMesaOrigen"
					, 0  as "IdMesaDestino"
					, CLIENTE."IdCliente" as "IdCliente" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolio" 
					, CLIENTE_CORREDOR."IdCliente"  as "IdClienteCorredor" -- id del corredor 
					, PORTAFOLIO_CORREDOR."IdPortafolio" as "IdPortafolioCorredor" -- id del corredor
                    , TIPO_REAJUSTE."ValorDestino" as "IdTipoReajuste" 
					, 0 as "MontoOperacion" 
					, TBCPMSPC."MONTO_CAPITAL" as "MontoOperacionInicial" 
					, TBCPMSPC."MONTO_COMPROM" as "MontoOperacionFinal" 
					, '' as "IdMoneda"
					, TBCPMSPC."TASA_COMPROM" as "Tasa"
					, 0 as "MontoOperacionMoneda"
					,0 as  "MontoOperacionInicialMoneda"
					,0 as  "MontoOperacionFinalMoneda"
					,'' as "IdMonedaLiquidacion"
					, 0 as "TCCierre" 
					, 0 as "ParidadCierre" 
					, 0 as "TCCosto" 
					, 0 as "PuntosTasasForward" 
					, 0 as "PuntosForward" 
					, 0 as "TCForward" 
					, 0 as "TasasMonedaPrincipal" 
					, 0 as "TasasMonedaSecundaria" 
					, 0 as "MontoDiferido" 
					, 0 as "MontoMargen" 
					, 0 as "IdFixing" 
					, '0001-01-01' as "FechaFixing" 
					, '00:00' as "HoraFixing" 
					, 0 as "IdSpotAsociado" 
					, 0 as "IdClasificacionCartera" 
					, '' as "AfectaCustodia" 
					, TBCPMSPC."PLAZO" as "PlazoEnDias" 
					, TBCPMSPC."FEC_VCTO_COM" as "FechaVencimiento" 
					, TBCPMSPC."FEC_LIQUID" as "FechaLiquidacion" 
					, '' as "IdCondicionLiquidacion" 
					, '0001-01-01' as "FechaCondicionLiquidacion" 
					, EMPLEADO."IdEmpleado" as "IdAgente" 
					, TBCPMSPC."TASA_AGE" as "TasaAgente" 
					, '' as "IdEstado" --debo homologar los estados de pactos
					, TBCPMSPC."FEC_OPERAC" as "FechaCreacion" 
					, 0 as "IdUsuarioCreacion" 
					, '' as "IdTipoCanalIngreso" 
					, '' as "Glosa"
					,TBCPMSPC."SEC_COMPROM" as "SecuenciaCompromisoMigracion"
					,p_IdCarga as "IdCarga"
					,TBCPMSPC."RUT_CLI" as "RUT_CLI"
					,TBCPMSPC."SEC_RUT_CLI" as "SEC_RUT"
					,TBCPMSPC."RUT_CORR" as "RUT_CORR"
					,TBCPMSPC."SEC_RUT_CORR" as "SEC_RUT_CORR"
					,TBCPMSPC."SEC_COMPROM" as "SEC_COMPROM"
					,TBCPMSPC."TIPO_COMPROM" as "TIPO_COMPROM"
					,TBCPMSPC."COD_PER" as "COD_PER"
			        FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" as TBCPMSPC
			        INNER JOIN :v_operaciones as OPERACIONES
			        	ON OPERACIONES."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
			        	AND OPERACIONES."COD_PER" = TBCPMSPC."COD_PER" 
			        INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CLI")
			        INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI")
			        INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE_CORREDOR
			         	ON CLIENTE_CORREDOR."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CORR")
			        INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO_CORREDOR
			            ON PORTAFOLIO_CORREDOR."IdCliente" = CLIENTE_CORREDOR."IdCliente"
			            AND PORTAFOLIO_CORREDOR."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CORR")
			        LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as TIPO_REAJUSTE
					  	ON  TIPO_REAJUSTE."TipoValor" = 'TipoReajuste'
					  	AND TIPO_REAJUSTE."Entidad"  = 'FACTURA'
					  	AND TIPO_REAJUSTE."ValorOrigen" = TBCPMSPC."TIPO_REAJ_COMP"
					LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::EMP.Empleado" as EMPLEADO
						ON EMPLEADO."CodigoAgenteMigracion" = TBCPMSPC."COD_AGENTE"
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Operacion" as HOM_OPERACION
						ON HOM_OPERACION."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 	
			        WHERE TBCPMSPC."RUT_CLI" =  CASE WHEN p_RutCliente = '        0-0' then  TBCPMSPC."RUT_CLI" ELSE p_RutCliente END
			          AND HOM_OPERACION."COD_PER" IS NULL;
		   END IF;

           -- contamos las operaciones cargadas
		   SELECT COUNT(*) INTO v_total_mov_cargadas FROM "BSCL.Homologacion.BD::HOM.Operacion"
		   WHERE "IdCarga" = p_IdCarga;
		   
		   ------------------------------
		   --cargamos el log de errores--
           ------------------------------
		   
		   	-- debe existir el cliente  
              insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESARF' as "Entidad"
                    ,'El Cliente sebra no existe en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCPMSPC."RUT_CLI"  as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_COMPROM' as "NombreCampoGenerico6"
                    , TBCPMSPC."SEC_COMPROM" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" as TBCPMSPC
              INNER JOIN :v_operaciones as OPERACIONES
			       	ON OPERACIONES."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
			       	AND OPERACIONES."COD_PER" = TBCPMSPC."COD_PER" 
			  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			       	ON CLIENTE."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CLI")
			  WHERE CLIENTE."DocumentoIdentidad" IS NULL;
			  
		   	-- debe existir la entidad Portafolio cliente la secuencia 
            -- de cliente debe calzar con el codigo portafolio de optimus

             insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESARF' as "Entidad"
                    ,'la secuencia de cuenta cliente sebra no fue hallada en portafolio' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBCPMSPC."RUT_CLI"  as "ValorCampoGenerico1" 
                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_COMPROM' as "NombreCampoGenerico6"
                    , TBCPMSPC."SEC_COMPROM" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" as TBCPMSPC
              INNER JOIN :v_operaciones as OPERACIONES
			       	ON OPERACIONES."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
			       	AND OPERACIONES."COD_PER" = TBCPMSPC."COD_PER" 
			  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			       	ON CLIENTE."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CLI")
		      LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI")
		   	  WHERE PORTAFOLIO."IdCliente" IS NULL;
		   
		   	 -- el rut de la corredora de la operacion
             -- debe existir 

             insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESARF' as "Entidad"
                    ,'el rut corredora no existe como cliente optimus' as "Mensaje"
                    , 'RUT_CORR' as "NombreCampoGenerico1"
                    , TBCPMSPC."RUT_CORR"  as "ValorCampoGenerico1" 
                    , '' as "NombreCampoGenerico2" 
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_COMPROM' as "NombreCampoGenerico6"
                    , TBCPMSPC."SEC_COMPROM" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" as TBCPMSPC
              INNER JOIN :v_operaciones as OPERACIONES
			       	ON OPERACIONES."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
			       	AND OPERACIONES."COD_PER" = TBCPMSPC."COD_PER" 
			  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			       	ON CLIENTE."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CLI")
		      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			        ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			        AND PORTAFOLIO."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI")
			  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE_CORREDOR
			       	ON CLIENTE_CORREDOR."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CORR")
		   	  WHERE CLIENTE_CORREDOR."IdCliente" IS NULL;

             -- el la cuenta de la corredora de la operacion
             -- debe existir 

             insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MESARF' as "Entidad"
                    ,'el rut corredora no existe como cliente optimus' as "Mensaje"
                    , 'RUT_CORR' as "NombreCampoGenerico1"
                    , TBCPMSPC."RUT_CORR"  as "ValorCampoGenerico1" 
                    , 'SEC_RUT_CORR' as "NombreCampoGenerico2" 
                    , TO_VARCHAR(TBCPMSPC."SEC_RUT_CORR") as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_COMPROM' as "NombreCampoGenerico6"
                    , TBCPMSPC."SEC_COMPROM" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10" 
              FROM "BSCL_VIRTUAL"."GES_TBCPMSPC" as TBCPMSPC
              INNER JOIN :v_operaciones as OPERACIONES
			       	ON OPERACIONES."SEC_COMPROM" = TBCPMSPC."SEC_COMPROM" 
			       	AND OPERACIONES."COD_PER" = TBCPMSPC."COD_PER" 
			  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			       	ON CLIENTE."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CLI")
		      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			        ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			        AND PORTAFOLIO."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CLI")
			  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE_CORREDOR
			       	ON CLIENTE_CORREDOR."DocumentoIdentidad" = trim(TBCPMSPC."RUT_CORR")
			  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO_CORREDOR
			        ON PORTAFOLIO_CORREDOR."IdCliente" = CLIENTE_CORREDOR."IdCliente"
			        AND PORTAFOLIO_CORREDOR."CodigoPortafolio" =  TO_VARCHAR(TBCPMSPC."SEC_RUT_CORR")
		   	  WHERE PORTAFOLIO_CORREDOR."IdPortafolio" IS NULL;		   
 	 
		   -- SACAMOS LOS REGISTROS PROCESADOS de los movimientos a facturar
		   p_ResumenDatosProcesados = select 'CARGAMOVIMIENTOFACTURACION' as "CodigoProceso" 
	   									,'FAC' as "Modulo" 
	   		 							,v_count_ope as "TotalRegistrosSebra"
	   		  							,v_total_mov_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   						    	  union all
	   						    	  select * from :p_ResumenDatosProcesados;
	   			  			    	
		------------------
		-- FIN SP 
		-------------------
	END;	
END;

 
