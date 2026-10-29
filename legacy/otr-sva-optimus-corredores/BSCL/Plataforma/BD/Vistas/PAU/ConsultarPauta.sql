SET SCHEMA "BSCL";

DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta";

CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta"
AS

SELECT
	tip."IdTipoPrograma",
	tip."Descripcion",
	tip."MaximoIntentos",
	tip."Modulo",
	tip."NombreClasePrograma",
	tip."TipoClasePrograma",
	tip."EsPauta",
    dep."Ejecucion", -- MANUAL, PROGRAMADA, REINTENTO 
    dep."RequiereTipoPrograma",
    dep."SiguienteTipoPrograma",
    prog."IdPrograma",
	prog."IdGrupoParametro",
	prog."IdAgenda",
	prog."Intento",
	prog."IdUsuario",
	prog."idEstado" 		as "IdEstadoPrograma",
	estProg."Nombre" 		as "NombreEstadoPrograma",
	prog."Resultado" 		as "ResultadoPrograma",
	prog."fechaCreacion" 	as "FechaCreacionPrograma",
	prog."fechaEjecucion" 	as "FechaEjecucionPrograma",
	prog."fechaFin" 		as "FechaFinPrograma",
 	prog."fechaSistema"		as "FechaSistemaPrograma",
 	prog."IdProgramaReprocesado",
    coalesce(control."IdControlPauta",0) as "IdControlPauta",
	coalesce(control."FechaSistema",prog."fechaSistema") as  "FechaSistema",
	coalesce(control."FechaCreacion",prog."fechaCreacion") as "FechaCreacion",
	case prog."idEstado" when 'ERR' then estProg."Nombre" else coalesce(control."IdEstado",prog."idEstado") end as "IdEstado",
	case prog."idEstado" when 'ERR' then estProg."Nombre" else coalesce(estControl."Nombre",estProg."Nombre") end as "NombreEstado",
	--coalesce(control."IdEstado",prog."idEstado") as "IdEstado", -- PEN, TER, ERR
	case prog."idEstado" when 'ERR' then 'Error al ejecutar programa' else coalesce(control."Mensaje",'Esperando registros de control') end as "Mensaje",
	agenda."EsProcesoManual",
	procesoPau."Orden"
	--coalesce(control."Mensaje",'Esperando registros de control') as "Mensaje"
FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" tip
LEFT JOIN "BSCL.Plataforma.BD::PAU.Dependencia" dep 
	on tip."IdTipoPrograma" = dep."IdTipoPrograma"
left join "BSCL.Plataforma.BD::BAT.Programa" prog on prog."IdTipoPrograma" = dep."IdTipoPrograma"
--ultimo registro ControlPauta
left join (select max("IdControlPauta") as "IdControlPauta","IdPrograma" from "BSCL.Plataforma.BD::PAU.ControlPauta" group by "IdPrograma") controlMax 
	on prog."IdPrograma"=controlMax."IdPrograma"
left join "BSCL.Plataforma.BD::PAU.ControlPauta" control on control."IdControlPauta" = controlMax."IdControlPauta"
left join "BSCL.Plataforma.BD::PLA.Estado" estProg on estProg."IdEstado" = prog."idEstado"
left join "BSCL.Plataforma.BD::PLA.Estado" estControl on estControl."IdEstado" = control."IdEstado"
left join "BSCL.Plataforma.BD::BAT.Agenda" agenda on agenda."IdTipoPrograma" = tip."IdTipoPrograma"
join "BSCL.Plataforma.BD::PAU.Proceso" procesoPau on (procesoPau."CodigoProceso" = tip."IdTipoPrograma" AND procesoPau."CodigoModulo" = 'SIS')
where tip."EsPauta" = 'S'
-- DATO DEFAULT PARA VISUALIZAR EN GRILLA
union all 
select
	tpdef."IdTipoPrograma",
	tpdef."Descripcion",
	tpdef."MaximoIntentos",
	tpdef."Modulo",
	tpdef."NombreClasePrograma",
	tpdef."TipoClasePrograma",
	tpdef."EsPauta",
    ddef."Ejecucion",
    ddef."RequiereTipoPrograma",
    ddef."SiguienteTipoPrograma",
    0	as "IdPrograma",
	0 as "IdGrupoParametro",
	agdef."IdAgenda",
	0 as "Intento",
	'0' as"IdUsuario",
	'TER' as "IdEstadoPrograma",
	'TERMINADO' as "NombreEstadoPrograma",
	'Programa no agendado para la fecha' as "ResultadoPrograma",
	'0001-01-01 00:00:00' 	as "FechaCreacionPrograma",
	'0001-01-01 00:00:00' 	as "FechaEjecucionPrograma",
	'0001-01-01 00:00:00' 	as "FechaFinPrograma",
 	'0001-01-01'			as "FechaSistemaPrograma",
 	null					as "IdProgramaReprocesado",
    null					as "IdControlPauta",
	'0001-01-01'			as "FechaSistema",
	'0001-01-01'			as "FechaCreacion",
	'TER' 					as "IdEstado",
	'TERMINADO' 			as "NombreEstado",
	'Programa no agendado para la fecha' as "Mensaje",
	agdef."EsProcesoManual",
	procesoPau."Orden"
from "BSCL.Plataforma.BD::BAT.TipoPrograma" tpdef
left join "BSCL.Plataforma.BD::BAT.Agenda" agdef 
	on agdef."IdTipoPrograma" = tpdef."IdTipoPrograma"
left join "BSCL.Plataforma.BD::PAU.Dependencia"ddef 
	on tpdef."IdTipoPrograma" = ddef."IdTipoPrograma"
join "BSCL.Plataforma.BD::PAU.Proceso" procesoPau on (procesoPau."CodigoProceso" = tpdef."IdTipoPrograma" AND procesoPau."CodigoProceso" = 'SIS')
where tpdef."EsPauta" = 'S';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarPauta" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
