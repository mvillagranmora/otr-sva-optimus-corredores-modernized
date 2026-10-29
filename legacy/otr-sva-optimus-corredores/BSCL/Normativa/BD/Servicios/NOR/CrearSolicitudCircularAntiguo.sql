SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CrearSolicitudCircularAntiguo";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CrearSolicitudCircularAntiguo"
( 
    -- Parametros de Entrada
     
   -- IN p_IdTipoCircular                       NVARCHAR(100) default '',
    IN p_FechaIngresoSolicitud				  DATE		default '1900-01-01',--NVARCHAR(10)  default '1900-01-01',
    IN p_FechaVencimientoSolicitud			  NVARCHAR(10) default '1900-01-01',
    IN p_IdUsuario							  BIGINT default 0,
    IN p_IdEmpresa  						  BIGINT default 0,
    IN p_Observaciones						  NVARCHAR(500) default '',
    IN p_IdCliente							  BIGINT default 0,
    IN p_FechaInicio        				  NVARCHAR(10) default '0001-01-01',
    IN p_FechaTermino          				  NVARCHAR(10) default '2999-01-01',
    IN p_FechaRealizacion     				  NVARCHAR(10) default '2999-01-01',
    IN p_Oficio                               NVARCHAR(100) default '',
    IN p_Mercado                              NVARCHAR(100) default '',
    IN p_Nemo                                 NVARCHAR(100) default '',
    IN p_IdEmpleado 						  BIGINT default 0,
    IN p_IdSuc 						  		  BIGINT default 0,
    IN p_RutEmpleado 						  NVARCHAR(100) default '',
    IN p_TipoInformacion					  NVARCHAR(3) 	Default '',
    IN p_IdClasificacionTipoInstrumento		  NVARCHAR(3) 	Default '',
    IN p_IdEmisor			       			  NVARCHAR(40)	Default '',
   	IN p_IdInstrumento						  BIGINT		Default 0,
    IN p_Relacion							  NVARCHAR(10) 	Default '',
    IN p_RutAut      						  NVARCHAR(100) default '',
    IN p_Monto		 						  DECIMAL(27,4)	DEFAULT 0,
    --Fk
    --IN p_FechaInicio 			
	IN p_MontoMin							  DECIMAL(27,4)	DEFAULT -1,	
	IN p_MontoMax							  DECIMAL(27,4)	DEFAULT -1,	
	IN p_FecEjec							  NVARCHAR(30) default '',--'0001-01-01',	
	IN p_FecEnrol							  NVARCHAR(30) default '',--'0001-01-01',	
	IN p_UsoFecha							  NVARCHAR(10) default '',	
	IN p_FecDesde							  NVARCHAR(30) default '',--'0001-01-01',	
	IN p_FecHasta							  NVARCHAR(30) default '',--'9999-01-01',	
	IN p_UsoFechaInd						  NVARCHAR(10) default '',	
	IN p_ClasificacionFatca 				  NVARCHAR(30) 	Default '',	
	IN p_IndicadorFatca 		              NVARCHAR(30) 	Default '',
	IN p_TipoPersona						  NVARCHAR(30) 	Default '',	
	IN p_registroIndicios 					  NVARCHAR(10) default '',	
    
    
    --listas    
    IN p_ListaRut           				  TABLE("Rut"     NVARCHAR(15)) ,
    IN p_ListaRutAut           				  TABLE("RutAut"  NVARCHAR(15)) ,
    IN p_ListaNemo           				  TABLE("Nemo"    NVARCHAR(15)),
    IN p_ListaRutEmpl          				  TABLE("Rut"     NVARCHAR(20)),
    
    --check todos
    IN p_TodosAut           				  BIGINT default -1,
    IN p_TodosNemo           				  BIGINT default -1,
    IN p_TodosRut           				  BIGINT default -1,
    IN p_TodosEmpl           				  BIGINT default -1,
    IN p_TodosRel           				  BIGINT default -1,
    
   -- IN p_TodosCli           				  BIGINT default 0,
    
    IN p_EsPauta 							 NVARCHAR(10) Default 'N',
    
    

--input para creacion documento
   	IN	p_IdTipoDocumento   NVARCHAR(10) default '',
--	IN	p_Esquema			NVARCHAR(200) default '',
--	IN	p_Version			NVARCHAR(200) default '',
	IN	p_FechaLimite		NVARCHAR(10) default '',
	IN	p_IdEstadoDocumento NVARCHAR(200) default '',
	IN	p_FechaCreacion		NVARCHAR(10) default '',
	IN	p_FechaPresentacion	NVARCHAR(10) default '',
    
--input para ingreso tarea/programa
						
	 IN  p_IdModulo            NVARCHAR(3),
	 IN	 p_IdAgenda	 		   BIGINT default 0, 
	 IN	 p_Segundos			   BIGINT default 0,
	 IN  p_TablaParametros     TABLE("NombreParametro" nvarchar(100) , "Valor" nvarchar(100)),
	 IN  p_CreaParam		   NVARCHAR(1),

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
     DECLARE countListRutAut 			INT DEFAULT 0; 
     DECLARE countListNemo				INT DEFAULT 0; 
     DECLARE countListRutEmpl			INT DEFAULT 0; 
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
	DECLARE v_fechaHoraSistema NVARCHAR(40);	
	        	
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

	/* Obtencion de valores comunes */
	
	SELECT COUNT(1) INTO v_count 
			FROM "BSCL.Normativa.BD::DOC.TipoDocumento"
			WHERE "IdTipoDocumento" = p_IdTipoDocumento;

	IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'TIPO_DOCUMENTO_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
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
		
		--Solo si son las instrucciones de la 380, se ocupa la fecha de realización, ya que no se ocupa rango de fechas
		IF p_IdTipoDocumento='NCG380INS' THEN
			v_FechaVersion :=TO_VARCHAR (p_FechaRealizacion, 'DD-MM-YYYY');
		END IF;
		
		SELECT COUNT(1) INTO v_count FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
		
		IF(v_count=0) THEN
				p_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",:v_erroroutmsg as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;

	END IF;



select count(1) into countListRut     from :p_ListaRut;
select count(1) into countListRutAut  from :p_ListaRutAut;
select count(1) into countListNemo    from :p_ListaNemo;
select count(1) into countListRutEmpl from :p_ListaRutEmpl;

--v_TablaParametrosPrograma = select "NombreParametro" as "NombreParametro", "Valor" as "Valor" from :p_TablaParametros;

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


		call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
			p_IdEmpresa => p_IdEmpresa,
			p_out_FechaHoraSistema => v_fechaHoraSistema
		);




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
	"EsPauta",
	"FechaIngreso",
	"FechaCreacion",
	"FechaModificacion",
	"IdEstado" )				
	(
	select
	v_SecuenciaSolicitud as IdSolicitudCircular,
	v_IdNormativa,
	--p_IdEmpresa,
	v_idDoc,
	v_fechaHoraSistema,
	p_FechaVencimientoSolicitud,
	p_IdUsuario,
	'Pendiente',
	p_Observaciones,
	p_IdTipoDocumento,
	p_IdCliente,
	p_FechaInicio,
	p_FechaTermino,
	case 
	when countListRut>0 then 1
	when countListRut=0 then 0
	else 0
	end ,
	case 
	when countListRutAut>0 then 1
	when countListRutAut=0 then 0
	else 0
	end ,
	case 
	when countListNemo>0 then 1
	when countListNemo=0 then 0
	else 0
	end ,
	case 
	when countListRutEmpl>0 then 1
	when countListRutEmpl=0 then 0
	else 0
	end, 
	'',
	'',
	'',
	0,
	p_EsPauta,
	v_fechaHoraSistema,
	CURRENT_UTCTIMESTAMP,
	CURRENT_UTCTIMESTAMP,
	'PEN'
	from dummy
	);
	

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'EMP', p_IdEmpresa from DUMMY);


--INGRESO parametros solicitud
--if listado rut

if countListRut>0
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT', (ltrim(rtrim(substring("LR"."Rut",1,LENGTH("LR"."Rut")-2)))) from :p_ListaRut as "LR");
end if;

--if listado rut aut
IF countListRutAut>0
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUTAUT', (ltrim(rtrim(substring("LRA"."RutAut",1,LENGTH("LRA"."RutAut")-2)))) from :p_ListaRutAut as "LRA");
end if; 

--if listado nemo
if countListNemo>0
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'NEMO', "LN"."Nemo" from :p_ListaNemo as "LN");
end if; 


--if listado rut empleado
if countListRutEmpl>0
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUTEMPL', (ltrim(rtrim(substring("LRE"."Rut",1,LENGTH("LRE"."Rut")-2))))  from :p_ListaRutEmpl as "LRE");
end if; 


insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDCLI', IFNULL(p_IdCliente,0) from dummy);


if p_IdCliente <>0
then
select top 1 (ltrim(rtrim(substring("DocumentoIdentidad",1,LENGTH("DocumentoIdentidad")-2)))) into v_DocIdentidad from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente" = p_IdCliente;

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUT', v_DocIdentidad from dummy);



end if; 

if p_RutEmpleado <>''
then

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUTEMPL', (ltrim(rtrim(substring(p_RutEmpleado,1,LENGTH(p_RutEmpleado)-2)))) from dummy);

