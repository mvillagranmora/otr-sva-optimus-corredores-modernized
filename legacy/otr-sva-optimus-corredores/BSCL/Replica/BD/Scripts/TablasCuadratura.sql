set schema bscl;
DROP TABLE "BSCL.Custodia.BD::CUS.CuadraturaOperacionesCartera";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CUS.CuadraturaOperacionesCartera" (
	"DocumentoIdentidad" 		NVARCHAR(40),
	"IdTipoDocumentoIdentidad" 	NVARCHAR(3),
	"CodigoPortafolio"			NVARCHAR(40),
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"Nemotecnico" 				NVARCHAR(40),
	"TipoMovimiento" 			NVARCHAR(40),
	"IdMovimiento"				BIGINT,
	"Cantidad"					DECIMAL(26,8),
	"FechaOperacion" 			TIMESTAMP
);
	
DROP TABLE "BSCL.Custodia.BD::CUS.CuadraturaMovimientosCartera";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CUS.CuadraturaMovimientosCartera" (
	"DocumentoIdentidad" 		NVARCHAR(40),
	"IdTipoDocumentoIdentidad" 	NVARCHAR(3),
	"CodigoPortafolio"			NVARCHAR(40),
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"Nemotecnico" 				NVARCHAR(40),
	"TipoMovimiento" 			NVARCHAR(40),
	"IdCartera"					BIGINT,
	"Cantidad"					DECIMAL(26,8),
	"FechaOperacion" 			TIMESTAMP
);

DROP TABLE "BSCL.Custodia.BD::CUS.CuadraturaOperacionesGarantia";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CUS.CuadraturaOperacionesGarantia" (
	"DocumentoIdentidad" 		NVARCHAR(40),
	"IdTipoDocumentoIdentidad" 	NVARCHAR(3),
	"CodigoPortafolio"			NVARCHAR(40),
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"Nemotecnico" 				NVARCHAR(40),
	"TipoMovimiento" 			NVARCHAR(40),
	"IdMovimiento"				BIGINT,
	"Cantidad"					DECIMAL(26,8),
	"FechaOperacion" 			TIMESTAMP
);

DROP TABLE "BSCL.Custodia.BD::CUS.CuadraturaMovimientosGarantia";
CREATE COLUMN TABLE "BSCL.Custodia.BD::CUS.CuadraturaMovimientosGarantia" (
	"DocumentoIdentidad" 		NVARCHAR(40),
	"IdTipoDocumentoIdentidad" 	NVARCHAR(3),
	"CodigoPortafolio"			NVARCHAR(40),
	"Nemotecnico" 				NVARCHAR(40),
	"TipoMovimiento" 			NVARCHAR(40),
	"IdCartera"					BIGINT,
	"Cantidad"					DECIMAL(26,8),
	"FechaOperacion" 			TIMESTAMP
);

