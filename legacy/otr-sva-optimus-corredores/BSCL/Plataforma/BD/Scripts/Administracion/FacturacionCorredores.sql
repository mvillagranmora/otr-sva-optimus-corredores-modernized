set schema bscl
---Movimientos x Facturación, que mueven custodia.
--Movimientos x Garantías agrupadas por el Folio de Solicitud
Movimientos de la Mesa RF debe ser por Línea
--Aportes y Rescates, Reinversiones: Folio agrupador, Tipo Cmte, Folio Cmbte (id solicitudaporterescate), Nemo.
Vencimientos: Por Folio de Cartera.
Anticipo de Pactos y anticipo de Prestamos y Anticipo de Simultáneas 
-----------Movimientos en Tránsito x Operaciones de Nominales.

--DESAGRUPADO

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
) WHERE AGNO = 2020 AND MES = 9


select 'GARANTIA INGRESOS' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
	select "IdClasificacionTipoInstrumento",'N' as "CUENTAPROPIA", year("INGFechaOperacion") as a,month("INGFechaOperacion") as m,count(1) as c
	from 
		(select rank() over (partition by "FolioGarantia" order by "IdGarantia" asc) as ranking,*
		from "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
		where "IdOrigenGarantia" = 'CUS' and "IdEstado" != 'ANU')
	where ranking = 1
	group by "IdClasificacionTipoInstrumento","IdPortafolio","INGFechaOperacion"
	order by "INGFechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m








select distinct year("FechaOperacion"),month("FechaOperacion"),"FolioComprobante","IdTipoComprobante"
from "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
group by "FechaOperacion","FolioComprobante","IdTipoComprobante"
order by "FechaOperacion"

select year("FechaOperacion"),month("FechaOperacion"),count(1)
from "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
group by "FechaOperacion","FolioComprobante","IdTipoComprobante"

select top 10 *
from "BSCL"."BSCL.Custodia.BD::GTA.Garantia"
group by "FolioComprobante","IdTipoComprobante"




select 'CUSTODIA_X_FACTURACION' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
from
(
select year("FechaSistema") as a,month("FechaSistema") as m,count(1) as c
from "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante" 
where "ActualizarCustodia" = 'S' and "IdEstadoFacturacion" IN ('FAC','ENV') and year("FechaSistema")  ='2019' and month("FechaSistema") = '04'
group by "FechaSistema"
order by "FechaSistema"
) group by a,m




