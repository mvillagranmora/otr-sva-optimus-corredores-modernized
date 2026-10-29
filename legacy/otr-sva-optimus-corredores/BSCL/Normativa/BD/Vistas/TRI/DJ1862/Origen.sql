set schema "BSCL";

drop view "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Origen";

create view "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Origen" (
	"IdEmpresa",
    "ID",
	"IdDocumento",
	"VboUsuario",
	"Observaciones",
	"FuenteDatos",
	"RutCliente",
	"IdCliente",
	"IdPortafolio",
	"Numero",
	"TaxID",
	"PaisOtorganteIdentificador",
	"Nombre",
	"CodigoMoneda",
	"MontoEnPesos",
	"NumeroTransaccion",
	"CodigoModalidadOperacion",
	"CodigoConcepto",
	"FechaOperacion",
	"NombreContraparte",
	"TaxIDContraparte",
	"CodigoPaisContraparte",
	"BancoContraparte",
	"TipoDocumento",
	"NumeroDocumento",
	"TipoTransaccion",
	"IdTipoOperacion",
	"IdTipoProducto",
	"IdComprobante"
) as
select
    caab."IdEmpresa" AS "IdEmpresa"
    ,0 AS "ID"
    ,0 AS "IdDocumento"
    ,'N' AS "VboUsuario"
    ,'' AS "Observaciones"
    ,'' AS "FuenteDatos"
	,lpad(caab."DocumentoIdentidad",11, ' ') AS "RutCliente"
	,caab."IdCliente" AS "IdCliente"
	,caab."IdPortafolio" AS "IdPortafolio"
	,0 AS "Numero"
	,CASE CLIENTE."TipoDocumentoIdentidad" WHEN 'TAX' THEN CLIENTE."DocumentoIdentidad" ELSE '' END AS "TaxID"
	,COALESCE(HPAIS."ValorDestino", '') AS "PaisOtorganteIdentificador"
	,upper(caab."NombreCliente")  AS "Nombre"
	,13 AS "CodigoMoneda"
	,COALESCE (round(caab."Monto" * us."Valor",0),0) AS "MontoEnPesos"
	,COALESCE(caab."IdComprobante",0) AS "NumeroTransaccion"
	,'SW' AS "CodigoModalidadOperacion" 
	,CASE WHEN caab."IdConcepto" = 'ABREPS' THEN 11260 ELSE 21260 END AS "CodigoConcepto"
	,caab."FechaSistema" AS "FechaOperacion"
	,'PERSHING LLC' AS "NombreContraparte"
	,'0-0' AS "TaxIDContraparte"
	,'225' AS "CodigoPaisContraparte"
	,'BANK OF NEW YORK MELLON' AS "BancoContraparte"
	,'' AS "TipoDocumento"
	,0 AS "NumeroDocumento"
	,'' AS "TipoTransaccion"
	,'' AS "IdTipoOperacion"
	,'' AS "IdTipoProducto"
	,caab."IdComprobante" AS "IdComprobante"
FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonos" caab 
LEFT JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" us ON (us."Fecha"= caab."FechaSistema" )  AND us."IdParametroFinanciero" = 'DO'
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLIENTE ON (CLIENTE."IdCliente" = caab."IdCliente")
LEFT OUTER JOIN "BSCL.Normativa.BD.Vistas::DOC.ConsultarHomologacion" HPAIS
		ON (HPAIS."Esquema"='DJ1862' AND TRIM(CLIENTE."IdPaisNacimiento")= TRIM(HPAIS."ValorOrigen") AND HPAIS."Tipo"='CodigoPais1862' AND "Version" = YEAR(caab."FechaSistema"))
WHERE caab."Monto" >= 10000
	AND caab."IdConcepto" IN ('CAAPPS','ABREPS')
	AND caab."IdEstado" = 'CRT'
with read only;

grant select on "BSCL.Normativa.BD.Vistas::TRI.DJ1862.Origen" to bcs_desarrollador with grant option;