SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCtaPropia"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCtaPropia"  
AS

SELECT 
    DPL."IdLimite",
    DPL."IdTipoLimite",
    DPL."NombreTipoLimite",
    DPL."IdMesa",
    DPL."IdCanalIngreso",
    CASE DPL."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TCI."Nombre" END AS "CanalIngreso",
    DPL."IdTipoInstrumento",
    CASE DPL."IdTipoInstrumento" WHEN '0'  THEN 'TODOS' ELSE DPL."TipoInstrumento" END AS "TipoInstrumento",
    DPL."IdClasificacionTipoInstrumento",
    DPL."IdInstrumento",
    CASE WHEN (DPL."IdTipoOperacion" = '0') OR (DPL."IdInstrumento" = '0') THEN 'TODOS' ELSE COALESCE(INS."Nemotecnico",' -') END AS "Nemotecnico",
    COALESCE(INS."RazonSocial",' -') AS "RazonSocial",
    DPL."IdTipoProducto",
    TIPPROD."Nombre" as "TipoProducto",
    DPL."IdTipoOperacion",
    CASE DPL."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE COALESCE(TIPOP."Nombre",' -') END AS "TipoOperacion",
    DPL."UnidadPlazo",
    DPL."Plazo",
    DPL."IdTipoReajuste",
    CASE WHEN DPL."IdTipoLimite"='LIMCTPPOSICIONINTERBANCARIO' OR DPL."IdTipoLimite"='LIMCTPMESADISTRIBUCION' OR DPL."IdTipoLimite"='LIMCTPFORWARD' THEN DPL."CodigoOperador" ELSE ' -' END AS "CodigoOperador",
    DPL."MontoTotal", --LIMITE TOTAL
    DPL."MontoIntraday",
    DPL."MontoOvernight",
    --DPL."IdMoneda",
    CASE WHEN DPL."IdTipoLimite"='LIMCTPEXCLUIREMISOR' OR DPL."IdTipoLimite"='LIMCTPCLASIFRIESGO' THEN 'CLP' ELSE DPL."IdMoneda" END AS "IdMoneda",
    CASE WHEN DPL."IdMoneda" = '0' THEN 'TODOS' ELSE COALESCE(MDA."NombreLargo",' -') END AS "Moneda",
    MDA."Nombre" AS "NombreMoneda",
    DPL."IdMonedaCalculo",
    COALESCE(MDACAL."NombreLargo",' -') AS "MonedaCalculo",
    DPL."ClasificacionRiesgoMinima",
    --DPL."IdEstado" as "EstadoProducto",
    "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion") AS "IdEstado",
    ESTDET."Nombre" as "NombreEstadoLimite",
    DPL."EsPersonalizada", -- S o N
    DPL."FechaVigencia",
    DPL."EsTotalizador", -- S o N
    DPL."FechaIngreso",
    DPL."FechaCreacionDetallePlantilla",
    DPL."FechaModificacionDetallePlantilla",
    DPL."IdEstadoAutorizacion",
    DPL."IdPlantillaLimite",
    DPL."UI_Limite",
    DPL."IdCliente",
    DPL."IdEmpresa",
    (CASE WHEN DPL."IdTipoLimite"='LIMCTPEXCLUIREMISOR' THEN 
        (SELECT COUNT(1) FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteEmisor" AS LE WHERE LE."IdLimite" = DPL."IdLimite" AND LE."IdEstado" = 'ACT') ELSE 0 END) AS "CantidadEmisores",
    DPL."IdClasificacionRiesgo",
    COALESCE(CR."ClasificacionRiesgo",' -') AS "ClasificacionRiesgo",
    DPL."IdComportamiento",
    DPL."IdTipoEvento",
    DPL."Prioridad",
    DPL."IdAgente",
    CASE WHEN DPL."IdAgente" = 0 THEN 'TODOS' ELSE COALESCE(EMPLE."CodigoYNombreEmpleado",' -') END AS "CodigoYNombreEmpleado",
    EMPLE."CodigoTipoCargoEmpleado",
    CASE WHEN DPL."IdTipoLimite"='LIMCTPGRUPOEMISOR' THEN DPL."IdGrupoEmisor" ELSE ' -' END AS "IdGrupoEmisor",
    CASE WHEN DPL."IdTipoLimite"='LIMCTPEMISOR' THEN DPL."IdEmisor" ELSE ' -' END AS "IdEmisor",
    DPL."IdTipoComportamiento",
    DPL."TipoComportamiento",
    COALESCE(DPL."NombreCompleto",' -') AS "NombreCompleto",
    COALESCE(DPL."DocumentoIdentidad",' -') AS "DocumentoIdentidad"
FROM "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" AS DPL
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento"             AS INS      ON (INS."IdInstrumento" = DPL."IdInstrumento")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion"           AS TIPOP    ON (TIPOP."IdTipoOperacion" = DPL."IdTipoOperacion")
LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"        AS TCI      ON (TCI."IdTipoCanalIngreso" = DPL."IdCanalIngreso")
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"            AS TIPPROD  ON (TIPPROD."IdTipoProducto" = DPL."IdTipoProducto")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado"                  AS ESTDET   ON (ESTDET."IdEstado" = "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion"))
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"  AS MDA      ON (MDA."IdMoneda" = DPL."IdMoneda")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarMoneda"  AS MDACAL   ON (MDACAL."IdMoneda" = DPL."IdMonedaCalculo")
-- MMACHMAR se comenta JOIN por inconsistencia en ClasificacionRiesgo. Se conversó habrá modificacion. Se deja el otro JOIN
--LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo" AS CR ON (DPL."IdClasificacionRiesgo" = CR."IdClasificacionRiesgo")
LEFT JOIN ( SELECT DISTINCT("IdClasificacionRiesgo"), "ClasificacionRiesgo"
            FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarClasificacionRiesgo"
            WHERE "IdEstadoClasificacionRiesgo"='ACT' AND "IdEstadoTipoClasificacionRiesgo"='ACT'
          ) AS CR
    ON (DPL."IdClasificacionRiesgo" = CR."IdClasificacionRiesgo")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMPLE ON (DPL."IdAgente" = EMPLE."IdEmpleado")
--WHERE DPL."IdEstado" = 'ACT'
ORDER BY "IdTipoLimite" ASC, "IdClasificacionTipoInstrumento" ASC, "IdTipoInstrumento" ASC;

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.UIConsultarLimiteCtaPropia" TO BCS_DESARROLLADOR WITH GRANT OPTION;
