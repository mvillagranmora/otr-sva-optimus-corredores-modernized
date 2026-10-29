SET SCHEMA "BSCL"; 
GO
drop table "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ";
GO
drop table "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS";
GO
drop table "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ";
GO
drop table "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS";
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ"(
	"IdValorizacion"		BIGINT,
	"FechaValorizacion"		NVARCHAR(10),
	"ValorizaACompra"		NVARCHAR(1),
	"ValorizaAMercado"		NVARCHAR(1),
	"IdInstrumento"			BIGINT,
	"Nemotecnico"			NVARCHAR(40),
	"IdMoneda"				NVARCHAR(3),
	"Cantidad"				DECIMAL(26,8),
	"Precio"				DECIMAL(26,8),
	"Tamano"				BIGINT,
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"				NVARCHAR(40),
	"TipoCartera"			NVARCHAR(250),
	"IdTipoCriterioTasa"	NVARCHAR(40), 
	"EsCuentaPropia"		NVARCHAR(1)
)ON COMMIT DELETE ROWS;
GO
CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ" 
ON "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosPQ"(
"IdValorizacion" ASC
);
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS"(
	"IdValorizacion"		BIGINT,
	"FechaValorizacion"		NVARCHAR(10),
	"ValorizaACompra"		NVARCHAR(1),
	"ValorizaAMercado"		NVARCHAR(1),
	---INSTRUMENTO
	"IdInstrumento"			BIGINT,
	"Nemotecnico"			NVARCHAR(40),
	"FechaEmision"			NVARCHAR(10),
	"FechaVencimiento"		NVARCHAR(10),
	"IdTipoReajuste"		NVARCHAR(3),
	"Cantidad"				DECIMAL(26,8),
	"ValorRescate" 			DECIMAL(26,8),
	"Precio"				DECIMAL(26,8),
	"Tasa"					DECIMAL(26,8),
	"Monto"		 			DECIMAL(26,8), --> TODO, ESTE MONTO DEBERIA SER SOLO PARA LA FECHA 1
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"				NVARCHAR(40),
	"TipoCartera"			NVARCHAR(250),
	"IdTipoCriterioTasa"	NVARCHAR(40),
	"EsCuentaPropia"		NVARCHAR(1),
	"TipoValorizacion"		NVARCHAR(10)
)ON COMMIT DELETE ROWS;
GO
CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS" 
ON "BSCL.Plataforma.BD::VAL.Valorizar_Entrada_InstrumentosWS"(
"IdValorizacion" ASC
);
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ"(
	"IdValorizacion"					BIGINT,
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"FechaValorizacion"					DATE,
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdMoneda"							NVARCHAR(3),
	"Cantidad"							DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	"Tamano"							BIGINT,
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"							NVARCHAR(40),
	"TipoCartera"						NVARCHAR(250),
	"IdTipoCriterioTasa"				NVARCHAR(40),
	"EsCuentaPropia"					NVARCHAR(1),
	--MONTOS VALORIZADOS
	"Monto"								DECIMAL(26,8),
	"MontoMonedaLocal"					DECIMAL(26,8),
	--DE OBTENCION DE PRECIOS O TASAS
	"FechaPrecio"						DATE,
	"PrecioMercado"						DECIMAL(26,8),
	"IdMonedaMercado"					NVARCHAR(3),
	"CodigoFuentePrecio"				NVARCHAR(40),
	"TamanoMercado"						BIGINT,
	--utilizados para monto
	"ValorParametroFinanciero"			DECIMAL(26,8),
	"FechaParametroFinanciero"			DATE,
	--ERRORES O ADVERTENCIAS
	"CodigoMensaje"						NVARCHAR(100),
	"DescripcionMensaje"				NVARCHAR(5000)
)ON COMMIT DELETE ROWS;
GO
CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ" 
ON "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosPQ"(
"IdValorizacion" ASC
);
GO
CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS"(
	"IdValorizacion"					BIGINT,
	"ValorizaACompra"					NVARCHAR(1),
	"ValorizaAMercado"					NVARCHAR(1),
	"FechaValorizacion"					DATE,
	"IdClasificacionTipoInstrumento"	NVARCHAR(3),
	"IdTipoInstrumento"					NVARCHAR(3),
	"IdInstrumento"						BIGINT,
	"Nemotecnico"						NVARCHAR(40),
	"IdTipoReajuste"					NVARCHAR(3), -- reemplaza a idMoneda
	"Cantidad"							DECIMAL(26,8),
	"Precio"							DECIMAL(26,8),
	--PARA OBTENCION DE PRECIOS O TASAS
	"UsoPrecio"							NVARCHAR(40),
	"TipoCartera"						NVARCHAR(250),
	"IdTipoCriterioTasa"				NVARCHAR(40),
	"EsCuentaPropia"					NVARCHAR(1),
	--PARA OBTENCION DE INTERESES
	"Tasa"								DECIMAL(26,8),
	"Plazo"								BIGINT, -- PLAZO PARA LA OBENCION DE INTERESES
	"FechaVencimientoOperacion"			DATE, 
	"Base"								BIGINT,
	"PlazoResidual"						BIGINT,
	--MONTOS VALORIZADOS
	"Monto"								DECIMAL(26,8),
	"MontoMonedaLocal"					DECIMAL(26,8),
	--DE CALCULO DE INTERESES
	"DiasTranscurridos"					BIGINT,
    "TasaPorPlazo"						DECIMAL(26,8),
    "TasaPorDiasTranscurridos"			DECIMAL(26,8),
    "InteresDiario"						DECIMAL(26,8),
    "InteresAcumulado"					DECIMAL(26,8),
    "ValorPresente"						DECIMAL(26,8),
    "MontoAPlazo"						DECIMAL(26,8),
    "MontoAPlazoMonedaLocal" 			DECIMAL(26,8),
	--DE OBTENCION DE PRECIOS O TASAS
	"FechaPrecio"						DATE,
	"PrecioMercado"						DECIMAL(26,8),
	"IdMonedaMercado"					NVARCHAR(3),
	"CodigoFuentePrecio"				NVARCHAR(40),
	"TamanoMercado"						BIGINT,
	--DEL VALORIZADOR
	"Duration"							DECIMAL(26,8),
	"Convexidad"						DECIMAL(26,8),
	"PlazoValorizador"					DECIMAL(26,8),
	--utilizados para monto
	"ValorParametroFinanciero"			DECIMAL(26,8),
	"FechaParametroFinanciero"			DATE,
	--ERRORES O ADVERTENCIAS
	"CodigoMensaje"						NVARCHAR(100),
	"DescripcionMensaje"				NVARCHAR(5000),
	"CodigoWSCaller"					NVARCHAR(100),
	"DescripcionWSCaller"				NVARCHAR(5000)
)ON COMMIT DELETE ROWS;
GO
CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS" 
ON "BSCL.Plataforma.BD::VAL.Valorizar_Salida_InstrumentosValorizadosWS"(
"IdValorizacion" ASC
);
GO
commit;