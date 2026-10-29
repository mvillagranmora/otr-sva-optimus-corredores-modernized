SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites"  
AS

SELECT 
	PL."IdPlantillaLimite",
	PL."DescripcionPlantillaLimite",
	PL."FechaCreacion" AS "FechaCreacionPlantilla",
	PL."FechaModificacion" AS "FechaModificacionPlantilla",
	--PL."IdEstado" as "IdEstadoPlantilla",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITE',PL."IdEstado",PL."IdEstadoAutorizacion") AS "IdEstadoPlantilla",
	PL."PorDefecto",
	PL."IdEmpresa",
	DPL."IdLimite",
	DPL."IdTipoLimite",
	DPL."IdMesa",
	DPL."IdCanalIngreso",
	CASE DPL."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TCI."Nombre" END as "CanalIngreso",
	DPL."IdTipoInstrumento",
	IFNULL(TIPINS."Nombre",' -') AS "TipoInstrumento",
	DPL."IdInstrumento",
	DPL."IdTipoProducto",
	TIPPROD."Nombre" as "TipoProducto",
	DPL."IdTipoOperacion",
	IFNULL(TIPOP."Nombre",' -') AS "TipoOperacion",
	DPL."UnidadPlazo",
	DPL."Plazo",
	DPL."IdTipoReajuste",
	DPL."CodigoOperador",
	DPL."MontoTotal", --LIMITE TOTAL
	DPL."MontoIntraday",
    DPL."MontoOvernight",
	DPL."IdMoneda",
	MDA."NombreLargo" AS "Moneda",
	MDA."Nombre" AS "NombreMoneda",
	DPL."IdMonedaCalculo",
	MDACAL."NombreLargo" AS "MonedaCalculo",
    DPL."ClasificacionRiesgoMinima",
	--DPL."IdEstado" as "EstadoProducto",
	"BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion") AS "IdEstado",
	ESTDET."Nombre" as "NombreEstadoLimite",
	DPL."EsPersonalizada", -- S o N
	DPL."FechaVigencia",
	DPL."EsTotalizador", -- S o N
	DPL."FechaIngreso",
    DPL."FechaCreacion" AS "FechaCreacionDetallePlantilla",
    DPL."FechaModificacion" AS "FechaModificacionDetallePlantilla",
	DPL."IdEstadoAutorizacion",
	DPL."Nombre",
	DPL."UI_Limite",
	DPL."IdClasificacionRiesgo",
	DPL."IdAgente",
	DPL."IdClasificacionTipoInstrumento",
	IFNULL(CLATI."Nombre",' -') AS "NombreClasificacionTipoInstrumento",
	DPL."Porcentaje",
	DPL."IdGrupoEmisor"
FROM
	"BSCL.Plataforma.BD::LIM.PlantillaLimite" AS PL
	INNER JOIN "BSCL.Plataforma.BD::LIM.Limite"	AS DPL ON (PL."IdPlantillaLimite" = DPL."IdPlantillaLimite")
	LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLATI ON (CLATI."IdClasificacionTipoInstrumento" = DPL."IdClasificacionTipoInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"			AS TIPINS ON (TIPINS."IdTipoInstrumento" = DPL."IdTipoInstrumento")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"			AS TIPOP ON (TIPOP."IdTipoOperacion" = DPL."IdTipoOperacion")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"		AS TCI ON (TCI."IdTipoCanalIngreso" = DPL."IdCanalIngreso")
	LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"			AS TIPPROD ON (TIPPROD."IdTipoProducto" = DPL."IdTipoProducto")
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"					AS ESTDET ON (ESTDET."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion"))
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"	AS MDA ON (MDA."IdMoneda" = DPL."IdMoneda")
	LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"	AS MDACAL ON (MDACAL."IdMoneda" = DPL."IdMonedaCalculo");

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarDetallePlantillaLimites" TO BCS_DESARROLLADOR WITH GRANT OPTION;
