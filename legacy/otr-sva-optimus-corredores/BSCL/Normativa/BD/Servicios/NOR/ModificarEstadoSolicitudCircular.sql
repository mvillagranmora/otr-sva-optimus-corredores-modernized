SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::ModificarEstadoSolicitudCircular"
( 
    -- Parametros de Entrada
	IN p_IdSolicitudCircular 		  		  BIGINT DEFAULT 0, 
  	IN p_NombreArchivo           			  NVARCHAR(200) default '',
    IN p_RutaArchivo           			      NVARCHAR(200) default '',
    IN p_NumRegistros						  BIGINT default 0,
    IN p_IdProceso   						  BIGINT default 0,
    IN p_RutaArchivoLog           			  NVARCHAR(200) default '',
	IN p_NumRegistrosLog					  BIGINT default 0

    -- Salida
  -- OUT p_out_resultado						  NVARCHAR(500),
   
   -- Parametros de Salida
   --OUT p_out_AdvertenciasContador      INT,
   --OUT p_out_AdvertenciasDetalle       TABLE ("Codigo" nvarchar(100),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(5000)),	
   --OUT p_out_ErroresContador           INT,   
   --OUT p_out_ErroresDetalle            TABLE ("Codigo" nvarchar(100),"Descripcion" nvarchar(5000))
         
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
  BEGIN
  
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
  	DECLARE v_rutaArchivoSistema,v_rutaArchivoNormativo,v_rutaArchivoCompuesta,v_rutaArchivoCompuestaLog NVARCHAR(5000);		
	declare v_Secuencia int;
	DECLARE v_IdDocumento,v_IdEmpresa ,v_CondLog INT:=0;
	

SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
FROM "BSCL.Normativa.BD::DOC.Documento"
WHERE "IdDocumento" = v_IdDocumento;


---Ruta archivo de descarga
SELECT "Valor" INTO v_rutaArchivoSistema
FROM "BSCL.Plataforma.BD::PLA.Configuracion"
WHERE "CodigoParametro" = 'PLA.RutaBaseArchivos' AND "IdEmpresa"=v_IdEmpresa;


SELECT "Valor" INTO v_rutaArchivoNormativo 
FROM "BSCL.Normativa.BD::NOR.Configuracion"
WHERE "CodigoParametro"='SubRutaArchivosModulo'  AND "IdEmpresa"=v_IdEmpresa;


SELECT TOP 1 RTRIM(v_rutaArchivoSistema) || RTRIM(v_rutaArchivoNormativo) || RTRIM(p_NombreArchivo) INTO v_rutaArchivoCompuesta FROM DUMMY;

--SELECT TOP 1 RTRIM(v_rutaArchivoSistema) || RTRIM(v_rutaArchivoNormativo) || RTRIM(p_RutaArchivoLog) INTO v_rutaArchivoCompuestaLog FROM DUMMY;
SELECT TOP 1  RTRIM(p_RutaArchivoLog) INTO v_rutaArchivoCompuestaLog FROM DUMMY;


select count(1) into v_CondLog from "BSCL.Normativa.BD::NOR.LogNormativa" where "IdSolicitudCircular" = p_IdSolicitudCircular;

--se actualiza lognormativo , asignandole IdProceso correspondiente
IF(v_CondLog >0)THEN

update "BSCL.Normativa.BD::NOR.LogNormativa" set "IdProceso" = IFNULL(p_IdProceso,0) where "IdSolicitudCircular" = p_IdSolicitudCircular;

END IF;


IF(p_NumRegistros <>0)THEN
	--inactiva solicitudes con el mismo archivo
	update "BSCL.Normativa.BD::NOR.SolicitudCircular" 
	set "IdEstado" = 'INA' 
		where "RutaArchivo" = v_rutaArchivoCompuesta;
		
	update "BSCL.Normativa.BD::NOR.SolicitudCircular" 
	set "IdEstado" = 'TER', 
		"RutaArchivo" = v_rutaArchivoCompuesta,					
		"NombreArchivo" = p_NombreArchivo,					
		"NumeroRegistros" = p_NumRegistros,
		"IdProceso" = p_IdProceso,
		--"RutaLog" = v_rutaArchivoCompuestaLog,
		"NumeroRegistrosLog" = p_NumRegistrosLog
		where "IdSolicitudCircular" = p_IdSolicitudCircular;
END IF;

IF(p_NumRegistros=0) THEN

update "BSCL.Normativa.BD::NOR.SolicitudCircular" 
set "IdEstado" = 'TER', 
	"RutaArchivo" = '',					
	"NombreArchivo" = 'Sin Datos',					
	"NumeroRegistros" = 0,
	"IdProceso" = p_IdProceso,
	"RutaLog" = '',
	"NumeroRegistrosLog" = p_NumRegistrosLog
	where "IdSolicitudCircular" = p_IdSolicitudCircular;

END IF;

IF(p_IdSolicitudCircular <>0) THEN
	select 'OK' from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdSolicitudCircular" = p_IdSolicitudCircular;

ELSEIF(p_IdSolicitudCircular=0)THEN

	select 'NOKSOL' from "BSCL.Normativa.BD::NOR.SolicitudCircular" where "IdSolicitudCircular" = p_IdSolicitudCircular;

END IF;
--select * from "BSCL.Normativa.BD::NOR.SolicitudCircular" order by "IdSolicitudCircular" desc


END;