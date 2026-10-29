-- CentralizaAnticipoPactos_SelectDatosMovimiento.sql (ported verbatim; HANA-only syntax)
SELECT
     ConsultaSubyacente."IdSubyacente"                                  AS "IdMovimiento"
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)                     AS "DocumentoIdentidad"
    ,LEFT(ConsultarCliente."NombreCompleto",60)                         AS "NombreCliente"
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)                       AS "Portafolio"
    ,ConsultaSubyacente."IdMonedaInstrumento"                           AS "Moneda"
    ,ConsultarCartera."IdTipoProducto"                                  AS "Producto"
    ,ConsultaSubyacente."IdClasificacionTipoInstrumento"                AS "ClaseInstrumento"
    ,ConsultaSubyacente."IdTipoInstrumento"                             AS "TipoInstrumento"
    ,ConsultaSubyacente."Nemotecnico"                                   AS "Nemo"
    ,ConsultarCartera."IdBoveda"                                        AS "Boveda"
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))                 AS "IdCartera"
    ,ConsultaSubyacente."IdEmisor"                                      AS "Emisor"
    ,ConsultaSubyacente."IdTipoReajuste"                                AS "TipoReajuste"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor"(
         ConsultaSubyacente."IdClasificacionTipoInstrumento"
        ,ConsultaSubyacente."IdTipoInstrumento"
        ,ConsultaSubyacente."Nemotecnico"
        ,ConsultaSubyacente."IdEmisor")                                 AS "TipoEmisor"
    ,ConsultarCartera."CodigoIFRS"                                      AS "CodigoIFRS"
    ,'CUSTODIA'                                                         AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(
         ConsultaSubyacente."IdEmisor",ConsultaSubyacente."IdClasificacionTipoInstrumento",
         ConsultaSubyacente."IdTipoInstrumento",ConsultaSubyacente."Nemotecnico",
         ConsultaSubyacente."IdTipoReajuste")                           AS "ClaseEmisor"
    ,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')                     AS "Agente"
    ,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60)            AS "NombreAgente"
    ,IFNULL(ConsultarEjecutivo."CodigoSucursal",'')                     AS "SucursalAgente"
    ,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60)            AS "NombreSucursalAgente"
    ,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)  AS "CentroCostoAgente"
    ,CASE
        WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
        WHEN IndCli."ValorIndicador" = 'S' THEN 'RE'
        ELSE 'NR'
    END                                                                 AS "Relacion"
    ,CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'  THEN 'V'
        WHEN 'COMPAC'  THEN 'C'
    END                                                                 AS "CompraVenta"
    ,COALESCE(MES."Codigo",'')                                          AS "Mesa"
    ,COALESCE(Operacion."FolioOperacion",TO_BIGINT(0))                  AS "FolioOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaSubyacente" ConsultaSubyacente
    ON ConsultaSubyacente."FolioCarteraRelacionada" = ConsultarCartera."FolioCartera"
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion
    ON Operacion."FolioCartera" = ConsultarCartera."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
    ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
    ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
    ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
    ON ConsultarIndicadoresEmpleado."IdEmpleado" = ConsultarEjecutivo."IdEmpleadoEjecutivo"
    AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador" = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IndCli
    ON IndCli."IdCliente" = ConsultarCartera."IdCliente"
    AND IndCli."CodigoTipoIndicador" = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES
    ON ConsultarCartera."IdMesa" = MES."IdMesa"
WHERE ConsultarCartera."IdEmpresa"        = :IdEmpresa
  AND ConsultarCartera."EGRFechaOperacion" = :FechaContable
  AND ConsultarCartera."IdTipoOperacion"  IN ('COMPAC','VENPAC')
  AND ConsultarCartera."IdEstado"         = 'ANT'
  AND ConsultaSubyacente."IdEstado"       = 'ANT'
  AND Operacion."IdEstado"                = 'ANT'
  AND ConsultarCartera."IdTipoInstrumento" = 'PAC'
