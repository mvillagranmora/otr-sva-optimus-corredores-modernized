SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Entrada_Entrada";
DROP TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida";
DROP TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador";

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Entrada_Entrada"(
	"IdProceso"			BIGINT,
	"Fecha"				DATE,
	"IdMoneda"			NVARCHAR(3),
	"Cantidad"			DECIMAL(26,8),
	"Precio"			DECIMAL(26,8),
	"Tasa"				DECIMAL(26,8),
	"Plazo"				BIGINT,
    "PlazoResidual"		BIGINT,
    "Base"				BIGINT,
    PRIMARY KEY ("IdProceso")
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Salida_Salida"(
	"IdProceso"			BIGINT,
	"CodigoMensaje"		NVARCHAR(100),
	"Fecha"				DATE,
	"IdMoneda"			NVARCHAR(3),
	"Cantidad"			DECIMAL(26,8),
	"Precio"			DECIMAL(26,8),
	"Tasa"				DECIMAL(26,8),
	"Plazo"				BIGINT,
	"PlazoResidual"		BIGINT,
    "Base"				BIGINT,
    ---------DEVUELTOS
    "Monto"				DECIMAL(26,8),
    "MontoMonedaLocal"	DECIMAL(26,8),
    "DiasTranscurridos"	BIGINT,
    "TasaPorPlazo"		DECIMAL(26,8),
    "TasaPorDiasTranscurridos" DECIMAL(26,8),
    "InteresDiario"		DECIMAL(26,8),
    "InteresAcumulado"	DECIMAL(26,8),
    "ValorPresente"		DECIMAL(26,8),
    "MontoAPlazo"		DECIMAL(26,8),
    "MontoAPlazoMonedaLocal" DECIMAL(26,8),
    
    "DescripcionMensaje" NVARCHAR(5000),
	"CodigoDato1"		NVARCHAR(100),
	"ValorDato1"		NVARCHAR(520),
	"CodigoDato2"		NVARCHAR(100),
	"ValorDato2"		NVARCHAR(520),
	PRIMARY KEY ("IdProceso")
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador"(
	"IdIndicador"	BIGINT,
	"IdProceso"		BIGINT,
	"Fecha"			DATE,
	"MonedaOrigen"	NVARCHAR(3),
	"MontoOrigen"	DECIMAL(26,8),
	"EsMontoAPlazo" NVARCHAR(1)
) ON COMMIT DELETE ROWS;

CREATE CPBTREE INDEX "INX01_BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador" 
ON "BSCL.Plataforma.BD::VAL.CalcularIntereses_Proceso_Indicador"(
"IdIndicador","IdProceso","EsMontoAPlazo" ASC
);

commit

 
 

 

 

 





 






 

 
 

