
SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CER::CrearCertificadoCliente";
-- 
--  Proposito: Servicio que permite grabar el JSonData de los certificados invocado desde el JOB
--  Autor: - CFLORES
--  Fecha Creacion: 22/11/2016
--  
--  Modificacion: Se incluye forma masiva desde nomina de circularizacion
--  Autor: CFLORES - BCS
--  Fecha Modificacion: 29/08/2022

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.CER::CrearCertificadoCliente"
( 
    -- Parametros de Entrada
	IN p_IdEmpresa							  BIGINT,	
	IN p_IdTipoDocumento  					  NVARCHAR(10),
    IN p_Periodo        				 	  NVARCHAR(6),   ---YYYYMM 
    IN P_Certificados  TABLE (
								"IdCliente"							  BIGINT,	
								"IdPortafolio"						  BIGINT,	
								"JsonData"							  BLOB
							)
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
	DECLARE v_FechaInicio				NVARCHAR(10):='';
	DECLARE v_count 					INT:=0;
	DECLARE EXIT HANDLER FOR SALIDA_CONTROLADA	 
	BEGIN
		--salida/error/advertencia controlado por este servicio
		
	END;
		
	v_out_ErroresDetalle		= select '' as "Codigo",'' as "Descripcion" from dummy where 1=2;
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

		
		DELETE
		FROM "BSCL.Normativa.BD::CER.CertificadoCliente" AS CER 
		WHERE 
		EXISTS (SELECT 1 FROM  :P_Certificados AS NUE 
		   		 WHERE  CER."IdCliente"=NUE."IdCliente" AND CER."IdPortafolio"=NUE."IdPortafolio" 
					AND CER."IdTipoDocumento"=p_IdTipoDocumento
					AND CER."IdEmpresa"=p_IdEmpresa
					AND CER."Esquema"=v_Esquema
					AND CER."Anio"=LEFT(p_Periodo,4)
					AND CER."Periodo"=p_Periodo
					AND CER."Version"=v_Version);

		
		--IF v_count=0 THEN

				/*
				DELETE  FROM "BSCL.Normativa.BD::CER.CertificadoCliente" CER 
     			WHERE CER."IdTipoDocumento"=p_IdTipoDocumento
				AND CER."IdEmpresa"=p_IdEmpresa
				AND CER."Esquema"=v_Esquema
				AND CER."Anio"=LEFT(p_Periodo,4)
				AND CER."Periodo"=p_Periodo
				AND CER."Version"=v_Version;
			*/
			
		
				INSERT INTO "BSCL.Normativa.BD::CER.CertificadoCliente"
				("IdCertificadoCliente",
				"IdTipoDocumento",
				"IdEmpresa",
				"Esquema",
				"Anio",
				"Periodo",
				"IdCliente",
				"Version",
				"JsonData",  
				"IdPortafolio"
				)
				SELECT
				("BSCL.Normativa.BD.Secuencias::IdCertificadoCliente".NEXTVAL) 	 AS  "IdCertificadoCliente",
				p_IdTipoDocumento,
				p_IdEmpresa,
				v_Esquema,
				LEFT(p_Periodo,4) AS "Anio",
				p_Periodo,
				NUE."IdCliente",
				v_Version,
				NUE."JsonData",
				NUE."IdPortafolio"
				FROM :P_Certificados NUE;
				
			--END IF; 
		
		select 'OK' from DUMMY;



END;

