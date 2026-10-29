SELECT	"BSCL.Custodia.BD.Secuencias::EventoCapitalCliente".NEXTVAL				as "IdEventoCapitalCliente",			-- "IdEventoCapitalCliente"
		w."IdSocioDeNegocio"													as "IdSocioNegocio",
		'CNT'																	as "TipoSocioNegocio",
		0																		as "IdPortafolio",
		w."TipoDocumentoIdentidad"												as "IdTipoDocumentoIdentidad",
		w."DocumentoIdentidad"													as "DocumentoIdentidad",
		w."NombreCompleto"														as "NombreCliente",
		z."IdClasificacionTipoInstrumento"										as "IdClasificacionTipoInstrumento",
		z."IdTipoInstrumento"													as "IdTipoInstrumento",
		z."IdInstrumento"														as "IdInstrumento",
		z."Nemotecnico"															as "Nemo",
		0																		as "IdInstrumentoNuevo",
		''																		as "NemoNuevo",
		TO_DATE(:FechaPago, 'YYYY-MM-DD') 										as "FechaCierre",								--	"FechaCierre"
		TO_DATE(:FechaPago, 'YYYY-MM-DD') 										as "FechaPago",									--	"FechaPago"
		SUM(z."EGRCantidad")													as "CantidadDerecho",							-- es la sumatorioa de este campo
		'TRA'																	as "TipoCartera",								-- tipo de cartera
		''																		as "IdConceptoGarantia",						--	"IdConceptoGarantia"
		''																		as "IdTipoOperacionGarantia",					--	"IdTipoOperacionGarantia"
		IFNULL(FLOOR(SUM(IFNULL(z."EGRCantidad",0)) * :p_Factor),0)				as "CantidadNueva",								-- "CantidadNueva"
		0 																		as "MontoCapital",								--	"MontoCapital"
		0																		as "MontoInteres",								--	"MontoInteres"
		0																		as "Total",										-- se multiplica por el factor
		COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0)							as "IdEmpleadoEjecutivoPortafolio",
		COALESCE(EJE."CodigoEmpleado",'')										as "CodigoUsuarioAgente",						--	"CodigoUsuarioAgente"
		COALESCE(EJE."NombreCompletoEjecutivo",'')								as "NombreAgente",								--	"NombreAgente"
		COALESCE(EJE."CodigoSucursal",'')										as "CodigoSucursal",					--	"CodigoSucursal"
		Current_UTCTIMESTAMP 													as "FechaHoraIngresoRegistro",					--	"FechaHoraIngresoRegistro"
		Current_UTCTIMESTAMP 													as "FechaHoraUltimaActualizacion",				--	"FechaHoraUltimaActualizacion"
		z."IdBoveda"															as "IdBoveda",
		z."IdBolsa"																as "IdBolsa",
		'SIS'																	as "Origen"
FROM "BSCL.Custodia.BD::CUS.Cartera" z
	 INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarAsignacion" x
	 	ON z."EGRIdOperacion" = x."IdOrden"
	 INNER JOIN "BSCL.Orden.BD.Vistas::ConsultarTransaccion" y
	 	ON x."IdTransaccion" = y."IdTransaccion"
	 INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CNT.ConsultarContraparte" w
	 	ON y."IdCorredorContraparte" = w."IdContraparte"
	 LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"	AS EJE
		ON (z."IdPortafolio" = EJE."IdPortafolio")
WHERE z."IdInstrumento" IN (:IdInstrumento)
  AND z."IdEstado" != 'ANU'
  AND z."EGRFechaLiquidacion" > :EGRFechaLiquidacion
  AND x."IndicadorOrdenDirecta" = ''
GROUP BY	z."IdBolsa",
			w."IdSocioDeNegocio",
			w."TipoDocumentoIdentidad",
			w."DocumentoIdentidad",
			w."NombreCompleto",
			z."IdClasificacionTipoInstrumento",
			z."IdTipoInstrumento",
			z."IdInstrumento",
			z."Nemotecnico",
			COALESCE(EJE."IdEmpleadoEjecutivoPortafolio",0),
			COALESCE(EJE."CodigoEmpleado",''),
			COALESCE(EJE."NombreCompletoEjecutivo",''),
			COALESCE(EJE."CodigoSucursal",''),
			z."IdBoveda";