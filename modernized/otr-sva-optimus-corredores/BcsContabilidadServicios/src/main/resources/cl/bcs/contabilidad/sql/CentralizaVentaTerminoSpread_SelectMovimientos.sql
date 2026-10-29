SELECT
	TO_BIGINT(:IdTipoCentralizacion)								AS "IdTipoCentralizacion",
	Reserva."IdCarteraOrigen"										AS "IdMovimiento",
	'VTATERRFIF'													AS "IdTipoMovimiento",
	Operacion."IdEmpresa"											AS "IdEmpresa",
	Reserva."FolioCarteraOrigen"									AS "Numero",
	Reserva."FolioCarteraOrigen"									AS "Linea",
	YEAR(:FechaContable) ||'-'|| 
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	RIGHT('0' || DAYOFMONTH(:FechaContable),2)						AS "Fecha",
	Operacion."DocumentoIdentidad"									AS "DocumentoIdentidad",
	Operacion."IdPortafolio"										AS "IdPortafolio",
	TO_BIGINT(YEAR(:FechaContable))									AS "Ejercicio",
	RIGHT('0' || MONTH(:FechaContable),2) ||'-'|| 
	YEAR(Operacion."FechaOperacion")								AS "Periodo",
	Reserva."MontoValorizado"										AS "Monto",
	CASE WHEN (Operacion."AfectaCustodia" = 'S')
	THEN ConsultarCartera."IdMonedaSistema" 
	ELSE Operacion."IdMonedaPrincipal" END							AS "IdMoneda",
	Operacion."IdEstado"											AS "Estado"
FROM "BSCL.Mesa.BD.Vistas::ConsultarOperaciones" Operacion
INNER JOIN "BSCL.Custodia.BD::CUS.Reserva" Reserva on Reserva."IdOperacion" = Operacion."IdOperacion"
LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" ConsultarCartera ON Reserva."IdCarteraOrigen" = ConsultarCartera."IdCartera"
WHERE Operacion."IdEmpresa"				= :IdEmpresa 
	AND	Operacion."IdEstado"         	= 'VIG'
	AND Operacion."IdTipoOperacion"    	= 'VENTER'
	AND Operacion."FechaOperacion" 		= :FechaContable