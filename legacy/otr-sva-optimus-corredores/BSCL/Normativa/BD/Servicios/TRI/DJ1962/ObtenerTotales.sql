SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1962.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1962.ObtenerTotales"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    IN p_IdEmpresa BIGINT,

    -- ID usuario del login.
    IN p_IdUsuario BIGINT,

    -- ID modulo invocador.
    IN p_IdModulo NVARCHAR(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    IN p_IdDocumento BIGINT,

    -- Filtro VoBo del usuario.
    IN p_VboUsuario NVARCHAR(3) DEFAULT NULL,

    -- Filtro observaciones.
    IN p_Observaciones NVARCHAR(252) DEFAULT NULL,

    -- Filtro fuentes de datos.
    IN p_FuenteDatos NVARCHAR(42) DEFAULT NULL,
	
	
	-- Filtro Rut Cliente

    IN p_RutCliente NVARCHAR(40) DEFAULT NULL,
	
    -- Rango fecha del Operacion.
    in p_PeriodoReporte_Desde date default null,
    in p_PeriodoReporte_Hasta date default null,
	
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    
    IN p_Fondo NVARCHAR(40) DEFAULT NULL,

    -- RANGO MONTOS

    IN p_DetBenNetoPerPOS_C6_Desde                                      DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoPerNEG_C7_Desde                                      DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoUtilPer_C8_Desde                                     DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoIntPer_C9_Desde                                      DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoDivPer_C10_Desde                                     DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoGanCapPer_C11_Desde                                  DECIMAL(26,8) DEFAULT NULL,
    IN p_DetBenNetoPerGastDev_C12_Desde                                 DECIMAL(26,8) DEFAULT NULL,
    IN p_MontoNominalAportesActualizado_Desde							DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoNominalAportesActualizado_Hasta							DECIMAL(26,8) DEFAULT NULL,
    IN p_CantidadCuotasParticipe_Desde								 	DECIMAL(26,8) DEFAULT NULL,
    IN p_ValorCuotaPesos_Desde										 	DECIMAL(26,8) DEFAULT NULL,
    IN p_PorcentajeParticipacion31Dic_Desde								DECIMAL(26,8) DEFAULT NULL,
    
    

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						        NVARCHAR(100),
        "COUNT" 					        BIGINT,
        "DetBenNetoPerPOS_C6"	            DECIMAL(26,8),
        "DetBenNetoPerNEG_C7"	            DECIMAL(26,8),
        "DetBenNetoUtilPer_C8"              DECIMAL(26,8),
        "DetBenNetoIntPer_C9"               DECIMAL(26,8),
        "DetBenNetoDivPer_C10"              DECIMAL(26,8),
        "DetBenNetoGanCapPer_C11"           DECIMAL(26,8),
        "DetBenNetoPerGastDev_C12"          DECIMAL(26,8),
        "MontoNominalAportesActualizado"    DECIMAL(26,8),
        "CantidadCuotasParticipe"			DECIMAL(26,8),
        "ValorCuotaPesos"                   DECIMAL(26,8),
        "PorcentajeParticipacion31Dic"		DECIMAL(26,8)
    )
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Logica de Consulta ----------

