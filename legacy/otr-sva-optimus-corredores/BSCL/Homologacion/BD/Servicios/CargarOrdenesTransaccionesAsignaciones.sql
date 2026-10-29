SET SCHEMA "BSCL";
DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarOrdenesTransaccionesAsignaciones";
CREATE PROCEDURE "BSCL.Homologacion.BD.Servicios::CargarOrdenesTransaccionesAsignaciones"(
	 IN  p_IdEmpresa		   BIGINT,			-- ID Empresa destino
     IN  p_IdUsuario		   BIGINT,			-- ID del usuario que solicita el servicio
	 IN  p_IdCarga 			   BIGINT,          -- ID de la carga 
	 IN  p_Modulo              NVARCHAR(3),     -- modulo 
     IN  P_Modalidad           NVARCHAR(1),     -- Ingreso I incremental, 
     IN  p_Personas			   table ("RUT_CLI"   NVARCHAR(11), "SEC_RUT_CLI" NVARCHAR(1)),    -- rut del clientes sebra
	 IN  p_FechaInicial        NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_FechaFinal		   NVARCHAR(10),   --YYYY-MM-DD
	 IN  p_fechaCambio         NVARCHAR(20),   --'YYYY-MM-DD HH:MM:SS' 	
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
	   DECLARE v_total_ord_gestion BIGINT:=0;
	   DECLARE v_total_ord_cargadas BIGINT:=0;
	   DECLARE v_total_asig_gestion BIGINT:=0;
	   DECLARE v_total_asig_cargadas BIGINT:=0;
	   DECLARE v_total_transac_gestion BIGINT:=0;
	   DECLARE v_total_transac_cargadas BIGINT:=0;	   
	   DECLARE v_total_cobros_ordenes_gestion BIGINT:= 0;
	   DECLARE v_total_cobros_ordenes_gestion_aux BIGINT:= 0;
	   DECLARE v_total_cobros_ordenes_cargadas BIGINT:= 0;
	   DECLARE v_total_cobros_asignacion_gestion BIGINT:= 0;
	   DECLARE v_total_cobros_asignacion_gestion_aux BIGINT:= 0;
	   DECLARE v_total_cobros_asignacion_cargadas BIGINT:= 0;	   
       DECLARE v_Quantity BIGINT:=0;
       DECLARE v_tabla_cobros_asignacion table (
													 "IdCobroAsignacion"					BIGINT,
													 "IdAsignacion" 						BIGINT,
													 "IdCobro" 								NVARCHAR(6),
													 "FechaIngresoCobro"					TIMESTAMP,
													 "FechaIngresoRegistro" 				TIMESTAMP,
													 "IdPerfilCobro" 						NVARCHAR(40),
													 "FormaCobro" 							NVARCHAR(40),
													 "ValorCobro" 							DECIMAL(26,8),
													 "ValorCobroAplicado" 					DECIMAL(26,8),
													 "MontoCobroCalculado"					DECIMAL(26,8), 
													 "IdCarga"                              BIGINT
											);
    	DECLARE v_tabla_cobros_orden table (
													 "IdCobroOrden" 						BIGINT,
													 "IdGrupoCobro" 						BIGINT,
													 "IdOrden" 								BIGINT,
													 "IdCobro" 								NVARCHAR(40),
													 "ValorCobro" 							DECIMAL(26,8),
													 "ValorCobroAplicado" 					DECIMAL(26,8),
													 "MontoCobroCalculado" 					DECIMAL(26,8),
													 "FormaCobro" 							NVARCHAR(1),
													 "EsComprobante" 						NVARCHAR(1),
													 "IdPerfilCobro" 						NVARCHAR(16),
													 "FechaIngresoCobro" 					SECONDDATE,
													 "IdCarga"                              BIGINT,
													 "MontoMinimo"							DECIMAL(26,8)
													);
		--------------------
		--Negocio Aqui
		--------------------

		DELETE FROM "BSCL.Orden.BD::ORD.CobroOrden"
		WHERE "IdCarga" = 	p_IdCarga;
		 
		DELETE FROM "BSCL.Orden.BD::ORD.CobroAsignacion"
		WHERE "IdCarga" = 	p_IdCarga;

		select COUNT(*) INTO v_Quantity FROM :p_Personas;
		IF v_Quantity > 0 THEN 
		
		    SELECT count(1) into v_total_ord_gestion 	
			FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		    INNER JOIN :p_Personas AS personas 
		       	ON personas."RUT_CLI" = HOM."RUT_CLI"
		    	AND personas."SEC_RUT_CLI" = HOM."SEC_RUT_CLI"
		    LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		    	ON  ORDENES."IdOrden" = HOM."IdOrden"
		    WHERE ORDENES."IdOrden" IS NULL
		    	AND  HOM."IdCarga" = p_IdCarga; 


			INSERT INTO "BSCL.Orden.BD::ORD.Orden" 
						( "IdOrden","Folio","FechaRecepcion","FechaIngresoRegistro","FechaIngresoRegistroCorta","FechaSolicitudAnulacion","FechaAnulacion"
						,"FechaOperacion","FechaVigencia","FechaVencimiento","FechaEmision","FechaLiquidacion","FechaAceptacion","FechaAutorizacion"
						,"Glosa","IdEstadoOrden","Cantidad","Tasa","Plazo","ValorRescate","IdTipoReajuste","PrecioOrden","PrecioMercado"
						,"IndicadorFacturacionInmediata","ValorCaptacion","IdCondicionDeLiquidacion","IdTipoOperacion","IdCliente","IdPortafolioCliente"
						,"IdTipoCanalIngreso","IdLibroOrdenes","IdEmisor","IdEmpresa","IdBolsa","IdBolsaDistribucion","EspecificaBolsa"
						,"IdTipoCanalDistribucion","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdProducto","Nemotecnico"
						,"IdGrupoCobro","IdCarga","EsVentaCorta","SecOrdenMigracion","Reservado"
						,"IdUsuarioReserva","MotivoReserva","Bloqueado","IdUsuarioBloqueo","MotivoBloqueo","IdInstrumentoSubyacente","IdEmpleadoPortafolio"
						,"IdEmpleadoDigitador","IdTipoPrecio","IdTipoTasa","IdPorcentajeVisibilidad","IdTipoVigenciaOrden","IdMonedaOrden","IdMonedaLiquidacionOrden"
						,"MontoLocal","MontoOrden","MontoLiquidacion","TipoCambio","IdTipoCustodia","IdFormaCustodia")
			SELECT 
						 HOM."IdOrden",HOM."Folio",HOM."FechaRecepcion",HOM."FechaIngresoRegistro",HOM."FechaIngresoRegistroCorta",HOM."FechaSolicitudAnulacion",HOM."FechaAnulacion"
						,HOM."FechaOperacion",HOM."FechaVigencia",HOM."FechaVencimiento",HOM."FechaEmision",HOM."FechaLiquidacion",HOM."FechaAceptacion",HOM."FechaAutorizacion"
						,HOM."Glosa",HOM."IdEstadoOrden",HOM."Cantidad",HOM."Tasa",HOM."Plazo",HOM."ValorRescate",HOM."IdTipoReajuste",HOM."PrecioOrden",HOM."PrecioMercado"
						,HOM."IndicadorFacturacionInmediata",HOM."ValorCaptacion",HOM."IdCondicionDeLiquidacion",HOM."IdTipoOperacion",HOM."IdCliente",HOM."IdPortafolioCliente"
						,HOM."IdTipoCanalIngreso",HOM."IdLibroOrdenes",HOM."IdEmisor",HOM."IdEmpresa",HOM."IdBolsa",HOM."IdBolsaDistribucion",HOM."EspecificaBolsa"
						,HOM."IdTipoCanalDistribucion",HOM."IdInstrumento",HOM."IdTipoInstrumento",HOM."IdClasificacionTipoInstrumento",HOM."IdProducto",HOM."Nemotecnico"
						,HOM."IdGrupoCobro",HOM."IdCarga",HOM."EsVentaCorta",HOM."SecOrdenMigracion",HOM."Reservado"
						,HOM."IdUsuarioReserva",HOM."MotivoReserva",HOM."Bloqueado",HOM."IdUsuarioBloqueo",HOM."MotivoBloqueo",HOM."IdInstrumentoSubyacente",HOM."IdEmpleadoPortafolio"
						,HOM."IdEmpleadoDigitador",HOM."IdTipoPrecio",HOM."IdTipoTasa",HOM."IdPorcentajeVisibilidad",HOM."IdTipoVigenciaOrden",HOM."IdMonedaOrden",HOM."IdMonedaLiquidacionOrden"
						,HOM."MontoLocal",HOM."MontoOrden",HOM."MontoLiquidacion",HOM."TipoCambio",HOM."IdTipoCustodia",HOM."IdFormaCustodia"
		       FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		       INNER JOIN :p_Personas AS personas 
		         ON personas."RUT_CLI" = HOM."RUT_CLI"
		        AND personas."SEC_RUT_CLI" = HOM."SEC_RUT_CLI"
		       LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		         ON  ORDENES."IdOrden" = HOM."IdOrden"
		       WHERE ORDENES."IdOrden" IS NULL
		        AND  HOM."IdCarga" = p_IdCarga;

            -- cuento las asignacion
			SELECT COUNT(*) INTO v_total_asig_gestion 	       
			FROM "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
			INNER JOIN (
			             SELECT HOM."IdOrden" as "IdOrden"
				         FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		       				INNER JOIN :p_Personas AS personas 
		         			ON personas."RUT_CLI" = HOM."RUT_CLI"
		        			AND personas."SEC_RUT_CLI" = HOM."SEC_RUT_CLI"
		       			 LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		         			ON  ORDENES."IdOrden" = HOM."IdOrden"
		       			 WHERE ORDENES."IdOrden" IS NULL
		        			AND  HOM."IdCarga" = p_IdCarga
			  			) AS ordenes  
				ON HOM_ASIG."IdOrden" = ordenes."IdOrden"
			LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ORD_ASIG 
				ON ORD_ASIG."SecAsignacionMigracion" = HOM_ASIG."SecAsignacionMigracion"
			WHERE HOM_ASIG."IdCarga" = p_IdCarga
				AND ORD_ASIG."SecAsignacionMigracion" IS NULL;

		    -- segundo paso es cargar las asignaciones asociadas a estas ordenes 
            -- cuando la tabla de personas no viene vacia
 			INSERT INTO "BSCL.Orden.BD::ORD.Asignacion"
 				   (
 				    "IdAsignacion","FechaAsignacion","FechaIngresoRegistro","CantidadAsignada","MontoAsignado"
					,"ValorRescateAsignado","IdEstadoAsignacion","IdEstadoFacturacion"
					,"IdTransaccion","IdCarga","IdOrden","IdLibroOrdenes","EsAPlazo","IdAsignacionRef","EsOD"
					,"SecAsignacionMigracion"
				   )
			 SELECT 
				     HOM_ASIG."IdAsignacion",HOM_ASIG."FechaAsignacion",HOM_ASIG."FechaIngresoRegistro",HOM_ASIG."CantidadAsignada",HOM_ASIG."MontoAsignado"
					,HOM_ASIG."ValorRescateAsignado",HOM_ASIG."IdEstadoAsignacion",HOM_ASIG."IdEstadoFacturacion"
					,HOM_ASIG."IdTransaccion",HOM_ASIG."IdCarga",HOM_ASIG."IdOrden",HOM_ASIG."IdLibroOrdenes",HOM_ASIG."EsAPlazo",HOM_ASIG."IdCompFacturacionRef",HOM_ASIG."EsOD"
					,HOM_ASIG."SecAsignacionMigracion"
			  FROM "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
			  INNER JOIN "BSCL.Homologacion.BD::HOM.Orden" as ORDEN
		       ON HOM_ASIG."IdOrden" = ORDEN."IdOrden"
		      INNER JOIN :p_Personas AS personas 
		       ON personas."RUT_CLI" = ORDEN."RUT_CLI"
		       AND personas."SEC_RUT_CLI" = ORDEN."SEC_RUT_CLI"
			  LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" AS ORD_ASIG 
			   ON ORD_ASIG."IdAsignacion" = HOM_ASIG."IdAsignacion"
			  WHERE HOM_ASIG."IdCarga" = p_IdCarga
			   AND ORD_ASIG."IdAsignacion" IS NULL;
			      			
		 ELSE
		 
            SELECT COUNT(1) INTO v_total_ord_gestion 	       
		    FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		    LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		        ON  ORDENES."IdOrden" = HOM."IdOrden"
		    WHERE ORDENES."IdOrden" IS NULL
		      	AND  HOM."IdCarga" = p_IdCarga; 

		 	INSERT INTO "BSCL.Orden.BD::ORD.Orden" 
						( "IdOrden","Folio","FechaRecepcion","FechaIngresoRegistro","FechaIngresoRegistroCorta","FechaSolicitudAnulacion","FechaAnulacion"
						,"FechaOperacion","FechaVigencia","FechaVencimiento","FechaEmision","FechaLiquidacion","FechaAceptacion","FechaAutorizacion"
						,"Glosa","IdEstadoOrden","Cantidad","Tasa","Plazo","ValorRescate","IdTipoReajuste","PrecioOrden","PrecioMercado"
						,"IndicadorFacturacionInmediata","ValorCaptacion","IdCondicionDeLiquidacion","IdTipoOperacion","IdCliente","IdPortafolioCliente"
						,"IdTipoCanalIngreso","IdLibroOrdenes","IdEmisor","IdEmpresa","IdBolsa","IdBolsaDistribucion","EspecificaBolsa"
						,"IdTipoCanalDistribucion","IdInstrumento","IdTipoInstrumento","IdClasificacionTipoInstrumento","IdProducto","Nemotecnico"
						,"IdGrupoCobro","IdCarga","EsVentaCorta","SecOrdenMigracion","Reservado"
						,"IdUsuarioReserva","MotivoReserva","Bloqueado","IdUsuarioBloqueo","MotivoBloqueo","IdInstrumentoSubyacente","IdEmpleadoPortafolio"
						,"IdEmpleadoDigitador","IdTipoPrecio","IdTipoTasa","IdPorcentajeVisibilidad","IdTipoVigenciaOrden","IdMonedaOrden","IdMonedaLiquidacionOrden"
						,"MontoLocal","MontoOrden","MontoLiquidacion","TipoCambio","IdTipoCustodia","IdFormaCustodia")
						SELECT 
						 HOM."IdOrden",HOM."Folio",HOM."FechaRecepcion",HOM."FechaIngresoRegistro",HOM."FechaIngresoRegistroCorta",HOM."FechaSolicitudAnulacion",HOM."FechaAnulacion"
						,HOM."FechaOperacion",HOM."FechaVigencia",HOM."FechaVencimiento",HOM."FechaEmision",HOM."FechaLiquidacion",HOM."FechaAceptacion",HOM."FechaAutorizacion"
						,HOM."Glosa",HOM."IdEstadoOrden",HOM."Cantidad",HOM."Tasa",HOM."Plazo",HOM."ValorRescate",HOM."IdTipoReajuste",HOM."PrecioOrden",HOM."PrecioMercado"
						,HOM."IndicadorFacturacionInmediata",HOM."ValorCaptacion",HOM."IdCondicionDeLiquidacion",HOM."IdTipoOperacion",HOM."IdCliente",HOM."IdPortafolioCliente"
						,HOM."IdTipoCanalIngreso",HOM."IdLibroOrdenes",HOM."IdEmisor",HOM."IdEmpresa",HOM."IdBolsa",HOM."IdBolsaDistribucion",HOM."EspecificaBolsa"
						,HOM."IdTipoCanalDistribucion",HOM."IdInstrumento",HOM."IdTipoInstrumento",HOM."IdClasificacionTipoInstrumento",HOM."IdProducto",HOM."Nemotecnico"
						,HOM."IdGrupoCobro",HOM."IdCarga",HOM."EsVentaCorta",HOM."SecOrdenMigracion",HOM."Reservado"
						,HOM."IdUsuarioReserva",HOM."MotivoReserva",HOM."Bloqueado",HOM."IdUsuarioBloqueo",HOM."MotivoBloqueo",HOM."IdInstrumentoSubyacente",HOM."IdEmpleadoPortafolio"
						,HOM."IdEmpleadoDigitador",HOM."IdTipoPrecio",HOM."IdTipoTasa",HOM."IdPorcentajeVisibilidad",HOM."IdTipoVigenciaOrden",HOM."IdMonedaOrden",HOM."IdMonedaLiquidacionOrden"
						,HOM."MontoLocal",HOM."MontoOrden",HOM."MontoLiquidacion",HOM."TipoCambio",HOM."IdTipoCustodia",HOM."IdFormaCustodia"
		       FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		       LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		         ON  ORDENES."IdOrden" = HOM."IdOrden"
		       WHERE ORDENES."IdOrden" IS NULL
		       	AND  HOM."IdCarga" = p_IdCarga; 
		       
			-- cuento las asignacion
			SELECT COUNT(1) INTO v_total_asig_gestion 	       
			FROM "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
			INNER JOIN (
			             SELECT HOM."IdOrden"
			           	 FROM "BSCL.Homologacion.BD::HOM.Orden" as HOM
		       				LEFT JOIN  "BSCL.Orden.BD::ORD.Orden" as ORDENES
		        		 	ON  ORDENES."IdOrden" = HOM."IdOrden"
		      		   	 WHERE ORDENES."IdOrden" IS NULL
		       		   		AND  HOM."IdCarga" = p_IdCarga
			  			) AS ordenes 
				ON HOM_ASIG."IdOrden" = ordenes."IdOrden"
			LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ORD_ASIG 
				ON ORD_ASIG."SecAsignacionMigracion" = HOM_ASIG."SecAsignacionMigracion"
			WHERE HOM_ASIG."IdCarga" = p_IdCarga
				AND ORD_ASIG."SecAsignacionMigracion" IS NULL;
				 	   
				
		    -- segundo paso es cargar las asignaciones asociadas a estas ordenes 
            -- cuando la tabla de personas viene vacia
 			INSERT INTO "BSCL.Orden.BD::ORD.Asignacion"
 				   (
 				    "IdAsignacion","FechaAsignacion","FechaIngresoRegistro","CantidadAsignada","MontoAsignado"
					,"ValorRescateAsignado","IdEstadoAsignacion","IdEstadoFacturacion"
					,"IdTransaccion","IdCarga","IdOrden","IdLibroOrdenes","EsAPlazo","IdAsignacionRef","EsOD"
					,"SecAsignacionMigracion"
				   )
			 SELECT 
				     HOM_ASIG."IdAsignacion",HOM_ASIG."FechaAsignacion",HOM_ASIG."FechaIngresoRegistro",HOM_ASIG."CantidadAsignada",HOM_ASIG."MontoAsignado"
					,HOM_ASIG."ValorRescateAsignado",HOM_ASIG."IdEstadoAsignacion",HOM_ASIG."IdEstadoFacturacion"
					,HOM_ASIG."IdTransaccion",HOM_ASIG."IdCarga",HOM_ASIG."IdOrden",HOM_ASIG."IdLibroOrdenes",HOM_ASIG."EsAPlazo",HOM_ASIG."IdCompFacturacionRef",HOM_ASIG."EsOD"
					,HOM_ASIG."SecAsignacionMigracion"
			 FROM "BSCL.Homologacion.BD::HOM.Asignacion" AS HOM_ASIG
			 INNER JOIN "BSCL.Homologacion.BD::HOM.Orden" AS ordenes
			  	ON HOM_ASIG."IdOrden" = ordenes."IdOrden"
			 LEFT JOIN "BSCL.Orden.BD::ORD.Asignacion" ORD_ASIG 
			  	ON ORD_ASIG."IdAsignacion" = HOM_ASIG."IdAsignacion"
			 WHERE HOM_ASIG."IdCarga" = 88
			  	AND ORD_ASIG."IdAsignacion" IS NULL;
			  		
		END IF;
   
    	SELECT COUNT(*) into v_total_transac_gestion 
		FROM "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" AS HOM
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS ORD_TRAN
		    ON ORD_TRAN."IdTransaccion" = HOM."IdTransaccion"
		WHERE ORD_TRAN."IdTransaccion" IS NULL
		  AND HOM."IdCarga" = p_IdCarga;
			  	 
        -- recupero transacciones a partir de las asignaciones 
        -- puede darse el caso que se repitan debo evitar eso
             
        INSERT INTO "BSCL.Orden.BD::ORD.Transaccion"
			(  "IdTransaccion" ,"Folio" ,"FechaSistemaCreacion" ,"HoraSistemaCreacion" ,"FechaOperacion" ,"FechaLiquidacion" ,"FechaEmision" ,"FechaVencimiento" 
				,"FechaIngresoRegistro" ,"IdSistemaBursatil" ,"EsOD" ,"EsCuentaPropia" ,"EsFungible" ,"EsAPlazo" ,"IdEmisor" ,"Cantidad" ,"Monto" ,"Precio" 
				,"Tasa" ,"Nemotecnico" ,"NemotecnicoSVS" ,"ValorRescate" ,"ValorCaptacion" ,"IdEstado" ,"IdTransaccionPlazo" ,"IdTransaccionOd" ,"IdTipoInstrumento" 
				,"IdClasificacionTipoInstrumento" ,"IdTipoReajuste" ,"IdEmpleado" ,"IdTipoOperacion" ,"IdCondicionDeLiquidacion" ,"IdMoneda" ,"IdEmpresa" 
				,"IdBolsa" ,"IdInstrumento" ,"NemotecnicoSubyacente" ,"IdInstrumentoSubyacente" ,"FolioCartera" ,"FechaParidad" ,"ValorParidad" ,"FechaTipoCambio" 
				,"ValorTipoCambio" ,"IdMonedaLiquidacion" ,"IdMonedaSubyacente" ,"EsVentaCorta" ,"Plazo" ,"FechaIngresoOferta" ,"IdCarga" ,"CodigoOperadorContraparte" 
				,"FechaModificacionOperacion" ,"MontoValorizado" ,"IdLibroOrdenes" ,"Reservado" ,"IdUsuarioReserva" ,"MotivoReserva" ,"Bloqueado" ,"IdUsuarioBloqueo" 
				,"MotivoBloqueo" ,"IdComprobanteTesoreria" ,"MarcaNegociacion" ,"IndicadorDCV" ,"IdCorredorLocal" ,"IdProgramaProcesaAutomatico" ,"EsBilateral" 
				,"IdCamara" ,"TipoCambioCorresponsal" ,"IdCorredorCorresponsal" ,"MontoLiquidacionCorresponsal" ,"IdMonedaLiquidacionCorresponsal" ,"IdCorredorContraparte" 
				,"CodigoOperadorCorredor" 
			)  

		SELECT   HOM."IdTransaccion" ,HOM."Folio" ,HOM."FechaSistemaCreacion" ,HOM."HoraSistemaCreacion" ,HOM."FechaOperacion" ,HOM."FechaLiquidacion" ,HOM."FechaEmision" ,HOM."FechaVencimiento" 
				,HOM."FechaIngresoRegistro" ,HOM."IdSistemaBursatil" ,HOM."EsOD" ,HOM."EsCuentaPropia" ,HOM."EsFungible" ,HOM."EsAPlazo" ,HOM."IdEmisor" ,HOM."Cantidad" ,HOM."Monto" ,HOM."Precio" 
				,HOM."Tasa" ,HOM."Nemotecnico" ,Coalesce(HOM."NemotecnicoSVS",'') ,HOM."ValorRescate" ,HOM."ValorCaptacion" ,HOM."IdEstado" ,HOM."IdTransaccionPlazo" ,HOM."IdTransaccionOd" ,HOM."IdTipoInstrumento" 
				,HOM."IdClasificacionTipoInstrumento" ,Coalesce(HOM."IdTipoReajuste",''),HOM."IdEmpleado" ,HOM."IdTipoOperacion" ,HOM."IdCondicionDeLiquidacion" ,HOM."IdMoneda" ,HOM."IdEmpresa" 
				,HOM."IdBolsa" ,HOM."IdInstrumento" ,HOM."NemotecnicoSubyacente" ,HOM."IdInstrumentoSubyacente" ,HOM."FolioCartera" ,HOM."FechaParidad" ,HOM."ValorParidad" ,HOM."FechaTipoCambio" 
				,HOM."ValorTipoCambio" ,HOM."IdMonedaLiquidacion" ,HOM."IdMonedaSubyacente" ,HOM."EsVentaCorta" ,HOM."Plazo" ,HOM."FechaIngresoOferta" ,HOM."IdCarga" ,HOM."CodigoOperadorContraparte" 
				,HOM."FechaModificacionOperacion" ,HOM."MontoValorizado" ,HOM."IdLibroOrdenes" ,HOM."Reservado" ,HOM."IdUsuarioReserva" ,HOM."MotivoReserva" ,HOM."Bloqueado" ,HOM."IdUsuarioBloqueo" 
				,HOM."MotivoBloqueo" ,HOM."IdComprobanteTesoreria" ,HOM."MarcaNegociacion" ,HOM."IndicadorDCV" ,HOM."IdCorredorLocal" ,HOM."IdProgramaProcesaAutomatico" ,HOM."EsBilateral" 
				,HOM."IdCamara" ,HOM."TipoCambioCorresponsal" ,HOM."IdCorredorCorresponsal" ,HOM."MontoLiquidacionCorresponsal" ,HOM."IdMonedaLiquidacionCorresponsal" ,HOM."IdCorredorContraparte" 
				,HOM."CodigoOperadorCorredor" 
		FROM "BSCL"."BSCL.Homologacion.BD::HOM.Transaccion" AS HOM
		--INNER JOIN  "BSCL.Homologacion.BD::HOM.Asignacion" AS ASIG
		-- 	ON ASIG."IdTransaccion" = HOM."IdTransaccion"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" AS ORD_TRAN
		    ON ORD_TRAN."IdTransaccion" = HOM."IdTransaccion"
		WHERE ORD_TRAN."IdTransaccion" IS NULL
		  AND HOM."IdCarga" = p_IdCarga;

    -- cargamos los cobros de la asignaciON
    -- primero los gastos y luego derechos y acontinuacion comision

	    INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"
                    ("IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 ) 
		 SELECT
				 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
				,SAP_ASIG."IdAsignacion" as "IdAsignacion"
				,COBRO."IdCobro" as "IdCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
				,'COBROS_MIGRACION' as "IdPerfilCobro" 
				, COBRO."FormaCobro" as "FormaCobro"
				, 0 as "ValorCobro"
				,ROUND((MOV_FAC_RV."GASTOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
				,MOV_FAC_RV."GASTOS" as  "MontoCobroCalculado"
				,p_IdCarga as "IdCarga"
				FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" AS ASIG_SAP 
					ON ASIG_SAP."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
				INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_gasto"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" as SAP_ASIG
					ON SAP_ASIG."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				LEFT JOIN "BSCL.Orden.BD::ORD.CobroAsignacion" AS COBRO_ASIG
			    	ON COBRO_ASIG."IdAsignacion" = SAP_ASIG."IdAsignacion"
			   		AND COBRO_ASIG."IdCobro" = COBRO."IdCobro"
			    WHERE COBRO_ASIG."IdCobro" IS NULL
			        AND ASIG_SAP."IdCarga" = p_IdCarga;
        
        INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"
                    ("IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 ) 
        SELECT 
				 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
				,SAP_ASIG."IdAsignacion" as "IdAsignacion"
				,COBRO."IdCobro" as "IdCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
				,'COBROS_MIGRACION' as "IdPerfilCobro" 
				, COBRO."FormaCobro" as "FormaCobro"
				, 0 as "ValorCobro"
				,ROUND((MOV_FAC_RV."DERECHOS" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
				,MOV_FAC_RV."DERECHOS" as  "MontoCobroCalculado"
				,p_IdCarga as "IdCarga"
				FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" AS ASIG_SAP 
					ON ASIG_SAP."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
				INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_derecho"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" as SAP_ASIG
					ON SAP_ASIG."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				LEFT JOIN "BSCL.Orden.BD::ORD.CobroAsignacion" AS COBRO_ASIG
			   	 	ON COBRO_ASIG."IdAsignacion" = SAP_ASIG."IdAsignacion"
			   		AND COBRO_ASIG."IdCobro" = COBRO."IdCobro"
			    WHERE COBRO_ASIG."IdCobro" IS NULL
			    	AND ASIG_SAP."IdCarga" = p_IdCarga;
			    	
        -- cargamos asignacion cobros
        INSERT INTO  "BSCL.Orden.BD::ORD.CobroAsignacion"
                    ("IdCobroAsignacion","IdAsignacion" ,"IdCobro" ,"FechaIngresoCobro" ,"FechaIngresoRegistro" 
					 ,"IdPerfilCobro" ,"FormaCobro" ,"ValorCobro" ,"ValorCobroAplicado" ,"MontoCobroCalculado" 
					 ,"IdCarga"
					 ) 
        SELECT
				 "BSCL.Orden.BD.Secuencias::CobroAsignacion".NEXTVAL as "IdCobroAsignacion"
				,SAP_ASIG."IdAsignacion" as "IdAsignacion"
				,COBRO."IdCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoCobro"
				,MOV_FAC_RV."FEC_ING_MVTO" as "FechaIngresoRegistro"
				,'COBROS_MIGRACION' as "IdPerfilCobro" 
				, COBRO."FormaCobro" as "FormaCobro"
				, 0 as "ValorCobro"
				,ROUND((MOV_FAC_RV."COMISION" * 100 )/(CASE WHEN MOV_FAC_RV."MONTO" = 0 THEN 1 ELSE MOV_FAC_RV."MONTO" END),2) AS "ValorCobroAplicado"  
				,MOV_FAC_RV."COMISION" as  "MontoCobroCalculado"
				,p_IdCarga as "IdCarga"
				FROM "BSCL_VIRTUAL"."GES_ASIGNACION_RV" as ASIG
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" AS ASIG_SAP 
					ON ASIG_SAP."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				INNER JOIN "BSCL_VIRTUAL"."GES_SOLICITUD" as SOLI
					ON SOLI."sol_sec" =  ASIG."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_OPERACION_RV" as OPE_RV
					ON OPE_RV."sol_sec" = SOLI."sol_sec"
				INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMFRV" as MOV_FAC_RV
					ON ASIG."SEC_MOVTO_FAC" = MOV_FAC_RV."SEC_MOVTO_FAC"
				INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" as COBRO
					ON COBRO."IdCobro" = SOLI."sol_tipo_comision"
				INNER JOIN "BSCL.Homologacion.BD::HOM.Asignacion" as SAP_ASIG
					ON SAP_ASIG."SEC_ASIGNACION" = ASIG."SEC_ASIGNACION"
				LEFT JOIN "BSCL.Orden.BD::ORD.CobroAsignacion" AS COBRO_ASIG
			   	 	ON COBRO_ASIG."IdAsignacion" = SAP_ASIG."IdAsignacion"
			    	AND COBRO_ASIG."IdCobro" = COBRO."IdCobro"
			    WHERE COBRO_ASIG."IdCobro" IS NULL
                  AND ASIG_SAP."IdCarga" = p_IdCarga;

		SELECT COUNT(*) INTO v_total_cobros_asignacion_gestion_aux 	       
		FROM "BSCL.Orden.BD::ORD.CobroAsignacion" WHERE "IdCarga" = p_IdCarga;	
		
		v_total_cobros_asignacion_gestion = v_total_cobros_asignacion_gestion_aux + v_total_cobros_asignacion_gestion;
		
        -- cargamos los gastos 
        INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden"(
                    "IdCobroOrden" ,"IdGrupoCobro" ,"IdOrden" ,"IdCobro" 								
 		     	   ,"ValorCobroPerfil" ,"ValorCobroAplicado"  ,"MontoCobroCalculado" ,"FormaCobro" 							
 				   ,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" ,"IdCarga","MontoMinimo")
        SELECT
				   "BSCL.Orden.BD.Secuencias::CobrosOrden".NEXTVAL as "IdCobroOrden"
				   ,mvto."IdGrupoCobro" as "IdGrupoCobro"
				   ,mvto."IdOrden" as "IdOrden" 
			       ,mvto."sol_tipo_gasto" as "IdCobro"
			       ,COBROS."ValorCobro" as  "ValorCobro"
			       ,mvto."sol_val_gastos" AS "ValorCobroAplicado"
			       ,mvto."orv_mto_gastos" AS "MontoCobroCalculado"
			       ,COBROS."FormaCobro" as "FormaCobro"
			       ,'S' as "EsComprobante"
			       ,'COBROS_MIGRACION' as "IdPerfilCobro"
			       ,mvto."FechaRecepcion" as "FechaIngresoCobro"
			       ,p_IdCarga as "IdCarga"
			       ,0 as "MontoMinimo"
	          FROM "BSCL.Homologacion.BD::HOM.Orden" AS mvto
	          INNER JOIN  "BSCL.Plataforma.BD::COM.Cobro"  AS COBROS
	          	ON COBROS."IdCobro" = mvto."sol_tipo_gasto"
	          LEFT JOIN "BSCL.Orden.BD::ORD.CobroOrden" AS COBROS_ORD
	            ON COBROS_ORD."IdOrden" = mvto."IdOrden" 	
	           AND COBROS_ORD."IdCobro"	= mvto."sol_tipo_gasto"
	          WHERE COBROS_ORD."IdOrden" IS NULL
	            AND mvto."IdCarga" = p_IdCarga;


        -- cargamos los derechos			
	        INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden"(
	                    "IdCobroOrden" ,"IdGrupoCobro" ,"IdOrden" ,"IdCobro" 								
	 		     	   ,"ValorCobroPerfil" ,"ValorCobroAplicado"  ,"MontoCobroCalculado" ,"FormaCobro" 							
	 				   ,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" ,"IdCarga","MontoMinimo")
	        SELECT
					   "BSCL.Orden.BD.Secuencias::CobrosOrden".NEXTVAL as "IdCobroOrden"
					   ,mvto."IdGrupoCobro" as "IdGrupoCobro"
					   ,mvto."IdOrden" as "IdOrden"
				       ,mvto."sol_tipo_derecho" as "IdCobro"
				       ,COBROS."ValorCobro" as "ValorCobro"
				       ,mvto."sol_val_derechos" AS "ValorCobroAplicado"
				       ,mvto."orv_mto_derechos" AS "MontoCobroCalculado"
				       ,COBROS."FormaCobro" as "FormaCobro"
				       ,'S' as "EsComprobante"
				       ,'COBROS_MIGRACION' as "IdPerfilCobro"
				       ,mvto."FechaRecepcion" as "FechaIngresoCobro"
				       ,p_IdCarga as "IdCarga"
				       ,0 as "MontoMinimo"
	       FROM "BSCL.Homologacion.BD::HOM.Orden" AS mvto
	       INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBROS
	       		ON COBROS."IdCobro" = mvto."sol_tipo_derecho"
	       LEFT JOIN "BSCL.Orden.BD::ORD.CobroOrden" AS COBROS_ORD
	       		ON COBROS_ORD."IdOrden" = mvto."IdOrden" 	
	       		AND COBROS_ORD."IdCobro"	= mvto."sol_tipo_derecho"
	      	WHERE COBROS_ORD."IdOrden" IS NULL
	        	AND mvto."IdCarga" = p_IdCarga;

		   -- cargamos los comision
	        INSERT INTO "BSCL.Orden.BD::ORD.CobroOrden"(
	                    "IdCobroOrden" ,"IdGrupoCobro" ,"IdOrden" ,"IdCobro" 								
	 		     	   ,"ValorCobroPerfil" ,"ValorCobroAplicado"  ,"MontoCobroCalculado" ,"FormaCobro" 							
	 				   ,"EsComprobante" ,"IdPerfilCobro" ,"FechaIngresoCobro" ,"IdCarga","MontoMinimo")
	        SELECT
				   "BSCL.Orden.BD.Secuencias::CobrosOrden".NEXTVAL as "IdCobroOrden"
				   ,mvto."IdGrupoCobro" as "IdGrupoCobro"
				   ,mvto."IdOrden" as "IdOrden"
			       ,mvto."sol_tipo_comision" as "IdCobro"
			       ,COBROS."ValorCobro" as "ValorCobro" 
			       ,mvto."sol_val_comision" AS "ValorCobroAplicado"
			       ,mvto."orv_mto_comision" AS "MontoCobroCalculado"
			       ,COBROS."FormaCobro" as "FormaCobro"
			       ,'S'  as "EsComprobante"
			       ,'COBROS_MIGRACION' as "IdPerfilCobro"
			       ,mvto."FechaRecepcion" as "FechaIngresoCobro"
			       ,p_IdCarga as "IdCarga" 
			       ,0 as "MontoMinimo"
	          FROM "BSCL.Homologacion.BD::HOM.Orden" AS mvto
	          INNER JOIN "BSCL.Plataforma.BD::COM.Cobro" AS COBROS
	          	ON COBROS."IdCobro" = mvto."sol_tipo_comision"
	          LEFT JOIN "BSCL.Orden.BD::ORD.CobroOrden" AS COBROS_ORD
	            ON COBROS_ORD."IdOrden"  = mvto."IdOrden" 	
	            AND COBROS_ORD."IdCobro" = mvto."sol_tipo_comision"
	          WHERE COBROS_ORD."IdOrden" IS NULL	
	              AND mvto."IdCarga" = p_IdCarga;

        SELECT COUNT(*) INTO v_total_cobros_ordenes_gestion_aux 	       
		FROM "BSCL.Orden.BD::ORD.CobroOrden"
		where "IdOrden" > 0;	
		
		v_total_cobros_ordenes_gestion = v_total_cobros_ordenes_gestion_aux + v_total_cobros_ordenes_gestion;
	
		-- cuanto se cargo  
	    SELECT COUNT(*) INTO v_total_cobros_ordenes_cargadas FROM "BSCL.Orden.BD::ORD.CobroOrden" WHERE "IdCarga" = p_IdCarga;
	    SELECT COUNT(*) INTO v_total_cobros_asignacion_cargadas FROM "BSCL.Orden.BD::ORD.CobroAsignacion" WHERE "IdCarga" = p_IdCarga;
	    SELECT COUNT(*) INTO v_total_transac_cargadas FROM "BSCL.Orden.BD::ORD.Transaccion" WHERE "IdCarga" = p_IdCarga;
	    SELECT COUNT(*) INTO v_total_asig_cargadas FROM "BSCL.Orden.BD::ORD.Asignacion" WHERE "IdCarga" = p_IdCarga;
	    SELECT COUNT(*) INTO v_total_ord_cargadas FROM "BSCL.Orden.BD::ORD.Orden" WHERE "IdCarga" = p_IdCarga;

            --REVISAMOS CUANTOS FUERON CARGADOS
	   		SELECT COUNT(*) INTO v_total_ord_cargadas FROM 
	   		"BSCL.Orden.BD::ORD.Orden" where "IdCarga" = p_IdCarga;
	   			  
	   		-- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ORDEN
	   		p_ResumenDatosProcesados =  select 'CARGAORD' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_ord_gestion as "TotalRegistrosSebra"
	   			  									,v_total_ord_cargadas as "TotalRegistrosSap"  FROM DUMMY
       							    		 union all
       							    		 select 'CARGAASIG' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_asig_gestion as "TotalRegistrosSebra"
	   			  									,v_total_asig_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   			  							 union all
       							    		 select 'CARGATRANS' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_transac_gestion  as "TotalRegistrosSebra"
	   			  									,v_total_transac_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   			  							 union all
       							    		 select 'CARGACOASIG' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_cobros_asignacion_gestion as "TotalRegistrosSebra"
	   			  									,v_total_cobros_asignacion_cargadas as "TotalRegistrosSap"  FROM DUMMY
	   			  							union all
	   			  							select 'CARGACOORD' as "CodigoProceso" 
	   			  									,'ORD' as "Modulo" 
	   			 									,v_total_cobros_ordenes_gestion as "TotalRegistrosSebra"
	   			  									,v_total_cobros_ordenes_cargadas as "TotalRegistrosSap"  FROM DUMMY 
		  							        union all
	   			  							select * from :p_ResumenDatosProcesados;
	   			  							
	   			  IF (v_total_ord_gestion <> v_total_ord_cargadas)
	   			     or (v_total_asig_gestion <> v_total_asig_cargadas) 
	   			     or (v_total_transac_gestion <> v_total_transac_cargadas)
	   			     or (v_total_cobros_asignacion_gestion <> v_total_cobros_asignacion_cargadas) 
	   			     or (v_total_cobros_ordenes_gestion <> v_total_cobros_ordenes_cargadas )THEN 
	   			  	 v_hash= select 'ORD_CARGA_ERR_ORD' as "Codigo", '' as "Valor" FROM DUMMY;
				     CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('ORD_CARGA_ERR_ORD',:v_hash,:v_erroroutmsg);
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_ORD' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		END;
END;

 
