UPDATE "BSCL.APV.BD::INV.Bonificacion"
SET 
		"MontoCalculado"			= :MontoCalculado
		,"FechaCalculado"			= CURRENT_UTCTIMESTAMP
		,"PorcentajeEgreso"			= :PorcentajeEgreso
		,"PorcentajeTraspasoEgreso" = :PorcentajeTraspasoEgreso
		,"MontoEgreso"				= :MontoEgreso
		,"MontoTraspasoEgreso"		= :MontoTraspasoEgreso
		,"MontoTraspasoDevolucion"	= :MontoTraspasoDevolucion
		,"MontoEgresoDevolucion"	= :MontoEgresoDevolucion
		,"IdEstado"					= 'PRO'
WHERE	"IdBonificacion" 			= :IdBonificacion
AND 	"IdEmpresa"					= :IdEmpresa;



