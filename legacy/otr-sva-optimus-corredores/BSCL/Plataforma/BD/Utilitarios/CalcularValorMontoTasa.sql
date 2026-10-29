SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Plataforma.BD.Utilitarios::CalcularValorMontoTasa";

CREATE FUNCTION "BSCL.Plataforma.BD.Utilitarios::CalcularValorMontoTasa"
   (
	  p_Monto       DECIMAL(18,8),
	  p_Plazo       DECIMAL(18,8),
	  p_Tasa        DECIMAL(18,8),
	  p_Tipo	 	NVARCHAR(1) --Tipo Tasa -> I:interes, D:descuento	 
   ) 
   
   RETURNS p_MontoSalida decimal(18,8)
   LANGUAGE SQLSCRIPT
   SQL SECURITY INVOKER 

   AS
     BEGIN
     	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	      BEGIN
		    p_MontoSalida := 0;
	      END;
	
	      If (p_Tipo = 'I') Then
		    p_MontoSalida := (p_Monto * ( 1 + (p_Tasa / 100) * (p_Plazo/30)));
	      Else   
	        If (p_Tipo = 'D') then
			 --p_MontoSalida := p_Monto * ( 1 + (p_Tasa / 100.0) * (p_Plazo/30.0) );
		    End If;
		  End If;  
     END;

