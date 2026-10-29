SET SCHEMA "BSCL";
GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.FlujoPatrimonial";
GO
CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CAR.FlujoPatrimonial" (
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
	
DECLARE v_count INT;

	v_Flujo=SELECT 
		   D."IdCliente",
		   D."IdPortafolio",
		   CAST("FechaLiquidacion" as TIMESTAMP) as "fecha",
		   SUM(CASE "IdConcepto" WHEN 'RTROCI' THEN "Monto" ELSE 0 END)  as "retirosClp",
		   SUM(CASE "IdConcepto" WHEN 'APORCI' THEN "Monto" ELSE 0 END) as "abonosClp",
		   0 AS "saldoInicial"  ,
		   0 AS "saldoFinal"  ,
		   'TEXTO' AS "item"  ,
		   0 AS "cargo"  ,
		   0 AS "monto"  ,
		   0 AS "saldoFinalMov"  ,
		   0 AS "saldoInicialMov"  ,
		   0 AS "totalRV"  ,
		   0 AS "totalRf"  ,
		   0 AS "totalDER"  ,
		   0 AS "totalFm"  ,
		   0 AS "totalOP"  ,
		   0 AS "totalCAja"   
	   FROM :p_Datos D
	   JOIN "BSCL.Tesoreria.BD::TES.Comprobante" COM ON D."IdPortafolio" = COM."IdPortafolioTitularPago" 
	   WHERE "IdEstado" = 'LIQ' 
	   AND "IdConcepto" IN ('APORCI','RTROCI')
	   AND "FechaLiquidacion" IS NOT NULL
	   AND "FechaLiquidacion" BETWEEN :p_FechaDesde AND :p_FechaHasta
	   GROUP BY D."IdCliente", D."IdPortafolio", COM."FechaLiquidacion"
	   ORDER BY COM."FechaLiquidacion" desc;
	   
   
   SELECT COUNT(*) INTO v_count FROM :v_Flujo;
   IF (v_count =0) THEN
   v_Flujo = 
	   SELECT 
		   0 AS"IdCliente",
		   0 AS"IdPortafolio",
		   '0001-01-01' as "fecha",
		   0 AS "retirosClp",
		   0 AS "abonosClp",
		   0 AS "saldoInicial"  ,
		   0 AS "saldoFinal"  ,
		   '' AS "item"  ,
		   0 AS "cargo"  ,
		   0 AS "monto"  ,
		   0 AS "saldoFinalMov"  ,
		   0 AS "saldoInicialMov"  ,
		   0 AS "totalRV"  ,
		   0 AS "totalRf"  ,
		   0 AS "totalDER"  ,
		   0 AS "totalFm"  ,
		   0 AS "totalOP"  ,
		   0 AS "totalCAja"   
	   FROM dummy;
   END IF; 
   
   SELECT * FROM :v_Flujo;
   
   
   
   
END;
GO