SET SCHEMA "BSCL";
GO

DROP PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCartolaMovimientos";
GO

CREATE PROCEDURE "BSCL.Custodia.BD.Servicios::ConsultarCartolaMovimientos" ( 
	-- VARIABLES DE ENTRADA OBLIGATORIAS
	IN p_IdEmpresa                        BIGINT,
	
	-- VARIABLES DEL PROCESO
	IN p_IdCliente                        BIGINT		DEFAULT 0,
	IN p_IdPortafolio                     BIGINT		DEFAULT 0,
	IN p_FolioCartera                     BIGINT		DEFAULT 0,
	IN p_TipoOperacion                    NVARCHAR(10)	DEFAULT '',
	IN p_IdClasificacionTipoInstrumento   NVARCHAR(3)	DEFAULT '',
	IN p_IdTipoInstrumento                NVARCHAR(3)	DEFAULT '',
	IN p_IdInstrumento                    BIGINT		DEFAULT 0,
	IN p_IdMoneda                         NVARCHAR(3)	DEFAULT '',
	IN p_IdConcepto                       NVARCHAR(6)	DEFAULT '',
	IN p_IdBoveda                         NVARCHAR(3)	DEFAULT '',
	IN p_FechaInicio                      DATE,
	IN p_FechaTermino                     DATE,
	IN p_IdProductoAgrupador              NUMBER		DEFAULT NULL
	
	-- VARIABLES DE SALIDA OBLIGATORIAS
	-- p_out_Tabla=BSCL.Custodia.BD::GBL.ConsultarCartolaMovimientos_Salida_Tabla
)

LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA "BSCL"

AS
BEGIN
	
	DECLARE v_Ingresos TABLE ("FolioCartera" BIGINT, "IdCartera" BIGINT, "NombreProductoAgrupador" NVARCHAR(200));   
	
	If p_TipoOperacion = '' OR p_TipoOperacion = 'INGRESO' THEN
	
		v_Ingresos =
		SELECT
			"FolioCartera"
		   ,MIN("IdCartera") AS "IdCartera"
		   ,ProductoAgrupador."NombreProductoAgrupador"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS CAR
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = car."IdPortafolio" AND Portafolio."IdCliente" = car."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		WHERE
			car."IdEmpresa" = p_IdEmpresa 
		AND car."IdTipoInstrumento" not in ('PRT','EMP', 'SIM')
		AND car."IdEstado" <> 'ANU'
		AND car."IdCliente" = CASE WHEN p_IdCliente > 0 THEN p_IdCliente ELSE car."IdCliente" END
		AND car."IdPortafolio" = CASE WHEN p_IdPortafolio > 0 THEN p_IdPortafolio ELSE car."IdPortafolio" END
		AND car."FolioCartera" = CASE WHEN p_FolioCartera > 0 THEN p_FolioCartera ELSE car."FolioCartera" END
		AND car."IdClasificacionTipoInstrumento" = CASE WHEN p_IdClasificacionTipoInstrumento <> '' THEN p_IdClasificacionTipoInstrumento ELSE car."IdClasificacionTipoInstrumento" END
		AND car."IdTipoInstrumento" = CASE WHEN p_IdTipoInstrumento <> '' THEN p_IdTipoInstrumento ELSE car."IdTipoInstrumento" END
		AND car."IdInstrumento" = CASE WHEN p_IdInstrumento > 0 THEN p_IdInstrumento ELSE car."IdInstrumento" END
		AND car."IdMonedaInstrumento" = CASE WHEN p_IdMoneda <> '' THEN p_IdMoneda ELSE car."IdMonedaInstrumento" END
		AND car."IdConcepto" = CASE WHEN p_IdConcepto <> '' THEN p_IdConcepto ELSE car."IdConcepto" END
		AND car."IdBoveda" = CASE WHEN p_IdBoveda <> '' THEN p_IdBoveda ELSE car."IdBoveda" END
		AND "INGFechaOperacion" >= p_FechaInicio
		AND "INGFechaOperacion" <= p_FechaTermino
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador)
		GROUP BY
			"FolioCartera"
		   ,ProductoAgrupador."NombreProductoAgrupador";
		
		
		v_IngresosPorAnul =
		SELECT
			"FolioCartera"
		   ,"IdCartera"
		   ,ProductoAgrupador."NombreProductoAgrupador"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS car
		INNER JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as fac ON fac."IdTipoComprobante" = car."EGRIdTipoComprobante" AND fac."IdComprobante" = car."EGRFolioComprobante" AND fac."FechaComprobante" = car."EGRFechaComprobante"
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet ON CmbteDet."IdComprobante" = fac."IdComprobante" AND CmbteDet."IdInstrumento" = car."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" as Concepto ON car."EGRIdConcepto" = Concepto."IdConcepto"
		INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOperFac ON TipOperFac."IdTipoOperacion" = CmbteDet."IdTipoOperacion" 
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = car."IdPortafolio" AND Portafolio."IdCliente" = car."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		WHERE
			car."IdEmpresa" = p_IdEmpresa 
		AND car."IdTipoInstrumento" not in ('PRT','EMP', 'SIM')
