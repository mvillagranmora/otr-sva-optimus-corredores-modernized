/**************************************************************
Proposito	: Script consulta de movimientos para notificar
autor		: CFlores
fecha		: 14/05/2018
***********************************************************/
		
		SELECT
		  SOLICITUD."IdCliente"
		, SOLICITUD."IdPortafolio"
		, SOLICITUD."IdPersona"
		, SOLICITUD."IdSerieFondo"
		, SOLICITUD."NombreFondo"
		, SERIES."DiasComision"
	  	, SERIES."PorcentajeComision"
	  	, SERIES."PorcentajeRemuneracion"
	  	, SERIES."PorcentajeGastoOpera" 
	  	, SERIES."PorcentajeCostoAdm"
	  	, CLI."DocumentoIdentidad"
		FROM "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" SOLICITUD
		INNER JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" SERIES ON (SOLICITUD."IdSerieFondo"=SERIES."IdSerieFondo")
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI ON (CLI."IdCliente"=SOLICITUD."IdCliente")	
		WHERE  SOLICITUD."IdEmpresa"= :IdEmpresa
				
				
				AND TO_DATE(SOLICITUD."FechaCertificacion") =TO_DATE(:FechaCertificacion)
				AND SOLICITUD."EstadoSolicitud"='CRT'
		GROUP BY
		  SOLICITUD."IdCliente"
		, SOLICITUD."IdPortafolio"
		, SOLICITUD."IdPersona"		
		, SOLICITUD."IdSerieFondo"
		, SOLICITUD."NombreFondo"
		, SERIES."DiasComision"
	  	, SERIES."PorcentajeComision"
	  	, SERIES."PorcentajeRemuneracion"
	  	, SERIES."PorcentajeGastoOpera" 
	  	, SERIES."PorcentajeCostoAdm"			
		, CLI."DocumentoIdentidad"