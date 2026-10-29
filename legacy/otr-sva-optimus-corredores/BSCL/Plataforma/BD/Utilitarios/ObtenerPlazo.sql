SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerPlazo";

CREATE PROCEDURE
-- 
--  Proposito: Obtener el plazo (cantidad de dias) entre dos fechas enviadas.
--  Autor: - Oscar Carreno Tolmo - OCT
--  Fecha Creacion: 01/12/2015
--   
"BSCL.Plataforma.BD.Utilitarios::ObtenerPlazo" ( 	
    IN  p_FechaDesde	NVARCHAR(10),
    IN  p_FechaHasta	NVARCHAR(10),
    IN  p_DiasHabiles	NVARCHAR(1) default 'N',
    IN  p_DiasCondicionDeLiquidacion	INT default 0,
	IN  p_IdPais		BIGINT, 
	--ENUMERAR VARIABLES DE SALIDA	
	OUT p_out_PlazoDias	INTEGER
	
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN

	DECLARE v_Dias INTEGER :=0;
	DECLARE v_Row INTEGER :=1;
	DECLARE v_DiasHabiles INTEGER :=0;
	DECLARE v_DiaEsHabil NVARCHAR(1);
	DECLARE v_Fecha DATE; 
   	DECLARE v_dia_inicio_plazo nvarchar(10);
   	DECLARE v_dia_final_plazo nvarchar(10);
   
   --CALCULA FECHA DE LIQUIDACION
   	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
	     p_Fecha		=> :p_FechaDesde,
	     p_NumeroDias	=> :p_DiasCondicionDeLiquidacion,
	     p_EsHabil		=> 'S',
		 p_IdPais		=> :p_IdPais,
		 
		 p_out_FechaSiguente => v_dia_inicio_plazo
	);
	
	 --CALCULA FECHA DE LIQUIDACION
   	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
	     p_Fecha		=> :p_FechaHasta,
	     p_NumeroDias	=> 0,
	     p_EsHabil		=> 'S',
		 p_IdPais		=> :p_IdPais,
		 
		 p_out_FechaSiguente => v_dia_final_plazo
	);
   	
   	select :v_dia_inicio_plazo from dummy;
   
	SELECT DAYS_BETWEEN (TO_DATE (:v_dia_inicio_plazo, 'YYYY-MM-DD'), TO_DATE(:v_dia_final_plazo, 'YYYY-MM-DD')) 
 	INTO v_Dias FROM DUMMY;
 	
 	p_out_PlazoDias := v_Dias;
	
	IF (UPPER(:p_DiasHabiles) = 'S') THEN
		v_Fecha := p_FechaDesde;
		p_out_PlazoDias := 0;
		WHILE (:v_Row <= :v_Dias) DO
			CALL "BSCL.Plataforma.BD.Utilitarios::DiaEsHabil"(:v_Fecha, :p_IdPais, v_DiaEsHabil);
			
			IF (:v_DiaEsHabil = 'S') THEN
				v_DiasHabiles := v_DiasHabiles + 1;
			END IF;
			
			SELECT ADD_DAYS (TO_DATE (v_Fecha, 'YYYY-MM-DD'), 1) INTO v_Fecha FROM DUMMY;
			v_Row := v_Row + 1;
		END WHILE;
		p_out_PlazoDias := v_DiasHabiles;
	END IF;
END;
