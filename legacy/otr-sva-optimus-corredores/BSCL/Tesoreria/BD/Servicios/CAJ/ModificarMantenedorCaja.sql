SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::ModificarMantenedorCaja";

CREATE PROCEDURE "BSCL.Tesoreria.BD.Servicios.CAJ::ModificarMantenedorCaja" (
-- 
--  Proposito: Caja a ProductoEmpresa.
--  Autor: - BCS
--  Fecha Creacion: 01/02/2016
--  

-- Parámetros entrada comunes en todos los servicios públicos
	IN  p_IdEmpresa				BIGINT,						--ID Empresa destino
	IN  p_IdUsuario				BIGINT,						--ID del usuario que solicita el servicio
	IN  p_IdModulo				NVARCHAR(3),  				--ID del Modulo que lo invoca
	IN  p_SimularOperacion		NVARCHAR(1) DEFAULT 'N',	-- S/N se usa para simular operacion (algunos cASos)
	IN  p_UsaReglasNegocio	 	NVARCHAR(1) DEFAULT 'N',	-- S/N se usa en cASo de servicios anidados.	
		  
-- Parámetros entrada del servicio público
	IN p_Mantenedor 			NVARCHAR(11),
	IN p_Dato 					TABLE ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520), "ValorAnterior" NVARCHAR(520)),
	IN p_FechaModificacion		NVARCHAR(10),
    
