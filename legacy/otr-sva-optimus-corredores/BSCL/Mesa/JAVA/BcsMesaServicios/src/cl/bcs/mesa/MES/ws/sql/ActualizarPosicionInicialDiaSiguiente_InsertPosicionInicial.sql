INSERT INTO "BSCL.Mesa.BD::SPT.PosicionInicialMesa"(
	"IdPosicionInicial"
	,"IdEmpresa"
	,"Fecha"
	,"IdMoneda"
	,"TipoCambioInicial"
	,"CierreIngresado"
	,"PosicionInicial"
	,"TipoInstrumento"
	,"IdUsuario"
	,"Estado"
	,"ResultadoPosicionInicial"
)
VALUES(
	 "BSCL.Mesa.BD.Secuencias::IdComprobantePagoOperacion".NEXTVAL as "IdPosicionInicial",
	,:IdEmpresa
	,:Fecha
	,:IdMoneda
	,:TipoCambioInicial
	,:CierreIngresado
	,:PosicionInicial
	,:TipoInstrumento
	,:IdUsuario
	,:Estado
	,:ResultadoPosicionInicial
);