--		AND car."IdEstado" <> 'ANU'
		AND car."IdCliente" = CASE WHEN p_IdCliente > 0 THEN p_IdCliente ELSE car."IdCliente" END
		AND car."IdPortafolio" = CASE WHEN p_IdPortafolio > 0 THEN p_IdPortafolio ELSE car."IdPortafolio" END
		AND car."FolioCartera" = CASE WHEN p_FolioCartera > 0 THEN p_FolioCartera ELSE car."FolioCartera" END
		AND car."IdClasificacionTipoInstrumento" = CASE WHEN p_IdClasificacionTipoInstrumento <> '' THEN p_IdClasificacionTipoInstrumento ELSE car."IdClasificacionTipoInstrumento" END
		AND car."IdTipoInstrumento" = CASE WHEN p_IdTipoInstrumento <> '' THEN p_IdTipoInstrumento ELSE car."IdTipoInstrumento" END
		AND car."IdInstrumento" = CASE WHEN p_IdInstrumento > 0 THEN p_IdInstrumento ELSE car."IdInstrumento" END
		AND car."IdMonedaInstrumento" = CASE WHEN p_IdMoneda <> '' THEN p_IdMoneda ELSE car."IdMonedaInstrumento" END
		AND car."EGRIdConcepto" = CASE WHEN p_IdConcepto <> '' THEN p_IdConcepto ELSE car."EGRIdConcepto" END
		AND car."IdBoveda" = CASE WHEN p_IdBoveda <> '' THEN p_IdBoveda ELSE car."IdBoveda" END
		AND TipOperFac."AccionCustodia" = 'INGRESO'
		AND car."EGRFechaOperacion" >= p_FechaInicio
		AND car."EGRFechaOperacion" <= p_FechaTermino
		AND car."EGRIdTipoComprobante" IN ('NCE','NCR','NEO') --solo busque en facturación pa datos de NC
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);


		mi_vtbl =
		SELECT
			ing."IdCartera" AS "IdCartera"
		   ,car."FechaIngresoRegistro" AS "FechaRegistro"
		   ,car."INGFechaOperacion" AS "FechaOperacion"
		   ,'INGRESO' AS "TipoOperacion"
		   ,car."IdCliente" AS "IdCliente"
		   ,car."IdPortafolio" AS "IdPortafolio"
		   ,Port."CodigoPortafolio"
		   ,cli."NombreCompleto" AS "NombreCliente"
		   ,cli."DocumentoIdentidad" AS "DocumentoIdentidad"
		   ,car."IdConcepto" AS "IdConcepto"
		   ,Concepto."Nombre" AS "NombreConcepto"
		   ,car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
		   ,car."IdTipoInstrumento" AS "IdTipoInstrumento"
		   ,car."IdInstrumento" AS "IdInstrumento"
		   ,car."ISIN"
		   ,CASE
		   		WHEN car."IdTipoInstrumento" = 'SIM' OR car."IdTipoInstrumento" = 'PAC' THEN
				(
					SELECT "Nemotecnico" 
					FROM
					(
						SELECT "Nemotecnico", row_number() over(order by (SELECT 1 FROM dummy) ) AS "NroFila" 
                    	FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub
                    	WHERE car."FolioCartera" = miSub."FolioCarteraRelacionada"
                    ) AS Suby
                    WHERE Suby."NroFila" = 1
				)	ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,Case 
		      When "IdTipoOperacion" in ('COMSIM', 'VENSIM','COMREP','VENREP') Then Ins."IdEmisor"
		    Else car."IdEmisor" 
		    End AS "IdEmisor"
		   ,car."FechaVencimientoInstrumento" AS "FechaVencimientoInstr"
		   ,car."INGFechaLiquidacion" AS "FechaLiquidacion"
		   ,car."IdTipoReajuste" AS "IdTipoReajuste"
		   ,car."NemoSVS" AS "NemoSVS"
		   ,CASE WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN IFNULL(car."IdTipoReajuste", '') ELSE car."IdMonedaInstrumento" END AS "IdMonedaInstr"
		   --------------------------------------------------------
		   ,CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
		   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
		   ,CCPI."IdTipoCustodia"
		   ,CCPI."NombreTipoCustodia"
		   ---------------------------------------------------------
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "Cantidad"
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."INGValorRescate" ELSE car."INGCantidad" END AS "CantidadNegativa"
		   ,car."PrecioCompra" AS "Precio"
		   ,car."TasaCompra" AS "Tasa"
		   ,car."MontoCompra" AS "Monto"
		   ,car."ValorRescate" AS "ValorRescate"
		   --,car."Cantidad" AS "Saldo"		   
		   ,0 AS "Saldo"
		   ,car."FolioCartera" AS "FolioCartera"
		   ,car."FolioComprobante"
		   ,car."FechaComprobante"
		   ,car."IdTipoComprobante"
		   ,ing."NombreProductoAgrupador" AS "NombreProductoAgrupador"
		   ,car."EGRSpread"
           ,car."EGRSpreadMercado"
		FROM :v_Ingresos AS ing
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS car on car."IdCartera" = ing."IdCartera"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON car."IdInstrumento" = Ins."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Port ON Port."IdPortafolio" = car."IdPortafolio" 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli ON Cli."IdCliente" = Port."IdCliente" 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON car."IdConcepto" = Concepto."IdConcepto" 
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
		UNION ALL
		SELECT
			ingAnul."IdCartera" AS "IdCartera"
		   ,car."FechaModificacionRegistro" AS "FechaRegistro"
		   ,car."EGRFechaOperacion" AS "FechaOperacion"
		   ,'INGRESO' AS "TipoOperacion"
		   ,car."IdCliente" AS "IdCliente"
		   ,car."IdPortafolio" AS "IdPortafolio"
		   ,Port."CodigoPortafolio"
		   ,cli."NombreCompleto" AS "NombreCliente"
		   ,cli."DocumentoIdentidad" AS "DocumentoIdentidad"
		   ,fac."IdConcepto" AS "IdConcepto"
		   ,Concepto."Nombre" AS "NombreConcepto"
		   ,car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
		   ,car."IdTipoInstrumento" AS "IdTipoInstrumento"
		   ,car."IdInstrumento" AS "IdInstrumento"
		   ,car."ISIN"
		   ,CASE
		   		WHEN car."IdTipoInstrumento" = 'SIM' OR car."IdTipoInstrumento" = 'PAC' THEN
				(
					SELECT "Nemotecnico" 
					FROM 
					(
						SELECT "Nemotecnico", row_number() over(order by (SELECT 1 FROM dummy) ) AS "NroFila" 
						FROM "BSCL.Custodia.BD::CUS.Subyacente" AS miSub
						WHERE car."FolioCartera" = miSub."FolioCarteraRelacionada"
					) AS Suby
					WHERE Suby."NroFila" = 1
				)	ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,car."IdEmisor" AS "IdEmisor"
		   ,car."FechaVencimientoInstrumento" AS "FechaVencimientoInstr"
		   ,car."EGRFechaLiquidacion" AS "FechaLiquidacion"
		   ,car."IdTipoReajuste" AS "IdTipoReajuste"
		   ,car."NemoSVS" AS "NemoSVS"
		   ,CASE WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN IFNULL(car."IdTipoReajuste", '') ELSE car."IdMonedaInstrumento" END AS "IdMonedaInstr"
		   ---------------------------------------------------------
		   ,CASE WHEN COALESCE(CCPI."IdCuentaCustodioPortafolio",0) > 0 THEN CCPI."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
		   ,COALESCE(CCPI."Codigo",'') AS "NumeroCuentaBoveda"
		   ,CCPI."IdTipoCustodia"
		   ,CCPI."NombreTipoCustodia"
		   ---------------------------------------------------------
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."EGRValorRescate" ELSE coalesce(CmbteDet."Cantidad",car."EGRCantidad") END AS "Cantidad"
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."EGRValorRescate" ELSE coalesce(CmbteDet."Cantidad",car."EGRCantidad") END AS "CantidadNegativa"
		   ,coalesce(CmbteDet."Precio",car."PrecioCompra") AS "Precio"
		   ,car."EGRTasa" AS "Tasa"
		   ,coalesce(CmbteDet."Monto",car."MontoCompra") as "Monto"
		   ,car."EGRValorRescate" AS "ValorRescate"
		   --,coalesce(CmbteDet."Cantidad",car."EGRCantidad") AS "Saldo"
		   ,cast(0 as DECIMAL(26,8)) AS "Saldo"
		   ,car."FolioCartera" AS "FolioCartera"
		   ,car."EGRFolioComprobante"
		   ,car."EGRFechaComprobante"
		   ,car."EGRIdTipoComprobante"
		   ,ingAnul."NombreProductoAgrupador" AS "NombreProductoAgrupador"
		   ,car."EGRSpread"
           ,car."EGRSpreadMercado"
		FROM :v_IngresosPorAnul AS ingAnul
		INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS car on car."IdCartera" = ingAnul."IdCartera"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON car."IdInstrumento" = Ins."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Port ON Port."IdPortafolio" = car."IdPortafolio" 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli ON Cli."IdCliente" = Port."IdCliente" 
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCPI ON CCPI."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as fac ON fac."IdTipoComprobante" = car."EGRIdTipoComprobante" AND fac."IdComprobante" = car."EGRFolioComprobante" AND fac."FechaComprobante" = car."EGRFechaComprobante"
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet ON CmbteDet."IdComprobante" = fac."IdComprobante" AND CmbteDet."IdInstrumento" = car."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON fac."IdConcepto" = Concepto."IdConcepto";

	END IF;
	
	
	IF p_TipoOperacion = '' OR p_TipoOperacion = 'RETIRO' THEN
	
		mi_vtbl =
		SELECT *
		FROM :mi_vtbl
		UNION ALL
		SELECT
			car."IdCartera" AS "IdCartera"
		   ,car."FechaModificacionRegistro" AS "FechaRegistro"
		   ,car."EGRFechaOperacion" AS "FechaOperacion"
		   ,'RETIRO' AS "TipoOperacion"
		   ,car."IdCliente" AS "IdCliente"
		   ,car."IdPortafolio" AS "IdPortafolio"
		   ,Port."CodigoPortafolio"
		   ,cli."NombreCompleto" AS "NombreCliente"
		   ,cli."DocumentoIdentidad" AS "DocumentoIdentidad"
		   ,car."EGRIdConcepto" AS "IdConcepto"
		   ,Concepto."Nombre" AS "NombreConcepto"
		   ,car."IdClasificacionTipoInstrumento" AS "IdClasificacionTipoInstrumento"
		   ,car."IdTipoInstrumento" AS "IdTipoInstrumento"
		   ,car."IdInstrumento" AS "IdInstrumento"
		   ,car."ISIN"
		   ,CASE 
		   		WHEN car."IdTipoInstrumento" = 'SIM' OR car."IdTipoInstrumento" = 'PAC' THEN
				(
					SELECT distinct "Nemotecnico" 
					FROM "BSCL.Custodia.BD::CUS.Subyacente" AS Suby 
					WHERE (car."FolioCartera" = Suby."FolioCarteraRelacionada")
				)
				ELSE car."Nemotecnico"
			END AS "Nemotecnico"
		   ,Case 
		      When car."IdTipoOperacion" in ('COMSIM', 'VENSIM','COMREP','VENREP') Then Ins."IdEmisor"
		    Else car."IdEmisor" 
		    End AS "IdEmisor"
		   ,car."FechaVencimientoInstrumento" AS "FechaVencimientoInstr"
		   ,car."EGRFechaLiquidacion" AS "FechaLiquidacion"
		   ,car."IdTipoReajuste" AS "IdTipoReajuste"
		   ,car."NemoSVS" AS "NemoSVS"
		   ,CASE WHEN IFNULL(car."IdTipoReajuste", '') <> '' THEN IFNULL(car."IdTipoReajuste", '') ELSE car."IdMonedaInstrumento" END AS "IdMonedaInstr"
		   ---------------------------------------------------------
		   ,CASE WHEN COALESCE(CCPR."IdCuentaCustodioPortafolio",0) > 0 THEN CCPR."IdBoveda" ELSE car."IdBoveda" END "IdBoveda"
		   ,COALESCE(CCPR."Codigo",'') AS "NumeroCuentaBoveda"
		   ,CCPR."IdTipoCustodia"
		   ,CCPR."NombreTipoCustodia"
		   ---------------------------------------------------------
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."EGRValorRescate" ELSE coalesce(CmbteDet."Cantidad",car."EGRCantidad") END AS "Cantidad"
		   ,CASE WHEN car."IdClasificacionTipoInstrumento" = 'IF' AND car."EsFungible" = 'S' THEN car."EGRValorRescate" ELSE coalesce(CmbteDet."Cantidad",car."EGRCantidad") END *-1 AS "CantidadNegativa"
		   ,car."EGRPrecio" AS "Precio"
		   ,car."EGRTasa" AS "Tasa"
		   ,car."EGRMonto"as "Monto"
		   ,car."EGRValorRescate" AS "ValorRescate"
		   --,CAST((car."INGCantidad" - "EGRCantidad") AS DECIMAL(26,8)) AS "Saldo"
		   ,CAST(0 AS DECIMAL(26,8)) AS "Saldo"
		   ,car."FolioCartera" AS "FolioCartera"
		   ,car."EGRFolioComprobante" AS "FolioComprobante"
		   ,car."EGRFechaComprobante" AS "FechaComprobante"
		   ,car."EGRIdTipoComprobante"  AS "IdTipoComprobante"
		   ,ProductoAgrupador."NombreProductoAgrupador" AS "NombreProductoAgrupador"
		   ,car."EGRSpread"
		   ,car."EGRSpreadMercado"
		FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarTablaCartera" AS car
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" as Ins ON car."IdInstrumento" = Ins."IdInstrumento"
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS Port ON Port."IdPortafolio" = car."IdPortafolio" 
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS Cli ON Cli."IdCliente" = car."IdCliente" 
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS Concepto ON car."EGRIdConcepto" = Concepto."IdConcepto" 
		LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarCuentaCustodioPortafolio" as CCPR ON CCPR."IdCuentaCustodioPortafolio" = car."IdCuentaCustodioPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" as fac ON fac."IdTipoComprobante" = car."EGRIdTipoComprobante" AND fac."IdComprobante" = car."EGRFolioComprobante" AND fac."FechaComprobante" = car."EGRFechaComprobante" AND fac."IdTipoComprobante" IN ('NCE','NCR','NEO')
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio ON Portafolio."IdPortafolio" = car."IdPortafolio" AND Portafolio."IdCliente" = car."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV" AS MatrizAgrupador ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
		LEFT JOIN "BSCL.APV.BD::APV.ProductoAgrupador" ProductoAgrupador ON ProductoAgrupador."IdProductoAgrupador" = MatrizAgrupador."IdProductoAgrupador"
		LEFT OUTER JOIN "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" As CmbteDet ON CmbteDet."IdComprobante" = fac."IdComprobante" AND CmbteDet."IdInstrumento" = car."IdInstrumento"                
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" As TipOperFac ON TipOperFac."IdTipoOperacion" = CmbteDet."IdTipoOperacion"
		WHERE
			car."IdEmpresa" = p_IdEmpresa 
		AND car."IdTipoInstrumento" not in ('PRT','EMP', 'SIM')
		AND car."EGRCantidad" <> 0
		AND car."IdEstado" <> 'ANU'
		AND car."IdCliente" = CASE WHEN p_IdCliente > 0 THEN p_IdCliente ELSE car."IdCliente" END
		AND car."IdPortafolio" = CASE WHEN p_IdPortafolio > 0 THEN p_IdPortafolio ELSE car."IdPortafolio" END
		AND car."FolioCartera" = CASE WHEN p_FolioCartera > 0 THEN p_FolioCartera ELSE car."FolioCartera" END
		AND car."IdClasificacionTipoInstrumento" = CASE WHEN p_IdClasificacionTipoInstrumento <> '' THEN p_IdClasificacionTipoInstrumento ELSE car."IdClasificacionTipoInstrumento" END
		AND car."IdTipoInstrumento" = CASE WHEN p_IdTipoInstrumento <> '' THEN p_IdTipoInstrumento ELSE car."IdTipoInstrumento" END
		AND car."IdInstrumento" = CASE WHEN p_IdInstrumento > 0 THEN p_IdInstrumento ELSE car."IdInstrumento" END
		AND car."IdMonedaInstrumento" = CASE WHEN p_IdMoneda <> '' THEN p_IdMoneda ELSE car."IdMonedaInstrumento" END
		AND car."EGRIdConcepto" = CASE WHEN p_IdConcepto <> '' THEN p_IdConcepto ELSE car."EGRIdConcepto" END
		AND car."IdBoveda" = CASE WHEN p_IdBoveda <> '' THEN p_IdBoveda ELSE car."IdBoveda" END
		AND car."EGRFechaOperacion" >= p_FechaInicio
		AND car."EGRFechaOperacion" <= p_FechaTermino
		AND IFNULL(TipOperFac."AccionCustodia",'RETIRO') = 'RETIRO'
		AND (:p_IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") = :p_IdProductoAgrupador);
	
	END IF;
	
	
	INSERT INTO "BSCL.Custodia.BD::GBL.ConsultarCartolaMovimientos_Salida_Tabla" ("IdCartera","FechaRegistro","FechaOperacion","TipoOperacion","IdCliente",
          "IdPortafolio","CodigoPortafolio","NombreCliente","DocumentoIdentidad","IdConcepto","NombreConcepto","IdClasificacionTipoInstrumento","IdTipoInstrumento","IdInstrumento",
          "ISIN","Nemotecnico","IdEmisor","FechaVencimientoInstr","FechaLiquidacion","IdTipoReajuste","NemoSVS","IdMonedaInstr","IdBoveda",
          "NumeroCuentaBoveda","IdTipoCustodia","NombreTipoCustodia","Cantidad","Precio","Tasa",
          "Monto","ValorRescate","Saldo","FolioCartera","FolioComprobante","FechaComprobante","IdTipoComprobante","NombreProductoAgrupador","Spread","SpreadMercado")
	SELECT
		"IdCartera"
	   ,"FechaRegistro"
	   ,"FechaOperacion"
	   ,"TipoOperacion"
	   ,"IdCliente"
	   ,"IdPortafolio"
	   ,"CodigoPortafolio"
	   ,"NombreCliente"
	   ,"DocumentoIdentidad"
	   ,"IdConcepto"
	   ,"NombreConcepto"
	   ,"IdClasificacionTipoInstrumento"
	   ,"IdTipoInstrumento"
	   ,"IdInstrumento"
	   ,"ISIN"
	   ,"Nemotecnico"
	   ,"IdEmisor"
	   ,"FechaVencimientoInstr"
	   ,"FechaLiquidacion"
	   ,"IdTipoReajuste"
	   ,"NemoSVS"
	   ,"IdMonedaInstr"
	   ,"IdBoveda"
	   ,"NumeroCuentaBoveda"
	   ,"IdTipoCustodia"
	   ,"NombreTipoCustodia"
	   ,"Cantidad"
	   ,"Precio"
	   ,"Tasa"
	   ,"Monto"
	   ,"ValorRescate"
	   --,"Saldo"
	   ,SUM ("CantidadNegativa") OVER (partition by "FolioCartera" order by "FechaRegistro") AS "Saldo"
	   ,"FolioCartera"
	   ,"FolioComprobante"
	   ,"FechaComprobante"
	   ,"IdTipoComprobante"
	   ,"NombreProductoAgrupador"
	   ,"EGRSpread"
	   ,"EGRSpreadMercado"
	FROM :mi_vtbl
	ORDER BY "FechaRegistro" DESC;
	
END;
GO
