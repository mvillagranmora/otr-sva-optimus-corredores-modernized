set schema bscl;
DROP table #rescates;
create local temporary table #rescates(
	"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"TipoSolicitud" 			 NVARCHAR(3),
											    	"IdSerieFondo" 				 NVARCHAR(40),
											    	"CalculoPorMontoCuota" 		 NVARCHAR(1),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
											    	"IdMoneda" 					 NVARCHAR(3),
											    	"IdMedioDePago"				 NVARCHAR(3),
											    	"SolicitudBatch"			 NVARCHAR(1),
											    	"OrigenOperacion"			 NVARCHAR(1),
											    	"Glosa"						 NVARCHAR(100)
);

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(0,524891,512348,'RES','FMLIQUIDEZ_A','C',120.88, 22, 'USD', 'CIN', 'N', 'M', 'Glosa');
select * from #rescates;



call "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteRescateNormal" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'S',
	p_UsaReglasNegocio => 'S',
	p_SolicitudesAporteRescate => #rescates,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


--SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
