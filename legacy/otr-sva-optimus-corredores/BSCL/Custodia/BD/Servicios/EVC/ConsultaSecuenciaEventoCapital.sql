SET SCHEMA "BSCL"; 
GO
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaSecuenciaEventoCapital";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.ConsultaSecuenciaEventoCapital" (

--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          				BIGINT 			default 0,
    IN p_IdUsuario          				BIGINT 			default 0,
    IN p_IdModulo           				NVARCHAR(3) 	default '',  
	
    IN p_IdTipoEventoCapital				NVARCHAR(2) 	DEFAULT '',
    IN p_NumeroDividendo					BIGINT 			DEFAULT 0,
-- Parametros de Salida
	OUT p_out_SecuenciaEventoCapital		BIGINT,
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  INT,
	OUT p_out_AdvertenciasDetalle   TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador       INT,
	OUT p_out_ErroresDetalle        TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
	 )   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN SEQUENTIAL EXECUTION
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

		DECLARE v_count_EventoCapital 			BIGINT;
		DECLARE v_count_EventoCapitalCliente 	BIGINT;
		--variables de proceso
/*		DECLARE v_EstadoCarteraVigente 	NVARCHAR(3);*/
		DECLARE v_FechaSistema 					NVARCHAR(10);
		DECLARE v_Secuencia						BIGINT := 0;
		
		p_out_TblId = SELECT 0 as "Ids" from dummy where 1=2;

		-- Campos segun linea de Negocio (no se obtienen desde la tabla movimiento en transito)
		p_TblErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		v_TablaErrores = SELECT '' as "Codigo", '' as "Descripcion" FROM DUMMY WHERE 1=2;
		   
	    CALL  "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (p_IdEmpresa,v_FechaSistema);
	             
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
 		

		END;
		-----------------------------------------------------
		--fin Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------

		-----------------------------------------------------
		--proceso de Negocio
		---------------------------------------------------
		BEGIN

			p_out_AdvertenciasContador		:= 0;
			p_out_ErroresContador			:= 0;
			p_out_AdvertenciasDetalle		= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
			p_out_ErroresDetalle			= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;


			SELECT "BSCL.Custodia.BD.Secuencias::EventoCapitalSecuencia".NEXTVAL into v_Secuencia FROM DUMMY;	
			p_out_SecuenciaEventoCapital = v_Secuencia;
			
			--------------------------------------------------------------------------------------------------------
			--- 
			--------------------------------------------------------------------------------------------------------
			
			if p_out_ErroresContador > 0 then 
				SIGNAL SALIDA_CONTROLADA;
			end if;
		END;
		-----------------------------------------------------
		--fin proceso de Negocio
		---------------------------------------------------
	END;
END;
GO