SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica" AS
select     
               -- CAST (ROW_NUMBER () OVER (order by "Modulo" asc) AS BIGINT) as "ID"
                
                
                CAST (Estados."IdEstadoIdentificador" AS BIGINT) as "ID"
                ,CAST(Estados."Modulo" AS NVARCHAR(100)) as "Modulo"
 	                	,CAST(Estados."IdEstado" AS NVARCHAR(3)) as "IdEstado"
 	                 	,CAST(MaestroEstado."Nombre" AS NVARCHAR(100)) as "Nombre"
 	                 	,CAST(Estados."NombreEntidad" AS NVARCHAR(100)) as "NombreEntidad"
		from
		( select 'Portafolio'        as "Modulo", "IdEstadoPortafolio" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"    from "BSCL.Portafolio.BD::POR.EstadoPortafolio"
		UNION 
		select 'CuentaInversion'   as "Modulo", "IdEstadoCuentaInversion" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"    from "BSCL.CuentaInversion.BD::CTA.EstadoCuentaInversion"
		UNION 
		select 'Custodia'       as "Modulo", "IdEstadoCustodia" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"      from "BSCL.Custodia.BD::CUS.EstadoCustodia"
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'INA' as "IdEstado",'Instrumento' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Instrumento' as  "NombreEntidad" from dummy		
		UNION
		select 'Facturacion'     as "Modulo", "IdEstadoFacturacion" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"      from "BSCL.Facturacion.BD::FAC.EstadoFacturacion" 
		UNION 
		select 'FondoMutuo'     as "Modulo", "IdEstadoFondoMutuo" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"      from "BSCL.FondoMutuo.BD::FMM.EstadoFondoMutuo" 
		UNION 
		select 'Mesa'        as "Modulo", "IdEstadoMesa" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"        from "BSCL.Mesa.BD::MES.EstadoMesa" 
		UNION 
		select "Modulo",CAST (ROW_NUMBER () OVER (order by "Modulo" asc) AS BIGINT) as "IdEstadoIdentificador","IdEstado", "NombreEntidad"
		from 
		(select DISTINCT "IdTipoProducto"    as "Modulo",  "IdEstado", "NombreEntidad"     from "BSCL.Mesa.BD::MES.EstadoOperacion" )				
		UNION 
		select 'Orden'         as "Modulo", "IdEstadoOrden" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"        from "BSCL.Orden.BD::ORD.EstadoOrden" 
		UNION 
		select 'PerfilComercial'  as "Modulo", "IdEstadoPerfilComercial" as "IdEstadoIdentificador", "IdEstado", "Entidad"      from "BSCL.Plataforma.BD::COM.EstadoPerfilComercial" 
		UNION 
		select 'SocioDeNegocio'    as "Modulo", "IdEstadoSocioDeNegocio" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"    from "BSCL.SocioNegocio.BD::SOC.EstadoSocioDeNegocio" 
		UNION 
		select 'Tesoreria'      as "Modulo", "IdEstadoTesoreria" as "IdEstadoIdentificador", "IdEstado", "NombreEntidad"      from "BSCL.Tesoreria.BD::TES.EstadoTesoreria"
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Empresa' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Empresa' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'ClasificacionTipoInstrumento' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'ClasificacionTipoInstrumento' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'ModeloComercial' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'ModeloComercial' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'PerfilCobro' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'PerfilCobro' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Profesion' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Profesion' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'VIG' as "IdEstado",'TipoProducto' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'VCD' as "IdEstado",'TipoProducto' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'ValorCobro' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'ValorCobro' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'DetallePerfilCobro' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'DetallePerfilCobro' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'DetallePerfilProducto' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'DetallePerfilProducto' as  "NombreEntidad" from dummy
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'TipoRelacion' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'TipoRelacion' as  "NombreEntidad" from dummy			
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Paridad' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Paridad' as  "NombreEntidad" from dummy			
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'TipoIndicador' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'TipoIndicador' as  "NombreEntidad" from dummy			
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'PEN' as "IdEstado",'DTE' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'ENV' as "IdEstado",'DTE' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",3 as "IdEstadoIdentificador",'REC' as "IdEstado",'DTE' as  "NombreEntidad" from dummy			
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'PEN' as "IdEstado",'SII' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'ENV' as "IdEstado",'SII' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",3 as "IdEstadoIdentificador",'REC' as "IdEstado",'SII' as  "NombreEntidad" from dummy			
        UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'VIG' as "IdEstado",'Concepto' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Concepto' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",3 as "IdEstadoIdentificador",'INA' as "IdEstado",'Concepto' as  "NombreEntidad" from dummy			
        UNION
		select 'Mesa' as "Modulo",1 as "IdEstadoIdentificador",'ANU' as "IdEstado",'Spot' as  "NombreEntidad" from dummy
		UNION 
		select 'Mesa' as "Modulo",2 as "IdEstadoIdentificador",'LIQ' as "IdEstado",'Spot' as  "NombreEntidad" from dummy			
		UNION 
		select 'Mesa' as "Modulo",3 as "IdEstadoIdentificador",'PEN' as "IdEstado",'Spot' as  "NombreEntidad" from dummy			
        UNION 
		select 'Mesa' as "Modulo",4 as "IdEstadoIdentificador",'VIG' as "IdEstado",'Spot' as  "NombreEntidad" from dummy			
		UNION
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'MedioPago' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'MedioPago' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'VIG' as "IdEstado",'TipoInstrumento' as  "NombreEntidad" from dummy		
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Banco' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Banco' as  "NombreEntidad" from dummy			
		UNION 
		select 'Plataforma' as "Modulo",3 as "IdEstadoIdentificador",'BLO' as "IdEstado",'Banco' as  "NombreEntidad" from dummy		
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Perfil' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Perfil' as  "NombreEntidad" from dummy		
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Rol' as  "NombreEntidad" from dummy
		UNION 
		select 'Plataforma' as "Modulo",2 as "IdEstadoIdentificador",'INA' as "IdEstado",'Rol' as  "NombreEntidad" from dummy	
		UNION 
		select 'Plataforma' as "Modulo",1 as "IdEstadoIdentificador",'ACT' as "IdEstado",'Usuario' as  "NombreEntidad" from dummy	
		UNION 
		select 'Plataforma' as "Modulo",3 as "IdEstadoIdentificador",'BLO' as "IdEstado",'Usuario' as  "NombreEntidad" from dummy		
		               ) as Estados
		left join "BSCL.Plataforma.BD::PLA.Estado" as MaestroEstado
		ON  MaestroEstado."IdEstado" = Estados."IdEstado";

GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstadoAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 