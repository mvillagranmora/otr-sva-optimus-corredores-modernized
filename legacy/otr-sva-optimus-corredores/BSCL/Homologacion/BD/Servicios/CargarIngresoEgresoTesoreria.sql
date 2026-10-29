SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Homologacion.BD.Servicios::CargarIngresoEgresoTesoreria";

CREATE PROCEDURE
--  Proposito: servicios de carga de movimientos de tesoreria 
--  renta variable
--  Autor: - EAR
"BSCL.Homologacion.BD.Servicios::CargarIngresoEgresoTesoreria"(
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
	    DECLARE v_Total_Operaciones_Test BIGINT:=0;
	    DECLARE v_Total_Test_Cargados BIGINT :=0;
		   	        
               --EGRESOS MONETARIOS  egresos en moneda extranjera dolar preferencialmente en IMTRUST
				INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
				(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
					,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
					,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
					,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
					,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
					,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
					,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
					,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
					,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
					,"FOLIO_CMBTE_ING" ,"FOLIO_CMBTE_EGR" ,"SEC_MOVTO_CARGO" ,"SEC_MOVTO_ABONO"
					,"TIPO_CMBTE"
				)	
               select 
					"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso"  
					,p_IdEmpresa as "IdEmpresa" 
					,p_Modulo as "IdModulo"  
					,p_IdUsuario as "IdUsuario" 
					,TIPO_COMP_TESORERIA."ValorDestino" as "IdTipoMovimiento" --MOVING MOVEGR esos pueden ser 
					,CONCEPTO."ValorDestino" as "IdConcepto"  
					,0  as "IdCaja" 
					,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
					,TBTECMBT."MONTO" as "Monto" 
					,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
					,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaIngresoRegistro" 
					,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaMovimiento" 
					,'' as "IdTipoPersona"
					, PERSONA."IdPersona" as "IdPersona" 
					, PERSONA."Nombres"||' '||PERSONA."ApellidoPaterno"||' '||PERSONA."ApellidoMaterno" as "NombrePersona" 
					, 'R' as "IdTipoDocumentoIdentidad" 
					, DOCUMENTO_IDENTIDAD."DocumentoIdentidad" as "DocumentoIdentidad" 
					, '' as "IdTipoPersonaTitularPago" 
					, PERSONA."IdPersona" as "IdPersonaTitularPago" 
					, PERSONA."Nombres"||' '||PERSONA."ApellidoPaterno"||' '||PERSONA."ApellidoMaterno"  as "NombrePersonaTitularPago" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
					, '' as "SecuenciaAgrupacion" 
					, '' as "Glosa" 
					, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
					, 0  as "IdSucursalEmpleadoDigitador" 
					, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
					, 0  as "IdComprobanteOrigen" --  vienen de facturacion
					,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
					,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
					,'' as "IdTipoProducto" --  vienen de facturacion
					,'' as "IdTipoOperacion" --  vienen de facturacion
					, 0 as "IdOperacion"-- que es esto  
					, '0001-01-01' as "FechaOperacion" 
					,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
					,'' as "IdBolsa" --  vienen de facturacion 
					,'' as "IdMonedaOperacion" --  vienen de facturacion
					,TBTECMBT."MONTO" as "MontoOperacion" 
					,'0001-01-01' as "FechaParidad" 
					, '' as "IdTipoReajuste" --  vienen de facturacion 
					, 0 as "ValorParidad" -- 
					, 0 as "FolioCartera" 
					, 0 as "IdInstrumento" 
					, '' as "IdTipoInstrumento" 
					, '' as "IdClasificacionTipoInstrumento" 
					, '' as "EsBilateral" 
					, 'S' as "TitularPago" 
					, 'S' as "EsIngresoAutomatico" 
					, 'N' as "AgrupaComprobante" 
					,  0 as "IdUsuarioBloqueo" 
					,'0001-01-01' as "FechaHoraBloqueo" 
					,p_IdCarga as "IdCarga" 
					,CASE WHEN TBTECMBT."EST_CMBTE" ='I' then 'ING'
                      else CASE WHEN TBTECMBT."EST_CMBTE" ='A' then 'ANU'
                           else 'EMI'
                           end
                     end as "IdEstado" 
					,'N' as "MueveCuentaInversion"-- revisar 
					, 0 as "SecuenciaRelacionada" 
					,'N' as "NeteaComprobante" 
					, 0 as "IdUsuarioModificacion" 
					,'0001-01-01' as "FechaHoraModificacion" 
					, 0 as "SEC_MOVTO" 
					,'0001-01-01' as  "FEC_MOVTO"
					, 0 as "FOLIO_COMP_ADJ"
					,'' as "TIPO_COMP_ADJ"
					,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
					,0 as "FOLIO_CMBTE"
					,TBTEDCTO."FEC_CMBTE" as "FEC_CMBTE"
					,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM"
					, 0 as "FOLIO_CMBTE_ING" 
					, TBMOMVTO."FOLIO_CMBTE_EGR" as "FOLIO_CMBTE_EGR" 
					, 0 as "SEC_MOVTO_CARGO"
					, 0 as "SEC_MOVTO_ABONO"
					,'' as "TIPO_CMBTE" 
                   FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
					 INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 
					 INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
					    ON PORTAFOLIO."IdCliente" =  CLIENTE."IdCliente"     	
					  	AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''   	
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE" 	
                 	 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"	
                      WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

             -- log de errores primera consulta

             -- un movimiendo de egreso debe tener un comprobante si el campo egreso comprobante es mayor que cero
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS ' as "Entidad"
                    , 'TBMOMVTO no encontro registro egreso asociado en TBTECMBT' as "Mensaje"
                    , 'FOLIO_CMBTE_EGR' as "NombreCampoGenerico1"
                    , TBMOMVTO."FOLIO_CMBTE_EGR" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4" 
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0  as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     LEFT JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR" 
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND TBTECMBT."FOLIO_CMBTE" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

             -- el rut debe estar en la tabla documento identidad
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3" ,"NombreCampoGenerico4"  ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7"  ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10" ,"ValorCampoGenerico10")
             select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS ' as "Entidad"
                    , 'El Cliente no existe en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , '' as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND DOCUMENTO_IDENTIDAD."DocumentoIdentidad" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

             --  la persona debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS ' as "Entidad"
                    , 'la persona no se encuentra creada en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBMOMVTO."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , ''  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND PERSONA."IdPersona" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

              -- el cliente debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS' as "Entidad"
                    , 'la persona no tiene creado el cliente' as "Mensaje"
                    , 'RUT_DEST' as "NombreCampoGenerico1"
                    , TBTECMBT."RUT_DEST" as "ValorCampoGenerico1"
                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
                    , TO_CHAR(TBTECMBT."SEC_RUT_DEST") as "ValorCampoGenerico2"
                    , 'IdPersona' as "NombreCampoGenerico3"
                    , TO_CHAR(PERSONA."IdPersona") as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 	
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND CLIENTE."IdCliente" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

				-- el comprobante de tesoreria no esta homologado en optimus
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS' as "Entidad"
                    , 'el comprobante de tesoreria no esta homologado optimus' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoComprobanteTesoreria' as "ValorCampoGenerico1"
                    , 'TIPO_CMBTE' as "NombreCampoGenerico2"
                    ,  TBTEDCTO."TIPO_CMBTE" as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'FOLIO_CMBTE' as "NombreCampoGenerico7"
                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 	
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND TIPO_COMP_TESORERIA."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

                -- EL TIPO MONEDA TESORERIA debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS' as "Entidad"
                    , 'la moneda no se homologado en optimus' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
                    , 'TIPO_MON' as "NombreCampoGenerico2"
                    ,  TBTEDCTO."TIPO_MON"  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'FOLIO_CMBTE' as "NombreCampoGenerico7"
                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 	
					 INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND TIPO_MONEDA_TESORERIA."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK
               
             -- el concepto debe estar homologado
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS' as "Entidad"
                    , 'la moneda no se homologado en optimus' as "Mensaje"
                    , 'TipoValor' as "NombreCampoGenerico1"
                    , 'ConceptoTesoreria' as "ValorCampoGenerico1"
                    , 'CONCEPTO_MVTO' as "NombreCampoGenerico2"
                    ,  TBTECMBT."CONCEPTO_MVTO" as "ValorCampoGenerico2"
                    , 'TIPO_CMBTE' as "NombreCampoGenerico3"
                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'FOLIO_CMBTE' as "NombreCampoGenerico7"
                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 	
					 INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                     LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE"	
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND CONCEPTO."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

                --INGRESOS MONETARIOS ingreso en moneda extranjera dolar preferencialmente en IMTRUST
				INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
				(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
					,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
					,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
					,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
					,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
					,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
					,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
					,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
					,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
					,"FOLIO_CMBTE_ING","FOLIO_CMBTE_EGR","SEC_MOVTO_CARGO","SEC_MOVTO_ABONO","TIPO_CMBTE"
				)	
               select 
					"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso" 
					,p_IdEmpresa as "IdEmpresa" 
					,p_Modulo as "IdModulo"  
					,p_IdUsuario as "IdUsuario" 
					,TIPO_COMP_TESORERIA."ValorDestino" as "IdTipoMovimiento" --MOVING MOVEGR esos pueden ser 
					,'' as "IdConcepto" 
					,0  as "IdCaja" 
					,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
					,TBTECMBT."MONTO" as "Monto" 
					,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
					,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaIngresoRegistro" 
					,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaMovimiento" 
					,'' as "IdTipoPersona"
					, CLIENTE."IdPersona" as "IdPersona" 
					, CLIENTE."NombreCompleto" as "NombrePersona" 
					, 'R' as "IdTipoDocumentoIdentidad" 
					, CLIENTE."DocumentoIdentidad" as "DocumentoIdentidad" 
					, '' as "IdTipoPersonaTitularPago" 
					, CLIENTE."IdPersona" as "IdPersonaTitularPago" 
					, CLIENTE."NombreCompleto"  as "NombrePersonaTitularPago" 
					, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
					, '' as "SecuenciaAgrupacion" 
					, '' as "Glosa" 
					, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
					, 0  as "IdSucursalEmpleadoDigitador" 
					, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
					, 0  as "IdComprobanteOrigen" --  vienen de facturacion
					,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
					,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
					,'' as "IdTipoProducto" --  vienen de facturacion
					,'' as "IdTipoOperacion" --  vienen de facturacion
					, 0 as "IdOperacion"-- que es esto  
					, '0001-01-01' as "FechaOperacion" 
					,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
					,'' as "IdBolsa" --  vienen de facturacion 
					,'' as "IdMonedaOperacion" --  vienen de facturacion
					,TBTECMBT."MONTO" as "MontoOperacion" 
					,'0001-01-01' as "FechaParidad" 
					, '' as "IdTipoReajuste" --  vienen de facturacion 
					, 0 as "ValorParidad" -- 
					, 0 as "FolioCartera" 
					, 0 as "IdInstrumento" 
					, '' as "IdTipoInstrumento" 
					, '' as "IdClasificacionTipoInstrumento" 
					, '' as "EsBilateral" 
					, 'S' as "TitularPago" 
					, 'S' as "EsIngresoAutomatico" 
					, 'N' as "AgrupaComprobante" 
					,  0 as "IdUsuarioBloqueo" 
					,'0001-01-01' as "FechaHoraBloqueo" 
					,p_IdCarga as "IdCarga" 
					,CASE WHEN TBTECMBT."EST_CMBTE" ='I' then 'ING'
                      else CASE WHEN TBTECMBT."EST_CMBTE"  ='A' then 'ANU'
                           else 'EMI'
                           end
                     end as "IdEstado" 
					,'N' as "MueveCuentaInversion"-- revisar 
					, 0 as "SecuenciaRelacionada" 
					,'N' as "NeteaComprobante" 
					, 0 as "IdUsuarioModificacion" 
					,'0001-01-01' as "FechaHoraModificacion" 
					, 0 as "SEC_MOVTO" 
					,'0001-01-01' as  "FEC_MOVTO"
					, 0 as "FOLIO_COMP_ADJ"
					,'' as "TIPO_COMP_ADJ"
					,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
					,0 as "FOLIO_CMBTE"
					,TBTEDCTO."FEC_CMBTE" as "FEC_CMBTE"
					,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM"
					,TBMOMVTO."FOLIO_CMBTE_ING" as "FOLIO_CMBTE_ING"   
					, 0 as "FOLIO_CMBTE_EGR"  
					, 0 as "SEC_MOVTO_CARGO" 
					, 0 as "SEC_MOVTO_ABONO"
					,'' as "TIPO_CMBTE" 
				FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" 
                     INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
			    	 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE"
                 	LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                      WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK
  	
             -- INGRESO MONETARIOS LOG
			 -- log de errores SEGUNDA consulta
             -- un movimiendo de egreso debe tener un comprobante si el campo egreso comprobante es mayor que cero
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA INGRESO ' as "Entidad"
                    , 'TBMOMVTO no encontro registro ingreso asociado en TBTECMBT' as "Mensaje"
                    , 'FOLIO_CMBTE_EGR' as "NombreCampoGenerico1"
                    , TBMOMVTO."FOLIO_CMBTE_EGR" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                     ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    ,0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     LEFT JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                   	 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND TBMOMVTO."FOLIO_CMBTE_ING" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK

             -- el movimiento de comprobante debe estar en la tabla documento
			  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA INGRESO ' as "Entidad"
                    , 'TBTECMBT no encontro registro asociado TBTEDCTO' as "Mensaje"
                    , 'FOLIO_CMBTE' as "NombreCampoGenerico1"
                    , TBTECMBT."FOLIO_CMBTE"  as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                     ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    ,0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                     LEFT JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" 	
                   	 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                    WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND TBTEDCTO."FOLIO_CMBTE" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK

             -- un movimiendo de ingreso debe tener un cliente  
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    , p_IdCarga as "IdCarga"
                    , 'TESORERIA INGRESO ' as "Entidad"
                    , 'El Cliente no existe en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBMOMVTO."RUT_CLI" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                     ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND CLIENTE."DocumentoIdentidad" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK

             --  el portafolio debe existir 
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA INGRESO ' as "Entidad"
                    , 'El Portafolio no se encuentra creado en optimus' as "Mensaje"
                    , 'RUT_CLI' as "NombreCampoGenerico1"
                    , TBMOMVTO."RUT_CLI" as "ValorCampoGenerico1"
                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2"
                    , TBMOMVTO."SEC_RUT_CLI"  as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    , '' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			   FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''
					 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND PORTAFOLIO."IdCliente" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK

              --  el tipo del comprobante debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA INGRESO ' as "Entidad"
                    , 'El concepto comprobante del documento no esta homologado en optimus' as "Mensaje"
                    , 'TIPO_CMBTE' as "NombreCampoGenerico1"
                    , TBTEDCTO."TIPO_CMBTE" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''
			         LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				     LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND TIPO_COMP_TESORERIA."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK

 			 --  el concepto del comprobante debe existir
			 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
				,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
				,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
				,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
                    ,p_IdCarga as "IdCarga"
                    , 'TESORERIA EGRESOS ' as "Entidad"
                    , 'El concepto comprobante del documento no esta homologado en optimus' as "Mensaje"
                    , 'TIPO_CMBTE' as "NombreCampoGenerico1"
                    , TBTEDCTO."TIPO_CMBTE" as "ValorCampoGenerico1"
                    , '' as "NombreCampoGenerico2"
                    , '' as "ValorCampoGenerico2"
                    , '' as "NombreCampoGenerico3"
                    , '' as "ValorCampoGenerico3"
                    , '' as "NombreCampoGenerico4"
                    , '' as "ValorCampoGenerico4"
                    , '' as "NombreCampoGenerico5"
                    , '' as "ValorCampoGenerico5"
                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
                    ,'' as "NombreCampoGenerico7"
                    , 0 as "ValorCampoGenerico7"
                    ,'' as "NombreCampoGenerico8"
                    , 0 as "ValorCampoGenerico8"
                    ,'FEC_MVTO' as "NombreCampoGenerico9"
                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
                    ,'' as "NombreCampoGenerico10"
                    ,'0001-01-01' as "ValorCampoGenerico10"
    			    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBMOMVTO."FOLIO_CMBTE_ING"=TBTECMBT."FOLIO_CMBTE"
                     INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''
			         INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE"
				     LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" = TBMOMVTO."FOLIO_CMBTE_ING"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_ING" IS NULL
                        AND CONCEPTO."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_ING" > 0;--OK
                
                -- OPERACIONES DE ORDENES DE INGRESO Y EGRESO QUE NO VIENEN LA MOMVTO	
                INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
				(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
					,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
					,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
					,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
					,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
					,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
					,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
					,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
					,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
					,"FOLIO_CMBTE_ING","FOLIO_CMBTE_EGR","SEC_MOVTO_CARGO","SEC_MOVTO_ABONO","TIPO_CMBTE"
				)
                	select 
							"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso" 
							,p_IdEmpresa as "IdEmpresa" 
							,p_Modulo as "IdModulo"  
							,p_IdUsuario as "IdUsuario" 
							,TIPO_COMP_TESORERIA."ValorDestino" as "IdTipoMovimiento" --MOVING MOVEGR esos pueden ser 
							,CONCEPTO."ValorDestino" as "IdConcepto" -- 
							,0  as "IdCaja" 
							,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
							,TBTECMBT."MONTO" as "Monto" 
							,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
							,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaIngresoRegistro" 
							,SUBSTRING(TO_CHAR(TBTECMBT."FEC_ING"),1,10) as "FechaMovimiento" 
							,'' as "IdTipoPersona"
							, CLIENTE."IdPersona" as "IdPersona" 
							, CLIENTE."NombreCompleto" as "NombrePersona" 
							, 'R' as "IdTipoDocumentoIdentidad" 
							, CLIENTE."DocumentoIdentidad" as "DocumentoIdentidad" 
							, '' as "IdTipoPersonaTitularPago" 
							, CLIENTE."IdPersona" as "IdPersonaTitularPago" 
							, CLIENTE."NombreCompleto"  as "NombrePersonaTitularPago" 
							, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
							, '' as "SecuenciaAgrupacion" 
							, '' as "Glosa" 
							, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
							, 0  as "IdSucursalEmpleadoDigitador" 
							, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
							, 0  as "IdComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
							,'' as "IdTipoProducto" --  vienen de facturacion
							,'' as "IdTipoOperacion" --  vienen de facturacion
							, 0 as "IdOperacion"-- que es esto  
							, '0001-01-01' as "FechaOperacion" 
							,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
							,'' as "IdBolsa" --  vienen de facturacion 
							,'' as "IdMonedaOperacion" --  vienen de facturacion
							,TBTECMBT."MONTO" as "MontoOperacion" 
							,'0001-01-01' as "FechaParidad" 
							, '' as "IdTipoReajuste" --  vienen de facturacion 
							, 0 as "ValorParidad" -- 
							, 0 as "FolioCartera" 
							, 0 as "IdInstrumento" 
							, '' as "IdTipoInstrumento" 
							, '' as "IdClasificacionTipoInstrumento" 
							, '' as "EsBilateral" 
							, 'S' as "TitularPago" 
							, 'S' as "EsIngresoAutomatico" 
							, 'N' as "AgrupaComprobante" 
							,  0 as "IdUsuarioBloqueo" 
							,'0001-01-01' as "FechaHoraBloqueo" 
							,p_IdCarga as "IdCarga" 
							,CASE WHEN TBTECMBT."EST_CMBTE" ='I' then 'ING'
                      			 else CASE WHEN TBTECMBT."EST_CMBTE"  ='A' then 'ANU'
                          		 else 'EMI'
                          		 end
                    		 end as "IdEstado" 
							,'N' as "MueveCuentaInversion"-- revisar 
							, 0 as "SecuenciaRelacionada" 
							,'N' as "NeteaComprobante" 
							, 0 as "IdUsuarioModificacion" 
							,'0001-01-01' as "FechaHoraModificacion" 
							, 0 as "SEC_MOVTO" 
							,'0001-01-01' as  "FEC_MOVTO"
							, 0 as "FOLIO_COMP_ADJ"
							,'' as "TIPO_COMP_ADJ"
							,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
							,TBTEDCTO."FOLIO_CMBTE" as "FOLIO_CMBTE"
							,TBTEDCTO."FEC_CMBTE" as "FEC_CMBTE"
							,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM"
							, 0 as "FOLIO_CMBTE_ING" 
							, 0 as "FOLIO_CMBTE_EGR" 
							, 0 as "SEC_MOVTO_CARGO" 
							, 0 as "SEC_MOVTO_ABONO"
							,TBTECMBT."TIPO_CMBTE" as "TIPO_CMBTE"
					 FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
					 INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
                        AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
			    	 INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||'' --53152
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTECMBT."TIPO_CMBTE"
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE"	
                     LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
						ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
						AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
                     WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
                     	AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK

                 -- el comprobante debe tener un documento asociado
				 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
	              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
					,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
					,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
					,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
	              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
	                    ,p_IdCarga as "IdCarga"
	                    , 'TESORERIA OI/OP' as "Entidad"
	                    , 'el comprobante OI/OP no tiene documento creado sebra' as "Mensaje"
	                    , 'FOLIO_CMBTE' as "NombreCampoGenerico1"
	                    , TO_CHAR(TBTECMBT."FOLIO_CMBTE") as "ValorCampoGenerico1"
	                    , 'TIPO_CMBTE' as "NombreCampoGenerico2"
	                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico2"
	                    , '' as "NombreCampoGenerico3"
	                    , '' as "ValorCampoGenerico3"
	                    , '' as "NombreCampoGenerico4"
	                    , '' as "ValorCampoGenerico4"
	                    , '' as "NombreCampoGenerico5"
	                    , '' as "ValorCampoGenerico5"
	                    ,'' as "NombreCampoGenerico6"
	                    , 0 as "ValorCampoGenerico6"
	                    ,'' as "NombreCampoGenerico7"
	                    , 0 as "ValorCampoGenerico7"
	                    ,'' as "NombreCampoGenerico8"
	                    , 0 as "ValorCampoGenerico8"
	                    ,'' as "NombreCampoGenerico9"
	                    ,'0001-01-01' as "ValorCampoGenerico9"
	                    ,'' as "NombreCampoGenerico10"
	                    ,'0001-01-01' as "ValorCampoGenerico10"
	    			   FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
						 LEFT JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
						  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
	                        AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
	                     LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
							ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
							AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
                     	 WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
                     		AND TBTEDCTO."FOLIO_CMBTE" IS NULL
                     		AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    	AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK

		 			-- el movimiento comprobante debe tener un cliente asociado
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA OI/OP' as "Entidad"
		                    , 'El Cliente no existe en optimus' as "Mensaje"
		                    , 'RUT_CLI' as "NombreCampoGenerico1"
		                    , TBTECMBT."RUT_DEST" as "ValorCampoGenerico1"
		                    , '' as "NombreCampoGenerico2"
		                    , '' as "ValorCampoGenerico2"
		                    , '' as "NombreCampoGenerico3"
		                    , '' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico5"
		                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico5"
		                    ,'FOLIO_CMBTE' as "NombreCampoGenerico6"
		                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico6"
		                     ,'' as "NombreCampoGenerico7"
		                    , 0 as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'' as "NombreCampoGenerico9"
		                    ,'0001-01-01' as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
							INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
							  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
		                      	AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
	                     	LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
	                     	LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
								ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
								AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
	                     	WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
	                     		AND CLIENTE."DocumentoIdentidad" IS NULL
	                     		AND TBTEDCTO."FOLIO_CMBTE" IS NULL
	                     		AND TBTECMBT."FEC_ING" >= p_FechaInicial
			        	    	AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK

					-- el movimiento comprobante debe tener un portafolio
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    , p_IdCarga as "IdCarga"
		                    , 'TESORERIA OI/OP' as "Entidad"
		                    , 'El Cliente no existe en optimus' as "Mensaje"
		                    , 'RUT_CLI' as "NombreCampoGenerico1"
		                    , TBTECMBT."RUT_DEST" as "ValorCampoGenerico1"
		                    , 'SEC_RUT_DEST' as "NombreCampoGenerico2"
		                    , TBTECMBT."SEC_RUT_DEST" as "ValorCampoGenerico2"
		                    , '' as "NombreCampoGenerico3"
		                    , '' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico5"
		                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico5"
		                    ,'FOLIO_CMBTE' as "NombreCampoGenerico6"
		                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico6"
		                     ,'' as "NombreCampoGenerico7"
		                    , 0 as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'' as "NombreCampoGenerico9"
		                    ,'0001-01-01' as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
							INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
							  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
		                      	AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
	                     	INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
	                     	LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			           			 ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            		AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''
	                     	LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
								ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
								AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
	                     	WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
	                     		AND PORTAFOLIO."IdCliente" IS NULL
	                     		AND TBTECMBT."FEC_ING" >= p_FechaInicial
			        	    	AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK

					-- el tipo comprobante debe estar homologado
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA OI/OP' as "Entidad"
		                    , 'El portafolio no existe en optimus' as "Mensaje"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico1"
		                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico1"
		                    , '' as "NombreCampoGenerico2"
		                    , '' as "ValorCampoGenerico2"
		                    , 'TipoValor' as "NombreCampoGenerico3"
		                    , 'TipoComprobanteTesoreria' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico5"
		                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico5"
		                    ,'FOLIO_CMBTE' as "NombreCampoGenerico6"
		                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico6"
		                     ,'' as "NombreCampoGenerico7"
		                    , 0 as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'' as "NombreCampoGenerico9"
		                    ,'0001-01-01' as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
							INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
							  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
		                      	AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
	                     	INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
	                     	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
				            	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
				           		AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||'' --53152
                            LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
	                     		ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
	                     		AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTECMBT."TIPO_CMBTE"   
	                     	LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
								ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
								AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
	                     	WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
	                     		AND TIPO_COMP_TESORERIA."TipoValor" IS NULL
	                     		AND TBTECMBT."FEC_ING" >= p_FechaInicial
			        	    	AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK

						-- el tipo moneda debe estar homologada
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA OI/OP' as "Entidad"
		                    , 'el tipo moneda no esta homologado' as "Mensaje"
		                    , 'TipoValor' as "NombreCampoGenerico1"
		                    , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
		                    , 'TIPO_MON' as "NombreCampoGenerico2"
		                    , TRIM(TBTEDCTO."TIPO_MON") as "ValorCampoGenerico2"
		                    , '' as "NombreCampoGenerico3"
		                    , '' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico5"
		                    , TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico5"
		                    ,'FOLIO_CMBTE' as "NombreCampoGenerico6"
		                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico6"
		                     ,'' as "NombreCampoGenerico7"
		                    , 0 as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'' as "NombreCampoGenerico9"
		                    ,'0001-01-01' as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT --54880
							INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
							  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE" --54443
		                      	AND TBTEDCTO."TIPO_CMBTE" IN ('OP','OI')
	                     	INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
	                     	INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
				            	ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
				           		AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||'' --53152
                            INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
	                     		ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
	                     		AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTECMBT."TIPO_CMBTE"
	                     	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     			ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     			AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")   
	                     	LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
								ON TESORERIA_OPTIMUS."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
								AND TESORERIA_OPTIMUS."TIPO_CMBTE" = TBTECMBT."TIPO_CMBTE"
	                     	WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE" IS NULL
	                     		AND TIPO_MONEDA_TESORERIA."TipoValor" IS NULL
	                     		AND TBTECMBT."FEC_ING" >= p_FechaInicial
			        	    	AND TBTECMBT."FEC_ING" <= p_FechaFinal;--OK 
                    
                     
                         
                     -- el concepto debe estar homologado
                     insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA OI/OP' as "Entidad"
		                    , 'el concepto tesoreria no esta homologado en optimus' as "Mensaje"
		                    , 'TipoValor' as "NombreCampoGenerico1"
		                    , 'ConceptoTesoreria' as "ValorCampoGenerico1"
		                    , '' as "NombreCampoGenerico2"
		                    , '' as "ValorCampoGenerico2"
		                    , 'CONCEPTO_MVTO' as "NombreCampoGenerico3"
		                    ,  TBTECMBT."CONCEPTO_MVTO" as "ValorCampoGenerico3"
		                    , 'TIPO_CMBTE' as "NombreCampoGenerico4"
		                    ,  TBTECMBT."TIPO_CMBTE" as "ValorCampoGenerico4"
		                    , '' as "NombreCampoGenerico5"
		                    , '' as "ValorCampoGenerico5"
		                    ,'FOLIO_CMBTE' as "NombreCampoGenerico6"
		                    , TBTECMBT."FOLIO_CMBTE" as "ValorCampoGenerico6"
		                     ,'' as "NombreCampoGenerico7"
		                    , 0 as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'' as "NombreCampoGenerico9"
		                    ,'0001-01-01' as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"                 
                     FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO
                     INNER JOIN "BSCL_VIRTUAL"."GES_TBTECMBT" as TBTECMBT 
                     	ON TBTECMBT."FOLIO_CMBTE" = TBMOMVTO."FOLIO_CMBTE_EGR"
					 INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBTECMBT."FOLIO_CMBTE"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOCUMENTO_IDENTIDAD
					  	ON DOCUMENTO_IDENTIDAD."DocumentoIdentidad" =  CASE WHEN p_RutCliente = '        0-0' THEN trim(TBTECMBT."RUT_DEST") ELSE TRIM(p_RutCliente) end
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::PER.Persona" AS PERSONA
					    ON PERSONA."IdDocumentoIdentidad" =  DOCUMENTO_IDENTIDAD."IdDocumentoIdentidad"
					 INNER JOIN "BSCL"."BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE
					    ON CLIENTE."IdPersona" =  PERSONA."IdPersona" 
					 INNER JOIN "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" AS PORTAFOLIO
					    ON PORTAFOLIO."IdCliente" =  CLIENTE."IdCliente"     	
					  	AND PORTAFOLIO."CodigoPortafolio" =  TBTECMBT."SEC_RUT_DEST"||''   	
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                     INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                     LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBTECMBT."CONCEPTO_MVTO"
                        AND CONCEPTO."ValorOrigen2" = TBTECMBT."TIPO_CMBTE" 	
                 	 LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" = TBMOMVTO."FOLIO_CMBTE_EGR"	
                      WHERE TESORERIA_OPTIMUS."FOLIO_CMBTE_EGR" IS NULL
                        AND CONCEPTO."TipoValor" IS NULL
                        AND TBTECMBT."FEC_ING" >= p_FechaInicial
		        	    AND TBTECMBT."FEC_ING" <= p_FechaFinal
		        	    AND TBMOMVTO."FOLIO_CMBTE_EGR" > 0;--OK

                    
		   	    -- NOS VAMOS POR EL LADO DE LAS MOVIMIENTOS DE TESORERIA POR TBCTMVTO
                -- ABONOS

              	    INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
				(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
					,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
					,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
					,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
					,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
					,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
					,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
					,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
					,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
					,"FOLIO_CMBTE_ING","FOLIO_CMBTE_EGR","SEC_MOVTO_CARGO","SEC_MOVTO_ABONO","TIPO_CMBTE"
				)
                    select 
							"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso" 
							,p_IdEmpresa as "IdEmpresa" 
							,p_Modulo as "IdModulo"  
							,p_IdUsuario as "IdUsuario" 
							,TIPO_COMP_TESORERIA."ValorDestino"  --MOVING MOVEGR esos pueden ser 
							,'' as "IdConcepto" -- 
							,0  as "IdCaja" 
							,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
							,TBTEDCTO."MONTO_DOCUM" as "Monto" 
							,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
							,TBTEDCTO."FEC_DOCUM" as "FechaIngresoRegistro" 
							,TBTEDCTO."FEC_DOCUM" as "FechaMovimiento" 
							,'' as "IdTipoPersona"
							, CLIENTE."IdPersona" as "IdPersona" 
							, CLIENTE."NombreCompleto" as "NombrePersona" 
							, 'R' as "IdTipoDocumentoIdentidad" 
							, CLIENTE."DocumentoIdentidad" as "DocumentoIdentidad" 
							, '' as "IdTipoPersonaTitularPago" 
							, CLIENTE."IdPersona" as "IdPersonaTitularPago" 
							, CLIENTE."NombreCompleto"  as "NombrePersonaTitularPago" 
							, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
							, '' as "SecuenciaAgrupacion" 
							, '' as "Glosa" 
							, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
							, 0  as "IdSucursalEmpleadoDigitador" 
							, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
							, 0  as "IdComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
							,'' as "IdTipoProducto" --  vienen de facturacion
							,'' as "IdTipoOperacion" --  vienen de facturacion
							, 0 as "IdOperacion"-- que es esto  
							, '0001-01-01' as "FechaOperacion" 
							,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
							,'' as "IdBolsa" --  vienen de facturacion 
							,'' as "IdMonedaOperacion" --  vienen de facturacion
							,TBTEDCTO."MONTO_DOCUM" as "MontoOperacion" 
							,'0001-01-01' as "FechaParidad" 
							, '' as "IdTipoReajuste" --  vienen de facturacion 
							, 0 as "ValorParidad" -- 
							, 0 as "FolioCartera" 
							, 0 as "IdInstrumento" 
							, '' as "IdTipoInstrumento" 
							, '' as "IdClasificacionTipoInstrumento" 
							, '' as "EsBilateral" 
							, 'S' as "TitularPago" 
							, 'S' as "EsIngresoAutomatico" 
							, 'N' as "AgrupaComprobante" 
							,  0 as "IdUsuarioBloqueo" 
							,'0001-01-01' as "FechaHoraBloqueo" 
							,p_IdCarga as "IdCarga" 
							,CASE WHEN TBTEDCTO."EST_CMBTE" ='I' then 'ING'
                      					else CASE WHEN TBTEDCTO."EST_CMBTE" ='A' then 'ANU'
                           					 else 'EMI'
                          					 end
                     		            end as "IdEstado" 
							,'N' as "MueveCuentaInversion"-- revisar 
							, 0 as "SecuenciaRelacionada" 
							,'N' as "NeteaComprobante" 
							, 0 as "IdUsuarioModificacion" 
							,'0001-01-01' as "FechaHoraModificacion" 
							,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO" 
							,TBCTMVTO."FEC_MVTO" as  "FEC_MOVTO"
							,TBCTMVTO."FOLIO_COMP_ADJ" as "FOLIO_COMP_ADJ"
							,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
							,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
							,0 as "FOLIO_CMBTE"
							,'0001-01-01' as "FEC_CMBTE"
							,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM"
							,0 as"FOLIO_CMBTE_ING"
							,0 as "FOLIO_CMBTE_EGR"
							,0 as "SEC_MOVTO_CARGO"
							,TBMOMVTO."SEC_MOVTO_ABONO" as "SEC_MOVTO_ABONO"
							,'' as "TIPO_CMBTE"
				    from "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO."SEC_RUT_CLI"||'' --53152
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBCTMVTO."CONCEPTO_MVTO"
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
                     WHERE  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL
                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal;--OK
       
				     --- consultas de log cuarta consulta
                     -- EL ABONO DEBE ESTAR CREADO EN TABLA DE MOVIMIENTO 	
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA MOV Abono' as "Entidad"
		                    , 'el abono no se encuentra creado en TBCTMVTO' as "Mensaje"
		                    , '' as "NombreCampoGenerico1"
		                    , '' as "ValorCampoGenerico1"
		                    , '' as "NombreCampoGenerico2"
		                    , '' as "ValorCampoGenerico2"
		                    , '' as "NombreCampoGenerico3"
		                    , '' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , '' as "NombreCampoGenerico5"
		                    , '' as "ValorCampoGenerico5"
		                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
		                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
		                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
		                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
		                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
		                      LEFT JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
		                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
		                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
		                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      			ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
		                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL
		                        AND TBCTMVTO."FEC_MVTO" IS NULL
		                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
		                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;
                                
                           -- EL DOCUMENTO DEBE EXISTIR
							 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'no se encuentra creado dcto asociado a mov abono TBTEDCTO' as "Mensaje"
				                    , '' as "NombreCampoGenerico1"
				                    , '' as "ValorCampoGenerico1"
				                    , '' as "NombreCampoGenerico2"
				                    , '' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
				                      LEFT JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON  TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                     WHERE  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL
				                        AND TBTEDCTO."FOLIO_CMBTE" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
										AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;

		                      -- DEBE EXISTIR EL CLIENTE
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'no existe el cliente en optimus' as "Mensaje"
				                    , 'RUT_CLI' as "NombreCampoGenerico1"
				                    , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				                    , '' as "NombreCampoGenerico2"
				                    , '' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                      WHERE CLIENTE."DocumentoIdentidad" IS NULL
				                        AND TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;

							 -- DEBE EXISTIR EL PORTAFOLIO
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'no existe el portafolio en optimus' as "Mensaje"
				                    , 'RUT_CLI' as "NombreCampoGenerico1"
				                    , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2"
				                    , TO_CHAR(TBCTMVTO."SEC_RUT_CLI") as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON   TBCTMVTO."FEC_MVTO" = TBMOMVTO."FEC_MVTO"
				                        AND  TBCTMVTO."SEC_MOVTO" = TBMOMVTO."SEC_MOVTO_ABONO" 
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                      WHERE PORTAFOLIO."IdCliente" IS NULL
				                        AND TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;

 							  -- el tipo comprobante debe existir
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'el tipo comprobante del doc no esta homologado' as "Mensaje"
				                    , 'TIPO_CMBTE' as "NombreCampoGenerico1"
				                    , TBTEDCTO."TIPO_CMBTE" as "ValorCampoGenerico1"
				                    , 'TipoValor' as "NombreCampoGenerico2"
				                    , 'TipoComprobanteTesoreria' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                      WHERE TIPO_COMP_TESORERIA."TipoValor" IS NULL
				                        AND TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL 
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;
		                                
		             -- EL TIPO DE MONEDA TESORERIA debe estar homologar                    
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'la moneda no se encuentra homolagada' as "Mensaje"
				                    , 'TipoValor' as "NombreCampoGenerico1"
				                    , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
				                    , 'TIPO_MON' as "NombreCampoGenerico2"
				                    , TBTEDCTO."TIPO_MON"  as "ValorCampoGenerico2"
				                    , TRIM(TBTEDCTO."TIPO_MON") as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'FOLIO_CMBTE' as "NombreCampoGenerico8"
				                    ,TBTEDCTO."FOLIO_CMBTE" as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
				                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
				                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                      WHERE TIPO_COMP_TESORERIA."TipoValor" IS NULL
				                        AND TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL 
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;		           
		                                
                     -- conceptos tesoreria                    
					 		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV Abono' as "Entidad"
				                    , 'el concepto no se encuentra homologado' as "Mensaje"
				                    , 'TipoValor' as "NombreCampoGenerico1"
				                    , 'ConceptoTesoreria' as "ValorCampoGenerico1"
				                    , 'CONCEPTO_MVTO' as "NombreCampoGenerico2"
				                    , TBCTMVTO."CONCEPTO_MVTO" as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_ABONO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_ABONO" as "ValorCampoGenerico7"
				                    ,'FOLIO_CMBTE' as "NombreCampoGenerico8"
				                    ,TBTEDCTO."FOLIO_CMBTE" as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_ABONO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
				                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
				                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
				                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
				                        AND CONCEPTO."ValorOrigen" = TBCTMVTO."CONCEPTO_MVTO"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" = TBMOMVTO."SEC_MOVTO_ABONO"
				                      WHERE CONCEPTO."TipoValor" IS NULL
				                        AND TESORERIA_OPTIMUS."SEC_MOVTO_ABONO" IS NULL 
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_ABONO" > 0;		
                    
                   -- CARGOS GENERADOS 
				    INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
					(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
						,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
						,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
						,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
						,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
						,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
						,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
						,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
						,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
						,"FOLIO_CMBTE_ING","FOLIO_CMBTE_EGR","SEC_MOVTO_CARGO","SEC_MOVTO_ABONO","TIPO_CMBTE"
					)
                   select 
							 "BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso" 
							,p_IdEmpresa as "IdEmpresa" 
							,p_Modulo as "IdModulo"  
							,p_IdUsuario as "IdUsuario" 
							,TIPO_COMP_TESORERIA."ValorDestino" --MOVING MOVEGR esos pueden ser 
							,CONCEPTO."ValorDestino" as "IdConcepto" -- 
							,0  as "IdCaja" 
							,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
							,TBTEDCTO."MONTO_DOCUM" as "Monto" 
							,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
							,TBTEDCTO."FEC_DOCUM" as "FechaIngresoRegistro" 
							,TBTEDCTO."FEC_DOCUM" as "FechaMovimiento" 
							,'' as "IdTipoPersona"
							, CLIENTE."IdPersona" as "IdPersona" 
							, CLIENTE."NombreCompleto" as "NombrePersona" 
							, 'R' as "IdTipoDocumentoIdentidad" 
							, CLIENTE."DocumentoIdentidad" as "DocumentoIdentidad" 
							, '' as "IdTipoPersonaTitularPago" 
							, CLIENTE."IdPersona" as "IdPersonaTitularPago" 
							, CLIENTE."NombreCompleto"  as "NombrePersonaTitularPago" 
							, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
							, '' as "SecuenciaAgrupacion" 
							, '' as "Glosa" 
							, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
							, 0  as "IdSucursalEmpleadoDigitador" 
							, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
							, 0  as "IdComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
							,'' as "IdTipoProducto" --  vienen de facturacion
							,'' as "IdTipoOperacion" --  vienen de facturacion
							, 0 as "IdOperacion"-- que es esto  
							, '0001-01-01' as "FechaOperacion" 
							,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
							,'' as "IdBolsa" --  vienen de facturacion 
							,'' as "IdMonedaOperacion" --  vienen de facturacion
							,TBTEDCTO."MONTO_DOCUM" as "MontoOperacion" 
							,'0001-01-01' as "FechaParidad" 
							, '' as "IdTipoReajuste" --  vienen de facturacion 
							, 0 as "ValorParidad" -- 
							, 0 as "FolioCartera" 
							, 0 as "IdInstrumento" 
							, '' as "IdTipoInstrumento" 
							, '' as "IdClasificacionTipoInstrumento" 
							, '' as "EsBilateral" 
							, 'S' as "TitularPago" 
							, 'S' as "EsIngresoAutomatico" 
							, 'N' as "AgrupaComprobante" 
							,  0 as "IdUsuarioBloqueo" 
							,'0001-01-01' as "FechaHoraBloqueo" 
							,p_IdCarga as "IdCarga" 
							,TBTEDCTO."EST_CMBTE" as "IdEstado" 
							,'N' as "MueveCuentaInversion"-- revisar 
							, 0 as "SecuenciaRelacionada" 
							,'N' as "NeteaComprobante" 
							, 0 as "IdUsuarioModificacion" 
							,'0001-01-01' as "FechaHoraModificacion" 
							,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO" 
							,TBCTMVTO."FEC_MVTO" as  "FEC_MOVTO"
							,TBCTMVTO."FOLIO_COMP_ADJ" as "FOLIO_COMP_ADJ"
							,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
							,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
							,0  as "FOLIO_CMBTE"
							,'0001-01-01' as "FEC_CMBTE"
							,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM"
							,0 as  "FOLIO_CMBTE_ING"
							,0 as "FOLIO_CMBTE_EGR"
							,TBMOMVTO."SEC_MOVTO_CARGO" as "SEC_MOVTO_CARGO"
							,0 as "SEC_MOVTO_ABONO"
							,'' as "TIPO_CMBTE"
                      FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                        ON TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" --54443
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO."SEC_RUT_CLI"||'' --53152
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBCTMVTO."CONCEPTO_MVTO"	
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
                        AND TBTEDCTO."FEC_DOCUM" >= p_FechaInicial
		        	    AND TBTEDCTO."FEC_DOCUM" <= p_FechaFinal
		        	    AND TBMOMVTO."SEC_MOVTO_CARGO" > 0; 	 

                    -- quinta consulta 
                    -- EL cargo DEBE ESTAR CREADO EN TABLA DE MOVIMIENTO 	
					 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
		              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
						,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
						,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
						,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
		              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
		                    ,p_IdCarga as "IdCarga"
		                    , 'TESORERIA MOV cargo' as "Entidad"
		                    , 'el cargo no se encuentra creado en TBCTMVTO' as "Mensaje"
		                    , '' as "NombreCampoGenerico1"
		                    , '' as "ValorCampoGenerico1"
		                    , '' as "NombreCampoGenerico2"
		                    , '' as "ValorCampoGenerico2"
		                    , '' as "NombreCampoGenerico3"
		                    , '' as "ValorCampoGenerico3"
		                    , '' as "NombreCampoGenerico4"
		                    , '' as "ValorCampoGenerico4"
		                    , '' as "NombreCampoGenerico5"
		                    , '' as "ValorCampoGenerico5"
		                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
		                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
		                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
		                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
		                    ,'' as "NombreCampoGenerico8"
		                    , 0 as "ValorCampoGenerico8"
		                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
		                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
		                    ,'' as "NombreCampoGenerico10"
		                    ,'0001-01-01' as "ValorCampoGenerico10"
		                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
		                       LEFT JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
		                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
		                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
		                       LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      			ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
		                      WHERE TBCTMVTO."SEC_MOVTO" IS NULL
		                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
		                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
		                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;
                                
                           -- EL DOCUMENTO DEBE EXISTIR
							 insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'no se encuentra creado dcto asociado a mov cargo TBTEDCTO' as "Mensaje"
				                    , '' as "NombreCampoGenerico1"
				                    , '' as "ValorCampoGenerico1"
				                    , '' as "NombreCampoGenerico2"
				                    , '' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
				                      LEFT JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE TBTEDCTO."FOLIO_CMBTE" IS NULL
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;
		                                
		                      -- DEBE EXISTIR EL CLIENTE
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'no existe el cliente en optimus' as "Mensaje"
				                    , 'RUT_CLI' as "NombreCampoGenerico1"
				                    , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				                    , '' as "NombreCampoGenerico2"
				                    , '' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE CLIENTE."DocumentoIdentidad" IS NULL
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;

							 -- DEBE EXISTIR EL PORTAFOLIO
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'no existe el portafolio en optimus' as "Mensaje"
				                    , 'RUT_CLI' as "NombreCampoGenerico1"
				                    , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				                    , 'SEC_RUT_CLI' as "NombreCampoGenerico2"
				                    , TO_CHAR(TBCTMVTO."SEC_RUT_CLI") as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO"= TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE PORTAFOLIO."IdCliente" IS NULL 
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;


 							  -- el tipo comprobante debe existir
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'el tipo comprobante del doc no esta homologado' as "Mensaje"
				                    , 'TIPO_CMBTE' as "NombreCampoGenerico1"
				                    , TBTEDCTO."TIPO_CMBTE" as "ValorCampoGenerico1"
				                    , 'TipoValor' as "NombreCampoGenerico2"
				                    , 'TipoComprobanteTesoreria' as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                     LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE TIPO_COMP_TESORERIA."TipoValor" IS NULL
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;
		            
		            		--                    
		                     -- el tipo moneda debe existir
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'el tipo comprobante del doc no esta homologado' as "Mensaje"
				                    , 'TipoValor' as "NombreCampoGenerico1"
				                    , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
				                    , 'TIPO_MON' as "NombreCampoGenerico2"
				                    , TRIM(TBTEDCTO."TIPO_MON") as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
				                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
				                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")	
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE TIPO_MONEDA_TESORERIA."TipoValor" IS NULL
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;           
		                                
		                     -- el concepto debe existir
          					  insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
				              select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				                    ,  p_IdCarga as "IdCarga"
				                    , 'TESORERIA MOV cargo' as "Entidad"
				                    , 'el tipo comprobante del doc no esta homologado' as "Mensaje"
				                    , 'TipoValor' as "NombreCampoGenerico1"
				                    , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
				                    , 'TIPO_MON' as "NombreCampoGenerico2"
				                    , TRIM(TBTEDCTO."TIPO_MON") as "ValorCampoGenerico2"
				                    , '' as "NombreCampoGenerico3"
				                    , '' as "ValorCampoGenerico3"
				                    , '' as "NombreCampoGenerico4"
				                    , '' as "ValorCampoGenerico4"
				                    , '' as "NombreCampoGenerico5"
				                    , '' as "ValorCampoGenerico5"
				                    ,'SEC_MOVTO' as "NombreCampoGenerico6"
				                    , TBMOMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				                    ,'SEC_MOVTO_CARGO' as "NombreCampoGenerico7"
				                    , TBMOMVTO."SEC_MOVTO_CARGO" as "ValorCampoGenerico7"
				                    ,'' as "NombreCampoGenerico8"
				                    , 0 as "ValorCampoGenerico8"
				                    ,'FEC_MOVTO' as "NombreCampoGenerico9"
				                    ,TBMOMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				                    ,'' as "NombreCampoGenerico10"
				                    ,'0001-01-01' as "ValorCampoGenerico10"
				                    FROM "BSCL_VIRTUAL"."GES_TBMOMVTO" as TBMOMVTO 
				                      INNER JOIN "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
				                        ON  TBMOMVTO."FEC_MVTO" = TBCTMVTO."FEC_MVTO"
				                        AND TBMOMVTO."SEC_MOVTO_CARGO" = TBCTMVTO."SEC_MOVTO"
				                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
									  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO" 
				                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
				                      INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
					         			ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
				                      INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
							            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
							            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
									  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
				                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
				                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
				                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
				                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
				                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")	
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
				                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
				                        AND CONCEPTO."ValorOrigen" = TBCTMVTO."CONCEPTO_MVTO"		
				                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso" AS TESORERIA_OPTIMUS
                      					ON  TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" = TBMOMVTO."SEC_MOVTO_CARGO"
				                      WHERE CONCEPTO."TipoValor" IS NULL
				                     	AND TESORERIA_OPTIMUS."SEC_MOVTO_CARGO" IS NULL
				                     	AND TBMOMVTO."FEC_MVTO" >= p_FechaInicial
				                   	    AND TBMOMVTO."FEC_MVTO" <= p_FechaFinal
		                                AND TBMOMVTO."SEC_MOVTO_CARGO" > 0;   

                    -- MOVIMIENTOS DE CARGOS Y ABONOS DE TABLA CTMVTO QUE NO ESTAN EN LA MOMVTO
				    INSERT INTO "BSCL.Homologacion.BD::HOM.IngresoEgreso"
					(    "IdIngresoEgreso" ,"IdEmpresa" ,"IdModulo" ,"IdUsuario" ,"IdTipoMovimiento" ,"IdConcepto" ,"IdCaja" ,"IdMoneda" ,"Monto" ,"FechaLiquidacion" 
						,"FechaIngresoRegistro" ,"FechaMovimiento" ,"IdTipoPersona","IdPersona" ,"NombrePersona" ,"IdTipoDocumentoIdentidad" ,"DocumentoIdentidad" 
						,"IdTipoPersonaTitularPago" ,"IdPersonaTitularPago" ,"NombrePersonaTitularPago" ,"IdPortafolioTitularPago" ,"SecuenciaAgrupacion" ,"Glosa" 
						,"IdEmpleadoDigitador" ,"IdSucursalEmpleadoDigitador" ,"IdTipoComprobanteOrigen" ,"IdComprobanteOrigen" ,"FechaComprobanteOrigen" ,"FechaLiquidacionComprobanteOrigen" 
						,"IdTipoProducto" ,"IdTipoOperacion" ,"IdOperacion" ,"FechaOperacion" ,"IdCondicionLiquidacion" ,"IdBolsa" ,"IdMonedaOperacion" ,"MontoOperacion" 
						,"FechaParidad" ,"IdTipoReajuste" ,"ValorParidad" ,"FolioCartera" ,"IdInstrumento" ,"IdTipoInstrumento" ,"IdClasificacionTipoInstrumento" 
						,"EsBilateral" ,"TitularPago" ,"EsIngresoAutomatico" ,"AgrupaComprobante" ,"IdUsuarioBloqueo" ,"FechaHoraBloqueo" ,"IdCarga" ,"IdEstado" 
						,"MueveCuentaInversion" ,"SecuenciaRelacionada" ,"NeteaComprobante" ,"IdUsuarioModificacion" ,"FechaHoraModificacion"
						,"SEC_MOVTO","FEC_MOVTO","FOLIO_COMP_ADJ","TIPO_COMP_ADJ","SEC_DOCUM","FOLIO_CMBTE","FEC_CMBTE","TIPO_DOCUM"
						,"FOLIO_CMBTE_ING","FOLIO_CMBTE_EGR","SEC_MOVTO_CARGO","SEC_MOVTO_ABONO","TIPO_CMBTE"
					)

                    select 
							"BSCL.Tesoreria.BD.Secuencias::IngresoEgreso".NEXTVAL as "IdIngresoEgreso" 
							,p_IdEmpresa as "IdEmpresa" 
							,p_Modulo as "IdModulo"  
							,p_IdUsuario as "IdUsuario" 
							,TIPO_COMP_TESORERIA."ValorDestino" --MOVING MOVEGR esos pueden ser 
							,CONCEPTO."ValorDestino" as "IdConcepto" -- 
							,0  as "IdCaja" 
							,TIPO_MONEDA_TESORERIA."ValorDestino" as "IdMoneda" 
							,TBTEDCTO."MONTO_DOCUM" as "Monto" 
							,TBTEDCTO."FEC_LIQUID" as "FechaLiquidacion" 
							,TBTEDCTO."FEC_DOCUM" as "FechaIngresoRegistro" 
							,TBTEDCTO."FEC_DOCUM" as "FechaMovimiento" 
							,'' as "IdTipoPersona"
							, CLIENTE."IdPersona" as "IdPersona" 
							, CLIENTE."NombreCompleto" as "NombrePersona" 
							, 'R' as "IdTipoDocumentoIdentidad" 
							, CLIENTE."DocumentoIdentidad" as "DocumentoIdentidad" 
							, 'CNT' as "IdTipoPersonaTitularPago" 
							, CLIENTE."IdPersona" as "IdPersonaTitularPago" 
							, CLIENTE."NombreCompleto"  as "NombrePersonaTitularPago" 
							, PORTAFOLIO."IdPortafolio" as "IdPortafolioTitularPago" 
							, '' as "SecuenciaAgrupacion" 
							, '' as "Glosa" 
							, 0  as "IdEmpleadoDigitador" -- TBTECMBT."USUARIO_SOL"
							, 0  as "IdSucursalEmpleadoDigitador" 
							, '' as "IdTipoComprobanteOrigen"--  vienen de facturacion
							, 0  as "IdComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaComprobanteOrigen" --  vienen de facturacion
							,'0001-01-01' as "FechaLiquidacionComprobanteOrigen" --  vienen de facturacion
							,'' as "IdTipoProducto" --  vienen de facturacion
							,'' as "IdTipoOperacion" --  vienen de facturacion
							, 0 as "IdOperacion"-- que es esto  
							, '0001-01-01' as "FechaOperacion" 
							,'' as "IdCondicionLiquidacion" -- --  vienen de facturacion 
							,'' as "IdBolsa" --  vienen de facturacion 
							,'' as "IdMonedaOperacion" --  vienen de facturacion
							,TBTEDCTO."MONTO_DOCUM" as "MontoOperacion" 
							,'0001-01-01' as "FechaParidad" 
							, '' as "IdTipoReajuste" --  vienen de facturacion 
							, 0 as "ValorParidad" -- 
							, 0 as "FolioCartera" 
							, 0 as "IdInstrumento" 
							, '' as "IdTipoInstrumento" 
							, '' as "IdClasificacionTipoInstrumento" 
							, '' as "EsBilateral" 
							, 'S' as "TitularPago" 
							, 'S' as "EsIngresoAutomatico" 
							, 'N' as "AgrupaComprobante" 
							,  0 as "IdUsuarioBloqueo" 
							,'0001-01-01' as "FechaHoraBloqueo" 
							,p_IdCarga as "IdCarga" 
							,CASE WHEN TBTEDCTO."EST_CMBTE" ='I' then 'ING'
                      					else CASE WHEN TBTEDCTO."EST_CMBTE" ='A' then 'ANU'
                           					 else 'EMI'
                          					 end
                     		            end as "IdEstado" 
							,'N' as "MueveCuentaInversion"-- revisar 
							, 0 as "SecuenciaRelacionada" 
							,'N' as "NeteaComprobante" 
							, 0 as "IdUsuarioModificacion" 
							,'0001-01-01' as "FechaHoraModificacion" 
							,TBCTMVTO."SEC_MOVTO" as "SEC_MOVTO" 
							,TBCTMVTO."FEC_MVTO" as  "FEC_MOVTO"
							,TBCTMVTO."FOLIO_COMP_ADJ" as "FOLIO_COMP_ADJ"
							,TBCTMVTO."TIPO_COMP_ADJ" as "TIPO_COMP_ADJ"
							,TBTEDCTO."SEC_DOCUM" as "SEC_DOCUM"
							,0 as "FOLIO_CMBTE"
							,0 as "FEC_CMBTE"
							,TBTEDCTO."TIPO_DOCUM" as "TIPO_DOCUM" 
							,0 as "FOLIO_CMBTE_ING"
							,0 as "FOLIO_CMBTE_EGR"
							,0 as "SEC_MOVTO_CARGO"
							,0 as "SEC_MOVTO_ABONO"
							,'' as "TIPO_CMBTE"
					  FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO"
                        AND TBTEDCTO."FEC_CMBTE"  = TBCTMVTO."FEC_MVTO"
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
					  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" =  TBCTMVTO."SEC_RUT_CLI"||'' --53152
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON "TIPO_COMP_TESORERIA"."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND "TIPO_COMP_TESORERIA"."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")
                      INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS CONCEPTO
                        ON CONCEPTO."TipoValor" = 'ConceptoTesoreria' 
                        AND CONCEPTO."ValorOrigen" = TBCTMVTO."CONCEPTO_MVTO"  
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FEC_MOVTO"  = TBCTMVTO."FEC_MVTO"
                      	AND TESORERIA_OPTIMUS."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO" IS NULL
                      	AND TBTEDCTO."FEC_DOCUM" >= p_FechaInicial
		        	    AND TBTEDCTO."FEC_DOCUM" <= p_FechaFinal;
		             	     	 
                   -- el movimiento debe tener cliente
			 		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
					 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				               ,  p_IdCarga as "IdCarga"
				               , 'TESORERIA MOV MVTO' as "Entidad"
				               , 'el cliente no existe en optimus' as "Mensaje"
				               , 'RUT_CLI' as "NombreCampoGenerico1"
				               , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				               , '' as "NombreCampoGenerico2"
				               , '' as "ValorCampoGenerico2"
				               , '' as "NombreCampoGenerico3"
				               , '' as "ValorCampoGenerico3"
				               , '' as "NombreCampoGenerico4"
				               , '' as "ValorCampoGenerico4"
				               , '' as "NombreCampoGenerico5"
				               , '' as "ValorCampoGenerico5"
				               ,'SEC_MOVTO' as "NombreCampoGenerico6"
				               , TBCTMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				               ,'' as "NombreCampoGenerico7"
				               , 0 as "ValorCampoGenerico7"
				               ,'' as "NombreCampoGenerico8"
				               , 0 as "ValorCampoGenerico8"
				               ,'FEC_MOVTO' as "NombreCampoGenerico9"
				               ,TBCTMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				               ,'' as "NombreCampoGenerico10"
				               ,'0001-01-01' as "ValorCampoGenerico10"		 
			          FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO"
                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
					  LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FEC_MOVTO"  = TBCTMVTO."FEC_MVTO"
                      	AND TESORERIA_OPTIMUS."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO" IS NULL
                      	AND CLIENTE."DocumentoIdentidad" IS NULL
                      	AND TBCTMVTO."FEC_MVTO" >= p_FechaInicial
		        	    AND TBCTMVTO."FEC_MVTO" <= p_FechaFinal;
		        	    
                     -- el movimiento debe tener portafolio
			 		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
					 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				               ,  p_IdCarga as "IdCarga"
				               , 'TESORERIA MOV MVTO' as "Entidad"
				               , 'el portafolio no se encuentra en optimus' as "Mensaje"
				               , 'RUT_CLI' as "NombreCampoGenerico1"
				               , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				               , 'RUT_CLI' as "NombreCampoGenerico2"
				               , TBCTMVTO."SEC_RUT_CLI" as "ValorCampoGenerico2"
				               , '' as "NombreCampoGenerico3"
				               , '' as "ValorCampoGenerico3"
				               , '' as "NombreCampoGenerico4"
				               , '' as "ValorCampoGenerico4"
				               , '' as "NombreCampoGenerico5"
				               , '' as "ValorCampoGenerico5"
				               ,'SEC_MOVTO' as "NombreCampoGenerico6"
				               , TBCTMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				               ,'' as "NombreCampoGenerico7"
				               , 0 as "ValorCampoGenerico7"
				               ,'' as "NombreCampoGenerico8"
				               , 0 as "ValorCampoGenerico8"
				               ,'FEC_MOVTO' as "NombreCampoGenerico9"
				               ,TBCTMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				               ,'' as "NombreCampoGenerico10"
				               ,'0001-01-01' as "ValorCampoGenerico10"		 
			          FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO"
                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
					  LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FEC_MOVTO"  = TBCTMVTO."FEC_MVTO"
                      	AND TESORERIA_OPTIMUS."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO" IS NULL
                        AND PORTAFOLIO."IdCliente" IS NULL
                      	AND TBCTMVTO."FEC_MVTO" >= p_FechaInicial
		        	    AND TBCTMVTO."FEC_MVTO" <= p_FechaFinal;

 					-- el concepto comprobante debe ser homologado
			 		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
					 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				               ,  p_IdCarga as "IdCarga"
				               , 'TESORERIA MOV MVTO' as "Entidad"
				               , 'el tipo comprobante del doc no esta homologado' as "Mensaje"
				               , 'RUT_CLI' as "NombreCampoGenerico1"
				               , TBCTMVTO."RUT_CLI" as "ValorCampoGenerico1"
				               , '' as "NombreCampoGenerico2"
				               , '' as "ValorCampoGenerico2"
				               , '' as "NombreCampoGenerico3"
				               , '' as "ValorCampoGenerico3"
				               , '' as "NombreCampoGenerico4"
				               , '' as "ValorCampoGenerico4"
				               , '' as "NombreCampoGenerico5"
				               , '' as "ValorCampoGenerico5"
				               ,'SEC_MOVTO' as "NombreCampoGenerico6"
				               , TBCTMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				               ,'' as "NombreCampoGenerico7"
				               , 0 as "ValorCampoGenerico7"
				               ,'' as "NombreCampoGenerico8"
				               , 0 as "ValorCampoGenerico8"
				               ,'FEC_MOVTO' as "NombreCampoGenerico9"
				               ,TBCTMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				               ,'' as "NombreCampoGenerico10"
				               ,'0001-01-01' as "ValorCampoGenerico10"		 
			          FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO"
                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
					  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
					  LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FEC_MOVTO" = TBCTMVTO."FEC_MVTO"
                      	AND TESORERIA_OPTIMUS."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO" IS NULL
                        AND TIPO_COMP_TESORERIA."TipoValor" IS NULL
                      	AND TBCTMVTO."FEC_MVTO" >= p_FechaInicial
		        	    AND TBCTMVTO."FEC_MVTO" <= p_FechaFinal;
		        	    
		        
		      		-- la moneda debe estar homologada
			 		insert into "BSCL"."BSCL.Homologacion.BD::HOM.Error"
				              ("IdError","IdCarga","Entidad","Mensaje","NombreCampoGenerico1","ValorCampoGenerico1","NombreCampoGenerico2","ValorCampoGenerico2"
								,"NombreCampoGenerico3" ,"ValorCampoGenerico3","NombreCampoGenerico4" ,"ValorCampoGenerico4" ,"NombreCampoGenerico5" ,"ValorCampoGenerico5" 
								,"NombreCampoGenerico6" ,"ValorCampoGenerico6" ,"NombreCampoGenerico7" ,"ValorCampoGenerico7" ,"NombreCampoGenerico8" ,"ValorCampoGenerico8" 
								,"NombreCampoGenerico9" ,"ValorCampoGenerico9" ,"NombreCampoGenerico10","ValorCampoGenerico10")
					 select "BSCL.Homologacion.BD.Secuencias::Error".NEXTVAL as "IdError"
				               ,  p_IdCarga as "IdCarga"
				               , 'TESORERIA MOV MVTO' as "Entidad"
				               , 'la moneda debe estar homologada' as "Mensaje"
				               , 'TipoValor' as "NombreCampoGenerico1"
				               , 'TipoMonedaTesoreria' as "ValorCampoGenerico1"
				               , 'TIPO_MON'	as "NombreCampoGenerico2"
				               , TRIM(TBTEDCTO."TIPO_MON")	 as "ValorCampoGenerico2"
				               , '' as "NombreCampoGenerico3"
				               , '' as "ValorCampoGenerico3"
				               , '' as "NombreCampoGenerico4"
				               , '' as "ValorCampoGenerico4"
				               , '' as "NombreCampoGenerico5"
				               , '' as "ValorCampoGenerico5"
				               ,'SEC_MOVTO' as "NombreCampoGenerico6"
				               , TBCTMVTO."SEC_MOVTO" as "ValorCampoGenerico6"
				               ,'' as "NombreCampoGenerico7"
				               , 0 as "ValorCampoGenerico7"
				               ,'' as "NombreCampoGenerico8"
				               , 0 as "ValorCampoGenerico8"
				               ,'FEC_MOVTO' as "NombreCampoGenerico9"
				               ,TBCTMVTO."FEC_MVTO" as "ValorCampoGenerico9"
				               ,'' as "NombreCampoGenerico10"
				               ,'0001-01-01' as "ValorCampoGenerico10"		 
			          FROM "BSCL_VIRTUAL"."GES_TBCTMVTO" as TBCTMVTO 
                      INNER JOIN  "BSCL_VIRTUAL"."GES_TBTEDCTO" as TBTEDCTO
					  	ON TBTEDCTO."FOLIO_CMBTE" = TBCTMVTO."SEC_MOVTO"
                        AND TBTEDCTO."FEC_CMBTE" = TBCTMVTO."FEC_MVTO"
					  INNER JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as CLIENTE
			         	ON CLIENTE."DocumentoIdentidad" = CASE WHEN p_RutCliente = '        0-0' THEN trim(TBCTMVTO."RUT_CLI") ELSE TRIM(p_RutCliente) end
					  INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as PORTAFOLIO
			            ON PORTAFOLIO."IdCliente" = CLIENTE."IdCliente"
			            AND PORTAFOLIO."CodigoPortafolio" = TBCTMVTO."SEC_RUT_CLI"||'' --53152
					  INNER JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_COMP_TESORERIA
                     	ON TIPO_COMP_TESORERIA."TipoValor" = 'TipoComprobanteTesoreria'
                     	AND TIPO_COMP_TESORERIA."ValorOrigen" = TBTEDCTO."TIPO_CMBTE"
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS TIPO_MONEDA_TESORERIA
                     	ON TIPO_MONEDA_TESORERIA."TipoValor" = 'TipoMonedaTesoreria'
                     	AND TIPO_MONEDA_TESORERIA."ValorOrigen" = TRIM(TBTEDCTO."TIPO_MON")	
                      LEFT JOIN "BSCL.Homologacion.BD::HOM.IngresoEgreso"  AS TESORERIA_OPTIMUS
                      	ON  TESORERIA_OPTIMUS."FEC_MOVTO" = TBCTMVTO."FEC_MVTO"
                      	AND TESORERIA_OPTIMUS."SEC_MOVTO" = TBCTMVTO."SEC_MOVTO"
                      WHERE TESORERIA_OPTIMUS."SEC_MOVTO" IS NULL
                        AND TIPO_MONEDA_TESORERIA."TipoValor" IS NULL
                      	AND TBCTMVTO."FEC_MVTO" >= p_FechaInicial
		        	    AND TBCTMVTO."FEC_MVTO" <= p_FechaFinal;  
		        
             -- REVISAMOS CUANTAS OPERACIONES SERAN CARGADAS
                  SELECT COUNT(*) INTO v_Total_Operaciones_Test
                  FROM "BSCL_VIRTUAL"."GES_TBTEDCTO" as DOCUMENTO	
                  WHERE DOCUMENTO."FEC_DOCUM" >= p_FechaInicial
		        	AND DOCUMENTO."FEC_DOCUM" <= p_FechaFinal;

			-- REVISAMOS CUANTOS REGISTROS FUERON CARGADOS
                  SELECT COUNT(*) INTO v_Total_Test_Cargados FROM 
                 "BSCL.Homologacion.BD::HOM.IngresoEgreso" 
                  WHERE "IdCarga" = p_IdCarga;
 
                  
             -- SACAMOS LOS REGISTROS PROCESADOS E INGRESADOS EN LA ASIGNACION
	   			  p_ResumenDatosProcesados =  select 'CARGAOPERACIONTEST' as "CodigoProceso" 
	   			  									,'TES' as "Modulo" 
	   			 									,v_Total_Operaciones_Test as "TotalRegistrosSebra"
	   			  									,v_Total_Test_Cargados as "TotalRegistrosSap"  FROM DUMMY
       							    		   union all
       							    		   select * from :p_ResumenDatosProcesados;

                  IF v_Total_Operaciones_Test <> v_Total_Test_Cargados THEN 
				     p_out_ErroresDetalle =  select 'ORD_CARGA_ERR_TESORERIA' as "Codigo",'falla en la carga tesoreria' as "Descripcion" from dummy UNION ALL
					    	       			 select * from :p_out_ErroresDetalle;	
	   			  END IF;
		--------------------
		--FIN SP 
		--------------------
	END;	
END;

 