-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	INT,
	OUT p_out_AdvertenciasDetalle 	TABLE ("Codigo" NVARCHAR(40),"RequiereAutorizacion" NVARCHAR(40),"Descripcion" NVARCHAR(520)),	
	OUT p_out_ErroresContador 		INT,
	OUT p_out_ErroresDetalle 		TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520))
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS
BEGIN
	---------------------------
	--DECLARE Manejo de Errores
	---------------------------

	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;	
	DECLARE v_Secuencia 	 			BIGINT;	  
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_MensajeRegla	 			NVARCHAR(520);		
	DECLARE v_erroroutmsg  				NVARCHAR(5000);	
	DECLARE v_ErrorNoData  				NVARCHAR(100);
		
	DECLARE v_IdCaja					BIGINT;
	DECLARE v_IdCajaMoneda              BIGINT;
	DECLARE v_IdMoneda					NVARCHAR(3);
	

	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;		 					 	
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
		DECLARE v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			--error pseudo-controlado pero desde un Sub-SP
			CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
		
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= SELECT '' AS "Codigo",'' AS "RequiereAutorizacion",'' AS "Descripcion" FROM dummy WHERE 1=2;
	p_out_ErroresDetalle		= SELECT '' AS "Codigo",'' AS "Descripcion" FROM dummy WHERE 1=2;
	
	
	
	--------------------
	--VALIDACIONES
	--------------------	
	IF p_UsaReglasNegocio = 'S' THEN
	BEGIN
		v_MensajeRegla := '';
		---Validaciones datos de entrada			
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,v_Errores,:v_resultadoRegla);
		IF :v_resultadoRegla = 'RECH' THEN
			 SIGNAL SALIDA_CONTROLADA; 	
		END IF;

		IF (UPPER(:p_Mantenedor) = 'CAJAMONEDA') THEN
			SELECT "Valor" INTO v_IdMoneda 		FROM :p_Dato WHERE "Campo" = 'IdMoneda';			
			SELECT "Valor" INTO v_IdCaja 		FROM :p_Dato WHERE "Campo" = 'IdCaja';
			SELECT "Valor" INTO v_IdCajaMoneda 	FROM :p_Dato WHERE "Campo" = 'IdCajaMoneda';

			CALL "BSCL.Tesoreria.BD.Reglas::ServicioCumpleRequisitosCrearCajaMoneda"(:p_SimularOperacion,:v_IdCajaMoneda,:v_IdCaja, :v_IdMoneda,:v_CajasRechazadas,:v_ErroresDetalle,:v_resultadoRegla);
			IF :v_resultadoRegla = 'RECH' THEN				
				p_out_ErroresDetalle = SELECT "Codigo","Descripcion" FROM :v_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			END IF;
		END IF;

	END;
	END IF;
	
		
	BEGIN	
	
	---------------------------
	-- Validaciones Básicas
	---------------------------


		--Declare v_Dato table ("NumFila" BIGINT, "Campo" NVARCHAR(40), "Valor" NVARCHAR(520), "ValorAnterior" NVARCHAR(520));
		--- Empresa viene de los datos de entrada.
		--Caja
		DECLARE IdCaja			BIGINT; -- Campo Comun para mantenedor.
		DECLARE CodigoCaja 		NVARCHAR(40);
		DECLARE NombreCaja 		NVARCHAR(100);
		DECLARE Estado	 		NVARCHAR(3); -- Campo comun para mantenedor
		
		--Moneda
		DECLARE IdCajaMoneda	BIGINT;
		DECLARE IdMoneda		NVARCHAR(3);
		DECLARE Saldo			DECIMAL(26,8);
		DECLARE IdTipoMovimiento NVARCHAR(6);
		
		--Usuario
		DECLARE IdCajaUsuario	BIGINT;
		DECLARE IdUsuario		BIGINT;
		DECLARE Sobregiro		NVARCHAR(1);
		DECLARE FechaInicio 	NVARCHAR(10);
		DECLARE FechaTermino	NVARCHAR(10);
		
		-- ProductoEmpresa
		DECLARE IdCajaProductoEmpresa 	BIGINT;
		DECLARE IdProductoEmpresa		BIGINT;
		
		v_Dato = Select '' as "SubRegistro", "Campo", "ValorAnterior", "Valor"
            		from :p_Dato;
            		
		-------Mantenedor Caja
		IF UPPER(:p_Mantenedor) = 'CAJA' Then 
			
			SELECT "Valor" INTO IdCaja 		FROM :v_Dato WHERE "Campo" = 'IdCaja';
			
			UPDATE "BSCL.Tesoreria.BD::TES.Caja"
			SET "Codigo" = 		COALESCE((SELECT "Valor" 	FROM :v_Dato WHERE "Campo" = 'Codigo'), 	"Codigo"),
				"Nombre" = 		COALESCE((SELECT "Valor" 	FROM :v_Dato WHERE "Campo" = 'Nombre'), 	"Nombre"),
				"IdEstado" = 	COALESCE((SELECT "Valor" 	FROM :v_Dato WHERE "Campo" = 'IdEstado'), 	"IdEstado")
			WHERE "IdCaja" = IdCaja AND "IdEmpresa" = p_IdEmpresa;
			
			v_DatoLog = Select 'CAJA' AS "SubRegistro", 
							"Campo" AS "CampoRegistro" , 
							"ValorAnterior" AS "ValorAntiguo", 
							"Valor" AS "ValorNuevo" 
							From :v_Dato Where "Valor" <> "ValorAnterior";
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'TES' 
				,p_IdUsuario => :p_IdUsuario 
				,p_IdEmpresa => :p_IdEmpresa
				,p_FechaSistemaLog => :p_FechaModificacion
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro => 'CAJA'
				,p_IdProceso => IdCaja
				--,p_EstadoProceso => ''
				,p_NombreProceso => 'Modificacion datos de Caja'
				,p_DetalleLog => :v_DatoLog
				);
		
		-------Mantenedor Caja - Moneda
		ELSEIF UPPER(:p_Mantenedor) = 'CAJAMONEDA' THEN
			
			SELECT "Valor" INTO IdCajaMoneda 		FROM :v_Dato WHERE "Campo" = 'IdCajaMoneda';

			UPDATE "BSCL.Tesoreria.BD::TES.CajaMoneda"
			SET "IdMoneda" = 		 COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdMoneda'), 		"IdMoneda"),
				"Saldo" = 			 COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'Saldo'), 			"Saldo"),
				"IdTipoMovimiento" = COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdTipoMovimiento'), "IdTipoMovimiento"),
				"IdEstado" = 		 COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdEstado'), 		"IdEstado")
			WHERE "IdCajaMoneda" = IdCajaMoneda AND "IdEmpresa" = p_IdEmpresa;
			
			v_DatoLog = Select 'Moneda de Caja' AS "SubRegistro", 
							"Campo" AS "CampoRegistro" , 
							"ValorAnterior" AS "ValorAntiguo", 
							"Valor" AS "ValorNuevo" 
							From :v_Dato Where "Valor" <> "ValorAnterior";
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'TES' 
				,p_IdUsuario => :p_IdUsuario 
				,p_IdEmpresa => :p_IdEmpresa
				,p_FechaSistemaLog => :p_FechaModificacion
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro => 'CAJA'
				,p_IdProceso => IdCajaMoneda
				--,p_EstadoProceso => ''
				,p_NombreProceso => 'Modificacion Moneda de Caja'
				,p_DetalleLog => :v_DatoLog
				);
			
		-------Mantenedor Caja - Usuario
		ELSEIF UPPER(:p_Mantenedor) = 'CAJAUSUARIO' THEN
		
			SELECT "Valor" INTO IdCajaUsuario 		FROM :v_Dato WHERE "Campo" = 'IdCajaUsuario';
				
			UPDATE "BSCL.Tesoreria.BD::TES.CajaUsuario"
			SET "IdUsuario"  	= COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdUsuario'), 	"IdUsuario"),
				"Sobregiro" 	= COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'Sobregiro'), 	"Sobregiro"),
				"FechaInicio" 	= TO_DATE(COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'FechaInicio'), 	"FechaInicio"), 'YYYY-MM-DD'),
				"FechaTermino" 	= TO_DATE(COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'FechaTermino'),	"FechaTermino"), 'YYYY-MM-DD'),
				"IdEstado" 		= COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdEstado'), 	"IdEstado")
			WHERE "IdCajaUsuario" = IdCajaUsuario AND "IdEmpresa" = p_IdEmpresa;
			
			v_DatoLog = Select 'Usuario de Caja' AS "SubRegistro", 
							"Campo" AS "CampoRegistro" , 
							"ValorAnterior" AS "ValorAntiguo", 
							"Valor" AS "ValorNuevo" 
							From :v_Dato Where "Valor" <> "ValorAnterior";
			
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'TES' 
				,p_IdUsuario => :p_IdUsuario 
				,p_IdEmpresa => :p_IdEmpresa
				,p_FechaSistemaLog => :p_FechaModificacion
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro => 'CAJA'
				,p_IdProceso => IdCajaUsuario
				--,p_EstadoProceso => ''
				,p_NombreProceso => 'Modificacion Usuario - Caja'
				,p_DetalleLog => :v_DatoLog
				);
		
		-------Mantenedor Caja - Producto Empresa
		ELSEIF UPPER(:p_Mantenedor) = 'CAJAPRODUCTO' THEN
			
			SELECT "Valor" INTO IdCajaProductoEmpresa 	FROM :v_Dato WHERE "Campo" = 'IdCajaProductoEmpresa';
			
			UPDATE "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa"
			SET "IdProductoEmpresa" = 	COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdProductoEmpresa'), "IdProductoEmpresa"),
				"IdEstado"  = 			COALESCE((SELECT "Valor" FROM :v_Dato WHERE "Campo" = 'IdEstado'), 			"IdEstado")
			WHERE "IdCajaProductoEmpresa" = IdCajaProductoEmpresa AND "IdEmpresa" = p_IdEmpresa;
			
			v_DatoLog = Select 'Producto Empresa de Caja' AS "SubRegistro", 
							"Campo" AS "CampoRegistro" , 
							"ValorAnterior" AS "ValorAntiguo", 
							"Valor" AS "ValorNuevo" 
							From :v_Dato Where "Valor" <> "ValorAnterior";
							
			CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
				p_IdTipoLog => 'MODIFICACION' 
				,p_IdModulo => 'TES' 
				,p_IdUsuario => :p_IdUsuario 
				,p_IdEmpresa => :p_IdEmpresa
				,p_FechaSistemaLog => :p_FechaModificacion
				,p_IdUsuarioAtendido => :p_IdUsuario
				,p_IdTipoRegistro => 'CAJA'
				,p_IdProceso => IdCajaProductoEmpresa
				--,p_EstadoProceso => ''
				,p_NombreProceso => 'Modificacion Producto Empresa - Caja'
				,p_DetalleLog => :v_DatoLog
				);
		
		END IF;
	END;

END;
