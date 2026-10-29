SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.HistorialCambiosModificarFechas";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.HistorialCambiosModificarFechas" AS 

	SELECT
	     sol."IdSolicitudAporteRescate" AS "IdSolicitudAporteRescate",
	   	 sol."FechaLiquidacion" AS "FechaLiquidacion",
		 sol."FechaCertificacion"AS "FechaCertificacion",
		 sol."FechaAceptacion" AS "FechaAceptacion",
	     sol."FechaValorCuota" AS "FechaValorCuota",
	 	 sol."FechaLiquidacionAnterior" AS "FechaLiquidacionAnterior",
		 sol."FechaCertificacionAnterior" AS "FechaCertificacionAnterior",
		 sol."FechaAceptacionAnterior" AS "FechaAceptacionAnterior",
		 sol."FechaValorCuotaAnterior" AS "FechaValorCuotaAnterior",
         usmod."CodigoUsuario" AS "UsuarioModificador",
	     usaut."CodigoUsuario" AS "UsuarioAutorizador"
	FROM "BSCL.FondoMutuo.BD::FMM.HistorialCambiosModificarFechasSolicitudAporteRescate" AS sol
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS usmod ON (sol."UsuarioModificador" = usmod."IdUsuario")
	LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS usaut ON (sol."UsuarioAutorizador" = usaut."IdUsuario");
	
GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.HistorialCambiosModificarFechas" TO  BCS_DESARROLLADOR WITH GRANT OPTION;