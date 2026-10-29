SET SCHEMA "BSCL";
GO 
DROP VIEW "BSCL.Custodia.BD.Vistas::CUS.CustodiaPortafolioAnaliticos";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::CUS.CustodiaPortafolioAnaliticos"
AS



select  "IdEmpresa"
			,"IdClasificacionTipoInstrumento" 
			,"IdTipoInstrumento" 
			,"IdInstrumento" 
			,"Nemotecnico"  
			,"IdCliente" 
			,"IdPortafolio"
			,"IdBoveda"  
			,"NemoSVS" 			
			,SUM("CantidadCustodia") as "CantidadCustodia"
			,SUM("CantidadSimulCom") as "CantidadSimulCom"
			,SUM("CantidadSimulVen") as "CantidadSimulVen"
			,SUM("CantidadPrestamo") as "CantidadPrestamo"
			,SUM("CantidadEmpresti") as "CantidadEmpresti"
			,SUM("CantidadGarantia") as "CantidadGarantia"
			,SUM("CantidadTransIng") as "CantidadTransIng"
			,SUM("CantidadTransEgr") as "CantidadTransEgr"		
FROM (

-- CUSTODIA - SALDO NO RETIRADO
	select  	 car."IdEmpresa"
				,"IdClasificacionTipoInstrumento" 
				,"IdTipoInstrumento" 
				,"IdInstrumento" 
				,"Nemotecnico"  
				,"IdCliente" 
				,"IdPortafolio"
				,"IdBoveda"  
				,cast(IFNULL(SUM("INGCantidad"),0) as DECIMAL(26,8)) as "CantidadCustodia" 
				,0 													as "CantidadSimulCom"
				,0 													as "CantidadSimulVen"
				,0													as "CantidadPrestamo"
				, 0 as "CantidadEmpresti"
				, 0 as "CantidadGarantia"
				,0													as "CantidadTransIng"
				,0 												as "CantidadTransEgr"		
				,car."NemoSVS" 
	from "BSCL.Custodia.BD::CUS.Cartera" car
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = car."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
	where --"IdEmpresa" = 1 AND
		  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
		  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
		  "IdTipoInstrumento" NOT IN ('PRT', 'EMP', 'SIM') AND 
		 -- "IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END AND
		  --"IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END AND
		  --"IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumento" END AND
		  -- '2016-11-14' BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
			 cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
		  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
		  -- '2016-11-14' < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
			 cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
		  ---*************************
		  "IdEstado" <> 'ANU'
	group by car."IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento" 
			,"NemoSVS","Nemotecnico","IdCliente","IdPortafolio","IdBoveda" 
			
	UNION all
	
-- SIMULTANEA COMPRA
		select   sim."IdEmpresa"
				,"suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento" 
				,"suby_IdTipoInstrumento" as "IdTipoInstrumento"
				,"suby_IdInstrumento" as "IdInstrumento" 
				,"suby_Nemotecnico" as "Nemotecnico" 
				,"IdCliente" 
				,"IdPortafolio"
				,"suby_IdBoveda" 
				,0 															as "CantidadCustodia"
				,cast(IFNULL(SUM("INGCantidad"),0) * -1 as DECIMAL(26,8)) 	as "CantidadSimulCom"
				,0 															as "CantidadSimulVen"
				,0													as "CantidadPrestamo"	
				,0 as "CantidadEmpresti"
				,0 as "CantidadGarantia"
				,0													as "CantidadTransIng"	
				,0  												as "CantidadTransEgr"	
				,"suby_NemoSVS" as "NemoSVS" 						
		from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim
		INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = sim."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
		where --"IdEmpresa" = 1 AND
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  "IdTipoOperacion" = 'COMSIM' AND 
					  cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
					  cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  ---*************************
					  "IdEstado" <> 'ANU' --AND
					  --"suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "suby_IdClasificacionTipoInstrumento" END AND
					  --"suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "suby_IdTipoInstrumento" END AND
					  --"suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "suby_IdInstrumento" END
				group by sim."IdEmpresa", "suby_IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento", "suby_IdInstrumento", "IdCliente", "IdPortafolio" 
				         ,"suby_NemoSVS","suby_Nemotecnico","suby_IdBoveda" 
				 
	Union all	
	
-- SIMULTANEA VENTA
-- SELECT * FROM "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea"
	select  sim2."IdEmpresa"
			,"suby_IdClasificacionTipoInstrumento" as "IdClasificacionTipoInstrumento"
			,"suby_IdTipoInstrumento" as"IdTipoInstrumento"
			,"suby_IdInstrumento" as "IdInstrumento"
			,"suby_Nemotecnico" as "Nemotecnico" 
			,"IdCliente" 
			,"IdPortafolio"
			,"suby_IdBoveda"  
			,0 													as "CantidadCustodia"
			,0 													as "CantidadSimulCom"
			,cast(IFNULL(SUM("INGCantidad"),0) as DECIMAL(26,8)) as "CantidadSimulVen"
			,0													as "CantidadPrestamo"
			, 0 as "CantidadEmpresti"
			, 0 as "CantidadGarantia"
			,0													as "CantidadTransIng"
			, 0 												as "CantidadTransEgr"
			, "suby_NemoSVS" as "NemoSVS" 				
	from "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultanea" sim2
	INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = sim2."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
	where --"IdEmpresa" = 1 AND
		  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
		  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
		  "IdTipoOperacion" = 'VENSIM' AND
		   cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
		  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
		   cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
		  ---*************************
		  "IdEstado" <> 'ANU' --AND 
		  --"suby_IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "suby_IdClasificacionTipoInstrumento" END AND
		  --"suby_IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "suby_IdTipoInstrumento" END AND
		  --"suby_IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "suby_IdInstrumento" END
	group by   sim2."IdEmpresa", "suby_IdClasificacionTipoInstrumento", "suby_IdTipoInstrumento", "suby_IdInstrumento"
			 ,"suby_NemoSVS" ,"suby_Nemotecnico","IdCliente", "IdPortafolio", "suby_IdBoveda" 
	
	
-- CARTERA EN PRESTAMO
	union all
	
	select  		prt."IdEmpresa"
					,"IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumentoSubyacente" as "IdTipoInstrumento"
					, "IdInstrumentoSubyacente" as "IdInstrumento"
					,  "NemoSubyacente"  as "Nemotecnico"
					, "IdCliente", "IdPortafolio"
					, "IdBovedaSubyacente"
					,0 													as "CantidadCustodia"
					,0 													as "CantidadSimulCom"
					,0 													as "CantidadSimulVen"
					,cast(IFNULL(SUM("INGCantidad"),0) as DECIMAL(26,8)) as "CantidadPrestamo"
					, 0 as "CantidadEmpresti"
					, 0 as "CantidadGarantia"
					, 0													as "CantidadTransIng"
					, 0 												as "CantidadTransEgr"
					,"NemoSVSSubyacente" as "NemoSVS" 						
				from "BSCL.Custodia.BD.Vistas::PRT.ConsultarCarteraPrestamo" prt
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = prt."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
				where --"IdEmpresa" = 1 AND
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
					  cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end --and
					  ---*************************
					 -- "IdClasificacionTipoInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumentoSubyacente" END AND
					 -- "IdTipoInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumentoSubyacente" END AND
					 -- "IdInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumentoSubyacente" END
				group by prt."IdEmpresa", "IdClasificacionTipoInstrumentoSubyacente", "IdTipoInstrumentoSubyacente", "IdInstrumentoSubyacente" 
						,"NemoSVSSubyacente","NemoSubyacente", "IdCliente", "IdPortafolio", "IdBovedaSubyacente" 
						
	union all
-- EMPRESTITOS
	select  		emp."IdEmpresa" 
					,"IdClasificacionTipoInstrumentoSubyacente" as "IdClasificacionTipoInstrumento"
					, "IdTipoInstrumentoSubyacente" as "IdTipoInstrumento"
					, "IdInstrumentoSubyacente" as "IdInstrumento"
					, "NemoSubyacente" as "Nemotecnico"
					, "IdCliente", "IdPortafolio"
					, "IdBovedaSubyacente"
					,0 													as "CantidadCustodia"
					,0 													as "CantidadSimulCom"
					,0 													as "CantidadSimulVen"
					,0 													as "CantidadPrestamo"
					, cast(IFNULL(SUM("INGCantidad"),0) as DECIMAL(26,8)) as "CantidadEmpresti"
					, 0 as "CantidadGarantia"
					,0													as "CantidadTransIng"
					, 0 												as "CantidadTransEgr"						
					, "NemoSVSSubyacente" as "NemoSVS"
				from "BSCL.Custodia.BD.Vistas::PRT.ConsultarResumenEmprestitoHistorico" emp
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = emp."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
				where --"IdEmpresa" = 1 AND
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
					  cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end --and
					  ---*************************
					  --"IdClasificacionTipoInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumentoSubyacente" END AND
					  --"IdTipoInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumentoSubyacente" END AND
					  --"IdInstrumentoSubyacente" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumentoSubyacente" END 
				group by emp."IdEmpresa", "IdClasificacionTipoInstrumentoSubyacente", "IdTipoInstrumentoSubyacente", "IdInstrumentoSubyacente", "IdCliente", "IdPortafolio"
					,"NemoSVSSubyacente","NemoSubyacente","IdBovedaSubyacente"
	union all
	
-- GARANTIAS
	
		select      gar."IdEmpresa"
				    ,"IdClasificacionTipoInstrumento"
					,"IdTipoInstrumento" 
					,"IdInstrumento" 
					,"Nemotecnico" 
					,"IdCliente"
					,"IdPortafolio"
					,''  as "IdBovedaOperacionOrigen" 
					,0 													as "CantidadCustodia"
					,0 													as "CantidadSimulCom"
					,0 													as "CantidadSimulVen"
					,0 													as "CantidadPrestamo"
					,0 													as "CantidadEmpresti"				
					,cast(IFNULL(SUM("INGCantidad"),0) as DECIMAL(26,8)) as "CantidadGarantia"
					,0													as "CantidadTransIng"
					,0 												    as "CantidadTransEgr"						
					,"NemoSVS"                                   
				from "BSCL.Custodia.BD.Vistas::GTA.ConsultarTablaGarantia" gar
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = gar."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
				where --"IdEmpresa" = 1 AND
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  "IdEstado" <> 'ANU' AND 
					   cast(fecha."Valor" as DATE) BETWEEN "INGFechaLiquidacion" and case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end and
					  -- la siguiente, es para filtrar un solo registro en caso q 2 registros, ej: 05-02-2016 al 09-02-2016  y 09-02-2016 al 13-09-2016
					   cast(fecha."Valor" as DATE) < case when IFNULL("EGRFechaLiquidacion",'0001-01-01') = '0001-01-01' then '2300-12-31' else IFNULL("EGRFechaLiquidacion",'0001-01-01') end --and
					  ---*************************
					  --"IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END AND
					  --"IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END AND
					  --"IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumento" END
				group by gar."IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento" 
						,"NemoSVS","Nemotecnico","IdCliente", "IdPortafolio" 
		 
		 				
					
	union all					 	 
		 
-- MOVIMIENTOS EN TRANSITO INGRESOS
	select  		 movi."IdEmpresa"
				    ,"IdClasificacionTipoInstrumento"
			        ,"IdTipoInstrumento"
			        ,"IdInstrumento" 
				    ,"Nemotecnico"
				    ,"IdCliente"
				    ,"IdPortafolio"
				    ,"IdBoveda" 
					,0 													as "CantidadCustodia"
					,0 													as "CantidadSimulCom"
					,0 													as "CantidadSimulVen"
					,0 													as "CantidadPrestamo"
					,0 													as "CantidadEmpresti"				
					,0 													as "CantidadGarantia"
					,cast(IFNULL(SUM("Cantidad"),0) as DECIMAL(26,8)) 	as "CantidadTransIng"		
					,0 												as "CantidadTransEgr"		
					,"NemoSVS"
				from "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" movi
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = movi."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
				where --"IdEmpresa" = 1 AND
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  "IdEstado" = 'PEN' AND 
					  "Accion" = 'INGRESO' --AND
					  --"IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END AND
					  --"IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END AND
					  --"IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumento" END
				group by movi."IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento" 
					,"NemoSVS","Nemotecnico","IdCliente", "IdPortafolio", "IdBoveda" 
					
				union all		
-- MOVIMIENTOS EN TRANSITO EGRESOS
				select   move."IdEmpresa"
						,"IdClasificacionTipoInstrumento"
						, "IdTipoInstrumento"
						, "IdInstrumento"
						, "Nemotecnico"
						, "IdCliente"
						, "IdPortafolio"
						, "IdBoveda" 
						,0 													as "CantidadCustodia"
						,0 													as "CantidadSimulCom"
						,0 													as "CantidadSimulVen"
						,0 													as "CantidadPrestamo"
						,0 													as "CantidadEmpresti"				
						,0 													as "CantidadGarantia"
						,0 													as "CantidadTransIng"				
						,cast(IFNULL(SUM("Cantidad"),0) * -1 as DECIMAL(26,8)) as "CantidadTransEgr"
						,"NemoSVS"
				from "BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEnTransito" move
				INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConfiguracion" fecha ON fecha."IdEmpresa" = move."IdEmpresa" and fecha."CodigoParametro" = 'PLA.FechaSistema'
				where --"IdEmpresa" = 1 AND 
					  --"IdCliente" = case when p_IdCliente > 0 then p_IdCliente else "IdCliente" end AND
					  --"IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else "IdPortafolio" end AND
					  "IdEstado" = 'PEN' AND 
					  "Accion" = 'RETIRO' --AND
					  --"IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END AND
					  --"IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END AND
					  --"IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE "IdInstrumento" END
				group by move."IdEmpresa", "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento" 
						,"NemoSVS","Nemotecnico","IdCliente", "IdPortafolio","IdBoveda" 
) miTbl
group by  miTbl."IdEmpresa"
			,miTbl."IdClasificacionTipoInstrumento" 
			,miTbl."IdTipoInstrumento" 
			,miTbl."IdInstrumento" 
			,miTbl."Nemotecnico"  
			,miTbl."IdCliente" 
			,miTbl."IdPortafolio"
			,miTbl."IdBoveda"  
			,miTbl."NemoSVS"

;
GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::CUS.CustodiaPortafolioAnaliticos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO