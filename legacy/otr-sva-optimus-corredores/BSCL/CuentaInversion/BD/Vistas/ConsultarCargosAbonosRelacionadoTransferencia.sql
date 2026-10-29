SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonosRelacionadoTransferencia" ;

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonosRelacionadoTransferencia" AS 	 
SELECT
		 cargoabonomov."IdCargoAbonoMovimiento" ,
		 cuentainversion."IdCliente" 				AS "IdClienteRelacionado",
		 ident."DocumentoIdentidad"				    AS "DocumentoIdentidadRelacionado",
		 pernom."NombreCompleto" 					AS "NombreClienteRelacionado",
		 cuentainversion."IdPortafolio" 			AS "IdPortafolioRelacionado",
		 portafolio."CodigoPortafolio"				AS "CodigoPortafolioRelacionado",		 
		 portafolio."DescripcionPortafolio"			AS "DescripcionPortafolioRelacionado",
		 portafolio."Proposito"						AS "PropositoRelacionado"
   FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS cargoabonomov
  LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS cuentainversion ON  cuentainversion."IdCuentaInversion" = cargoabonomov."IdCuentaInversion"
  LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS cliente ON cliente."IdCliente" = cuentainversion."IdCliente"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS persona ON persona."IdPersona" = cliente."IdPersona"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS ident ON ident."IdDocumentoIdentidad" = persona."IdDocumentoIdentidad"
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" AS pernom ON pernom."IdPersona" = persona."IdPersona"
  LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" as portafolio ON portafolio."IdPortafolio" = cuentainversion."IdPortafolio" 
  WHERE cliente."Borrado"='N' AND persona."Borrado"='N' AND ident."Borrado"='N' 
WITH READ ONLY;	 	

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarCargosAbonosRelacionadoTransferencia" TO  BCS_DESARROLLADOR WITH GRANT OPTION;