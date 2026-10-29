SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia";
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia" ( 
	"IdInstrumentoSubyacente",
	 "IdBolsa",
	 "IdTipoInstrumento",
	 "IdTipoInstrumentoSubyacente",
	 "NombreTipoInstrumentoSubyacente",
	 "IdTipoClasificacionRiesgo",
	 "NombreTipoInstrumento",
	 "Nemotecnico",
	 "ExigibilidadEntidad",
	 "ExigibilidadCorredora",
	 "IdExigibilidadGarantia",
	 "ClasificacionRiesgo",
	 "IdInstrumento",
	 "IdEntidadClasificadoraRiesgo" 
	 ) AS 
 (((SELECT -- PRESTAMOS
	 TipoInstrumentoSubyacente."IdInstrumentoSubyacente" ,
	TipoInstrumentoSubyacente."IdBolsa" ,
	TipoInstrumentoSubyacente."IdTipoInstrumento" ,
	TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" ,
	TipoInstrumentoSub."Nombre" AS "NombreTipoInstrumentoSubyacente" ,
	ClasificacionRiesgo."IdTipoClasificacionRiesgo" ,
	TipoInstrumento."Nombre" as "NombreTipoInstrumento" ,
	Instrumento."Nemotecnico" AS "Nemotecnico" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadEntidad",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadEntidad" end as "ExigibilidadEntidad" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadCorredora",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadCorredora" end as "ExigibilidadCorredora" ,
	CASE WHEN ifnull(ExigibilidadGarantia."IdExigibilidadGarantia",	-1) = -1 THEN 0 ELSE ExigibilidadGarantia."IdExigibilidadGarantia" end as "IdExigibilidadGarantia" ,
	ClasificacionRiesgo."ClasificacionRiesgo" ,
	Instrumento."IdInstrumento" ,
	COALESCE (ExigibilidadGarantia."IdEntidadClasificadoraRiesgo", TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo",'') AS "IdEntidadClasificadoraRiesgo" 
from "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" as TipoInstrumentoSubyacente 
inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" as BolsaTipoInstrumento 
 on BolsaTipoInstrumento."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
 and BolsaTipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
 and BolsaTipoInstrumento."ExigeGarantia" = 'S' 
 and BolsaTipoInstrumento."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa 
 on Bolsa."IdBolsa" = BolsaTipoInstrumento."IdBolsa" 
 and Bolsa."IdEstado" ='ACT' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento 
 on TipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
 and TipoInstrumento."IdEstado" ='VIG' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumentoSub 
 on TipoInstrumentoSub."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
 and TipoInstrumentoSub."IdEstado" ='VIG' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" as TipoInstrumentoClasificacionRiesgo 
 on TipoInstrumentoClasificacionRiesgo."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
 and TipoInstrumentoClasificacionRiesgo."IdEstado"= 'ACT' 
inner join "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" as ClasificacionRiesgo 
 on ClasificacionRiesgo."IdTipoClasificacionRiesgo" = TipoInstrumentoClasificacionRiesgo."IdTipoClasificacionRiesgo" 
 and ClasificacionRiesgo."IdEntidadClasificadoraRiesgo" = TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo" 
 and ClasificacionRiesgo."IdEstado" ='ACT' 
 and ClasificacionRiesgo."ClasificacionRiesgo" not in ('C','D') 
inner join "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento 
 on Instrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
 and Instrumento."IdTipoInstrumento" IN ('PRT', 'PRF') 
left join (select	"IdBolsa" ,
					"IdTipoInstrumento" ,
					"IdTipoInstrumentoSubyacente" ,
					"ExigibilidadEntidad" ,
					"ExigibilidadCorredora" ,
					"IdExigibilidadGarantia" ,
					"Nemo" ,
					"IdInstrumento" ,
					"ClasificacionRiesgo" ,
					"IdEntidadClasificadoraRiesgo" ,
					"IdTipoClasificacionRiesgo" 
								from "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" 
								where "IdEstado" ='ACT') as ExigibilidadGarantia on ExigibilidadGarantia."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
																				and ExigibilidadGarantia."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
																				and ExigibilidadGarantia."IdTipoInstrumentoSubyacente" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
																				and ExigibilidadGarantia."IdInstrumento" = Instrumento."IdInstrumento" 
																				and ExigibilidadGarantia."IdEntidadClasificadoraRiesgo" = TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo" 
																				and ExigibilidadGarantia."ClasificacionRiesgo" = ClasificacionRiesgo."ClasificacionRiesgo" 
																				and ExigibilidadGarantia."IdTipoClasificacionRiesgo" = ClasificacionRiesgo."IdTipoClasificacionRiesgo" 
where TipoInstrumentoSubyacente."IdEstado" = 'ACT') 
UNION ALL  (
SELECT -- SIMULTANEAS
	 TipoInstrumentoSubyacente."IdInstrumentoSubyacente" ,
	TipoInstrumentoSubyacente."IdBolsa" ,
	TipoInstrumentoSubyacente."IdTipoInstrumento" ,
	TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" ,
	TipoInstrumentoSub."Nombre" AS "NombreTipoInstrumentoSubyacente" ,
	ClasificacionRiesgo."IdTipoClasificacionRiesgo" ,
	TipoInstrumento."Nombre" as "NombreTipoInstrumento" ,
	Instrumento."Nemotecnico" AS "Nemotecnico" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadEntidad",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadEntidad" end as "ExigibilidadEntidad" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadCorredora",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadCorredora" end as "ExigibilidadCorredora" ,
	CASE WHEN ifnull(ExigibilidadGarantia."IdExigibilidadGarantia",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."IdExigibilidadGarantia" end as "IdExigibilidadGarantia" ,
	ClasificacionRiesgo."ClasificacionRiesgo" ,
	Instrumento."IdInstrumento" ,
	COALESCE (ExigibilidadGarantia."IdEntidadClasificadoraRiesgo",TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo",'') 
from "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" as TipoInstrumentoSubyacente 
inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" as BolsaTipoInstrumento 
 on BolsaTipoInstrumento."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
 and BolsaTipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
 and BolsaTipoInstrumento."ExigeGarantia" = 'S' 
 and BolsaTipoInstrumento."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa 
 on Bolsa."IdBolsa" = BolsaTipoInstrumento."IdBolsa" 
 and Bolsa."IdEstado" ='ACT' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento 
 on TipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
 and TipoInstrumento."IdEstado" ='VIG' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumentoSub 
 on TipoInstrumentoSub."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
 and TipoInstrumentoSub."IdEstado" ='VIG' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumentoClasificacionRiesgo" as TipoInstrumentoClasificacionRiesgo 
 on TipoInstrumentoClasificacionRiesgo."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
 and TipoInstrumentoClasificacionRiesgo."IdEstado"= 'ACT' 
 AND tipoInstrumentoClasificacionRiesgo."EntidadClasificadoraRiesgoPrincipal" = 'S'
inner join "BSCL.Plataforma.BD::INS.ClasificacionRiesgo" as ClasificacionRiesgo 
 on ClasificacionRiesgo."IdTipoClasificacionRiesgo" = TipoInstrumentoClasificacionRiesgo."IdTipoClasificacionRiesgo" 
 and ClasificacionRiesgo."IdEntidadClasificadoraRiesgo" = TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo" 
 and ClasificacionRiesgo."IdEstado" ='ACT' 
inner join "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento 
 on Instrumento."IdInstrumento" = TipoInstrumentoSubyacente."IdInstrumento" 
 and Instrumento."IdTipoInstrumento"  in ('SIM', 'RRV' ) --= 'SIM' 
left join (select	 "IdBolsa" ,
					"IdTipoInstrumento" ,
					"IdTipoInstrumentoSubyacente" ,
					"ExigibilidadEntidad" ,
					"ExigibilidadCorredora" ,
					"IdExigibilidadGarantia" ,
					"Nemo" ,
					"IdInstrumento" ,
					"IdEntidadClasificadoraRiesgo" ,
					"ClasificacionRiesgo" ,
					"IdTipoClasificacionRiesgo" 
					from "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" 
					where "IdEstado" ='ACT') as ExigibilidadGarantia on ExigibilidadGarantia."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
																	and ExigibilidadGarantia."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
																	and ExigibilidadGarantia."IdTipoInstrumentoSubyacente" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
																	and ExigibilidadGarantia."IdInstrumento" = Instrumento."IdInstrumento" 
																	and ExigibilidadGarantia."IdEntidadClasificadoraRiesgo" = TipoInstrumentoClasificacionRiesgo."IdEntidadClasificadoraRiesgo" 
																	and ExigibilidadGarantia."ClasificacionRiesgo" = ClasificacionRiesgo."ClasificacionRiesgo" 
																	and ExigibilidadGarantia."IdTipoClasificacionRiesgo" = ClasificacionRiesgo."IdTipoClasificacionRiesgo" 
where TipoInstrumentoSubyacente."IdEstado" = 'ACT')) 
UNION ALL (
select	 -- consulta para recuperar futuros / REPORTES
	TipoInstrumentoSubyacente."IdInstrumentoSubyacente" ,
	TipoInstrumentoSubyacente."IdBolsa" ,
	TipoInstrumentoSubyacente."IdTipoInstrumento" ,
	TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" ,
	TipoInstrumentoSub."Nombre" AS "NombreTipoInstrumentoSubyacente" ,
	'' AS "IdTipoClasificacionRiesgo" ,
	TipoInstrumento."Nombre" as "NombreTipoInstrumento" ,
	Instrumento."Nemotecnico" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadEntidad",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadEntidad" end as "ExigibilidadEntidad" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadCorredora",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadCorredora" end as "ExigibilidadCorredora" ,
	CASE WHEN ifnull(ExigibilidadGarantia."IdExigibilidadGarantia",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."IdExigibilidadGarantia" end as "IdExigibilidadGarantia" ,
	'' as "ClasificacionRiesgo" ,
	Instrumento."IdInstrumento" ,
	'' as "IdEntidadClasificadoraRiesgo" 
from "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" as TipoInstrumentoSubyacente /*inner*/ 
inner join "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento" as GrupoTipoInstrumento 
	on GrupoTipoInstrumento."CodigoClasificacion" ='FUTUROS' 
	and GrupoTipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" /*inner*/ 
INNER join "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento 
	on Instrumento."IdTipoInstrumento" = GrupoTipoInstrumento."IdTipoInstrumento" 
	and Instrumento."IdEstado" = 'ACT' 
left join "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia" as ExigibilidadGarantia 
	on ExigibilidadGarantia."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
	and ExigibilidadGarantia."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
	and ExigibilidadGarantia."IdTipoInstrumentoSubyacente" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
	and ExigibilidadGarantia."Nemo" = Instrumento."Nemotecnico" 
	and ExigibilidadGarantia."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" as BolsaTipoInstrumento 
	on BolsaTipoInstrumento."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
	and BolsaTipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
	and BolsaTipoInstrumento."ExigeGarantia" = 'S' 
	and BolsaTipoInstrumento."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa 
	on Bolsa."IdBolsa" = BolsaTipoInstrumento."IdBolsa" 
	and Bolsa."IdEstado" = 'ACT' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumento 
	on TipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
	and TipoInstrumento."IdEstado" ='VIG' 
inner join "BSCL.Plataforma.BD::INS.TipoInstrumento" as TipoInstrumentoSub 
	on TipoInstrumentoSub."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumentoSubyacente" 
	and TipoInstrumentoSub."IdEstado" ='VIG')) 
	
UNION ALL (
--Derivados

SELECT 
    TipoInstrumentoSubyacente."IdInstrumentoSubyacente" ,
	TipoInstrumentoSubyacente."IdBolsa" ,
	TipoInstrumentoSubyacente."IdTipoInstrumento" ,
	TipoInstrumento."IdTipoInstrumento" AS "IdTipoInstrumentoSubyacente",
	TipoInstrumento."Nombre" AS "NombreTipoInstrumentoSubyacente" ,
	''AS "IdTipoClasificacionRiesgo" ,
	TipoInstrumento."Descripcion" as "NombreTipoInstrumento" ,
	Instrumento."Nemotecnico" AS "Nemotecnico" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadEntidad",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadEntidad" end as "ExigibilidadEntidad" ,
	CASE WHEN ifnull(ExigibilidadGarantia."ExigibilidadCorredora",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."ExigibilidadCorredora" end as "ExigibilidadCorredora" ,
	CASE WHEN ifnull(ExigibilidadGarantia."IdExigibilidadGarantia",-1) = -1 THEN 0 ELSE ExigibilidadGarantia."IdExigibilidadGarantia" end as "IdExigibilidadGarantia" ,
	'' AS "ClasificacionRiesgo",
	Instrumento."IdInstrumento",
	Entidad."IdEntidad" AS "IdEntidadClasificadoraRiesgo" 
FROM "BSCL.Plataforma.BD::INS.TipoInstrumentoSubyacente" AS TipoInstrumentoSubyacente 
INNER JOIN "BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" AS BolsaTipoInstrumento 
	ON  BolsaTipoInstrumento."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
	AND BolsaTipoInstrumento."IdTipoInstrumento" = TipoInstrumentoSubyacente."IdTipoInstrumento" 
	AND BolsaTipoInstrumento."ExigeGarantia" = 'S' 
	AND BolsaTipoInstrumento."IdEstado" = 'ACT'
INNER JOIN "BSCL.Custodia.BD::GTA.EntidadDestinoGarantia" AS Entidad 
	ON Entidad."IdEntidadDestinoGarantia" = BolsaTipoInstrumento."IdEntidadDestinoGarantia"
INNER JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" AS TipoInstrumento
	ON TipoInstrumento."Codigo" = TipoInstrumentoSubyacente."IdTipoInstrumento"
INNER JOIN "BSCL.Plataforma.BD::INS.Bolsa" as Bolsa 
	ON  Bolsa."IdBolsa" = BolsaTipoInstrumento."IdBolsa" 
	AND Bolsa."IdEstado" ='ACT'  
INNER JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Instrumento 
	ON Instrumento."IdTipoInstrumento" = TipoInstrumento."IdTipoInstrumento" 
	AND Instrumento."IdTipoInstrumento"  in ('FDO','FIP','FBC','FIC','FUF','IOA','IOE','IOI','IOD','IOB','IOC','IOU','IOM','IOF','FBO') 
LEFT JOIN "BSCL.Plataforma.BD::GTA.ExigibilidadGarantia"  as ExigibilidadGarantia on ExigibilidadGarantia."IdEstado"='ACT'
															AND ExigibilidadGarantia."IdBolsa" = TipoInstrumentoSubyacente."IdBolsa" 
															and ExigibilidadGarantia."IdInstrumento" = Instrumento."IdInstrumento" 
																	 
																	
where TipoInstrumentoSubyacente."IdEstado" = 'ACT'
)
WITH READ ONLY
GO
GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::ConsultarExigenciaGarantia"  TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO