SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalSuscripcion";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.CerrarEventoCapitalSuscripcion" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
	IN p_IdEventoCapital					BIGINT     		default 0,
	IN p_IdTipoEventoCapital				NVARCHAR(2) 	default '',

-- Parametros de Salida
	OUT p_out_InformacionContador  	INT,
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_ErroresContador       INT)
	 
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
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalEmisionLiberada_Salida_ErroresDetalle";
		select count(1) into p_out_AdvertenciasContador 
		from "BSCL.Custodia.BD::GBL.CerrarEventoCapitalEmisionLiberada_Salida_AdvertenciasDetalle";		 					 	
	END;

	DECLARE EXIT HANDLER FOR SQLEXCEPTION

	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP

			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			INSERT INTO "BSCL.Custodia.BD::GBL.CerrarEventoCapitalEmisionLiberada_Salida_ErroresDetalle"
				SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM DUMMY;				
				SELECT COUNT(1) INTO p_out_ErroresContador 
				FROM "BSCL.Custodia.BD::GBL.CerrarEventoCapitalEmisionLiberada_Salida_ErroresDetalle";									
		ELSE
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
		
	END;

	p_out_ErroresContador		:= 0;
--	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
--	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
	BEGIN
	
		-----------------------------------------------------
		--Declaraciones Variables
		-----------------------------------------------------
		DECLARE v_FechaSistema 				NVARCHAR(10);
		DECLARE v_errorcliente 				INT DEFAULT 0;   	
		DECLARE v_Estado 					NVARCHAR(3) DEFAULT 'CER';       
		DECLARE v_out_InformacionDetalle	TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));				
		DECLARE v_IdPaisSistema				BIGINT;
		DECLARE v_PrecioNemotecnico			DECIMAL(26,8);
		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)

		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------

		BEGIN SEQUENTIAL EXECUTION
			p_out_AdvertenciasContador	:= 0;
			p_out_ErroresContador		:= 0;
			
			UPDATE "BSCL.Custodia.BD::CUS.EventoCapital"
			SET "IdEstado" = v_Estado
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  AND "IdTipoEventoCapital" = 'SU';
	
			UPDATE "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
			SET "IdEstado" = 'CER'
			WHERE "IdEventoCapital" = p_IdEventoCapital
			  AND "IdTipoEventoCapital" = 'SU';
		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
GO
