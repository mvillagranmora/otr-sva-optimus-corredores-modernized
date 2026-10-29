SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Tesoreria.BD.Utilitarios::HomologacionCampoArchivo";

CREATE FUNCTION 
-- 
--  Proposito: homologa un codigo optimus por un codigo de una entidad externa
--             para la generacion de archivo de tranferencias bancarias
--  Autor: JVILLAGRAN - BCS
--  Fecha Creacion: 29-07-2016
--  
"BSCL.Tesoreria.BD.Utilitarios::HomologacionCampoArchivo" (
	p_IdFormatoArchivo          BIGINT,	
    p_Campo	                    NVARCHAR(50),
    p_ValorOptimus	            NVARCHAR(100)
)

	RETURNS p_out_ValorExterno NVARCHAR(100)
	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	DECLARE v_ValorExterno NVARCHAR(100) := '';

	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		p_out_ValorExterno := '';
	END;
	
	v_ValorExterno := p_ValorOptimus; -- Si no encuentra homologación, devuelve valor optimus

	SELECT "ValorExterno"
	INTO v_ValorExterno
	FROM "BSCL.Tesoreria.BD::TES.HomologacionArchivo"
	WHERE "IdFormatoArchivo" = :p_IdFormatoArchivo
	AND	  "Campo"            = :p_Campo
	AND	  "ValorOptimus"     = :p_ValorOptimus;

	p_out_ValorExterno := v_ValorExterno;
		
END;
