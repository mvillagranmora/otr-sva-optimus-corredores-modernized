/**************************************************************
Proposito	: Script consulta de movimientos para notificar
autor		: CFlores
fecha		: 14/05/2018
***********************************************************/


SELECT * FROM (
		SELECT 	SOLICITUD."IdCliente"
			  , SOLICITUD."IdPersona"
			  , DEL."IdDireccionElectronica" AS "IdDireccionElectronicaPrincipal"
			  , CLI."DocumentoIdentidad"
			  , CLI."NombreCompleto" AS "NombreDestinatario"
		FROM  "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarSolicitudAporteRescate" AS SOLICITUD
		INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS CLI 
			ON (CLI."IdCliente"=SOLICITUD."IdCliente")	
		INNER JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" DEL 
			ON (SOLICITUD."IdPersona" = DEL."IdPersona" AND DEL."Borrado" = 'N' AND DEL."Predeterminado" = 'S' AND DEL."IdEstado" = 'ACT')
		WHERE  SOLICITUD."IdEmpresa"= :IdEmpresa
		AND TO_DATE(SOLICITUD."FechaCertificacion") = TO_DATE(:FechaCertificacion)
		AND "EstadoSolicitud"='CRT'
		AND SOLICITUD."IdTipoMovimientoFondoMutuo" IN ('RESTRASP', 'APONORM', 'APOREINV', 'RESNORM', 'RESREINV', 'APOTRASP', 'APOCES', 'RESCCES')
		GROUP BY SOLICITUD."IdCliente", SOLICITUD."IdPersona", DEL."IdDireccionElectronica", CLI."DocumentoIdentidad", CLI."NombreCompleto"
		ORDER BY SOLICITUD."IdCliente"
	)
CROSS JOIN (
SELECT 
		"RazonSocial" AS "RazonCorredora", 
		"Direccion"   AS "DireccionCorredora", 
		"RUT" AS "RutCorredora", 
		"Telefono" AS "TelefonoCorredora"
		FROM "BSCL.Plataforma.BD.Vistas::CTT.ConsultarContratoDatosGenerales"
		WHERE "IdEmpresa" = :IdEmpresa
);

