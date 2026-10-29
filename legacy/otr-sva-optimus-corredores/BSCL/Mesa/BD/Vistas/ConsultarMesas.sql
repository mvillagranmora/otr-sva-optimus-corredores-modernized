SET SCHEMA "BSCL";
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarMesas";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarMesas" AS
SELECT
	 MES."IdMesa"
    ,MES."IdEmpresa" 			
	,MES."IdCliente" 			
	,MES."IdPortafolio" 			
	,coalesce(CLI."DocumentoIdentidad",'') as "DocumentoIdentidad"
	,MES."Codigo"				
	,MES."Nombre" 				
	,MES."IdTipoProducto" 		
	,MES."IdEstadoMesa"			
	,MES."IdEstadoRegistro"		
	,MES."IdUsuarioModificacion" 	
	,MES."FechaModificacion"
	,MES."PorDefecto"
	,MES."ValorRazonable"
	,IFNULL(POR."CodigoPropositoPortafolio",' - ') AS "CodigoPortafolio"
	,MES."CodigoIFRS"
	,CASE MES."IdTipoProducto" 
                        WHEN 'MEME' THEN 'MSP'
                        WHEN 'MEFW' THEN 'MFW'
                        WHEN 'MERF' THEN 'MRF'
     	END AS "CodigoModulo"
    ,MES."AplicaFusion"
FROM "BSCL.Mesa.BD::MES.Mesa" MES
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI ON (CLI."IdCliente" = MES."IdCliente")
	LEFT JOIN  "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" POR ON (POR."IdPortafolio" = MES."IdPortafolio");
GO

GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarMesas" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO
