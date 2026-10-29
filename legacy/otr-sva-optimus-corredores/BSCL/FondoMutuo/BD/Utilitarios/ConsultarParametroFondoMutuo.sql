SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo";

CREATE PROCEDURE
-- 
--  Proposito: Consultar Parametro Fondo Mutuo
--  Autor: - Gat - Blac
--  Fecha Creacion: 27/10/2016 
--     
"BSCL.FondoMutuo.BD.Utilitarios::ConsultarParametroFondoMutuo" (
	--VARIABLES OBLIGATORIAS
    IN p_IdEmpresa 		BIGINT,
	IN p_Parametro		NVARCHAR(255),
	OUT p_out_Resultado	NVARCHAR(255)						
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	
	DECLARE v_count BIGINT;
	
	SELECT COUNT(1) INTO v_count 
	FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo"
	WHERE "NombreTipoParametro" = :p_Parametro AND "IdEmpresa" = :p_IdEmpresa AND "IdEstado" = 'VIG';
	
	p_out_Resultado := '';
	
	IF v_count = 1 THEN
		SELECT "ValorParametro" INTO p_out_Resultado
		FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarParametroFondoMutuo"
		WHERE "NombreTipoParametro" = :p_Parametro AND "IdEmpresa" = :p_IdEmpresa AND "IdEstado" = 'VIG';
	END IF;
	
END;