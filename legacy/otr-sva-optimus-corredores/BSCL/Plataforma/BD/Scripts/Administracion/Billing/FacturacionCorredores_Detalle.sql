set schema bscl;

--RESUMEN
SELECT * FROM (
	select 'PACTOS' AS TIPO,a as AGNO,m as MES,count(1) AS MOVIMIENTOS
	from
	(
		select distinct c."FolioCartera" as f, year(c."FechaOperacion") as a,month(c."FechaOperacion") as m,s."FolioSubyacente"
		from "BSCL"."BSCL.Custodia.BD::CUS.Subyacente" s
		left join "BSCL"."BSCL.Custodia.BD::CUS.Cartera" c on s."FolioCarteraRelacionada" = c."FolioCartera"
		where c."IdTipoInstrumento" = 'PAC'
		group by c."FolioCartera",s."FolioSubyacente",c."FechaOperacion"
		order by c."FolioCartera"
	) group by a,m
	union
	select 'CUENTA_INVERSION' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select year("FechaSistema") as a,month("FechaSistema") as m,count(1) as c
		from "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
		group by "FechaSistema"
		order by "FechaSistema"
	) group by a,m
	union
	select 'CUSTODIA_X_FACTURACION_CUSTODIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select year("FechaSistema") as a,month("FechaSistema") as m,count(1) as c
		from "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
		where "ActualizarCustodia" = 'S' and "NemotecnicoInstrumento" != '' and "IdEstadoFacturacion" IN ('FAC','ENV')
		and "IdTipoOperacion" NOT IN ('VENSPT','COMSPT')
		group by "FechaSistema"
		order by "FechaSistema"
	) group by a,m
	union
	select 'CUSTODIA_X_FACTURACION_NOMINAL' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select year("FechaOperacion") as a,month("FechaOperacion") as m,count(1) as c
		from "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" m
		where "EsNominal" = 'S'
		group by "FechaOperacion"
		order by "FechaOperacion"
	) group by a,m
	union
	select 'RETIROS_GARANTIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select year("EGRFechaOperacion") as a,month("EGRFechaOperacion") as m,count(1) as c
		from bscl."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
		where "IdOrigenGarantia"= 'CUS' and "EGRCantidad" > 0
		group by "IdClasificacionTipoInstrumento","IdPortafolio","EGRFechaOperacion"
		order by "EGRFechaOperacion"
	) group by a,m
	union
	select 'INGRESOS_GARANTIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select year("INGFechaOperacion") as a,month("INGFechaOperacion") as m,count(1) as c
		from 
		(
			select rank() over (partition by "FolioGarantia" order by "IdGarantia" asc) as ranking,*
			from bscl."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
			where "IdOrigenGarantia" = 'CUS' and "IdEstado" != 'ANU')
		where ranking = 1
		group by "IdClasificacionTipoInstrumento","IdPortafolio","INGFechaOperacion"
	) group by a,m
	union
	select 'MOV_CUSTODIA_X_FFMM' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select distinct year("FechaCertificacion") as a,month("FechaCertificacion") as m,count(1) as c
		from "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
		group by "FechaCertificacion"
		order by "FechaCertificacion"
	) group by a,m
) WHERE AGNO >= 2021
ORDER BY TIPO, AGNO, MES


--DETALLE MOVIMIENTOS RV
SELECT year("FechaSistema") AS agno,month("FechaSistema") AS mes,dayofmonth("FechaSistema") AS dia,"IdTipoOperacion","NemotecnicoInstrumento",count(1) AS MOVIMIENTOS FROM 
"BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
WHERE "ActualizarCustodia" = 'S' and "NemotecnicoInstrumento" != '' and "IdEstadoFacturacion" IN ('FAC','ENV')
		and "IdTipoOperacion" NOT IN ('VENSPT','COMSPT') AND year("FechaSistema") >= 2021
group by "IdTipoOperacion","NemotecnicoInstrumento",year("FechaSistema"),month("FechaSistema"),dayofmonth("FechaSistema")
order by agno, mes,dia,"IdTipoOperacion","NemotecnicoInstrumento" desc

--TOP 10 NEMOS
SELECT top 10 "IdTipoOperacion","NemotecnicoInstrumento",count(1) AS MOVIMIENTOS FROM 
"BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
WHERE year("FechaSistema") = 2021 AND month("FechaSistema") = 10  AND dayofmonth("FechaSistema") = 14
and "ActualizarCustodia" = 'S' and "NemotecnicoInstrumento" != '' and "IdEstadoFacturacion" IN ('FAC','ENV')
		and "IdTipoOperacion" NOT IN ('VENSPT','COMSPT')
group by "IdTipoOperacion","NemotecnicoInstrumento"
order by MOVIMIENTOS desc
 
--DETALLE MOVIMIENTOS FFMM
select year("FechaCertificacion") as agno,month("FechaCertificacion") as mes,dayofmonth("FechaCertificacion") AS dia,"IdTipoMovimientoFondoMutuo",count(1) as c
		from "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
		WHERE year("FechaCertificacion") > 2020
		group by year("FechaCertificacion"),month("FechaCertificacion"),"IdTipoMovimientoFondoMutuo",dayofmonth("FechaCertificacion")
		order by agno, mes, dia
		
		
		
		
		
		
