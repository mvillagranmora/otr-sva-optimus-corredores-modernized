SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentosPorGrupoActivo"; 
GO
CREATE VIEW "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentosPorGrupoActivo"  
AS
SELECT 
    PER."IdDetallePerfilProductos",
    PER."IdPerfilProductos",
    PER."IdTipoProducto",
    PER."IdTipoOperacion",
    PER."IdCanalIngreso",
    PER."IdEstado" as "IdEstadoDetallePerfilProducto", 
    PER."PermiteRuteo",
    GRP."IdGrupoActivos",
    GRP."IdTipoGrupoActivos",
    GRP."IdEstado" as "IdEstadoGrupoActivo",
    GRP."IdEstadoDetalle" as "IdEstadoInstrumentoEnGrupoActivo",
    INS."IdInstrumento",
    INS."Nemotecnico",
    INS."RazonSocial",
    INS."IdMoneda",
    INS."IdTipoInstrumento",
    INS."IdEstado" as "IdEstadoInstrumento",
    INS."EsAutorizado"
FROM 
"BSCL.Plataforma.BD::COM.DetallePerfilProductos" PER
LEFT JOIN "BSCL.Plataforma.BD.Vistas::COM.ConsultarGrupoActivos" GRP ON (PER."IdGrupoActivos" = GRP."IdGrupoActivos")
LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS ON (GRP."IdInstrumento" = INS."IdInstrumento")
WHERE
    PER."IdGrupoActivos" != ''
    AND PER."IdEstado" = 'ACT'
    AND GRP."IdEstado" = 'ACT'
    AND GRP."IdEstadoDetalle" = 'ACT'
    AND INS."IdEstado" = 'ACT'
    AND INS."EsAutorizado" = 'S'
    AND (INS."IdTipoInstrumento" = 'CFI' or INS."IdTipoInstrumento" = 'CFX')

group by
PER."IdDetallePerfilProductos",
PER."IdPerfilProductos",
PER."IdTipoProducto",
PER."IdTipoOperacion",
PER."IdCanalIngreso",
PER."IdEstado", 
PER."PermiteRuteo",
GRP."IdGrupoActivos",
GRP."IdTipoGrupoActivos",
GRP."IdEstado" ,
GRP."IdEstadoDetalle",
INS."IdInstrumento",
INS."Nemotecnico",
INS."RazonSocial",
INS."IdMoneda",
INS."IdTipoInstrumento",
INS."IdEstado",
INS."EsAutorizado"
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumentosPorGrupoActivo" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO