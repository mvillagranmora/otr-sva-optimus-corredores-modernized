SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::DarFormatoRut";

CREATE FUNCTION
--
--  Proposito: Devuelve el formato de un RUT
--  Autor: SMF - BCS
--  Fecha Creacion: 20/01/2023
"BSCL.Plataforma.BD.Utilitarios::DarFormatoRut" (
	IN rut nvarchar(12)
)
RETURNS nuevo_rut nvarchar(14)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER AS
BEGIN
	DECLARE cont INT DEFAULT 0;
    DECLARE i INT DEFAULT 0;
    DECLARE v_nuevo_rut VARCHAR(14) DEFAULT '';
   
	IF (LENGTH(rut) = 0) THEN
    	v_nuevo_rut := '';
    ELSE
        rut := REPLACE(rut,'.', '');
        rut := REPLACE(rut, '-', '');
        v_nuevo_rut := CONCAT('-',RIGHT(rut,1));
        
        i := LENGTH(rut);
        
        WHILE i >= 2 DO
            i := i - 1;
            
            v_nuevo_rut := CONCAT(SUBSTRING(rut,i, 1),v_nuevo_rut);
            cont := cont + 1;
            
            IF(cont = 3 AND i <> 0) THEN
            	v_nuevo_rut := CONCAT('.',v_nuevo_rut);
                cont := 0;
            END IF;
        END WHILE;
	END IF;
	nuevo_rut := v_nuevo_rut;
END;