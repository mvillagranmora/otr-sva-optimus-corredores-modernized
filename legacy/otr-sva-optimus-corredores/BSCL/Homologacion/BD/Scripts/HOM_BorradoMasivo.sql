SET SCHEMA BSCL;

-- script de limpieza de datos de carga masiva 
--limpiar todas las operaciones 
delete from "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" where "IdCarga" = 88;
delete from "BSCL.Facturacion.BD::FAC.ComprobanteDetalle" where "IdCarga" = 88;
delete from "BSCL.Facturacion.BD::FAC.Comprobante" where "IdCarga" = 88;
delete from "BSCL.Facturacion.BD::FAC.MovimientoComprobante"  where "IdCarga" = 88;

delete from  "BSCL.Orden.BD::ORD.CobroAsignacion"
where "IdAsignacion" in (select "IdAsignacion" from "BSCL.Orden.BD::ORD.Asignacion"
                         where "IdCarga" = 88);

--borramos asignacion
delete from "BSCL.Homologacion.BD::HOM.Asignacion" where "IdCarga" = 88;
delete from "BSCL.Orden.BD::ORD.Asignacion" where "IdCarga" = 88;

delete from "BSCL.Orden.BD::ORD.CobroOrden" 
where "IdOrden" in (select "IdOrden" from "BSCL.Orden.BD::ORD.Orden"
                         where "IdCarga" = 88);

-- borramos ordenes
delete from "BSCL.Homologacion.BD::HOM.Orden" where "IdCarga" = 88;
delete from "BSCL.Orden.BD::ORD.Orden" where "IdCarga" = 88;

-- borramos transaccion
delete from "BSCL.Homologacion.BD::HOM.Transaccion" where "IdCarga" = 88;
delete from "BSCL.Orden.BD::ORD.Transaccion" where "IdCarga" = 88;

-- borramos contrapartes
delete from "BSCL.Homologacion.BD::HOM.Contraparte" where "IdCarga" = 88;

--borramos aquellos socios de negocios provenientes de empleados
delete from "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
where "IdEmpleado" in (select "IdEmpleado" from "BSCL.SocioNegocio.BD::EMP.Empleado"
					   where "IdCarga" = 88);

--borramos empleados 
delete from "BSCL.Homologacion.BD::HOM.Empleado"; 
delete from  "BSCL.SocioNegocio.BD::EMP.Empleado"  where "IdCarga" = 88;

-- borramos los movimientos de cargo y abonos
delete from "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento"
where "IdCuentaInversion" in (select "IdCuentaInversion" 
                               from "BSCL.CuentaInversion.BD::CTA.CuentaInversion"
                               where "IdCarga" = 88);

--borramos las cuentas de inversion 
delete  from "BSCL.CuentaInversion.BD::CTA.CuentaInversion" where "IdCarga" = 88 ;

--borramos las anotaciones del agente
delete from "BSCL.Portafolio.BD::POR.Anotacion"
where "IdPortafolio" in (select "IdPortafolio" from "BSCL.Portafolio.BD::POR.Portafolio"
                         where "IdCarga"=88);  
                         
-- borramos los indicadores                         
delete from "BSCL.Plataforma.BD::IND.Indicador"
where "IdEntidad" in (select "IdPortafolio" from "BSCL.Portafolio.BD::POR.Portafolio" as por
                      inner join  "BSCL.SocioNegocio.BD::CLI.Cliente" as cli
                      ON cli."IdCliente" = por."IdCliente" 
					  where cli."IdCarga" = 88)
and "IdTipoEntidad" = 'PORTAFOLIO';

delete from "BSCL.Plataforma.BD::IND.Indicador" 
where "IdEntidad" in (select "IdCliente" from  "BSCL.SocioNegocio.BD::CLI.Cliente" as cli
                      where cli."IdCarga" = 88)
and "IdTipoEntidad" = 'CLIENTE';    
               
-- borramos los portafolios
delete from "BSCL.Portafolio.BD::POR.Portafolio"
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD::CLI.Cliente"
					  where "IdCarga" = 88); 

-- borramos los documentos
delete from "BSCL.Plataforma.BD::DOC.Documento"
where "IdCliente" in (select "IdCliente" from "BSCL.SocioNegocio.BD::CLI.Cliente"
					  where "IdCarga" = 88); 

-- borramos los indicadores cliente
delete from "BSCL.Plataforma.BD::IND.Indicador"
where "IdEntidad" in (select "IdCliente" from "BSCL.SocioNegocio.BD::CLI.Cliente"
					  where "IdCarga" = 88)
and "IdTipoEntidad" = 'CLIENTE';
 

-- borramos el cliente
delete from "BSCL.SocioNegocio.BD::CLI.Cliente" where "IdCarga" = 88;

-- borramos los datos asociados a la persona -- direcciones
delete from "BSCL.SocioNegocio.BD::PER.DireccionFisica"
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);

delete
from  "BSCL"."BSCL.SocioNegocio.BD::PER.DireccionElectronica"
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);
                      
-- borramos los telefonos 
delete 
from  "BSCL.SocioNegocio.BD::PER.Telefono"
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);
                      
---borramos los socios de negocios 
delete from "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio"
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);
          
          
delete from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);         
                
delete from "BSCL.SocioNegocio.BD::PER.NombreFantasia"      
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);

delete from "BSCL.SocioNegocio.BD::REL.RelacionPersona"
where "IdPersonaOrigen" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);
                                       
delete from "BSCL.SocioNegocio.BD::REL.RelacionPersona"
where "IdPersonaDestino" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);  
                      
--borramos la contraparte
delete from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" 
where "IdPersona" in (select "IdPersona" from "BSCL.SocioNegocio.BD::PER.Persona"
                      where "IdCarga" = 88);                     
                           
-- borramos la persona
delete from "BSCL.SocioNegocio.BD::PER.Persona" where "IdCarga" = 88;




