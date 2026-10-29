--MOVIMIENTOS AGRUPADOS (ESTA SE USA)
SELECT COALESCE(TIPO,'OPTMOVTOSTOTALES') AS TIPO,': ',MOVIMIENTOS FROM ( 
	SELECT TIPO,SUM(MOVIMIENTOS) AS MOVIMIENTOS FROM (
		select 'OPTMVTOSCAJA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
		from
		(
			select year("FechaSistema") as a,month("FechaSistema") as m,count(1) as c
			from "BSCL"."BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
			group by "FechaSistema"
			order by "FechaSistema"
		) group by a,m
		union
		select 'OPTMOVTOSCUSTODIA' AS TIPO,a as AGNO,m as MES,count(1) AS MOVIMIENTOS
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
		select 'OPTMOVTOSCUSTODIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
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
		select 'OPTMOVTOSNOMINAL' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
		from
		(
			select year("FechaOperacion") as a,month("FechaOperacion") as m,count(1) as c
			from "BSCL"."BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" m
			where "EsNominal" = 'S'
			group by "FechaOperacion"
			order by "FechaOperacion"
		) group by a,m
		union
		select 'OPTMOVTOSCUSTODIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
		from
		(
			select year("EGRFechaOperacion") as a,month("EGRFechaOperacion") as m,count(1) as c
			from bscl."BSCL.Custodia.BD.Vistas::GTA.ConsultarGarantiaAnalitica" m
			where "IdOrigenGarantia"= 'CUS' and "EGRCantidad" > 0
			group by "IdClasificacionTipoInstrumento","IdPortafolio","EGRFechaOperacion"
			order by "EGRFechaOperacion"
		) group by a,m
		union
		select 'OPTMOVTOSCUSTODIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
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
		select 'OPTMOVTOSCUSTODIA' AS TIPO,a as AGNO,m as MES,sum(c) AS MOVIMIENTOS
		from
		(
			select year("FechaCertificacion") as a,month("FechaCertificacion") as m,count(1) as c
			from "BSCL"."BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate"
			group by "FechaCertificacion"
			order by "FechaCertificacion"
		) group by a,m
	)
	WHERE AGNO = :AGNO AND MES = :MES
	GROUP BY ROLLUP(TIPO)
)
UNION
--corredores conectados
select 'OPTCUENTASUSER',': ',count(1) AS MOVIMIENTOS from (
	select year(a."FechaIngreso") as year,month(a."FechaIngreso") as month,a."IdUsuario","CodigoUsuario","IdEntidad"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
	LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica" emp ON emp."IdEmpleado" = c."IdEntidad"
	WHERE c."EsUsuarioBolsa" = 'N' AND year(a."FechaIngreso") = :AGNO and month(a."FechaIngreso") = :MES AND (c."UsuarioCorredores" = 'S') AND a."IdCanal" IN ('COR','SSO')
	--AND year(a."FechaIngreso") = 2022 AND month(a."FechaIngreso") = 8
	GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario","CodigoUsuario","IdEntidad" ORDER BY "CodigoUsuario"
)
UNION 
--negocios conectados
select 'OPTCUENTASNEGOCIOS',': ',count(1) AS MOVIMIENTOS from (
	select year(a."FechaIngreso") as year,month(a."FechaIngreso") as month,a."IdUsuario","CodigoUsuario","IdEntidad","IdCanal"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
	LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica" emp ON emp."IdEmpleado" = c."IdEntidad"
	where c."EsUsuarioBolsa" = 'N' AND a."IdCanal" = 'AGE' AND (c."UsuarioNegocios" = 'S') AND year(a."FechaIngreso") = :AGNO and month(a."FechaIngreso") = :MES
	AND "CodigoUsuario" NOT IN (
		select "CodigoUsuario"
		from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
		left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
		LEFT JOIN "BSCL"."BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleadoAnalitica" emp ON emp."IdEmpleado" = c."IdEntidad"
		where c."EsUsuarioBolsa" = 'N' AND a."IdCanal" = 'COR' AND (c."UsuarioCorredores" = 'S') AND year(a."FechaIngreso") = :AGNO and month(a."FechaIngreso") = :MES
		--AND year(a."FechaIngreso") = 2022 AND month(a."FechaIngreso") = 8
		GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario","CodigoUsuario","IdEntidad","IdCanal" ORDER BY "CodigoUsuario"	
	)
	--AND year(a."FechaIngreso") = 2022 AND month(a."FechaIngreso") = 8
	GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario","CodigoUsuario","IdEntidad","IdCanal" ORDER BY "CodigoUsuario"
)
UNION
--usuarios clientes
select 'OPTCUENTASCLIENTES',': ',count(1) AS MOVIMIENTOS from (
	select year(a."FechaIngreso") as year,month(a."FechaIngreso") as month,a."IdUsuario",cli."Nombres",cli."ApellidoPaterno",cli."RazonSocial"
	from "BSCL"."BSCL.Plataforma.BD::ACC.Sesion" a
	left join "BSCL"."BSCL.Plataforma.BD::ACC.Usuario" c on a."IdUsuario" = c."IdUsuario"
		LEFT JOIN "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolioAnalitica" cli ON cli."IdCliente" = c."IdEntidad"
	where C."EsUsuarioBolsa" = 'N' AND (c."UsuarioClientes" = 'S')-- AND a."IdCanal" = 'CLI' 
	GROUP BY  year(a."FechaIngreso"),month(a."FechaIngreso"),a."IdUsuario",cli."Nombres",cli."ApellidoPaterno",cli."RazonSocial"
	ORDER BY YEAR, MONTH,"IdUsuario"
) where year = :AGNO and month = :MES
UNION all
--dte
select 'OPTDOCUMENTOSDTE',': ',COUNT(1) AS MOVIMIENTOS
from "BSCL"."BSCL.Facturacion.BD::FAC.Comprobante"
where "IdEstadoSII" = 'ACP' AND year("FechaComprobante") = :AGNO AND month("FechaComprobante") = :MES
GROUP BY  year("FechaComprobante"),month("FechaComprobante"),"IdEstadoSII"
UNION 
--ws
select 'OPTHITSWEBSERVICES',': ',COUNT(1)
from "BSCL"."BSCL.Plataforma.BD::PLA.AuditoriaWebServices"
where  NOT CONTAINS("Entrada",'monitor',EXACT) AND NOT CONTAINS("Entrada",'monitor2',EXACT) AND year("FechaInicio") = :AGNO AND month("FechaInicio") = :MES
GROUP BY  year("FechaInicio"),month("FechaInicio")
		