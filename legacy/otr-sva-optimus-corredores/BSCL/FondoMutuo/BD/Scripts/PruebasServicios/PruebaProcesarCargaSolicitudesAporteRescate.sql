SET SCHEMA "BSCL"; 

DROP table #procesar;

create local temporary table #procesar(
	"IdInterno"	BIGINT,
	"IdCarga"	BIGINT
);

INSERT INTO #procesar
SELECT 0, 91 FROM DUMMY;

CALL "BSCL.FondoMutuo.BD.Servicios::ProcesarCargaSolicitudesAporteRescate"(
	p_IdEmpresa => 1,
	p_IdUsuario => 21,
	p_IdModulo => 'FMM',
	p_IdCargas => #procesar,
	p_out_Resultado => ?,
	p_out_AdvertenciasContador => ?,
	p_out_AdvertenciasDetalle => ?,
	p_out_ErroresContador => ?,
	p_out_ErroresDetalle => ?
);
