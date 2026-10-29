SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO3P1.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO3P1.ObtenerTotales"
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
    IN p_RUTContraparte NVARCHAR(40) DEFAULT NULL,
	
	
	
    -- Rango fecha del Operacion.
    in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,
	
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,

    IN p_TipoOperacion_Desde NVARCHAR(40) DEFAULT NULL,
    
	IN p_EventoReporte_Desde NVARCHAR(40) DEFAULT NULL,
	IN p_PlataformaNegociacion_Desde NVARCHAR(40) DEFAULT NULL,

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
	    p_out_tbl =  SELECT  'Total USD ' as "TYPE",
	                    count(*) as "COUNT",
   	                    sum(CASE "TipoOperacion" WHEN '1' THEN  "MontoMonedaComprada" 
			                    				 WHEN '2' THEN  "MontoMonedaVendida"
			                    				 ELSE 0 END ) as "Monto"
                        from "BSCL.Normativa.BD::NOR.DatosREPO3P1"
	                    WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaOperacion_Desde IS NULL OR :p_FechaOperacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD'))
                            )
	                    and ("RUTContraparte" like :p_RUTContraparte OR  :p_RUTContraparte is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
                        AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)    
	                union all
             			select
	                    'Total Compras' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaComprada") as "Monto"
                        from "BSCL.Normativa.BD::NOR.DatosREPO3P1"
	                    WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaOperacion_Desde IS NULL OR :p_FechaOperacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD'))
                            )
	                    and ("RUTContraparte" like :p_RUTContraparte OR  :p_RUTContraparte is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)
	                    AND "TipoOperacion"='1'
	               union all
             			select
	                    'Total Ventas' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("MontoMonedaVendida") as "Monto"
                        from "BSCL.Normativa.BD::NOR.DatosREPO3P1"
	                    WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_FechaOperacion_Desde IS NULL OR :p_FechaOperacion_Hasta IS NULL)
                                OR (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD'))
                            )
	                    and ("RUTContraparte" like :p_RUTContraparte OR  :p_RUTContraparte is null)
	                    AND ("TipoOperacion" = :p_TipoOperacion_Desde OR :p_TipoOperacion_Desde is null)
	                    AND ("EventoReporte" = :p_EventoReporte_Desde OR :p_EventoReporte_Desde is null)
	                    AND ("PlataformaNegociacion" = :p_PlataformaNegociacion_Desde OR :p_PlataformaNegociacion_Desde is null)
	                    AND "TipoOperacion"='2'    
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    --0 as "Cantidad",
	                    0 as "Monto"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    0 as "Monto"
	                    from "BSCL.Normativa.BD::NOR.DatosREPO3P1"
	                   
	                ;
      end if;
  end;