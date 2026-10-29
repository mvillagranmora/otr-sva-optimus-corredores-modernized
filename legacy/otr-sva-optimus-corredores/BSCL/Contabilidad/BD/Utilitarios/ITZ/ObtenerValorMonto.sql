SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerValorMonto";

--  Proposito: Obtiene el campo correlativo a SAP para un campo segun su tipo de centralizacion
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/09/2017
CREATE FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerValorMonto"(
	IN p_Regla				NVARCHAR(60),
	IN p_Campo				NVARCHAR(60),
	IN p_IdCentralizacion	BIGINT
)	RETURNS p_out_resultado NVARCHAR(60)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_resultado := '';
	END;
	p_condicion :='';
	
	SELECT "Valor" INTO p_condicion
		FROM "BSCL.Contabilidad.BD::ITZ.ConfiguracionParametros"
		WHERE "Codigo"='CONT1'; --codigo regla
		
	-- Salida Valor o Monto
	if(:p_condicion='A' AND 'Debit')
		--Debit
		SELECT * FROM "BSCL.Contabilidad.BD::ITZ." 
	
				
	
END;
