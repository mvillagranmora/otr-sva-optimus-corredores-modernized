SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ValidarPrestamoPorAnular";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ValidarPrestamoPorAnular" (

/*****************************************/
-- 04-08-2016: CORREGIR SP, se han comentado querys para que compile, aún no se usa 
-- la funcionalidad de anular
/****************************************/


	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT 		default 0,
    IN p_IdUsuario          		BIGINT 		default 0,
    IN p_IdModulo           		NVARCHAR(3) default '',
   	IN p_UsaReglasNegocio 			NVARCHAR(1) default 'N',
    
	IN p_FolioCarteraPrestamo		BIGINT 		default 0,
	IN p_FechaAnulacion				DATE		default '0001-01-01',

	
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) default empty,	
	OUT p_out_ErroresContador 		int,   
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000)) default empty
 )   
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN 
	--------------------
	--DECLARE Manejo de Errores
	--------------------	

	DECLARE abort int default 0;	
	DECLARE SALIDA_CONTROLADA CONDITION; 
	DECLARE v_errorcuenta INT DEFAULT 0;  
	DECLARE v_errorhashmsg table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;
	END;
	
	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
	BEGIN
		p_out_AdvertenciasContador := 0;
		p_out_ErroresContador := 1;
		p_out_ErroresDetalle = SELECT 'PRESTAMO_NO_EXISTE' as "Codigo", 'Préstamo no existe' as "Descripcion" from dummy;
	END;



	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,SUBSTRING(::SQL_ERROR_MESSAGE, 1,520)  from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;


	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_MensajeRegla          NVARCHAR(520);
		DECLARE v_ResultadoRegla        NVARCHAR(40);
		DECLARE v_IdEjecucion 			BIGINT;
		DECLARE v_IdModuloCustodia      NVARCHAR(3);

	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			SELECT "IdModulo" INTO v_IdModuloCustodia FROM "BSCL.Plataforma.BD::PLA.Modulo"
		    WHERE "Nombre" = 'CUSTODIA';
		    --v_IdModuloCustodia := 'CUS';  --Modulo Custodia
			
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_ErroresReqBasicos,:v_resultadoRegla);
			
			select count(1) into p_out_ErroresContador from :v_ErroresReqBasicos; 
			IF :p_out_ErroresContador > 0 THEN
				p_out_ErroresDetalles = SELECT "Codigo","Descripcion" FROM :v_ErroresReqBasicos;
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------


		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN
    		declare v_IdEstadoPrestamo, v_IdEstadoFacVtaCorta, v_IdEstadoFacPri, v_IdEstadoFactPremio, v_IdEstadoMovto  				NVARCHAR(3) := '';
    		declare v_FolioComprobanteFacVtaCorta, v_IdComprobantePagoPrima, v_IdMovtoFacturarPagoPrima, v_FolioCarteraEmpre, v_IdComprobanteFactPremio	BIGINT := 0;
    		declare v_fechaPrestamo, v_FechaVencimiento 	DATE := '0001-01-01';
    		declare v_TieneEmprestitos SMALLINT := 0;
    		declare v_IdTipoPrestamo NVARCHAR(6) := '';
    		declare v_IdTipoComprobanteFacVtaCorta NVARCHAR(6) := '';
			
			BEGIN
				DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
				BEGIN
				END;
				