if x_CodigoValidacion is null then
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(1) as "COUNT",
	                    SUM("DetBenNetoPerPOS_C6")	            AS "DetBenNetoPerPOS_C6",
                        SUM("DetBenNetoPerNEG_C7")	            AS "DetBenNetoPerNEG_C7",
                        SUM("DetBenNetoUtilPer_C8")             AS "DetBenNetoUtilPer_C8",
                        SUM("DetBenNetoIntPer_C9")              AS "DetBenNetoIntPer_C9",
                        SUM("DetBenNetoDivPer_C10")             AS "DetBenNetoDivPer_C10",
                        SUM("DetBenNetoGanCapPer_C11")          AS "DetBenNetoGanCapPer_C11",
                        SUM("DetBenNetoPerGastDev_C12")         AS "DetBenNetoPerGastDev_C12",
                        SUM("MontoNominalAportesActualizado")   AS "MontoNominalAportesActualizado",
                        SUM("CantidadCuotasParticipe")          AS "CantidadCuotasParticipe",
                        SUM("ValorCuotaPesos")                  AS "ValorCuotaPesos",
                        SUM("PorcentajeParticipacion31Dic")     AS "PorcentajeParticipacion31Dic"

                        FROM "BSCL.Normativa.BD::TRI.DatosDJ1962"

	                    WHERE "IdDocumento" = p_IdDocumento

	                union all
	                 select
	                    'FILTRO' as "TYPE",
	                    count(1) as "COUNT",
	                    SUM("DetBenNetoPerPOS_C6")	            AS "DetBenNetoPerPOS_C6",
                        SUM("DetBenNetoPerNEG_C7")	            AS "DetBenNetoPerNEG_C7",
                        SUM("DetBenNetoUtilPer_C8")             AS "DetBenNetoUtilPer_C8",
                        SUM("DetBenNetoIntPer_C9")              AS "DetBenNetoIntPer_C9",
                        SUM("DetBenNetoDivPer_C10")             AS "DetBenNetoDivPer_C10",
                        SUM("DetBenNetoGanCapPer_C11")          AS "DetBenNetoGanCapPer_C11",
                        SUM("DetBenNetoPerGastDev_C12")         AS "DetBenNetoPerGastDev_C12",
                        SUM("MontoNominalAportesActualizado")   AS "MontoNominalAportesActualizado",
                        SUM("CantidadCuotasParticipe")          AS "CantidadCuotasParticipe",
                        SUM("ValorCuotaPesos")                  AS "ValorCuotaPesos",
                        SUM("PorcentajeParticipacion31Dic")     AS "PorcentajeParticipacion31Dic"

                        FROM "BSCL.Normativa.BD::TRI.DatosDJ1962"

	                    WHERE "IdDocumento" = p_IdDocumento
                        AND ("VboUsuario" LIKE p_VboUsuario or p_VboUsuario IS NULL)
	                    AND ("Observaciones" LIKE p_Observaciones or p_Observaciones IS NULL)
	                    AND ("FuenteDatos" LIKE p_FuenteDatos or p_FuenteDatos IS NULL)
	                    AND (trim("RutCliente") like trim(p_RutCliente) or p_RutCliente is null)
                        AND ("DetBenNetoPerPOS_C6"								    >= p_DetBenNetoPerPOS_C6_Desde OR p_DetBenNetoPerPOS_C6_Desde IS NULL)
						AND ("DetBenNetoPerNEG_C7"								    >= p_DetBenNetoPerNEG_C7_Desde OR p_DetBenNetoPerNEG_C7_Desde IS NULL)
                        AND ("DetBenNetoUtilPer_C8"								    >= p_DetBenNetoUtilPer_C8_Desde OR p_DetBenNetoUtilPer_C8_Desde IS NULL)
                        AND ("DetBenNetoIntPer_C9"    							    >= p_DetBenNetoIntPer_C9_Desde OR p_DetBenNetoIntPer_C9_Desde IS NULL)     
                        AND ("DetBenNetoDivPer_C10"     							>= p_DetBenNetoDivPer_C10_Desde OR p_DetBenNetoDivPer_C10_Desde IS NULL)         
                        AND ("DetBenNetoGanCapPer_C11"   							>= p_DetBenNetoGanCapPer_C11_Desde OR p_DetBenNetoGanCapPer_C11_Desde IS NULL)        
                        AND ("DetBenNetoPerGastDev_C12"  							>= p_DetBenNetoPerGastDev_C12_Desde OR p_DetBenNetoPerGastDev_C12_Desde IS NULL)		
                        AND ("MontoNominalAportesActualizado"						>= p_MontoNominalAportesActualizado_Desde OR p_MontoNominalAportesActualizado_Desde IS NULL)
 					    AND ("MontoNominalAportesActualizado" 						<= p_MontoNominalAportesActualizado_Desde OR p_MontoNominalAportesActualizado_Desde IS NULL)
                        AND ("CantidadCuotasParticipe"								>= p_CantidadCuotasParticipe_Desde OR p_CantidadCuotasParticipe_Desde IS NULL)
						AND ("ValorCuotaPesos"										>= p_ValorCuotaPesos_Desde OR p_ValorCuotaPesos_Desde IS NULL)
                        AND ("PorcentajeParticipacion31Dic"						    >= p_PorcentajeParticipacion31Dic_Desde OR p_PorcentajeParticipacion31Dic_Desde IS NULL)
                        AND (trim("Fondo")												LIKE trim(p_Fondo) OR p_Fondo IS NULL )												
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(1) as "COUNT",
	                    SUM("DetBenNetoPerPOS_C6")	            AS "DetBenNetoPerPOS_C6",
                        SUM("DetBenNetoPerNEG_C7")	            AS "DetBenNetoPerNEG_C7",
                        SUM("DetBenNetoUtilPer_C8")             AS "DetBenNetoUtilPer_C8",
                        SUM("DetBenNetoIntPer_C9")              AS "DetBenNetoIntPer_C9",
                        SUM("DetBenNetoDivPer_C10")             AS "DetBenNetoDivPer_C10",
                        SUM("DetBenNetoGanCapPer_C11")          AS "DetBenNetoGanCapPer_C11",
                        SUM("DetBenNetoPerGastDev_C12")         AS "DetBenNetoPerGastDev_C12",
                        SUM("MontoNominalAportesActualizado")   AS "MontoNominalAportesActualizado",
                        SUM("CantidadCuotasParticipe")          AS "CantidadCuotasParticipe",
                        SUM("ValorCuotaPesos")                  AS "ValorCuotaPesos",
                        SUM("PorcentajeParticipacion31Dic")     AS "PorcentajeParticipacion31Dic"

                        FROM "BSCL.Normativa.BD::TRI.DatosDJ1962"

	                    WHERE "IdDocumento" = p_IdDocumento

	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(1) as "COUNT",
	                    SUM("DetBenNetoPerPOS_C6")	            AS "DetBenNetoPerPOS_C6",
                        SUM("DetBenNetoPerNEG_C7")	            AS "DetBenNetoPerNEG_C7",
                        SUM("DetBenNetoUtilPer_C8")             AS "DetBenNetoUtilPer_C8",
                        SUM("DetBenNetoIntPer_C9")              AS "DetBenNetoIntPer_C9",
                        SUM("DetBenNetoDivPer_C10")             AS "DetBenNetoDivPer_C10",
                        SUM("DetBenNetoGanCapPer_C11")          AS "DetBenNetoGanCapPer_C11",
                        SUM("DetBenNetoPerGastDev_C12")         AS "DetBenNetoPerGastDev_C12",
                        SUM("MontoNominalAportesActualizado")   AS "MontoNominalAportesActualizado",
                        SUM("CantidadCuotasParticipe")          AS "CantidadCuotasParticipe",
                        SUM("ValorCuotaPesos")                  AS "ValorCuotaPesos",
                        SUM("PorcentajeParticipacion31Dic")     AS "PorcentajeParticipacion31Dic"

	                    from "BSCL.Normativa.BD::TRI.DatosDJ1962"
                        WHERE "IdDocumento" = p_IdDocumento
                        AND "ID" IN (
	                        SELECT a."IdDetalleDocumento"
	                        FROM "BSCL.Normativa.BD::TRI.DetalleValidacion" a
	                        LEFT JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b
	                        ON a."IdResumenValidacion"=b."ID"
	                        WHERE b."CodigoValidacion"=x_CodigoValidacion
	                    )
	                   
	                ;
      end if;
  end;   
	                
	              