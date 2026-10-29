SET SCHEMA "BSCL";
GO
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerCampoTipoCentralizacion";
GO
--  Proposito: Obtiene el campo correlativo a SAP para un campo segun su tipo de centralizacion
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/09/2017
CREATE FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerCampoTipoCentralizacion"(
	IN p_IdTipoCentralizacion	BIGINT,
	IN p_CampoSAP				NVARCHAR(60)
)	RETURNS p_out_resultado NVARCHAR(60)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_resultado := '';
	END;
	
	p_out_resultado := '';	
	SELECT "CampoTipoCentralizacion" INTO p_out_resultado
		FROM "BSCL.Contabilidad.BD::ITZ.CamposHomologadosSap"
		WHERE "IdTipoCentralizacion"= :p_IdTipoCentralizacion AND
				"CampoSAP"=:p_CampoSAP;	
				
	
END;
GO