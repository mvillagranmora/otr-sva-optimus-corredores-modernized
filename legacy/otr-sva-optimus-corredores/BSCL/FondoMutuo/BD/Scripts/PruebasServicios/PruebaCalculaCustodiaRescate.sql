set schema bscl;
DROP table #solicitudes;

create local temporary table #solicitudes("IdSolicitudAporteRescate"		   BIGINT,
										"IdSerieFondo"		   NVARCHAR(40),
									 	"CalculoPorMontoCuota" NVARCHAR(1),
									 	"Monto"				   DECIMAL(26,8),
									 	"NumeroCuotas"		   DECIMAL(26,8),
									 	"ValorCuota"		   DECIMAL(26,8),
									 	"MontoDestino"		   DECIMAL(26,8),
									 	"ValorParidad"		   DECIMAL(26,8),
									 	"IdMonedaOrigen"	   NVARCHAR(3),
									 	"IdMonedaDestino"	   NVARCHAR(3),
									 	"TipoSolicitud"		   NVARCHAR(3),
									 	"IdInstrumento"		   BIGINT,
									 	"IdCliente"			   BIGINT,
									 	"IdPortafolio"		   BIGINT,
									 	"FechaCertificacion"   NVARCHAR(10));
									 	
INSERT INTO #solicitudes
SELECT 540,'FMLIQUIDEZ_A','M',233, 44.3336,5.2556,400000,500,'USD','USD','RES',346394,524891,512348,'2016-09-20' FROM DUMMY;
INSERT INTO #solicitudes
SELECT 541,'FMLIQUIDEZ_A','M',85000,16173.2247,5.2556,400000,500,'USD','USD','RES',346394,524891,512348,'2016-09-20' FROM DUMMY;
INSERT INTO #solicitudes
SELECT 542,'FMLIQUIDEZ_A','M',2000,380.5464,5.2556,400000,500,'USD','USD','RES',346394,524891,512348,'2016-09-20' FROM DUMMY;


call "BSCL.FondoMutuo.BD.Utilitarios::CalculaCustodiaRescate" (
	p_IdEmpresa => 1,
	p_IdPais => 44,
	p_FechaSistema => '2016-09-20',
	p_Solicitudes => #solicitudes,
	p_out_Resultado => ?
);
