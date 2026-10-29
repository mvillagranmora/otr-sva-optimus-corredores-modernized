INSERT INTO "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleDestino"(
     "IdDetalleTraspaso"
	,"IdTraspaso"
	,"IdSerieFondo"
	,"Porcentaje"
	,"IdEstado"
    ,"FechaIngreso"
    ,"FechaIngresoRegistro"
)
VALUES(
	"BSCL.FondoMutuo.BD.Secuencias::TraspasoDetalleDestino".NEXTVAL
	,:IdTraspaso
	,:IdSerieFondo
	,:Porcentaje
	,:IdEstado
	,:FechaIngreso
	,CURRENT_UTCTIMESTAMP
);  