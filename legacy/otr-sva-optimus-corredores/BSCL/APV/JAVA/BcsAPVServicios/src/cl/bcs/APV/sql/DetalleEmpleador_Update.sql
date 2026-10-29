UPDATE "BSCL.APV.BD::REC.Empleador"
SET 
		"IdRecaudacion"				= :IdRecaudacion
		,"IdPersona"				= :IdPersona
		,"FechaIngreso"				= :FechaIngreso
		,"PeriodoPago"				= :PeriodoPago
		,"NumeroHojas"				= :NumeroHojas
		,"TipoAfiliado"				= :TipoAfiliado
		,"TipoPago"					= :TipoPago
		,"Insistencias"				= :Insistencias
		,"MontoDepositoConvenido"	= :MontoDepositoConvenido
		,"MontoDepositoAPV"			= :MontoDepositoAPV
		,"Reajustes"				= :Reajustes
		,"Intereses"				= :Intereses
		,"Origen"					= :Origen
		,"Observaciones"			= :Observaciones
		,"IdEstado"					= :IdEstado
WHERE	"IdRecaudacionEmpleador" 	= :IdRecaudacionEmpleador
AND 	"IdEmpresa"					= :IdEmpresa;