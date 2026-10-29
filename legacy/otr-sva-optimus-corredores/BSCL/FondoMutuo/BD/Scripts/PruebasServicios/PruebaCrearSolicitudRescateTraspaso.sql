set schema bscl;
DROP table #rescates;
create local temporary table #rescates(
	"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"IdSerieFondo" 				 NVARCHAR(40),
											    	"IdPortafolioDestino"		 BIGINT,
											    	"CalculoPorMontoCuota" 		 NVARCHAR(1),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
											    	"IdMoneda" 					 NVARCHAR(3),
											    	"FormaPago"					 BIGINT,
											    	"SolicitudBatch"			 NVARCHAR(1),
											    	"OrigenOperacion"			 NVARCHAR(1),
											    	"Glosa"						 NVARCHAR(100)
);


DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(0,524891,512348,'FMLIQUIDEZ_A',512348,'M',10,2,'CLP', 10056, 'N', 'M', 'Glosa');




call "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudRescateTraspaso" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_SolicitudesRescate => #rescates,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
