SET SCHEMA "BSCL";
GO 
DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaCuentaBancaria" ;
GO 
CREATE PROCEDURE
-- 
--  Proposito: Servicio para la creacion de personas.
--  Autor: CFlores - BCS
--  Fecha Creacion: 17/12/2015
-- 

"BSCL.SocioNegocio.BD.Servicios::PER.CrearPersonaCuentaBancaria" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
	  
	-- Parametros entrada del servicio publico
	IN  p_IdBanco  					BIGINT,	--//[FK];CuentaBancaria;IdBanco;Banco;IdBanco
	IN  p_IdTipoCuentaBancaria  	NVARCHAR(4),	--//[FK];CuentaBancaria;IdTipoCuentaBancaria;TipoCuentaBancaria;IdTipoCuentaBancaria
	IN  p_IdMoneda  				NVARCHAR(3),	--//[FK];CuentaBancaria;IdMoneda;TipoMonedaCuenta;IdTipoMonedaCuenta
	IN  p_NumeroCuentaBancaria   	NVARCHAR(100),    
	IN  p_IdPersona					BIGINT,
	IN  p_IdEstado	 				NVARCHAR(3),
	IN  p_IdPlaza					BIGINT,
	IN  p_CrearPreferenciaPago		NVARCHAR(1) DEFAULT 'N',
	IN  p_IdCliente					BIGINT DEFAULT 0,
	IN  p_EsPac                     NVARCHAR(1) DEFAULT 'N',
	--PERU
	IN  p_IdTipoIdentificadorCuenta	  NVARCHAR(3),
	IN  p_IdentificadorCuenta		  NVARCHAR(40),
	IN 	p_IdPersonaRelacionada		  BIGINT,
	IN  p_CodigoCuentaInterbancario	  NVARCHAR(40),
	
	IN  p_CodigoAba					  NVARCHAR(50),
	IN  p_OtroCodigoBancoExtranjero	  NVARCHAR(50),
	IN  p_CodigoIban				  NVARCHAR(50),
	IN  p_OtroCodigoCuentaExtranjero  NVARCHAR(50),
    

	-- Parametros salida del servicio publico
    OUT p_out_RefId bigint,
	-- Salidas para mensajes de advertencias y errores
	OUT p_out_AdvertenciasContador 	int,
	OUT p_out_AdvertenciasDetalle 	table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 		int,
	OUT p_out_ErroresDetalle 		table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520))
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA BSCL
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
			--Error interno no manejado  (El log sera captado por la capa de XS control)		
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
		DECLARE v_IdCuentaBancaria			 	bigint:=0;
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		DECLARE v_IdConcepto	       			NVARCHAR(6):='NUECTB';
		DECLARE v_RequiereAutorizacion			NVARCHAR(1):='';
		DECLARE v_IdEstadoAutorizacion			NVARCHAR(3):='PEN';
		
	    DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
	    DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
        DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));
       
       	DECLARE v_existe 						NVARCHAR(1);
		DECLARE v_NumeroCuentaBancaria 			NVARCHAR(100):='';
       
		
		-----------------------------------------------------
		--Validaciones basicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		
		--------------------
		--Negocio Aqui
		--------------------
		
		BEGIN
			
		SELECT CASE WHEN COALESCE(p_NumeroCuentaBancaria,'') = '' THEN '0' ELSE p_NumeroCuentaBancaria END INTO v_NumeroCuentaBancaria FROM DUMMY;
		
		v_exitente = SELECT  "IdCuentaBancaria" FROM "BSCL.SocioNegocio.BD::PER.CuentaBancaria" 
						WHERE "NumeroCuentaBancaria" = v_NumeroCuentaBancaria 
						AND "IdBanco" = p_IdBanco
						AND "IdPersona" = p_IdPersona
						AND "Borrado" = 'N';
					
		SELECT CASE WHEN count(1) > 0 THEN 'S' ELSE 'N' END AS "EXISTE" INTO v_existe FROM :v_exitente;
	
	
		IF v_existe = 'N' THEN 
			
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			
			-- recuperamos numero de secuencia que usaremos para el id 
		    SELECT "BSCL.Plataforma.BD.Secuencias::BAN.IdCuentaBancaria".NEXTVAL INTO v_IdCuentaBancaria FROM DUMMY;
	
			p_out_RefId:=v_IdCuentaBancaria;
			
		 	------------------------------------------
			--Crear Documentos de identidad
			------------------------------------------


					insert into "BSCL.SocioNegocio.BD::PER.CuentaBancaria"
					(
						   "IdCuentaBancaria",
						   "IdBanco",
						   "IdTipoCuentaBancaria",
						   "IdMoneda",
						   "NumeroCuentaBancaria",
						   "IdPersona",
						   "FechaIngreso",
						   "FechaCreacion",
						   "FechaModificacion",
						   "IdEstado", 
						   "IdEstadoAutorizacion",
						   "IdUsuarioCreador",
						   "IdPlaza",
						    "IdTipoIdentificadorCuenta",
						    "IdentificadorCuenta",
						    "IdPersonaRelacionada",
						    "CodigoCuentaInterbancario",
							"CodigoAba",
							"OtroCodigoBancoExtranjero",
							"CodigoIban",
							"OtroCodigoCuentaExtranjero",
							"EsPac"
						) VALUES (
							v_IdCuentaBancaria,
							p_IdBanco,
							p_IdTipoCuentaBancaria,
							p_IdMoneda,
							v_NumeroCuentaBancaria,
							p_IdPersona,							
							v_FechaIngreso,--TO_TIMESTAMP(p_FechaIngreso,'YYYY-MM-DD HH24:MI:SS.FF7') 					
							CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
							CURRENT_UTCTIMESTAMP,
						  	p_IdEstado,							
							v_IdEstadoAutorizacion,
							p_IdUsuario,
							p_IdPlaza,
							p_IdTipoIdentificadorCuenta,
							p_IdentificadorCuenta,
							p_IdPersonaRelacionada,
							p_CodigoCuentaInterbancario,							
							p_CodigoAba,
							p_OtroCodigoBancoExtranjero,
							p_CodigoIban,
							p_OtroCodigoCuentaExtranjero,
							p_EsPac
							);
							
					
					IF p_CrearPreferenciaPago = 'S' THEN
					
						UPDATE  DOC
						SET
							DOC."PorDefecto" = 'N'
						FROM "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" DOC
						INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = DOC."IdPortafolio" 
						INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE ON PRE."IdPreferenciaPago" = DOC."IdPreferenciaPago"
						WHERE POR."IdCliente" = :p_IdCliente 
						AND PRE."IdMoneda" = :p_IdMoneda
						AND PRE."RecibePagaPortafolio" = 'R'
						AND PRE."IdTipoProducto" NOT IN ('MERF','MEFW');		
					
						insert into "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio"
						(					
						"IdPreferenciaPagoPortafolio",
						"IdPortafolio",
						"IdPreferenciaPago",
						"IdCuentaBancaria",
						"IdTarjetaBancaria",
					   	"FechaCreacion",
						"FechaModificacion",
						"IdUsuarioCreador",
						"IdEstado",
						"IdEmpresa",			
						"FechaIngreso",					 
						"IdEstadoAutorizacion",
						"PorDefecto"
						) 
						SELECT "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio".NEXTVAL,
						POR."IdPortafolio",
						PRE."IdPreferenciaPago",
						v_IdCuentaBancaria,
						0,
						CURRENT_UTCTIMESTAMP, --p_Creacion, -- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
						CURRENT_UTCTIMESTAMP,
						p_IdUsuario,
						'ACT',
						p_IdEmpresa,
						v_FechaIngreso,
						v_IdEstadoAutorizacion,
						'S' 
						FROM 
						"BSCL.Portafolio.BD::POR.Portafolio" POR,
						"BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
						WHERE POR."IdCliente" = :p_IdCliente AND PRE."IdMedioDePago" = 'TRA' AND PRE."IdMoneda" = :p_IdMoneda AND PRE."RecibePagaPortafolio" = 'R'
						AND "IdTipoProducto" NOT IN ('MERF','MEFW');
			
					END IF;
			------------------------------------------
			--Pregunta Si requiere autorizacion
			------------------------------------------
			V_RegistroSolicitudCambio =SELECT  * FROM "BSCL.Plataforma.BD::PLA.SolicitudCambio" WHERE 1=2;
			
			call "BSCL.Plataforma.BD.Servicios::AUT.RequiereEventoAutorizacion" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			);	
			
			/*
			call "BSCL.SocioNegocio.BD.Servicios::PER.RequiereAutorizacionPersonaCuentaBancaria" (
				:p_IdEmpresa,
				:p_IdUsuario,
				:p_IdModulo,
				:V_RegistroSolicitudCambio,
				:v_IdConcepto,
				:v_RequiereAutorizacion
			); */
						
	if v_RequiereAutorizacion ='S' and p_UsaReglasNegocio != 'N' then
			------------------------------------------
			--Genera Evento de Autorizacion
			------------------------------------------

				DECLARE	v_Glosa							NVARCHAR(520);
				DECLARE v_out_idAuth 					bigint;
				DECLARE v_out_AuthAdvertenciasContador 	int;
				DECLARE v_out_AuthErroresContador 		int; 
				v_out_AuthAdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
				v_out_AuthErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
				
				SELECT "DocumentoIdentidad" || ' / ' || "NombreCompleto" || ' / NroCta: ' || v_NumeroCuentaBancaria  into  v_Glosa 
				FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" WHERE "IdPersona" = p_IdPersona;	
					
						CALL "BSCL.Plataforma.BD.Servicios::AUT.CrearEventoAutorizacion" (
							p_IdEmpresa 				=> p_IdEmpresa,
							p_IdUsuario 				=> p_IdUsuario,
							p_IdModulo 					=> p_IdModulo,
							p_UsaReglasNegocio 			=> 'N',								
							p_IdTipoEvento 				=> v_IdConcepto,--'NUEDOC',
							p_IdModuloSolicita 			=> p_IdModulo,
							p_IdUsuarioSolicita 		=> p_IdUsuario,
							p_Fecha 					=> TO_VARCHAR (:v_FechaIngreso, 'YYYY-MM-DD'), 
							p_IdConcepto 				=> v_IdConcepto,
							p_NumeroOperacionConcepto 	=>  CAST (v_IdCuentaBancaria AS NVARCHAR(40)),--v_secuencia_IdSolicitudCambio,
							p_FechaOperacionConcepto 	=> TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD'),
							p_IdCliente 				=> p_IdPersona, --v_IdCliente,
							p_Glosa 					=> v_Glosa,
							--salida
							p_out_IdEvento 				=> v_out_idAuth,
							p_out_AdvertenciasContador 	=> v_out_AuthAdvertenciasContador,
							p_out_AdvertenciasDetalle 	=>  v_out_AuthAdvertenciasDetalle,
							p_out_ErroresContador 		=>  v_out_AuthErroresContador,
							p_out_ErroresDetalle 		=>  v_out_AuthErroresDetalle);		
					
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
				SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'CUENTA_BANCARIA_', 'Creado', :p_IdEmpresa, 0, :p_IdPersona, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Persona - CrearPersonaCuentaBancaria.sql'
				FROM dummy;
				
				v_IdEstadoAutorizacion:='ING';
				CALL "BSCL.SocioNegocio.BD.Servicios::PER.ConfirmarCrearPersonaCuentaBancaria"(
					p_IdEmpresa,
					p_IdUsuario,
					p_IdModulo,
					p_UsaReglasNegocio,
					v_IdCuentaBancaria,
					v_IdEstadoAutorizacion,
					v_out_AdvertenciasContador,
					v_out_AdvertenciasDetalle,
					v_out_ErroresContador,
					v_out_ErroresDetalle);

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
			
		
		ELSE 
		
				p_out_ErroresContador		:= 1;
				p_out_ErroresDetalle		=  SELECT "CodDiccionario" AS "Codigo", "Descripcion" FROM "BSCL.Plataforma.BD::PLA.Diccionario" WHERE "CodDiccionario" = 'ERROR_INSERTAR_CUENTA_BANCARIA_REPETIDA';	
		
		END IF;
			
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;