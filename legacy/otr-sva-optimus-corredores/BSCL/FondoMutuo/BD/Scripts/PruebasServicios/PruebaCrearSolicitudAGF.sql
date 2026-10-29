set schema bscl;
DROP table #solicitudes;

create local temporary table #solicitudes(
	"IdSolicitudAporteRescate"	BIGINT
);

INSERT INTO #solicitudes
SELECT 10091 FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::CrearSolicitudAGF"(
	p_IdEmpresa => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_EsManual => 'N',
	p_Solicitudes => #solicitudes,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);