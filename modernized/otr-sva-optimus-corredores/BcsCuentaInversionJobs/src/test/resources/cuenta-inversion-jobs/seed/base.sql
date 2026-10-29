-- baseline of every case: one DUMMY row, the program row of the run (IdPrograma 900) with visible placeholder values (so an update that sets
-- a value, or NULL, is visible) and the program type ACTUALIZAR_SALDO_CARGO_ABONO in the free state; a case seed may DELETE or UPDATE them.
INSERT INTO DUMMY VALUES ('X');
INSERT INTO "BSCL.Plataforma.BD::BAT.Programa" ("IdPrograma", "RutaSalida", "RutaLog", "Comentario") VALUES (900, 'SIN-RUTA', 'SIN-LOG', 'SIN-COMENTARIO');
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoProgramaEjecucion" ("IdTipoPrograma", "IdEstado") VALUES ('ACTUALIZAR_SALDO_CARGO_ABONO', 'LIBRE');
