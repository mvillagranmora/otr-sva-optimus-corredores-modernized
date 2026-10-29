SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR::SolicitudCircularizacion";


CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.CIR::SolicitudCircularizacion"
( 
    -- Parametros de Entrada
    IN p_IdModulo					          NVARCHAR(3),
    IN p_IdUsuario							  BIGINT default 0,
    IN p_IdEmpresa  						  BIGINT default 0,
     
    IN p_FechaIngresoSolicitud				  DATE		default '1900-01-01',--NVARCHAR(10)  default '1900-01-01',

    IN p_Periodo        				 	  NVARCHAR(6) default '',   ---YYYYMM 
	IN p_TodosRut           				  BIGINT default 0,
    IN p_IdCliente							  BIGINT default 0,
    
    --Tabla de parametros
	--IN  p_RegistroParametro		  TABLE("Codigo" 					NVARCHAR(40), 
	--									"Valor" 					NVARCHAR(100)),-- parametro estandar
    --listas    
    IN p_ListaRut           				  TABLE("Rut"     NVARCHAR(15)) ,
    
--input para creacion documento
   	IN	p_IdTipoDocumento   NVARCHAR(10) default '',
	
--input para ingreso tarea/programa
	
	 IN  p_TablaParametros     TABLE("NombreParametro" nvarchar(100) , "Valor" nvarchar(100)),
    -- Salida
    OUT p_out_resultado						  BIGINT,
   
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
         
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  
     DECLARE countListRut 				INT DEFAULT 0; 
     DECLARE v_Secuencia 				INT DEFAULT 0; 
     DECLARE v_SecuenciaParam			INT DEFAULT 0; 
     DECLARE v_SecuenciaSolicitud		INT DEFAULT 0; 
     DECLARE v_idDoc 				    INT DEFAULT 0; 
     declare v_TablaParametrosPrograma      table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
     declare v_TablaParametrosProgramaAUX   table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
     
	 DECLARE v_IdNormativa 								NVARCHAR(100) DEFAULT '';
	 DECLARE v_Esquema,v_Version, v_FechaVersion		NVARCHAR(10) DEFAULT '';
	 DECLARE v_IdTipoProgramaProceso		  			NVARCHAR(40)  DEFAULT '';
	 
	 DECLARE v_count,v_countDocumento 					INT:=0;
	 DECLARE v_CreaParam		   		NVARCHAR(1):='S';
	 DECLARE v_FechaInicio				NVARCHAR(10):='';
   	--------------------
	--DECLARE Manejo de Errores
	--------------------	  
	DECLARE SALIDA_CONTROLADA 			CONDITION; 
	DECLARE SALIDA_CONTROLADA_NO_DATA 	CONDITION;
	DECLARE v_errorcuenta 				INT DEFAULT 0;  
	DECLARE v_errorhashmsg 				TABLE ("Codigo" NVARCHAR(100),"Valor" NVARCHAR(5000));
	DECLARE v_erroroutmsg  				NVARCHAR(5000);		
	DECLARE v_ErrorNoData  				NVARCHAR(100);
	DECLARE v_resultadoRegla 			NVARCHAR(40);
	DECLARE v_ErroresDetalle			TABLE ("Codigo" NVARCHAR(40),"Descripcion" NVARCHAR(520));
    DECLARE v_DocIdentidad  			NVARCHAR(100);
	DECLARE v_fechaHoraSistema 							 NVARCHAR(40):='';	
	        	
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		SELECT count(1) into p_out_ErroresContador FROM :p_out_ErroresDetalle;
		SELECT count(1) into p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
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
		declare v_errorExt NVARCHAR(40);
		IF ::SQL_ERROR_CODE > 10000 THEN 
			--error pseudo-controlado pero desde un Sub-SP
			call "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (::SQL_ERROR_CODE,v_errorExt);			
			p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL  
								   SELECT v_errorExt,::SQL_ERROR_MESSAGE FROM dummy;				
			SELECT count(1) into p_out_ErroresContador 
			FROM :p_out_ErroresDetalle;										
		ELSE
			--Error interno no manejado  (El log será captado por la capa de XS control)		
			RESIGNAL; 
		END IF;		
	END;
	
	
	DECLARE EXIT HANDLER FOR SQL_ERROR_CODE 1299
		BEGIN
			v_ErrorNoData := 'DATA_NO_EXISTE';
			SIGNAL SALIDA_CONTROLADA_NO_DATA;
		END;
	

	p_out_AdvertenciasContador		:= 0;
	p_out_ErroresContador			:= 0;
	p_out_AdvertenciasDetalle		= SELECT '' as "Codigo",'' as "RequiereAutorizacion",'' as "Descripcion" FROM dummy where 1=2;
	p_out_ErroresDetalle			= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;  	
	p_out_ResultadoProceso			= SELECT '' as "Campo", 0 as "Id", '' as "Servicio", '' as "Resultado" FROM dummy where 1=2;
	v_ErroresDetalle				= SELECT '' as "Codigo",'' as "Descripcion" FROM dummy where 1=2;

	select count(1) into p_out_ErroresContador from :p_out_ErroresDetalle; 
		IF :p_out_ErroresContador > 0 THEN
			SIGNAL SALIDA_CONTROLADA;
		END IF;	
		
		
	--------------------
	--LOGICA
	--------------------

	IF(p_Periodo='') THEN
				p_out_ErroresDetalle =  select 'ERROR_PERIODO' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	END IF;
	  

	/* Obtencion de valores comunes */
	
	SELECT COUNT(1) INTO v_count 
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;

	IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'TIPO_DOCUMENTO_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  ELSE
	  
	  	SELECT TOP 1 "IdNormativa", "Esquema","IdTipoProgramaProceso" INTO v_IdNormativa,v_Esquema,v_IdTipoProgramaProceso 
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_Periodo='' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			SELECT  LEFT(p_Periodo,4) || '-' || RIGHT(p_Periodo,2) || '-01'  INTO v_FechaInicio FROM DUMMY;
			v_FechaVersion :=TO_VARCHAR (v_FechaInicio, 'DD-MM-YYYY');
			
		END IF;	
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);

		IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;

	END IF;
		
	call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
				p_IdEmpresa => p_IdEmpresa,
				p_out_FechaHoraSistema => v_fechaHoraSistema
	);	

	------------------------------------------------------------
	--VALIDACIONES ESPECIALES PARA LA NOMINA DE CIRCULARIZACION
	------------------------------------------------------------
	SELECT COUNT(1) INTO v_count  FROM  :p_ListaRut;
	IF v_count=0 AND p_IdCliente=0 AND p_TodosRut=0 THEN
		p_out_ErroresDetalle =  select 'CLIENTE_NO_EXISTE' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 
	END IF;


	------------------------------------------------------------
	-- OBTENER PARAMETROS DE CIRCULARIZACION
	------------------------------------------------------------
	select count(1) into countListRut     from :p_ListaRut;
	


