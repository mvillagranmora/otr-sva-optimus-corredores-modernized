INSERT INTO "BSCL.SocioNegocio.BD::ENR.PreferenciaPagoPortafolio"(
	"IdPreferenciaPagoPortafolio",
	"IdEnrolamiento",
	"IdPreferenciaPago",
	"IdPersonaRelacionada",
	"IdCuentaBancaria",
	"IdEmpresa",	
	"IdTarjetaBancaria",	
	"PorDefecto"
)
SELECT
	"BSCL.SocioNegocio.BD.Secuencias::BorradorPreferenciaPagoPortafolio".NEXTVAL,
	ENR."IdEnrolamiento",
	PRE."IdPreferenciaPago",
	:IdPersonaRelacionada,
	:IdCuentaBancaria,
	:IdEmpresa,
	0,
	'S'
FROM "BSCL.SocioNegocio.BD::ENR.Enrolamiento" ENR,
	 "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" PRE
WHERE ENR."IdEnrolamiento" IN (:IdEnrolamiento)
  AND PRE."IdMedioDePago" = 'TRA'
  AND PRE."IdMoneda" IN (:IdMoneda)
  AND PRE."RecibePagaPortafolio" = 'R'
  AND PRE."IdTipoProducto" NOT IN ('MERF','MEFW')
  AND PRE."IdEmpresa" IN (:IdEmpresa);