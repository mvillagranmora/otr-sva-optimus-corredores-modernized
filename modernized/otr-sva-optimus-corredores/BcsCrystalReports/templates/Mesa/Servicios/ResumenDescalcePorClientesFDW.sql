SET SCHEMA "BSCL"; 

DROP PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ResumenDescalcePorClientesFDW";

--
--  Proposito: servicio Resumen Descalce Diario Por Clientes
--  Autor: EORTIZ - PRONET-IT
--  Fecha Creacion: /12/2016
--

CREATE PROCEDURE "BSCL.Mesa.CrystalReports.Servicios::ResumenDescalcePorClientesFDW"
(
-- Parametros entrada 
IN p_IdEmpresa				BIGINT,
IN p_FechaOperacion			DATE,
IN p_Usuario				VARCHAR(100)

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER  
	DEFAULT SCHEMA "BSCL"
	--READS SQL DATA   
AS
BEGIN
	--------------------
	--Variables
	--------------------
	DECLARE varDOLAR 		BIGINT;
		
	DECLARE v_tablaForward TABLE(
	 "FechaVencimiento"  NVARCHAR(10)
	,"NombreCliente"     NVARCHAR(100)     
	,"NombreInstrumento"    NVARCHAR(100)   
	,"FechaOperacion"       NVARCHAR(10)
	,"Plazo"             INT
	,"MontoCompra"   DECIMAL(26,8)
	,"MontoVenta"   DECIMAL(26,8)
	,"Condicion"   NVARCHAR(10)
	);
		

	v_Tbl01 =
		SELECT "RutCliente"
			    ,"NombreCliente"         
			    ,"NombreInstrumento"   
			    ,"Nemotecnico"        
				,"IdMonedaPrincipal"
				,"IdMonedaSecundaria"     

				,SUM( CASE WHEN "IdTipoOperacion" = 'COMFWD' AND "MontoMonedaPrincipal" <> 0 THEN 
					  "MontoValorRazonableNeto" ELSE 0 END ) AS "ValorRazonableCompra"
				,SUM( CASE WHEN "IdTipoOperacion" = 'COMFWD' AND "MontoMonedaPrincipal" = 0 THEN 
					  "MontoValorRazonableNeto" ELSE 0 END ) AS "ValorRazonableVenta"
				,SUM( "MontoValorRazonableNeto" ) AS "ValorRazonable"
			    ,SUM( CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN "MontoMonedaPrincipal" ELSE 0 END )  AS "MontoCompra"
			    ,SUM( CASE WHEN "IdTipoOperacion" = 'VENFWD' THEN "MontoMonedaPrincipal" ELSE 0 END )  AS "MontoVenta"
			    ,CASE WHEN 	SUM(CASE WHEN "IdTipoOperacion" = 'COMFWD' THEN "MontoMonedaPrincipal" ELSE 0 END) = 
			          		SUM(CASE WHEN "IdTipoOperacion" = 'VENFWD' THEN "MontoMonedaPrincipal" ELSE 0 END) 
			     	  THEN	'CALCE'
				 	  ELSE	'DESCALCE'
			     END  AS "Condicion"
		FROM 	"BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" 
		WHERE 	"Estado" = 'VIG'
		GROUP 	BY 	"RutCliente", "NombreCliente", "NombreInstrumento", "Nemotecnico", 
					"IdMonedaPrincipal", "IdMonedaSecundaria";
					

	v_Tbl02 =
		SELECT	*,
				CASE 
				WHEN "MontoCompra" - "MontoVenta" = 0 THEN 0
				WHEN "MontoCompra" - "MontoVenta" < 0 THEN (("MontoCompra" - "MontoVenta")*100)/ "MontoVenta"
				ELSE (("MontoCompra" - "MontoVenta")*100) / "MontoCompra"
				END AS "Regla3"
		FROM	:v_Tbl01;


	v_Movimientos =
		SELECT	T02.*
				,"MontoCompra" - "MontoVenta" AS "Descalse"
				,CASE WHEN "Regla3" = 0 THEN 0      
					  WHEN "Regla3" > 0 THEN "ValorRazonableCompra" * ("Regla3"/100)
					  WHEN "Regla3" < 0 THEN "ValorRazonableVenta" * ("Regla3"/100) * -1
				 END AS "ValorRazonableConDescalce" 
				,CASE WHEN "Regla3" = 0 THEN "ValorRazonable"      
					  WHEN "Regla3" > 0 THEN "ValorRazonable" - ( "ValorRazonableCompra" * ("Regla3"/100) )
					  WHEN "Regla3" < 0 THEN "ValorRazonable" - ( "ValorRazonableVenta" * ("Regla3"/100) * -1 )
				 END AS "ValorRazonableSinDescalce" 				
				 ,"ValorRazonableVenta" AS "TotalActivos"
		FROM	:v_Tbl02 AS T02;				
			        

	SELECT * FROM :v_Movimientos;


 END;