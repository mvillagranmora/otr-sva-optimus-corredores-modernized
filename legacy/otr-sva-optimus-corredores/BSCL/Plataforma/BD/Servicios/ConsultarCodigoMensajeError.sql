SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError";

CREATE PROCEDURE
-- 
--  Proposito: Traduce codigo error hana a codigo bcs traducible humano
--  Autor: - BCS
--  Fecha Creacion: 20/07/2015
--  
"BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError" (
	IN  p_CodigoIntero int,
	OUT p_out_CodigoExterno nvarchar(40)
) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	READS SQL DATA AS
BEGIN
	declare v_rows int;
	
	select  count(1) into v_rows
	  from  "BSCL.Plataforma.BD::PLA.CodigoError"
	 where "IdError" = p_CodigoIntero;	
	 	
	if v_rows > 0 then
		select  "Codigo" into  p_out_CodigoExterno  
		  from  "BSCL.Plataforma.BD::PLA.CodigoError"
		 where "IdError" = p_CodigoIntero;			
	else 
		p_out_CodigoExterno:= 'ERR_INTERNO';
	end if;
	 
		
END;
