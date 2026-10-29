SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema";

CREATE PROCEDURE 
-- 
--  Proposito: Obtener el dia en que esta posicionado el sistema
--  Autor: NRM - BCS
--  Fecha Creacion: 01/07/2015
--  
"BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" ( 
	IN  p_IdEmpresa         BIGINT,
	OUT p_out_FechaSistema  NVARCHAR(10)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN 

	DECLARE v_Cantidad INTEGER;
	DECLARE FECHA_SISTEMA_NO_ENCONTRADA CONDITION FOR SQL_ERROR_CODE 10003;
	
	v_Cantidad := 0; 
	
	SELECT COUNT("CodigoParametro") INTO v_Cantidad
	  FROM "BSCL.Plataforma.BD::PLA.Configuracion"
	 WHERE "IdEmpresa" = :p_IdEmpresa and "CodigoParametro" = 'PLA.FechaSistema';
	 
	 IF (v_Cantidad < 1) THEN
	 	SIGNAL FECHA_SISTEMA_NO_ENCONTRADA;
	 ELSE
	 	 SELECT "Valor" INTO p_out_FechaSistema
		 FROM "BSCL.Plataforma.BD::PLA.Configuracion"
		 WHERE "IdEmpresa" = :p_IdEmpresa and "CodigoParametro" = 'PLA.FechaSistema';
	 END IF;
	 
	 
END;
