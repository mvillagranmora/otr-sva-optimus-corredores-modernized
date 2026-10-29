SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P1.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P1.ObtenerTotales"
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
    IN p_Estado NVARCHAR(3) DEFAULT NULL,

    -- Filtro EstadoSICAM.
    IN p_EstadoSICAM NVARCHAR(3) DEFAULT NULL,

    -- Filtro fuentes de datos.
    IN p_FuenteDatos NVARCHAR(42) DEFAULT NULL,
	
	-- Filtro Rut Persona
    IN p_RUTContraparte2 NVARCHAR(40) DEFAULT NULL,
	
	
	
    -- Rango fecha del Operacion.
    in p_FechaNegociacion_Desde date default null,
    in p_FechaNegociacion_Hasta date default null,
	
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,

    IN p_TipoOperacion_Desde NVARCHAR(40) DEFAULT NULL,
    
	IN p_EventoReporte_Desde NVARCHAR(40) DEFAULT NULL,

	IN p_MonedaCompradaRecibida_Desde NVARCHAR(40) DEFAULT NULL,
	
	IN p_MonedaVendidaEntregada_Desde NVARCHAR(40) DEFAULT NULL,
	IN p_MonedaPrimaria_Desde NVARCHAR(40) DEFAULT NULL,
	IN p_MonedaPrimaria_Hasta NVARCHAR(40) DEFAULT NULL,

	IN p_PlataformaNegociacion_Desde NVARCHAR(40) DEFAULT NULL,
	IN p_CodigoOperacionCambio_Desde NVARCHAR(40) DEFAULT NULL,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						NVARCHAR(100),
        "COUNT" 					BIGINT,
        "Monto"						DECIMAL(26,8)
    )
)
    LANGUAGE sqlscript
    SQL security invoker
    DEFAULT SCHEMA "BSCL"
AS
BEGIN
    ------------ Logica de Consulta ----------

