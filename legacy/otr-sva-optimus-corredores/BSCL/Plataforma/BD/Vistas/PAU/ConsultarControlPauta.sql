SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlPauta"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlPauta" 
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
	case prog."idEstado" when 'ERR' then 'ERR' else coalesce(control."IdEstado",prog."idEstado") end as "IdEstado",
	case prog."idEstado" when 'ERR' then estProg."Nombre" else coalesce(estControl."Nombre",estProg."Nombre") end as "NombreEstado",
	--coalesce(control."IdEstado",prog."idEstado") as "IdEstado", -- PEN, TER, ERR
	case prog."idEstado" when 'ERR' then 'Error al ejecutar programa' else coalesce(control."Mensaje",'Esperando registros de control') end as "Mensaje"
	--coalesce(control."Mensaje",'Esperando registros de control') as "Mensaje"
FROM "BSCL.Plataforma.BD::BAT.TipoPrograma" tip
JOIN "BSCL.Plataforma.BD::PAU.Dependencia" dep 
	on tip."IdTipoPrograma" = dep."IdTipoPrograma"
left join "BSCL.Plataforma.BD::BAT.Programa" prog on prog."IdTipoPrograma" = dep."IdTipoPrograma"
left join "BSCL.Plataforma.BD::PAU.ControlPauta" control on control."IdPrograma" = prog."IdPrograma"
left join "BSCL.Plataforma.BD::PLA.Estado" estProg on estProg."IdEstado" = prog."idEstado"
left join "BSCL.Plataforma.BD::PLA.Estado" estControl on estControl."IdEstado" = control."IdEstado"
where tip."EsPauta" = 'S';
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlPauta" TO  BCS_DESARROLLADOR WITH GRANT OPTION;

--select * from "BSCL.Plataforma.BD.Vistas::PAU.ConsultarControlPauta";
