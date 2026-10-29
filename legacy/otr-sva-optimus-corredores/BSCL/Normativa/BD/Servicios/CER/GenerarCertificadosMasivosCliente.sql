SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.GenerarCertificadosMasivosCliente";
-- 
--  Proposito: Servicio para la recuperacion de un certificado de cliente
--  Autor: - CHERNANDEZ
--  Fecha Creacion: 13/09/2016
--  
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.GenerarCertificadosMasivosCliente" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa					BIGINT,			--ID Empresa destino
	IN  p_IdUsuario					BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo					NVARCHAR(3),  	--ID del Modulo que lo invoca

	-- Parametros de entrada para recuperar la informacion del certificado
	IN  p_DatosMasivos				TABLE("IdCertificado" BIGINT),

	-- Parametros salida del servicio publico
	OUT p_out_Result				NVARCHAR(3),
	
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
		DECLARE v_SecuenciaSolicitud	BIGINT DEFAULT 0; 
		DECLARE v_out_IdPrograma		TABLE("IdPrograma" BIGINT);
	    DECLARE v_TablaParametrosProgramaAUX   table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
   		DECLARE v_fechaSistema NVARCHAR(10);
   		DECLARE v_FechaVencimientoSolicitud NVARCHAR(10) default '1900-01-01';
   		DECLARE v_Observaciones NVARCHAR(100) DEFAULT 'GENERACION MASIVA CERTIFICADO CLIENTE';
   		DECLARE v_idDoc BIGINT;
   		
		-----------------------------------------------------
		--Validaciones básicas (Codigos,Existencias,etc)
		-----------------------------------------------------
		BEGIN
		/*			
			--Verificar que el certificado exista
			select COUNT("IdCertificadoCliente") INTO v_ExisteCertificado 
			FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarCertificadoCliente" 
			where "IdCertificadoCliente" = p_IdCertificadoCliente and "IdEmpresa" = p_IdEmpresa;
	
			IF v_ExisteCertificado < 1 THEN
				p_out_ErroresDetalle =  select 'UI_NO_EXISTE_CERTIFICADO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			END IF;
		*/
			--Si existen errores en las validaciones debe salir
			select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
			IF :p_out_ErroresContador > 0 THEN
				SIGNAL SALIDA_CONTROLADA;
			END IF;						  
			
		END;
		------------------------------------------
		--LOGICA DE NEGOCIO
		------------------------------------------
			
		--obtener fecha de sistema
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerConfiguracion"(p_IdEmpresa, 'PLA.FechaSistema', :v_fechaSistema);
		
		Select "BSCL.Normativa.BD.Secuencias::SolicitudCircular".NEXTVAL into v_SecuenciaSolicitud from dummy;
		select "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL into v_idDoc from dummy;
		
		insert into "BSCL.Normativa.BD::NOR.SolicitudCircular"
		(
		"IdSolicitudCircular",
		"IdTipoCircular",
		"IdDocumento",			
		"FechaIngresoSolicitud",			
		"FechaVencimientoSolicitud",	
		"IdUsuario",						
		"EstadoSolicitud",				
	    "Observaciones",
	    "IdTipoDocumento",						
		"IdCliente",						
		"FechaInicio",					
		"FechaFin",						
		"ListaRut",						
		"ListaRutAut", 					
		"ListaNemo",	
		"ListaRutEmpl",					
		"Periodicidad",					
		"RutaArchivo",					
		"NombreArchivo",					
		"NumeroRegistros")				
		(
		select
		v_SecuenciaSolicitud as IdSolicitudCircular,
		'MASIVOCLIENTE',
		v_idDoc,
		v_fechaSistema,
		v_FechaVencimientoSolicitud,
		p_IdUsuario,
		'Pendiente',
		v_Observaciones,
		'CERTCLI',
		0,
		v_fechaSistema,
		v_fechaSistema,
		0,
		0,
		0,
		0, 
		'',
		'',
		'',
		0
		from dummy
		);
		
		--ingresar documento
		INSERT INTO "BSCL.Normativa.BD::DOC.Documento"
			(	"IdDocumento",
		    	"IdTipoDocumento",
			    "Esquema",
			    "Version",
			    "IdEmpresa",
			    "FechaLimite",
			    "IdEstadoDocumento",
			    "FechaCreacion",
			    "FechaPresentacion",
			    "DocumentoIdentidadDeclarante",
			    "DocumentoIdentidadRepresentante",
			    "DireccionElectronicaDeclaracion",
			    "DireccionFisicaDeclaracion",
			    "ComunaDeclaracion",
			    "TelefonoDeclaracion",
			    "FolioDeclaracion",
			    "EnProceso",
			    "FechaInicioUltimoProceso",
			    "AnoMes"
				)
			(select 
			    v_idDoc as "IdDocumento",
			    'CERTCLI', 
			    'CERTCLI',            
			    'V1',                 
			    p_IdEmpresa AS "IdEmpresa",                    
			    '0001-01-01' as "FechaLimite",       
			    'PEN' as "IdEstadoDocumento",        
			    CURRENT_UTCTIMESTAMP as "FechaCreacion",     
			    '0001-01-01' as "FechaPresentacion", 
			    '' as "DocumentoIdentidadDeclarante",
			    '' as "DocumentoIdentidadRepresentante",
			    '' as "DireccionElectronicaDeclaracion",
			    '' as "DireccionFisicaDeclaracion",
			    '' as "ComunaDeclaracion",
			    '' as "TelefonoDeclaracion",
			    '' as "FolioDeclaracion",
			    'N' as "EnProceso",
			    null as "EnProcesoDesde",
			    '' AS "AnoMes"
			from DUMMY dm);
		
		v_TablaParametrosProgramaAUX = SELECT 'IdEmpresa' AS "NombreParametro", p_IdEmpresa AS "Valor" FROM DUMMY
							 UNION ALL SELECT 'SecuenciaSolicitud' AS "NombreParametro", v_SecuenciaSolicitud AS "Valor" FROM DUMMY
							 UNION ALL SELECT 'IdDocumento' AS "NombreParametro", v_idDoc AS "Valor" FROM DUMMY;
		
		insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
		("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
		select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IdCertificado', "IdCertificado" from :p_DatosMasivos;
		
		
		CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"(
		 p_IdEmpresa  
		,p_IdUsuario
		,'NOR' 
		,0
		,'CERTIFICADOS_CLIENTES_MASIVO'
		,0
		,:v_TablaParametrosProgramaAUX
		,'S'
		,:v_out_IdPrograma,p_out_AdvertenciasContador,:p_out_AdvertenciasDetalle,p_out_ErroresContador,:p_out_ErroresDetalle);

		p_out_Result := 'OK';
		/*
		SELECT "JsonData"
		FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarCertificadoCliente"
		WHERE "IdCertificadoCliente" = p_IdCertificadoCliente and "IdEmpresa" = p_IdEmpresa;
		*/		
	END;
END;