if x_CodigoValidacion is null then
	    p_out_tbl = 
	    	            SELECT CASE WHEN :p_MonedaPrimaria_Desde 	is NOT NULL THEN 'Total  ' || LPAD(COALESCE (:p_MonedaPrimaria_Desde,''),3,' ' ) 
			                    ELSE 'Total' 
			                    END  as "TYPE",
	                    count(*) as "COUNT",
	                    sum(CASE "TipoOperacion" WHEN '01' THEN  "MontoMonedaCompradaRecibida" 
			                    				 WHEN '03' THEN  "MontoMonedaCompradaRecibida"
			                    				 WHEN '02' THEN  "MontoMonedaVendidaEntregada"		 
			                    				 WHEN '04' THEN  "MontoMonedaVendidaEntregada" ELSE 0 END ) as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaNegociacion_Desde IS NULL OR :p_FechaNegociacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaNegociacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaNegociacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaNegociacion_Hasta,'YYYY-MM-DD'))
                            )  
	                    and ("RUTContraparte2" like :p_RUTContraparte2 OR  :p_RUTContraparte2 is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("MonedaCompradaRecibida" = :p_MonedaCompradaRecibida_Desde OR :p_MonedaCompradaRecibida_Desde is null)
	                    AND ("MonedaVendidaEntregada" = :p_MonedaVendidaEntregada_Desde OR :p_MonedaVendidaEntregada_Desde is null)
	                    AND ("MonedaPrimaria" = :p_MonedaPrimaria_Desde OR :p_MonedaPrimaria_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)    
	                    AND ("CodigoOperacionCambio" = :p_CodigoOperacionCambio_Desde OR :p_CodigoOperacionCambio_Desde is null)
	                   UNION ALL
	                   select
	                    'Total Compras' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaCompradaRecibida") as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
	                    WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaNegociacion_Desde IS NULL OR :p_FechaNegociacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaNegociacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaNegociacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaNegociacion_Hasta,'YYYY-MM-DD'))
                            )
	                    and ("RUTContraparte2" like :p_RUTContraparte2 OR  :p_RUTContraparte2 is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("MonedaCompradaRecibida" = :p_MonedaCompradaRecibida_Desde OR :p_MonedaCompradaRecibida_Desde is null)
	                    AND ("MonedaVendidaEntregada" = :p_MonedaVendidaEntregada_Desde OR :p_MonedaVendidaEntregada_Desde is null)
	                    AND ("MonedaPrimaria" = :p_MonedaPrimaria_Desde OR :p_MonedaPrimaria_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)    
	                    AND ("CodigoOperacionCambio" = :p_CodigoOperacionCambio_Desde OR :p_CodigoOperacionCambio_Desde is null)
	                    AND "TipoOperacion"='01'
	                union all
	                 select
	                    'Total Ventas' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaVendidaEntregada") as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaNegociacion_Desde IS NULL OR :p_FechaNegociacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaNegociacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaNegociacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaNegociacion_Hasta,'YYYY-MM-DD'))
                            )  
	                    and ("RUTContraparte2" like :p_RUTContraparte2 OR  :p_RUTContraparte2 is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("MonedaCompradaRecibida" = :p_MonedaCompradaRecibida_Desde OR :p_MonedaCompradaRecibida_Desde is null)
	                    AND ("MonedaVendidaEntregada" = :p_MonedaVendidaEntregada_Desde OR :p_MonedaVendidaEntregada_Desde is null)
	                    AND ("MonedaPrimaria" = :p_MonedaPrimaria_Desde OR :p_MonedaPrimaria_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)  
	                    AND ("CodigoOperacionCambio" = :p_CodigoOperacionCambio_Desde OR :p_CodigoOperacionCambio_Desde is null)
	                    AND "TipoOperacion"='02'
	                   UNION ALL
	                   select
	                    'Total trans ingreso' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaCompradaRecibida") as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaNegociacion_Desde IS NULL OR :p_FechaNegociacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaNegociacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaNegociacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaNegociacion_Hasta,'YYYY-MM-DD'))
                            )  
	                    and ("RUTContraparte2" like :p_RUTContraparte2 OR  :p_RUTContraparte2 is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("MonedaCompradaRecibida" = :p_MonedaCompradaRecibida_Desde OR :p_MonedaCompradaRecibida_Desde is null)
	                    AND ("MonedaVendidaEntregada" = :p_MonedaVendidaEntregada_Desde OR :p_MonedaVendidaEntregada_Desde is null)
	                    AND ("MonedaPrimaria" = :p_MonedaPrimaria_Desde OR :p_MonedaPrimaria_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)  
	                    AND ("CodigoOperacionCambio" = :p_CodigoOperacionCambio_Desde OR :p_CodigoOperacionCambio_Desde is null)
	                    AND "TipoOperacion"='03' 
	                    
	                  UNION ALL
	                   select
	                    'Total trans egreso' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaVendidaEntregada") as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaNegociacion_Desde IS NULL OR :p_FechaNegociacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaNegociacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaNegociacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaNegociacion_Hasta,'YYYY-MM-DD'))
                            )  
	                    and ("RUTContraparte2" like :p_RUTContraparte2 OR  :p_RUTContraparte2 is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("MonedaCompradaRecibida" = :p_MonedaCompradaRecibida_Desde OR :p_MonedaCompradaRecibida_Desde is null)
	                    AND ("MonedaVendidaEntregada" = :p_MonedaVendidaEntregada_Desde OR :p_MonedaVendidaEntregada_Desde is null)
	                    AND ("MonedaPrimaria" = :p_MonedaPrimaria_Desde OR :p_MonedaPrimaria_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)   
	                    AND ("CodigoOperacionCambio" = :p_CodigoOperacionCambio_Desde OR :p_CodigoOperacionCambio_Desde is null)
	                    AND "TipoOperacion"='04'    
	                    
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    0 as "Monto"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                   0 as "Monto"
	                    from "BSCL.Normativa.BD.Vistas::NOR.REPO2P1.Datos"
	                    WHERE "IdDocumento" = :p_IdDocumento
	                ;
      end if;
  end;