SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Invalidos" (
    "CodigoValidacion",
	"ID",
	"IdDocumento",
	"VboUsuario",
	"Observaciones",
	"FuenteDatos",
	"EjecutadoComplementoIPC",
	"Numero",
	"TipoCartera",
	"RutCliente",
	"IdCliente",
	"FechaOperacion",
	"RutInstitucion",
	"SalAhorroNetoEjerNEGT1",
	"SalArrastreEjerSIGT1",
	"SalAhorroNetoEjerT2",
	"SalArrastreEjerSigNegT2",
	"MtoRetiro57BisT2",
	"SalAhorroNetoEjeNegT3",
	"SalArrastreEjeSigNegT3",
	"MtoRetiroAnoT3",
	"MtoRenRetFFMMPOS",
	"MtoRenRetFFMMNEG",
	"MtoRenRetOTROSPOS",
	"MtoRenRetOTROSNEG",
	"NumeroCertificado"
) AS
SELECT
    b."CodigoValidacion",
	a."ID",
	a."IdDocumento",
	a."VboUsuario",
	a."Observaciones",
	a."FuenteDatos",
	a."EjecutadoComplementoIPC",
	a."Numero",
	a."TipoCartera",
	a."RutCliente",
	a."IdCliente",
	a."FechaOperacion",
	a."RutInstitucion",
	a."SalAhorroNetoEjerNEGT1",
	a."SalArrastreEjerSIGT1",
	a."SalAhorroNetoEjerT2",
	a."SalArrastreEjerSigNegT2",
	a."MtoRetiro57BisT2",
	a."SalAhorroNetoEjeNegT3",
	a."SalArrastreEjeSigNegT3",
	a."MtoRetiroAnoT3",
	a."MtoRenRetFFMMPOS",
	a."MtoRenRetFFMMNEG",
	a."MtoRenRetOTROSPOS",
	a."MtoRenRetOTROSNEG",
	a."NumeroCertificado"
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1944.Invalidos" to bcs_desarrollador with grant option;