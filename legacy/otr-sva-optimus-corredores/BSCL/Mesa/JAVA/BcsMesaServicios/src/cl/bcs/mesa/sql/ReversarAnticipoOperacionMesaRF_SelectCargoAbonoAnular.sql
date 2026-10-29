SELECT
	ConsCarAbn."IdCargoAbonoMovimiento" , 
	'ANULACION OPERACION MESA RF/IF' AS "GlosaAnulacion"
FROM
	"BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" ConsCarAbn
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" Ope ON Ope."IdOperacion" = ConsCarAbn."IdOperacion"	
WHERE
	"IdConcepto" = 'ANPCME'
	AND "IdEstadoEvento" IS NULL
	AND "IdEstado" != 'ANU';