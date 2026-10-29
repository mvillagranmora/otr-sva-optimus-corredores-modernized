SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.FondoMutuo.BD.Utilitarios::ObtenerSpreadRescate";

--  Proposito: Calcula el spread de una operacion de rescate
--  Autor: - Gat-Blac 
--  Fecha Creacion: 15/11/2016
CREATE FUNCTION "BSCL.FondoMutuo.BD.Utilitarios::ObtenerSpreadRescate"(
	p_ValorFactorReajuste			DECIMAL(26,8),
	p_CostoFactorReajusteRescate	DECIMAL(26,8),
	p_CantidadRescate				DECIMAL(26,8),
	p_ValorCuota					DECIMAL(26,8)
) RETURNS p_out_Spread	DECIMAL(26,8)
LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	
	BEGIN
		p_out_Spread := -1;
	END;
	
	IF p_ValorFactorReajuste > 0 AND p_CostoFactorReajusteRescate > 0 AND p_CantidadRescate > 0 AND p_ValorCuota > 0 THEN
		p_out_Spread := TO_DECIMAL(ROUND((p_CantidadRescate * p_ValorCuota)-(p_ValorFactorReajuste * p_CostoFactorReajusteRescate),8),26,8);
	ELSE
		p_out_Spread := -1;
	END IF;
	
	
END;
