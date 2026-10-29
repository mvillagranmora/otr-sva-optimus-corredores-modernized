INSERT INTO "BSCL.APV.BD::TRA.OrdenTraspaso"(
	 "IdOrdenTraspaso"			--BIGINT	NOT NULL,
	,"IdTraspaso"				--BIGINT	NOT NULL,
	,"IdOrden"					--BIGINT,
	,"IdSolicitudAporteRescate"  --BIGINT,
)
VALUES(
 	 "BSCL.APV.BD.Secuencia::TRA.IdOrdenTraspaso".NEXTVAL
	,:IdTraspaso
	,:IdOrden
	,:IdSolicitudAporteRescate
);