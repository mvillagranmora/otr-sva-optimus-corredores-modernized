SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CrearResumenGeneracion";

--  Proposito: Activa una version los esquemas seleccionados.
--  Fecha Creacion: 19/10/2021
--SELECT * FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion" ORDER BY 1 DESC
				  
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.CrearResumenGeneracion"(
	IN p_IdEmpresa 								BIGINT,
	IN p_IdUsuario 								BIGINT,
	IN p_IdModulo 								NVARCHAR(3),
	IN p_IdEsquema								NVARCHAR(10),
    IN p_Version								NVARCHAR(10),
    IN p_JobId									BIGINT,
    IN p_NombreArchivo							NVARCHAR(200),
    IN p_Glosa									NVARCHAR(200),
    IN p_TotalRegistrosGenerados				BIGINT
)

	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
		
		BEGIN
		DECLARE v_resultadoRegla 			NVARCHAR(40);
		DECLARE v_count						BIGINT;
		DECLARE v_countVersiones			BIGINT;
		DECLARE v_countRepetidos 			BIGINT;
		DECLARE v_FechaSistema				NVARCHAR(10);
		DECLARE v_countEsquemasValidados	BIGINT;
		DECLARE v_idDoc,v_idResumen,v_idExiste	    INT DEFAULT 0;
		
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.CrearRegistroCertificado','INICIO','INICIO');
		
		CALL "BSCL.Plataforma.BD.Reglas::ServicioCumpleRequisitosBasicos"(:p_IdEmpresa,:p_IdUsuario,:p_IdModulo,:v_out_ErroresDetalle_Basicos,:v_resultadoRegla);
		
	
		SELECT TOP 1 "IdDocumento" INTO v_idDoc FROM "BSCL.Normativa.BD::DOC.Documento"
				 WHERE "IdTipoDocumento"='DECLACORR'
				 AND  "Esquema"=p_IdEsquema
		   	     AND  "Version"=p_Version
				 AND  "IdEmpresa"=p_IdEmpresa;

		SELECT COALESCE(MAX("ID"),0)+1 INTO v_idResumen FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion";
	
		SELECT count(1) INTO v_idExiste FROM "BSCL.Normativa.BD::TRI.ResumenGeneracion" WHERE "IdProceso"=p_JobId AND "IdDocumento"=v_idDoc;
	
	    IF v_idExiste=0 THEN
			INSERT INTO "BSCL.Normativa.BD::TRI.ResumenGeneracion"
			(ID, "IdProceso", "IdDocumento", "IdUsuario", "Fecha", "Glosa", "TotalRegistrosGenerados", "NombreArchivo", "UltimoCorrelativo")
			VALUES(v_idResumen, p_JobId, v_idDoc, p_IdUsuario, CURRENT_UTCTIMESTAMP, p_Glosa, p_TotalRegistrosGenerados, p_NombreArchivo, 0);
		ELSE
			UPDATE "BSCL.Normativa.BD::TRI.ResumenGeneracion"
			SET  "Fecha"=CURRENT_UTCTIMESTAMP, "Glosa"=p_Glosa, "TotalRegistrosGenerados"=p_TotalRegistrosGenerados, "NombreArchivo"=p_NombreArchivo, "UltimoCorrelativo"=1
			WHERE "IdProceso"=p_JobId AND "IdDocumento"=v_idDoc;
		END IF;
		
				
		CALL "BSCL.Plataforma.BD.Servicios::Depuracion"(:p_IdEmpresa,CURRENT_UTCTIMESTAMP,:p_IdUsuario,'BSCL.Normativa.BD.Servicios::TRI.ActivarVersion','FIN','FIN');
		
	
		
		END;
	END;	