end if; 


if p_RutAut <>''
then

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RUTAUT', (ltrim(rtrim(substring(p_RutAut,1,LENGTH(p_RutAut)-2)))) from dummy);

end if; 

IF p_Monto >0  THEN

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'MONTO', CAST (p_Monto AS NVARCHAR(100))  from dummy);

END IF;


IF p_IdEmpleado <>0  THEN

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDEMPLEADO', CAST (p_IdEmpleado AS NVARCHAR(100))  from dummy);

END IF;

if p_FechaInicio <> '1900-01-01'
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FINI', p_FechaInicio from dummy);

end if; 

if p_FechaTermino <> '2999-01-01'
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FTER', p_FechaTermino from dummy);

end if; 

if p_FechaRealizacion <> '2999-01-01'
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FREA', p_FechaRealizacion from dummy);

end if; 

if p_Oficio <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'OFI', p_Oficio from dummy);

end if; 

if p_Mercado <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'MCDO', p_Mercado from dummy);
/*
else

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'MCDO', '0' from dummy);
*/
end if; 

if p_Nemo <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'NEMO', p_Nemo from dummy);

end if; 

if p_TipoInformacion <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TIPINF', p_TipoInformacion from dummy);

end if; 

if p_IdClasificacionTipoInstrumento <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'CLAINS', p_IdClasificacionTipoInstrumento from dummy);

end if; 

IF p_IdSuc <>0  THEN

insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDSUCURSAL', CAST (:p_IdSuc AS NVARCHAR(100))  from dummy);

END IF;

if p_IdEmisor <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDEMI', p_IdEmisor from dummy);

end if; 

if p_IdInstrumento <> 0
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IDINS', p_IdInstrumento from dummy);

end if; 

if p_Relacion <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RELA', p_Relacion from dummy);

end if; 

--TODOS CLI
/*
p_TodosCli
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TAUT', p_TodosAut from dummy);
*/

--TODOS AUT
if p_TodosAut <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TAUT', p_TodosAut from dummy);
end if; 

--TODOS RUT


if p_TodosRut <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TRUT', p_TodosRut from dummy);
end if; 


--TODOS NEMO
if p_TodosNemo <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TNEMO', p_TodosNemo from dummy);
end if;

--TODOS EMPLEADO
if p_TodosEmpl <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TEMPL', p_TodosEmpl from dummy);
end if;
--TODOS RELACIONES
if p_TodosRel <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TREL', p_TodosRel from dummy);
end if;



-------------------------------------------------------------------FATCA

