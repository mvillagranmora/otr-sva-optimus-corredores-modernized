SET SCHEMA "BSCL"; 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ValidarEmprestitoPorAnular";
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::PRT.ValidarEmprestitoPorAnular" (


	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT 		default 0,
    IN p_IdUsuario          		BIGINT 		default 0,
    IN p_IdModulo           		NVARCHAR(3) default '',
   	IN p_UsaReglasNegocio 			NVARCHAR(1) default 'N',
    
	IN p_FolioCarteraEmprestito		BIGINT 		default 0,
	IN p_FechaAnulacion				DATE		default '0001-01-01',

	
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)) default empty,	
	OUT p_out_ErroresContador 		int,   
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000)) default empty
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
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
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
			IF :p_out_ErroresContador > 0 THEN
				select count(1) into p_out_ErroresContador from :v_ErroresReqBasicos; 
				p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresReqBasicos;
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
    		declare v_IdEstadoEmprestito, v_IdEstadoFactPremio  	NVARCHAR(3) := '';
    		declare v_FolioCarteraEmpre, v_FolioComprobanteFactPremio 	BIGINT := 0;
    		declare v_IdTipoComprobanteFactPremio 					NVARCHAR(6) := '';
    		declare v_fechaPrestamo, v_FechaVencimiento, v_FechaComprobanteFactPremio 	DATE := '0001-01-01';

			
			BEGIN
				DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
				BEGIN
				END;
				
				SELECT top 1 
					IFNULL(emp."FolioCartera",0),
					IFNULL(emp."FolioComprobante",0),
					IFNULL(emp."IdTipoComprobante",''),
					IFNULL(emp."FechaComprobante",'0001-01-01'),
					IFNULL(facPre."IdEstadoComprobante",''),
					IFNULL(emp."FechaVencimientoOperacion",  TO_DATE('0001-01-01'))
				INTO
					v_FolioCarteraEmpre, 
					v_FolioComprobanteFactPremio, 
					v_IdTipoComprobanteFactPremio,
					v_FechaComprobanteFactPremio,
					v_IdEstadoFactPremio, 
					v_FechaVencimiento
				FROM "BSCL.Custodia.BD::CUS.Cartera" emp
				LEFT JOIN "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" facPre ON (facPre."IdEmpresa" = p_IdEmpresa 
--							and facPre."FolioComprobante" = emp."FolioComprobante"
--							and facPre."IdTipoComprobante" = emp."IdTipoComprobante"
							and facPre."FolioCartera" = emp."FolioCartera"
							and facPre."IdConceptoMovimiento" IN ('ANTEMP','VENEMP')
							)
				WHERE 
					emp."IdEmpresa" = p_IdEmpresa 
					AND emp."IdClasificacionTipoInstrumento" = 'RV' 
					and emp."IdTipoInstrumento" = 'EMP' 
					and emp."IdEstado" = 'VIG' 
					and emp."FolioCartera" = p_FolioCarteraEmprestito; 
				
				
			IF v_FolioCarteraEmpre > 0 THEN
				-- el emprestito debe estar vigente
				IF :v_IdEstadoEmprestito = 'ANU' THEN
					p_out_ErroresContador := p_out_ErroresContador + 1;
				
					p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
											UNION ALL
											select 'EMPRESTITO_SE_ENCUENTRA_ANULADO' as "Codigo", 'Empréstito se encuentra anulado' as "Descripcion" from dummy;
				END IF;
				
				-- la fecha de anulación no puede ser mayor al vencimiento
				IF :p_FechaAnulacion > :v_FechaVencimiento THEN
					p_out_ErroresContador := p_out_ErroresContador + 1;
				
					p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
											UNION ALL
											select 'EMPRESTITO_VENCIDO' as "Codigo", 'Empréstito ya se encuentra vencido' as "Descripcion" from dummy;
				END IF;
				
				IF :v_FolioComprobanteFactPremio > 0 AND :v_IdEstadoFactPremio <> 'ANU' THEN
					p_out_ErroresContador := p_out_ErroresContador + 1;
				
					p_out_ErroresDetalle = select * from :p_out_ErroresDetalle
											UNION ALL
											select 'DEBE_ANULAR_FACTURA_POR_PREMIO' as "Codigo", concat('Debe anular factura generada por pago de premio del préstamo, Folio: ', cast(v_FolioComprobanteFactPremio as NVARCHAR(10)) ) as "Descripcion" from dummy; 
				END IF;

			
			
			ELSE -- el emprestito no existe
				p_out_ErroresDetalle =  select * from :p_out_ErroresDetalle
										UNION ALL
										select 'EMPRESTITO_NO_EXISTE' as "Codigo", 'Empréstito no existe' as "Descripcion" from dummy; 
											
				p_out_ErroresContador := p_out_ErroresContador + 1;

			END IF;
				
				
				
			END;
			

			

			
		END;
		---------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------

	END;
END; 
