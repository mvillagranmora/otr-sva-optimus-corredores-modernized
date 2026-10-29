INSERT INTO "BSCL.FondoMutuo.BD::TRA.TraspasoDetalleOrigen"(
     "IdDetalleTraspaso"
	,"IdTraspaso"
	,"IdSerieFondo"
	,"Cantidad"
	,"IdEstado"
	,"FechaIngreso" 
    ,"FechaIngresoRegistro"
)
VALUES(
	"BSCL.FondoMutuo.BD.Secuencias::TraspasoDetalleOrigen".NEXTVAL
	,:IdTraspaso
	,:IdSerieFondo
	,:Cantidad
	,:IdEstado
	,:FechaIngreso
	,CURRENT_UTCTIMESTAMP
);  