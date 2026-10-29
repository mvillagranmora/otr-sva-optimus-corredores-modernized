SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta";

--CALL "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta"
--(14,1,'Solicitud por Pauta','CIR1064','','','','','','',0,861711)
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta"
( 
    -- Parametros de Entrada
     
    IN p_IdUsuario							  BIGINT default 0,
    IN p_IdEmpresa  						  BIGINT default 0,
    IN p_Observaciones						  NVARCHAR(500) default '',
    IN p_IdTipoDocumento  					  NVARCHAR(10) default '',
    IN p_FechaInicio        				  NVARCHAR(10) default '0001-01-01',
    IN p_FechaTermino          				  NVARCHAR(10) default '2999-01-01',
    IN p_FechaLimite						  NVARCHAR(10) default '',
    IN p_FechaPresentacion					  NVARCHAR(10) default '',
    IN p_NombreArchivo						  NVARCHAR(20) default '',
    IN p_RutaArchivo						  NVARCHAR(30) default '',
    IN p_NumRegistros 						  BIGINT default 0,
    IN p_IdProceso 						      BIGINT default 0
    
    
/*
--input para creacion documento
   	IN	p_IdTipoDocumento   NVARCHAR(10) default '',
--	IN	p_Esquema			NVARCHAR(200) default '',
--	IN	p_Version			NVARCHAR(200) default '',
	IN	p_FechaLimite		NVARCHAR(10) default '',
	IN	p_IdEstadoDocumento NVARCHAR(200) default '',
	IN	p_FechaCreacion		NVARCHAR(10) default '',
	IN	p_FechaPresentacion	NVARCHAR(10) default '',
  */  

    -- Salida
  -- OUT p_out_resultado						  BIGINT
 /*  
   -- Parametros de Salida
   OUT p_out_AdvertenciasContador      INT,
   OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   OUT p_out_ErroresContador           INT,   
   OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
  */       
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
  

     DECLARE v_Secuencia 				INT DEFAULT 0; 
     DECLARE v_SecuenciaParam			INT DEFAULT 0; 
     DECLARE v_SecuenciaSolicitud		INT DEFAULT 0; 
     DECLARE v_idDoc 				    INT DEFAULT 0; 
     declare v_TablaParametrosPrograma      table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
     declare v_TablaParametrosProgramaAUX   table( "NombreParametro" nvarchar(100), "Valor" nvarchar(100));
     
	 DECLARE v_IdNormativa 								NVARCHAR(100) DEFAULT '';
	 DECLARE v_Esquema,v_Version, v_FechaVersion		NVARCHAR(10) DEFAULT '';
	 DECLARE v_IdTipoProgramaExportacion		  		NVARCHAR(40)  DEFAULT '';
	 
	 DECLARE v_count,v_countDocumento 					INT:=0;
 /*
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
		
	*/
		
	--------------------
	--LOGICA
	--------------------

	/* Obtencion de valores comunes */
	

	
	SELECT COUNT(1) INTO v_count 
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;

	IF(v_count=0) THEN
				--p_out_ErroresDetalle =  select 'TIPO_DOCUMENTO_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				--SIGNAL SALIDA_CONTROLADA; 	
	  ELSE
	  
	  	SELECT TOP 1 "IdNormativa", "Esquema","IdTipoProgramaExportacion" INTO v_IdNormativa,v_Esquema,v_IdTipoProgramaExportacion 
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;
			
		---Obtener version del esquema	
		
		IF p_FechaInicio='' OR p_FechaInicio='0001-01-01' THEN
			v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
		ELSE --Se obtiene segun fecha 
			v_FechaVersion :=TO_VARCHAR (p_FechaInicio, 'DD-MM-YYYY');
		END IF;	
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);

		IF(v_count=0) THEN
			--	p_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
			--	SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;

	END IF;



--obtiene secuencia documento y solicitud circular


SELECT COUNT(1) INTO v_countDocumento FROM "BSCL.Normativa.BD::DOC.Documento"
 WHERE "IdTipoDocumento"=p_IdTipoDocumento
 AND  "Esquema"=v_Esquema
 AND  "Version"=v_Version
 AND  "IdEmpresa"=p_IdEmpresa
 AND  "FechaLimite"=p_FechaLimite
 AND  "FechaPresentacion"=p_FechaPresentacion;

IF v_countDocumento=0 THEN
	select "BSCL.Normativa.BD.Secuencias::Documento".NEXTVAL into v_idDoc from dummy;
	ELSE
		SELECT TOP 1 "IdDocumento" INTO v_idDoc FROM "BSCL.Normativa.BD::DOC.Documento"
		 WHERE "IdTipoDocumento"=p_IdTipoDocumento
		 AND  "Esquema"=v_Esquema
   	     AND  "Version"=v_Version
		 AND  "IdEmpresa"=p_IdEmpresa
		 AND  "FechaLimite"=p_FechaLimite
		 AND  "FechaPresentacion"=p_FechaPresentacion ORDER BY "IdDocumento" DESC;
END IF;

Select "BSCL.Normativa.BD.Secuencias::SolicitudCircular".NEXTVAL into v_SecuenciaSolicitud from dummy;

--creación solicitud circular



	insert into "BSCL.Normativa.BD::NOR.SolicitudCircular"
	(
	"IdSolicitudCircular",
	"IdTipoCircular",
	--"IdEmpresa",
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
	"NumeroRegistros",
	"EsPauta")				
	(
	select
	v_SecuenciaSolicitud as IdSolicitudCircular,
	v_IdNormativa,
	--p_IdEmpresa,
	v_idDoc,
	(select Top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema'),
	(select Top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema'),
	p_IdUsuario,
	'Asignado',
	p_Observaciones,
	p_IdTipoDocumento,
	0,
	(select Top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema'),
	(select Top 1 "Valor" from "BSCL.Plataforma.BD::PLA.Configuracion" where "CodigoParametro" = 'PLA.FechaSistema'),
	0 ,
	0,
	0,
	0, 
	'',
	p_RutaArchivo,
	p_NombreArchivo,
	p_NumRegistros,
	'S'
	from dummy
	);
	

/*
p_out_resultado := v_SecuenciaSolicitud;

select IFNULL(p_out_resultado,0) from dummy;
*/

select IFNULL(v_SecuenciaSolicitud,0) as "IdSolicitudCircular" from dummy;


END;

/*
CALL "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta"
(14,1,'CIR1064','CIR1064-860296.txt','C:\ArchivosPrueba\CIR1064-860296.txt',7729,860296)


CALL %s.%s(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)
  
CALL "BSCL.Normativa.BD.Servicios.CIR1064::CrearSolicitudCircularPauta"(14,1,'Solicitud por Pauta','CIR1064','','','','','CIR1064-860314.txt','C:\ArchivosPrueba\CIR1064-860314.txt',7729)


    IN p_IdUsuario							  BIGINT default 0,
    IN p_IdEmpresa  						  BIGINT default 0,
    IN p_Observaciones						  NVARCHAR(500) default '',
    IN p_IdTipoDocumento  					  NVARCHAR(10) default '',
    IN p_FechaInicio        				  NVARCHAR(10) default '0001-01-01',
    IN p_FechaTermino          				  NVARCHAR(10) default '2999-01-01',
    IN p_FechaLimite						  NVARCHAR(10) default '',
    IN p_FechaPresentacion					  NVARCHAR(10) default ''
    IN p_NombreArchivo						  NVARCHAR(20) default ''
    IN p_RutaArchivo						  NVARCHAR(30) default ''
    IN p_NumRegistros 						  BIGINT default 0,
    IN p_IdProceso 						  BIGINT default 0,*/