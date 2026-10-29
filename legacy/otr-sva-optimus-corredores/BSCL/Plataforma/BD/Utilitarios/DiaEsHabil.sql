SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil";

CREATE PROCEDURE 
-- 
--  Proposito: Determina si el dia enviado es habil o no..
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--   
"BSCL.Plataforma.BD.Utilitarios::DiaEsHabil" ( 	
    IN p_Fecha	NVARCHAR(10),
    IN p_IdPais	BIGINT,
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_Habil	NVARCHAR(1)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

    DECLARE v_Quantity	BIGINT;
	DECLARE v_dia		TINYINT;
	DECLARE v_AgnoCarga	INTEGER;
    
	p_out_Habil := 'S';
	v_Quantity := 0 ;
	
	SELECT YEAR (TO_DATE (p_Fecha, 'YYYY-MM-DD')) INTO v_AgnoCarga FROM DUMMY;
	SELECT WEEKDAY (TO_DATE (p_Fecha, 'YYYY-MM-DD')) INTO v_dia FROM DUMMY;
	
	SELECT COUNT("IdFeriado") INTO v_Quantity FROM "BSCL.Plataforma.BD::REG.DiaFeriado"
	WHERE (("Tipo" = 'FERIADO' AND "Fecha" = p_Fecha)
	   OR ("Tipo" = 'INHABIL' AND "Dia" = v_dia))
	  AND "IdPais" = p_IdPais
	  AND "AgnoCarga" = v_AgnoCarga
	  AND "IdEstado"='ACT';	   

     
 	IF (:v_Quantity > 0) THEN
 		p_out_Habil := 'N';
 	END IF; 	
END;
