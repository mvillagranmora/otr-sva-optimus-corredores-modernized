
INSERT INTO "BSCL.Plataforma.BD::EVT.EventoDetalle"
(
	"IdEventoDetalle",
	"IdEvento",
	"Llave",
	"Valor",
	"ValorBlob"
) 
values
("BSCL.Plataforma.BD.Secuencias::EVT.EventoDetalle".nextVal, :IdEvento,:Llave,:Valor,:ValorBlob)




