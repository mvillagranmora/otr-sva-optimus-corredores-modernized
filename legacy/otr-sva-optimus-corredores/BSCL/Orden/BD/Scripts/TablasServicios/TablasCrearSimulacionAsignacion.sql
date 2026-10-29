SET SCHEMA "BSCL"; 
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Carteras";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Cobros";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_AdvertenciasDetalle";
GO
DROP TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_ErroresDetalle";
GO



CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Transacciones"(
	"IdMovimiento" 	BIGINT,
	"IdTransaccion"	BIGINT,
	"Cantidad" 		DECIMAL (26,8),
	"Monto" 		DECIMAL (26,8),
	"ValorRescate" 	DECIMAL (26,8)
) ON COMMIT DELETE ROWS;
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Ordenes"(
	"IdOrden" 	BIGINT
) ON COMMIT DELETE ROWS;

GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Portafolios"(
	"IdPortafolio"	BIGINT, 
	"Cantidad" DECIMAL (26,8), 
	"Porcentaje" DECIMAL (26,8)
) ON COMMIT DELETE ROWS;
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Entrada_Carteras"(
	"IdPortafolio"	BIGINT,
	"FolioCartera"	BIGINT,
	"Cantidad" 		DECIMAL (26,8)
) ON COMMIT DELETE ROWS;
GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Simulacion"(
	"IdInterno"				BIGINT,
	"IdMovimiento"			BIGINT,
	"FechaSimulacion"		TIMESTAMP,
	"FechaIngresoRegistro"	TIMESTAMP,
	"IdTransaccion"			BIGINT,
	"FolioTransaccion"		NVARCHAR(40),
	"FechaTransaccion"		DATE,
	"FolioCartera"			BIGINT,
	"IdPortafolio"			BIGINT,
	"CodigoPortafolio"		NVARCHAR(100),
	"IdCliente"				BIGINT,
	"IdLibroOrdenes"		BIGINT,
	"IdInstrumento"			BIGINT,
	"Instrumento"			NVARCHAR(40),
	"IdTipoInstrumento"		NVARCHAR(3),
	"IdClasificacionTipoInstrumento"		NVARCHAR(2),
	"IdMonedaInstrumento"	NVARCHAR(3),
	"IdOrden"				BIGINT,
	"NombreCompleto"		NVARCHAR(100),
	"DocumentoIdentidad"	NVARCHAR(40),
	"CantidadAsignacion"	DECIMAL(26,8),
	"ValorRescateAsignacion"DECIMAL(26,8),
	"PorcentajeAsignacion"	DECIMAL(26,8),
	"PrecioSimulacion"		DECIMAL(26,8),
	"IdTipoProducto"		NVARCHAR(4),
	"IdTipoOperacion"		NVARCHAR(6),
	"IdTipoCanalIngreso"	NVARCHAR(3),
	"IdBolsa"				NVARCHAR(3),
	--"IdFormaCustodia"		NVARCHAR(3),
	--"IdBoveda"				NVARCHAR(40),
	--"Glosa"					NVARCHAR(500),
	--"TasaVentaCorta"		DECIMAL(26,8),
	--"PlazoVentaCorta"		BIGINT,
	--"PagoCobroCuentaInversion"	NVARCHAR(1),
	"IdMonedaLiquidacionOrden"	NVARCHAR(3)
	
) ON COMMIT DELETE ROWS;
GO


CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_Cobros"(
	"IdInterno"			BIGINT,
	"IdInternoCobro"		BIGINT,
	"IdPortafolio"				BIGINT,
	"IdBolsa" 					NVARCHAR(10),
	"IdTipoProducto"			NVARCHAR(4),
	"IdTipoOperacion"			NVARCHAR(6),
	"IdTipoCanalIngreso"		NVARCHAR(4),
	"IdCobro"					NVARCHAR(40),
	"ValorCobro"				DECIMAL(26,8),
	"FormaCobro"				NVARCHAR(1),
	"ValorTopeMinimoOrden"		DECIMAL(26,8),
	"IdMoneda"					NVARCHAR(3),
	"CodigoCobroClasificacion"	NVARCHAR(40),
	"PermiteCambiarValor"		NVARCHAR(1)
) ON COMMIT DELETE ROWS;

GO

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_AdvertenciasDetalle"(
"IdInterno"				BIGINT,
"IdOperacion"			BIGINT,
"Codigo" 				NVARCHAR(100),
"RequiereAutorizacion" 	NVARCHAR(100),
"Descripcion" 			NVARCHAR(5000)
) ON COMMIT DELETE ROWS;

GO
CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Orden.BD::ORD.CrearSimulacionAsignacion_Salida_ErroresDetalle"(
"IdInterno"				BIGINT,
"IdOperacion"			BIGINT,
"Codigo" 		NVARCHAR(100),
"Descripcion" 	NVARCHAR(5000)
) ON COMMIT DELETE ROWS;
GO

commit
