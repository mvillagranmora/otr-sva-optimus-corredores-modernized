SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CER::EjecutarCertificadoCliente";
-- 
--  Proposito: Servicio que ejecuta unitariamente el certificado desde el JOB segun tipo de documento (Generacion masiva)
--  Autor: - CFLORES
--  Fecha Creacion: 22/11/2016
--  

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.CER::EjecutarCertificadoCliente"
( 
    -- Parametros de Entrada
	IN p_IdEmpresa							  BIGINT,
	IN p_IdSolicitudCircular 		  		  BIGINT DEFAULT 0,	
	IN p_IdTipoDocumento  					  NVARCHAR(10),
    IN p_Periodo        				 	  NVARCHAR(6),   ---YYYYMM 
    IN p_IdCliente							  BIGINT,
    IN p_IdPortafolio						  BIGINT
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
	DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	DECLARE v_Esquema,v_Version, v_FechaVersion		NVARCHAR(10) DEFAULT '';
	DECLARE v_FechaInicio,v_FechaFinMes				NVARCHAR(10):='';
	DECLARE v_count 					INT:=0;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		
	END;
	BEGIN
	INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor")VALUES (2);
	END;
	

	SELECT TOP 1  "Esquema" INTO v_Esquema
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
				v_out_ErroresDetalle =  select 'VERSION_ESQUEMA_NO_CONFIGURADA' as "Codigo",'' as "Descripcion" from dummy;
				SIGNAL SALIDA_CONTROLADA; 	
	  	ELSE
	  		SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"(:p_IdTipoDocumento,:v_FechaVersion);
  		END IF;
  		
  		--CAMPOS DE SALIDA PARA EL JOB
  		---"IdCliente","IdPortafolio", "IdTipoDocumento"
		SELECT 0 AS "IdCliente",0 AS "IdPortafolio",'' AS  "IdTipoDocumento" FROM DUMMY WHERE 1=2;
  		
  		----OBTENCION DE DATOS SEGUN CERTIFICADO
  	--	IF p_IdTipoDocumento='CIRCCarta' THEN
  			SELECT LAST_DAY (TO_DATE(v_FechaInicio, 'YYYY-MM-DD')) "last day" INTO  v_FechaFinMes FROM DUMMY;
  			/*
			CALL "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia"
			(
				
			    p_IdEmpresa				=> p_IdEmpresa,
			    p_IdCliente				=> :p_IdCliente,
			    p_IdPortafolio			=> :p_IdPortafolio,
			    p_FechaConsulta			=> TO_VARCHAR (v_FechaFinMes, 'YYYY-MM-DD'),
			    p_FormaSalida			=> 'CARTA'
			);
*/

CALL "BSCL.Normativa.BD.Servicios::CIR.CertificadoCustodia"
			(
				
			    p_IdEmpresa				=> 1,
			    p_IdCliente				=> 395174,
			    p_IdPortafolio			=> 343710,
			    p_FechaConsulta			=> '2016-11-30',
			    p_FormaSalida			=> 'CARTA'
			);

			
  --		END IF;



END;
