SET SCHEMA "BSCL";  

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CER::ObtenerListaClientesCertificado";
-- 
--  Proposito: Servicio que obtiene la lista de clientes segun certificado para la generacion masiva
--  Autor: - CFLORES
--  Fecha Creacion: 22/11/2016
--  

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.CER::ObtenerListaClientesCertificado"
( 
    -- Parametros de Entrada
	IN p_IdEmpresa							  BIGINT,	
	IN p_IdTipoDocumento  					  NVARCHAR(10),
    IN p_Periodo        				 	  NVARCHAR(6)   ---YYYYMM 
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
	/*
	BEGIN
	INSERT INTO "BSCL.Normativa.BD::CIR.Paso" ("Valor")VALUES (1);
	END;
	
*/

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
		--SELECT 0 AS "IdCliente",0 AS "IdPortafolio",'' AS  "IdTipoDocumento" FROM DUMMY WHERE 1=2;
  		
  		IF p_IdTipoDocumento='CIRCCarta' THEN
			SELECT NOM."IdCliente",NOM."IdPortafolio",'CIRCCarta' AS "IdTipoDocumento" from
			"BSCL.Normativa.BD.Vistas::CIR.ConsultarNomina"  AS NOM
			WHERE   NOM."Notificar"='SI' AND NOM."Periodo"=p_Periodo; 
  		END IF;



END;
