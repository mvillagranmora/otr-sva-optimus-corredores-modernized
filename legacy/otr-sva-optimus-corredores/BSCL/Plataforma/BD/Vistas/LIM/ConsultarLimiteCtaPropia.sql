SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia"  
AS

SELECT 
    DPL."IdLimite",
    DPL."Nombre",
    DPL."IdTipoLimite",
    TPO."Nombre" AS "NombreTipoLimite",
    DPL."IdMesa",
    DPL."IdCanalIngreso",
    DPL."IdTipoInstrumento",
    TIPINS."Nombre" AS "TipoInstrumento",
    DPL."IdClasificacionTipoInstrumento",
    INS."RazonSocial" AS "RazonSocialInstrumento",
    DPL."IdInstrumento",
    DPL."IdTipoProducto",
    DPL."IdTipoOperacion",
    DPL."UnidadPlazo",
    DPL."Plazo",
    DPL."IdTipoReajuste",
    DPL."CodigoOperador",
    DPL."MontoTotal", --LIMITE TOTAL
    DPL."MontoIntraday",
    DPL."MontoOvernight",
    DPL."IdMoneda",
    DPL."IdMonedaCalculo",
    DPL."ClasificacionRiesgoMinima",
    --DPL."IdEstado" as "EstadoProducto",
    "BSCL.Plataforma.BD.Utilitarios::ObtenerEstadoAutorizacion"('PLANTILLALIMITEDETALLE',DPL."IdEstado",DPL."IdEstadoAutorizacion") AS "IdEstado",
    DPL."IdEstado" AS "IdEstadoConfigurable",
    DPL."EsPersonalizada", -- S o N
    DPL."FechaVigencia",
    DPL."EsTotalizador", -- S o N
    DPL."FechaIngreso",
    DPL."FechaCreacion" AS "FechaCreacionDetallePlantilla",
    DPL."FechaModificacion" AS "FechaModificacionDetallePlantilla",
    DPL."IdEstadoAutorizacion",
    DPL."IdPlantillaLimite",
    DPL."UI_Limite",
    CLI."IdCliente",
    EMP."IdEmpresa",
    DPL."IdClasificacionRiesgo",
    TPO."IdComportamiento",
    TPO."IdTipoEvento",
    TPO."Prioridad",
    DPL."IdAgente",
    DPL."IdGrupoEmisor",
    DPL."IdEmisor",
    TC."IdTipoComportamiento",
    TC."Nombre" AS "TipoComportamiento",
    PER."DocumentoIdentidad",
    PER."NombreCompleto",
    DPL."UsaPlazo" AS "UsaPlazo",
    DPL."MargenInferior" AS "MargenInferior",
    DPL."MargenSuperior" AS "MargenSuperior",
    DPL."Porcentaje" AS "Porcentaje"
FROM "BSCL.Plataforma.BD::LIM.Limite" AS DPL
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" AS EMP ON (EMP."IdEmpresa" = DPL."IdEmpresa")
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdPersona" = EMP."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EMP.ConsultarEmpleado" AS EMPL ON  (DPL."IdAgente" = EMPL."IdEmpleado")
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER    ON (EMPL."IdPersona" = PER."IdPersona") OR (CLI."IdPersona" = PER."IdPersona")
INNER JOIN "BSCL.Plataforma.BD::LIM.TipoLimite"             AS TPO  ON (TPO."IdTipoLimite"=DPL."IdTipoLimite" AND TPO."IdEstado"='ACT')
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"         AS TIPINS   ON (TIPINS."IdTipoInstrumento" = DPL."IdTipoInstrumento")
INNER JOIN "BSCL.Plataforma.BD::REQ.TipoComportamiento"     AS TC ON (TC."IdTipoComportamiento" = DPL."IdTipoComportamiento")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" AS INS ON (INS."IdInstrumento" = DPL."IdInstrumento")
WHERE CLI."IdEstado" = 'ACT';

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarLimiteCtaPropia" TO BCS_DESARROLLADOR WITH GRANT OPTION;
