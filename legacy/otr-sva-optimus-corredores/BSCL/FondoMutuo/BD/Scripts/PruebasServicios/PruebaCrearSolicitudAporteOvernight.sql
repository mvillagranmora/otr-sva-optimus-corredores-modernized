set schema bscl;
DROP table #aportes;
create local temporary table #aportes(
	"IdInterno"					 			BIGINT,
	"IdCliente" 				 			BIGINT,
	"IdPortafolio" 				 			BIGINT,
	"IdSerieFondo" 				 			NVARCHAR(40),
	"Monto" 					 			DECIMAL(26,8),
	"NumeroCuotas" 				 			DECIMAL(26,8),
	"IdMoneda" 					 			NVARCHAR(3),
	"IdMedioDePago"				 			NVARCHAR(3),
	"IdMedioDePagoRescate"		 			NVARCHAR(3),
	"IdPreferenciaPagoPortafolioRescate"	BIGINT,
	"Visado"					 			NVARCHAR(1),
	"SolicitudBatch"			 			NVARCHAR(1),
	"OrigenOperacion"			 			NVARCHAR(1),
	"IdRegimenTributario"		 			NVARCHAR(40),
	"AdministracionLiquidez"	 			NVARCHAR(1),
	"Glosa"						 			NVARCHAR(100),
	"IdAutorizadoOrigen"					BIGINT
);

DELETE FROM  #aportes;
INSERT INTO #aportes
("IdInterno","IdCliente","IdPortafolio","IdSerieFondo","Monto","NumeroCuotas","IdMoneda","IdMedioDePago","IdMedioDePagoRescate","IdPreferenciaPagoPortafolioRescate"
,"Visado","SolicitudBatch","OrigenOperacion","IdRegimenTributario","AdministracionLiquidez","Glosa","IdAutorizadoOrigen")
SELECT 0,524891,512348,'FM_CUSTODIA_A',2300,437.6284,'CLP','CIN','CIN',0,'N','N','M','NINGUNO','N','',0 FROM DUMMY;

call "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAporteOvernight" (
	p_IdEmpresa	 => 1,
	p_IdUsuario	 => 21,
	p_IdModulo	 => 'FMM',
	p_SimularOperacion => 'N',
	p_UsaReglasNegocio => 'S',
	p_SolicitudesAporte => #aportes,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
