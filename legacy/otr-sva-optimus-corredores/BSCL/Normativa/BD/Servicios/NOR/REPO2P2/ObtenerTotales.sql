SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P2.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::NOR.REPO2P2.ObtenerTotales"
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
    IN p_RUTEntidadMCF NVARCHAR(10) DEFAULT NULL,
	
	
	
    -- Rango fecha del Operacion.
    in p_Fecha_Desde date default null,
    in p_Fecha_Hasta date default null,
	
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,

    

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						NVARCHAR(100),
        "COUNT" 					BIGINT,
        "SaldoCuentaCambioConversion"	DECIMAL(26,8),
        "SaldoMonedaOriginal"	DECIMAL(26,8)
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
	                    count(*) as "COUNT",
	                    sum("SaldoCuentaCambioConversion") as "SaldoCuentaCambioConversion",
   	                    sum("SaldoMonedaOriginal")  as "SaldoMonedaOriginal"
	                    from "BSCL.Normativa.BD::NOR.DatosREPO2P2"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_Fecha_Desde IS NULL OR :p_Fecha_Hasta IS NULL)
                                OR (TO_NVARCHAR("Fecha",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_Fecha_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_Fecha_Hasta,'YYYY-MM-DD'))
                            )

	                union all
	                 select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoCuentaCambioConversion") as "SaldoCuentaCambioConversion",
	                    sum("SaldoMonedaOriginal") as "SaldoMonedaOriginal"
	                    from "BSCL.Normativa.BD::NOR.DatosREPO2P2"
                        WHERE "IdDocumento" = :p_IdDocumento
                        AND (
                                (:p_Fecha_Desde IS NULL OR :p_Fecha_Hasta IS NULL)
                                OR (TO_NVARCHAR("Fecha",'YYYY-MM-DD') 
                                        BETWEEN TO_NVARCHAR(:p_Fecha_Desde,'YYYY-MM-DD') 
                                            AND TO_NVARCHAR(:p_Fecha_Hasta,'YYYY-MM-DD'))
                            )  
	                    and ("RUTEntidadMCF" like :p_RUTEntidadMCF OR  :p_RUTEntidadMCF is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    --0 as "Cantidad",
	                    0 as "SaldoCuentaCambioConversion",
	                    0 as "SaldoMonedaOriginal"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                   -- sum("Cantidad") as "Cantidad",
	                    sum("SaldoCuentaCambioConversion") as "SaldoCuentaCambioConversion",
	                    sum("SaldoMonedaOriginal") as "SaldoMonedaOriginal"
	                    from "BSCL.Normativa.BD::NOR.DatosREPO2P2"
	                   
	                ;
      end if;
  end;