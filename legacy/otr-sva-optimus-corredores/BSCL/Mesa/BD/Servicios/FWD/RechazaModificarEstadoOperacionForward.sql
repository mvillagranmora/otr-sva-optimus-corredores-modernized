SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.RechazaModificarEstadoOperacionForward";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::FWD.RechazaModificarEstadoOperacionForward" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_IdGrupoAutorizacion						BIGINT,
	IN 	p_IdEvaluacionLimite					    TABLE ("IdEvaluacionLimite"	BIGINT),
	IN  p_IdConcepto	       						NVARCHAR(6),
	-- Parametros salida del servicio publico
	--OUT p_out_RefId 								BIGINT,
			
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  			nvarchar(5000);	        	
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;	
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------						 	
		--LOG DE AUDITORIA (inicio)
		DECLARE v_DetalleLog TABLE (  "SubRegistro" 	nvarchar(40)
									, "CampoRegistro" 	nvarchar(40)
									, "ValorAntiguo" 	nvarchar(250)
									, "ValorNuevo" nvarchar(250)
								    );
		
		DECLARE v_fechaHoraSistema NVARCHAR(40);
		DECLARE v_NombreProceso	NVARCHAR(100):='Rechazo movimiento Forward excedido por límites';
		--------------------
		--Validaciones
		--------------------
		--------------------
		--Reglas Negocio
		--------------------		
		--------------------
		--Lógica Negocio
		--------------------
		--ACTUALIZACION DE VALORES 
/*
		UPDATE FWD
		SET FWD."IdEstado" = 'RZD' 
		FROM "BSCL.Mesa.BD::MES.Operacion" AS FWD
		INNER JOIN :p_IdEvaluacionLimite
				ON FWD."IdOperacion" = "IdEvaluacionLimite"
			   AND FWD."IdEstado" = 'PEL';
*/
		v_TblNuevoEstado =
	        	SELECT
	        		 "IdEvaluacionLimite"	AS "IdOperacion"
	        		,'MEFW'			AS "IdTipoProducto"
					,'RZD' 			AS "IdNuevoEstado"
					,0				AS "IdReserva"
					,0				AS "FolioCarteraCustodia"
					,0				AS "MontoOperacionAnticipo"
					,0				AS "MontoOperacionInicialAnticipo"
					,0				AS "MontoOperacionFinalAnticipo"
					,0				AS "TasaAnticipo"
					,0				AS "MontoOperacionMonedaAnticipo"
					,0				AS "MontoOperacionInicialMonedaAnticipo"
					,0				AS "MontoOperacionFinalMonedaAnticipo"
					,''				AS "IdMonedaLiquidacionAnticipo"
					,''				AS "IdCondicionLiquidacionAnticipo"
					,'0001-01-01'	AS "FechaCondicionLiquidacionAnticipo"
					,''				AS "Glosa"
				FROM :p_IdEvaluacionLimite;
			
		INSERT INTO "BSCL.Mesa.BD::PAR.CambiarEstadoOperacion_IN"
		SELECT * FROM :v_TblNuevoEstado;
	   
		CALL "BSCL.Mesa.BD.Servicios::CambiarEstadoOperacion" (
		     p_IdEmpresa 		=> :p_IdEmpresa     /*IN p_IdEmpresa        BIGINT,*/
		    ,p_IdUsuario 		=> :p_IdUsuario     /*IN p_IdUsuario        BIGINT, */
		    ,p_IdModulo 		=> 'MFW'    	/*IN p_IdModulo			NVARCHAR(3),*/
		    ,p_simularOperacion => 'N'  			/*IN p_simularOperacion nvarchar(1),  — S/N se usa para simular operacion (algunos casos) */
		    ,p_UsaReglasNegocio => 'N'  			/*IN p_UsaReglasNegocio nvarchar(1),  — S/N se usa en caso de servicios anidados.*/
		    ,p_IdPais 			=> 0         		/*IN p_IdPais         	BIGINT,*/
			--,p_TblNuevoEstado 	=> :v_TblNuevoEstado
		    --,p_out_TblResultado => p_out_TblResultado
		);	

		
		-- LLamo a Rechazar Límite							   
		CALL  "BSCL.Plataforma.BD.Servicios::LIM.RechazaAutorizacionEvaluacionLimite"(
			  :p_IdEmpresa
			, :p_IdUsuario
			, :p_IdModulo
			,'N'
			, :p_IdGrupoAutorizacion 
			, :p_IdEvaluacionLimite
			, :p_IdConcepto
			, p_out_AdvertenciasContador
			, p_out_AdvertenciasDetalle
			, p_out_ErroresContador
			, p_out_ErroresDetalle
		);
		
		
		--DATOS CABECERA LOG
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaHoraSistema => v_fechaHoraSistema
		);
		
		--DATOS DETALLE LOG
		p_DetalleLog = 
		(Select 'LIMITE'	    AS "SubRegistro",
				'Estado'        AS "CampoRegistro",--MOD."IdLimite"			 		 as "CampoRegistro",
				FWD."IdEstado"  AS "ValorAntiguo",--MOD."IdEstadoAutorizacion"  	 as "ValorAntiguo",
				'RZD'			AS "ValorNuevo" --:p_IdEstadoAutorizacion 		 as "ValorNuevo" 
		FROM "BSCL.Mesa.BD::MES.Operacion" AS FWD
		INNER JOIN :p_IdEvaluacionLimite AS EVA 
				ON FWD."IdOperacion" = EVA."IdEvaluacionLimite");

		--GRABAR LOG
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
			 p_IdTipoLog		 	=> 'INFORMATIVO' 
			,p_IdModulo 			=> :p_IdModulo 
			,p_IdUsuario 			=> :p_IdUsuario 
			,p_IdEmpresa 			=> :p_IdEmpresa 
			,p_FechaSistemaLog 		=> TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD')
			,p_IdUsuarioAtendido 	=> :p_IdUsuario
			,p_IdTipoRegistro 		=> 'FORWARD' --v_TipoEntidad--'CLIENTE'
			,p_DetalleLog 			=> :p_DetalleLog	-- hacia arriba obligatorios
			,p_NombreProceso 		=> :v_NombreProceso-- hacia abajo opcionales
			,p_IdRegistro 			=> 455 --:v_IdEntidadOrigen
			,p_EstadoProceso 		=> 'RZD'
			);
		--LOG DE AUDITORIA (Fin)				
		
						
		--------------------
		--FIN SP 
		--------------------
	END;	
END;