---------------------------------------- esta query vale
--select sum(movimientos) from (
select * from (
	select 'INGRESOS CUSTODIA' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select "IdClasificacionTipoInstrumento",case when e."IdPortafolio" is not null then 'S' ELSE 'N' end as "CUENTAPROPIA", year("FechaOperacion") as a,month("FechaOperacion") as m,count(1) as c
		from bscl."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraIngresosAnalitica" m
		left join bscl."BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" e on e."IdPortafolio" = m."IdPortafolio"
		group by "IdClasificacionTipoInstrumento",e."IdPortafolio","FechaOperacion"
		order by "FechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m
	union all
	select 'EGRESOS CUSTODIA' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select "IdClasificacionTipoInstrumento",case when e."IdPortafolio" is not null then 'S' ELSE 'N' end as "CUENTAPROPIA", year("FechaOperacion") as a,month("FechaOperacion") as m,count(1) as c
		from bscl."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" m
		left join bscl."BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" e on e."IdPortafolio" = m."IdPortafolio"
		group by "IdClasificacionTipoInstrumento",e."IdPortafolio","FechaOperacion"
		order by "FechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m
	union all
	select 'CUSTODIA NOMINALES' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select "IdClasificacionTipoInstrumento",case when e."IdPortafolio" is not null then 'S' ELSE 'N' end as "CUENTAPROPIA", year("FechaOperacion") as a,month("FechaOperacion") as m,count(1) as c
		from "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" m
		left join bscl."BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" e on e."IdPortafolio" = m."IdPortafolio"
		where "EsNominal" = 'S'
		group by "IdClasificacionTipoInstrumento",e."IdPortafolio","FechaOperacion"
		order by "FechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m
	union all
	select 'GARANTIA RETIROS' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
		select "IdClasificacionTipoInstrumento",'N' as "CUENTAPROPIA", year("EGRFechaOperacion") as a,month("EGRFechaOperacion") as m,count(1) as c
		from bscl."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
		where "IdOrigenGarantia"= 'CUS' and "EGRCantidad" > 0
		group by "IdClasificacionTipoInstrumento","IdPortafolio","EGRFechaOperacion"
		order by "EGRFechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m
	union all
	select 'GARANTIA INGRESOS' AS TIPO,"IdClasificacionTipoInstrumento","CUENTAPROPIA",a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
	from
	(
	select "IdClasificacionTipoInstrumento",'N' as "CUENTAPROPIA", year("INGFechaOperacion") as a,month("INGFechaOperacion") as m,count(1) as c
	from 
		(select rank() over (partition by "FolioGarantia" order by "IdGarantia" asc) as ranking,*
		from bscl."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
		where "IdOrigenGarantia" = 'CUS' and "IdEstado" != 'ANU')
	where ranking = 1
	group by "IdClasificacionTipoInstrumento","IdPortafolio","INGFechaOperacion"
	order by "INGFechaOperacion"
	) group by "IdClasificacionTipoInstrumento", "CUENTAPROPIA",a,m
)
where agno = 2020 and mes = 9


select year("FechaComprobante") as a,month("FechaComprobante"),"IdEstadoSII",COUNT(1) 
from "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
where "IdEstadoSII" = 'ACP'
GROUP BY  year("FechaComprobante"),month("FechaComprobante"),"IdEstadoSII"

--ws
select year("FechaInicio") as a,month("FechaInicio"),COUNT(1)
from "BSCL"."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
GROUP BY  year("FechaInicio"),month("FechaInicio")

--corredores, conectados
select count(1) from (
	select year(a."FechaIngreso") as year,month(a."FechaIngreso") as month,a."IdUsuario"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
	where a."IdUsuario" not in (1011,1007,1003,1005,1008,1006) and c."UsuarioClientes" = 'N'
	GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario"
) where year = 2020 and month = 9

	select distinct c."CodigoUsuario",c."IdUsuario"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
	where a."IdUsuario" not in (1011,1007,1003,1005,1008,1006) and c."UsuarioCorredores" = 'S'
	and year(a."FechaIngreso") = 2020 and month(a."FechaIngreso") = 10

--corredores, vigentes
select count(1)
from "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" a
where a."IdUsuario" not in (1011,1007,1003,1005,1008,1006) and a."UsuarioClientes" = 'N'


--clientes
select count(1) from (
	select year(a."FechaIngreso") as year,month(a."FechaIngreso") as month,a."IdUsuario"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
	where c."UsuarioClientes" = 'S'
	GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario"
) where year = 2020 and month = 9

--sap b1
select count(1) from sura.ousr



select * from "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" where "UsuarioServicios" = 'S' order by "CodigoUsuario"


select "Servicio",count(1)
from "BSCL"."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where year("FechaInicio") = 2020 and month("FechaInicio") = 9
group by "Servicio"
order by "Servicio"











 SELECT * FROM "BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica"
                     
                     
select 'CUSTODIA NOMINALES' AS TIPO,"IdClasificacionTipoInstrumento","Origen","CUENTAPROPIA",a as AGNO,m as MES,count(c) AS MOVIMIENTOS
from
(               
select "IdClasificacionTipoInstrumento",CASE "IdCarga" WHEN 690 THEN 'COREAV' ELSE 'SEBRA' END AS "Origen",case when e."IdPortafolio" is not null then 'S' ELSE 'N' end as "CUENTAPROPIA", 
	year("FechaOperacion") as a,month("FechaOperacion") as m,m."IdTipoComprobante",m."FolioComprobante",
	count(1) as c
from bscl."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" m
left join "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" e on e."IdPortafolio" = m."IdPortafolio"
where year("FechaOperacion")  ='2019' and month("FechaOperacion") = '04'
group by "IdClasificacionTipoInstrumento","IdCarga",e."IdPortafolio","FechaOperacion",m."IdTipoComprobante",m."FolioComprobante"
order by "FechaOperacion"                     
      ) group by "IdClasificacionTipoInstrumento","Origen", "CUENTAPROPIA",a,m


                     

select 'CUSTODIA NOMINALES' AS TIPO,"IdClasificacionTipoInstrumento","Origen","CUENTAPROPIA",a as AGNO,m as MES,count(c) AS MOVIMIENTOS
from
(               
select "IdClasificacionTipoInstrumento",CASE "IdCarga" WHEN 690 THEN 'COREAV' ELSE 'SEBRA' END AS "Origen",case when e."IdPortafolio" is not null then 'S' ELSE 'N' end as "CUENTAPROPIA", 
	year("FechaOperacion") as a,month("FechaOperacion") as m,m."IdTipoOperacion",m."IdTipoComprobante",m."FolioComprobante",
	count(1) as c
from bscl."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" m
left join "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia" e on e."IdPortafolio" = m."IdPortafolio"
where year("FechaOperacion")  ='2019' and month("FechaOperacion") = '04' and "IdClasificacionTipoInstrumento" = 'RV'
group by "IdClasificacionTipoInstrumento","IdCarga",e."IdPortafolio",m."IdTipoOperacion","FechaOperacion",m."IdTipoComprobante",m."FolioComprobante"
order by "FechaOperacion"                     
      ) group by "IdClasificacionTipoInstrumento","Origen", "CUENTAPROPIA",a,m


                     
                     
select "FolioCartera","IdEstado",*
from bscl."BSCL.Custodia.BD.Vistas::CUS.ConsultarMovimientoCarteraEgresosAnalitica" m                
where "FolioComprobante" = 120062
                     

                     
select "IdTipoOperacion","ActualizarCustodia",count(1) as c
from "BSCL"."BSCL.Facturacion.BD::FAC.MovimientoComprobante"
where 
 "FechaMovimientoComprobante" >= '2019-01-01' and "FechaMovimientoComprobante" < '2019-02-01'
 and "NemotecnicoInstrumento" != ''
group by "IdTipoOperacion","ActualizarCustodia"





SELECT "Fe

