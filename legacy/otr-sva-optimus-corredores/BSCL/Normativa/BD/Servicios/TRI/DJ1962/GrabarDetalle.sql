SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1962.GrabarDetalle";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1962.GrabarDetalle"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa 									BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 									BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 									NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    -- ID del detalle.
    -- Si el id es mayor que cero, se asume que es un registro existente para actualizar.
    -- En caso contrario, se intenta crear un nuevo registro.
    IN p_ID 										BIGINT		 ,
    -- ID del documento.
    IN p_IdDocumento 								BIGINT		 ,
    -- VoBo del usuario sobre el registro (S/N).
    IN p_VboUsuario 								CHAR(1)		 ,
    -- Observacion ingresada por usuario que edito el registro.
    IN p_Observaciones 								NVARCHAR(250),
    -- Fuente de los datos.
    IN p_FuenteDatos 								NVARCHAR(40) ,							


	-- SECCION B
	IN p_RutAdministradora									    NVARCHAR(40)	,--C1
	IN p_RutFondoB											    NVARCHAR(40)	,--C2
	IN p_RunFondo											    NVARCHAR(40)	,--C3

	IN p_TipoFondo											    NVARCHAR(100)	,--C4
    IN p_TributaSa											    NVARCHAR(1)		,--C5
    IN p_DetBenNetoPerPOS_C6  							        DECIMAL(26,8)	,--C6
	IN p_DetBenNetoPerNEG_C7 							        DECIMAL(26,8)	,--C7
    IN p_DetBenNetoUtilPer_C8  									DECIMAL(26,8)	,--DetBenNetoUtilPer_C8
	IN p_DetBenNetoIntPer_C9  									DECIMAL(26,8)	,--DetBenNetoIntPer_C9
	IN p_DetBenNetoDivPer_C10 									DECIMAL(26,8)	,--DetBenNetoDivPer_C10
	IN p_DetBenNetoGanCapPer_C11 								DECIMAL(26,8)	,--DetBenNetoGanCapPer_C11
	IN p_DetBenNetoPerGastDev_C12 								DECIMAL(26,8)	,--DetBenNetoPerGastDev_C12

    -- SECCION C
    IN p_Numero												    BIGINT			,
    IN p_RutFondoC											    NVARCHAR(40)	,


	IN p_RutCliente											    NVARCHAR(40)	,
	IN p_NombreParticipe									    NVARCHAR(250)	,						
	IN p_TaxId												    NVARCHAR(250)	,
	IN p_CodigoPais											    NVARCHAR(100)	,
    IN p_MonedaReajuste										    NVARCHAR(100)	,
    IN p_MontoAporteNominal								        DECIMAL(26,8)	,
	IN p_MontoNominalAportesActualizado						    DECIMAL(26,8)	,
	IN p_CantidadCuotasParticipe							    DECIMAL(26,8)	,
    IN p_ParticipeCumpleRequisitos							    BIGINT			,
    IN p_CuotasCumplenRequisitos							    BIGINT			,
	IN p_ValorCuotaPesos									    DECIMAL(26,8)	,
    IN p_PorcentajeParticipacion31Dic						    DECIMAL(26,8)	,
    IN p_TipoContribuyente                                      NVARCHAR(1)     ,

    -- DATOS DE CONTROL
    IN p_IdOperacion										    BIGINT			,
    IN p_IdEmisor											    NVARCHAR(40)	,
    IN p_FechaRespaldo								            DATE		    ,
    IN p_Fondo												    NVARCHAR(100)	,
    --IN p_IdCliente 											    BIGINT          ,
	--IN p_IdPortafolio 										    BIGINT          ,
	IN p_FolioCartera                                           BIGINT          ,
    IN p_FechaOperacion									        DATE			,
   
	 
	    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 									BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 					INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),



    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 						INT,

    -- Tabla de errores.
    OUT p_out_ErroresDetalle TABLE("Codigo" NVARCHAR(40), "Descripcion" NVARCHAR(5000))
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Variables Propias ----------
        
    -- Variable para la version (Fecha de Respaldo)

    DECLARE v_Version NVARCHAR(40);


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

    --Obtencion de la version
    select "Version" into v_Version from "BSCL.Normativa.BD::DOC.Documento" DOC where DOC."IdDocumento"=p_IdDocumento;

    -- MODIFICAR
    IF p_ID > 0 THEN
        p_out_id := p_ID;

 
		UPDATE "BSCL.Normativa.BD::TRI.DatosDJ1962"
		SET "IdDocumento"                   = 	p_IdDocumento
		, "VboUsuario"                      = 	p_VboUsuario
		, "Observaciones"                   = (CASE WHEN LENGTH(TRIM(:p_Observaciones)) BETWEEN 4 AND 5 AND LEFT(UPPER(TRIM(:p_Observaciones)), 1) = 'H' THEN "Observaciones" ELSE :p_Observaciones END)
		, "FuenteDatos"                     = 	p_FuenteDatos
        ,"RutAdministradora"                = p_RutAdministradora
        ,"RutFondoB"                        = p_RutFondoB
        ,"RunFondo"                         = p_RunFondo
        ,"TipoFondo"                        = p_TipoFondo
        ,"TributaSa"                        = p_TributaSa
        ,"DetBenNetoPerPOS_C6"              = p_DetBenNetoPerPOS_C6
        ,"DetBenNetoPerNEG_C7"              = p_DetBenNetoPerNEG_C7
        ,"DetBenNetoUtilPer_C8"             = p_DetBenNetoUtilPer_C8
        ,"DetBenNetoIntPer_C9"              = p_DetBenNetoIntPer_C9
        ,"DetBenNetoDivPer_C10"             = p_DetBenNetoDivPer_C10
        ,"DetBenNetoGanCapPer_C11"          = p_DetBenNetoGanCapPer_C11
        ,"DetBenNetoPerGastDev_C12"          = p_DetBenNetoPerGastDev_C12
        ,"Numero"                            = p_Numero
        ,"RutFondoC"                         = p_RutFondoC
        ,"RutCliente"                        = p_RutCliente
        ,"NombreParticipe"                   = p_NombreParticipe
        ,"TaxId"                             = p_TaxId
        ,"CodigoPais"                        = p_CodigoPais
        ,"MonedaReajuste"                    = p_MonedaReajuste
        ,"MontoAporteNominal"                = p_MontoAporteNominal
        ,"MontoNominalAportesActualizado"    = p_MontoNominalAportesActualizado
        ,"CantidadCuotasParticipe"           = p_CantidadCuotasParticipe
        ,"ParticipeCumpleRequisitos"          = p_ParticipeCumpleRequisitos
        ,"CuotasCumplenRequisitos"            = p_CuotasCumplenRequisitos
        ,"ValorCuotaPesos"                   = p_ValorCuotaPesos
        ,"PorcentajeParticipacion31Dic"      = p_PorcentajeParticipacion31Dic
        ,"TipoContribuyente"                 = TO_VARCHAR(p_TipoContribuyente)

        --CONTROL INTERNO E INFORMACION
        ,"IdOperacion"                       = p_IdOperacion
        ,"IdEmisor"                          = TO_VARCHAR(p_IdEmisor)
        ,"FechaRespaldo"                     = p_FechaRespaldo
        ,"Fondo"                             = p_Fondo
        ,"FechaOperacion"                    = p_FechaOperacion

		WHERE ID = p_ID;
	
    -- CREAR 
    ELSE
        SELECT IFNULL(MAX("ID"),0)+1 INTO p_out_id FROM "BSCL.Normativa.BD::TRI.DatosDJ1962";

		       
		       
		INSERT INTO "BSCL.Normativa.BD::TRI.DatosDJ1962" (
            "ID","IdDocumento","VboUsuario","Observaciones","FuenteDatos",
            "RutAdministradora","RutFondoB","RunFondo","TipoFondo","TributaSa",
            "DetBenNetoPerPOS_C6","DetBenNetoPerNEG_C7","DetBenNetoUtilPer_C8",
            "DetBenNetoIntPer_C9","DetBenNetoDivPer_C10","DetBenNetoGanCapPer_C11",
            "DetBenNetoPerGastDev_C12","Numero","RutFondoC","RutCliente",
            "NombreParticipe","TaxId","CodigoPais","MonedaReajuste",
            "MontoAporteNominal","MontoNominalAportesActualizado",
            "CantidadCuotasParticipe","ParticipeCumpleRequisitos",
            "CuotasCumplenRequisitos","ValorCuotaPesos",
            "PorcentajeParticipacion31Dic","TipoContribuyente",
            "IdOperacion","IdEmisor","FechaRespaldo","Fondo",
            "IdCliente","IdPortafolio","FolioCartera","FechaOperacion"
        )
		
		VALUES (
            p_out_id,                                   -- ID
            p_IdDocumento,                              -- IdDocumento
            'N',                                        -- VboUsuario
            '',                                         -- Observaciones
            'MANTENEDOR',                               -- FuenteDatos

            p_RutAdministradora,                        -- RutAdministradora
            p_RutFondoB,                                -- RutFondoB
            p_RunFondo,                                 -- RunFondo
            p_TipoFondo,                                -- TipoFondo
            p_TributaSa,                                -- TributaSa
            p_DetBenNetoPerPOS_C6,                      -- C6
            p_DetBenNetoPerNEG_C7,                      -- C7
            p_DetBenNetoUtilPer_C8,                     -- C8
            p_DetBenNetoIntPer_C9,                      -- C9
            p_DetBenNetoDivPer_C10,                     -- C10
            p_DetBenNetoGanCapPer_C11,                  -- C11
            p_DetBenNetoPerGastDev_C12,                 -- C12
            0,                                          -- Numero
            p_RutFondoC,                                -- RutFondoC
            p_RutCliente,                               -- RutCliente
            p_NombreParticipe,                          -- NombreParticipe
            p_TaxId,                                    -- TaxId
            p_CodigoPais,                               -- CodigoPais
            p_MonedaReajuste,                           -- MonedaReajuste
            IFNULL(p_MontoAporteNominal,0),                       -- MontoAporteNominal
            p_MontoNominalAportesActualizado,           -- MontoNominalAportesActualizado
            p_CantidadCuotasParticipe,                  -- CantidadCuotasParticipe
            p_ParticipeCumpleRequisitos,                -- ParticipeCumpleRequisitos
            p_CuotasCumplenRequisitos,                  -- CuotasCumplenRequisitos
            p_ValorCuotaPesos,                          -- ValorCuotaPesos
            p_PorcentajeParticipacion31Dic,             -- PorcentajeParticipacion31Dic
            p_TipoContribuyente,                        -- TipoContribuyente
            0,                                          -- IdOperacion
            p_IdEmisor,                                 -- IdEmisor
            TO_DATE(v_Version || '-12-31', 'YYYY-MM-DD'),-- FechaRespaldo (IMPORTANTE DATE FILTER)
            p_Fondo,                                    -- Fondo
            0,                                          -- IdCliente (En 0, para aceptar registros de clientes que esten fuera de OPTIMUS)
            0,                                          -- IdPortafolio 
            0,                                          -- FolioCartera
            '0001-01-01'                        -- FechaOperacion
        );
		  
       
    END IF;
END;