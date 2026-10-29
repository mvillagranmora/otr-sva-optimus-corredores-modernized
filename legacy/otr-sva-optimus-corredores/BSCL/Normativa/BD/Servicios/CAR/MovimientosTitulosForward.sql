SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosTitulosForward";
GO

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.MovimientosTitulosForward" (
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
        "FEC_MVTO" TIMESTAMP  ,
        "SEC_MOVTO" DECIMAL(18,6)  ,
        "operacion" NVARCHAR(1024)  ,
        "NEMO" NVARCHAR(1024)  ,
        "PB" NVARCHAR(1024)  ,
        "CANTIDAD" DECIMAL(18,6)  ,
        "precioTir" DECIMAL(18,6)  ,
        "montoNeto" DECIMAL(18,6)  ,
        "COMISION" DECIMAL(18,6)  ,
        "DERECHOS" DECIMAL(18,6)  ,
        "GASTOS" DECIMAL(18,6)  ,
        "montoTotal" DECIMAL(18,6)  ,
        "MERCADO" NVARCHAR(1024)  ,
        "fecha_vencimiento" NVARCHAR(1024)  ,
        "NemoMP" NVARCHAR(1024)  ,
        "NemoMS" NVARCHAR(1024)  ,
        "folioOper" DECIMAL(18,6)   );

      
  INSERT INTO #temp_data
 	   SELECT
 	    CAR."IdCliente" 				as "IdCliente",
		CAR."IdPortafolio" 				as "IdPortafolio",
        TO_VARCHAR(CAR."FechaOperacion",'DD-MM-YYYY') AS "FEC_MVTO"  ,
        0 								AS "SEC_MOVTO"  ,
        CAR."NombreTipoOperacion" 		AS "operacion"  ,
        CAR."Nemotecnico" 				AS "NEMO"  ,
        'TEXTO' 						AS "PB"  ,
        CAR."Cantidad"					AS "CANTIDAD"  ,
        CAR."PrecioCompra" 				AS "precioTir"  ,
        CAR."MontoCompra"				AS "montoNeto"  ,
        0 AS "COMISION"  ,
        0 AS "DERECHOS"  ,
        0 AS "GASTOS"  ,
        0 AS "montoTotal"  ,
        'TEXTO' AS "MERCADO"  ,
        TO_VARCHAR(CAR."FechaVencimientoOperacion",'DD-MM-YYYY') AS "fecha_vencimiento",
        'TEXTO' AS "NemoMP"  ,
        'TEXTO' AS "NemoMS"  ,
        CAR."FolioComprobante" AS "folioOper"  
 	   FROM 
		:p_Datos D
		JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraSaldos" CAR ON D."IdPortafolio" = CAR."IdPortafolio"
		JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" INS ON INS."IdInstrumento" = CAR."IdInstrumento" 
		AND CAR."IdTipoOperacion" IN ('INGTIT','TRAITT','RETTIT')
		AND INS."Nemotecnico" = CAR."Nemotecnico"
		LEFT JOIN "BSCL.Custodia.BD.Vistas::SIM.ConsultarCarteraSimultaneaVigentes" SIM ON SIM."IdCartera" = CAR."IdCartera" AND SIM."IdEstado" = 'VIG'
		WHERE CAR."IdEstado" = 'VIG';
    SELECT * FROM #temp_data;
    DROP TABLE #temp_data;
END;
