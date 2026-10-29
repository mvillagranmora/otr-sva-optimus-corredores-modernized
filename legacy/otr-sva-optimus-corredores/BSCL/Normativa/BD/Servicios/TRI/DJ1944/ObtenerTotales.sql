SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1944.ObtenerTotales"
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
	
	-- Filtro Rut Persona
    IN p_RutCliente NVARCHAR(40) DEFAULT NULL,
	
	
	
    -- Rango fecha del Operacion.
    in p_FechaOperacion_Desde date default null,
    in p_FechaOperacion_Hasta date default null,
	
    /*
    -- Filtro Identificador Extranjero
    IN p_DatosPersonaIndentificadorExtranjero NVARCHAR(40) DEFAULT NULL,
    
    -- Rango del ID cliente.
    IN p_IdCliente_Desde BIGINT DEFAULT NULL,
    IN p_IdCliente_Hasta BIGINT DEFAULT NULL,

    -- Rango del ID portafolio.
    IN p_IdPortafolio_Desde BIGINT DEFAULT NULL,
    IN p_IdPortafolio_Hasta BIGINT DEFAULT NULL,
			
	-- Rango Monto Compras
	IN p_MontoCompras_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoCompras_Hasta DECIMAL(26,8) DEFAULT NULL,
	
	-- Rango Monto Ventas
	IN p_MontoVentas_Desde DECIMAL(26,8) DEFAULT NULL,
	IN p_MontoVentas_Hasta DECIMAL(26,8) DEFAULT NULL,*/
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,
    
    --Filtro Moneda
    --IN p_CodigoMonedaTransada NVARCHAR(10) DEFAULT NULL,
    

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						NVARCHAR(100),
        "COUNT" 					BIGINT,
        "SalAhorroNetoEjerNEGT1"	DECIMAL(26,8)--,
        --"MontoVentas" 				DECIMAL(26,8)
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
	                   -- 0 as "Cantidad",
	                    --0 as "MontoNocional"
	                    sum("SalAhorroNetoEjerNEGT1") as "SalAhorroNetoEjerNEGT1"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1944"
	                    where (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') between TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_FechaOperacion_Hasta is null)  

	                union all
	                 select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SalAhorroNetoEjerNEGT1") as "SalAhorroNetoEjerNEGT1"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1944"
	                    where (TO_NVARCHAR("FechaOperacion",'YYYY-MM-DD') between TO_NVARCHAR(:p_FechaOperacion_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_FechaOperacion_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_FechaOperacion_Hasta is null)  
	                    and ("RutCliente" like :p_RutCliente OR  :p_RutCliente is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    --0 as "Cantidad",
	                    0 as "SalAhorroNetoEjerNEGT1"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                   -- sum("Cantidad") as "Cantidad",
	                    sum("SalAhorroNetoEjerNEGT1") as "SalAhorroNetoEjerNEGT1"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1944"
	                   
	                ;
      end if;
  end;