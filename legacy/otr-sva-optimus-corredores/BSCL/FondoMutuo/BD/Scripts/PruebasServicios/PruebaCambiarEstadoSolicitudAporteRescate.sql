set schema bscl;
DROP table #rescates;
create local temporary table #rescates("IdSolicitudAporteRescate" BIGINT);

DELETE FROM  #rescates;
INSERT INTO #rescates VALUES(188);
select * from #rescates;



call "BSCL.FondoMutuo.BD.Servicios::CambiarEstadoSolicitudAporteRescate" (
	p_Solicitudes => #rescates,
	p_NuevoEstado	 => 'ANU',
	p_out_Resultado => ?
);


SELECT * FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate";
