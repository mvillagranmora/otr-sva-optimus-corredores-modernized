SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento"; 

CREATE VIEW "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento"  as
SELECT 
"IdTipoEvento",
"Nombre", 
"HorasVigencia", 
"EtapasSecuenciales", 
"IdUsuarioSuperAutorizador", 
"IdRolSuperAutorizador", 
"IdTipoProducto", 
"IdTipoOperacion", 
"IdEstado", 
"Codigo", 
"IdEmpresa",
"IdModulo"
--"RutaServicioConfirmar",
--"RutaServicioRechazar",
--"ModoParametros"
FROM "BSCL.Plataforma.BD::AUT.TipoEvento";


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::AUT.ConsultarTipoEvento" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
