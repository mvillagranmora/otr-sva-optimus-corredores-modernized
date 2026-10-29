SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.Procesar4porcientoDJ1874";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1874.Procesar4porcientoDJ1874"
(
    ------------ Parametros de Entrada Comunes  ----------

    -- ID empresa del login.
    IN p_IdEmpresa 										BIGINT		 ,
    -- ID usuario del login.
    IN p_IdUsuario 										BIGINT		 ,
    -- ID modulo invocador.
    IN p_IdModulo 										NVARCHAR(3)	 ,
    ------------ Parametros de Entrada Propios ----------
    IN p_Version							            NVARCHAR(40) ,

    ------------ Parametros de Salida Propios ----------
    -- ID del registro.
    OUT p_out_id 										BIGINT		 ,

    ------------ Salidas Advertencias ----------

    -- Contador de advertencias.
    OUT p_out_AdvertenciasContador 						INT			 ,

    -- Tabla de advertencias.
    OUT p_out_AdvertenciasDetalle TABLE("Codigo" NVARCHAR(40), "RequiereAutorizacion" NVARCHAR(40), "Descripcion" NVARCHAR(5000)),

    ------------ Salidas Errores ----------

    -- Contador de errores
    -- Importante: Debe ser cero para que se haga commit.
    OUT p_out_ErroresContador 							INT,

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

  
    
    v_retencion = SELECT DJ1874."ID"
                              ,DJ1874."IdCliente"
							  ,DJ1874."IdEstadoInstrumento"
							  ,DJ1874."FechaOperacion"
							  ,DJ1874."FechaAdquisicion"
							  ,DJ1874."InteresDevengadoPesos"
							  ,DJ1874."InteresDevengadoMonedaUnidadReajuste"
							  ,DJ1874."MovimientoAdquision"
							  ,DJ1874."NemotecnicoSVS"
							  ,DJ1874."Nemotecnico"
							  ,DJ1874."IdInstrumento"
							  ,DJ1874."Cantidad"
							  ,DJ1874."Tasa"
							  ,DJ1874."Precio"
                              ,DJ1874."IdMonedaInstrumento"
                              ,DJ1874."IdMonedaEmisionInstrumento"
                              ,DJ1874."ValResultado"
					   FROM "BSCL.Normativa.BD::TRI.DatosDJ1874" DJ1874
                       WHERE YEAR(DJ1874."FechaOperacion") = p_Version;
			
	v_retencion_calculados = SELECT MONTOS."ID"
                              ,MONTOS."IdCliente"
							  ,MONTOS."IdEstadoInstrumento"
							  ,MONTOS."FechaOperacion"
							  ,MONTOS."FechaAdquisicion"
							  ,MONTOS."InteresDevengadoPesos"
							  ,MONTOS."InteresDevengadoMonedaUnidadReajuste"
							  ,MONTOS."MovimientoAdquision"
							  ,MONTOS."NemotecnicoSVS"
							  ,MONTOS."Nemotecnico"
							  ,MONTOS."IdInstrumento"
							  ,MONTOS."Cantidad"
							  ,MONTOS."Tasa"
							  ,MONTOS."Precio"
                              ,MONTOS."IdMonedaInstrumento"
                              ,MONTOS."IdMonedaEmisionInstrumento"
                              ,((MONTOS."InteresDevengadoPesos"*0.04)*(IPCVNT."Valor"/IPCCOM."Valor")) AS "RetencionPesos"
                              ,CASE 
                                 WHEN "IdMonedaInstrumento" <> 'CLP'
                                    THEN 
                                    	CASE
                                    		WHEN MONVNT."Valor" IS NULL OR MONCOM."Valor" IS NULL
                                    			THEN
                                    				((MONTOS."InteresDevengadoMonedaUnidadReajuste"*0.04)*1)
                                    			ELSE 
                                    				((MONTOS."InteresDevengadoMonedaUnidadReajuste"*0.04)*(MONVNT."Valor"/MONCOM."Valor"))
                                    	END
                                    ELSE
                                        ((MONTOS."InteresDevengadoPesos"*0.04)*(IPCVNT."Valor"/IPCCOM."Valor"))
                               END
                                AS "RetencionMonedaUnidadReajuste"
							 FROM :v_retencion MONTOS
                                    LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCCOM  ON 
															(substring(ADD_MONTHS (TO_DATE (MONTOS."FechaAdquisicion", 'YYYY-MM-DD'), -1),0,7) = IPCCOM."AgnoMes")  AND IPCCOM."IdParametroFinanciero" = 'IPC'
                                    LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" IPCVNT  ON 
															(substring(ADD_MONTHS (TO_DATE (MONTOS."FechaOperacion", 'YYYY-MM-DD'), -1),0,7) = IPCVNT."AgnoMes")  AND IPCVNT."IdParametroFinanciero" = 'IPC'
                                    LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONCOM  ON
															(substring(ADD_MONTHS (TO_DATE (MONTOS."FechaAdquisicion", 'YYYY-MM-DD'), -1),0,7) = MONCOM."AgnoMes")  AND 															((CASE
															WHEN MONTOS."IdMonedaInstrumento" <> 'USD' THEN
																MONTOS."IdMonedaInstrumento"
															ELSE
																'DO'
											 				END)  = MONCOM."IdParametroFinanciero")                                                                 
 
                                    LEFT  JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" MONVNT  ON 
															(CONCAT(substring(ADD_MONTHS (TO_DATE (MONTOS."FechaOperacion", 'YYYY-MM-DD'), -1),0,4),'-12') = MONVNT."AgnoMes")  AND 
															((CASE
															WHEN MONTOS."IdMonedaInstrumento" <> 'USD' THEN
																MONTOS."IdMonedaInstrumento"
															ELSE
																'DO'
											 				END)  = MONVNT."IdParametroFinanciero")                                                                    
							 WHERE MONTOS."ValResultado" = 'OK'
                             AND MONTOS."IdEstadoInstrumento" = 'PROCESAR';
                                  
    
        p_out_id := 0;
        UPDATE DJ1874
			SET DJ1874."RetencionPesos4porciento" = DJ."RetencionPesos",
				DJ1874."RetencionMonedaUnidadReajuste4porciento" = DJ."RetencionMonedaUnidadReajuste",
                DJ1874."RetencionMonedaUnidadReajuste" = DJ1874."RetencionMonedaUnidadReajuste" + DJ."RetencionMonedaUnidadReajuste",
                DJ1874."RetencionPesos" = DJ1874."RetencionPesos" + DJ."RetencionPesos",
				"IdEstadoInstrumento" = 'PROCESADO'
			FROM "BSCL.Normativa.BD::TRI.DatosDJ1874" AS DJ1874
			INNER JOIN :v_retencion_calculados AS DJ ON (DJ."ID" = DJ1874."ID");
	
END;