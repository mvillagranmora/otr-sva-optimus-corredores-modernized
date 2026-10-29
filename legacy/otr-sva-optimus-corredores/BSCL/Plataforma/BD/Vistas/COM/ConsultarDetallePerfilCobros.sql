SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros"; 
 		
CREATE VIEW "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros"  
AS

SELECT 
    ROW_NUMBER() over (order by VC."IdCobro")  as "IdInternal",
    DPC."Prioridad",
    DPC."IdPerfilCobros",
    PC."DescripcionPerfilCobros",
    PC."IdEmpresa",
    PC."IdEstado" as "EstadoPerfilCobros",
    ESTPERF."Nombre" as "NombreEstadoPerfilCobros",
    PC."EsPerfilPersonalizado",
    DPC."IdDetallePerfilCobros",
    DPC."IdBolsa",
    DPC."IdTipoProducto",
    CASE DPC."IdTipoProducto" WHEN '0' THEN 'TODOS' ELSE TIPPROD."Nombre" END as "TipoProducto",
    DPC."IdTipoOperacion",
    CASE DPC."IdTipoOperacion" WHEN '0' THEN 'TODOS' ELSE TIPOP."Nombre" END as "TipoOperacion",
    DPC."IdCanalIngreso",
    CASE DPC."IdCanalIngreso" WHEN '0' THEN 'TODOS' ELSE TCI."Nombre" END as "CanalIngreso",
    DPC."DiasPlazo",
    DPC."IdEstado" as "EstadoDetalle",
    ESTDETPERF."Nombre" as "NombreEstadoDetalle",
    VC."IdValorCobro",
    VC."DescripcionValorCobro",
    VC."IdCobro",
    VC."ValorCobro", -- ej:si es variable 19 es 19% , si es fijo 19 es 19 pesos/uf/etc...
    VC."EsPersonalizado",
    CASE C."FormaCobro" WHEN 'V' THEN CONCAT('% ',CAST(VC."ValorCobro" as VARCHAR)) ELSE CAST(VC."ValorCobro" as VARCHAR) END as "ValorCobroTexto",
    VC."ValorTopeMinimoOrden",
    CASE VC."IdMoneda" WHEN 'UF' THEN CONCAT('UF ',CAST(VC."ValorTopeMinimoOrden" as VARCHAR)) ELSE CAST(VC."ValorTopeMinimoOrden" as VARCHAR) END as "ValorTopeMinimoOrdenTexto",
    DPC."IdMoneda",
    VC."IdEstado" as "EstadoValorCobro",
    C."NombreCobro",
    C."IdCobroClasificacion",
    C."FormaCobro", -- Fijo/Variable
    C."PermiteCambiarValor",
    CC."CodigoCobroClasificacion",
    0 as "CobroCalculado",
    CASE WHEN TI."IdClasificacionTipoInstrumento"  IS NULL THEN 'TODOS' ELSE TI."IdClasificacionTipoInstrumento" END as "IdClasificacionTipoInstrumento"
FROM 
    "BSCL.Plataforma.BD::COM.DetallePerfilCobros" as DPC 
    LEFT JOIN"BSCL.Plataforma.BD::COM.PerfilCobros" as PC
        on (PC."IdPerfilCobros" = DPC."IdPerfilCobros")
    LEFT JOIN "BSCL.Plataforma.BD::COM.ValorCobro" as VC 
        on (DPC."IdValorCobro" = VC."IdValorCobro")
    LEFT JOIN "BSCL.Plataforma.BD::COM.Cobro" as C 
        on (C."IdCobro" = VC."IdCobro")
    LEFT JOIN "BSCL.Plataforma.BD::COM.CobroClasificacion" as CC 
        on (CC."IdCobroClasificacion" = C."IdCobroClasificacion")
    LEFT JOIN  "BSCL.Plataforma.BD::INS.TipoOperacion"  as TIPOP 
        on (TIPOP."IdTipoOperacion" = DPC."IdTipoOperacion")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" as TCI
        on (TCI."IdTipoCanalIngreso" = DPC."IdCanalIngreso")
    LEFT JOIN "BSCL.Plataforma.BD::INS.TipoProducto"  as TIPPROD
        on (TIPPROD."IdTipoProducto" = DPC."IdTipoProducto")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTPERF
        ON (ESTPERF."IdEstado" = PC."IdEstado")
    LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" as ESTDETPERF
        ON (ESTDETPERF."IdEstado" = DPC."IdEstado")
    LEFT JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoInstrumento" TI 
        ON (TI."IdTipoOperacion" = DPC."IdTipoOperacion" and TI."IdBolsa" = DPC."IdBolsa")
    WHERE "Borrado" = 'N';
	
--ORDER BY DPC."Prioridad"

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::COM.ConsultarDetallePerfilCobros" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
