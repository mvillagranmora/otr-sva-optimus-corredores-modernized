SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimitePortafolio"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimitePortafolio" 
AS
SELECT
	LIMPOR."IdCliente",
	LIMPOR."IdPortafolio",
   	LIMPOR."PorDefecto",		    
	LIMPOR."IdPlantillaLimite",
	LIMPOR."IdLimite",
	LIMPOR."IdTipoLimite",
	TPO."Nombre" AS "NombreTipoLimite",
	LIMPOR."IdTipoInstrumento",
	COALESCE(TIPINS."Nombre",' -') AS "TipoInstrumento",
	LIMPOR."IdClasificacionTipoInstrumento",
	IFNULL(CLATI."Nombre",' -') AS "NombreClasificacionTipoInstrumento",
	LIMPOR."IdTipoProducto",
	LIMPOR."IdTipoOperacion",
	IFNULL(TIPOP."Nombre",' -') AS "TipoOperacion",
	LIMPOR."Nombre",
	LIMPOR."IdEstado",
	ESTDET."Nombre" as "NombreEstadoLimite",
	LIMPOR."IdInstrumento",
	LIMPOR."UnidadPlazo",
	LIMPOR."Plazo",
	LIMPOR."IdTipoReajuste",
	LIMPOR."CodigoOperador",
	LIMPOR."MontoTotal",
	LIMPOR."MontoIntraday",
	LIMPOR."MontoOvernight",
	LIMPOR."IdMoneda",
	IFNULL(MDA."NombreLargo",' -') AS "Moneda",
	LIMPOR."IdMonedaCalculo",
	IFNULL(MDACAL."NombreLargo",' -') AS "MonedaCalculo",
	LIMPOR."ClasificacionRiesgoMinima",
	LIMPOR."EsPersonalizada",
	LIMPOR."FechaVigencia",
	LIMPOR."EsTotalizador",
	LIMPOR."IdMesa",
	LIMPOR."IdCanalIngreso",
	CASE LIMPOR."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE COALESCE(TCI."Nombre",' -') END AS "CanalIngreso",
	LIMPOR."IdAgente",
	LIMPOR."Porcentaje",
	LIMPOR."IdGrupoEmisor",
	LIMPOR."IdEmpresa",
	POR."CodigoPortafolio",
	LIMPOR."DocumentoIdentidad",
	LIMPOR."NombreCompleto"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimitePortafolio" LIMPOR
INNER JOIN "BSCL.Plataforma.BD::LIM.TipoLimite"   			AS TPO	ON (TPO."IdTipoLimite"=LIMPOR."IdTipoLimite" AND TPO."IdEstado"='ACT')
LEFT JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" AS CLATI ON (CLATI."IdClasificacionTipoInstrumento" = LIMPOR."IdClasificacionTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"			AS TIPINS	ON (TIPINS."IdTipoInstrumento" = LIMPOR."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"			AS TIPOP	ON (TIPOP."IdTipoOperacion" = LIMPOR."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"		AS TCI		ON (TCI."IdTipoCanalIngreso" = LIMPOR."IdCanalIngreso")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"			AS TIPPROD	ON (TIPPROD."IdTipoProducto" = LIMPOR."IdTipoProducto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"					AS ESTDET	ON (ESTDET."IdEstado" = LIMPOR."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"	AS MDA		ON (MDA."IdMoneda" = LIMPOR."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"	AS MDACAL	ON (MDACAL."IdMoneda" = LIMPOR."IdMonedaCalculo")
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"	AS POR		ON (LIMPOR."IdPortafolio" = POR."IdPortafolio")
WHERE LIMPOR."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimitePortafolio" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
