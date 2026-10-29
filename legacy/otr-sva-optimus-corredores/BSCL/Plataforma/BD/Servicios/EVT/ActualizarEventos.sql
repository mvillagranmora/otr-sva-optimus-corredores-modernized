SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::EVT.ActualizarEventos";

CREATE PROCEDURE 
-- 
--  Proposito: Actualiza eventos masivo
--  Autor: VARIACODE - BCS
--  Fecha Creacion: 04/03/2016
--  
"BSCL.Plataforma.BD.Servicios::EVT.ActualizarEventos"(
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa          		BIGINT,
    IN p_IdUsuario          		BIGINT,
    IN p_IdModulo           		NVARCHAR(3),
 

	IN p_eventos					TABLE ("IdEvento" BIGINT),
	
 	OUT p_msgsalida 			    nvarchar(520)
    )
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN
 	    
	    
	    UPDATE  evt
	    SET evt."Procesado" = 'S'
	    FROM :p_eventos p_evt INNER JOIN "BSCL.Plataforma.BD::EVT.Evento" evt
	    ON evt."IdEvento" = p_evt."IdEvento";
	  	p_msgsalida := 'OK';
 	
END;
