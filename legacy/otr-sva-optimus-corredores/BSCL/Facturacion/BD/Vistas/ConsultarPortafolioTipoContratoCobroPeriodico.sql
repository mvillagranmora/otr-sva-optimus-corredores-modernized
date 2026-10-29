SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPortafolioTipoContratoCobroPeriodico"; 
 
CREATE VIEW "BSCL.Facturacion.BD.Vistas::ConsultarPortafolioTipoContratoCobroPeriodico" As

SELECT ConsCli."IdCliente",
       ConsIndPor."IdPortafolio",
       ConsCli."DocumentoIdentidad", 
       Case
         When Length(ConsCli."NombreCompleto") > 0 Then "NombreCompleto"
       Else ConsCli."RazonSocial"  
       End As "Nombre",
       ConsIndPor."ValorIndicador" As "ValorContratoCobroPeriodico",
       ConsIndPor."CodigoValorIndicador" As "CodigoContratoCobroPeriodico"       
FROM "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" As ConsCli
 Inner Join "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" As ConsIndPor
  On ConsIndPor."IdCliente" =  ConsCli."IdCliente"
 And ConsIndPor."CodigoTipoIndicador" = 'FAC_MODFAC';
 
GRANT SELECT ON "BSCL.Facturacion.BD.Vistas::ConsultarPortafolioTipoContratoCobroPeriodico" TO  BCS_DESARROLLADOR WITH GRANT OPTION;     
 
 
