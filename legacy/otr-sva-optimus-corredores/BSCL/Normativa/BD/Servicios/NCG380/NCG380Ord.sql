SET SCHEMA "BSCL";  
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ord";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios.NCG380::NCG380Ord"
( 
    -- Parametros de Entrada
    IN p_IdSolicitudCircular BIGINT DEFAULT 0, 
    --VARIABLES DE SALIDA 
    OUT p_out_TablaSalida "BSCL.Normativa.BD::NOR.SALIDA_NCG380ORD" --Se debe llamar siemte p_out_TablaSalida
)   
    LANGUAGE SQLSCRIPT
    SQL SECURITY INVOKER 
    DEFAULT SCHEMA "BSCL" 
AS
BEGIN
    DECLARE v_FechaInicio,v_FechaTermino DATE DEFAULT '0001-01-01';
    DECLARE v_IdEmpresa,v_SegundosZonaHoraria INT:=0;
    DECLARE v_Version,v_FechaVersion  NVARCHAR(10) DEFAULT ''; 
    SELECT top 1 "Valor" INTO v_SegundosZonaHoraria
    FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro"= 'PLA.SegundosZonaHoraria';    
    --Se obtiene la fecha de inicio y fecha termino
    SELECT
        CAST(COALESCE(MAX("IdEmpresa"),0) AS INT),
        CAST(COALESCE(MAX(UTCTOLOCAL("FechaInicio", 'America/Santiago','platform')),'0001-01-01') AS DATE),
    	CAST(COALESCE(MAX(UTCTOLOCAL("FechaTermino", 'America/Santiago','platform')), '0001-01-01') AS DATE)
        INTO v_IdEmpresa,v_FechaInicio,v_FechaTermino
    FROM
    (
        SELECT
            CASE WHEN "TipoParametro"='EMP' THEN "Valor" ELSE NULL END AS "IdEmpresa",
            CASE WHEN "TipoParametro"='FINI' THEN "Valor" ELSE NULL END AS "FechaInicio",
            CASE WHEN "TipoParametro"='FTER' THEN "Valor" ELSE NULL END AS "FechaTermino"
        FROM (SELECT
                "TipoParametro","Valor"
              FROM "BSCL.Normativa.BD::NOR.SolicitudParametros"
              WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" IN ('EMP','FINI','FTER'))
    );
        
    IF (:v_FechaInicio = '' OR :v_FechaInicio='0001-01-01') THEN
        v_FechaVersion :=TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'DD-MM-YYYY');
    ELSE --Se obtiene segun fecha 
        v_FechaVersion :=TO_VARCHAR (:v_FechaInicio, 'DD-MM-YYYY');
    END IF; 
    SELECT TOP 1 "P_OUT_VERSION" INTO v_Version FROM "BSCL.Normativa.BD.Utilitarios::NOR.ObtenerVersionEsquema"('NCG380ORD',:v_FechaVersion);
    
    --SELECT :v_FechaInicio,:v_FechaVersion,v_Version FROM dummy; --borrar
    p_out_TablaSalida =
    SELECT
        c."IdOrden" AS "NumeroOrden",
        CASE WHEN c."FolioOrden" = '' THEN 0 ELSE c."FolioOrden" END AS "FolioOrden",
        c."DocumentoIdentidad" AS "NumeroCliente",
        c."CodigoPortafolio" AS "NumeroCuenta",
        c."DocumentoIdentidad" AS "NumeroPersonaOrden",
        c."IdEmpleadoPortafolio" AS "NumeroEjecutivo",
        COALESCE(hco."ValorDestino",'TM') AS "ClaseOrden",
        CASE WHEN c."EsVentaCorta" = 'S' THEN 'VC' --INVC -->Intermediación Venta Corta --Todo, no se si es lo correcto, ademas vc no esta en las definicion del rq, pero si en el codigo de sebra
             WHEN c."AccionOrden" = 'COMPRA' AND c."IdTipoInstrumento" = 'SIM' THEN 'VP'
             WHEN c."AccionOrden" = 'VENTA' AND c."IdTipoInstrumento" = 'SIM' THEN 'CP'
             ELSE hto."ValorDestino"
        END AS "TipoOrden",
        CASE WHEN c."IdTipoInstrumento" NOT IN ('PRT','EMP','SIM','RRV','RRF') AND c."ISINInstrumento" NOT IN ('') THEN c."ISINInstrumento"
        WHEN c."IdTipoInstrumento" IN ('PRT','EMP','SIM','RRV','RRF') AND c."ISINInstrumentoSubyacente" NOT IN ('') THEN c."ISINInstrumentoSubyacente"
        ELSE 'INSTRUMENTO SIN ISIN'  END AS "Isin",--TODO --> NO ESTA CONSIDERANDO EL SUBYACENTE
        CASE 
            WHEN c."IdTipoInstrumento" in ('PRT','EMP','SIM','RRV','RRF') THEN c."InstrumentoSubyacente"     
            WHEN c."IdTipoInstrumento" = 'PAG' THEN ord."NemotecnicoSVS"
            ELSE c."Instrumento"      
        END AS "Nemotecnico", --TODO --> NO ESTA CONSIDERANDO EL SUBYACENTE, --PARA LOS IF DEBE CONSIDERAR LOS EMISORES
        COALESCE(hti."ValorDestino",'') AS "TipoInstrumento",
        c."CantidadOrden" AS "Cantidad",
        c."MontoOrden" AS "Monto",
        COALESCE(hm."ValorDestino",c."IdMonedaOrden") AS "Moneda",
        CASE WHEN c."IdClasificacionTipoInstrumento" = 'IF' THEN c."TasaRFIF"
             WHEN c."IdClasificacionTipoInstrumento" = 'RF' THEN c."TasaRFIF"
             ELSE c."PrecioOrden"
        END AS "Precio",
        c."Plazo" as "PlazoValidez",
        LPAD(COALESCE(TRIM(CAST (hpb."ValorDestino" AS VARCHAR(10)) ),'00'),2,'0') AS "PreferenciaBolsa",
        LPAD(COALESCE(TRIM(CAST (hpm."ValorDestino" AS VARCHAR(10)) ),'00'),2,'0') AS "PreferenciaMecanismo",
       -- LPAD(COALESCE(TRIM(CAST (hpm."ValorDestino" AS VARCHAR(10)) ),'00'),2,'0') || ';'|| 
       -- LPAD(COALESCE(TRIM(CAST (hpb."ValorDestino" AS VARCHAR(10)) ),'00'),2,'0')        AS "Preferencias",
        CAST(UTCTOLOCAL (c."FechaRecepcion",'est') AS DATE) AS "FechaRecepcion",
        SUBSTR_BEFORE(SUBSTR_AFTER(TRIM(ADD_SECONDS(TO_TIMESTAMP (c."FechaRecepcion"), :v_SegundosZonaHoraria)),' '),'.') AS "HoraRecepcion",
        CAST(UTCTOLOCAL (c."FechaAceptacion",'est') AS DATE) AS "FechaAceptacion",
        SUBSTR_BEFORE(SUBSTR_AFTER(TRIM(ADD_SECONDS(TO_TIMESTAMP (c."FechaAceptacion"), :v_SegundosZonaHoraria)),' '),'.') AS "HoraAceptacion",
        c."DocumentoIdentidad" AS "NUMEROPERSONA",
        CASE WHEN c."IdEstadoOrden" = 'ANU' THEN 1 else 0 END AS "Anulacion",
        0 AS "NumeroOrdenReemplazo",
        CASE WHEN c."IdEstadoOrden" = 'ANU' THEN CAST(c."FechaAceptacion" AS DATE) else '' END AS "FechaAnulacion",
        ---c."FechaAnulacion",
        c."GlosaOrden" AS "Observaciones",
        c."DocumentoIdentidad" AS "DocumentoIdentidadRut",
        c."InstrumentoNegociacion" AS "InstrumentoNegociacion"
    FROM (
        SELECT
            c."FolioOrden",
            c."IdOrden",c."IdCliente",c."CodigoPortafolio",c."IdPersonaAutorizadoDarOrdenes",c."IdEmpleadoPortafolio",c."CodigoClaseOrden",
            c."ISINInstrumento",c."ISINInstrumentoSubyacente",c."Instrumento",c."InstrumentoSubyacente" , 
            CASE 
                WHEN c."IdTipoInstrumento" = 'PAG' THEN c."ValorRescate" 
                ELSE c."CantidadOrden" 
            END AS "CantidadOrden",
            c."MontoOrden",COALESCE(c."IdMonedaOrden",'') AS "IdMonedaOrden",c."PrecioOrden",c."TasaRFIF",
            CASE 
                WHEN c."IdTipoVigenciaOrden" = 'SH' THEN 1
                WHEN c."IdTipoVigenciaOrden" = 'OP' THEN 0
                WHEN c."IdTipoVigenciaOrden" = 'SE' THEN 7
                WHEN c."IdTipoVigenciaOrden" = 'FF' THEN DAYS_BETWEEN(TO_DATE(c."FechaAceptacion"), TO_DATE(c."FechaVigencia"))
            END AS "Plazo",
            c."FechaRecepcion",c."FechaAceptacion",c."IdEstadoOrden",c."FechaAnulacion",c."GlosaOrden",COALESCE(c."IdClasificacionTipoInstrumento",'') AS "IdClasificacionTipoInstrumento",
            COALESCE(c."IdBolsa",'') AS "IdBolsa",COALESCE(c."IdTipoInstrumento",'') AS "IdTipoInstrumento",c."IdTipoOperacion",c."EsVentaCorta",c."DocumentoIdentidad", c."IdPersona",c."FechaOperacionOrden",c."AccionOrden", c."InstrumentoNegociacion"  
        FROM "BSCL.Orden.BD.Vistas::ConsultarOrden" AS c
        LEFT JOIN "BSCL.Orden.BD::ORD.CobroOrden" AS cob ON (cob."IdOrden" = c."IdOrden")
        INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'RUT') AS r ON (r."Valor" IS NULL OR r."IdSistema" = c."IdCliente")
        INNER JOIN (SELECT "Valor","IdSistema" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'NEMO') AS n ON (n."Valor" IS NULL OR n."IdSistema" = c."IdInstrumento")
        INNER JOIN (SELECT "Valor" FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" WHERE "IdSolicitudCircular" = :p_IdSolicitudCircular AND "TipoParametro" = 'CLAINS') AS m ON (m."Valor" IS NULL OR m."Valor" = c."IdClasificacionTipoInstrumento")
        INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLIENTE ON CLIENTE."IdCliente" = c."IdCliente"
        GROUP BY 
            c."FolioOrden",
            c."IdOrden",c."IdCliente",c."CodigoPortafolio",c."IdPersonaAutorizadoDarOrdenes",c."IdEmpleadoPortafolio",c."CodigoClaseOrden",
            c."ISINInstrumento",c."ISINInstrumentoSubyacente",c."Instrumento",c."InstrumentoSubyacente" ,c."CantidadOrden",c."MontoOrden","IdMonedaOrden",c."PrecioOrden",c."TasaRFIF",
            c."Plazo",c."FechaRecepcion",c."FechaAceptacion",c."IdEstadoOrden",c."FechaAnulacion",c."GlosaOrden","IdClasificacionTipoInstrumento",
            "IdBolsa","IdTipoInstrumento",c."IdTipoOperacion",c."EsVentaCorta",c."DocumentoIdentidad", c."IdPersona",c."FechaOperacionOrden",c."AccionOrden", c."InstrumentoNegociacion",  
            c."IdTipoVigenciaOrden", c."FechaVigencia", c."ValorRescate"
    ) AS c
    LEFT JOIN "BSCL.Orden.BD::ORD.Orden" AS ord ON (ord."IdOrden" = c."IdOrden" AND c."IdTipoInstrumento" = 'PAG')
    --homologacion
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hco ON (hco."ValorOrigen" = c."CodigoClaseOrden"
                                                           AND hco."Version" = :v_Version
                                                           AND hco."Esquema" = 'NCG380Ord'
                                                           AND hco."IdEstado" = 'ACT'
                                                           AND hco."Tipo" = 'ClaseOrden')
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hto ON (hto."ValorOrigen" = c."IdTipoOperacion"
                                                           AND hto."Version" = :v_Version
                                                           AND hto."Esquema" = 'NCG380Ord'
                                                           AND hto."IdEstado" = 'ACT'
                                                           AND hto."Tipo" = 'TipoOrden')
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hti ON (hti."ValorOrigen" = c."IdTipoInstrumento"
                                                           AND hti."Version" = :v_Version
                                                           AND hti."Esquema" = 'NCG380Ord'
                                                           AND hti."IdEstado" = 'ACT'
                                                           AND hti."Tipo" = 'TipoInstrumento')
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hpm ON (hpm."ValorOrigen" = c."IdClasificacionTipoInstrumento"
                                                           AND hpm."Version" = :v_Version
                                                           AND hpm."Esquema" = 'NCG380Ord'
                                                           AND hpm."IdEstado" = 'ACT'
                                                           AND hpm."Tipo" = 'PreferenciaMecanismo')
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hpb ON (hpb."ValorOrigen" = c."IdBolsa"
                                                           AND hpb."Version" = :v_Version
                                                           AND hpb."Esquema" = 'NCG380Ord'
                                                           AND hpb."IdEstado" = 'ACT'
                                                           AND hpb."Tipo" = 'PreferenciaBolsa')
    LEFT JOIN "BSCL.Normativa.BD::DOC.Homologacion" AS hm ON (hm."ValorOrigen" = c."IdMonedaOrden"
                                                          AND hm."Version" = :v_Version
                                                          AND hm."Esquema" = 'NCG380Ord'
                                                          AND hm."IdEstado" = 'ACT'
                                                          AND hm."Tipo" = 'Moneda')
    WHERE CAST(c."FechaAceptacion" AS DATE) >= :v_FechaInicio
     	AND (CASE WHEN c."IdTipoInstrumento" in ('PRT','EMP','SIM','RRV','RRF') THEN c."InstrumentoSubyacente"     ELSE   c."Instrumento"      END <> '')
     	AND (:v_FechaTermino IS NULL OR CAST(c."FechaAceptacion" AS DATE) <= :v_FechaTermino)
    ORDER BY c."IdCliente" ASC;
      
END;
GO