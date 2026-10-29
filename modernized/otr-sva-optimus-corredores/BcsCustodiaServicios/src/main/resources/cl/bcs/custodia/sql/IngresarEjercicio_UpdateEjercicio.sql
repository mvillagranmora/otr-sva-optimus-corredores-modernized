UPDATE "BSCL.Custodia.BD::DER.OpcionesEjercicioBloqueo"
SET 	
	"IdCartera"=:IdCartera, 
	"Bloqueado"=:Bloqueado, 
	"ContratosEjercidos"=:ContratosEjercidos, 
	"ContratosSorteados"=:ContratosSorteados, 
	"FechaHora"=:FechaHora, 
	"IdEstado"=:IdEstado, 
	"IdEstadoCCLV"=:IdEstadoCCLV, 
	"DescripcionEstadoCCLV"=:DescripcionEstadoCCLV, 
	"IdCliente"=:IdCliente, 
	"IdPortafolio"=:IdPortafolio, 
	"IdEmpresa"=:IdEmpresa
WHERE "IdEjercicio" = :IdEjercicio;