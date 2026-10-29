SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarTipoCambio";

CREATE PROCEDURE "BSCL.Mesa.BD.Servicios::ConsultarTipoCambio"
    ( 
	-- Tabla Entrada
	IN p_in_Monedas table (   "Fecha"             		SECONDDATE			
   								, "IdMoneda"			    NVARCHAR(10)),			   
	--Salida   	
   	OUT p_out_Monedas table ( "Fecha"             		SECONDDATE			
   								, "IdMoneda"			    NVARCHAR(10)
   								, "TipoCambioMercado" 		DECIMAL(26,8)
								, "ParidadMercado"   		DECIMAL(26,8)
								)
    ) 

	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA 
AS   
  BEGIN			
  	
     CREATE LOCAL TEMPORARY TABLE #FechasCorrespondientes (  "Fecha"   SECONDDATE
												  ,"IdMoneda"	    NVARCHAR(3));
     
     insert into #FechasCorrespondientes 
     Select MAX(Paridad."Fecha") as "Fecha", 
     		Paridad."IdMoneda"
     From :p_in_Monedas as InMonedas
     left join "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" Paridad
     on Paridad."Fecha" <= InMonedas."Fecha" AND Paridad."IdMoneda" = InMonedas."IdMoneda"
     group by Paridad."IdMoneda";
						 
     p_out_Monedas = Select Paridad."Fecha",
						    Paridad."IdMoneda",
						    Paridad."TipoCambioMercado",
						    Paridad."ParidadMercado"
					     From #FechasCorrespondientes as FechaYMoneda
					     LEFT JOIN "BSCL.Mesa.BD::SPT.ParidadYTipoCambio" Paridad
					     on Paridad."Fecha" = FechaYMoneda."Fecha" AND Paridad."IdMoneda" = FechaYMoneda."IdMoneda";
						 
     DROP TABLE #FechasCorrespondientes;

  END;
