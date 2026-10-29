SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Custodia.BD.Utilitarios::CalcularInteresesYReajustesValorizacion";

--  Proposito: Calcula intereses y reajustes valorización anterior y próximo
--  Autor: - JG
--  Fecha Creacion: 23/01/2017

/*
Intereses y reajustes próximo
	RLpr = (Mac*Fpr)-MLac
	ILpr = MLpr-(Mac*Fpr)
	Rpr  = (((Mac*Fpr)-MLac)*(Mpr-Mac))/(MLpr-MLac)
	Ipr  = ((MLpr-(Mac*Fpr))*(Mpr-Mac))/(MLpr-MLac)
Intereses y reajustes anterior
	RLac = (Man*Fac)-MLan
	ILac = MLac-(Man*Fac)
	Rac  = (((Man*Fac)-MLan)*(Mac-Man))/(MLac-MLan)
	Iac  = ((MLac-(Man*Fac))*(Mac-Man))/(MLac-MLan)
*/

CREATE FUNCTION "BSCL.Custodia.BD.Utilitarios::CalcularInteresesYReajustesValorizacion"(
	IN p_Entrada table(
		"IdTablaOrigen" 							bigint,
		"TablaOrigen"								nvarchar(10),
		"IdClasificacionTipoInstrumento"			nvarchar(3),
		"IdMoneda"									nvarchar(3),
		-- COMPRA
		"MontoCompraAnterior" 						decimal(26,8),
		"MontoCompraAnteriorMonedaInstrumento" 		decimal(26,8),
		"MontoCompraActual" 						decimal(26,8),
		"MontoCompraActualMonedaInstrumento" 		decimal(26,8),
		"MontoCompraProximo" 						decimal(26,8),
		"MontoCompraProximoMonedaInstrumento" 		decimal(26,8),
		"ValorParametroFinancieroCompraActual"		decimal(26,8),
		"ValorParametroFinancieroCompraProximo"		decimal(26,8),
		-- MERCADO
		"MontoMercadoAnterior" 						decimal(26,8),
		"MontoMercadoAnteriorMonedaInstrumento" 	decimal(26,8),
		"MontoMercadoActual" 						decimal(26,8),
		"MontoMercadoActualMonedaInstrumento" 		decimal(26,8),
		"MontoMercadoProximo" 						decimal(26,8),
		"MontoMercadoProximoMonedaInstrumento" 		decimal(26,8),
		"ValorParametroFinancieroMercadoActual"		decimal(26,8),
		"ValorParametroFinancieroMercadoProximo"	decimal(26,8)
	)
) RETURNS table(
		-- DATOS ENTRADA
		"IdTablaOrigen" 							bigint,
		"TablaOrigen"								nvarchar(10),
		"IdClasificacionTipoInstrumento"			nvarchar(3),
		"IdMoneda"									nvarchar(3),
		-- COMPRA
		"MontoCompraAnterior" 						decimal(26,8),
		"MontoCompraAnteriorMonedaInstrumento" 		decimal(26,8),
		"MontoCompraActual" 						decimal(26,8),
		"MontoCompraActualMonedaInstrumento" 		decimal(26,8),
		"MontoCompraProximo" 						decimal(26,8),
		"MontoCompraProximoMonedaInstrumento" 		decimal(26,8),
		"ValorParametroFinancieroCompraActual"		decimal(26,8),
		"ValorParametroFinancieroCompraProximo"		decimal(26,8),
		-- MERCADO
		"MontoMercadoAnterior" 						decimal(26,8),
		"MontoMercadoAnteriorMonedaInstrumento" 	decimal(26,8),
		"MontoMercadoActual" 						decimal(26,8),
		"MontoMercadoActualMonedaInstrumento" 		decimal(26,8),
		"MontoMercadoProximo" 						decimal(26,8),
		"MontoMercadoProximoMonedaInstrumento" 		decimal(26,8),
		"ValorParametroFinancieroMercadoActual"		decimal(26,8),
		"ValorParametroFinancieroMercadoProximo"	decimal(26,8),
		-- DATOS SALIDA
		-- COMPRA
		"InteresCompraAnterior"						decimal(26,8),
		"ReajusteCompraAnterior"					decimal(26,8),
		"InteresCompraAnteriorMonedaInstrumento"	decimal(26,8),
		"ReajusteCompraAnteriorMonedaInstrumento"	decimal(26,8),
		"InteresCompraProximo"						decimal(26,8),
		"ReajusteCompraProximo"						decimal(26,8),
		"InteresCompraProximoMonedaInstrumento"		decimal(26,8),
		"ReajusteCompraProximoMonedaInstrumento"	decimal(26,8),
		-- MERCADO
		"InteresMercadoAnterior"					decimal(26,8),
		"ReajusteMercadoAnterior"					decimal(26,8),
		"InteresMercadoAnteriorMonedaInstrumento"	decimal(26,8),
		"ReajusteMercadoAnteriorMonedaInstrumento"	decimal(26,8),
		"InteresMercadoProximo"						decimal(26,8),
		"ReajusteMercadoProximo"					decimal(26,8),
		"InteresMercadoProximoMonedaInstrumento"	decimal(26,8),
		"ReajusteMercadoProximoMonedaInstrumento"	decimal(26,8)
	)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA BSCL AS
