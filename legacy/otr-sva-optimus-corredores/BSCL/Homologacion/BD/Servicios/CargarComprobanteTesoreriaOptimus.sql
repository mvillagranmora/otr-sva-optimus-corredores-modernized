SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarComprobanteTesoreriaOptimus";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de solicitudes FFMM 
--  TESORERIA
--  Autor: - EAR
 
"BSCL.Homologacion.BD.Servicios::CargarComprobanteTesoreriaOptimus"(
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
	   DECLARE v_Total_comprobantes_test BIGINT:=0;
	   DECLARE v_Total_comprobantes_Cargados BIGINT :=0;
	   DECLARE v_Quantity  BIGINT :=0;
	   

           SELECT count(*) into v_Total_comprobantes_test
           FROM "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" AS HOM
				LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante"  AS COM
				  ON COM."IdComprobante" = HOM."IdComprobante"
			    WHERE HOM."IdCarga" = p_IdCarga
			      AND COM."IdComprobante" IS NULL;
			      
            	INSERT INTO "BSCL.Tesoreria.BD::TES.Comprobante"
				 ( 
                    "IdComprobante" ,"IdEmpresa" ,"IdUsuario" ,"TipoComprobante" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" 
					,"FechaLiquidacion" ,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona" ,"IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" 
					,"DocumentoIdentidad" ,"TitularPago" ,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" 
					,"SecuenciaAgrupacion" ,"EsBilateral" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" ,"GeneradoDesdeMovimientos" ,"IdEvento" 
					,"MueveCuentaInversion" ,"ComprobanteNeteado" ,"ComprobanteAgrupado" ,"SeparadoPorConcepto"
				)	
				SELECT  
	              HOM."IdComprobante" ,HOM."IdEmpresa" ,HOM."IdUsuario" ,HOM."TipoComprobante" ,HOM."IdTipoMovimiento" ,HOM."IdConcepto" ,HOM."IdCaja" ,HOM."IdMoneda" ,HOM."Monto" 
				 ,HOM."FechaLiquidacion" ,HOM."FechaIngresoRegistro" ,HOM."FechaMovimiento" ,HOM."IdTipoPersona" ,HOM."IdPersona" ,HOM."NombrePersona" ,HOM."IdTipoDocumentoIdentidad" 
				 ,HOM."DocumentoIdentidad" ,HOM."TitularPago" ,HOM."IdTipoPersonaTitularPago" ,HOM."IdPersonaTitularPago" ,HOM."NombrePersonaTitularPago" ,HOM."IdPortafolioTitularPago" 
				 ,HOM."SecuenciaAgrupacion" ,HOM."EsBilateral" ,HOM."IdUsuarioBloqueo" ,HOM."FechaHoraBloqueo" ,HOM."IdCarga" ,HOM."IdEstado" ,HOM."GeneradoDesdeMovimientos" ,HOM."IdEvento" 
				 ,HOM."MueveCuentaInversion" ,HOM."ComprobanteNeteado" ,HOM."ComprobanteAgrupado" ,HOM."SeparadoPorConcepto"
	            FROM "BSCL.Homologacion.BD::HOM.ComprobanteTesoreria" AS HOM
	            LEFT JOIN "BSCL.Tesoreria.BD::TES.Comprobante" AS DET
	             ON DET."IdComprobante"	= HOM."IdComprobante"
	            WHERE DET."IdComprobante" IS NULL
	              AND HOM."IdCarga" = p_IdCarga;

			-- contamos los cargados
		    select count(*) into v_Total_comprobantes_Cargados
		    from "BSCL.Tesoreria.BD::TES.Comprobante"
		    where "IdCarga" = p_IdCarga;
			 
		
	         -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
		     p_ResumenDatosProcesados =  select 'CARGACOMPROBANTE_TES' as "CodigoProceso" 
		   			  							,'TES' as "Modulo" 
		   			 							,v_Total_comprobantes_test as "TotalRegistrosSebra"
		   			  							,v_Total_comprobantes_Cargados as "TotalRegistrosSap"  FROM DUMMY
	       							     union all
	       							     select * from :p_ResumenDatosProcesados;

          IF v_Total_comprobantes_test <> v_Total_comprobantes_Cargados THEN 
			 p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_COMP_TEST' as "Codigo",'falla en la carga comprobante tesoreria' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   	  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
