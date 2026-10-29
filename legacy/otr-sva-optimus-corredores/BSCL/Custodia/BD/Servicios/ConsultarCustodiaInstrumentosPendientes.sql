SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaInstrumentosPendientes";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCustodiaInstrumentosPendientes" (
    --VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa                          BIGINT,
    IN p_IdUsuario                          BIGINT,
    IN p_IdModulo                           NVARCHAR(3), 
    IN p_IdCliente                          BIGINT          DEFAULT 0,
    IN p_IdPortafolio                       BIGINT          DEFAULT 0,
    IN p_IdClasificacionTipoInstrumento     NVARCHAR(3),
    IN p_IdTipoInstrumento                  NVARCHAR(3),
    IN p_IdInstrumento                      BIGINT          DEFAULT 0,
    --IN p_IdBoveda                         NVARCHAR(3)     DEFAULT '',            --solo actua como filtro, no agrupa x boveda
    --IN p_AgrupaPorBoveda                  NVARCHAR(1)     DEFAULT 'N',
    IN p_AgrupaPorRelacion                  NVARCHAR(1)     DEFAULT 'N',           -- S: usa relacion como un criterio más de agrupacion y la muestra   N: no considera relacion en la agrupacion
    IN p_FechaConsulta                      NVARCHAR(10)    DEFAULT '0001-01-01',    
    IN p_TipoFechaConsulta                  NVARCHAR(3)     DEFAULT 'LIQ',         -- Consultar por LIQ: Fecha Liquidación  ADQ: Fecha Adquisicion
    IN p_IdProductoAgrupador                BIGINT          DEFAULT NULL,                          
    
    -- Parametros de Salida
    --OUT p_out_tbl                           "BSCL.Custodia.BD::CUS.CustodiaPortafolio" DEFAULT empty
    OUT p_out_tbl 							TABLE ("IdEmpresa" BIGINT, "TipoFechaConsulta" NVARCHAR(3), "FechaCustodia" NVARCHAR(10), "IdClasificacionTipoInstrumento" NVARCHAR(3), "NombreClasificacionTipoInstrumento" NVARCHAR(100)
    		, "IdTipoInstrumento" NVARCHAR(3), "NombreTipoInstrumento" NVARCHAR(100), "ISIN" NVARCHAR(40), "IdInstrumento" BIGINT, "ClasificacionRiesgoInstrumento" NVARCHAR(20), "Nemotecnico" NVARCHAR(40), "IdEmisor" NVARCHAR(40), "EsFungible" NVARCHAR(1)
    		, "IdCliente" BIGINT, "IdPortafolio" BIGINT, "DocumentoIdentidad" NVARCHAR(40), "DocumentoIdentidadRutCavali" NVARCHAR(40), "NombreCliente" NVARCHAR(500), "CodigoPortafolio" NVARCHAR(100), "IdBoveda" NVARCHAR(3), "NumeroCuentaBoveda" NVARCHAR(40)
    		, "Relacion" NVARCHAR(20), "DescripcionRelacion" NVARCHAR(100), "CantidadCustodia" DECIMAL(26,8), "PPPCustodia" DECIMAL(26,8), "ValorPPPCustodia" DECIMAL(26,8), "MontoPPPCustodia" DECIMAL(26,8), "PrecioMercadoCustodia" DECIMAL(26,8)
    		, "PrecioCompraCustodia" DECIMAL(26,8), "Rentabilidad" DECIMAL(26,8), "ValorMercadoCustodia" DECIMAL(26,8), "MontoMercadoCustodia" DECIMAL(26,8), "DiferenciaMercadoCustodia" DECIMAL(26,8), "IdMonedaInstrReajuste" NVARCHAR(3)
    		, "EsPagaderoEn" NVARCHAR(3), "FechaRespaldo" NVARCHAR(27), "InteresCorridoCustodia" DECIMAL(26,8), "IdMonedaPrincipal" NVARCHAR(3), "CantidadSimulCom" DECIMAL(26,8), "PPPSimulCom" DECIMAL(26,8), "ValorPPPSimulCom" DECIMAL(26,8)
    		, "PrecioMercadoSimulCom" DECIMAL(26,8), "ValorMercadoSimulCom" DECIMAL(26,8), "DiferenciaMercadoSimulCom" DECIMAL(26,8), "InteresCorridoSimulCom" DECIMAL(26,8), "CantidadSimulVen" DECIMAL(26,8), "PPPSimulVen" DECIMAL(26,8)
    		, "ValorPPPSimulVen" DECIMAL(26,8), "PrecioMercadoSimulVen" DECIMAL(26,8), "ValorMercadoSimulVen" DECIMAL(26,8), "DiferenciaMercadoSimulVen" DECIMAL(26,8), "InteresCorridoSimulVen" DECIMAL(26,8), "CantidadPrestamo" DECIMAL(26,8)
    		, "PPPPrestamo" DECIMAL(26,8), "ValorPPPPrestamo" DECIMAL(26,8), "PrecioMercadoPrestamo" DECIMAL(26,8), "ValorMercadoPrestamo" DECIMAL(26,8), "DiferenciaMercadoPrestamo" DECIMAL(26,8), "InteresCorridoPrestamo" DECIMAL(26,8)
    		, "CantidadEmpresti" DECIMAL(26,8), "PPPEmpresti" DECIMAL(26,8), "ValorPPPEmpresti" DECIMAL(26,8), "PrecioMercadoEmpresti" DECIMAL(26,8), "ValorMercadoEmpresti" DECIMAL(26,8), "DiferenciaMercadoEmpresti" DECIMAL(26,8)
    		, "InteresCorridoEmpresti" DECIMAL(26,8), "CantidadGarantia" DECIMAL(26,8), "PPPGarantia" DECIMAL(26,8), "ValorPPPGarantia" DECIMAL(26,8), "PrecioMercadoGarantia" DECIMAL(26,8), "ValorMercadoGarantia" DECIMAL(26,8)
    		, "DiferenciaMercadoGarantia" DECIMAL(26,8), "InteresCorridoGarantia" DECIMAL(26,8), "CantidadPactosV" DECIMAL(26,8), "PPPPactosV" DECIMAL(26,8), "ValorPPPPactosV" DECIMAL(26,8), "PrecioMercadoPactosV" DECIMAL(26,8), "ValorMercadoPactosV" DECIMAL(26,8)
    		, "DiferenciaMercadoPactosV" DECIMAL(26,8), "InteresCorridoPactosV" DECIMAL(26,8), "CantidadPactosC" DECIMAL(26,8), "PPPPactosC" DECIMAL(26,8), "ValorPPPPactosC" DECIMAL(26,8), "PrecioMercadoPactosC" DECIMAL(26,8), "ValorMercadoPactosC" DECIMAL(26,8)
    		, "DiferenciaMercadoPactosC" DECIMAL(26,8), "InteresCorridoPactosC" DECIMAL(26,8), "CantidadReporteVenta" DECIMAL(26,8), "PPPReporteVenta" DECIMAL(26,8), "ValorPPPReporteVenta" DECIMAL(26,8), "PrecioMercadoReporteVenta" DECIMAL(26,8)
    		, "ValorMercadoReporteVenta" DECIMAL(26,8), "DiferenciaMercadoReporteVenta" DECIMAL(26,8), "InteresCorridoReporteVenta" DECIMAL(26,8), "CantidadReporteCompra" DECIMAL(26,8), "PPPReporteCompra" DECIMAL(26,8), "ValorPPPReporteCompra" DECIMAL(26,8)
    		, "PrecioMercadoReporteCompra" DECIMAL(26,8), "ValorMercadoReporteCompra" DECIMAL(26,8), "DiferenciaMercadoReporteCompra" DECIMAL(26,8), "InteresCorridoReporteCompra" DECIMAL(26,8), "CantidadTransIng" DECIMAL(26,8), "CantidadTransEgr" DECIMAL(26,8)
    		, "TotalPosicion" DECIMAL(26,8), "TotalPosicionPresunta" DECIMAL(26,8), "Producto" NVARCHAR(100), "FechaVencimiento" NVARCHAR(10))
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
READS SQL DATA

AS
BEGIN
    
    DECLARE v_DocumentoIdentidadCuentaPropia    NVARCHAR(40);
    DECLARE v_IdPersonaCuentaPropia             BIGINT;
    DECLARE v_IdClienteCuentaPropia             BIGINT;                
    DECLARE v_FechaConsulta                     NVARCHAR(10) := '2300-12-31';
    
    DECLARE v_tblInst TABLE (
        "IdClasificacionTipoInstrumento"        NVARCHAR(3)
       ,"IdTipoInstrumento"                     NVARCHAR(3)
       ,"IdInstrumento"                         BIGINT
       ,"Nemotecnico"                           NVARCHAR(40)
       ,"IdEmisor"                              NVARCHAR(40)
       ,"EsFungible"                            NVARCHAR(1)
       ,"IdCliente"                             BIGINT
       ,"IdPortafolio"                          BIGINT
       ,"NombreCliente"                         NVARCHAR(302)
       ,"DocumentoIdentidad"                    NVARCHAR(40)
       --,"IdBoveda"                                NVARCHAR(3)
       ,"IdCuentaCustodioPortafolio"            BIGINT
       ,"Relacion"                              NVARCHAR(20)
       ,"ClasificacionRiesgoInstrumento"        NVARCHAR(20)
       ,"IdMonedaReajuste"                      NVARCHAR(3)
    );
    
    DECLARE v_tblTrasnIng TABLE (
        "IdClasificacionTipoInstrumento"        NVARCHAR(3)
       ,"IdTipoInstrumento"                     NVARCHAR(3)
       ,"IdInstrumento"                         BIGINT
       ,"Nemotecnico"                           NVARCHAR(40)
       ,"IdEmisor"                              NVARCHAR(40)
       ,"EsFungible"                            NVARCHAR(1)
       ,"IdCliente"                             BIGINT
       ,"IdPortafolio"                          BIGINT
       --,"IdBoveda"                                NVARCHAR(3)
       ,"IdCuentaCustodioPortafolio"            BIGINT
       ,"Relacion"                              NVARCHAR(20)
       ,"Cantidad"                              DECIMAL(26,8)
       ,"IdMonedaReajuste"                      NVARCHAR(3)
    )
    DEFAULT SELECT
        '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico",'' as "IdEmisor", '' as "EsFungible"
       ,0 as "IdCliente", 0 as "IdPortafolio",0 as "IdCuentaCustodioPortafolio",'' as "Relacion", 0 as "Cantidad", '' as "IdMonedaReajuste"
    FROM DUMMY
    WHERE 1 = 2;
    
    DECLARE v_tblTrasnEgr TABLE (
        "IdClasificacionTipoInstrumento"        NVARCHAR(3)
       ,"IdTipoInstrumento"                     NVARCHAR(3)
       ,"IdInstrumento"                         BIGINT
       ,"Nemotecnico"                           NVARCHAR(40)
       ,"IdEmisor"                              NVARCHAR(40)
       ,"EsFungible"                            NVARCHAR(1)
       ,"IdCliente"                             BIGINT
       ,"IdPortafolio"                          BIGINT
       --,"IdBoveda"                                NVARCHAR(3)
       ,"IdCuentaCustodioPortafolio"            BIGINT
       ,"Relacion"                              NVARCHAR(20)
       ,"Cantidad"                              DECIMAL(26,8)
       ,"IdMonedaReajuste"                      NVARCHAR(3)
    )
    DEFAULT SELECT
        '' as "IdClasificacionTipoInstrumento", '' as "IdTipoInstrumento", 0 as "IdInstrumento", '' as  "Nemotecnico",'' as "IdEmisor", '' as "EsFungible"
       ,0 as "IdCliente", 0 as "IdPortafolio",0 as "IdCuentaCustodioPortafolio",'' as "Relacion", 0 as "Cantidad", '' as "IdMonedaReajuste"
    FROM DUMMY
    WHERE 1 = 2;
    
    
    IF p_FechaConsulta = '0001-01-01' THEN
    
        SELECT top 1 "Valor"
        INTO v_FechaConsulta
        FROM "BSCL.Plataforma.BD::PLA.Configuracion"
        WHERE "CodigoParametro" = 'PLA.FechaSistema'
        AND "IdEmpresa" = p_IdEmpresa;
    
    ELSE
    
        v_FechaConsulta := p_FechaConsulta;
    
    END IF;
    
    
    -- Obtiene los datos de la corredora(para cuenta propia)    
    SELECT "IdPersona", "IdCliente", "DocumentoIdentidad"
    INTO v_IdPersonaCuentaPropia, v_IdClienteCuentaPropia, v_DocumentoIdentidadCuentaPropia
    FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafoliosCuentaPropia"
    WHERE "IdEmpresa" = :p_IdEmpresa
    GROUP BY "IdPersona","IdCliente", "DocumentoIdentidad";
    
    
    -- Obtiene los relacionados a la corredora
    v_tblRelacionados =
    SELECT distinct PER."IdPersonaDestino"
    FROM "BSCL.SocioNegocio.BD.Vistas::REL.ConsultarRelacionPersona" AS PER
    WHERE PER."IdPersonaOrigen" = :v_IdPersonaCuentaPropia
    AND PER."IdEmpresa" = p_IdEmpresa
    AND PER."IdEstado" = 'ACT';
    
    
    ------------------------------------
    -- Movimiento en transito, ingreso
    ------------------------------------
    BEGIN
    -- Detalle
    
        v_tblTrasnIng =
        SELECT
            MOV."IdClasificacionTipoInstrumento"
           ,MOV."IdTipoInstrumento"
           ,MOV."IdInstrumento"
           ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
           ,MOV."IdEmisor"
           ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
           ,MOV."IdCliente"
           ,MOV."IdPortafolio"
           ,0 as "IdCuentaCustodioPortafolio"
           ,CASE
                WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
                WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
                ELSE 'TER'                                                  -- Tercero
            END AS "Relacion"
           ,cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) as DECIMAL(26,8)) as "Cantidad"
           ,CASE
                WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' then MOV."IdMonedaInstrumento"
                WHEN ifnull(MOV."IdTipoReajuste",'') <> '' then MOV."IdTipoReajuste"
                WHEN ifnull(MOV."IdMonedaSistema",'') <> '' then MOV."IdMonedaSistema"
                ELSE ''
            END AS "IdMonedaReajuste"
        FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
        LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
        LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
        WHERE 
            MOV."IdEmpresa" = p_IdEmpresa 
        AND MOV."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else MOV."IdCliente" END
        AND MOV."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else MOV."IdPortafolio" END
        AND MOV."IdEstado" = 'PEN'
        --AND TipoOperacion."Accion" = 'INGRESO'
        AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END
        AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE "IdTipoInstrumento" END
        AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END
        AND 
        (
            (v_FechaConsulta >= MOV."FechaOperacion" AND v_FechaConsulta <= MOV."FechaCertificacionRegistro" AND "FechaCertificacionRegistro" >= '0001-01-02')
            OR  
            (v_FechaConsulta >= MOV."FechaOperacion" AND MOV."FechaCertificacionRegistro" = '0001-01-01' )
        )
        AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
        
        
        -- Resumen
        v_tblTrasnIng =
        SELECT
            "IdClasificacionTipoInstrumento"
           ,"IdTipoInstrumento"
           ,"IdInstrumento"
           ,"Nemotecnico"
           ,"IdEmisor"
           ,"EsFungible"
           ,"IdCliente"
           ,"IdPortafolio"
           ,"IdCuentaCustodioPortafolio"
           ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
           ,cast(SUM("Cantidad") as DECIMAL(26,8)) as "Cantidad"
           ,"IdMonedaReajuste"
        FROM :v_tblTrasnIng
        GROUP BY
            "IdClasificacionTipoInstrumento"
           ,"IdTipoInstrumento"
           ,"IdInstrumento"
           ,"Nemotecnico"
           ,"IdEmisor"
           ,"EsFungible"
           ,"IdCliente"
           ,"IdPortafolio"
           ,"IdCuentaCustodioPortafolio"
           ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
           ,"IdMonedaReajuste";
    
    END;
    --Movimiento en transito, ingreso
    
    
    ---------------------------------------------
    -- Movimiento en transito, egreso
    ---------------------------------------------
    BEGIN
    -- Detalle
    
        v_tblTrasnEgr =
        SELECT
            MOV."IdClasificacionTipoInstrumento"
           ,MOV."IdTipoInstrumento"
           ,MOV."IdInstrumento"
           ,case when "IdClasificacionTipoInstrumento" = 'IF' then MOV."NemoSVS" else MOV."Nemotecnico" end as "Nemotecnico"
           ,MOV."IdEmisor"
           ,case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."EsFungible" else 'N' end as "EsFungible"
           ,MOV."IdCliente"
           ,MOV."IdPortafolio"
           ,0 as "IdCuentaCustodioPortafolio"
           ,CASE
                WHEN CLI."IdCliente" = :v_IdClienteCuentaPropia THEN 'CPRO' -- Cuenta propia
                WHEN COALESCE(REL."IdPersonaDestino",0) > 0 then 'REL'      -- Relacionado
                ELSE 'TER'                                                  -- Tercero
            END AS "Relacion"
           ,cast(IFNULL(case when MOV."IdClasificacionTipoInstrumento" = 'IF' then MOV."ValorRescate" else MOV."Cantidad" end,0) * -1 as DECIMAL(26,8)) as "Cantidad"
           ,CASE
                WHEN ifnull(MOV."IdMonedaInstrumento",'') <> '' then MOV."IdMonedaInstrumento"
                WHEN ifnull(MOV."IdTipoReajuste",'') <> '' then MOV."IdTipoReajuste"
                WHEN ifnull(MOV."IdMonedaSistema",'') <> '' then MOV."IdMonedaSistema"
                ELSE ''
            END AS "IdMonedaReajuste"
        FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" AS MOV
        LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" as TipoOperacion ON MOV."IdTipoOperacion" = TipoOperacion."IdTipoOperacion"
        LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = MOV."IdCliente")
        LEFT JOIN :v_tblRelacionados as REL ON (REL."IdPersonaDestino" = CLI."IdPersona")
        LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = MOV."IdPortafolio" AND Portafolio."IdCliente" = MOV."IdCliente"
        LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
        WHERE
            MOV."IdEmpresa" = p_IdEmpresa
        AND MOV."IdCliente" = case when p_IdCliente > 0 then p_IdCliente else MOV."IdCliente" END
        AND MOV."IdPortafolio" = case when p_IdPortafolio > 0 then p_IdPortafolio else MOV."IdPortafolio" END
        AND MOV."IdEstado" = 'PEN'
        AND TipoOperacion."Accion" = 'RETIRO'
        AND MOV."IdClasificacionTipoInstrumento" = CASE WHEN IFNULL(p_IdClasificacionTipoInstrumento,'') <> '' THEN p_IdClasificacionTipoInstrumento ELSE "IdClasificacionTipoInstrumento" END
        AND MOV."IdTipoInstrumento" = CASE WHEN IFNULL(p_IdTipoInstrumento,'') <> '' THEN p_IdTipoInstrumento ELSE MOV."IdTipoInstrumento" END
        AND MOV."IdInstrumento" = CASE WHEN IFNULL(p_IdInstrumento,0) > 0 THEN p_IdInstrumento ELSE MOV."IdInstrumento" END
        AND 
        (
            (v_FechaConsulta >= MOV."FechaOperacion" AND v_FechaConsulta <= MOV."FechaCertificacionRegistro" AND MOV."FechaCertificacionRegistro" >= '0001-01-02')
            OR  
            (v_FechaConsulta >= MOV."FechaOperacion" AND MOV."FechaCertificacionRegistro" = '0001-01-01' )
        )
        AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
        
        
        -- Resumen
        v_tblTrasnEgr = 
        SELECT
            "IdClasificacionTipoInstrumento"
           ,"IdTipoInstrumento"
           ,"IdInstrumento"
           ,"Nemotecnico"
           ,"IdEmisor"
           ,"EsFungible"
           ,"IdCliente"
           ,"IdPortafolio"
           ,"IdCuentaCustodioPortafolio"
           ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' end as "Relacion"
           ,cast(SUM("Cantidad") as DECIMAL(26,8))  as "Cantidad"
           ,"IdMonedaReajuste"
        FROM :v_tblTrasnEgr
        GROUP BY
            "IdClasificacionTipoInstrumento"
           ,"IdTipoInstrumento"
           ,"IdInstrumento"
           ,"Nemotecnico"
           ,"IdEmisor"
           ,"EsFungible"
           ,"IdCliente"
           ,"IdPortafolio"
           ,"IdCuentaCustodioPortafolio"
           ,case when p_AgrupaPorRelacion = 'S' then "Relacion" else '' END
           ,"IdMonedaReajuste";
    
    END;
    --Movimiento en transito, ingreso
    
    
    -- Instrumentos de los cuales el portafolio tiene custodia
    v_tblInst =
    SELECT DISTINCT
        a."IdClasificacionTipoInstrumento"
       ,a."IdTipoInstrumento"
       ,a."IdInstrumento"
       ,a."Nemotecnico"
       ,a."IdEmisor"
       ,a."EsFungible"
       ,a."IdCliente"
       ,a."IdPortafolio"
       ,CLI."NombreCompleto" as "NombreCliente"
       ,CLI."DocumentoIdentidad"
       ,a."IdCuentaCustodioPortafolio"
       , a."Relacion"
       ,COALESCE(CLA."ValorClasificacionRiesgo",'') AS "ClasificacionRiesgoInstrumento"
       ,a."IdMonedaReajuste"
    FROM
    (
        SELECT DISTINCT
            "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio","Relacion","IdMonedaReajuste"
        FROM :v_tblTrasnIng
        WHERE "Cantidad"<> 0
        UNION ALL
        SELECT DISTINCT
            "IdClasificacionTipoInstrumento", "IdTipoInstrumento", "IdInstrumento", "Nemotecnico","IdEmisor","EsFungible", "IdCliente", "IdPortafolio", "IdCuentaCustodioPortafolio","Relacion","IdMonedaReajuste"
        FROM :v_tblTrasnEgr
        WHERE "Cantidad"<> 0
    ) AS a
    INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdEmpresa" = p_IdEmpresa AND CLI."IdCliente" = a."IdCliente")
    LEFT OUTER JOIN "BSCL.Plataforma.BD::INS.InstrumentoValorClasificacionRiesgo" CLA ON (a."IdInstrumento" = CLA."IdInstrumento" AND CLA."IdEntidadClasificadoraRiesgo" ='BCS');
    
    
    ------------------------------------------
    -- SALIDA
    ------------------------------------------
    p_out_tbl =
    SELECT
       :p_IdEmpresa as "IdEmpresa"
       ,:p_TipoFechaConsulta as "TipoFechaConsulta"
       ,:v_FechaConsulta as "FechaCustodia"
       ,ins."IdClasificacionTipoInstrumento"
       ,inst2."NombreClasificacionTipoInstrumento"
       ,ins."IdTipoInstrumento"
       ,inst2."NombreTipoInstrumento"
       ,inst2."ISIN"
       ,ins."IdInstrumento"
       ,ins."ClasificacionRiesgoInstrumento"
       ,ins."Nemotecnico"
       ,ins."IdEmisor"
       ,ins."EsFungible"
       ,ins."IdCliente"
       ,ins."IdPortafolio"
       ,ins."DocumentoIdentidad"
       ,NULL AS "DocumentoIdentidadRutCavali"
       ,ins."NombreCliente"
       ,Port."CodigoPortafolio"
       --,CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE ins."IdBoveda" END "IdBoveda"
       ,'' as "IdBoveda"
       ,COALESCE(CCPI."Codigo",'') as "NumeroCuentaBoveda"
       ,ins."Relacion"
       ,ins."Relacion" as "DescripcionRelacion"
       -- CUSTODIA
       ,0 as "CantidadCustodia"
       ,0 as "PPPCustodia"
       ,0 as "ValorPPPCustodia"
       ,0 as "MontoPPPCustodia"
       ,0 as "PrecioMercadoCustodia"
       ,0 as "PrecioCompraCustodia"
       ,0 as "Rentabilidad"
       ,0 as "ValorMercadoCustodia"
       ,0 as "MontoMercadoCustodia"
       ,0 as "DiferenciaMercadoCustodia"
       ,ins."IdMonedaReajuste" as "IdMonedaInstrReajuste"
       ,'' AS "EsPagaderoEn"
       ,'' as "FechaRespaldo"
       ,0  as "InteresCorridoCustodia"     
       ,'' as "IdMonedaPrincipal"   
       -- SIMULTANEA COMPRA
       ,0 as "CantidadSimulCom"
       ,0 as "PPPSimulCom"
       ,0 as "ValorPPPSimulCom"
       ,0 as "PrecioMercadoSimulCom"
       ,0 as "ValorMercadoSimulCom"
       ,0 as "DiferenciaMercadoSimulCom"
       ,0 AS "InteresCorridoSimulCom"
       -- SIMULTANEA VENTA
       ,0 as "CantidadSimulVen"
       ,0 as "PPPSimulVen"
       ,0 as "ValorPPPSimulVen"
       ,0 as "PrecioMercadoSimulVen"
       ,0 as "ValorMercadoSimulVen"
       ,0 as "DiferenciaMercadoSimulVen"
       ,0 as "InteresCorridoSimulVen"  
       -- PRESTAMO
       ,0 as "CantidadPrestamo"
       ,0 as "PPPPrestamo"
       ,0 as "ValorPPPPrestamo"
       ,0 as "PrecioMercadoPrestamo"
       ,0 as "ValorMercadoPrestamo"
       ,0 as "DiferenciaMercadoPrestamo"
       ,0 as "InteresCorridoPrestamo"
       -- EMPRESTITO
       ,0 as "CantidadEmpresti"
       ,0 as "PPPEmpresti"
       ,0 as "ValorPPPEmpresti"
       ,0 as "PrecioMercadoEmpresti"
       ,0 as "ValorMercadoEmpresti"
       ,0 as "DiferenciaMercadoEmpresti"
       ,0 as "InteresCorridoEmpresti"  
       -- GARANTIA
       ,0 as "CantidadGarantia"
       ,0 as "PPPGarantia"
       ,0 as "ValorPPPGarantia"
       ,0 as "PrecioMercadoGarantia"
       ,0 as "ValorMercadoGarantia"
       ,0 as "DiferenciaMercadoGarantia"
       ,0 as "InteresCorridoGarantia"   
       -- VENTAS CON PACTOS
       ,0 as "CantidadPactosV"
       ,0 as "PPPPactosV"
       ,0 as "ValorPPPPactosV"
       ,0 as "PrecioMercadoPactosV"
       ,0 as "ValorMercadoPactosV"
       ,0 as "DiferenciaMercadoPactosV"
       ,0 as "InteresCorridoPactosV" 
       -- COMPRAS CON PACTOS
       ,0 as "CantidadPactosC"
       ,0 as "PPPPactosC"
       ,0 as "ValorPPPPactosC"
       ,0 as "PrecioMercadoPactosC"
       ,0 as "ValorMercadoPactosC"
       ,0 as "DiferenciaMercadoPactosC"
       ,0 as "InteresCorridoPactosC"
       -- REPORTE DE VENTA
       ,0 AS "CantidadReporteVenta"
       ,0 AS "PPPReporteVenta"
       ,0 AS "ValorPPPReporteVenta"
       ,0 AS "PrecioMercadoReporteVenta"
       ,0 AS "ValorMercadoReporteVenta"
       ,0 AS "DiferenciaMercadoReporteVenta"
       ,0 as "InteresCorridoReporteVenta" 
       -- REPORTE DE COMPRA
       ,0 AS "CantidadReporteCompra"
       ,0 AS "PPPReporteCompra"
       ,0 AS "ValorPPPReporteCompra"
       ,0 AS "PrecioMercadoReporteCompra"
       ,0 AS "ValorMercadoReporteCompra"
       ,0 AS "DiferenciaMercadoReporteCompra"
       ,0 as "InteresCorridoReporteCompra"
       -- TRANSITO INGRESO
       ,cast(sum(ifnull( mti."Cantidad",0)) as DECIMAL(26,8)) as "CantidadTransIng"
       -- TRANSITO INGRESO
       ,cast(sum(ifnull( mte."Cantidad",0)) as DECIMAL(26,8)) as "CantidadTransEgr"
       -- TOTALIZADOR: POSICION
       ,0 as "TotalPosicion"
       -- TOTALIZADOR: POSICION PRESUNTA
       ,0 as "TotalPosicionPresunta"
       ,ProductoAgrupador."NombreProductoAgrupador" AS "Producto"   
       ,'0001-01-01' as "FechaVencimiento"     
    FROM :v_tblInst as ins
    INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as inst2 on ins."IdInstrumento" = inst2."IdInstrumento"
    LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" AS CCPI ON CCPI."IdCuentaCustodioPortafolio" = ins."IdCuentaCustodioPortafolio"
    LEFT JOIN :v_tblTrasnIng as mti on mti."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
          AND mti."IdTipoInstrumento" = ins."IdTipoInstrumento"
          AND mti."IdInstrumento" = ins."IdInstrumento"
          AND mti."Nemotecnico" = ins."Nemotecnico"
          AND mti."EsFungible" = ins."EsFungible"
          AND mti."IdCliente" = ins."IdCliente"
          AND mti."IdPortafolio" = ins."IdPortafolio"
          AND mti."Relacion" = ins."Relacion"
    LEFT JOIN :v_tblTrasnEgr as mte on mte."IdClasificacionTipoInstrumento" = ins."IdClasificacionTipoInstrumento"
          AND mte."IdTipoInstrumento" = ins."IdTipoInstrumento"
          AND mte."IdInstrumento" = ins."IdInstrumento"
          AND mte."EsFungible" = ins."EsFungible"
          AND mte."IdCliente" = ins."IdCliente"
          AND mte."IdPortafolio" = ins."IdPortafolio"
          AND mte."Relacion" = ins."Relacion"
    INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" as Port ON Port."IdPortafolio" = ins."IdPortafolio"
    LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Port."IdMatrizAgrupadorAPV"
    LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" AS ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
    GROUP BY
        ins."IdClasificacionTipoInstrumento"
       ,inst2."NombreClasificacionTipoInstrumento"
       ,ins."IdTipoInstrumento"
       ,inst2."NombreTipoInstrumento"
       ,inst2."ISIN"
       ,ins."IdInstrumento"
       ,ins."ClasificacionRiesgoInstrumento"
       ,ins."Nemotecnico"
       ,ins."IdEmisor"
       ,ins."EsFungible"
       ,ins."IdCliente"
       ,ins."IdPortafolio"
       ,ins."DocumentoIdentidad"
       ,ins."NombreCliente"
       ,Port."CodigoPortafolio"
       ,CCPI."IdCuentaCustodioPortafolio"
       ,CCPI."Codigo"
       ,ins."Relacion"
       ,ins."IdMonedaReajuste"
       ,ProductoAgrupador."NombreProductoAgrupador"
    ORDER BY
        ins."IdClasificacionTipoInstrumento" DESC
       ,ins."IdTipoInstrumento"
       ,ins."Nemotecnico"
       ,ins."IdEmisor"
       ,ins."EsFungible"
       ,ins."IdCliente"
       ,ins."IdPortafolio"
       ,ins."DocumentoIdentidad"
       ,ins."NombreCliente"
       ,Port."CodigoPortafolio"
       ,ins."Relacion";
    
END;
GO