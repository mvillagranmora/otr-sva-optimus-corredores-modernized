SET SCHEMA "BSCL";
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerCodigoBO";

--  Proposito: Obtiene el codigo a enviar a SAP segun la tabla de cuatro entradas
--  Autor: - Gat-Blac 
--  Fecha Creacion: 12/09/2017

CREATE FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerCodigoBO"(
	IN p_ProductoConcepto	NVARCHAR(60),
	IN p_Relacionado		NVARCHAR(60),
	IN p_TipoEntrada		NVARCHAR(60),
	IN p_Bolsa				NVARCHAR(60),
	IN p_Proceso			NVARCHAR(60)
		
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
	SELECT "CodigoBO" INTO p_out_resultado
		FROM "BSCL.Contabilidad.BD::ITZ.EstructuraCodigosBO"
		WHERE 	"ProductoConcepto"= :p_ProductoConcepto AND
				"Relacionado"=:p_Relacionado			AND
				"Tipo"=:p_TipoEntrada			AND
				"Bolsa"= :p_Bolsa ;	
				
	
END;