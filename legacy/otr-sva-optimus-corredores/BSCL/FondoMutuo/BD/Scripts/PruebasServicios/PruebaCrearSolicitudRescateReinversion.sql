set schema bscl;
DROP table #rescates;
DROP table #aportes;
create local temporary table #rescates(
	"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"IdSerieFondo" 				 NVARCHAR(40),
											    	"CalculoPorMontoCuota" 		 NVARCHAR(1),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
											    	"IdMoneda" 					 NVARCHAR(3),
											    	"FormaPago"					 BIGINT,
											    	"SolicitudBatch"			 NVARCHAR(1),
											    	"OrigenOperacion"			 NVARCHAR(1),
											    	"Glosa"						 NVARCHAR(100)
);
create local temporary table #aportes(
	"IdInterno"					 BIGINT,
													"IdCliente"					 BIGINT,
													"IdPortafolio"				 BIGINT,
													"IdSerieFondo"				 NVARCHAR(40),
													"IdEmisor"					 NVARCHAR(40),
													"Monto"						 DECIMAL(26,8),
													"NumeroCuotas"				 DECIMAL(26,8),
													"IndicadorMLT"				 NVARCHAR(1),
													"IdMoneda"					 NVARCHAR(3),
													"FormaPago"					 BIGINT,
													"Glosa"						 NVARCHAR(100)  
);

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(0,524891,512348,'FMLIQUIDEZ_A','M',10,2,'CLP', 10056, 'N', 'M', 'Glosa');
DELETE FROM  #aportes;
INSERT INTO #aportes VALUES(0,524891,512348,'FMLIQUIDEZ_B','',10,2,'S','CLP', 10056, 'Glosa');




call "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudRescateReinversion" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_SolicitudesRescate => #rescates,
	p_SolicitudesAporte => #aportes,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
