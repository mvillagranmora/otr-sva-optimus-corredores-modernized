SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarMovimientoComprobanteDetalle";
   
CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 
"BSCL.Homologacion.BD.Servicios::CargarMovimientoComprobanteDetalle"(
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

	-- declaracion variables
	   DECLARE v_total_detcom_gestion DECIMAL(26,8):=0;
	   DECLARE v_total_detcom_gestion_aux BIGINT:=0;
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
		--------------------
		--Negocio Aqui
		--------------------

		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;
		
		delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error" 
		where "Entidad" = 'MOVIMIENTOCOMPROBANTEDETALLE'
		and "IdCarga" = p_IdCarga;


		IF P_Modalidad = 'R' THEN --REPROCESO

	        DELETE FROM "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOM
	        WHERE HOM."IdComprobanteDetalle" IN ( SELECT HOM."IdComprobanteDetalle" from "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS HOM
							                      LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"  AS DETALLE
							           				 ON DETALLE."IdComprobanteDetalle" = HOM."IdComprobanteDetalle"
							           				  WHERE HOM."IdCarga" = p_IdCarga
							           				  AND DETALLE."IdComprobanteDetalle" IS NULL);
	   END IF;
		
       insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"(    
             "IdMovimientoComprobanteDetalle","IdComprobanteDetalle" ,"IdMovimientoComprobante" ,"IdTransaccion" ,"IdInstrumento" ,"NemotecnicoInstrumento" 
			,"IdConceptoMovimiento","Cantidad" ,"Precio" ,"PrecioPromedio" ,"Monto" ,"Tasa" ,"TasaEmision" ,"SpreadTasaEmision" ,"ValorRescate" ,"ValorParidad" 
			,"ValorCaptacionMonedaOrigen" ,"SecuenciaAgrupacion" ,"IdEstadoMovimientoComprobanteDetalle" ,"FechaIngresoRegistro" ,"MontoLiquidacion" ,"IdMonedaLiquidacion" 
			,"MontoMovimientoComprobanteDetalle" ,"IdMonedaMovimientoComprobanteDetalle" ,"IdMoneda" ,"IdInstrumentoSubyacente" ,"IdMonedaSubyacente" ,"IdTipoInstrumento" 
			,"IdTipoInstrumentoSubyacente" ,"NombreTipoInstrumento" ,"NombreTipoInstrumentoSubyacente" ,"NemotecnicoSubyacente" ,"IdClasificacionTipoInstrumento" 
			,"IdClasificacionTipoInstrumentoRelacionado" ,"IdInstrumentoRelacionado" ,"IdTipoInstrumentoRelacionado" ,"NemotecnicoRelacionado" ,"NombreClasificacionTipoInstrumentoRelacionado" 
			,"NombreTipoInstrumentoRelacionado" ,"NombreClasificacionTipoInstrumento","FOLIO_COMP_FAC","TIPO_COMP_FAC","LIN_COMP_FAC","RUT_CLI","SEC_RUT_CLI","IdCarga"
			,"SecMovimientoFacturarMigracion"
			) 
        select 
       			 "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdComprobanteDetalle" as "IdComprobanteDetalle" 
				, MOV_COMPROBANTE."IdMovimientoComprobante" as "IdMovimientoComprobante" 
				, 0 as "IdTransaccion" 
				, DETALLE_COMPROBANTE."IdInstrumento" as "IdInstrumento" 
				, DETALLE_COMPROBANTE."NemotecnicoInstrumento" as "NemotecnicoInstrumento" 
				, '' as "IdConceptoMovimiento" 
				, TBCTMFRV."CANTIDAD" as "Cantidad" 
				, TBCTMFRV."PRECIO" as "Precio" 
				, TBCTMFRV."PRECIO" as "PrecioPromedio" 
				, TBCTMFRV."MONTO" as "Monto" 
				, TBCTMFRV."TASA" as "Tasa" 
				, TBCTMFRV."TASA" as "TasaEmision" 
				, TBCTMFRV."SPREAD" as "SpreadTasaEmision" 
				, 0 as "ValorRescate" 
				, 0 as "ValorParidad" 
				, TBCTMFRV."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen" 
				, 0 as "SecuenciaAgrupacion" 
				, DETALLE_COMPROBANTE."IdEstadoComprobanteDetalle" as "IdEstadoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."FechaIngresoRegistro" as "FechaIngresoRegistro" 
				, TBCTMFRV."MONTO" as "MontoLiquidacion" 
				, DETALLE_COMPROBANTE."IdMonedaLiquidacion" as "IdMonedaLiquidacion" 
				, DETALLE_COMPROBANTE."MontoComprobanteDetalle" as "MontoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMonedaMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMoneda" 
				, DETALLE_COMPROBANTE."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."IdMonedaSubyacente" as  "IdMonedaSubyacente" 
				, DETALLE_COMPROBANTE."IdTipoInstrumento" as "IdTipoInstrumento" 
				, DETALLE_COMPROBANTE."IdTipoInstrumentoSubyacente" as "IdTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumento" as "NombreTipoInstrumento" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumentoSubyacente"  as "NombreTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NemotecnicoSubyacente"  as "NemotecnicoSubyacente" 
				, DETALLE_COMPROBANTE."IdClasificacionTipoInstrumento"  as "IdClasificacionTipoInstrumento" 
				, '' as "IdClasificacionTipoInstrumentoRelacionado" 
				, 0 as "IdInstrumentoRelacionado" 
				, '' as "IdTipoInstrumentoRelacionado" 
				, '' as "NemotecnicoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumentoRelacionado" 
				, '' as "NombreTipoInstrumentoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumento" 
				, DETALLE_COMPROBANTE."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
				, DETALLE_COMPROBANTE."TIPO_COMP_FAC"as "TIPO_COMP_FAC"
				, DETALLE_COMPROBANTE."LINEA_COMP_FAC"as "LIN_COMP_FAC"
				, DETALLE_COMPROBANTE."RUT_CLI" as "RUT_CLI"
				, DETALLE_COMPROBANTE."SEC_RUT_CLI" as "SEC_RUT_CLI"
				, p_IdCarga as "IdCarga"
				,TBCTMFRV."SEC_MOVTO_FAC" as  "SecMovimientoFacturarMigracion"
			FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" AS TBCTMFRV
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS MOV_COMPROBANTE 
				ON MOV_COMPROBANTE."SEC_MOVTO_FAC" = TBCTMFRV."SEC_MOVTO_FAC"
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
			  ON TBCTMFRV."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			  AND TBCTMFRV."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
			  AND TBCTMFRV."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
			  AND TBCTMFRV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRV."RUT_DEST" else p_RutCliente end
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
				ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
			LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
				ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
				AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
			WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
			  AND COMPROBANTE."FEC_ING_COMP"  >= p_FechaInicial
			  AND COMPROBANTE."FEC_ING_COMP" <= p_FechaFinal; 		   
        --- movimientos if 
        
        insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"(    
             "IdMovimientoComprobanteDetalle","IdComprobanteDetalle" ,"IdMovimientoComprobante" ,"IdTransaccion" ,"IdInstrumento" ,"NemotecnicoInstrumento" 
			,"IdConceptoMovimiento","Cantidad" ,"Precio" ,"PrecioPromedio" ,"Monto" ,"Tasa" ,"TasaEmision" ,"SpreadTasaEmision" ,"ValorRescate" ,"ValorParidad" 
			,"ValorCaptacionMonedaOrigen" ,"SecuenciaAgrupacion" ,"IdEstadoMovimientoComprobanteDetalle" ,"FechaIngresoRegistro" ,"MontoLiquidacion" ,"IdMonedaLiquidacion" 
			,"MontoMovimientoComprobanteDetalle" ,"IdMonedaMovimientoComprobanteDetalle" ,"IdMoneda" ,"IdInstrumentoSubyacente" ,"IdMonedaSubyacente" ,"IdTipoInstrumento" 
			,"IdTipoInstrumentoSubyacente" ,"NombreTipoInstrumento" ,"NombreTipoInstrumentoSubyacente" ,"NemotecnicoSubyacente" ,"IdClasificacionTipoInstrumento" 
			,"IdClasificacionTipoInstrumentoRelacionado" ,"IdInstrumentoRelacionado" ,"IdTipoInstrumentoRelacionado" ,"NemotecnicoRelacionado" ,"NombreClasificacionTipoInstrumentoRelacionado" 
			,"NombreTipoInstrumentoRelacionado" ,"NombreClasificacionTipoInstrumento","FOLIO_COMP_FAC","TIPO_COMP_FAC","LIN_COMP_FAC","RUT_CLI","SEC_RUT_CLI","IdCarga"
			,"SecMovimientoFacturarMigracion"
			) 
        select 
       			 "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdComprobanteDetalle" as "IdComprobanteDetalle" 
				, MOV_COMPROBANTE."IdMovimientoComprobante" as "IdMovimientoComprobante" 
				, 0 as "IdTransaccion" 
				, DETALLE_COMPROBANTE."IdInstrumento" as "IdInstrumento" 
				, DETALLE_COMPROBANTE."NemotecnicoInstrumento" as "NemotecnicoInstrumento" 
				, '' as "IdConceptoMovimiento" 
				, TBCTMFIF."CANTIDAD" as "Cantidad" 
				, 0 as "Precio" 
				, 0 as "PrecioPromedio" 
				, TBCTMFIF."MONTO" as "Monto" 
				, TBCTMFIF."TASA" as "Tasa" 
				, TBCTMFIF."TASA_EMISION" as "TasaEmision" 
				, TBCTMFIF."SPREAD_TASA_EMISION" as "SpreadTasaEmision" 
				, TBCTMFIF."VAL_RESC" as "ValorRescate" 
				, TBCTMFIF."PARIDAD_MORIGEN" as "ValorParidad" 
				, TBCTMFIF."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen" 
				, 0 as "SecuenciaAgrupacion" 
				, DETALLE_COMPROBANTE."IdEstadoComprobanteDetalle" as "IdEstadoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."FechaIngresoRegistro" as "FechaIngresoRegistro" 
				, TBCTMFIF."MONTO" as "MontoLiquidacion" 
				, DETALLE_COMPROBANTE."IdMonedaLiquidacion" as "IdMonedaLiquidacion" 
				, DETALLE_COMPROBANTE."MontoComprobanteDetalle" as "MontoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMonedaMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMoneda" 
				, DETALLE_COMPROBANTE."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."IdMonedaSubyacente" as  "IdMonedaSubyacente" 
				, DETALLE_COMPROBANTE."IdTipoInstrumento" as "IdTipoInstrumento" 
				, DETALLE_COMPROBANTE."IdTipoInstrumentoSubyacente" as "IdTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumento" as "NombreTipoInstrumento" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumentoSubyacente"  as "NombreTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NemotecnicoSubyacente"  as "NemotecnicoSubyacente" 
				, DETALLE_COMPROBANTE."IdClasificacionTipoInstrumento"  as "IdClasificacionTipoInstrumento" 
				, '' as "IdClasificacionTipoInstrumentoRelacionado" 
				, 0 as "IdInstrumentoRelacionado" 
				, '' as "IdTipoInstrumentoRelacionado" 
				, '' as "NemotecnicoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumentoRelacionado" 
				, '' as "NombreTipoInstrumentoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumento" 
				, DETALLE_COMPROBANTE."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
				, DETALLE_COMPROBANTE."TIPO_COMP_FAC"as "TIPO_COMP_FAC"
				, DETALLE_COMPROBANTE."LINEA_COMP_FAC"as "LIN_COMP_FAC"
				, DETALLE_COMPROBANTE."RUT_CLI" as "RUT_CLI"
				, DETALLE_COMPROBANTE."SEC_RUT_CLI" as "SEC_RUT_CLI"
				, p_IdCarga as "IdCarga"
				,TBCTMFIF."SEC_MOVTO_FAC" as  "SecMovimientoFacturarMigracion"
			FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" AS TBCTMFIF
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS MOV_COMPROBANTE 
				ON MOV_COMPROBANTE."SEC_MOVTO_FAC" = TBCTMFIF."SEC_MOVTO_FAC"
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
				ON  DETALLE_COMPROBANTE."FOLIO_COMP_FAC" = TBCTMFIF."FOLIO_COMP_FAC"
				AND DETALLE_COMPROBANTE."TIPO_COMP_FAC" = TBCTMFIF."TIPO_COMP_FAC"
				AND DETALLE_COMPROBANTE."LINEA_COMP_FAC" = TBCTMFIF."LIN_COMP_FAC"
				AND TBCTMFIF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFIF."RUT_DEST" else p_RutCliente end
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
				ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
		    LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
				ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
				AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
			 WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL	
			    AND COMPROBANTE."FEC_ING_COMP"  >= p_FechaInicial
			    AND COMPROBANTE."FEC_ING_COMP" <= p_FechaFinal; 
				
		--movimientos rf
       insert into "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"(    
             "IdMovimientoComprobanteDetalle","IdComprobanteDetalle" ,"IdMovimientoComprobante" ,"IdTransaccion" ,"IdInstrumento" ,"NemotecnicoInstrumento" 
			,"IdConceptoMovimiento","Cantidad" ,"Precio" ,"PrecioPromedio" ,"Monto" ,"Tasa" ,"TasaEmision" ,"SpreadTasaEmision" ,"ValorRescate" ,"ValorParidad" 
			,"ValorCaptacionMonedaOrigen" ,"SecuenciaAgrupacion" ,"IdEstadoMovimientoComprobanteDetalle" ,"FechaIngresoRegistro" ,"MontoLiquidacion" ,"IdMonedaLiquidacion" 
			,"MontoMovimientoComprobanteDetalle" ,"IdMonedaMovimientoComprobanteDetalle" ,"IdMoneda" ,"IdInstrumentoSubyacente" ,"IdMonedaSubyacente" ,"IdTipoInstrumento" 
			,"IdTipoInstrumentoSubyacente" ,"NombreTipoInstrumento" ,"NombreTipoInstrumentoSubyacente" ,"NemotecnicoSubyacente" ,"IdClasificacionTipoInstrumento" 
			,"IdClasificacionTipoInstrumentoRelacionado" ,"IdInstrumentoRelacionado" ,"IdTipoInstrumentoRelacionado" ,"NemotecnicoRelacionado" ,"NombreClasificacionTipoInstrumentoRelacionado" 
			,"NombreTipoInstrumentoRelacionado" ,"NombreClasificacionTipoInstrumento","FOLIO_COMP_FAC","TIPO_COMP_FAC","LIN_COMP_FAC","RUT_CLI","SEC_RUT_CLI","IdCarga"
			,"SecMovimientoFacturarMigracion"
			) 
        select       
       			 "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdComprobanteDetalle" as "IdComprobanteDetalle" 
				, MOV_COMPROBANTE."IdMovimientoComprobante" as "IdMovimientoComprobante"  
				, 0 as "IdTransaccion" 
				, DETALLE_COMPROBANTE."IdInstrumento" as "IdInstrumento" 
				, DETALLE_COMPROBANTE."NemotecnicoInstrumento" as "NemotecnicoInstrumento" 
				, '' as "IdConceptoMovimiento" 
				, TBCTMFRF."CANTIDAD" as "Cantidad" 
				, TBCTMFRF."PRECIO" as "Precio" 
				, 0 as "PrecioPromedio" 
				, TBCTMFRF."MONTO" as "Monto" 
				, TBCTMFRF."TIR" as "Tasa" 
				, 0 as "TasaEmision" 
				, 0 as "SpreadTasaEmision" 
				, 0 as "ValorRescate" 
				, TBCTMFRF."PARIDAD_MORIGEN" as "ValorParidad" 
				, TBCTMFRF."CAPTACION_MORIGEN" as "ValorCaptacionMonedaOrigen" 
				, 0 as "SecuenciaAgrupacion" 
				, DETALLE_COMPROBANTE."IdEstadoComprobanteDetalle" as "IdEstadoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."FechaIngresoRegistro" as "FechaIngresoRegistro" 
				, TBCTMFRF."MONTO" as "MontoLiquidacion" 
				, DETALLE_COMPROBANTE."IdMonedaLiquidacion" as "IdMonedaLiquidacion" 
				, DETALLE_COMPROBANTE."MontoComprobanteDetalle" as "MontoMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMonedaMovimientoComprobanteDetalle" 
				, DETALLE_COMPROBANTE."IdMoneda" as "IdMoneda" 
				, DETALLE_COMPROBANTE."IdInstrumentoSubyacente" as "IdInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."IdMonedaSubyacente" as  "IdMonedaSubyacente" 
				, DETALLE_COMPROBANTE."IdTipoInstrumento" as "IdTipoInstrumento" 
				, DETALLE_COMPROBANTE."IdTipoInstrumentoSubyacente" as "IdTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumento" as "NombreTipoInstrumento" 
				, DETALLE_COMPROBANTE."NombreTipoInstrumentoSubyacente"  as "NombreTipoInstrumentoSubyacente" 
				, DETALLE_COMPROBANTE."NemotecnicoSubyacente"  as "NemotecnicoSubyacente" 
				, DETALLE_COMPROBANTE."IdClasificacionTipoInstrumento"  as "IdClasificacionTipoInstrumento" 
				, '' as "IdClasificacionTipoInstrumentoRelacionado" 
				, 0  as "IdInstrumentoRelacionado" 
				, '' as "IdTipoInstrumentoRelacionado" 
				, '' as "NemotecnicoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumentoRelacionado" 
				, '' as "NombreTipoInstrumentoRelacionado" 
				, '' as "NombreClasificacionTipoInstrumento" 
				, DETALLE_COMPROBANTE."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
				, DETALLE_COMPROBANTE."TIPO_COMP_FAC"as "TIPO_COMP_FAC"
				, DETALLE_COMPROBANTE."LINEA_COMP_FAC"as "LIN_COMP_FAC"
				, DETALLE_COMPROBANTE."RUT_CLI" as "RUT_CLI"
				, DETALLE_COMPROBANTE."SEC_RUT_CLI" as "SEC_RUT_CLI"
				, p_IdCarga as "IdCarga"
				, TBCTMFRF."SEC_MOVTO_FAC" as  "SecMovimientoFacturarMigracion"
			FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" AS TBCTMFRF
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.MovimientoComprobante" AS MOV_COMPROBANTE 
				ON MOV_COMPROBANTE."SEC_MOVTO_FAC" = TBCTMFRF."SEC_MOVTO_FAC"
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
				ON  DETALLE_COMPROBANTE."FOLIO_COMP_FAC" = TBCTMFRF."FOLIO_COMP_FAC" 
				AND DETALLE_COMPROBANTE."TIPO_COMP_FAC"  = TBCTMFRF."TIPO_COMP_FAC" 
				AND DETALLE_COMPROBANTE."LINEA_COMP_FAC" = TBCTMFRF."LIN_COMP_FAC" 
				AND TBCTMFRF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRF."RUT_DEST" else p_RutCliente end
			INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
				ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
			LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
				ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
				AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
				AND MOV_COMP_DETALLE."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
			WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL	
			  AND COMPROBANTE."FEC_ING_COMP"  >= p_FechaInicial
			  AND COMPROBANTE."FEC_ING_COMP" <= p_FechaFinal;

		    -- identificamos los errores rv
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (
                 "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10"
				)
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLERV mov facturar no esta asociado a a detalle factura ' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFRV."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFRV."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFRV."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" AS TBCTMFRV
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFRV."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFRV."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFRV."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFRV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRV."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND DETALLE_COMPROBANTE."FechaIngresoRegistro" IS NULL;

              --detalle comprobante no tiene comprobante cabecera
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLERV detalle sin cabecera en optimus' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFRV."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFRV."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFRV."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" AS TBCTMFRV
					INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFRV."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFRV."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFRV."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFRV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRV."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
						ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND COMPROBANTE."FOLIO_COMP_FAC" IS NULL;
		
		-- errores IF
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (
                 "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10"
				)
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLERF mov facturar no esta asociado a a detalle factura ' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFIF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFIF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFIF."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" AS TBCTMFIF
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFIF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFIF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFIF."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFIF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFIF."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND DETALLE_COMPROBANTE."FechaIngresoRegistro" IS NULL;

              --detalle comprobante no tiene comprobante cabecera
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLEIF detalle sin cabecera en optimus' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFIF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFIF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFIF."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" AS TBCTMFIF
					INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFIF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFIF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFIF."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFIF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFIF."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
						ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND COMPROBANTE."FOLIO_COMP_FAC" IS NULL;
		
		-- errores RF
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              (
                 "IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10"
				)
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLERF mov facturar no esta asociado a a detalle factura ' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFRF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFRF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFRF."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" AS TBCTMFRF
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFRF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFRF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFRF."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFRF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRF."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND DETALLE_COMPROBANTE."FechaIngresoRegistro" IS NULL;

              --detalle comprobante no tiene comprobante cabecera
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'MOVIMIENTOCOMPROBANTEDETALLE' as "Entidad"
                    ,'MOVIMIENTOCOMPROBANTEDETALLERF detalle sin cabecera en optimus' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTMFRF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTMFRF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'LIN_COMP_FAC' as "NombreCampoGenerico7"
                    , TBCTMFRF."LIN_COMP_FAC" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              		FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" AS TBCTMFRF
					INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
					  	ON TBCTMFRF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
					  	AND TBCTMFRF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					  	AND TBCTMFRF."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
						AND TBCTMFRF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRF."RUT_DEST" else p_RutCliente end
					LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
						ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
					LEFT JOIN "BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle" AS MOV_COMP_DETALLE
						ON MOV_COMP_DETALLE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
						AND MOV_COMP_DETALLE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
						AND MOV_COMP_DETALLE."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
					WHERE MOV_COMP_DETALLE."LIN_COMP_FAC" IS NULL
					    AND COMPROBANTE."FOLIO_COMP_FAC" IS NULL;

	  	---solo contamos la renta variable
	  	SELECT count(*) into v_total_detcom_gestion_aux 
	  	FROM "BSCL_VIRTUAL"."GES_TBCTMFRV" AS TBCTMFRV
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
			ON TBCTMFRV."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			AND TBCTMFRV."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
			AND TBCTMFRV."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" AS COMPROBANTE
			ON COMPROBANTE."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			AND COMPROBANTE."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
		WHERE TBCTMFRV."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRV."RUT_DEST" else p_RutCliente end
		  AND COMPROBANTE."FEC_ING_COMP"  >= p_FechaInicial
		  AND COMPROBANTE."FEC_ING_COMP" <= p_FechaFinal;
					
		v_total_detcom_gestion = v_total_detcom_gestion+v_total_detcom_gestion_aux;
		v_total_detcom_gestion_aux=0;
					
		SELECT count(*) into v_total_detcom_gestion_aux
		FROM "BSCL_VIRTUAL"."GES_TBCTMFIF" AS TBCTMFIF
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
			ON TBCTMFIF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			AND TBCTMFIF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
			AND TBCTMFIF."LIN_COMP_FAC"  =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
			ON TBCTMSCF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			AND TBCTMSCF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
		WHERE TBCTMFIF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFIF."RUT_DEST" else p_RutCliente end
			AND TBCTMSCF."FEC_ING_COMP"  >= p_FechaInicial
			AND TBCTMSCF."FEC_ING_COMP" <= p_FechaFinal;

		v_total_detcom_gestion = v_total_detcom_gestion+v_total_detcom_gestion_aux;
		v_total_detcom_gestion_aux=0;
		
		SELECT count(*) into v_total_detcom_gestion_aux 
		FROM "BSCL_VIRTUAL"."GES_TBCTMFRF" AS TBCTMFRF
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS DETALLE_COMPROBANTE
			ON TBCTMFRF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
		   AND TBCTMFRF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
		   AND TBCTMFRF."LIN_COMP_FAC" =  DETALLE_COMPROBANTE."LINEA_COMP_FAC"
        INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" AS TBCTMSCF
			ON TBCTMSCF."FOLIO_COMP_FAC" =  DETALLE_COMPROBANTE."FOLIO_COMP_FAC"
			AND TBCTMSCF."TIPO_COMP_FAC" =  DETALLE_COMPROBANTE."TIPO_COMP_FAC"
		WHERE TBCTMFRF."RUT_DEST" = CASE WHEN TRIM(p_RutCliente) = '0-0'  then TBCTMFRF."RUT_DEST" else p_RutCliente end
			AND TBCTMSCF."FEC_ING_COMP"  >= p_FechaInicial
			AND TBCTMSCF."FEC_ING_COMP" <= p_FechaFinal;
		
		v_total_detcom_gestion = v_total_detcom_gestion+v_total_detcom_gestion_aux;
					
     	select count(*) into v_total_detcom_cargadas from 
     	"BSCL.Homologacion.BD::HOM.MovimientoComprobanteDetalle"
      	where "IdCarga" = p_IdCarga;

		-- SACAMOS LOS REGISTROS  comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGAMOVIMIENTOCOMPROBANTEDETALLE' as "CodigoProceso" 
   			  							,'FAC' as "Modulo" 
   			 							,v_total_detcom_gestion as "TotalRegistrosSebra"
   			  							,v_total_detcom_cargadas as "TotalRegistrosSap"  FROM DUMMY
   							    	union all
   							    	select * from :p_ResumenDatosProcesados;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
