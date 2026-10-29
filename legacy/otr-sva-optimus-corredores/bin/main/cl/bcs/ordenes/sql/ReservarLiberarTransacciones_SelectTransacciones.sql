SELECT
	TRA."IdTransaccion",
	TRA."Reservado",
	TRA."Bloqueado",
	TRA."Nemotecnico",
	TRA."IdTipoOperacion",
	TRA."Folio",
	TRA."Monto",
	TRA."Cantidad",
	TRA."IdBolsa",

	TOPE."AccionOrden",
	TRA."IdGrupoReserva",
	TRA."IdUsuarioReserva"
	
FROM "BSCL.Orden.BD::ORD.Transaccion" AS TRA
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" TOPE ON TOPE."IdTipoOperacion" = TRA."IdTipoOperacion"

WHERE TRA."IdEmpresa" = :IdEmpresa
AND TRA."IdTransaccion" IN (:IdsTransaccion);
