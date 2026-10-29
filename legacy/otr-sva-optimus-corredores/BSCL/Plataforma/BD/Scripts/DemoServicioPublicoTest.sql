SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::DemoServicioPublico";

CREATE PROCEDURE
-- 
--  Proposito: Esqueleto de un servicio publico
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::DemoServicioPublico" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			--ID Empresa destino
	IN  p_IdUsuario					bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  		--ID del Modulo que lo invoca	
	IN  p_simularOperacion			nvarchar(1),	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 		nvarchar(1),	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
	IN  p_idcliente 				bigint,
	IN  p_monto 					Decimal(26,8),

	-- Parametros salida del servicio publico
	
	
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
		declare v_RutCliente nvarchar(100) := 'RUT';
		declare v_NombreCliente nvarchar(100) := 'NOMBRECLIENTE';
						
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			--validacion invalidante configuración básica del sistema
			--CALL "BSCL"."BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:p_out_ErroresDetalle,:v_resultadoRegla);
			--if :v_resultadoRegla = 'RECH' then
			--	SIGNAL SALIDA_CONTROLADA; 	
			--end if;
			--validacion invalidante (No continua, lleva signal)
			IF length(v_RutCliente)<2 THEN
				v_hash= select 'CLIENTE_RUT' as "Codigo",:v_RutCliente as "Valor" FROM DUMMY UNION ALL
				        select 'CLIENTE_NOMBRE',:v_NombreCliente from DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RUT_INVALIDO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'RUT_INVALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle; 
				SIGNAL SALIDA_CONTROLADA; 		  
			END IF;		
			--validacion sin signal
			IF length(v_RutCliente)<2 THEN
				v_hash= select 'CLIENTE_RUT' as "Codigo",:v_RutCliente as "Valor" FROM DUMMY UNION ALL
				        select 'CLIENTE_NOMBRE',:v_NombreCliente from DUMMY;
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('RUT_INVALIDO',:v_hash,:v_erroroutmsg);
				p_out_ErroresDetalle =  select 'RUT_INVALIDO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
										select * from :p_out_ErroresDetalle; 
			END IF;				
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;				
		END;
		-----------------------------------------------------
		--Validaciones via Motor (solo complejas)
		--(Normalmente no deberia usarse este bloque de codigo ) 
		-----------------------------------------------------		
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			DECLARE v_reglashashmsg Table ("Codigo" nvarchar(40),"Valor" nvarchar(520));			
			DECLARE v_reglassalida  Table("idRegla" bigint, "Habilitado" nvarchar(1),"CodComportamiento"   nvarchar(40),"ResultadoEvaluacion" integer,"ReglaActivada" integer,"MensajeRegla"  nvarchar(520));		
			--Pasamos informacion al motor de reglas para las validaciones( ver en wiki motor de reglas por las definiciones de parametros )
			v_reglashashmsg = 
			   select 'IdCliente' 	as "Codigo",CAST (:p_idcliente AS NVARCHAR) 	 as "Valor" from dummy UNION ALL
			   select 'Monto' 		as "Codigo",CAST (:p_monto AS NVARCHAR) 		 as "Valor" from dummy UNION ALL
			   select 'Tasa' 		as "Codigo",'12.1234' 							 as "Valor" from dummy;								   
			CALL "BSCL.Plataforma.BD.Servicios::ProcesarReglasNegocio" (
				:p_IdEmpresa,
				0,   --grupo  regla ver tabla ReglaGrupo
				'S', --S: Solo validaciones, N:Solo reglas
				:v_reglashashmsg, --tabla
				:v_resultadoRegla,
				:v_reglassalida);
				---
			if (v_resultadoRegla='RECH') then
				CALL "BSCL.Plataforma.BD.Servicios::ConvertirMensajesReglas"(
					:v_reglassalida,
					:p_out_AdvertenciasDetalle,
					:p_out_ErroresDetalle);
				SIGNAL SALIDA_CONTROLADA;
			end if;						
		END;
		--
	
			
		--------------------
		--Reglas Negocio 
		--------------------		
		IF p_UsaReglasNegocio = 'S' THEN 
		BEGIN
			declare v_resultadoRegla nvarchar(40);
			DECLARE v_reglashashmsg Table ("Codigo" nvarchar(40),"Valor" nvarchar(520));			
			DECLARE v_reglassalida  Table("idRegla" bigint, "Habilitado" nvarchar(1),"CodComportamiento"   nvarchar(40),"ResultadoEvaluacion" integer,"ReglaActivada" integer,"MensajeRegla"  nvarchar(520));
			--Pasamos informacion al motor de reglas ( ver en wiki motor de reglas por las definiciones de parametros )
			v_reglashashmsg = 
			   select 'IdCliente' 	as "Codigo",CAST (:p_idcliente AS NVARCHAR) 	 as "Valor" from dummy UNION ALL
			   select 'Monto' 		as "Codigo",CAST (:p_monto AS NVARCHAR) 		 as "Valor" from dummy UNION ALL
			   select 'Tasa' 		as "Codigo",'12.1234' 							 as "Valor" from dummy;								   
			CALL "BSCL.Plataforma.BD.Servicios::ProcesarReglasNegocio" (
				:p_IdEmpresa,
				0,   --grupo  regla ver tabla ReglaGrupo
				'N', --S: Solo validaciones, N:Solo Reglas
				:v_reglashashmsg, --tabla
				:v_resultadoRegla,
				:v_reglassalida);
			--Alguna de las reglas tiene solamente adevertencias
			if :v_resultadoRegla = 'ADVE' then
				BEGIN 
					CALL "BSCL.Plataforma.BD.Servicios::ConvertirMensajesReglas"(
						:v_reglassalida,
						:p_out_AdvertenciasDetalle,
						:p_out_ErroresDetalle);
				END;
			--Alguna de las reglas requiere autorizacion
			elseif :v_resultadoRegla = 'RAUT' then
				BEGIN					
					CALL "BSCL.Plataforma.BD.Servicios::ConvertirMensajesReglas"(
						:v_reglassalida,
						:p_out_AdvertenciasDetalle,
						:p_out_ErroresDetalle);				 					
					--DEPENDIENDO DEL CASO 
					--   Si es un servicio de solicitud de una operacion => continua. 
					--   Si es un servicio de ingresar/confirmar operacion, ingresa autorizacion, y salida controlada
					SIGNAL SALIDA_CONTROLADA; --Normalmente no hago nada	 
				END;										 
			--Alguna de las reglas detienen la operacion
			elseif :v_resultadoRegla = 'RECH' then
				BEGIN 
					--TODO: Enviar mensajes  --  ( utilizar SP UTIL ConvertirMensajeASalida)
					SIGNAL SALIDA_CONTROLADA;					
				END;
			end if;		
						
		END;
		END IF;
		--Solo fue una simulacion del servicio... move bitch,get out the way
		if p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;				
		--------------------
		--Negocio Aqui
		--------------------
		--se podrian llamar otros servicios publicos ej: 
		--  tesoreria
		--  facturacion
		--  custodia
		--  contabilidad
							
		--------------------
		--Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
