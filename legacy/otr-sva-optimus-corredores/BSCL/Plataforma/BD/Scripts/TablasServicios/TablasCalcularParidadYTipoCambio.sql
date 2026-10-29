SET SCHEMA "BSCL"; 

DROP TABLE "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada";
DROP TABLE "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida";

CREATE GLOBAL TEMPORARY COLUMN TABLE "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"(
	"IdTrabajo" 					BIGINT,
	"IdInterno" 					BIGINT,
	"Fecha"							DATE,
	"IdMonedaOrigen"				NVARCHAR(3),
	"IdMonedaDestino"				NVARCHAR(3),
	"ValorCambioLocalModificado"	DECIMAL(26,8),
	"MontoOrigen"					DECIMAL(26,8),
	"IdUso"							NVARCHAR(40)
) ON COMMIT DELETE ROWS;


CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida"(
	"IdTrabajo" 					BIGINT,
	"IdInterno" 					BIGINT,
	"Fecha"							DATE,
	"IdMonedaOrigen"				NVARCHAR(3),
	"IdMonedaDestino"				NVARCHAR(3),
	"ValorCambioLocalModificado"	DECIMAL(26,8),
	"MontoOrigen"					DECIMAL(26,8),
	"IdUso"							NVARCHAR(40),
	"FactorOrigenDestino"			DECIMAL(26,8),
	"MontoDestino"					DECIMAL(26,8),
	"FactorOrigenLocal"				DECIMAL(26,8),
	"MontoLocal"					DECIMAL(26,8),
	"FactorOrigenCambio"			DECIMAL(26,8),
	"FechaOrigenCambio"				DATE,
	"IdTipoRegistroOrigenCambio"	NVARCHAR(40),
	"FactorCambioLocal"				DECIMAL(26,8),
	"FechaCambioLocal"				DATE,
	"IdTipoRegistroCambioLocal"		NVARCHAR(40),
	"FactorLocalCambio"				DECIMAL(26,8),
	"FechaLocalCambio"				DATE,
	"IdTipoRegistroLocalCambio"		NVARCHAR(40),
	"FactorCambioDestino"			DECIMAL(26,8),
	"FechaCambioDestino"			DATE,
	"IdTipoRegistroCambioDestino"	NVARCHAR(40),
	"Resultado"						NVARCHAR(40),
	"CodigoMensaje"					NVARCHAR(100)
) ON COMMIT DELETE ROWS;

commit

 
 

 

 

 





 






 

 
 

