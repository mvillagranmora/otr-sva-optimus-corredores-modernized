SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion";

CREATE PROCEDURE
-- 
--  Proposito: Obtener la fecha de liquidacion segun fecha, pais y condicion de liquidacion enviada.
--  Autor: NRM - BCS
--  Fecha Creacion: 01/07/2015 
--    
"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaLiquidacion" (
	IN  p_FechaIngreso				NVARCHAR(10),	
    IN  p_IdTipoOperacion			NVARCHAR(6) DEFAULT NULL,
    IN  P_IdCondicionDeLiquidacion	NVARCHAR(2),
	IN  p_IdPais					BIGINT,	    
    
    OUT p_out_FechaLiquidacion		NVARCHAR(10)    
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN
	
	DECLARE v_Quantity   		BIGINT;
	DECLARE v_PlazoEnDias       INTEGER;
 	DECLARE v_SoloDiasHabiles   NVARCHAR(1);
 	
	DECLARE CONDICION_DE_LIQUIDACION_NO_CONFIGURADA CONDITION FOR SQL_ERROR_CODE 10008;
	
	
	IF (:p_IdTipoOperacion IS NULL) THEN 
		
		SELECT "PlazoEnDias" INTO v_PlazoEnDias 		
		 	  FROM "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion"	  
		 	 WHERE "IdCondicionDeLiquidacion" = P_IdCondicionDeLiquidacion;
		
		CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" (p_FechaIngreso, v_PlazoEnDias, 'S', p_IdPais, p_out_FechaLiquidacion);
	
	ELSE
	
		v_Quantity := 0;	  
	 	
	 	SELECT COUNT("IdTipoOperacionYCondicionDeLiquidacion") INTO v_Quantity		
	 	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion"	  
	 	 WHERE "IdTipoOperacion" = p_IdTipoOperacion
	 	   AND "IdCondicionDeLiquidacion" = P_IdCondicionDeLiquidacion;
	 	   
	 	IF (:v_Quantity > 0) THEN
		 	SELECT "PlazoEnDias", "SoloDiasHabiles" INTO v_PlazoEnDias, v_SoloDiasHabiles 		
		 	  FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarTipoOperacionYCondicionDeLiquidacion"	  
		 	 WHERE "IdTipoOperacion" = p_IdTipoOperacion
		 	   AND "IdCondicionDeLiquidacion" = P_IdCondicionDeLiquidacion;
		
			CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" (p_FechaIngreso, v_PlazoEnDias, v_SoloDiasHabiles, p_IdPais, p_out_FechaLiquidacion);
		ELSE
			SIGNAL CONDICION_DE_LIQUIDACION_NO_CONFIGURADA;
		END IF ;
		
	END IF;
	
	
END;
 
