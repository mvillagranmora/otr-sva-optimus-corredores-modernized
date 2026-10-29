SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraForward";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.CarteraForward" (
    IN p_IdModulo NVARCHAR(40),
    IN p_IdUsuario BIGINT,
    IN p_IdEmpresa BIGINT,
    IN p_FechaDesde DATE,
    IN p_FechaHasta DATE,
    IN p_Datos TABLE ("IdCliente" BIGINT, "IdPortafolio" BIGINT)
)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"
--READS SQL DATA
AS
BEGIN
    CREATE LOCAL TEMPORARY TABLE #temp_data (
        "IdCliente" BIGINT,
        "IdPortafolio" BIGINT,
        "tipo" NVARCHAR(1024)  ,
        "plazo" BIGINT  ,
        "emision" NVARCHAR(1024)  ,
        "vencimiento" NVARCHAR(1024)  ,
        "saldoPlazo" BIGINT  ,
        "precioCierre" DECIMAL(18,6)  ,
        "Fecha" NVARCHAR(1024)  ,
        "folioOper" BIGINT  ,
        "Operacion" NVARCHAR(1024)  ,
        "monto" DECIMAL(18,6)  ,
        "moneda" NVARCHAR(1024)  ,
        "montoPeso" DECIMAL(18,6)  ,
        "valorRazonable" DECIMAL(18,6)  ,
        "liquidacion" NVARCHAR(1024)  ,
        "OperacionMoneda" NVARCHAR(1024)  ,
        "WarningFechavalorRazonable" NVARCHAR(1024)  ,
        "FechavalorRazonable" NVARCHAR(1024)   );

    INSERT INTO #temp_data
    SELECT
        CAR."IdCliente"					 	as "IdCliente",
        CAR."IdPortafolio"					as "IdPortafolio",
        '' 									AS "tipo"  ,
        CAR."PlazoEnDiasOperacion" 			AS "plazo"  ,
        CAR."FechaEmisionInstrumento"		AS "emision"  ,
	 	CAR."FechaVencimientoInstrumento" 	AS "vencimiento"  ,
        0 									AS "saldoPlazo"  ,
        0 									AS "precioCierre"  ,
        CAR."FechaOperacion" 				AS "Fecha"  ,
        CAR."FolioComprobante" 				AS "folioOper"  ,
        CAR."NombreTipoOperacion" 			AS "Operacion"  ,
        CAR."MontoCompra" 					AS "monto"  ,
        CAR."Moneda" 						AS "moneda"  ,
        car."MontoMonedaPrincipal"			AS "montoPeso"  ,
        0 									AS "valorRazonable"  ,
        '' 									AS "liquidacion"  ,
        '' 									AS "OperacionMoneda"  ,
        CAR."FechaVencimientoOperacion" 	AS "WarningFechavalorRazonable"  ,
        CAR."FechaOperacion" 				AS "FechavalorRazonable"   
    FROM
     :p_Datos D 
     JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR ON D."IdPortafolio" = CAR."IdPortafolio"
     WHERE 
     CAR."IdClasificacionTipoInstrumento" = 'FW'
     AND CAR."IdEstado" = 'VIG'
     AND CAR."FechaOperacion" BETWEEN p_FechaDesde AND p_FechaHasta
    ;
    
    
   

    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