UNION ALL
SELECT
     ConsultarCartera."IdCartera"                                       AS "IdMovimiento"
    ,LEFT(ConsultarCartera."DocumentoIdentidad",60)                     AS "DocumentoIdentidad"
    ,LEFT(ConsultarCliente."NombreCompleto",60)                         AS "NombreCliente"
    ,LEFT(ConsultarCartera."CodigoPortafolio",60)                       AS "Portafolio"
    ,ConsultarCartera."IdMonedaInstrumento"                             AS "Moneda"
    ,ConsultarCartera."IdTipoProducto"                                  AS "Producto"
    ,ConsultarCartera."IdClasificacionTipoInstrumento"                  AS "ClaseInstrumento"
    ,ConsultarCartera."IdTipoInstrumento"                               AS "TipoInstrumento"
    ,ConsultarCartera."Nemotecnico"                                     AS "Nemo"
    ,ConsultarCartera."IdBoveda"                                        AS "Boveda"
    ,CAST(ConsultarCartera."IdCartera" AS NVARCHAR(60))                 AS "IdCartera"
    ,ConsultarCartera."IdEmisor"                                        AS "Emisor"
    ,ConsultarCartera."IdTipoReajuste"                                  AS "TipoReajuste"
    ,''                                                                 AS "TipoEmisor"
    ,ConsultarCartera."CodigoIFRS"                                      AS "CodigoIFRS"
    ,'CUSTODIA'                                                         AS "TipoCartera"
    ,"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerClasificacionEmisor"(
         ConsultarCartera."IdEmisor",ConsultarCartera."IdClasificacionTipoInstrumento",
         ConsultarCartera."IdTipoInstrumento",ConsultarCartera."Nemotecnico",
         ConsultarCartera."IdTipoReajuste")                             AS "ClaseEmisor"
    ,IFNULL(ConsultarEjecutivo."CodigoEmpleado",'')                     AS "Agente"
    ,LEFT(IFNULL(ConsultarEjecutivo."NombreCompleto",''),60)            AS "NombreAgente"
    ,IFNULL(ConsultarEjecutivo."CodigoSucursal",'')                     AS "SucursalAgente"
    ,LEFT(IFNULL(ConsultarEjecutivo."NombreSucursal",''),60)            AS "NombreSucursalAgente"
    ,LEFT(IFNULL(ConsultarIndicadoresEmpleado."ValorIndicador",''),60)  AS "CentroCostoAgente"
    ,CASE
        WHEN ConsultarCartera."DocumentoIdentidad" = ConsultarEmpresa."DocumentoIdentidad" THEN 'CP'
        WHEN UPPER(IndCli."CodigoValorIndicador") = 'VINCULADO' THEN 'RE'
        ELSE 'NR'
    END                                                                 AS "Relacion"
    ,CASE ConsultarCartera."IdTipoOperacion"
        WHEN 'VENPAC'  THEN 'V'
        WHEN 'COMPAC'  THEN 'C'
    END                                                                 AS "CompraVenta"
    ,COALESCE(MES."Codigo",'')                                          AS "Mesa"
    ,COALESCE(Operacion."FolioOperacion",TO_BIGINT(0))                  AS "FolioOperacion"
FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCartera" ConsultarCartera
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" Operacion
    ON Operacion."FolioCartera" = ConsultarCartera."FolioCartera"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" ConsultarCliente
    ON ConsultarCliente."IdCliente" = ConsultarCartera."IdCliente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" ConsultarEmpresa
    ON ConsultarEmpresa."IdEmpresa" = ConsultarCartera."IdEmpresa"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" ConsultarEjecutivo
    ON ConsultarEjecutivo."IdEmpleadoEjecutivo" = ConsultarCartera."IdAgente"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresEmpleado" ConsultarIndicadoresEmpleado
    ON ConsultarIndicadoresEmpleado."IdEmpleado" = ConsultarEjecutivo."IdEmpleadoEjecutivo"
    AND ConsultarIndicadoresEmpleado."CodigoTipoIndicador" = 'CENCOSTO'
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" IndCli
    ON IndCli."IdCliente" = ConsultarCartera."IdCliente"
    AND IndCli."CodigoTipoIndicador" = 'RELCORR'
LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES
    ON ConsultarCartera."IdMesa" = MES."IdMesa"
WHERE ConsultarCartera."IdEmpresa"        = :IdEmpresa
  AND ConsultarCartera."EGRFechaOperacion" = :FechaContable
  AND ConsultarCartera."IdTipoOperacion"  IN ('COMPAC','VENPAC')
  AND ConsultarCartera."IdEstado"         = 'ANT'
  AND Operacion."IdEstado"                = 'ANT'
