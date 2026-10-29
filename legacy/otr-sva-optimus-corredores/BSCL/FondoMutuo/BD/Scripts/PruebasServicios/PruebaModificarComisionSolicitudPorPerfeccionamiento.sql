SET SCHEMA BSCL;


DROP table #solicitudes;

create local temporary table #solicitudes(
									"IdInterno"					 BIGINT,
									"IdSolicitudAporteRescate"	 BIGINT,
									"IdCliente" 				 BIGINT,
							    	"IdPortafolio" 				 BIGINT,
							    	"IdSerieFondo" 		 		 NVARCHAR(40),
							    	"CalculoPorMontoCuota"		 NVARCHAR(1),
							    	"TipoSolicitud"				 NVARCHAR(3),
							    	"IdMoneda"					 NVARCHAR(3),
							    	"Monto"						 DECIMAL(26,8),
							    	"NumeroCuotas"				 DECIMAL(26,8),
							    	"MontoComision"				 DECIMAL(26,8)
								);
								
INSERT INTO #solicitudes
SELECT 0,13423,"IdCliente","IdPortafolio","IdSerieFondo","CalculoPorMontoCuota","TipoSolicitud","IdMoneda","Monto","NumeroCuotas",10 AS "MontoComision"
FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" WHERE "IdSolicitudAporteRescate" = 13423;

CALL "BSCL.FondoMutuo.BD.Servicios::ModificarComisionSolicitudPorPerfeccionamiento"(
	p_IdEmpresa =>1,
    p_IdUsuario =>21,
    p_IdModulo => 'FMM',
    p_SimularOperacion =>'N',
    p_UsaReglasNegocio =>'N',
	p_Solicitudes => #solicitudes,
	p_out_Resultado => ? ,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=>?,
	p_out_ErroresContador 		=>?,
	p_out_ErroresDetalle 		=>?
);

SELECT TOP 10 * FROM "BSCL.Plataforma.BD::PLA.Depuracion" WHERE "Servicio" = 'MODIFICAR COMISION SOLICITUD PERFECCIONAMIENTO' ORDER BY "Fecha" DESC;