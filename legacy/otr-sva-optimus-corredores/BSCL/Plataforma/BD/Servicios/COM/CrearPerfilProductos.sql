SET SCHEMA "BSCL";

DROP PROCEDURE  "BSCL.Plataforma.BD.Servicios::COM.CrearPerfilProductos";

CREATE PROCEDURE  
-- 
--  Proposito: Creacion de Perfil de Cobros, recibe Cabecera y Detalle.
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::COM.CrearPerfilProductos" (
	-- Parametros entrada comunes en todos los servicios publicos

	IN  p_IdEmpresa							bigint,			--ID Empresa destino
	IN  p_IdUsuario							bigint,			--ID del usuario que solicita el servicio
	IN  p_IdModulo							nvarchar(3),  	--ID del Modulo que lo invoca
	IN  p_simularOperacion					nvarchar(1),	-- S/N se usa para simular operacion (algunos casos)
	IN  p_UsaReglasNegocio	 				nvarchar(1),	-- S/N se usa en caso de servicios anidados.	

	IN p_IdPerfilProductos					NVARCHAR(40),	
	IN p_DescripcionPerfilProductos			NVARCHAR(100),		
	IN p_IdCategoriaInversionista			NVARCHAR(40),		
	IN p_EsPerfilPersonalizado				VARCHAR(1),	
		--,"p_FechaCreacion"				NVARCHAR(1)		
		--,"p_FechaModificacion"			NVARCHAR(1)		
	IN p_IdEstado							NVARCHAR(3),	
	IN p_PorDefecto							NVARCHAR(1),
	
	IN p_DetallePerfil TABLE(
		"p_IdTipoProducto"					NVARCHAR(4)		
		,"p_IdTipoOperacion"				NVARCHAR(6)		
		,"p_IdCanalIngreso"					VARCHAR(3)	
		,"p_IdMoneda"						VARCHAR(3)  
		--,"p_FechaCreacion"				NVARCHAR(1)	  	
		,"p_IdEstado"						NVARCHAR(3)			
		,"p_IdGrupoActivos"					NVARCHAR(40)	
		--,"p_IdEmpresa"						BIGINT			
	),
	
	-- Parametros salida del servicio publico
	OUT p_out_RefId bigint,	
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


		--
 

		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
			declare v_resultadoRegla nvarchar(40);
/*
			SELECT COUNT(1) INTO v_cantidadPersona from :p_Persona;
			if :v_cantidadPersona != 1 then
				v_hashmsg = select 'TipoEntidad' as "Codigo", 'persona' as "Valor" from dummy;			     		
				CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('SOLO_REGISTRO_UNICO',:v_hashmsg, :v_erroroutmsg);		
 				p_out_ErroresDetalle =  select 'SOLO_REGISTRO_UNICO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
								select * from :p_out_ErroresDetalle;
				SIGNAL SALIDA_CONTROLADA; 	
			end if;				
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;		
*/		
		END;
		-----------------------------------------------------
		--Validaciones via Motor (solo complejas)
		--(Normalmente no deberia usarse este bloque de codigo ) 
		-----------------------------------------------------		
		/*BEGIN
			declare v_resultadoRegla nvarchar(40);
			DECLARE v_reglashashmsg Table ("Codigo" nvarchar(40),"Valor" nvarchar(520));			
			DECLARE v_reglassalida  Table("idRegla" bigint, "Habilitado" nvarchar(1),"CodComportamiento"   nvarchar(40),"ResultadoEvaluacion" integer,"ReglaActivada" integer,"MensajeRegla"  nvarchar(520));		
			--Pasamos informacion al motor de reglas para las validaciones( ver en wiki motor de reglas por las definiciones de parametros )
			v_reglashashmsg = 
			   --select 'IdCliente' 	as "Codigo",CAST (:p_idcliente AS NVARCHAR) 	 as "Valor" from dummy UNION ALL
			   --select 'Monto' 		as "Codigo",CAST (:p_monto AS NVARCHAR) 		 as "Valor" from dummy UNION ALL
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
		END;*/
		--
	
			
		--------------------
		--Reglas Negocio 
		--------------------		
		
		
		--Solo fue una simulacion del servicio... move bitch,get out the way
		if p_simularOperacion = 'S' then
			SIGNAL SALIDA_CONTROLADA;
		end if;		
				
		--------------------
		--Negocio Aqui
		--------------------
		BEGIN
			
			INSERT INTO "BSCL.Plataforma.BD::COM.PerfilProductos"(
				"IdPerfilProductos",
				"DescripcionPerfilProductos",
				"IdCategoriaInversionista",
				"EsPerfilPersonalizado",
				"FechaCreacion",
				"FechaModificacion",
				"IdEstado",
				"PorDefecto",
				"IdEmpresa"
			)
			VALUES(
				:p_IdPerfilProductos,	
				:p_DescripcionPerfilProductos,	
				:p_IdCategoriaInversionista,	
				:p_EsPerfilPersonalizado,
				'2015-11-17',
				'2015-11-17',		
				:p_IdEstado,	
				:p_PorDefecto,
				:p_IdEmpresa
			);

			
			INSERT INTO "BSCL.Plataforma.BD::COM.DetallePerfilProductos" (
				"IdDetallePerfilProductos",
				"IdPerfilProductos",
				"IdTipoProducto",
				"IdTipoOperacion",
				"IdCanalIngreso",
				"IdMoneda",
				"IdGrupoActivos",
				"FechaCreacion",
				"IdEstado",
				"IdEmpresa"
			)
			SELECT 
				"BSCL.Plataforma.BD.Secuencias::DetallePerfilProductos".NEXTVAL as "IdDetallePerfilProductos",
				:p_IdPerfilProductos,
				"p_IdTipoProducto",
				"p_IdTipoOperacion",
				"p_IdCanalIngreso",
				'CLP',
				"p_IdGrupoActivos",
				'2015-11-13',
				"p_IdEstado",
				:p_IdEmpresa
			FROM
				:p_DetallePerfil;
	
			
			select 1 into p_out_RefId from dummy;
					
		END;		
		--------------------
		--Normativo 
		--------------------
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;