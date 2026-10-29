SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente";

CREATE PROCEDURE
-- 
--  Proposito: Obtener el siguiente dia de corrido o habil, segun fecha enviada.
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
    IN  p_Fecha			NVARCHAR(10),
    IN	p_NumeroDias	INTEGER,
    IN  p_EsHabil		NVARCHAR(1),
	IN  p_IdPais		BIGINT, 
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_FechaSiguente  NVARCHAR(10)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

	DECLARE v_EsHabil INTEGER;
	DECLARE v_DiaEsHabil NVARCHAR(1);
	DECLARE v_Fecha DATE; 
   
	v_EsHabil := p_NumeroDias;
	
	SELECT ADD_DAYS (TO_DATE (p_Fecha, 'YYYY-MM-DD'), :v_EsHabil) INTO p_out_FechaSiguente FROM DUMMY;
 
	
	IF (UPPER(:p_EsHabil) = 'S') THEN
		v_Fecha := p_Fecha;
		WHILE (:v_EsHabil > 0) DO
			SELECT ADD_DAYS (TO_DATE (v_Fecha, 'YYYY-MM-DD'), 1) INTO v_Fecha FROM DUMMY;
			CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(v_Fecha, p_IdPais, :v_DiaEsHabil);
			
			IF (:v_DiaEsHabil = 'S') THEN
				v_EsHabil := v_EsHabil - 1;
			END IF;
		END WHILE;
		p_out_FechaSiguente := v_Fecha;
	END IF;	
	
END;
