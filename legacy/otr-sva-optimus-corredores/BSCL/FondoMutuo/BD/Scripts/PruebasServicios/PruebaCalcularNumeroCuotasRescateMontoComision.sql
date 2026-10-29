SET SCHEMA "BSCL";

DROP table #solicitudes_entrada;

CREATE local temporary table  #solicitudes_entrada(
	"IdSolicitudAporteRescate"	BIGINT,
	"IdSerieFondo"		   		NVARCHAR(40),
 	"Monto"				   		DECIMAL(26,8),
 	"NumeroCuotas"				DECIMAL(26,8),
 	"MontoDestino"				DECIMAL(26,8),
 	"ValorCuota"				DECIMAL(26,8),
 	"ValorParidad"				DECIMAL(26,8),
 	"IdMonedaOrigen"	   		NVARCHAR(3),
 	"IdMonedaDestino"	   		NVARCHAR(3),
 	"IdInstrumento"		   		BIGINT,
 	"IdCliente"					BIGINT,
 	"IdPortafolio"		   		BIGINT,
 	"DecimalesCuota"			BIGINT
);

INSERT INTO  #solicitudes_entrada
SELECT 838,'FMLIQUIDEZ_A',15000,5970.6245,15000,2.5123,1,'USD','USD',346394,524891,512348,4 FROM DUMMY;
INSERT INTO  #solicitudes_entrada
SELECT 839,'FMLIQUIDEZ_A',15000,5970.6245,15000,2.5123,1,'USD','USD',346394,524891,512348,4 FROM DUMMY;

call "BSCL.FondoMutuo.BD.Utilitarios::CalcularNumeroCuotasRescateMontoComision" (
	p_IdEmpresa	 => 1,
	p_IdPais => 44,
	p_FechaSistema => '2016-10-18',
	p_IdUsuario	 => 21,
	p_Solicitudes => #solicitudes_entrada,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
