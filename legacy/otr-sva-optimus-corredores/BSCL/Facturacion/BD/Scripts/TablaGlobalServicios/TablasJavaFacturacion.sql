SET SCHEMA "BSCL"; 
GO

--CrearMovimientoComprobante.java
DROP TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso";
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobante_Proceso"(
	"NumeroMvtoCmbte"			BIGINT,
	"IdConceptoMovimiento"		NVARCHAR(6),
	"IdTipoComprobante"			NVARCHAR(3),
	"IdCondicionDeLiquidacion"	NVARCHAR(2),
	"IdTipoReajuste"			NVARCHAR(3),
	"IdTipoConvenio"			NVARCHAR(3),
	"IdEmisor"					NVARCHAR(40),
	"IdMonedaLiquidacion"		NVARCHAR(3),
	"IdInstrumento"				BIGINT,
	"IdInstrumentoRelacionado"	BIGINT,
	"IdEmpleadoDigitador"		BIGINT,
	"IdInstrumentoSubyacente"	BIGINT,
	"IdTipoProducto"			NVARCHAR(4),
	"IdMoneda"					NVARCHAR(3),
	"IdMonedaSubyacente"		NVARCHAR(3),
	"IdTipoCanalIngreso"		NVARCHAR(3),
	"IdTipoOperacion"			NVARCHAR(6),
	"IdBoveda"					NVARCHAR(3),
	"IdCorredoraContraparte"	BIGINT,
	"IdBolsa"					NVARCHAR(3),
	"IdOrden"					BIGINT,
	"IdPortafolio"				BIGINT	
) ON COMMIT DELETE ROWS;
GO

DROP TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteFormaDePago_Proceso";
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteFormaDePago_Proceso"(
	"NumeroMvtoCmbte"				BIGINT,
	"NumeroMvtoCmbteFormaDePago"	BIGINT,
	"IdMedioDePago"					NVARCHAR(3),
	"IdModalidadDePago"				NVARCHAR(6),
	"IdMoneda"						NVARCHAR(3),
	"IdTipoCuentaBancaria"			NVARCHAR(40),
	"IdTarjetaBancaria"				BIGINT,
	"IdTipoTarjetaBancaria"			NVARCHAR(40),
	"IdBanco"						BIGINT
);
GO

DROP TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteCobro_Proceso";
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Facturacion.BD::FAC.CrearMovimientoComprobanteCobro_Proceso"(
	"NumeroMvtoCmbte"			BIGINT,
	"NumeroMvtoCmbteCobro"		BIGINT,
	"IdCobro"					NVARCHAR(40),
	"IdMonedaCobro"				NVARCHAR(6),
	"IdMonedaLiquidacionCobro"	NVARCHAR(3)
);
GO

