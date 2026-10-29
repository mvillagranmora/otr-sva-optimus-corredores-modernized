INSERT INTO "BSCL.Plataforma.BD::INS.RegistroPrecioBolsaInstrumento"(
	"IdInstrumento"
	,"FechaSistema"
	,"IdEmpresa"                                                                        
	,"Precio"   
	,"Registro"
	,"CodigoFuente"
)

VALUES(
	:IdInstrumento
	,:FechaSistema
	,:IdEmpresa
	,:Precio
	,LEFT(NOW(),25)
	,:CodigoFuente
)
