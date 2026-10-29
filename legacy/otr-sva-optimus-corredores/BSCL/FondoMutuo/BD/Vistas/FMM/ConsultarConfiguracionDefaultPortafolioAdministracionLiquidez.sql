SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionDefaultPortafolioAdministracionLiquidez";

CREATE VIEW "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionDefaultPortafolioAdministracionLiquidez"
AS
SELECT PORT.* 
       ,"Configuracion"."IdMoneda"
       ,"Configuracion"."MontoMinimo"
       ,"Configuracion"."IdSerieFondo" FROM "BSCL.FondoMutuo.BD::FMM.ConfiguracionAdministracionLiquidez" AS "Configuracion", (
	SELECT  "ConsultarPortafolio"."CodigoPortafolio"	
		   ,"ConsultarPortafolio"."DescripcionPortafolio"	
		   ,"ConsultarPortafolio"."Proposito" 	
		   ,"ConsultarPortafolio"."IdPortafolio"
		   ,"ConsultarCliente"."RazonSocial"  
		   ,"ConsultarCliente"."NombreCompleto"	
		   ,"ConsultarCliente"."DocumentoIdentidad"	
		   ,"ConsultarCliente"."IdPersona"	
	       ,"SocioNegocio"."IdSocioDeNegocio"  
	       ,"ConsultarCliente"."IdCliente"
	       ,"ConsultarCliente"."IdEmpresa"	
    FROM "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" AS "ConsultarPortafolio"
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "ConsultarCliente" ON  ( "ConsultarCliente"."IdCliente" = "ConsultarPortafolio"."IdCliente" )
	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" AS "INDICADOR" ON ("INDICADOR"."IdCliente" = "ConsultarPortafolio"."IdCliente" 
					AND "INDICADOR"."CodigoTipoIndicador" = 'CNTADMLIQ' 
					AND "INDICADOR"."ValorIndicador" = 'S')
	INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio" AS "INDICADOR_PORTAFOLIO" ON ("INDICADOR_PORTAFOLIO"."IdPortafolio" = "ConsultarPortafolio"."IdPortafolio" 
											  												AND "INDICADOR_PORTAFOLIO"."CodigoTipoIndicador" = 'FFMM_MMAUT' 
											  												AND "INDICADOR_PORTAFOLIO"."ValorIndicador" = 'S')
	INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS "SocioNegocio" ON  ( "SocioNegocio"."IdCliente" = "ConsultarCliente"."IdCliente" )
) AS "PORT";

GRANT SELECT ON "BSCL.FondoMutuo.BD.Vistas::FMM.ConsultarConfiguracionDefaultPortafolioAdministracionLiquidez" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
