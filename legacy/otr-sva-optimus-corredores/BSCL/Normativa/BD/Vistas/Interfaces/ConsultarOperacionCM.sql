SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarOperacionCM";
GO
CREATE VIEW "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarOperacionCM"
AS
SELECT DISTINCT
	TO_CHAR(DCOMP."FechaMovimientoComprobante",'YYYYMMDD')	AS "FechaOperacion"
	,TO_BIGINT(1) AS "ModalidadOperacion" --FALTA
	,TO_BIGINT(CAV."CorrelativoBVL") AS "NumeroOperacion"
	,TO_BIGINT(1) AS "CodigoParticipante" --FALTA
	,CASE DCOMP."AccionTipoOperacion" WHEN 'COMPRA' THEN TO_BIGINT(1) ELSE TO_BIGINT(2) END AS "TipoDetalle"
	,TO_BIGINT(0) AS "CodigoTitular"--FALTA
	,DCOMP."Cantidad" AS "CantidadCavali"
	,TO_DECIMAL(TO_BIGINT(0),26,8) AS "CantidadFisica" --FALTA
	,TO_BIGINT(0) AS "Giro" --FALTA
	,TO_BIGINT(0) AS "FlagTrabajador" --FALTA
	,TO_DECIMAL(0,26,8) AS "ComisionContado" --FALTA
	,TO_DECIMAL(0,26,8) AS "ComisionPlazo" --FALTA
	,'N' AS "FlagPoliza" --FALTA
	,'N' AS "FlagContado" --FALTA
	,'N' AS "FlagPlazo" --FALTA
	,TO_BIGINT(0) AS "CodigoCustodioValores" --FALTA
	,TO_BIGINT(0) AS "CodigoCustodioFondo" --FALTA
	,TO_DECIMAL(0,26,8) AS "InteresContado" --FALTA
	,TO_DECIMAL(0,26,8) AS "InteresPlazo" --FALTA
	,'N' AS "LiquidacionDTC" --FALTA
	,TO_BIGINT(0) AS "FlagDayTrade" --FALTA
	,TO_CHAR(DCOMP."FechaVencimiento",'YYYYMMDD') AS "FechaVencimientoDocumento"
	,TO_BIGINT(0) AS "IndicadorCartaFianza"
	,TO_DECIMAL(0,26,8) AS "MontoCartaFianza"
	,TO_BIGINT(0) AS "CodigoBanco"
	,DCOMP."FechaMovimientoComprobante"
	,PRODU."Nombre" AS "NombreProducto"
	,CONCE."Nombre" AS "NombreConcepto"
	,DCOMP."IdEmpresa"
	,TO_BIGINT(CAV."CorrelativoBVL") AS "CorrelativoBVL"
FROM    "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante"    DCOMP
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoOperacion"      TOPER 
		ON (DCOMP."IdTipoOperacion" = TOPER."IdTipoOperacion" AND DCOMP."IdTipoInstrumento" = TOPER."IdTipoInstrumento")                
INNER JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarConcepto"           CONCE
		ON (DCOMP."IdConceptoMovimiento" = CONCE."IdConcepto")                      
INNER JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarTipoProducto"       PRODU
		ON (DCOMP."CodigoTipoProducto" = PRODU."IdTipoProducto")
INNER JOIN "BSCL.Orden.BD::ORD.CargaCuadraturaCavali" CAV
		ON TO_NVARCHAR(CAV."NumeroOperacion") = DCOMP."FolioTransaccion"	
WHERE DCOMP."IdEstadoComprobante" IN ('FAC', 'ENV')
AND DCOMP."IdTipoProducto" IN ('INIF', 'INRF', 'INSI', 'INRV','RERV','RERF');
GO
GRANT SELECT ON "BSCL.Normativa.BD.Vistas::Interfaces.ConsultarOperacionCM" TO BCS_DESARROLLADOR WITH GRANT OPTION;