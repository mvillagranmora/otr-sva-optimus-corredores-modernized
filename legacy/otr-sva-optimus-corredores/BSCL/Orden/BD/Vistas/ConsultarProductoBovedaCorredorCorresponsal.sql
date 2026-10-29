SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Orden.BD.Vistas::ConsultarProductoBovedaCorredorCorresponsal"; 
GO		
CREATE VIEW "BSCL.Orden.BD.Vistas::ConsultarProductoBovedaCorredorCorresponsal"  
AS
SELECT DISTINCT Corresponsal."IdCorredorCorresponsal"
	 , Corresponsal."IdPais"
	 , Corresponsal."IdContraparte"
	 , Corresponsal."IdPersona"
	 , Corresponsal."IdSocioDeNegocio"
     , Corresponsal."CodigoCorredor"
     , Corresponsal."IdEstado"
     , DetallePerfilCobros."IdTipoProducto"
     , DetallePerfilCobros."NombreTipoProducto"
     , Boveda."IdBoveda"
     , Boveda."Codigo"
     , Boveda."Nombre"
     , Boveda."IdEstado" AS "IdEstadoBoveda"
     , DetallePerfilCobros."IdPerfilCobros"      
FROM "BSCL.Orden.BD::ORD.CorredorCorresponsal" AS Corresponsal
INNER JOIN "BSCL.Plataforma.BD.Vistas::COM_2.ConsultarDetallePerfilCobros" AS DetallePerfilCobros			 
	ON DetallePerfilCobros."IdPerfilCobros" = Corresponsal."IdPerfilCobros"
LEFT OUTER JOIN "BSCL.Orden.BD::ORD.CorredorCorresponsalBoveda" AS CorresponsalBoveda
			 ON CorresponsalBoveda."IdCorredorCorresponsal" = Corresponsal."IdCorredorCorresponsal"
			AND CorresponsalBoveda."IdTipoProducto" = DetallePerfilCobros."IdTipoProducto"
LEFT OUTER JOIN "BSCL.Plataforma.BD::PLA.Boveda" AS Boveda
	ON CorresponsalBoveda."IdBoveda" = Boveda."IdBoveda"			
WHERE DetallePerfilCobros."IdBolsa" = 'OUT' OR DetallePerfilCobros."IdBolsa" IS NULL;
GO
GRANT SELECT ON "BSCL.Orden.BD.Vistas::ConsultarProductoBovedaCorredorCorresponsal" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
GO