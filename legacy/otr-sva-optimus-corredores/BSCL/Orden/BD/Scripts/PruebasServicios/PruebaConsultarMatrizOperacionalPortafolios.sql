SET SCHEMA "BSCL";
drop TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT 
("IdInterno" bigint, 
"IdPortafolio" bigint, 
"IdTipoProducto" NVARCHAR(4), 
"IdTipoOperacion" NVARCHAR(6), 
"IdCanalIngreso" NVARCHAR(4),
"IdBolsa" NVARCHAR(3),
"IdCorredorCorresponsal" BIGINT);

	
INSERT INTO #TT ("IdInterno","IdPortafolio","IdBolsa","IdTipoProducto","IdTipoOperacion","IdCanalIngreso","IdCorredorCorresponsal")
SELECT 1 as "IdInterno",343710 as "IdPortafolio",'BCS' as "IdBolsa",'INAC' as "IdTipoProducto",'COMACN' as "IdTipoOperacion",'TRA' as "IdCanalIngreso",0 as "IdCorredorCorresponsal" from dummy;

CALL "BSCL.Orden.BD.Servicios::ConsultarCobrosPortafolios"(
	p_IdEmpresa => 1,
	p_IdUsuario => 4,
	p_IdModulo => 'ORD',
    p_Portafolios => #TT,
    p_out_MatrizOperacional => ?,
    p_out_AdvertenciasContador => ?,
    p_out_AdvertenciasDetalle => ?,
    p_out_ErroresContador => ?,
    p_out_ErroresDetalle => ?
);

SELECT "IdPerfilCobros","IdPerfilCobrosExtra",* FROM "BSCL"."BSCL.Portafolio.BD::POR.Portafolio" where "IdPortafolio" = 343710
IdPerfilCobros;IdPerfilCobrosExtra;IdPortafolio;CodigoPortafolio;Proposito;IdCliente;IdEstado;FechaBloqueo;ComentarioBloqueo;IdTipoBloqueo;FechaIngreso;FechaCreacion;FechaModificacion;IdCarga;IdPerfilComercialBaseOrigen;IdPerfilProductos;IdPerfilCobros;IdEstadoAutorizacion;DescripcionPortafolio;IdPerfilLimites;IdPerfilCondicionesOperativas;IdPerfilProductosExtra;IdPerfilCobrosExtra;IdPerfilLimitesExtra;IdPerfilCondicionesOperativasExtra;IdTipoDesbloqueo;FechaDesbloqueo;ComentarioDesbloqueo;IdDireccionEnvioCorrespondencia;IdTelefonoContacto;IdEmpleadoEjecutivo;EsEjecutivoExclusivo;NumeroFicha;IdNombreFantasia;Alias;IdPersonaBrokerExtranjero;IdPerfilCobrosPeru;IdPerfilCobrosPeruExtra
TODOS LOS COBROSH;EXT_343710;343.710;1;PROPGENERALES;395.174;ACT;01-01-0001 0:00:00.0;BLOQUEO;BCOBRANZA;03-06-2016 20:24:40.0;30-05-2016 20:24:40.0;24-05-2017 15:52:51.0;0;PERFIL ARRIESGADO BCS;PRODUCTOS ARRIESGADO BCS;TODOS LOS COBROSH;AUT;Portafolio Default;PLANTILLA_ARRIESGADO_001;0;EXT_343710;EXT_343710;EXT_343710;0;?;01-01-0001 0:00:00.0;112;474.799;350.814;7.298;N;0;0;RENZITO;1.335.561;ASDF;EXT_343710

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::COM.DetallePerfilCobros" where "IdMoneda" = '0'

update "BSCL"."BSCL.Plataforma.BD::COM.DetallePerfilCobros" set "IdMoneda"=  'CLP' where "IdMoneda" = '0'

update "BSCL"."BSCL.Orden.BD::ORD.Asignacion" set "IdEstadoAsignacion" = 'ANU' where "IdAsignacion" = 3726954
COMMIT

SELECT * FROM "BSCL"."BSCL.Orden.BD::ORD.CobroOrden" where "IdOrden"  = 312242
UPDATE "BSCL"."BSCL.Orden.BD::ORD.CobroOrden" SET "PermiteCambiarValor" = 'S' where "IdOrden"  = 312242 and "IdCobro" != 'IVA'
