SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CLI.CrearDeclaracionOrigenFondos";
GO
CREATE PROCEDURE
--
--  Proposito: Servicio Declaración de Origen de Fondos.
--  Autor: JG - BCS
--  Fecha Creacion: 17/05/2016
--
"BSCL.SocioNegocio.BD.Servicios::CLI.CrearDeclaracionOrigenFondos" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa						 	BIGINT,		--ID Empresa destino
	IN  p_IdUsuario						 	BIGINT,		--ID del usuario que solicita el servicio
	IN  p_IdModulo						 	NVARCHAR(3),--ID del Modulo que lo invoca
    -- Origen de datos del servicio
	IN  p_IdCliente							BIGINT,
	IN  p_IdTipoOperacionOrigenFondos		NVARCHAR(40),
	IN  p_FechaDeclaracion					SECONDDATE,
	IN  p_Comentario						NVARCHAR(500) DEFAULT '',
	IN  p_IdEstado 							NVARCHAR(3),
	IN  p_OperacionInscripcion				NVARCHAR(40) DEFAULT '',
	
	IN 	p_Detalle							TABLE(
												"IdTipoOrigenFondo" NVARCHAR(40),
	 											"Valor"				DECIMAL(26,8),
	 											"Otros"				NVARCHAR(40)
	 										),

	-- Parametros salida del servicio publico
	OUT p_out_RefId  	 	 	 	 	 	BIGINT,

	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador  	 	int,
	OUT p_out_AdvertenciasDetalle  	 	 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),
	OUT p_out_ErroresContador  	 	 	 	int,
	OUT p_out_ErroresDetalle  	 	 	 	table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
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
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_count							INT:=0;
		DECLARE v_IdDeclaracionOrigenFondos	 	BIGINT:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NUEDOF';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
       
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		SELECT COUNT(1) INTO v_count FROM "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" WHERE  "IdCliente"=p_IdCliente
						AND "IdTipoOperacionOrigenFondos"=p_IdTipoOperacionOrigenFondos
						AND TO_DATE("FechaDeclaracion")=TO_DATE(p_FechaDeclaracion);
			
		IF(v_count > 0) THEN
			p_out_ErroresDetalle =  select 'REGISTRO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			SIGNAL SALIDA_CONTROLADA; 	
		END IF;
		
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.SocioNegocio.BD.Secuencias::DeclaracionOrigenFondos".NEXTVAL INTO v_IdDeclaracionOrigenFondos FROM DUMMY;
	
			
			p_out_RefId := -1;
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------

					INSERT INTO "BSCL.SocioNegocio.BD::CLI.DeclaracionOrigenFondos" (
						 "IdDeclaracionOrigenFondos",
						 "IdCliente",
						 "IdTipoOperacionOrigenFondos",
						 "FechaDeclaracion",
						 "Comentario",
						 "IdUsuarioCreador",
						 "FechaIngreso",
						 "FechaCreacion",
						 "FechaModificacion",
						 "IdEmpresa",
						 "IdEstado",
						 "IdEstadoAutorizacion",
						 "OperacionInscripcion" )
					VALUES (
						v_IdDeclaracionOrigenFondos,
						p_IdCliente,
						p_IdTipoOperacionOrigenFondos,
						p_FechaDeclaracion,
						p_Comentario,
						p_IdUsuario,
						v_FechaIngreso,
						CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						p_IdEmpresa,
						p_IdEstado,
						v_IdEstadoAutorizacion,
						p_OperacionInscripcion
					);
					
					--EL DETALLE.
					INSERT INTO "BSCL.SocioNegocio.BD::CLI.DetalleDeclaracionOrigenFondos" (
						"IdTipoOrigenFondo",
	 					"IdDeclaracionOrigenFondos",
		 				"Valor",
	 					"IdUsuarioCreador",
						"FechaIngreso",
						"FechaCreacion",
						"FechaModificacion", 
						"IdEstado",
						"Otros")
					SELECT 
						"IdTipoOrigenFondo",
						v_IdDeclaracionOrigenFondos,
						"Valor",
						p_IdUsuario,
						v_FechaIngreso,
						CURRENT_UTCTIMESTAMP,
						CURRENT_UTCTIMESTAMP,
						'ACT',
						"Otros"
					FROM :p_Detalle;
					
				p_out_RefId:=v_IdDeclaracionOrigenFondos;	
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio = SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);	
									
			if v_RequiereAutorizacion='S' then
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------

				DECLARE	v_Glosa			NVARCHAR(520);
				DECLARE v_out_idAuth 	bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int;
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" into  v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" WHERE "IdCliente" = p_IdCliente;	
						
				CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
					p_IdEmpresa 				=> p_IdEmpresa,
					p_IdUsuario 				=> p_IdUsuario,
					p_IdModulo 					=> p_IdModulo,
					p_UsaReglasNegocio 			=> 'N',								
					p_IdTipoEvento 				=> v_IdConcepto,--'NUEDOC',
					p_IdModuloSolicita 			=> p_IdModulo,
					p_IdUsuarioSolicita 		=> p_IdUsuario,
					p_Fecha 					=> TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'), 
					p_IdConcepto 				=> v_IdConcepto,
					p_NumeroOperacionConcepto 	=> CAST (v_IdDeclaracionOrigenFondos AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
					p_FechaOperacionConcepto 	=> TO_VARCHAR (v_FechaIngreso, 'YYYY-MM-DD'),
					p_IdCliente 				=> p_IdCliente, --v_IdCliente,
					p_Glosa 					=> v_Glosa,
					--salida
					p_out_IdEvento 				=> v_out_idAuth,
					p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
					p_out_AdvertenciasDetalle 	=> v_out_AuthAdvertenciasDetalle,
					p_out_ErroresContador 		=> v_out_AuthErroresContador,
					p_out_ErroresDetalle 		=> v_out_AuthErroresDetalle);		
					
			 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AuthAdvertenciasContador;	
					  			 
			 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
			                             From :v_out_AuthAdvertenciasDetalle Union All 
			                             Select "Codigo","RequiereAutorizacion", "Descripcion"
			                             From :p_out_AdvertenciasDetalle;  
					                                          
					                                          
			 p_out_ErroresContador := p_out_ErroresContador + v_out_AuthErroresContador;
					                                         
			 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
			                        From :v_out_AuthErroresDetalle Union All 
			                        Select "Codigo", "Descripcion" 
			                        From :p_out_ErroresDetalle; 
			 if 	p_out_ErroresContador > 0 then
						SIGNAL SALIDA_CONTROLADA;
						
					else
						--Mensaje de Salida
						p_out_AdvertenciasContador := p_out_AdvertenciasContador + 1;	
						v_hash= select '' as "Codigo",'' as "Valor" FROM DUMMY;
						CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajeDiccionario" ('REQUIERE_AUTORIZACION',:v_hash,:v_erroroutmsg);
						p_out_AdvertenciasDetalle =  select 'REQUIERE_AUTORIZACION' as "Codigo",'S' as "RequiereAutorizacion",:v_erroroutmsg as "Descripcion" from dummy UNION ALL
												     select * from :p_out_AdvertenciasDetalle;
						SIGNAL SALIDA_CONTROLADA;
			  end if;								
			              	                                     
					                                     
			else	--Confirma la creacion y cambia el estado del documento


				INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
				("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
				SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'DECLARACION_ORIGEN_FONDOS_', 'Creado', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - CrearDeclaracionOrigenFondos.sql' FROM dummy;
				
				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.SocioNegocio.BD.Servicios::CLI.ConfirmarCrearDeclaracionOrigenFondos"(
					p_IdEmpresa						=> p_IdEmpresa,
					p_IdUsuario						=> p_IdUsuario,
					p_IdModulo						=> p_IdModulo,
					p_UsaReglasNegocio				=> 'N',
					p_IdDeclaracionOrigenFondos		=> v_IdDeclaracionOrigenFondos,
					p_IdEstadoAutorizacion			=> v_IdEstadoAutorizacion,
					p_out_AdvertenciasContador		=> v_out_AdvertenciasContador,
					p_out_AdvertenciasDetalle 		=> v_out_AdvertenciasDetalle,
					p_out_ErroresContador			=> v_out_ErroresContador,
					p_out_ErroresDetalle			=> v_out_ErroresDetalle);

				 p_out_AdvertenciasContador := p_out_AdvertenciasContador + v_out_AdvertenciasContador;	
					  			 
				 p_out_AdvertenciasDetalle = Select "Codigo","RequiereAutorizacion", "Descripcion" 
				                             From :v_out_AdvertenciasDetalle Union All 
				                             Select "Codigo","RequiereAutorizacion", "Descripcion"
				                             From :p_out_AdvertenciasDetalle;  
				                                         
					                                          
				 p_out_ErroresContador := p_out_ErroresContador + v_out_ErroresContador;
					                                         
				 p_out_ErroresDetalle = Select "Codigo", "Descripcion" 
				                             From :v_out_ErroresDetalle Union All 
				                             Select "Codigo", "Descripcion" 
				                             From :p_out_ErroresDetalle; 		
			end if;			
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;