--obtiene secuencia documento y solicitud circular

SELECT COUNT(1) INTO v_countDocumento FROM "BSCL.Normativa.BD::DOC.Documento"
 WHERE "IdTipoDocumento"=p_IdTipoDocumento
 AND  "Esquema"=v_Esquema
 AND  "Version"=v_Version
 AND  "IdEmpresa"=p_IdEmpresa
 AND  "AnoMes"=p_Periodo;

IF v_countDocumento=0 THEN
	select "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL into v_idDoc from dummy;
	ELSE
		SELECT TOP 1 "IdDocumento" INTO v_idDoc FROM "BSCL.Normativa.BD::DOC.Documento"
		 WHERE "IdTipoDocumento"=p_IdTipoDocumento
		 AND  "Esquema"=v_Esquema
		 AND  "Version"=v_Version
		 AND  "IdEmpresa"=p_IdEmpresa
		 AND  "AnoMes"=p_Periodo
		 ORDER BY "IdDocumento" DESC;
END IF;

Select "BSCL.Normativa.BD.Secuencias::SolicitudCircular".NEXTVAL into v_SecuenciaSolicitud from dummy;

--creación solicitud circular


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
	v_IdNormativa,
	v_idDoc,
	v_fechaHoraSistema,
	'1900-01-01',
	p_IdUsuario,
	'Pendiente',
	'',
	p_IdTipoDocumento,
	p_IdCliente,
	TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
	TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD'),
	case 
	when countListRut>0 then 1
	when countListRut=0 then 0
	else 0
	end,
	0,
	0,
	0, 
	'',
	'',
	'',
	0
	from dummy
	);
	

INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'EMP', p_IdEmpresa FROM DUMMY);

/*
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FINI', TO_VARCHAR (v_fechaHoraSistema, 'YYYY-MM-DD') from dummy);
 

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FTER', TO_VARCHAR (:v_fechaHoraSistema, 'YYYY-MM-DD') from dummy);
*/
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'USUA', p_IdUsuario from dummy);




--INGRESO parametros solicitud
--if listado rut

IF countListRut>0 THEN
	INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
	("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT_MAS', (ltrim(rtrim("LR"."Rut"))) FROM :p_ListaRut as "LR");
	--(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT_MAS', (ltrim(rtrim(substring("LR"."Rut",1,LENGTH("LR"."Rut")-2)))) FROM :p_ListaRut as "LR");
END IF;

IF p_IdCliente <>0 THEN
	--select top 1 (ltrim(rtrim(substring("DocumentoIdentidad",1,LENGTH("DocumentoIdentidad")-2)))) into v_DocIdentidad from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente" = p_IdCliente;
	
	--INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
	--("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	--(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT', v_DocIdentidad FROM dummy);
	
	INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
	("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDCLI', p_IdCliente FROM dummy);
END IF;

--TODOS RUT


IF p_TodosRut <>0 THEN
	INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
	("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TRUT', p_TodosRut from dummy);
END IF;


IF p_Periodo <> '' THEN
	INSERT INTO "BSCL.Normativa.BD::NOR.SolicitudParametros"
	("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
	(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'PERI', p_Periodo from dummy);
END IF; 




IF v_countDocumento=0 THEN
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
	select
	    v_idDoc as "IdDocumento",
	    p_IdTipoDocumento as "IdTipoDocumento", 
	    v_Esquema as "Esquema",            
	    v_Version as "Version",                 
	    p_IdEmpresa as "IdEmpresa",                    
	    '' as "FechaLimite",       
	    'PEN' as "IdEstadoDocumento",        
	    CURRENT_UTCTIMESTAMP as "FechaCreacion",     
	    '' as "FechaPresentacion", 
	    '' as "DocumentoIdentidadDeclarante",
	    '' as "DocumentoIdentidadRepresentante",
	    '' as "DireccionElectronicaDeclaracion",
	    '' as "DireccionFisicaDeclaracion",
	    '' as "ComunaDeclaracion",
	    '' as "TelefonoDeclaracion",
	    '' as "FolioDeclaracion",
	    'N' as "EnProceso",
	    null as "EnProcesoDesde",
	    p_Periodo AS "AnoMes"
	from dummy;
END IF;
--insercion de programa

v_TablaParametrosProgramaAUX = 
select 'IdSolicitudCircular' as "NombreParametro",to_varchar(v_SecuenciaSolicitud) as "Valor" from dummy
union all
select 'idDocumento' as "NombreParametro",to_varchar(v_idDoc) as "Valor" from dummy
union all
select 'IdTipoTarea' as "NombreParametro",v_IdTipoProgramaProceso as "Valor" from dummy
union all
select 'IdEmpresa' as "NombreParametro",to_varchar(p_IdEmpresa) as "Valor" from dummy
/*
select 'EsPauta' as "NombreParametro",'N' as "Valor" from dummy
union all
select 'NOMSAL' as "NombreParametro",'' as "Valor" from dummy
union all
select 'EXTSAL' as "NombreParametro",'' as "Valor" from dummy
union all
select 'COMSAL' as "NombreParametro",'' as "Valor" from dummy
union all
select 'NOMCOLSAL' as "NombreParametro",'' as "Valor" from dummy
union all
select 'LINPARSAL' as "NombreParametro",'0' as "Valor" from dummy
*/
union all
select "NombreParametro" as "NombreParametro", "Valor" as "Valor" from :p_TablaParametros;


CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
(p_IdEmpresa,p_IdUsuario,p_IdModulo,0,v_IdTipoProgramaProceso,0,:v_TablaParametrosProgramaAUX,v_CreaParam,out_no_ocupable,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);


END;

