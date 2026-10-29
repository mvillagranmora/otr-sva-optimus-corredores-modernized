SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Utilitarios::AvanzarFechaSistema";

CREATE PROCEDURE 
-- 
--  Proposito: Avanzar de dia la fecha del sistema
--  Autor: NRM - BCS 
--  Fecha Creacion: 01/07/2015
--  
"BSCL.Plataforma.BD.Utilitarios::AvanzarFechaSistema" ( 
	IN p_IdEmpresa			BIGINT,
	OUT p_out_FechaSistema  NVARCHAR(10)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN

	DECLARE v_FechaSistema  NVARCHAR(10);
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaSistema" (:p_IdEmpresa,v_FechaSistema);
	
	CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaSiguiente" ( 	
	    v_FechaSistema,
	    1,
	    'S',
		1, --IDPAIS 		
		p_out_FechaSistema
    );
    
	UPDATE "BSCL.Plataforma.BD::PLA.Configuracion"
	   SET "Valor" = p_out_FechaSistema
     WHERE "IdEmpresa" = p_IdEmpresa and "CodigoParametro" = 'PLA.FechaSistema';
END;
