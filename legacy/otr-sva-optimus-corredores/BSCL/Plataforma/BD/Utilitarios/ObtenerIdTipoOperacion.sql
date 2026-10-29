SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerIdTipoOperacion";

CREATE PROCEDURE 
-- 
--  Proposito: Obtiene el id del tipo operacion segun codigo solicitado
--  Autor: - BCS
--  Fecha Creacion: 27/07/2015
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerIdTipoOperacion" ( 	
    IN p_IdTipoInstrumento	BIGINT,
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_IdTipoClasificacionInstrumento	BIGINT
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

	SELECT IFNULL("IdClasificacionTipoInstrumento",0) INTO p_out_IdTipoClasificacionInstrumento FROM "BSCL.Plataforma.BD::INS.TipoInstrumento"
	WHERE "IdTipoInstrumento" = p_IdTipoInstrumento;
     	
END;