/****** revisar ******
			SELECT top 1 
					IFNULL(prt."IdTipoPrestamo",''), 
					IFNULL(prt."FolioComprobante",0), 
					IFNULL(prt."IdTipoComprobante",0), 
					IFNULL(prt."IdEstado",''), 
					IFNULL(prt."FechaOperacion",TO_DATE('0001-01-01')), 
					IFNULL(facPri."IdEstadoComprobante",''), 
					IFNULL(MovPri."IdMovimientoComprobante", 0),
					IFNULL(prt."FechaVencimientoOperacion", TO_DATE('0001-01-01')),
					IFNULL(MovPri."IdEstadoFacturacion", '')
				INTO
					v_IdTipoPrestamo, 
					v_FolioComprobanteFacVtaCorta,
					v_IdTipoComprobanteFacVtaCorta,
					v_IdEstadoPrestamo, 
					v_fechaPrestamo, 
					v_IdEstadoFacVtaCorta, 
					v_IdMovtoFacturarPagoPrima,
					v_FechaVencimiento,
					v_IdEstadoMovto

				FROM "BSCL.Custodia.BD::CUS.Cartera3" prt
---				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteSimple" facVC ON 
---									facVC."IdEmpresa" = p_IdEmpresa 
---									and facVC."FolioComprobante" = prt."FolioComprobante"
--									and facVC."IdTipoComprobante" = prt."IdTipoComprobante"
									
				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarComprobanteSimple" facPri ON 
					facPri."IdEmpresa" = p_IdEmpresa and 
					facPri."FolioComprobante" = prt."FolioComprobante" and
					facPri."IdTipoComprobante" = prt."IdTipoComprobante" 

					
				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobanteSimple" MovPri ON 
					MovPri."IdEmpresa" = p_IdEmpresa and
					MovPri."FolioCartera" = prt."FolioCartera" and 
					MovPri."IdConceptoMovimiento" = 'INGPRT' and 
					MovPri."IdEstadoFacturacion" = 'ING'
					
				WHERE 
					prt."IdEmpresa" = p_IdEmpresa AND
					prt."IdClasificacionTipoInstrumento" = 'RV' and 
					prt."IdTipoInstrumento" = 'PRT' and 
					prt."IdEstado" = 'VIG' and 
					prt."FolioCartera" = p_FolioCarteraPrestamo;
*/

			END;
			

			
			IF v_IdEstadoPrestamo <> '' THEN
				-- el prestamo debe estar vigente
				IF v_IdEstadoPrestamo = 'VCD' OR v_IdEstadoPrestamo = 'ANU' THEN
					p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
											UNION ALL
											select 'PRESTAMO_NO_VIGENTE' as "Codigo", 'Préstamo no se encuentra vigente' as "Descripcion" from dummy;
											
					p_out_ErroresContador := p_out_ErroresContador + 1;
				END IF;
				
				IF p_FechaAnulacion > v_FechaVencimiento THEN
					p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
											UNION ALL
											select 'PRESTAMO_VENCIDO' as "Codigo", 'Préstamo ya se encuentra vencido' as "Descripcion" from dummy;
											
					p_out_ErroresContador := p_out_ErroresContador + 1;
				END IF;
				
				-- si la factura de la Prima no se encuentra anulada
				IF v_IdComprobantePagoPrima > 0 AND v_IdEstadoFacPri <> 'ANU' THEN
					p_out_ErroresContador := p_out_ErroresContador + 1;
					
					p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
											UNION ALL
											select 'DEBE_ANULAR_FACTURA_POR_PRIMA' as "Codigo", concat('Debe anular factura generada por pago de prima del préstamo, IdComprobante:' , cast(v_IdComprobantePagoPrima as NVARCHAR(10))) as "Descripcion" from dummy ; 
				END IF;
	
								
				--validaciones x venta corta
				IF v_IdTipoPrestamo = 'PRTVTC' THEN
				
					-- solo anula prestamo del día
					IF v_fechaPrestamo <> p_FechaAnulacion THEN
						p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
												UNION ALL
												select 'PRT_FEC_ANULACION_DISTINTA_FEC_CREACION' as "Codigo", 'Sólo puede anular el préstamo el día de su creación' as "Descripcion" from dummy ; 
										
						p_out_ErroresContador := p_out_ErroresContador + 1;
					END IF;
					
					-- si la factura de la venta corta no se encuentra anulada
					IF v_FolioComprobanteFacVtaCorta > 0 AND v_IdEstadoFacVtaCorta <> 'ANU' THEN
						p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
												UNION ALL
												select 'DEBE_ANULAR_FACTURA_DE_VENTA_CORTA' as "Codigo", concat('Debe anular factura de venta corta, IdComprobante:', cast(v_FolioComprobanteFacVtaCorta as NVARCHAR(10))) as "Descripcion" from dummy; 
						p_out_ErroresContador := p_out_ErroresContador + 1;
					END IF;
					
					
					-- si hay un movimiento x facturar pendiente x el pago de la prima
					IF v_IdMovtoFacturarPagoPrima > 0 and v_IdEstadoMovto = 'FAC' THEN
						p_out_ErroresContador := p_out_ErroresContador + 1;
						
						p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
												UNION ALL
												select 'DEBE_ANULAR_MOVIMIENTO_POR_PREMIO_PRIMA' as "Codigo", 'Debe anular movimiento a facturar por pago de prima del préstamo' as "Descripcion" from dummy; 
					END IF;
					
				END IF;
				
				
				
				BEGIN
					DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
					BEGIN
					END;
/**** revisar					
					SELECT top 1 IFNULL(emp."FolioCartera",0),  IFNULL(emp."IdComprobanteFactReferencia",0), IFNULL(facPre."IdEstadoComprobante",'')
					INTO        v_FolioCarteraEmpre          ,  v_IdComprobanteFactPremio             , v_IdEstadoFactPremio
					FROM "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestito" emp
					LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" facPre ON 
								facPre."IdEmpresa" = p_IdEmpresa 
								and facPre."IdComprobante" = emp."IdComprobanteFactReferencia"
								and facPre."IdConceptoMovimiento" = 'FAOPRE'
					
					WHERE 
						emp."FolioCarteraPrestamo" = p_FolioCarteraPrestamo AND 
						emp."IdEstado" = 'VIG'
					ORDER BY emp."IdComprobanteFactReferencia" DESC;
*/
				END;
					
				IF v_FolioCarteraEmpre > 0 THEN
					p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;
					p_out_AdvertenciasDetalle = select * from :p_out_AdvertenciasDetalle 
												UNION ALL
												select 'PRESTAMO_TIENE_EMPRESTITOS_ASIGNADOS' as "Codigo", 'N' as "RequiereAutorizacion", 'Préstamo tiene empréstitos asigandos' as "Descripcion" from dummy; 
					
					
					IF v_IdComprobanteFactPremio > 0 AND v_IdEstadoFactPremio <> 'ANU' THEN
						p_out_ErroresContador := p_out_ErroresContador + 1;
					
						p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
												UNION ALL
												select 'DEBE_ANULAR_FACTURA_POR_PREMIO' as "Codigo", 'Debe anular factura generada por pago de premio del préstamo' as "Descripcion" from dummy; 
					END IF;
				END IF;

			
			
			ELSE -- el prestamo no existe
				p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
										UNION ALL
										select 'PRESTAMO_NO_EXISTE' as "Codigo", 'Préstamo no existe' as "Descripcion" from dummy; 
											
				p_out_ErroresContador := p_out_ErroresContador + 1;

			END IF;
			
		END;
		---------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
