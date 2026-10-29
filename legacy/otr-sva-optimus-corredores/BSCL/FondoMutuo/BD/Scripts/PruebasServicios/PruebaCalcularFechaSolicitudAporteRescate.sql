set schema bscl;
DROP table #rescates;
create local temporary table #rescates(
									"IdInterno"			  BIGINT,
									"IdSerieFondo"		  NVARCHAR(40),
									"FechaEmision"		  NVARCHAR(10),
									"EstadoFondo"		  NVARCHAR(1),
									"EstadoLiquidacion"	  NVARCHAR(1),
									"MoneyMarket"		  NVARCHAR(1),
									"NumeroDiasRetencion" BIGINT,
									"NumeroValutaCertificacion" BIGINT,
									"NumeroValutaCurse" BIGINT,
									"SolicitudBatch" NVARCHAR(1));

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(0,'FMLIQUIDEZ_A','2016-08-24','C','C','N',0,0,0,'N');
INSERT INTO #rescates VALUES(0,'FMLIQUIDEZ_A','2016-08-25','C','C','N',0,0,0,'N');

call "BSCL.FondoMutuo.BD.Utilitarios::CalcularFechaSolicitudAporteRescate" (
	p_IdPais	 => 1,
	p_SolicitudAporteRescate => #rescates,
	p_FechasAporteRescate => ?
);