BEGIN
		
	RETURN SELECT 
		--DATOS ENTRADA
		 "IdTablaOrigen" 							as "IdTablaOrigen"
		,"TablaOrigen"								as "TablaOrigen"
		,"IdClasificacionTipoInstrumento" 			as "IdClasificacionTipoInstrumento"
		,"IdMoneda" 								as "IdMoneda"
		--COMPRA
		,"MontoCompraAnterior" 						as "MontoCompraAnterior"
		,"MontoCompraAnteriorMonedaInstrumento" 	as "MontoCompraAnteriorMonedaInstrumento"
		,"MontoCompraActual" 						as "MontoCompraActual"
		,"MontoCompraActualMonedaInstrumento" 		as "MontoCompraActualMonedaInstrumento"
		,"MontoCompraProximo" 						as "MontoCompraProximo"
		,"MontoCompraProximoMonedaInstrumento" 		as "MontoCompraProximoMonedaInstrumento"
		,"ValorParametroFinancieroCompraActual" 	as "ValorParametroFinancieroCompraActual"
		,"ValorParametroFinancieroCompraProximo"	as "ValorParametroFinancieroCompraProximo"
		--MERCADO
		,"MontoMercadoAnterior" 					as "MontoMercadoAnterior"
		,"MontoMercadoAnteriorMonedaInstrumento" 	as "MontoMercadoAnteriorMonedaInstrumento"
		,"MontoMercadoActual" 						as "MontoMercadoActual"
		,"MontoMercadoActualMonedaInstrumento" 		as "MontoMercadoActualMonedaInstrumento"
		,"MontoMercadoProximo" 						as "MontoMercadoProximo"
		,"MontoMercadoProximoMonedaInstrumento" 	as "MontoMercadoProximoMonedaInstrumento"
		,"ValorParametroFinancieroMercadoActual" 	as "ValorParametroFinancieroMercadoActual"
		,"ValorParametroFinancieroMercadoProximo"	as "ValorParametroFinancieroMercadoProximo"
		--DATOS SALIDA
		--COMPRA
		,coalesce("MontoCompraActual"-("MontoCompraAnteriorMonedaInstrumento"*"ValorParametroFinancieroCompraActual"),0) 										
			as "InteresCompraAnterior" 		-- MLac-(Man*Fac) 
		,coalesce(("MontoCompraAnteriorMonedaInstrumento"*"ValorParametroFinancieroCompraActual")-"MontoCompraAnterior",0)		
			 as "ReajusteCompraAnterior" 	-- (Man*Fac)-MLan					
		,case 
			when "MontoCompraActual"-"MontoCompraAnterior" = 0 then 0
			else coalesce((("MontoCompraActual"-("MontoCompraAnteriorMonedaInstrumento"*"ValorParametroFinancieroCompraActual"))*("MontoCompraActualMonedaInstrumento"-"MontoCompraAnteriorMonedaInstrumento"))/("MontoCompraActual"-"MontoCompraAnterior"),0) 
 			end as "InteresCompraAnteriorMonedaInstrumento" -- ((MLac-(Man*Fac))*(Mac-Man))/(MLac-MLan)	
		,case
			when "MontoCompraActual"-"MontoCompraAnterior" = 0 then 0
			else coalesce(((("MontoCompraAnteriorMonedaInstrumento"*"ValorParametroFinancieroCompraActual")-"MontoCompraAnterior")*("MontoCompraActualMonedaInstrumento"-"MontoCompraAnteriorMonedaInstrumento"))/("MontoCompraActual"-"MontoCompraAnterior"),0) 
			end as "ReajusteCompraAnteriorMonedaInstrumento" -- (((Man*Fac)-MLan)*(Mac-Man))/(MLac-MLan)	
		,coalesce("MontoCompraProximo"-("MontoCompraActualMonedaInstrumento"*"ValorParametroFinancieroCompraProximo"),0)  
			as "InteresCompraProximo"		-- MLpr-(Mac*Fpr)					
		,coalesce(("MontoCompraActualMonedaInstrumento"*"ValorParametroFinancieroCompraProximo")-"MontoCompraActual",0)
			as "ReajusteCompraProximo"		-- (Mac*Fpr)-MLac					
		,case 
			when "MontoCompraProximo"-"MontoCompraActual" = 0 then 0 
			else coalesce((("MontoCompraProximo"-("MontoCompraActualMonedaInstrumento"*"ValorParametroFinancieroCompraProximo"))*("MontoCompraProximoMonedaInstrumento"-"MontoCompraActualMonedaInstrumento"))/("MontoCompraProximo"-"MontoCompraActual"),0) 
			end as "InteresCompraProximoMonedaInstrumento" -- ((MLpr-(Mac*Fpr))*(Mpr-Mac))/(MLpr-MLac)		
		,case 
			when "MontoCompraProximo"-"MontoCompraActual" = 0 then 0 
			else coalesce(((("MontoCompraActualMonedaInstrumento"*"ValorParametroFinancieroCompraProximo")-"MontoCompraActual")*("MontoCompraProximoMonedaInstrumento"-"MontoCompraActualMonedaInstrumento"))/("MontoCompraProximo"-"MontoCompraActual"),0) 
			end as "ReajusteCompraProximoMonedaInstrumento"	-- (((Mac*Fpr)-MLac)*(Mpr-Mac))/(MLpr-MLac)	
		-- MERCAD O
		,coalesce("MontoMercadoActual"-("MontoMercadoAnteriorMonedaInstrumento"*"ValorParametroFinancieroMercadoActual"),0) 										
			as "InteresMercadoAnterior" 		-- MLac-(Man*Fac) 
		,coalesce(("MontoMercadoAnteriorMonedaInstrumento"*"ValorParametroFinancieroMercadoActual")-"MontoMercadoAnterior",0)		
			 as "ReajusteMercadoAnterior" 	-- (Man*Fac)-MLan					
		,case 
			when "MontoMercadoActual"-"MontoMercadoAnterior" = 0 then 0
			else coalesce((("MontoMercadoActual"-("MontoMercadoAnteriorMonedaInstrumento"*"ValorParametroFinancieroMercadoActual"))*("MontoMercadoActualMonedaInstrumento"-"MontoMercadoAnteriorMonedaInstrumento"))/("MontoMercadoActual"-"MontoMercadoAnterior"),0) 
 			end as "InteresMercadoAnteriorMonedaInstrumento" -- ((MLac-(Man*Fac))*(Mac-Man))/(MLac-MLan)	
		,case
			when "MontoMercadoActual"-"MontoMercadoAnterior" = 0 then 0
			else coalesce(((("MontoMercadoAnteriorMonedaInstrumento"*"ValorParametroFinancieroMercadoActual")-"MontoMercadoAnterior")*("MontoMercadoActualMonedaInstrumento"-"MontoMercadoAnteriorMonedaInstrumento"))/("MontoMercadoActual"-"MontoMercadoAnterior"),0) 
			end as "ReajusteMercadoAnteriorMonedaInstrumento" -- (((Man*Fac)-MLan)*(Mac-Man))/(MLac-MLan)	
		,coalesce("MontoMercadoProximo"-("MontoMercadoActualMonedaInstrumento"*"ValorParametroFinancieroMercadoProximo"),0)  
			as "InteresMercadoProximo"		-- MLpr-(Mac*Fpr)					
		,coalesce(("MontoMercadoActualMonedaInstrumento"*"ValorParametroFinancieroMercadoProximo")-"MontoMercadoActual",0)
			as "ReajusteMercadoProximo"		-- (Mac*Fpr)-MLac					
		,case 
			when "MontoMercadoProximo"-"MontoMercadoActual" = 0 then 0 
			else coalesce((("MontoMercadoProximo"-("MontoMercadoActualMonedaInstrumento"*"ValorParametroFinancieroMercadoProximo"))*("MontoMercadoProximoMonedaInstrumento"-"MontoMercadoActualMonedaInstrumento"))/("MontoMercadoProximo"-"MontoMercadoActual"),0) 
			end as "InteresMercadoProximoMonedaInstrumento" -- ((MLpr-(Mac*Fpr))*(Mpr-Mac))/(MLpr-MLac)		
		,case 
			when "MontoMercadoProximo"-"MontoMercadoActual" = 0 then 0 
			else coalesce(((("MontoMercadoActualMonedaInstrumento"*"ValorParametroFinancieroMercadoProximo")-"MontoMercadoActual")*("MontoMercadoProximoMonedaInstrumento"-"MontoMercadoActualMonedaInstrumento"))/("MontoMercadoProximo"-"MontoMercadoActual"),0) 
			end as "ReajusteMercadoProximoMonedaInstrumento"	-- (((Mac*Fpr)-MLac)*(Mpr-Mac))/(MLpr-MLac)	
	from :p_Entrada;	
end;