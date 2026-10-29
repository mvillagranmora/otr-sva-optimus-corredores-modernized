INSERT INTO "BSCL.Custodia.BD::CUS.EventoCapitalCliente"
(
		"IdEventoCapitalCliente",			
		"IdEventoCapital",			
		"IdTipoEventoCapital",		
		"Secuencia",			
		"IdSocioNegocio",					
		"TipoSocioNegocio",		
		"IdPortafolio",				
		"IdTipoDocumentoIdentidad",	
		"DocumentoIdentidad",	
		"NombreCliente",				
		"IdClasificacionTipoInstrumento",	
		"IdTipoInstrumento",		
		"IdInstrumento",			
		"Nemo",					
		"IdInstrumentoNuevo",
		"NemoNuevo",						
		"FechaCierre",						
		"FechaPago",
		"CantidadDerecho",		
		"Precio",
		"TipoCartera",						
		"CantidadNueva",
		"PrecioNuevo",
		"MontoCapital",						
		"MontoInteres",				
		"Total",				
		"IdMoneda",						
		"SecuenciaCupon",	
		"IdEmpleadoEjecutivoPortafolio",				
		"CodigoUsuarioAgente",				
		"NombreAgente",				
		"CodigoSucursal",
		"FechaHoraIngresoRegistro",
		"FechaHoraUltimaActualizacion",		
		"IdUsuario",						
		"IdEstado",
		"IdBoveda",
		"IdBolsa",
		"IdConceptoGarantia",
		"IdTipoOperacionGarantia",
		"MontoReajuste",
		"Origen",
		"IdTipoReajuste",
		"INGMontoInteresCorteCupon",
		"NumeroDividendo",
		"IdCuentaCustodioPortafolio",
		"MontoRetencion"
)
VALUES
(
	:IdEventoCapitalCliente,
	:IdEventoCapital,
	:IdTipoEventoCapital,  -- PB 		
	:Secuencia,
	:IdSocioDeNegocio,		
	'CLI',   -- TipoSocioNegocio  		
	:IdPortafolio,
	:TipoDocumentoIdentidad,
	:DocumentoIdentidad,
	:RazonSocial,
	:IdClasificacionTipoInstrumento, 
	:IdTipoInstrumento,
	:IdInstrumento,
	:Nemo,
	0,  		 -- IdInstrumentoNuevo
	'', 		 -- NemoNuevo
	:FechaPago , -- FechaCierre
	:FechaPago , -- FechaPago
	:Cantidad,	 -- CantidadDerecho
	:Precio,
	'CUS',		 -- TipoCartera
	:CantidadNueva, 
	:PrecioNuevo,
	:MontoCapital,		    --	CAR."INGMontoCapitalCorteCupon"  -- no lo toma de la cartera, sinó del formulario de ingreso ya que puede ser parcial			
	:MontoInteres,		    --	CAR."INGMontoInteresCorteCupon"	 -- no lo toma de la cartera, sinó del formulario de ingreso ya que puede ser parcial		
	:Total,				    --	CAR."INGMontoCorteCupon"         -- no lo toma de la cartera, sinó del formulario de ingreso ya que puede ser parcial
	:INGIdMonedaPagoCupon,   -- IdMoneda
	:INGNumeroCupon,		-- SecuenciaCupon 			
	:IdEmpleadoEjecutivoPortafolio,		
	:CodigoEmpleado,
	:NombreCompletoEjecutivo,
	:CodigoSucursal,
	CURRENT_UTCTIMESTAMP,		-- FechaHoraIngresoRegistro
	CURRENT_UTCTIMESTAMP,		-- FechaHoraUltimaActualizacion		
	:IdUsuario,
	'VIG', 						-- IdEstado
	:IdBoveda,
	:IdBolsa,
	'', 						-- IdConceptoGarantia
	'', 						-- IdTipoOperacionGarantia
	:MontoReajuste,	--		CAR."INGMontoReajusteCorteCupon"		 -- no lo toma de la cartera, sinó del formulario de ingreso ya que puede ser parcial
	'MAN', -- Origen
	:IdTipoReajuste,
	:MontoInteres,	--		CAR."INGMontoInteresCorteCupon" 		-- no lo toma de la cartera, sinó del formulario de ingreso ya que puede ser parcial
	:NumeroDividendo,
	:IdCuentaCustodioPortafolio,
	:MontoRetencion
);




