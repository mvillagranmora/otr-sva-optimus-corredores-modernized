set schema BSCL;
drop table #p_Asignaciones;
CREATE LOCAL TEMPORARY TABLE #p_Asignaciones ("IdInterno" BIGINT,"IdMovimiento" BIGINT, "IdPortafolio" BIGINT
								, "IdTransaccion" BIGINT, "Cantidad" DECIMAL(26,8), "ValorRescate" DECIMAL(26,8));
drop table #p_CobrosModificados;
CREATE LOCAL TEMPORARY TABLE #p_CobrosModificados (		"IdInterno" 			BIGINT,
														"IdCobro" 				NVARCHAR(6),
														"ValorCobroAplicado" 	DECIMAL(26,8)
													);

[{"IdInterno":2155,"IdMovimiento":45196108,"IdPortafolio":343710,"IdTransaccion":45196108,"Cantidad":1000,"ValorRescate":0}]
[{"IdInterno":2156,"IdMovimiento":45196108,"IdPortafolio":27499,"IdTransaccion":45196108,"Cantidad":1000,"ValorRescate":0}
--update "BSCL.Facturacion.BD::FAC.Comprobante" set "IdEstadoComprobante"='FAC' where "IdComprobante"=31791;
--update "BSCL.Facturacion.BD::FAC.MovimientoComprobante" set "IdEstadoFacturacion"='FAC' where "IdMovimientoComprobante" in (33276,33277);
--update  "BSCL.Orden.BD::ORD.Asignacion" set "IdEstadoAsignacion"= 'EAF' where "IdAsignacion" in (183916,183915);
--delete from "BSCL.Orden.BD::ORD.Asignacion" where "IdAsignacion" in (183935,183936,183937);
--delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobante" where "IdCliente" in (249160,249161,249162);
--delete from "BSCL.Facturacion.BD::FAC.Comprobante" where "IdComprobante" in (31799,31800,31801);
insert into #p_Asignaciones ("Cantidad","IdInterno","IdMovimiento","IdPortafolio","IdTransaccion","ValorRescate")
select 1000 as "Cantidad",2156 as "IdInterno",45196108 as "IdMovimiento",27499 as "IdPortafolio",45196108 as "IdTransaccion",0 as "ValorRescate" from dummy;



/*
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1842,"IdPortafolio",28837,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1843,"IdPortafolio",28836,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1843,"IdPortafolio",28835,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1843,"IdPortafolio",28834,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1844,"IdPortafolio",28830,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1844,"IdPortafolio",28831,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1844,"IdPortafolio",28832,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1844,"IdPortafolio",28833,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1845,"IdPortafolio",28829,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
insert into #p_Asignaciones
select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL,1846,"IdPortafolio",28828,133333,0 from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT'  and "IdPerfilComercialBaseOrigen" != '0' and "IdPerfilProductos" != '0' and "IdPerfilProductosExtra" != '0' and "IdPerfilCobros" != '0' and "IdPerfilCobrosExtra" != '0' and "IdPortafolio" != 10000;
	
	*/
	
--IdPerfilComercialBaseOrigen;IdPerfilProductos;IdPerfilProductosExtra;IdPerfilCobros;IdPerfilCobrosExtra;IdPerfilLimites;IdPerfilLimitesExtra;IdPerfilCondicionesOperativas;IdPerfilCondicionesOperativasExtra;IdEstadoAutorizacion;FechaIngreso;FechaCreacion;FechaModificacion;IdDireccionEnvioCorrespondencia;IdTelefonoContacto
--45.448;742.531;1.355.491;14.965.866;250.000;0;1.355.491;8;;Intermediación;1.176.844;ACT;ACTIVO;14-10-2015 10:59:59.0;OK;?;0;0;0;0;0;0;0;0;0;CON;14-10-2015 10:59:20.0;14-10-2015 10:59:20.0;14-10-2015 11:00:15.0;0;0

/*
insert into #p_Asignaciones
select 1,744219,153929,	27922277,3750000,0 from dummy union all
select 2,744219,153930,	27922277,3750000,0 from dummy union all
select 3,744219,153931,	27922277,3750000,0 from dummy union all
select 4,744219,27499 ,	27922277,3750000,0 from dummy;
*/
select * from #p_Asignaciones;
SET 'DEPURACION' = 'S';
call "BSCL.Orden.BD.Servicios::RefacturarMovimiento" (
p_IdEmpresa => 1
,p_IdUsuario => 9
,p_IdModulo => 'ORD'
,p_UsaReglasNegocio => 'N'
--,p_IdLibroOrdenes => 2
,p_Origen => 'TRAN'
,p_Tipo => 'REASIG'--'REASIG'
,p_Asignaciones => #p_Asignaciones
,p_CobrosModificados =>  #p_CobrosModificados
,p_out_NotasDeCreditoGeneradas => ?
,p_out_ComprobantesAnulados => ?
,p_out_MovimientoComprobanteAnulados => ?
,p_out_AsignacionesAnuladas => ?
,p_out_OrdenesAnuladas => ?
,p_out_OrdenesGeneradas => ?
,p_out_AsignacionesGeneradas => ?
,p_out_MovimientoComprobanteGenerados => ?
,p_out_ComprobantesGenerados => ?
,p_out_AdvertenciasContador => ?
,p_out_AdvertenciasDetalle => ?
,p_out_ErroresContador => ?
,p_out_ErroresDetalle => ?
);


update "BSCL.Facturacion.BD::FAC.Comprobante" set "IdEstadoComprobante"='ENV' where "IdComprobante"=744205

select count(*) from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT')
	and "IdEstado"='ACT';

select * from "BSCL"."BSCL.Portafolio.BD.Vistas::ConsultarPortafolio" 
where "IdCliente" in (
select "IdCliente" from "BSCL"."BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio" where "IdCliente"=249178);
select * from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente"
where "IdCliente" in (
select "IdCliente" from "BSCL"."BSCL.SocioNegocio.BD.Vistas::SOC.UIConsultarSocioDeNegocio") where "IdCliente"=249178);
select * from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCliente"=249178
select "IdCliente",* from "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" where "IdCliente">0 and "EsEnrolado"='S' and "IdEstado"='ACT'

