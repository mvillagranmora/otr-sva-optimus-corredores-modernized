set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Origen";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Origen" (
    "IdEmpresa",
    "ID",
    "IdDocumento",
    "VboUsuario",
    "Observaciones",
    "FuenteDatos",
    "Numero",
    "RutCliente",
	"TaxID",
	"RutClienteB",
	"TaxIDB",
	"CodigoPaisA",
    "CodigoPaisB",
	"AcuerdoMarcoTipo",
	"AcuerdoMarcoNumero",
	"AcuerdoMarcoFechaSuscripcion",
	"ContratoNumero",
	"ContratoFechaSuscripcion",
	"ContratoTipo",
	"ContratoNombreInstrumento",
	"FechaSuscripcion",
	"IdCliente",
	"IdPortafolio"
) as
SELECT a."IdEmpresa" as "IdEmpresa",
       0 as "ID",
       1 as "IdDocumento", --modificar
       'N' as "VboUsuario", --modificar
       '' as "Observaciones", --modificar
       '' as "FuenteDatos", --modificar
       1 as "Numero", --modificar
       a."RutCliente" as "RutCliente",
       CASE b."TipoDocumentoIdentidad" WHEN 'TAX' THEN b."DocumentoIdentidad" ELSE '' END as "TaxID", --de donde lo saco?
       '' as "RutClienteB",
       0 as "TaxIDB",
       COALESCE(HEVT."ValorDestino",0) as "CodigoPaisA",
       0 as "CodigoPaisB",
	   1 as "AcuerdoMarcoTipo",
	   (ltrim(rtrim(substring(b."DocumentoIdentidad",1,LENGTH(b."DocumentoIdentidad")-2)))) as "AcuerdoMarcoNumero",
	   a."FechaOperacion" as "AcuerdoMarcoFechaSuscripcion", --lo entregaran despues ya que aun no se guarda
	   a."FolioOperacion" as "ContratoNumero",
	   a."FechaOperacion" as "ContratoFechaSuscripcion",
	   1 as "ContratoTipo",
	   '' as "ContratoNombreInstrumento",
	   a."FechaOperacion" as "FechaSuscripcion",
	   a."IdCliente" as "IdCliente",
	   a."IdPortafolio" as "IdPortafolio"
FROM "BSCL.Mesa.BD.Vistas::FWD.ConsultarMovimientosFWD" a
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" b on (b."IdCliente" = a."IdCliente")
INNER JOIN "BSCL.Plataforma.BD.Vistas::REG.ConsultarPais" PAIS ON PAIS."IdPais"=b."IdPaisResidencia"
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" REL on (REL."IdCliente" = a."IdCliente" and REL."IdUso"='IND' AND REL."CodigoTipoIndicador"='DJ1820')
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HEVT ON (HEVT."Esquema"='DJ1839' AND PAIS."CodigoPais"=HEVT."ValorOrigen" AND HEVT."Tipo"='PAISCONTRA' AND a."FechaOperacion" BETWEEN TO_DATE(HEVT."FechaDatosDesde") AND TO_DATE(HEVT."FechaDatosHasta"))
LEFT OUTER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarAnexo" ANE ON (ANE."IdCliente"=a."IdCliente" AND ANE."IdTipoDocumento"='CONTRATOCONDICIONESGRALES' AND  ANE."IdEstado"='ACT')
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1839.Origen" to bcs_desarrollador with grant option;
