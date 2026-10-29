SET SCHEMA "BSCL";


DROP PROCEDURE  "BSCL.SocioNegocio.BD.Servicios::CLI.ModificarAnotacionCliente" ;

CREATE PROCEDURE
-- 
--  Proposito: Servicio para la modificacion de anotaciones de SocioNegocio.
--  Autor: CAPONTE - BCS
--  Fecha Creacion: 07/02/2016
-- 

"BSCL.SocioNegocio.BD.Servicios::CLI.ModificarAnotacionCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					bigint,			-- ID  Empresa destino
	IN  p_IdUsuario					bigint,			-- ID  del usuario que solicita el servicio
	IN  p_IdModulo					nvarchar(3),  	-- ID  del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 		nvarchar(1)	,	-- S/N se usa en caso de servicios anidados.
		  
	-- Parametros entrada del servicio publico
   	IN  p_IdCliente						BIGINT,
   	IN  p_IdAnotacion					BIGINT,
   	IN  p_Titulo						NVARCHAR(100),
   	IN  p_Detalle						NVARCHAR(500),
   	IN  p_IdMotivo						NVARCHAR(6),
   	IN  p_IdAmbito						NVARCHAR(6),
   	IN  p_IdRelevancia					NVARCHAR(6),
   	IN  p_Borrado						NVARCHAR(1) DEFAULT 'N',
   	IN  p_FechaDesde					NVARCHAR(10) DEFAULT '',
   	IN  p_FechaHasta					NVARCHAR(10) DEFAULT '',

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
		
		DECLARE v_FechaIngreso					SECONDDATE;-- FORMATO 'YYYY-MM-DD HH24:MI:SS.FF7'
		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		---------------------------------------------------

					
		--------------------
		--Negocio Aqui
		--------------------

		
		BEGIN
			
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(p_IdEmpresa,:v_FechaIngreso);
			
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'Titulo', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "Titulo" != :p_Titulo AND "IdAnotacion" = :p_IdAnotacion;
		
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'IdMotivo', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "IdMotivoAnotacion" != :p_IdMotivo AND "IdAnotacion" = :p_IdAnotacion;			

			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'IdAmbito', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "IdAmbitoAnotacion" != :p_IdAmbito AND "IdAnotacion" = :p_IdAnotacion;
			
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'IdRelevancia', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "IdRelevanciaAnotacion" != :p_IdRelevancia AND "IdAnotacion" = :p_IdAnotacion;
			
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'FechaDesde', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "ValidoDesde" != :p_FechaDesde AND "IdAnotacion" = :p_IdAnotacion;
			
			INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
			("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
			SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 'ANOTACIONES_CLIENTE_', 'FechaHasta', :p_IdEmpresa, :p_IdCliente, 0, :p_IdUsuario, LEFT(:v_FechaIngreso,10) , 0, 'PEN', 'Cliente - ModificarAnotacionCliente.sql'
			FROM "BSCL.SocioNegocio.BD::CLI.Anotacion" 
			WHERE "ValidoHasta" != :p_FechaHasta AND "IdAnotacion" = :p_IdAnotacion;

				
			UPDATE 	"BSCL.SocioNegocio.BD::CLI.Anotacion"
			SET "Titulo" 				= p_Titulo, 
				"Detalle"				= p_Detalle,
				"IdMotivoAnotacion" 	= p_IdMotivo,
				"FechaModificacion" 	= v_FechaIngreso,
				"IdAmbitoAnotacion" 	= p_IdAmbito,
				"IdRelevanciaAnotacion" = p_IdRelevancia,
				"Borrado"				= p_Borrado,
				"ValidoDesde"			= p_FechaDesde,
				"ValidoHasta"			= p_FechaHasta
			WHERE 	"IdAnotacion" 		= p_IdAnotacion
			AND 	"IdCliente" 		= p_IdCliente;
			
			p_out_RefId = :p_IdAnotacion;				
		END;		
	
		--------------------
		-- Normativo 
		--------------------
		
		
		--------------------
		--FIN SP 
		--------------------
	END;	
END;