if p_MontoMin <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'MMIN', CAST (p_MontoMin AS NVARCHAR(100))  from dummy);
end if;

if p_MontoMax <> -1
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'MMAX', CAST (p_MontoMax AS NVARCHAR(100)) from dummy);
end if;

--if (p_FecEjec <> '' and p_FecEjec <> '0001-01-01')
if (p_FecEjec <> '')
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FEJEC', p_FecEjec from dummy);
end if;
--if  (p_FecEnrol <> '' and p_FecEnrol <> '0001-01-01')
if  (p_FecEnrol <> '')
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FENR', p_FecEnrol from dummy);
end if;
if p_UsoFecha <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'UFEC', p_UsoFecha from dummy);
end if;
--if (p_FecDesde <> '' and p_FecDesde <> '0001-01-01')
if (p_FecDesde <> '')
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FEDES', p_FecDesde from dummy);
end if;
--if  (p_FecHasta <> '' and p_FecHasta <> '9999-01-01')
if  (p_FecHasta <> '')
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'FEHAS', p_FecHasta from dummy);
end if;

if p_UsoFechaInd <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'UFEC', p_UsoFechaInd from dummy);
end if;
if p_ClasificacionFatca <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'CFAT', p_ClasificacionFatca from dummy);
end if;
if p_IndicadorFatca <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'IFAT', p_IndicadorFatca from dummy);
end if;

if p_TipoPersona <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'TP', p_TipoPersona from dummy);
end if;

if p_RegistroIndicios <> ''
then
insert into "BSCL.Normativa.BD::NOR.SolicitudParametros"
("IdSolicitudParametros", "IdSolicitudCircular", "TipoParametro","Valor")
(select "BSCL.Normativa.BD.Secuencias::SolicitudParametros".NEXTVAL, v_SecuenciaSolicitud,'RIND', p_RegistroIndicios from dummy);
end if;

-----------------------------------fin insercion parametros FATCA				







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
	    "FechaInicioUltimoProceso"	   
		)
	
	select
	    v_idDoc as "IdDocumento",
	    p_IdTipoDocumento as "IdTipoDocumento", 
	    v_Esquema as "Esquema",            
	    v_Version as "Version",                 
	    p_IdEmpresa as "IdEmpresa",                    
	    p_FechaLimite as "FechaLimite",       
	    p_IdEstadoDocumento as "IdEstadoDocumento",        
	    p_FechaCreacion as "FechaCreacion",     
	    p_FechaPresentacion as "FechaPresentacion", 
	    '' as "DocumentoIdentidadDeclarante",
	    '' as "DocumentoIdentidadRepresentante",
	    '' as "DireccionElectronicaDeclaracion",
	    '' as "DireccionFisicaDeclaracion",
	    '' as "ComunaDeclaracion",
	    '' as "TelefonoDeclaracion",
	    '' as "FolioDeclaracion",
	    'N' as "EnProceso",
	    null as "EnProcesoDesde"
	from dummy;
END IF;
--insercion de programa

v_TablaParametrosProgramaAUX = 
select 'IdSolicitudCircular' as "NombreParametro",to_varchar(v_SecuenciaSolicitud) as "Valor" from dummy
union all
select 'idDocumento' as "NombreParametro",to_varchar(v_idDoc) as "Valor" from dummy
union all
select 'EsPauta' as "NombreParametro",to_varchar(p_EsPauta) as "Valor" from dummy
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
union all
select 'DESCCOL' as "NombreParametro",'NOM' as "Valor" from dummy
union all
select "NombreParametro" as "NombreParametro", "Valor" as "Valor" from :p_TablaParametros;


CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarPrograma"
(p_IdEmpresa,p_IdUsuario,p_IdModulo,p_IdAgenda,v_IdTipoProgramaExportacion,p_Segundos,:v_TablaParametrosProgramaAUX,p_CreaParam,out_no_ocupable,p_out_AdvertenciasContador,p_out_AdvertenciasDetalle,p_out_ErroresContador,p_out_ErroresDetalle);


p_out_resultado := 1;

END;

