SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1870.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1870.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa                              BIGINT       ,
    -- ID usuario del login.
    IN p_IdUsuario                              BIGINT       ,
    -- ID modulo invocador.
    IN p_IdModulo                               NVARCHAR(3)  ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID                                     BIGINT       ,
    -- ID del documento.
    IN p_IdDocumento                            BIGINT       ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario                             CHAR(1)      ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones                          NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos                            NVARCHAR(40) ,
    IN p_IdComprobante                          BIGINT       ,
    IN p_FechaComprobante                       DATE         ,
    IN p_IdCliente                              BIGINT       ,
    IN p_IdPortafolio                           BIGINT       ,
    IN p_Numero                                 BIGINT       ,
    IN p_DatosPersonaRut                        NVARCHAR(40) ,
    IN p_DatosPersonaDV                         NVARCHAR(10) ,
    IN p_DatosPersonaIndentificadorExtranjero   NVARCHAR(40) ,
    IN p_DatosPersonaPaisOtorganteIdentificador NVARCHAR(10) ,
    IN p_DatosPersonaNombreRazonSocial          NVARCHAR(100),
    IN p_TipoTransaccionDJ                      NVARCHAR(100),
    IN p_ValoresTransados                       NVARCHAR(100),
    IN p_FechaTransaccionDia                    NVARCHAR(10) ,
    IN p_FechaTransaccionMes                    NVARCHAR(10) ,
    IN p_FechaTransaccionAnio                   NVARCHAR(10) ,
    IN p_DocumentoTipoDocumento                 NVARCHAR(100),
    IN p_DocumentoNumero                        NVARCHAR(100),
    IN p_CodigoMonedaTransada                   NVARCHAR(10) ,
    IN p_MontoCompras                           DECIMAL(26,8),
    IN p_MontoVentas                            DECIMAL(26,8),
    IN p_TipoCambio                             DECIMAL(26,8),
    IN p_TipoDocumento                          NVARCHAR(10) ,
    IN p_NumeroDocumento                        BIGINT       ,
    IN p_FechaDocumento                         DATE         ,
    IN p_IdBaseDocumento                        BIGINT       ,
    IN p_TipoTransaccion                        NVARCHAR(6)  ,
    IN p_IdTipoOperacion                        NVARCHAR(6)  ,
    IN p_IdTipoProducto                         NVARCHAR(4)  ,


    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id                                BIGINT       ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador              INT          ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------

    ------------ Bloque Comun de Inicializacion ----------

    DECLARE salida_controlada condition;
    DECLARE exit handler FOR salida_controlada
    BEGIN
        SELECT COUNT(1) INTO p_out_AdvertenciasContador FROM :p_out_AdvertenciasDetalle;
        SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
    END;

    DECLARE exit handler FOR sqlexception
    BEGIN
        DECLARE v_errorExt NVARCHAR(40);
        IF ::sql_error_code > 10000 THEN
            -- Error pseudo-controlado pero desde un sub-sp.
            CALL "BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
            p_out_ErroresDetalle = SELECT * FROM :p_out_ErroresDetalle UNION ALL
                                   SELECT v_errorExt, ::sql_error_message FROM dummy;
            SELECT COUNT(1) INTO p_out_ErroresContador FROM :p_out_ErroresDetalle;
        ELSE
            -- Error interno no manejado (el log sera captado por la capa de XS control).
            RESIGNAL;
        END IF;
    END;

    p_out_AdvertenciasContador := 0;
    p_out_ErroresContador := 0;
    p_out_AdvertenciasDetalle = SELECT '' AS "Codigo", '' AS "RequiereAutorizacion", '' AS "Descripcion" FROM dummy WHERE 1 = 2;
    p_out_ErroresDetalle = SELECT '' AS "Codigo", '' AS "Descripcion" FROM dummy WHERE 1 = 2;

    ------------ Logica de Actualizacion ----------

    IF p_ID > 0 THEN
        p_out_id := p_ID;

        UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1870" SET
            "IdDocumento"                            = p_IdDocumento,
            "VboUsuario"                             = p_VboUsuario,
            "Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END),
            "FuenteDatos"                            = p_FuenteDatos,
            "DatosPersonaRut"                        = p_DatosPersonaRut,
            "DatosPersonaDV"                         = p_DatosPersonaDV,
            "DatosPersonaIndentificadorExtranjero"   = p_DatosPersonaIndentificadorExtranjero,
            "DatosPersonaPaisOtorganteIdentificador" = p_DatosPersonaPaisOtorganteIdentificador,
            "DatosPersonaNombreRazonSocial"          = p_DatosPersonaNombreRazonSocial,
            "TipoTransaccionDJ"                      = p_TipoTransaccionDJ,
            "ValoresTransados"                       = p_ValoresTransados,
            "FechaTransaccionDia"                    = p_FechaTransaccionDia,
            "FechaTransaccionMes"                    = p_FechaTransaccionMes,
            "FechaTransaccionAnio"                   = p_FechaTransaccionAnio,
            "DocumentoTipoDocumento"                 = p_DocumentoTipoDocumento,
            "DocumentoNumero"                        = p_DocumentoNumero,
            "CodigoMonedaTransada"                   = p_CodigoMonedaTransada,
            "MontoCompras"                           = p_MontoCompras,
            "MontoVentas"                            = p_MontoVentas,
            "TipoCambio"                             = p_TipoCambio,
            "FechaDocumento"                         = p_FechaDocumento,
            "IdCliente"                              = p_IdCliente,
            "IdPortafolio"                           = p_IdPortafolio
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1870";

        INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1870" (
            "ID",
            "IdDocumento",
            "VboUsuario",
            "Observaciones",
            "FuenteDatos",
            "DatosPersonaRut",
            "DatosPersonaDV",
            "DatosPersonaIndentificadorExtranjero",
            "DatosPersonaPaisOtorganteIdentificador",
            "DatosPersonaNombreRazonSocial",
            "TipoTransaccionDJ",
            "ValoresTransados",
            "FechaTransaccionDia",
            "FechaTransaccionMes",
            "FechaTransaccionAnio",
            "DocumentoTipoDocumento",
            "DocumentoNumero",
            "CodigoMonedaTransada",
            "MontoCompras",
            "MontoVentas",
            "TipoCambio",
            "FechaDocumento",
            "IdCliente",
            "IdPortafolio",
            "IndNacionalidad",
            "Paridad",
            "LineaFactura"
        ) VALUES (
            p_out_id,
            p_IdDocumento,
            'N',
            '',
            'MANTENEDOR',
            p_DatosPersonaRut,
            p_DatosPersonaDV,
            p_DatosPersonaIndentificadorExtranjero,
            p_DatosPersonaPaisOtorganteIdentificador,
            p_DatosPersonaNombreRazonSocial,
            p_TipoTransaccionDJ,
            p_ValoresTransados,
            p_FechaTransaccionDia,
            p_FechaTransaccionMes,
            p_FechaTransaccionAnio,
            p_DocumentoTipoDocumento,
            p_DocumentoNumero,
            p_CodigoMonedaTransada,
            p_MontoCompras,
            p_MontoVentas,
            p_TipoCambio,
            p_FechaDocumento,
            p_IdCliente,
            p_IdPortafolio,
            '',
            0,
            0
        );

    END IF;
END;