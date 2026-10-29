SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1958.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1958.GrabarDetalle"(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 								BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 								BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 								NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
     -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID                                     BIGINT   ,
    -- ID del documento.
    IN p_IdDocumento                            BIGINT   ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario                             CHAR(1)   ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones                          NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos                            NVARCHAR(40) ,
    -- IN p_IdComprobante                       BIGINT       ,

    IN p_PaisResidenciaDeclarante               NVARCHAR(2),
    IN p_TipoInstitucion                        NVARCHAR(1),
    IN p_PoseeCuentasFinanciera                 NVARCHAR(1),
    IN p_PeriodoReporte                         DATE,
    IN p_PaisResidencia                         NVARCHAR(2),
    IN p_TipoPersona                            NVARCHAR(1),
    IN p_TipoJuridico                           NVARCHAR(6),
    IN p_Nombres                                NVARCHAR(100),
    IN p_Apellidos                              NVARCHAR(100),
    IN p_RazonSocial                            NVARCHAR(150),
    IN p_PaisTaxID                              NVARCHAR(2),
    IN p_TaxID                                  NVARCHAR(21),
    IN p_PaisDomicilio                          NVARCHAR(2),
    IN p_TipoDomicilio                          NVARCHAR(7),
    IN p_Domicilio                              NVARCHAR(180),
    IN p_FechaNacimiento                        DATE,
    IN p_NumeroCuenta                           BIGINT,
    IN p_CuentaNoDocumentada                    BIGINT,
    IN p_CuentaCerrada                          BIGINT,
    IN p_DivisaSaldo                            NVARCHAR(3),
    IN p_SaldoActualizado                       DECIMAL(26,8),
    IN p_PaisResidenciaControlador              NVARCHAR(2),
    IN p_NombreControlador                      NVARCHAR(100),
    IN p_ApellidosControlador                   NVARCHAR(100),
    IN p_PaisTaxIDControlador                   NVARCHAR(2),
    IN p_TaxIDControlador                       NVARCHAR(21),
    IN p_PaisDomicilioControlador               NVARCHAR(2),
    IN p_TipoDomicilioControlador               NVARCHAR(7),
    IN p_DomicilioControlador                   NVARCHAR(180),
    IN p_FechaNacimientoControlador             DATE,
    IN p_TipoPagoCuentaFinanciera               NVARCHAR(6),
    IN p_DivisaPagoCuentaFinanciera             NVARCHAR(3),
    IN p_MontoPagoActualizado                   DECIMAL(26,8),
    IN p_IdCliente                              BIGINT,
    IN p_Nemo                                   NVARCHAR(40),
    IN p_IdTipoInstrumento                      NVARCHAR(3),
    IN p_IdClasificacionTipoInstrumento         NVARCHAR(3),
    IN p_TipoRegistro                           NVARCHAR(40),

    IN p_RutFondo								NVARCHAR(40)	,--c37

    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id                                BIGINT,

    ------------ Salidas Advertencias ----------
    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador              INT,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------
    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador                   INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS BEGIN
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
            CALL "BSCL"."BSCL.Plataforma.BD.Servicios::ConsultarCodigoMensajeError"(::sql_error_code, v_errorExt);
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
    p_out_AdvertenciasDetalle = select '' as "Codigo", '' as "RequiereAutorizacion", '' as "Descripcion" from dummy where 1 = 2;
    p_out_ErroresDetalle = select '' as "Codigo", '' as "Descripcion" from dummy where 1 = 2;

    ------------ Logica de Actualizacion ----------
    IF p_ID > 0 THEN
        p_out_id := p_ID;

        UPDATE "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1958" 
		SET
            "IdDocumento"								= p_IdDocumento
            ,"VboUsuario"								= p_VboUsuario
			,"Observaciones" = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
			,"FuenteDatos"								= p_FuenteDatos
            ,"PaisResidenciaDeclarante"                 = p_PaisResidenciaDeclarante
            ,"TipoInstitucion"                          = p_TipoInstitucion
            ,"PoseeCuentasFinanciera"                   = p_PoseeCuentasFinanciera
            ,"PeriodoReporte"                           = p_PeriodoReporte
            ,"PaisResidencia"                           = p_PaisResidencia
            ,"TipoPersona"                              = p_TipoPersona
            ,"TipoJuridico"                             = p_TipoJuridico
            ,"Nombres"                                  = p_Nombres
            ,"Apellidos"                                = p_Apellidos
            ,"RazonSocial"                              = p_RazonSocial
            ,"PaisTaxID"                                = p_PaisTaxID
            ,"TaxID"                                    = p_TaxID
            ,"PaisDomicilio"                            = p_PaisDomicilio
            ,"TipoDomicilio"                            = p_TipoDomicilio
            ,"Domicilio"                                = p_Domicilio
            ,"FechaNacimiento"                          = p_FechaNacimiento
            ,"NumeroCuenta"                             = p_NumeroCuenta
            ,"CuentaNoDocumentada"                      = p_CuentaNoDocumentada
            ,"CuentaCerrada"                            = p_CuentaCerrada
            ,"DivisaSaldo"                              = p_DivisaSaldo
            ,"SaldoActualizado"                         = p_SaldoActualizado
            ,"PaisResidenciaControlador"                = p_PaisResidenciaControlador
            ,"NombreControlador"                        = p_NombreControlador
            ,"ApellidosControlador"                     = p_ApellidosControlador
            ,"PaisTaxIDControlador"                     = p_PaisTaxIDControlador
            ,"TaxIDControlador"                         = p_TaxIDControlador
            ,"PaisDomicilioControlador"                 = p_PaisDomicilioControlador
            ,"TipoDomicilioControlador"                 = p_TipoDomicilioControlador
            ,"DomicilioControlador"                     = p_DomicilioControlador
            ,"FechaNacimientoControlador"               = p_FechaNacimientoControlador
            ,"TipoPagoCuentaFinanciera"                 = p_TipoPagoCuentaFinanciera
            ,"DivisaPagoCuentaFinanciera"               = p_DivisaPagoCuentaFinanciera
            ,"MontoPagoActualizado"                     = p_MontoPagoActualizado
            ,"IdCliente"                                = p_IdCliente
            ,"Nemo"                                     = TRIM(p_Nemo)
            ,"IdTipoInstrumento"                        = p_IdTipoInstrumento
            ,"IdClasificacionTipoInstrumento"           = p_IdClasificacionTipoInstrumento
            ,"TipoRegistro"                             = p_TipoRegistro
            ,"RutFondo"                         = p_RutFondo
        WHERE "ID" = p_ID;
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1958";
        INSERT INTO "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1958"(
            "ID"
			,"IdDocumento"
			,"VboUsuario"
			,"Observaciones"
			,"FuenteDatos"
            ,"PaisResidenciaDeclarante"	
            ,"TipoInstitucion"
            ,"PoseeCuentasFinanciera"
            ,"PeriodoReporte"
            ,"PaisResidencia"
            ,"TipoPersona"
            ,"TipoJuridico"
            ,"Nombres"
            ,"Apellidos"
            ,"RazonSocial"
            ,"PaisTaxID"
            ,"TaxID"
            ,"PaisDomicilio"
            ,"TipoDomicilio"
            ,"Domicilio"
            ,"FechaNacimiento"
            ,"NumeroCuenta"
            ,"CuentaNoDocumentada"
            ,"CuentaCerrada"
            ,"DivisaSaldo"
            ,"SaldoActualizado"
            ,"PaisResidenciaControlador"
            ,"NombreControlador"
            ,"ApellidosControlador"
            ,"PaisTaxIDControlador"
            ,"TaxIDControlador"
            ,"PaisDomicilioControlador"
            ,"TipoDomicilioControlador"
            ,"DomicilioControlador"
            ,"FechaNacimientoControlador"
            ,"TipoPagoCuentaFinanciera"
            ,"DivisaPagoCuentaFinanciera"
            ,"MontoPagoActualizado"
            ,"IdCliente"
            ,"Nemo"
            ,"IdTipoInstrumento"
            ,"IdClasificacionTipoInstrumento"
            ,"TipoRegistro"
            ,"RutFondo"
        ) VALUES (
            p_out_id
            ,p_IdDocumento
            ,'N'
            ,''
            ,'MANTENEDOR'
            ,p_PaisResidenciaDeclarante
            ,p_TipoInstitucion
            ,p_PoseeCuentasFinanciera
            ,p_PeriodoReporte
            ,p_PaisResidencia
            ,p_TipoPersona
            ,p_TipoJuridico
            ,p_Nombres
            ,p_Apellidos
            ,p_RazonSocial
            ,p_PaisTaxID
            ,p_TaxID
            ,p_PaisDomicilio
            ,p_TipoDomicilio
            ,p_Domicilio
            ,p_FechaNacimiento
            ,p_NumeroCuenta
            ,p_CuentaNoDocumentada
            ,p_CuentaCerrada
            ,p_DivisaSaldo
            ,p_SaldoActualizado
            ,p_PaisResidenciaControlador
            ,p_NombreControlador
            ,p_ApellidosControlador
            ,p_PaisTaxIDControlador
            ,p_TaxIDControlador
            ,p_PaisDomicilioControlador
            ,p_TipoDomicilioControlador
            ,p_DomicilioControlador
            ,p_FechaNacimientoControlador
            ,p_TipoPagoCuentaFinanciera
            ,p_DivisaPagoCuentaFinanciera
            ,p_MontoPagoActualizado
            ,p_IdCliente
            ,TRIM(p_Nemo)
            ,p_IdTipoInstrumento
            ,p_IdClasificacionTipoInstrumento
            ,p_TipoRegistro
            ,p_RutFondo
        );
    END IF;
END;