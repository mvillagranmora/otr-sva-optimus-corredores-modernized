SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarComprobanteDetalle";

CREATE PROCEDURE
-- 
--  Proposito: servicios de carga de MOVIMIENTOS A FACTURAR RV
--  renta variable
--  Autor: - EAR 
"BSCL.Homologacion.BD.Servicios::CargarComprobanteDetalle"(
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
	   DECLARE v_total_detcom_cargadas DECIMAL(26,8):=0;	    
	   
	   DECLARE v_Quantity BIGINT:=0;
	   DECLARE v_PerId NVARCHAR(11);
		--------------------
		--Negocio Aqui
		--------------------

		-- convierto el rut a formato sebra 
		select lpad(p_RutCliente,11) into p_RutCliente from dummy;
		select TRIM(SUBSTRING(p_RutCliente,1,9)) into v_PerId from dummy;
		
		delete from "BSCL"."BSCL.Homologacion.BD::HOM.Error" where "Entidad" = 'DETALLECOMPROBANTE'
		and "IdCarga" = p_IdCarga;

		IF P_Modalidad = 'R' THEN --REPROCESO
	        DELETE FROM "BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOM
	        WHERE HOM."IdComprobanteDetalle" IN ( SELECT HOM."IdComprobanteDetalle" from "BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOM
							                      LEFT JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle"  AS DETALLE
							           				 ON DETALLE."IdComprobanteDetalle" = HOM."IdComprobanteDetalle"
							           				  WHERE HOM."IdCarga" = p_IdCarga
							           				  AND DETALLE."IdComprobanteDetalle" IS NULL);
		END IF;
		
        -- todos los tipos comprobantes exceptos los otros conceptos
		insert into "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle"(
		"IdComprobanteDetalle","IdComprobante","NroLinea","FechaLiquidacion","IdInstrumento","NemotecnicoInstrumento","IdTipoOperacion"
		,"IdTipoProducto","Cantidad","Precio","Monto","IdBolsa","IdAgente","IdSucursal","IdEstadoComprobanteDetalle","FechaIngresoRegistro"
		,"IdCarga","FolioComprobanteFacturacionMigracion","MontoLiquidacion","IdMoneda","IdMonedaLiquidacion","MontoComprobanteDetalle"
		,"IdMonedaComprobanteDetalle","IdInstrumentoSubyacente","IdMonedaSubyacente","IdTransaccion","FechaComprobanteDetalle","NemotecnicoSubyacente"
		,"IdTipoInstrumento","IdTipoInstrumentoSubyacente","NombreTipoInstrumento","NombreTipoInstrumentoSubyacente","IdClasificacionTipoInstrumento"
		,"IdClasificacionTipoInstrumentoRelacionado","IdInstrumentoRelacionado","IdTipoInstrumentoRelacionado","NemotecnicoRelacionado","NombreClasificacionTipoInstrumentoRelacionado"
		,"NombreTipoInstrumentoRelacionado","NombreClasificacionTipoInstrumento","RUT_CLI","SEC_RUT_CLI","LINEA_COMP_FAC","FOLIO_COMP_FAC" ,"TIPO_COMP_FAC"
		,"COD_MOVTO","MERCADO","FEC_VCTO","TIPO_OPERAC")
		SELECT
		 "BSCL.Facturacion.BD.Secuencias::MovimientoComprobanteDetalle".NEXTVAL as "IdComprobanteDetalle" 
		, COMPROBANTE_SAP."IdComprobante" as "IdComprobante" 
		, TBCTDTCF."LIN_COMP_FAC" as "NroLinea" 
		, COALESCE(TBCTMSCF."FEC_PAGO",'0001-01-01') as "FechaLiquidacion" 
		, COALESCE (INSTRUMENTO_SIM."IdInstrumento",INSTRUMENTO."IdInstrumento",0) as "IdInstrumento" 
		, COALESCE (INSTRUMENTO_SIM."Nemotecnico",INSTRUMENTO."Nemotecnico",'')as "NemotecnicoInstrumento" 
		,COMPROBANTE_SAP."IdTipoOperacion" as "IdTipoOperacion" 
		,COMPROBANTE_SAP."IdTipoProducto" as "IdTipoProducto" 
		,TBCTDTCF."CANTIDAD" as "Cantidad" 
		,TBCTDTCF."PRECIO" as "Precio" 
		,TBCTDTCF."MONTO" as "Monto" 
		,COALESCE(TBCTMSCF."IND_BOLSA",'') as "IdBolsa" 
		,EMPLEADO."IdEmpleado" as "IdAgente" 
		,SUCURSAL."IdSucursal" as "IdSucursal" 
		,ESTADO_COMPROBANTE."ValorDestino" as "IdEstadoComprobanteDetalle" 
		,COALESCE(TBCTMSCF."FEC_ING_COMP",'0001-01-01') as "FechaIngresoRegistro" 
		,p_IdCarga as "IdCarga" 
		,COALESCE(TBCTMSCF."FOLIO_COMP_FAC",0) as "FolioComprobanteFacturacionMigracion" 
		,TBCTDTCF."MONTO" as "MontoLiquidacion" 
		,TBCTDTCF."MON_MORIGEN" as "IdMoneda" 
		,'CLP' as "IdMonedaLiquidacion" 
		,TBCTDTCF."MONTO" as "MontoComprobanteDetalle" 
		,'CLP' as "IdMonedaComprobanteDetalle" 
		,CASE WHEN IFNULL(INSTRUMENTO_SIM."IdInstrumento",0) = 0 THEN 0 ELSE COALESCE(INSTRUMENTO."IdInstrumento",0) END as "IdInstrumentoSubyacente" 
		,CASE WHEN IFNULL(INSTRUMENTO_SIM."IdMoneda",'') = '' THEN 0 ELSE COALESCE(INSTRUMENTO."IdMoneda",'') END as "IdMonedaSubyacente" 
		,0 -- COALESCE(TRANSACCION."IdTransaccion",0) as "IdTransaccion"
		,COALESCE(TBCTMSCF."FEC_ING_COMP",'0001-01-01') as "FechaComprobanteDetalle" 
		,CASE WHEN IFNULL(INSTRUMENTO_SIM."Nemotecnico",'') = '' THEN '' ELSE COALESCE(INSTRUMENTO."Nemotecnico",'') END as "NemotecnicoSubyacente" 
		,COALESCE (INSTRUMENTO_SIM."IdTipoInstrumento",INSTRUMENTO."IdTipoInstrumento",'')  as "IdTipoInstrumento" 
		,CASE WHEN IFNULL(INSTRUMENTO_SIM."IdTipoInstrumento",'') = '' THEN '' ELSE COALESCE(INSTRUMENTO."IdTipoInstrumento",'') END as "IdTipoInstrumentoSubyacente" 
		,COALESCE(TIPOINSTRUMENTO_SIM."Nombre",TIPOINSTRUMENTO."Nombre",'')  as "NombreTipoInstrumento" 
		,CASE WHEN IFNULL(TIPOINSTRUMENTO_SIM."Nombre",'') = '' THEN '' ELSE COALESCE(TIPOINSTRUMENTO."Nombre",'') END as "NombreTipoInstrumentoSubyacente" 
		,COALESCE (TIPOINSTRUMENTO_SIM."IdClasificacionTipoInstrumento",TIPOINSTRUMENTO."IdClasificacionTipoInstrumento",'') as "IdClasificacionTipoInstrumento" 
		,'' as "IdClasificacionTipoInstrumentoRelacionado" 
		,0 as "IdInstrumentoRelacionado" 
		,'' as "IdTipoInstrumentoRelacionado" 
		,'' as "NemotecnicoRelacionado" 
		,'' as "NombreClasificacionTipoInstrumentoRelacionado" 
		,'' as "NombreTipoInstrumentoRelacionado" 
		,'' as "NombreClasificacionTipoInstrumento" 
		,TBCTMSCF."RUT_CLI" as "RUT_CLI"
		,TBCTMSCF."SEC_RUT_CLI" as "SEC_RUT_CLI"
		,TBCTDTCF."LIN_COMP_FAC" as "LINEA_COMP_FAC"
		,TBCTDTCF."FOLIO_COMP_FAC" as "FOLIO_COMP_FAC"
		,TBCTDTCF."TIPO_COMP_FAC" as "TIPO_COMP_FAC"
		,TBCTDTCF."COD_MOVTO" as "COD_MOVTO"
		,TBCTDTCF."MERCADO" as "MERCADO"
		,TBCTDTCF."FEC_VCTO" as "FEC_VCTO"
		,TBCTDTCF."TIPO_OPERAC" as "TIPO_OPERAC"
		FROM  "BSCL_VIRTUAL"."GES_TBCTDTCF" as  TBCTDTCF
		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as TBCTMSCF
			ON TBCTDTCF."FOLIO_COMP_FAC" = TBCTMSCF."FOLIO_COMP_FAC"
			AND TBCTDTCF."TIPO_COMP_FAC" = TBCTMSCF."TIPO_COMP_FAC"
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as COMPROBANTE_SAP -- debo descomentar cuando cargue los comprobantes
		     ON COMPROBANTE_SAP."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
			AND COMPROBANTE_SAP."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
		INNER JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Homologacion" as ESTADO_COMPROBANTE
            ON  ESTADO_COMPROBANTE."TipoValor" = 'EstadoFacturacion'
            AND ESTADO_COMPROBANTE."ValorOrigen" = TBCTMSCF."EST_COMP"	
		LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO
		    ON INSTRUMENTO."Nemotecnico" = IFNULL(TRIM(TBCTDTCF."NEMO"),'')
		LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO
		    ON TIPOINSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO."IdTipoInstrumento"
		LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INSTRUMENTO_SIM
		    ON INSTRUMENTO_SIM."IdInstrumento" = CASE WHEN (TBCTMSCF."IND_SIMUL" = 'TS' OR TBCTMSCF."IND_SIMUL" = 'TP') THEN
		                                               CASE WHEN  TBCTDTCF."TIPO_OPERAC" ='V' THEN 
		                                                     2249
		                                                ELSE 
		                                                     2248
		                                               END
		                                           ELSE -1
		                                          END  
		LEFT JOIN "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" as TIPOINSTRUMENTO_SIM
		    ON TIPOINSTRUMENTO."IdTipoInstrumento" = INSTRUMENTO_SIM."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" as EMPLEADO
	  	   	ON EMPLEADO."CodigoAgenteMigracion"  = TBCTMSCF."COD_AGENTE"
	  	LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUCURSAL
	  	   	ON SUCURSAL."CodigoSucursal" = TRIM(TBCTMSCF."COD_SUC")
	  	   	AND SUCURSAL."IdEmpresa" = p_IdEmpresa
		LEFT JOIN "BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION_DETALLE_COMP
	    	ON HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
			AND HOMOLOGACION_DETALLE_COMP."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
			AND HOMOLOGACION_DETALLE_COMP."LINEA_COMP_FAC" = TBCTDTCF."LIN_COMP_FAC"
		WHERE HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" IS NULL
		    AND COMPROBANTE_SAP."FEC_ING_COMP" >= p_FechaInicial
		    AND COMPROBANTE_SAP."FEC_ING_COMP" <= p_FechaFinal;
		  
	    --realizaremos un update para tratar de recuperar la transaccion
        UPDATE A1 SET A1."IdTransaccion" = COALESCE(A4."IdTransaccion",0) 
        FROM "BSCL"."BSCL.Homologacion.BD::HOM.ComprobanteDetalle" as A1
            ,"BSCL_VIRTUAL"."GES_TBCTMSCF" as A2
            ,"BSCL_VIRTUAL"."GES_TBCTMFRV" as A3
            ,"BSCL"."BSCL.Homologacion.BD::HOM.Asignacion" as A4
        WHERE A1."FOLIO_COMP_FAC" = A2."FOLIO_COMP_FAC"
          AND A1."TIPO_COMP_FAC" = A2."TIPO_COMP_FAC"
          AND A3."FOLIO_COMP_FAC" = A2."FOLIO_COMP_FAC"
          AND A3."TIPO_COMP_FAC" = A2."TIPO_COMP_FAC"
          AND A4."SEC_MOVTO_FAC" = A3."SEC_MOVTO_FAC";

		-- contamos los errores
		-- todo detalle debe tener una cabecera
		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'DETALLECOMPROBANTE' as "Entidad"
                    ,'TBCTDTCF no tiene cabecera en TBCTMSCF en sebra' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTDTCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTDTCF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
           FROM "BSCL_VIRTUAL"."GES_TBCTDTCF" as  TBCTDTCF
		   LEFT JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as TBCTMSCF
				ON TBCTMSCF."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
				AND TBCTMSCF."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
		   LEFT JOIN "BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION_DETALLE_COMP
	    		ON HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
				AND HOMOLOGACION_DETALLE_COMP."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
				AND HOMOLOGACION_DETALLE_COMP."LINEA_COMP_FAC" = TBCTDTCF."LIN_COMP_FAC"
			WHERE HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" IS NULL
		    	AND TBCTMSCF."FOLIO_COMP_FAC" IS NULL
		    	AND TBCTMSCF."FEC_ING_COMP" >= p_FechaInicial
		    	AND TBCTMSCF."FEC_ING_COMP" <= p_FechaFinal;

			 -- todo detalle de comprobante debe haber sido cargado en optimus en tabla homologacion	
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    ,'DETALLECOMPROBANTE' as "Entidad"
                    ,'TBCTMSCF no fue migrado a HOM.Comprobante en Optimus' as "Mensaje"
                    , 'TIPO_COMP_FAC' as "NombreCampoGenerico1"
                    , TBCTDTCF."TIPO_COMP_FAC" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'FOLIO_COMP_FAC' as "NombreCampoGenerico6"
                    , TBCTDTCF."FOLIO_COMP_FAC" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'' as "NombreCampoGenerico9"
                    ,'0001-01-01' as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
              FROM "BSCL_VIRTUAL"."GES_TBCTDTCF" as  TBCTDTCF
			  INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as TBCTMSCF
					ON TBCTMSCF."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
					AND TBCTMSCF."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
					AND TBCTMSCF."MERCADO" NOT IN ('RF','IF')
			  LEFT JOIN "BSCL"."BSCL.Homologacion.BD::HOM.Comprobante" as COMPROBANTE_SAP
		     		ON COMPROBANTE_SAP."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
					AND COMPROBANTE_SAP."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
		   	  LEFT JOIN "BSCL.Homologacion.BD::HOM.ComprobanteDetalle" AS HOMOLOGACION_DETALLE_COMP
	    			ON HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
					AND HOMOLOGACION_DETALLE_COMP."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
					AND HOMOLOGACION_DETALLE_COMP."LINEA_COMP_FAC" = TBCTDTCF."LIN_COMP_FAC"
			  WHERE HOMOLOGACION_DETALLE_COMP."FOLIO_COMP_FAC" IS NULL
			        AND COMPROBANTE_SAP."FOLIO_COMP_FAC" IS NULL
		    	 	AND TBCTMSCF."FEC_ING_COMP" >= p_FechaInicial
		    		AND TBCTMSCF."FEC_ING_COMP" <= p_FechaFinal;

	  	SELECT count(*) into v_total_detcom_gestion
	  	FROM "BSCL_VIRTUAL"."GES_TBCTDTCF" as  TBCTDTCF
		INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMSCF" as TBCTMSCF
			ON TBCTMSCF."FOLIO_COMP_FAC" = TBCTDTCF."FOLIO_COMP_FAC"
			AND TBCTMSCF."TIPO_COMP_FAC" = TBCTDTCF."TIPO_COMP_FAC"
		WHERE TBCTMSCF."FEC_ING_COMP" >= p_FechaInicial
		  AND TBCTMSCF."FEC_ING_COMP" <= p_FechaFinal;

     	select count(*) into v_total_detcom_cargadas from 
     	"BSCL.Homologacion.BD::HOM.ComprobanteDetalle"
      	where "IdCarga" = p_IdCarga;

		-- SACAMOS LOS REGISTROS  comprobantes
	   	p_ResumenDatosProcesados =  select 'CARGADETALLECOMPROBANTE' as "CodigoProceso" 
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

 
