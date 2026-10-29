SELECT 	OPE."IdOperacion"
		,OPE."IdEstado", TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
		,OPE."IdCliente"
		,OPE."IdPortafolio"
		,OPE."IdTipoOperacion"
		,OPE."PlazoEnDias"
		,OPE."IdUsuarioCreacion"
		,OPE."MontoOperacionInicial" 
		,RES."TotalDetalle"
		,PAG."TotalPagos"
FROM 	"BSCL.Mesa.BD::MES.Operacion" OPE
		LEFT JOIN (	SELECT 	Reserva."IdOperacion", 
							SUM("MontoValorizado") AS "TotalDetalle" 
					FROM 	"BSCL.Custodia.BD::CUS.Reserva" Reserva
					INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP   ON (TMP."IdOperacion" = Reserva."IdOperacion")
					GROUP BY Reserva."IdOperacion" ) RES
		ON RES."IdOperacion" = OPE."IdOperacion"
		LEFT JOIN (	SELECT 	Pagos."IdOperacion", 
							SUM("Monto") AS "TotalPagos" 
					FROM 	"BSCL.Mesa.BD::MES.ComprobantePagoOperacion" Pagos
					INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = Pagos."IdOperacion")
					GROUP BY Pagos."IdOperacion" ) PAG
		ON PAG."IdOperacion" = OPE."IdOperacion"
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = OPE."IdOperacion");

