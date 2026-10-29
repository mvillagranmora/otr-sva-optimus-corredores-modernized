SET SCHEMA "BSCL";

DROP VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Invalidos";

CREATE VIEW "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Invalidos" (
   	"CodigoValidacion",
	"ID",
	"IdDocumento",
	"VboUsuario",
	"Observaciones",
	"FuenteDatos",
	"IdCliente",
	"Numero",											
	"RutCliente",                       --C1
	"TipoInstrumento",                       --C2
	"NumeroIdentificadorInversion",                       --C3
	"FechaInversionOriginal",                               --C4
	"RetiroInversion54BISMontoNominal",                     --C8
	"RetiroInversion54BISInteresPositivo",                     --C9
	"RetiroInversion54BISInteresNegativo",                     --C10
	"NumeroCertificado"                
) AS
SELECT
   	b."CodigoValidacion",
	a."ID",
	a."IdDocumento",
	a."VboUsuario",
	a."Observaciones",
	a."FuenteDatos",
	a."IdCliente",
	a."Numero",											
	a."RutCliente",                       --C1
	a."TipoInstrumento",                       --C2
	a."NumeroIdentificadorInversion",                       --C3
	a."FechaInversionOriginal",                               --C4
	a."RetiroInversion54BISMontoNominal",                     --C8
	a."RetiroInversion54BISInteresPositivo",                     --C9
	a."RetiroInversion54BISInteresNegativo",                     --C10
	a."NumeroCertificado"                
FROM "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Datos" a
INNER JOIN "BSCL.Normativa.BD::TRI.ResumenValidacion" b ON (b."IdDocumento" = a."IdDocumento")
INNER JOIN "BSCL.Normativa.BD::TRI.DetalleValidacion" c ON (c."IdResumenValidacion" = b."ID")
WHERE a."ID" = c."IdDetalleDocumento"
with read only;
    
grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1914.Invalidos" to bcs_desarrollador with grant option;
