INSERT INTO "BSCL.Normativa.BD::SIS.RegistroMandatos"
(
"IdSolicitudAporteRescate", "FolioMandato", "Valor", "IdEstado", "FechaIngreso", "IdUsuario", "IdEmpresa")
VALUES(
	:IdSolicitudAporteRescate
	,:FolioMandato
	,:Valor
	,'ING'
	,CURRENT_UTCTIMESTAMP
	,:IdUsuario
	,:IdEmpresa
	);