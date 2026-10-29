SET SCHEMA "BSCL";  
GO 
DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.NOM::RecepcionarNominasTransferencias";
GO
CREATE PROCEDURE 
-- 
--  Proposito: Indicar ya sea que la nómina o el archivo de transferencia fue recepcionado exitosamente
--				por la entidad destino (Banco). Una vez recepcionado no se permitira modificar el detalle de la
--				nómina.
--  Autor: NRM - BCS
--  Fecha Creacion: 10/03/2016
--   
"BSCL.Tesoreria.BD.Servicios.NOM::RecepcionarNominasTransferencias" (
    -- Parametros de Validacion
    IN p_IdEmpresa 						BIGINT,  
    IN p_IdUsuario 						BIGINT, 
    IN p_IdModulo 						NVARCHAR(3),    
    IN p_UsaReglasNegocio 				NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.   
   
    -- Parametros de Entrada
	IN p_FechaMovimiento				NVARCHAR(10),
    IN p_RegistrosARecepcionar 			TABLE ("IdNominaTransferencia" BIGINT),
          
    -- Parametros de Salida
	OUT p_out_ResultadoProceso			TABLE ("Campo" VARCHAR(40),"Id" BIGINT, "Servicio" VARCHAR(250), "Resultado" VARCHAR(40)),

    --Salidas para mensajes de advertencias y errores
    OUT p_out_AdvertenciasContador 		INT,	  
    OUT p_out_AdvertenciasDetalle  		TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
  	OUT p_out_ErroresContador      		INT,
    OUT p_out_ErroresDetalle       		TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000))
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN

	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(520));
	DECLARE v_erroroutmsg  				NVARCHAR(520);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));	        	
	DECLARE v_RechazadosPorValidacion	TABLE ("IdNominaTransferencia" BIGINT);
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
	END;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador
		FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;	
	END;
	
	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2; 	 	
    p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
    
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;
	v_RechazadosPorValidacion		= SELECT 0 as "IdNominaTransferencia" FROM dummy where 1=2;
	
	--------------------
	--VALIDACIONES
	--------------------	   
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA;
		END IF;
		
		CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosRecepcionarNominaTransferencia"(:p_FechaMovimiento,:p_RegistrosARecepcionar,:v_RechazadosPorValidacion,:v_ErroresDetalle,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN				
			p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
			SIGNAL SALIDA_CONTROLADA; 	
		ELSEIF :v_resultadoRegla = 'ADVE' THEN
			--LLamada al servicio de autorizacion si se requiere
			p_out_AdvertenciasDetalle = SELECT "Codigo",'N' as "RequiereAutorizacion", "Descripcion" FROM :v_ErroresDetalle;
			SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
		END IF;
	END; 
	END IF;
    
    
    --------------------
	--LOGICA
	--------------------
	BEGIN
		DECLARE Quantity					BIGINT;
		DECLARE v_AdvertenciasDetalle		TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
		DECLARE v_ErroresDetalle			TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(520));
		DECLARE v_InformacionContador		INT;
		DECLARE v_AdvertenciasContador		INT;
		DECLARE v_ErroresContador			INT;
		
		DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	 	

		--Quitar registros rechazados por validacion
		v_RegistrosAProcesar =
	 	SELECT "IdNominaTransferencia"
	 	  FROM :p_RegistrosARecepcionar
	 	 WHERE "IdNominaTransferencia" NOT IN (SELECT "IdNominaTransferencia" FROM :v_RechazadosPorValidacion);	 
	 	
	 	
	 	
	 	--*****************************************************************
		--* Liquidar comprobantes                                         *
		--*****************************************************************	
	 	v_ComprobantesALiquidar =
		SELECT NT."IdComprobantePartidaEgreso" as "IdComprobante", NT."IdTipoMovimiento",'' AS "AfectaCuentaInversion"
		  FROM :v_RegistrosAProcesar as RP
		 INNER JOIN "BSCL.Tesoreria.BD.Vistas::ConsultarNominaTransferencia" as NT ON NT."IdNominaTransferencia" = RP."IdNominaTransferencia";
		
		v_TablaComprobanteFormaDePago = 
		SELECT CFP."IdComprobante", CFP."IdComprobanteFormaDePago"
		  FROM :v_RegistrosAProcesar as RP
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdNominaTransferencia" = RP."IdNominaTransferencia"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobante" = NT."IdComprobantePartidaEgreso";
		
		
		CALL "BSCL.Tesoreria.BD.Servicios::LiquidarComprobantes" (
			 p_IdEmpresa					=> p_IdEmpresa
			,p_IdUsuario					=> p_IdUsuario
			,p_IdModulo						=> p_IdModulo
			,p_UsaReglasNegocio				=> 'S'
			,p_FechaMovimiento				=> :p_FechaMovimiento
			,p_TablaComprobante				=> :v_ComprobantesALiquidar
			,p_TablaComprobanteFormaDePago	=> :v_TablaComprobanteFormaDePago
			,p_out_InformacionContador  	=> v_InformacionContador
			,p_out_AdvertenciasContador		=> v_AdvertenciasContador
			,p_out_ErroresContador			=> v_ErroresContador
		);
		
		p_out_AdvertenciasDetalle		= SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_AdvertenciasDetalle" UNION ALL
									      SELECT "Codigo", "RequiereAutorizacion", "Descripcion" FROM :p_out_AdvertenciasDetalle;
		p_out_ErroresDetalle 			= SELECT "Codigo", "Descripcion" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ErroresDetalle";
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;										      
		IF :v_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;
	 	
	 	
	 	--****************************************
		--* Recepcionar Nominas o Transferencias *
		--****************************************
		v_ComprobantesLiquidados =
		SELECT NT."IdNominaTransferencia"
		FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso"  as RP
		 INNER JOIN "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago" as CFP ON CFP."IdComprobante" = RP."Id"
		 INNER JOIN "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT ON NT."IdComprobantePartidaEgreso" = CFP."IdComprobante"
		 WHERE RP."Resultado" IN ('LIQ','LIP')
		   AND RP."Campo" = 'IdComprobante'
		   AND CFP."IdEstado" = 'LIQ';
 		

		UPDATE 	NT
		   SET 	NT."IdEstado" = 'RCP',
		   		NT."FechaRecepcion" = CAST(:p_FechaMovimiento AS DATE)
		  FROM  "BSCL.Tesoreria.BD::TES.NominaTransferencia" as NT
		 INNER  JOIN :v_ComprobantesLiquidados as RP ON RP."IdNominaTransferencia" = NT."IdNominaTransferencia";

	 	UPDATE 	NTD
		   SET 	NTD."IdEstado" = 'RCP'
		  FROM  "BSCL.Tesoreria.BD::TES.NominaTransferenciaDetalle" as NTD
		 INNER  JOIN :v_ComprobantesLiquidados as RP ON RP."IdNominaTransferencia" = NTD."IdNominaTransferencia";
		
		  							
		--********************************
		--* Resultado del proceso *
		--********************************
		p_out_ResultadoProceso =
		SELECT  'IdNominaTransferencia' AS "Campo",
				"IdNominaTransferencia" as "Id",
				'BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearRegistros' as "Servicio",
				'RCP' as "Resultado"
		  FROM :v_ComprobantesLiquidados
		 UNION ALL
		SELECT  'IdNominaTransferencia' AS "Campo",
				"IdNominaTransferencia" as "Id",
				'BSCL.Tesoreria.BD.Servicios::BloquearDesbloquearRegistros' as "Servicio",
				'RECH' as "Resultado"
		  FROM :v_RechazadosPorValidacion
		 UNION ALL		
		SELECT "Campo","Id","Servicio","Resultado" FROM "BSCL.Tesoreria.BD::GBL.LiquidarComprobantes_Salida_ResultadoProceso";
		
		
		--********************************
		--*             LOG              *
		--********************************
	END;
END;