SET SCHEMA "BSCL"; 
GO 
DROP PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.NotificarJuntaAccionistas";
GO
CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::EVC.NotificarJuntaAccionistas" (

    IN  p_IdEmpresa							BIGINT,			
	IN  p_IdUsuario							BIGINT,			
	IN  p_IdModulo							NVARCHAR(3),  	

	IN  p_TablaNotificacion	  				TABLE(
													"IdClienteNominaJuntaAccionista" BIGINT,
													"NombreCliente"          		 VARCHAR(250),
													"CiudadCliente"          	     VARCHAR(250),
													"EnvioCorrespondencia"           VARCHAR(250),
													"FechaRealizacion"               DATE,
													"HoraRealizacion"                VARCHAR(8),
													"Materias"                       VARCHAR(500),
													"FechaCierre"					 DATE,
													"IdDireccionElectronica"         BIGINT,
													"Lugar"                          VARCHAR(250),
													"IdPersona"                      BIGINT
												  ),
	OUT p_out_IdEvento         			 	TABLE(
													"IdEvento"        BIGINT
												  ),
	OUT p_out_AdvertenciasContador 			int,
	OUT p_out_AdvertenciasDetalle 			table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
	OUT p_out_ErroresContador 				int,
	OUT p_out_ErroresDetalle 				table ("Codigo" nvarchar(40),"Descripcion" nvarchar(5000))
	)   
	 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	AS
	
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	DECLARE SALIDA_CONTROLADA 		CONDITION; 
	DECLARE v_errorcuenta 			INT DEFAULT 0;  
	DECLARE v_errorhashmsg 			table ("Codigo" nvarchar(40),"Valor" nvarchar(520));
	DECLARE v_erroroutmsg  			nvarchar(520);	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA


	BEGIN
		--salida/error/advertencia controlado por este servicio
	p_out_AdvertenciasContador	:= 0;
	p_out_ErroresContador		:= 0;
	p_out_AdvertenciasDetalle	= select '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" from dummy where 1=2;
	p_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
	
		select count(1) into p_out_ErroresContador 
		from :p_out_ErroresDetalle;
		select count(1) into p_out_AdvertenciasContador 
		from :p_out_AdvertenciasDetalle;		 					 	
	END;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		declare v_errorExt nvarchar(40);
		IF ::SQL_ERROR_CODE > 10000 THEN
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = select * from :p_out_ErroresDetalle UNION ALL  
								   select v_errorExt,::SQL_ERROR_MESSAGE from dummy;				
			select count(1) into p_out_ErroresContador 
			from :p_out_ErroresDetalle;										
		ELSE	
			RESIGNAL; 
		END IF;		
	END;	

	
	BEGIN	
		--------------------
		--Declare Variables
		--------------------
		DECLARE v_out_Agregar_Evento TABLE("IdEvento" BIGINT,"IdSecuencia" BIGINT);
		DECLARE	v_Count,v_IdPersona,v_IdPersonaCumplimiento,v_IdDireccionDestinatario BIGINT:=0;
		DECLARE v_IdPersona_Corredora BIGINT:=0;
		
		--------------------
		--Lógica Negocio
		--------------------
		
		--Inicializacion de variables:
	
		BEGIN -- NOTIFICACIONES 
		
		SELECT COUNT(1) INTO v_count FROM :p_TablaNotificacion;
		
		
		-- Obtiene IdPersona de la empresa
		SELECT "IdPersona" 
		INTO v_IdPersona_Corredora
		FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" WHERE "IdEmpresa" = :p_IdEmpresa;
		
		
		IF v_count > 0 THEN 
			----NOTIFICACION A CUMPLIMIENTO
			v_GrupoEventoNotificacion = SELECT 
					ROW_NUMBER()over ( order by TNO."IdClienteNominaJuntaAccionista") AS "IdInterno"
					,TNO."IdPersona"
					,TNO."IdDireccionElectronica"
					,TNO."NombreCliente"
					,TNO."CiudadCliente"
					,TNO."EnvioCorrespondencia"
					,TNO."FechaRealizacion"
					,TNO."HoraRealizacion"
					,TNO."Materias"
					,TNO."FechaCierre"
					,TNO."Lugar"
					FROM :p_TablaNotificacion TNO;
					
			v_Eventos = 
				 SELECT 
				CAST('JUNTA_ACCIONISTAS' AS NVARCHAR(40))	AS "CodigoTipoEvento", 
				NOF."IdInterno" 					AS "IdSecuencia", 
				CURRENT_UTCTIMESTAMP 				AS "FechaCreacionSistema", 
				:v_IdPersona_Corredora				AS "IdPersonaRemitente", 
				NOF."IdPersona"          			AS "IdPersonaDestinatario", 
				0 									AS "IdPortafolio" 
				FROM  :v_GrupoEventoNotificacion NOF;
								
				v_IdDirecciones =
				SELECT NOF."IdInterno"					  AS "IdSecuencia",
					   NOF."IdDireccionElectronica"       AS "IdDireccion",
					   'TO' 							  AS "TipoDestinatario"
				FROM  :v_GrupoEventoNotificacion NOF;
				
				v_ArchivosAdjuntos =
				SELECT 0 AS "IdSecuencia", null AS "ArchivoAdjunto", '' AS "Nombre", '' AS "Contrasenia" FROM DUMMY WHERE 1=2;

				--Datos del Cliente en mail
											
			v_EventoDetalle =   						--- DATOS CLIENTE
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'NombreCliente' AS "Llave",  
				CAST (NOF."NombreCliente" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'CiudadCliente' AS "Llave",  
				CAST (NOF."CiudadCliente" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'EnvioCorrespondencia' AS "Llave",  
				CAST (NOF."EnvioCorrespondencia" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION 
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'FechaRealizacion' AS "Llave",  
				CAST (NOF."FechaRealizacion" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'HoraRealizacion' AS "Llave",  
				CAST (NOF."HoraRealizacion" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION  
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'Materias' AS "Llave",  
				CAST (NOF."Materias" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION  
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'FechaCierre' AS "Llave",  
				CAST (NOF."FechaCierre" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF
				UNION  
				SELECT NOF."IdInterno" AS "IdSecuencia", 
				'Lugar' AS "Llave",  
				CAST (NOF."Lugar" AS  NVARCHAR(100)) AS "Valor" 
				FROM :v_GrupoEventoNotificacion NOF;
				
				
				v_out_Agregar_Evento = SELECT 0 AS "IdEvento",0 as "IdSecuencia" FROM DUMMY WHERE 1=2;	
					
 				CALL "BSCL.Plataforma.BD.Servicios::EVT.AgregarEvento"(
 					p_IdEmpresa => p_IdEmpresa,
 					p_IdUsuario => p_IdUsuario,
 					p_IdModulo => p_IdModulo,
 					p_Eventos => :v_Eventos,
 					p_Valores => :v_EventoDetalle,
 					p_ArchivosAdjuntos => :v_ArchivosAdjuntos,
 					p_IdDirecciones => :v_IdDirecciones,
 					p_out_IdEvento => :v_out_Agregar_Evento
 					);
 					
 				UPDATE ClienteNominaJuntaAccionista 
		 		SET    "IdEstadoNotificacion" = 'OK'
		 		FROM "BSCL.Custodia.BD::CUS.ClienteNominaJuntaAccionista" ClienteNominaJuntaAccionista
		 		INNER JOIN :p_TablaNotificacion NOTIFICACIONES ON (NOTIFICACIONES."IdClienteNominaJuntaAccionista" = ClienteNominaJuntaAccionista."IdClienteNominaJuntaAccionista");
		 		
	 			p_out_IdEvento=SELECT "IdEvento"  FROM :v_out_Agregar_Evento;	
	 		
		END IF;
			
		END;
			
			
		--------------------
		--FIN SP 
		--------------------
	END;	
END;