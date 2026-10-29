SET SCHEMA "BSCL";  
go
DROP PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaOficializarOrdenes";
go
-- 
--  Proposito: Servicio para  realizar la ofiacializacion de las ordenes.
--  Autor: - CHERNANDEZ - BCS
--  Fecha Creacion: 12/04/2016
--  
CREATE PROCEDURE "BSCL.Orden.BD.Servicios::CuadraturaOficializarOrdenes" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para la creacion del usuario
	---IN  p_FechaVencimiento			timestamp,  		
	IN  p_FechaVencimiento			NVARCHAR(10),

	-- Parametros salida del servicio publico
	OUT p_ordenes_oficializadas BIGINT,	
	OUT p_estado_oficializacion NVARCHAR(5),	
	
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador int,
	OUT p_out_AdvertenciasDetalle table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador int,
	OUT p_out_ErroresDetalle table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
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
			--Error interno no manejado  (El log será captado por la capa de XS control)		
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
		DECLARE v_idorden BIGINT;
		DECLARE v_new_folio NVARCHAR(40);
		DECLARE v_fechaSistema NVARCHAR(10);
		DECLARE v_CodigoUsuario NVARCHAR(40);
		DECLARE v_textoResultado NVARCHAR(40);
				
		DECLARE CURSOR c_ordenes(v_idorden bigint) FOR select "IdOrden" from "BSCL.Orden.BD::ORD.Orden"
				----WHERE "FechaVigencia" >= TO_DATE(:p_FechaVencimiento)
				WHERE "FechaVigencia" >= :p_FechaVencimiento
				AND "Folio" = ''
				AND "IdEstadoOrden" NOT IN ('ANU','RZD','NAU');
		
		DECLARE p_DetalleLog TABLE ("SubRegistro" nvarchar(40), "CampoRegistro" nvarchar(40), "ValorAntiguo" nvarchar(250), "ValorNuevo" nvarchar(250));
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						
		END;
		-----------------------------------------------------
		--Actualizar la fecha de vigencia de todas las ordenes vigentes, que se encuentren en el rango de fecha sistema recibido.
		-----------------------------------------------------
		
		p_estado_oficializacion:= 'NOK++';
		
		BEGIN
			p_ordenes_oficializadas:= 0;
			FOR cur_row AS c_ordenes(v_idorden)
			DO
				SELECT TO_VARCHAR("BSCL.Orden.BD.Secuencias::FolioOrdenes".NEXTVAL) into v_new_folio  FROM DUMMY;
				 
				UPDATE "BSCL.Orden.BD::ORD.Orden"
				SET "Folio" = v_new_folio
				WHERE "IdOrden" = cur_row."IdOrden";
				
				p_ordenes_oficializadas:= :p_ordenes_oficializadas+1;
			END FOR;
    		
  			p_estado_oficializacion:= 'OK+++';
		END;
		
		---------------------------------------------------		
		--Ejecutar el programa de logeo.
		---------------------------------------------------
		p_DetalleLog =  select 'UI_ORDENES_OFICIALIZADAS' as "SubRegistro", TO_VARCHAR(p_ordenes_oficializadas) as "CampoRegistro", '' as "ValorAntiguo", 'CIEMOD' as "ValorNuevo"
		FROM dummy;
		
		--Ejecutar el programa de logeo.
		CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
		p_IdTipoLog => 'INFORMATIVO' 
		,p_IdModulo => 'ORD' 
		,p_IdUsuario => p_IdUsuario 
		,p_IdEmpresa => p_IdEmpresa 
		-----,p_FechaSistemaLog => TO_VARCHAR (p_FechaVencimiento, 'YYYY-MM-DD')
		,p_FechaSistemaLog => p_FechaVencimiento
		,p_IdUsuarioAtendido => p_IdUsuario
		,p_IdTipoRegistro => 'ORDEN'
		,p_IdProceso => 6
		,p_NombreProceso => 'OFICIALIZACION_ORDEN'
		,p_DetalleLog => :p_DetalleLog
		);
		
		--LOG PARA EL CIERRE DE MODULO
		SELECT "CodigoUsuario" INTO v_CodigoUsuario FROM "BSCL.Plataforma.BD.Vistas::ACC.ConsultarUsuario" 
		WHERE "IdUsuario" = p_IdUsuario AND "IdEmpresa" = p_IdEmpresa;

		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaSistema => v_fechaSistema
		);
		
		
		IF :p_out_ErroresContador > 0 THEN
			v_textoResultado = 'Proceso con errores';
		ELSE
			v_textoResultado = 'OK';
		END IF;
		
		INSERT INTO "BSCL.Plataforma.BD::PAU.ControlProceso"
		("IdEmpresa", "IdControlProceso", "CodigoModulo", "CodigoProceso", "FechaSistema", "FechaRegistro", "CodigoUsuario", "ResultadoProceso")
		VALUES(p_IdEmpresa, "BSCL.Plataforma.BD.Secuencias::IdControlProceso".NEXTVAL, 'ORD', 'OFICIALIZACION_ORDEN', v_fechaSistema, CURRENT_UTCTIMESTAMP, v_CodigoUsuario, v_textoResultado);
	END;
END;