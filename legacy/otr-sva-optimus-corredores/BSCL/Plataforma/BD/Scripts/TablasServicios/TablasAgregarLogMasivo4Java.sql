SET SCHEMA "BSCL"; 

drop table "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log";
drop table "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle";

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_Log"(
	"IdTrabajo"					BIGINT,
	"IdCabeceraLog"				BIGINT,
	"IdTipoLog" 				NVARCHAR(40),
	"IdModulo"					NVARCHAR(3),
	"IdUsuario"					BIGINT,
	"IdEmpresa"					BIGINT,
	"FechaSistemaLog"			NVARCHAR(10),
	"IdUsuarioAtendido"			BIGINT,
	"IdCliente"					BIGINT,
	"EstadoCliente"				NVARCHAR(3),
	"NombreCliente"				NVARCHAR(100), 
	"IdTipoDocumentoIdentidad"	NVARCHAR(40), 
	"IdDocumentoIdentidad"		NVARCHAR(40), 
	"IdPortafolio"				BIGINT,
	"EstadoPortafolio"			NVARCHAR(3),
	"IdTipoRegistro"			NVARCHAR(40), 
	"IdRegistro"				NVARCHAR(40),
	"EstadoRegistro"			NVARCHAR(3),
	"IdTipoOperacion"			NVARCHAR(40), 
	"Folio"						BIGINT,
	"Cantidad"					DECIMAL(26,8),
	"PrecioTasa"				DECIMAL(26,8),
	"Monto"						DECIMAL(26,8),
	"IdMonedaOrigen"			NVARCHAR(3),
	"IdMonedaLiquidacion"		NVARCHAR(3),
	"TipoCambio"				NVARCHAR(40),
	"Nemo"						NVARCHAR(260),
	"IdProceso"					BIGINT,
	"EstadoProceso"				NVARCHAR(3),
	"NombreProceso"				NVARCHAR(100)
) ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY ROW TABLE "BSCL.Plataforma.BD::PLA.AgregarLogMasivo4Java_Entrada_LogDetalle"(
	"IdTrabajo"					BIGINT,
	"IdCabeceraLog" 			BIGINT,
	"SubRegistro" 				NVARCHAR(40), 
	"CampoRegistro" 			NVARCHAR(40), 
	"ValorAntiguo"				NVARCHAR(5000), 
	"ValorNuevo" 				NVARCHAR(5000)
) ON COMMIT DELETE ROWS;