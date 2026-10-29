SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::CRE.ModificarAperturaCierreModulo";
-- 
--  Proposito: Modificar apretura manual, cierre manual para cierre de modulos.
--  Autor: CESPINOSA - BCS
--  Fecha Creacion: 15/04/2016  
-- 
CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::CRE.ModificarAperturaCierreModulo" (

	-- Parametros de Validacion
	IN p_IdEmpresa                     			BIGINT,                                                                            
	IN  p_IdUsuario                     		BIGINT,                                                                            
	IN  p_IdModulo                      		NVARCHAR(3),                                                                       
	IN  p_simularOperacion              		NVARCHAR(1),	-- S/N se usa para simular operacion (algunos casos)                  
	IN  p_UsaReglasNegocio              		NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	
	-- Parámetros para lógica
	IN p_IdCREModuloEmpresa						BIGINT,
	IN p_TipoApertura							CHAR(1), /* [A]utomatico - [M]anual */
	IN p_TipoCierre								CHAR(1), /* [A]utomatico - [M]anual */
	IN p_LogDetalle								TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250)),
	
	-- Parametros de Salida
	OUT p_out_AdvertenciasContador      INT,
	OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador           INT,   
	OUT p_out_ErroresDetalle            TABLE("Codigo" NVARCHAR(100),"Descripcion" nvarchar(520),"CodigoDato1" NVARCHAR(100),"ValorDato1" NVARCHAR(520),"CodigoDato2" NVARCHAR(100),"ValorDato2" NVARCHAR(520)) DEFAULT EMPTY
	
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS

BEGIN
	
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				table ("Codigo" nvarchar(100),"Valor" nvarchar(5000));
	DECLARE v_erroroutmsg  				nvarchar(520);	        	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA_NO_DATA     
	BEGIN
		v_hash= SELECT '' as "Codigo",'' as "Valor" FROM DUMMY;	
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" (:v_ErrorNoData,:v_hash,:v_erroroutmsg);
		p_out_ErroresDetalle =  SELECT :v_ErrorNoData  as "Codigo",:v_erroroutmsg as "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" FROM dummy UNION ALL
								SELECT * FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
	END;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE, '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" from dummy;
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
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" from dummy where 1=2;
	
	BEGIN --LOGICA
		--------------------
		-- Declare Variables
		--------------------
		DECLARE v_Obligatorios					NVARCHAR(250);
		DECLARE v_resultadoRegla 				NVARCHAR(40);
		DECLARE v_FechaSistema                  NVARCHAR(10);
		DECLARE v_CountChequera                 SMALLINT := 0;
		
		-- Para el Diccionario
        DECLARE p_TablaCodigosMensajes			TABLE ("CodigoMensaje" NVARCHAR(40),"CodigoDato" NVARCHAR(100),"ValorDato" NVARCHAR(520));
    	DECLARE p_out_TablaMensajes				TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));    	
    	    	
	    DECLARE v_AdvertenciasContador 			INT;
	    DECLARE v_ErroresContador      			INT;
	    DECLARE v_AdvertenciasDetalle  			TABLE ("Codigo" NVARCHAR(100),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(5000));	  	
	    DECLARE v_ErroresDetalle       			TABLE ("Codigo" NVARCHAR(100),"Descripcion" NVARCHAR(5000));
    	
    	
    	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
    	
    	p_TablaCodigosMensajes = Select '' as "CodigoMensaje", '' as "CodigoDato", '' as "ValorDato" from dummy where 1=2;
		p_out_TablaMensajes = Select '' as "Codigo", '' as "Descripcion" from dummy where 1=2;
		
		--------------------------------------------
		-- VALIDACIONES BASICAS
		-------------------------------------------- 
		BEGIN
			-- Validación básica de Empresa, Usuario, Módulo
			CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_out_ErroresDetalle,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;
		END;
		
		-- Obtiene Fecha de Sistema
	   	Call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(:p_IdEmpresa, v_FechaSistema);
		
		--------------------------------------------   
		-- DICCIONARIO
		--------------------------------------------
        CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionario" (:p_TablaCodigosMensajes, :p_out_TablaMensajes);

		p_out_ErroresDetalle =  Select "Codigo", "Descripcion", '' as "CodigoDato1", '' AS "ValorDato1", '' as "CodigoDato2", '' AS "ValorDato2" 
		                        From :p_out_TablaMensajes Union All
								Select "Codigo", "Descripcion", "CodigoDato1", "ValorDato1", "CodigoDato2", "ValorDato2"
								From :p_out_ErroresDetalle;		


		--Si existen errores en las validaciones debe salir
		select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;					

		
		--Solo fue una simulacion del servicio.
		If p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;
		
		------------------------------------------------------
		-- MODIFICACION
		------------------------------------------------------	
		
		UPDATE "BSCL.Plataforma.BD::CRE.ModuloEmpresa"
		SET "TipoApertura" 	= :p_TipoApertura, 
			"TipoCierre"	= :p_TipoCierre
		WHERE "IdCREModuloEmpresa" = :p_IdCREModuloEmpresa;
		
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
					 p_IdTipoLog 		 => 'MODIFICACION' 
					,p_IdModulo 		 => 'PLA' 
					,p_IdUsuario 		 => :p_IdUsuario 
					,p_IdEmpresa 		 => :p_IdEmpresa
					,p_FechaSistemaLog 	 => :v_FechaSistema
					,p_IdUsuarioAtendido => :p_IdUsuario
					,p_IdTipoRegistro 	 => 'APERTURA-CIERRE-MODULO'   -- Select * From "BSCL.Plataforma.BD::PLA.TipoRegistro";
					,p_IdProceso 		 => :p_IdCREModuloEmpresa
					,p_NombreProceso 	 => UPPER('Modificación Apertura Cierre Módulo')
					,p_DetalleLog 		 => :p_LogDetalle
			);	

	END;
	
END;