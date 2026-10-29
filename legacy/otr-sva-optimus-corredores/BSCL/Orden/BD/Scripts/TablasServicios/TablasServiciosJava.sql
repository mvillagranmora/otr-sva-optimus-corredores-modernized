SET SCHEMA "BSCL"; 
GO

--CrearTransaccionesMarketData.java
DROP TABLE "BSCL.Orden.BD::ORD.CrearTransaccionesMarketData_Proceso";
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearTransaccionesMarketData_Proceso"(
	"IdInterno"		BIGINT,
	"Nemotecnico"	NVARCHAR(40),
	"AccionOrden"	NVARCHAR(40),
	"IdTipoReajuste"	NVARCHAR(3)
	
) ON COMMIT DELETE ROWS;
GO

--CrearTransaccionesBolsaElectronica.java
DROP TABLE "BSCL.Orden.BD::ORD.CrearTransaccionesBolsaElectronica_Proceso";
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearTransaccionesBolsaElectronica_Proceso"(
	"IdInterno"		BIGINT,
	"Nemotecnico"	NVARCHAR(40),
	"AccionOrden"	NVARCHAR(40),
	"IdTipoReajuste"	NVARCHAR(3),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2)
	
) ON COMMIT DELETE ROWS;
GO

--CrearOrdenes.java
DROP TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso";
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearOrdenes_Proceso"(
	"IdInterno"	 						BIGINT,
	"IdBoveda"							NVARCHAR(40),
	"IdFormaCustodia"					NVARCHAR(3),
	"IdTipoVigencia"					NVARCHAR(2),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCondicionDeLiquidacion"			NVARCHAR(2),
	"IdPorcentajeVisibilidad"			BIGINT,
	"IdTipoPrecio"						NVARCHAR(1),
	"IdPortafolio"						BIGINT,
	"IdTipoCanalIngreso"				NVARCHAR(3),
	"IdTipoReajuste"					NVARCHAR(3),
	"IdBolsa"							NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"IdTipoCanalDistribucion"			NVARCHAR(3)
	
) ON COMMIT DELETE ROWS;
GO

--CrearTransacciones.java
DROP TABLE "BSCL.Orden.BD::ORD.CrearTransacciones_Proceso";
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearTransacciones_Proceso"(
	"IdInterno"	 						BIGINT,
	"IdInstrumento"						BIGINT,
	"IdInstrumentoSubyacente"			BIGINT,
	"IdLibroOrdenes"					BIGINT,
	"IdBolsa"							NVARCHAR(3),
	"IdTipoOperacion"					NVARCHAR(6),
	"IdCorredoraCorresponsal"			BIGINT,
	"IdTipoReajuste"					NVARCHAR(3),
	"IdCondicionDeLiquidacionContado"	NVARCHAR(2),
	"CodigoOperadorCorredor"			NVARCHAR(40)
	
) ON COMMIT DELETE ROWS;
GO