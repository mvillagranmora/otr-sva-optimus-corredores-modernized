SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Ord";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::NCG380Ord"
( 
    -- Parametros de Entrada
 	IN p_IdSolicitudCircular BIGINT DEFAULT 0, 
    --VARIABLES DE SALIDA 
	OUT p_out_TablaSalida "BSCL.Normativa.BD::NOR.SALIDA_NCG380ORD" --Se debe llamar siemte p_out_TablaSalida
)  	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL" 
AS
BEGIN

	CALL "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ord" (
	   p_IdSolicitudCircular   => :p_IdSolicitudCircular,
	   p_out_TablaSalida       => p_out_TablaSalida);
	   
    SELECT * FROM :p_out_TablaSalida;
END;
GO