set schema bscl;
DROP table #rescates;
create local temporary table #rescates(
	"IdInterno"					 BIGINT,
													"IdCliente" 				 BIGINT,
											    	"IdPortafolio" 				 BIGINT,
											    	"TipoSolicitud" 			 NVARCHAR(3),
											    	"IdSerieFondo" 				 NVARCHAR(40),
											    	"IdTipoMovimientoFondoMutuo" NVARCHAR(10),
											    	"IdDividendo" 				 BIGINT,
											    	"IdSerieFondoDestino" 		 NVARCHAR(40),
											    	"IdEmisorDestino" 			 NVARCHAR(40),
											    	"IdClienteDestino" 			 BIGINT,
											    	"IdPortafolioDestino" 		 BIGINT,
											    	"IndicadorMLT" 				 NVARCHAR(1),
											    	"Overnight" 				 NVARCHAR(1),
											    	"IdTipoCartera" 			 NVARCHAR(3),
											    	"CalculoPorMontoCuota" 		 NVARCHAR(1),
											    	"FechaEmision"				 NVARCHAR(10),
													"Monto" 					 DECIMAL(26,8),
													"NumeroCuotas" 				 DECIMAL(26,8),
													"ValorCuota" 				 DECIMAL(26,8),
													"CostoUF" 					 DECIMAL(26,2),
													"CostoMonedaLocal" 			 DECIMAL(26,8),
													"ValorUFFechaInversion" 	 DECIMAL(26,2),
													"ParidadFechaInversion" 	 DECIMAL(26,8),
											    	"IdMoneda" 					 NVARCHAR(3),
											    	"MontoComision" 			 DECIMAL(26,8),
											    	"MontoPago" 				 DECIMAL(26,8),
											    	"Estado"					 NVARCHAR(3),
											    	"FormaPago"					 BIGINT,
											    	"Glosa"						 NVARCHAR(100)
);

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(0,524891,512348,'APO','FMLIQUIDEZ_A','APONORM',0,'','',0,0,'','','','','2016-06-08',10,2,0,0,0,0,0,'CLP',0,0,'PDA', 10056, '2333333');
select * from #rescates;



call "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteRescate" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_SolicitudesAporteRescate => #rescates,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
