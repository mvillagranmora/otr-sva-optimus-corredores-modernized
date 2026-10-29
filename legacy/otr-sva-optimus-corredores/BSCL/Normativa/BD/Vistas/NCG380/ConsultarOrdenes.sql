SET SCHEMA "BSCL"; 

DROP VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarOrdenes";

CREATE VIEW "BSCL.Normativa.BD.Vistas::NCG380.ConsultarOrdenes"
as
select  
"ORD"."IdOrden" as "NumeroOrden",
"ORD"."IdCliente" as "NumeroCliente" ,
"ORD"."IdPortafolio" as "NumeroCuenta",
"CLI"."IdPersona" as "NumeroPersonaOrden",
"ORD"."IdEmpleadoPortafolio" as "NumeroEjecutivo",
(
select "DET"."DetalleIndicador" from 
"BSCL.Plataforma.BD::IND.Indicador" as "IND" 
left join "BSCL.Plataforma.BD::IND.DetalleIndicador" as "DET" 
on ("DET"."IdTipoIndicador" = "IND"."IdTipoIndicador") 
and  ("IND"."ValorIndicador" = TO_NVARCHAR("DET"."IdDetalleIndicador")) 
where ("IND"."IdEntidad" = "CLI"."IdCliente"  ) 
and "IND"."IdTipoIndicador" 
in 
(select "IdTipoIndicador" 
from "BSCL.Plataforma.BD::IND.TipoIndicador" 
where "CodigoTipoIndicador" = 'CLASEORDEN')
 ) as "ClaseOrden",
-- ( select "DET"."DetalleIndicador" from  "BSCL.Plataforma.BD::IND.Indicador" AS "IND"  left join "BSCL.Plataforma.BD::IND.DetalleIndicador" "DET"  on ("DET"."IdTipoIndicador" = "IND"."IdTipoIndicador")  and ("IND"."ValorIndicador" = TO_NVARCHAR("DET"."IdDetalleIndicador"))  where ("IND"."IdEntidad" = "CLI"."IdCliente")  and "IND"."IdTipoIndicador"  in  (select "IdTipoIndicador"  from "BSCL.Plataforma.BD::IND.TipoIndicador"  where "CodigoTipoIndicador" = 'CLASEORDEN') ) AS ClaseOrden,
"ORD"."IdTipoOperacion" as "TipoOrden",
"INS"."ISIN" as "Isin",
"ORD"."Instrumento" as "Nemotecnico" ,
"ORD"."NombreTipoInstrumento" as "TipoInstrumento",
"ORD"."CantidadAsignadaOrden" as "Cantidad", -- o "CantidadDisponible"?
"ORD"."MontoAsignadoOrden" as "Monto",
"ORD"."IdMonedaOrden" as "Moneda", -- o moneda de instrumento?
"ORD"."PrecioOrden" as "Precio" , --ojo con ordenes con precio limite, estas poseen una condicion a implementar (ver requerimiento y asociar con campo TipoPrecio)
"ORD"."Plazo" as "PlazoValidez",
"ORD"."IdBolsa" as "PreferenciaBolsa",
'Preferencia Mecanismo' as "PreferenciaMecanismo",
"ORD"."FechaRecepcion" as "FechaRecepcion" ,
SUBSTR_AFTER((ltrim(rtrim("ORD"."FechaRecepcion"))),' ') as "HoraRecepcion",
"ORD"."FechaOperacionOrden" as "FechaAceptacion"  , 
SUBSTR_AFTER((ltrim(rtrim("ORD"."FechaOperacionOrden"))),' ') as "HoraAceptacion",
IFNULL("IdPersonaAutorizadoDarOrdenes",0) as NumeroPersona , --asociar con tabla persona
case when "ORD"."IdEstadoOrden" = 'ANU' then 1
else 0
end 
as "Anulacion" , --analizar condicion , si existe fecha anulacion, poner 1 (orden anulada), caso contrario poner 0 (orden no anulada)
0 as "NumeroOrdenReemplazo" ,
"ORD"."FechaAnulacion"  as "FechaAnulacion" ,
"ORD"."GlosaOrden" as "Observaciones", -- buscar campo equivalente
(ltrim(rtrim(substring("CLI"."DocumentoIdentidad",1,LENGTH("CLI"."DocumentoIdentidad")-2))))  as "DocumentoIdentidadRut",
"FolioOrden" as "folio",
"IdEstadoOrden" as "Estado",
"ORD"."IdClasificacionTipoInstrumento" as "IdClasificacion"
from  "BSCL.Orden.BD.Vistas::ConsultarOrden" as "ORD"
left join "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as "CLI" on "ORD"."IdCliente" = "CLI"."IdCliente" 
left join "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento" as "INS" on "ORD"."IdInstrumento" = "INS"."IdInstrumento" 
;

GRANT SELECT ON "BSCL.Normativa.BD.Vistas::NCG380.ConsultarOrdenes" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 