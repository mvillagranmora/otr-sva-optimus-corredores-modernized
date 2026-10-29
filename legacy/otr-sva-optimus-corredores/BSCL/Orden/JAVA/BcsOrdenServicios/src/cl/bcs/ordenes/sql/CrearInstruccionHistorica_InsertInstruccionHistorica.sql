INSERT INTO "BSCL.Orden.BD::ORD.InstruccionHistorica"
("IdInstruccionHistoria", "IdInstruccion", "IdEstadoInstruccion", "IdMotivo", "FechaIngreso")
VALUES(:IdInstruccionHistorica, :IdInstruccion, :IdEstadoInstruccion, :IdMotivo, CURRENT_TIMESTAMP);

		