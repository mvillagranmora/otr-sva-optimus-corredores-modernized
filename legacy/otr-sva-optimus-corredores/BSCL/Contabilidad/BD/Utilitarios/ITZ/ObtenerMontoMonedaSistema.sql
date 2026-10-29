SET SCHEMA "BSCL";
GO
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerMontoMonedaSistema";
GO
--  Proposito: Obtiene el valor de moneda sistema
--  Autor: - Gat-Blac 
--  Fecha Creacion: 03/10/2017
CREATE FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerMontoMonedaSistema"(
	IN p_IdMovimiento			BIGINT,
	IN p_IdTipoCentralizacion	BIGINT
	
)	RETURNS p_out_resultado DECIMAL(18,2)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
	
	DECLARE v_NombreCampoTC NVARCHAR(60);
	DECLARE v_count BIGINT;
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_resultado := 0;
	END;
	
	BEGIN
		p_out_resultado := 0;
		v_NombreCampoTC := "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerCampoTipoCentralizacion"(:p_IdTipoCentralizacion,'MontoMonedaSistema');
		
		SELECT COUNT(1) INTO v_count
			FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato"
			WHERE "IdMovimiento"= :p_IdMovimiento AND "IdTipoDatoEvento"=:v_NombreCampoTC;
			
		IF(v_count > 0) THEN 
			SELECT TOP 1
				CASE	WHEN "ValorVarchar"<>''
						THEN CAST("ValorVarchar" AS DECIMAL(18, 2))		
						ELSE 
							"ValorDecimal"
						END
						INTO p_out_resultado
				FROM "BSCL.Contabilidad.BD::ITZ.MovimientoDato"
				WHERE "IdMovimiento"= :p_IdMovimiento AND "IdTipoDatoEvento"=:v_NombreCampoTC;
		END IF;		
	END;
END;
GO