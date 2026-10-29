SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1958.ObtenerTotales";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::TRI.DJ1958.ObtenerTotales"
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
    IN p_Tax_ID NVARCHAR(40) DEFAULT NULL,
	
	
	
    -- Rango fecha del Operacion.
    in p_PeriodoReporte_Desde date default null,
    in p_PeriodoReporte_Hasta date default null,
	
		
    IN x_CodigoValidacion NVARCHAR(40) DEFAULT NULL,

    

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    OUT p_out_tbl TABLE (
        "TYPE" 						NVARCHAR(100),
        "COUNT" 					BIGINT,
        "SaldoActualizado"	DECIMAL(26,8),
        "MontoPagoActualizado"	DECIMAL(26,8)
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
	                    sum("SaldoActualizado") as "SaldoActualizado",
   	                    sum("MontoPagoActualizado") as "MontoPagoActualizado"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1958"
	                    where (TO_NVARCHAR("PeriodoReporte",'YYYY-MM-DD') between TO_NVARCHAR(:p_PeriodoReporte_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_PeriodoReporte_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_PeriodoReporte_Hasta is null)  

	                union all
	                 select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoActualizado") as "SaldoActualizado",
	                    sum("MontoPagoActualizado") as "MontoPagoActualizado"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1958"
	                    where (TO_NVARCHAR("PeriodoReporte",'YYYY-MM-DD') between TO_NVARCHAR(:p_PeriodoReporte_Desde,'YYYY-MM-DD') and TO_NVARCHAR(:p_PeriodoReporte_Hasta,'YYYY-MM-DD')) OR ("IdDocumento" = :p_IdDocumento and p_PeriodoReporte_Hasta is null)  
	                    and ("TaxID" like :p_Tax_ID OR  :p_Tax_ID is null)
	                ;
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    --0 as "Cantidad",
	                    0 as "SaldoActualizado",
	                    0 as "MontoPagoActualizado"
	                    from DUMMY
	                union all
	                   select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                   -- sum("Cantidad") as "Cantidad",
	                    sum("SaldoActualizado") as "SaldoActualizado",
	                    sum("MontoPagoActualizado") as "MontoPagoActualizado"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1958"
	                   
	                ;
      end if;
  end;