select * from "BSCL.Facturacion.BD.Vistas::ConsultarDetalleMovimientoDeComprobante" where "IdMovimientoComprobante" in (34411,34412);
select * from "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" where "IdMovimientoComprobante" in (34411,34412);
select * from "BSCL.Orden.BD.Vistas::ConsultarAsignacion" where "IdAsignacion" in (190748,190749);
--rollback
--commit
select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoProducto"
select * from "BSCL"."BSCL.Plataforma.BD::INS.TipoOperacion"
select * from bscl."BSCL"."BSCL.Plataforma.BD::PLA.Servicio" where "IdServicio"=38

RutaServicio
call bscl."BSCL.Plataforma.BD.Servicios::COM.ConsultarCobrosProducto"


select * from "BSCL.Plataforma.BD::COM.DetallePerfilProductos"
select max("IdOrden") from "BSCL.Orden.BD::ORD.Orden" where "IdOrden" = 1891596 MAX(IdOrden)

select "BSCL.Orden.BD.Secuencias::Orden".NEXTVAL from dummy;


--ALTER SEQUENCE "BSCL.Orden.BD.Secuencias::Orden" RESTART WITH 1891593
select * from "BSCL.Orden.BD::ORD.Transaccion" where "IdTransaccion" in (3174883,3174884);
select * from "BSCL.Orden.BD::ORD.Orden" where "IdOrden" in (select "IdOrden" from "BSCL.Orden.BD::ORD.Asignacion" where "IdTransaccion" in (3174883,3174884));
select * from "BSCL.Orden.BD::ORD.Asignacion" where "IdTransaccion" in (7683536);
select * from "BSCL.Facturacion.BD::FAC.MovimientoComprobante"where "IdTransaccion" in (4777352);

update "BSCL.Facturacion.BD::FAC.Comprobante" set "IdEstadoComprobante" = 'ENV' where  "IdComprobante" =739865

select * from "BSCL.Facturacion.BD::FAC.Comprobante" where "IdComprobante" =32155









set schema BSCL;
drop table #p_Asignaciones;
drop table #p_Transacciones;
CREATE LOCAL TEMPORARY TABLE #p_Asignaciones(
						"IdTransaccion" 	BIGINT,
						"CantidadAsignada"		DECIMAL(26,8),
						"ValorRescateAsignado"	DECIMAL(26,8)
						);
CREATE LOCAL TEMPORARY TABLE #p_Transacciones(
							"IdTransaccion" 			BIGINT,
							"CantidadRefacturar"		DECIMAL(26,8),
							"ValorRescateRefacturar"	DECIMAL(26,8)
						);
						
insert into #p_Asignaciones ("IdTransaccion","CantidadAsignada","ValorRescateAsignado")
select 42317094,100,0 from dummy union all
select 42317094,1000,0 from dummy;

insert into #p_Transacciones ("IdTransaccion","CantidadRefacturar","ValorRescateRefacturar")
select 42317094,10100,0 from dummy;

select * from #p_Asignaciones;
select * from #p_Transacciones;

CALL "BSCL.Orden.BD.Reglas::ServicioCumpleRequisitosRefacturacion"(1,#p_Asignaciones,#p_Transacciones,?,?);--v_out_SalidaDetalle,v_out_Salida);

