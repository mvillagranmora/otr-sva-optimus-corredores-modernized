SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate";
GO
CREATE PROCEDURE
--  Proposito: Calcula Fecha Aceptacion, Fecha Certificacion y Fecha de liquidacion de un fondo.
--  Autor: Gat-Blac 
--  Fecha Creacion: 05/09/2016
-------------------------------MODIFICACIONES-----------------------------------------------------------------
--  Proposito: Se exporta obtencion de valores para el calculo de fechas de un fondo a la tabla BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo.
--             Se agrega la fecha de valor cuota
--             Se llama varias veces al nuevo utilitario BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate, ya que se debe esperar el resultado
--             del calculo de una fecha, para poder calcular la siguiente fecha
--             Se obtiene el valor del parametro MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE, para poder determinar cual es la forma en la que se realiza el calulo
--             si el valor esta en blanco, se seguira realizando como hasta ahora (unitario o batch).
--  Autor: OCT - Oscar Carreno Tolmo
--  Fecha Creacion: 04/12/2017
"BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate" (
    IN p_IdEmpresa              BIGINT,
	IN p_IdPais		  			BIGINT,
	IN p_SolicitudAporteRescate TABLE(
									"IdInterno"			  		BIGINT,
									"IdSerieFondo"		  		NVARCHAR(40),
									"FechaEmision"		  		NVARCHAR(10),
									"EstadoFondo"		  		NVARCHAR(1),
									"EstadoLiquidacion"	  		NVARCHAR(1),
									"NumeroDiasRetencion" 		BIGINT,
									"NumeroValutaValorCuota"    BIGINT,
									"NumeroValutaCertificacion"	BIGINT,
									"NumeroValutaCurse"			BIGINT,
									"SolicitudBatch"			NVARCHAR(1),
									"TipoSolicitud"             NVARCHAR(3),
									"FondoExtranjero"           NVARCHAR(1),
									"IdPaisFondo"               BIGINT,
									"IdPaisEmisor"              BIGINT,
									"IdPaisMoneda"              BIGINT
								),
	OUT p_FechasAporteRescate	TABLE(
									"IdInterno"            BIGINT,
									"IdSerieFondo"         NVARCHAR(40),
									"FechaEmision"         DATE,
									"FechaDigitacion"      DATE,          
									"FechaAceptacion"      DATE,----"FechaCurse"
									"FechaValorCuota"      DATE,
									"FechaCertificacion"   DATE,
									"FechaLiquidacion"     DATE
								)
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
AS
BEGIN
	/*
	* TODO: AGREGAR VALUTA POR TIPO DE MOV TESO PARA DOCUMENTOS Y PAIS
	*/
	DECLARE v_IdModalidad NVARCHAR(10);
	DECLARE v_fechas TABLE ("IdInterno" BIGINT,"Fecha" DATE,"NumeroDias" BIGINT,"EsDiaHabil" NVARCHAR(1),"IdPaisLocal" BIGINT,"IdPaisEmisor" BIGINT,"IdPaisFondo" BIGINT,"IdPaisMoneda" BIGINT);
	DECLARE v_fechasOUT TABLE ("IdInterno" BIGINT,"Fecha" DATE,"NumeroDias" BIGINT,"FechaFinal" DATE,"EsDiaHabil" NVARCHAR(1));
	DECLARE v_feriadosOUT TABLE ("IdInterno" BIGINT,"Fecha" DATE,"IdPais" BIGINT,"NombrePais" NVARCHAR(100),"DescripcionDia" NVARCHAR(100));
                       
	v_fechas =
        SELECT 0 AS "IdInterno",'' AS "Fecha",0 AS "NumeroDias",'N' AS "EsDiaHabil",0 AS "IdPaisLocal",0 AS "IdPaisEmisor",0 AS "IdPaisFondo",0 AS "IdPaisMoneda" FROM DUMMY WHERE 1=2;
        
    v_fechasOUT =
        SELECT 0 AS "IdInterno",'0001-01-01' AS "Fecha",0 AS "NumeroDias",'0001-01-01' AS "FechaFinal",'N' AS "EsDiaHabil" FROM DUMMY WHERE 1=2;

    v_feriadosOUT= 
        SELECT 0 AS "IdInterno",'0001-01-01' AS "Fecha",0 AS "IdPais",'' AS "NombrePais",'' AS "DescripcionDia" FROM DUMMY WHERE 1=2;
    
    SELECT
        CASE WHEN "ValorParametro" = '' THEN NULL ELSE "ValorParametro" END INTO v_IdModalidad
    FROM "BSCL.FondoMutuo.BD::FMM.Parametro"
    WHERE "NombreTipoParametro" = 'MODALIDAD_SOLICITUD_FECHA_APORTE_RESCATE';
    
	INSERT INTO "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"(
        "IdInternoProceso","IdInterno","IdSerieFondo","FechaEmision",
        --DatosCalculados
        "FechaInicial","CantidadDiasValuta","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda",
        --Configuracion
        "IdConfiguracionFechasFondoMutuo","IdTipoFecha","EsHabil","IdTipoFechaOrigen","DebeSerHabilFechaOrigen")
	SELECT
        ROW_NUMBER() over (ORDER BY c."IdConfiguracionFechasFondoMutuo"),
	    --Operacion
        s."IdInterno",s."IdSerieFondo",s."FechaEmision",
        --DatosCalculados
        CASE WHEN c."IdTipoFechaOrigen" IS NULL THEN s."FechaEmision" ELSE NULL END AS "FechaInicial",
        (c."CantidadDias" + (
            CASE WHEN c."ValutaCurse" = 'S' THEN COALESCE(s."NumeroValutaCurse",0) ELSE 0 END +
            CASE WHEN c."ValutaValorCuota" = 'S' THEN COALESCE(s."NumeroValutaValorCuota",0) ELSE 0 END +
            CASE WHEN c."ValutaCertificacion" = 'S' THEN COALESCE(s."NumeroValutaCertificacion",0) ELSE 0 END +
            CASE WHEN c."ValutaRetencion" = 'S' THEN COALESCE(s."NumeroDiasRetencion",0) ELSE 0 END)) AS "CantidadDiasValuta",        
        CASE WHEN c."CalendarioLocal" = 'S' THEN :p_IdPais ELSE NULL END,
        CASE WHEN c."CalendarioEmisor" = 'S' THEN s."IdPaisEmisor" ELSE NULL END,
        CASE WHEN c."CalendarioFondo" = 'S' THEN s."IdPaisFondo" ELSE NULL END,
        CASE WHEN c."CalendarioMoneda" = 'S' THEN s."IdPaisMoneda" ELSE NULL END,
        --Configuracion
        c."IdConfiguracionFechasFondoMutuo",c."IdTipoFecha",
        --------------------------------------------------------------------------
        c."EsHabil",c."IdTipoFechaOrigen",c."DebeSerHabilFechaOrigen"
	FROM :p_SolicitudAporteRescate AS s
	INNER JOIN "BSCL.FondoMutuo.BD::FMM.ConfiguracionFechasFondoMutuo" AS c ON ((c."TipoSolicitud" IS NULL OR c."TipoSolicitud" = s."TipoSolicitud")
	                                                                        AND (c."EstadoFondo" IS NULL OR c."EstadoFondo" = s."EstadoFondo")
	                                                                        AND (c."FondoExtranjero" IS NULL OR c."FondoExtranjero" = s."FondoExtranjero"))
	WHERE (:v_IdModalidad IS NULL AND ((c."IdModalidad" = 'UNITARIO' AND "SolicitudBatch" = 'N') OR (c."IdModalidad" = 'BATCH' AND "SolicitudBatch" = 'S')) OR c."IdModalidad" = :v_IdModalidad)
	AND c."IdEmpresa" = :p_IdEmpresa
	AND c."IdEstado" = 'ACT'; 
	
	--IdTipoFechaOrigen --> null
	v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFechaOrigen" IS NULL;
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
    	
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFechaOrigen" IS NULL)
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);                                                                                                     
    END IF;
    
    
    --IdTipoFechaOrigen --> DIGITA (Digitación)
    --Si un registro tiene como fecha origen DIGITA, se verifica si la fecha es habil, si no lo es se manda a calcular la fecha nuevamente con la nueva configuracion
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFecha" = 'DIGITA'
          AND "DebeSerHabilFechaOrigen" = 'S'
          AND "FechaInicialEsHabil" = 'N';
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFecha" = 'DIGITA')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFechaOrigen" = 'DIGITA'
          AND "IdTipoFechaOrigen" != "IdTipoFecha";
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFechaOrigen" = 'DIGITA')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    
    --IdTipoFechaOrigen --> CURSE (fecha de curse)
    --Si un registro tiene como fecha origen CURSE, se verifica si la fecha es habil, si no lo es se manda a calcular la fecha nuevamente con la nueva configuracion
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFecha" = 'CURSE'
          AND "DebeSerHabilFechaOrigen" = 'S'
          AND "FechaInicialEsHabil" = 'N';
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFecha" = 'CURSE')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFechaOrigen" = 'CURSE'
          AND "IdTipoFechaOrigen" != "IdTipoFecha";
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFechaOrigen" = 'CURSE')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    --IdTipoFechaOrigen --> VCUOTA (Valor Cuota)
    --Si un registro tiene como fecha origen VCUOTA, se verifica si la fecha es habil, si no lo es se manda a calcular la fecha nuevamente con la nueva configuracion
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFecha" = 'VCUOTA'
          AND "DebeSerHabilFechaOrigen" = 'S'
          AND "FechaInicialEsHabil" = 'N';
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFecha" = 'VCUOTA')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFechaOrigen" = 'VCUOTA'
          AND "IdTipoFechaOrigen" != "IdTipoFecha";
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFechaOrigen" = 'VCUOTA')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    --IdTipoFechaOrigen --> CERTIF (Certificación)
    --Si un registro tiene como fecha origen CERTIF, se verifica si la fecha es habil, si no lo es se manda a calcular la fecha nuevamente con la nueva configuracion
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFecha" = 'CERTIF'
          AND "DebeSerHabilFechaOrigen" = 'S'
          AND "FechaInicialEsHabil" = 'N';
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFecha" = 'CERTIF')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFechaOrigen" = 'CERTIF'
          AND "IdTipoFechaOrigen" != "IdTipoFecha";
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFechaOrigen" = 'CERTIF')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    --IdTipoFechaOrigen --> LIQUID (Liquidación)
    --Si un registro tiene como fecha origen LIQUID, se verifica si la fecha es habil, si no lo es se manda a calcular la fecha nuevamente con la nueva configuracion
    v_fechas =
        SELECT
            "IdInternoProceso" AS "IdInterno","FechaInicial" AS "Fecha","CantidadDiasValuta" AS "NumeroDias",
            "EsHabil" AS "EsDiaHabil","IdPaisLocal","IdPaisEmisor","IdPaisFondo","IdPaisMoneda"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
        WHERE "IdTipoFecha" = 'LIQUID'
          AND "DebeSerHabilFechaOrigen" = 'S'
          AND "FechaInicialEsHabil" = 'N';
    
    IF (NOT IS_EMPTY(:v_fechas)) THEN
        CALL "BSCL.FondoMutuo.BD.Utilitarios::ObtenerFechaAporteRescate"(
            p_FechasIN      =>:v_fechas,
            p_FechasOUT     =>v_fechasOUT,
            p_FeriadosOUT   =>v_feriadosOUT);
        
        UPDATE p
            SET
                "FechaFinal" = f."FechaFinal",
                "FechaFinalEsHabil" = f."EsDiaHabil" 
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN :v_fechasOUT AS f ON (f."IdInterno" = p."IdInternoProceso");
        
        UPDATE p
            SET
                "FechaInicial" = f."FechaFinal",
                "FechaInicialEsHabil" = f."FechaFinalEsHabil"
        FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS p
        INNER JOIN "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas" AS f ON (f."IdTipoFecha" = p."IdTipoFechaOrigen"
                                                                                                     AND f."IdInterno" = p."IdInterno"
                                                                                                     AND f."IdTipoFecha" = 'LIQUID')
        WHERE p."IdInternoProceso" NOT IN (SELECT "IdInterno" FROM :v_fechas);
    END IF;
    
    --SELECT * FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"; --BORRAR
    
    --Se agrupan las fechas por id interno
    p_FechasAporteRescate =
        SELECT 
            s."IdInterno",s."IdSerieFondo",s."FechaEmision",f."FechaDigitacion",f."FechaCurse" AS "FechaAceptacion",f."FechaValorCuota",
            f."FechaCertificacion",f."FechaLiquidacion"
        FROM :p_SolicitudAporteRescate AS s
        LEFT JOIN ( SELECT
                        "IdInterno",
                        COALESCE(MAX("FechaDigitacion"),'0001-01-01') AS "FechaDigitacion",
                        COALESCE(MAX("FechaCurse"),'0001-01-01') AS "FechaCurse",
                        COALESCE(MAX("FechaValorCuota"),'0001-01-01') AS "FechaValorCuota",
                        COALESCE(MAX("FechaCertificacion"),'0001-01-01') AS "FechaCertificacion",
                        COALESCE(MAX("FechaLiquidacion"),'0001-01-01') AS "FechaLiquidacion"       
                    FROM
                    (
                        SELECT
                            "IdInterno",
                            CASE WHEN "IdTipoFecha"='DIGITA' THEN "FechaFinal" ELSE NULL END AS "FechaDigitacion",
                            CASE WHEN "IdTipoFecha"='CURSE' THEN "FechaFinal" ELSE NULL END AS "FechaCurse",
                            CASE WHEN "IdTipoFecha"='VCUOTA'  THEN "FechaFinal" ELSE NULL END AS "FechaValorCuota",
                            CASE WHEN "IdTipoFecha"='CERTIF'  THEN "FechaFinal" ELSE NULL END AS "FechaCertificacion",
                            CASE WHEN "IdTipoFecha"='LIQUID'  THEN "FechaFinal" ELSE NULL END AS "FechaLiquidacion"
                        FROM (SELECT
                                "IdInterno","IdTipoFecha","FechaFinal"
                              FROM "BSCL.FondoMutuo.BD::GBL.CalcularFechaSolicitudAporteRescate_Proceso_Fechas"
                             )
                    )
                    GROUP BY "IdInterno") AS f ON (f."IdInterno" = s."IdInterno");
END;
GO