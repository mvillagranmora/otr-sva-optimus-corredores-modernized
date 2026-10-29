SET SCHEMA "BSCL";
--BSCL.SocioNegocio.BD::REL.Servicios.ConsultarTipoRelacion
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion";
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" as
--ORIGEN RELACIONADOS	 
  SELECT
	CAST( "REL"."IdTipoRelacion" AS NVARCHAR(6)) AS "IdTipoRelacion",
	 "REL"."EsPersonaNatural" ,
	 CAST("REL"."IdTipoEntidadOrigen" AS NVARCHAR(100)) AS "IdTipoEntidadOrigen",
     CAST("REL"."Administracion" AS NVARCHAR(40)) AS "Administracion",  --RELACIONADOS
	 CAST("REL"."NombreTipoRelacion" AS NVARCHAR(100)) AS "NombreTipoRelacion" ,
	 CAST("REL"."IdTipoRelacionDestino" AS NVARCHAR(6)) AS "IdTipoRelacionDestino",
	 "REL"."EsIndefinida" ,
	 CAST("REL"."IdEstado" AS NVARCHAR(3)) AS "IdEstado",
 	 '' 							 AS "IdTipoEntidadDestino",
  	"REL"."IdEmpresa",
  	"REL"."UsaPorcentajeParticipacion",
  	CASE "REL"."UsaPorcentajeParticipacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaPorcentajeParticipacion",
  	"REL"."RetiraDocumentos",
  	CASE "REL"."RetiraDocumentos" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaRetiraDocumentos",
  	"REL"."UsaFinalidadRelacion",
  	CASE "REL"."UsaFinalidadRelacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaFinalidadRelacion",
  	"REL"."UsaCondicionesRelacion",
  	CASE "REL"."UsaCondicionesRelacion" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaCondicionesRelacion",
  	"REL"."UsaGradoVinculacionParental",
  	CASE "REL"."UsaGradoVinculacionParental" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaGradoVinculacionParental",
  	"REL"."UsaFirmaDocumento",
  	CASE "REL"."UsaFirmaDocumento" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoUsaFirmaDocumento",
	"REL"."AccesoOptimusCliente",
  	CASE "REL"."AccesoOptimusCliente" WHEN 'S' THEN 'SI' ELSE 'NO' END as "TextoAccesoOptimusCliente"
FROM "BSCL.SocioNegocio.BD::REL.TipoRelacion" AS "REL"
UNION ALL --ORIGEN EMPLEADO
SELECT
	 CAST("REL"."CodigoTipoCargoEmpleado" AS NVARCHAR(6)) AS "IdTipoRelacion",
	 'S'							 AS "EsPersonaNatural",
	 CAST('PERSONA' AS NVARCHAR(100)) AS "IdTipoEntidadOrigen",
     CAST('EMPLEADO' AS NVARCHAR(40)) AS "Administracion",  --RELACIONADOS	 
	 CAST("REL"."NombreTipoCargoEmpleado" AS NVARCHAR(100)) AS "NombreTipoRelacion",
	 CAST('CEMDOR' AS NVARCHAR(6)) AS "IdTipoRelacionDestino", --EMPLEADOR (CORREDORA)
	 'N'							 AS "EsIndefinida",
	 CAST('ACT' AS NVARCHAR(3)) AS "IdEstado",
  	 '' 							 AS "IdTipoEntidadDestino",
	 "REL"."IdEmpresa"				 AS "IdEmpresa",
	 'N' 							 AS "UsaPorcentajeParticipacion", 
	 'NO'							 AS "TextoUsaPorcentajeParticipacion",
	 'N'							 AS "RetiraDocumentos",
	 'NO'							 AS "TextoUsaRetiraDocumentos",
	 'N'						   	 AS "UsaFinalidadRelacion",
  	 'NO'							 AS "TextoUsaFinalidadRelacion",
  	 'N'						   	 AS "UsaCondicionesRelacion",
  	 'NO'							 AS "TextoUsaCondicionesRelacion",
  	 'N'						   	 AS "UsaGradoVinculacionParental",
  	 'NO'							 AS "TextoUsaGradoVinculacionParental",
	 'N'						   	 AS "UsaFirmaDocumento",
  	 'NO'							 AS "TextoUsaFirmaDocumento",
  	 'N'						   	 AS "AccesoOptimusCliente",
  	 'NO'							 AS "TextoAccesoOptimusCliente"
FROM "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarTipoCargoEmpleado" AS "REL";

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarTipoRelacion" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
